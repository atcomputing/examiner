/**
 * 
 * Copyright 2012 AT Computing BV
 *
 * This file is part of Examiner.
 *
 * Examiner is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Examiner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Examiner.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.atcomputing.examtrainer.scorecalculation;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.main.ExamTrainer;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author martijn brekhof
 *
 */

public class CalculateScore extends AsyncTask<Object, Integer, Integer> {
	private ShowScoreActivity showScoreActivity;
	private ProgressDialog dialog;
	private String dialogMessage;
	private String[] messagesClassA;
	private String[] messagesClassB;
	private String[] messagesClassC;
	private String[] messagesClassD;
	private String[] messagesClassE;
	private String[] messagesClassF;

	private int itemsNeededToPass;
	private int amountOfItems;
	private int progress;
	private boolean showScoreInstantaneously;

	CalculateScore(ShowScoreActivity context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		this.showScoreInstantaneously = prefs.getBoolean(context.getResources().getString(R.string.pref_key_show_score_instantaneous), false);

		this.showScoreActivity = context;
		this.progress = 0;
		this.itemsNeededToPass = (int) ExamTrainer.getItemsNeededToPass();
		this.amountOfItems = (int) ExamTrainer.getAmountOfItems();
	}

	protected void setContext(ShowScoreActivity context) {

		this.showScoreActivity = context;

		this.dialog = new ProgressDialog(context);
		this.dialog.setMessage(dialogMessage);
		this.dialog.setMax(amountOfItems);
		this.dialog.setProgress(progress);
		this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		this.dialog.show();
	}

	protected void onPreExecute() {
		if( ! this.showScoreInstantaneously ) {
			Resources resource = showScoreActivity.getResources();

			this.dialog = new ProgressDialog(showScoreActivity);
			this.dialogMessage = resource.getString(R.string.Calculating_your_score);
			this.dialog.setMessage(dialogMessage);
			this.dialog.setMax(amountOfItems);
			this.dialog.setProgress(progress);
			this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			this.dialog.show();

			this.messagesClassA = resource.getStringArray(R.array.class_A);
			this.messagesClassB = resource.getStringArray(R.array.class_B);
			this.messagesClassC = resource.getStringArray(R.array.class_C);
			this.messagesClassD = resource.getStringArray(R.array.class_D);
			this.messagesClassE = resource.getStringArray(R.array.class_E);
			this.messagesClassF = resource.getStringArray(R.array.class_F);
		}
	}

	protected Integer doInBackground(Object... questionIds) {

		ExaminationDbAdapter examinationDbHelper;
		examinationDbHelper = new ExaminationDbAdapter(this.showScoreActivity);
		try {
			examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		} catch (SQLiteException e) {
			throw(e);
		}

		int answers_correct = 0;
		int amountOfQuestions = questionIds.length;
		if( ( this.showScoreInstantaneously ) || (this.amountOfItems < 15) ) {
			answers_correct = examinationDbHelper.calculateScore(ExamTrainer.getScoresId());
		} else {
			long scoresId = ExamTrainer.getScoresId();
			long delay = 5000 / this.amountOfItems;
			for(int i = 0; i < amountOfQuestions; i++) {
				long questionId = (Long) questionIds[i];
				
				boolean answerCorrect = false;
				answerCorrect = examinationDbHelper.getResult(scoresId, questionId) == 1;

				if( answerCorrect ) {
					answers_correct++;
				}

				publishProgress(i, answers_correct);

				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		examinationDbHelper.close();

		return answers_correct;
	}

	protected void onProgressUpdate(Integer... progress) {
		dialog.setProgress(progress[0]);
		if ( progress[0] % 10 == 0 ) {
			updateText(progress[0], progress[1]);
		}
	}

	protected void onPostExecute(Integer answersCorrect) {

		//Update score in database
		ExaminationDbAdapter examinationDbHelper;
		examinationDbHelper = new ExaminationDbAdapter(this.showScoreActivity);
		try {
			examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
			examinationDbHelper.updateScore(ExamTrainer.getScoresId(), answersCorrect);
			examinationDbHelper.close();
		} catch (SQLiteException e) {
			Log.w("CalculateScore", e.getMessage());
		}

		if( ! this.showScoreInstantaneously ) {
			dialog.dismiss();
		}

		this.showScoreActivity.showResult();
	}

	protected void updateText(int itemsCalculated, int score) {
		/**
		 * gnuplot function:
		 * itemsNeededToPass = 44
		 * amountOfItems = 65
		 * plot [x=1:65] f(x) = ((x*44)/65) - 65 + x, f(x), g(x) = x*(65/44), g(x), h(x) = (x*44)/65, h(x)
		 * 
		 * f(x): bottom line below which it will be impossible to pass
		 * h(x): line above which you may pass
		 * g(x): best case scenario, all questions correct 
		 * 
		 * Class A: score > itemsNeededToPass
		 * 			messagesClassA[ x / ((amountOfItems / amountOfMessagesInClassA) + 1)  ]
		 * Class B: ( score - h(x) ) > ( g(x) - score )
		 * 			messagesClassB[ x / ((amountOfItems / amountOfMessagesInClassB) + 1)  ]
		 * Class C: ( score - h(x) ) < ( g(x) - score )
		 * 			messagesClassC[ x / ((amountOfItems / amountOfMessagesInClassC) + 1)  ]
		 * Class D: ( score - f(x) ) > ( h(x) - score )
		 * 			messagesClassD[ x / ((amountOfItems / amountOfMessagesInClassD) + 1)  ]
		 * Class E: ( score - f(x) ) < ( h(x) - score )
		 * 			messagesClassE[ x / ((amountOfItems / amountOfMessagesInClassE) + 1)  ]
		 * Class F:  score < f(x)
		 * 			messagesClassF[ x / ((amountOfItems / amountOfMessagesInClassF) + 1)  ]
		 */

		int fx = ((itemsCalculated * itemsNeededToPass) / amountOfItems) - amountOfItems + itemsCalculated;
		int gx = itemsCalculated * (amountOfItems/itemsNeededToPass);
		int hx = (itemsCalculated * itemsNeededToPass) / amountOfItems;

		if( score > hx ) {
			if( score > itemsNeededToPass ) {
				dialogMessage = messagesClassA[ itemsCalculated / ((amountOfItems / messagesClassA.length) + 1)  ];
			} else if ( ( score - hx ) > ( gx - score ) ) {
				dialogMessage = messagesClassB[ itemsCalculated / ((amountOfItems / messagesClassB.length) + 1)  ];
			} else {
				dialogMessage = messagesClassC[ itemsCalculated / ((amountOfItems / messagesClassC.length) + 1)  ];
			}
		} else {
			if ( score < fx ) {
				dialogMessage = messagesClassF[ itemsCalculated / ((amountOfItems / messagesClassF.length) + 1)  ];
			} else if ( ( score - fx ) > ( hx - score ) ) {
				dialogMessage = messagesClassD[ itemsCalculated / ((amountOfItems / messagesClassD.length) + 1)  ];
			} else {
				dialogMessage = messagesClassE[ itemsCalculated / ((amountOfItems / messagesClassE.length) + 1)  ];
			}
		}

		dialog.setMessage(dialogMessage);
	}
}