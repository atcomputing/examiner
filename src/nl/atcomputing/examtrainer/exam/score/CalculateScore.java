package nl.atcomputing.examtrainer.exam.score;

import java.util.Random;

import nl.atcomputing.examtrainer.ExamTrainer;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.exam.ExamQuestion;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
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
		private static final Random randomNumberGenerator = new Random();
		
		private int itemsNeededToPass;
		private int amountOfItems;
		private int progress;
		
		CalculateScore(ShowScoreActivity context) {
			this.showScoreActivity = context;
		}
		
		protected void setContext(Context context) {
			this.dialog = new ProgressDialog(context);
			dialog.setMessage(dialogMessage);
			dialog.setMax(amountOfItems);
			dialog.setProgress(progress);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.show();
		}
		
		protected void onPreExecute() {
//			itemsNeededToPass = (int) ExamTrainer.getItemsNeededToPass();
//			amountOfItems = (int) ExamTrainer.getAmountOfItems();
			
			itemsNeededToPass = 44;
			amountOfItems = 65;
			progress = 0;
			
			Resources resource = showScoreActivity.getResources();
			dialog = new ProgressDialog(showScoreActivity);
			dialogMessage = resource.getString(R.string.Calculating_your_score);
			dialog.setMessage(dialogMessage);
			dialog.setMax(amountOfItems);
			dialog.setProgress(progress);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.show();
			
			messagesClassA = resource.getStringArray(R.array.class_A);
			messagesClassB = resource.getStringArray(R.array.class_B);
			messagesClassC = resource.getStringArray(R.array.class_C);
			messagesClassD = resource.getStringArray(R.array.class_D);
			messagesClassE = resource.getStringArray(R.array.class_E);
			messagesClassF = resource.getStringArray(R.array.class_F);
		}
		
		protected Integer doInBackground(Object... questionIds) {
			
			ExaminationDbAdapter examinationDbHelper;
			examinationDbHelper = new ExaminationDbAdapter(this.showScoreActivity);
			try {
				examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
			} catch (SQLiteException e) {
				throw(e);
			}

			int amountOfQuestions = questionIds.length;
			int answers_correct = 0;

			for(int i = 0; i < amountOfQuestions; i++) {
				long questionId = (Long) questionIds[i];

				String questionType = examinationDbHelper.getQuestionType(questionId);
				boolean answerCorrect = false;
				if( questionType.equalsIgnoreCase(ExamQuestion.TYPE_OPEN) ) {
					answerCorrect = examinationDbHelper.checkScoresAnswersOpen(questionId, ExamTrainer.getScoresId());
				}
				else {
					answerCorrect = examinationDbHelper.checkScoresAnswersMultipleChoice(questionId, ExamTrainer.getScoresId());
				}

				if ( answerCorrect ) {
					answers_correct++;
					examinationDbHelper.addResultPerQuestion(ExamTrainer.getScoresId(), questionId, true);
				}
				else {
					examinationDbHelper.addResultPerQuestion(ExamTrainer.getScoresId(), questionId, false);
				}
				
				
				//publishProgress(i, answers_correct);
				
			}
			examinationDbHelper.close();
			
			for (int i = 0; i < amountOfItems; i++) {
				progress = i;
				publishProgress(progress, progress-randomNumberGenerator.nextInt(30));
				try {
					Thread.sleep((long) (100 + ((i/(double) amountOfItems) * 200)));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			return answers_correct;
		}
		
		protected void onProgressUpdate(Integer... progress) {
			dialog.incrementProgressBy(1);
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
				Log.d("CalculateScore", e.getMessage());
			}
			
			dialog.dismiss();
			
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