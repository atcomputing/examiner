package nl.atcomputing.examtrainer;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;

public class CalculateScore extends AsyncTask<Object, Integer, Integer> {
		private Context context;
		private ShowScoreView showScoreView;
		private ProgressDialog dialog;
		
		CalculateScore(Context context, ShowScoreView showScoreView) {
			this.context = context;
			this.showScoreView = showScoreView;
		}
		
		protected void onPreExecute() {
			dialog = new ProgressDialog(context);
			dialog.setMessage(context.getResources().getString(R.string.Calculating_your_score));
			//dialog.setMax((int) ExamTrainer.getAmountOfItems());
			dialog.setMax(100);
			dialog.setProgress(0);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.show();
		}
		
		protected Integer doInBackground(Object... questionIds) {
			
			ExaminationDbAdapter examinationDbHelper;
			examinationDbHelper = new ExaminationDbAdapter(context);
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
					answerCorrect = examinationDbHelper.checkScoresAnswersOpen(questionId, ExamTrainer.getExamId());
				}
				else {
					answerCorrect = examinationDbHelper.checkScoresAnswersMultipleChoice(questionId, ExamTrainer.getExamId());
				}

				if ( answerCorrect ) {
					answers_correct++;
					examinationDbHelper.addResultPerQuestion(ExamTrainer.getExamId(), questionId, true);
				}
				else {
					examinationDbHelper.addResultPerQuestion(ExamTrainer.getExamId(), questionId, false);
				}
				
				//publishProgress(answers_correct);
				
			}
			for (int i = 0; i < 100; i++) {
				publishProgress(i);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			return answers_correct;
		}
		
		protected void onProgressUpdate(Integer... score) {
			dialog.incrementProgressBy(1);
			updateText(score[0]);
	     }
		
		protected void onPostExecute(Integer answersCorrect) {
			this.showScoreView.setScore(answersCorrect);
			
			//Update score in database
			ExaminationDbAdapter examinationDbHelper;
			examinationDbHelper = new ExaminationDbAdapter(context);
			try {
				examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
				examinationDbHelper.updateScore(ExamTrainer.getExamId(), answersCorrect);
				examinationDbHelper.close();
			} catch (SQLiteException e) {
				Log.d("CalculateScore", e.getMessage());
			}
			
			dialog.dismiss();
			
			this.showScoreView.showResult();
		}
		
		protected void updateText(int score) {
			/**
			 * percNeeded = ExamTrainer.getItemsNeededToPass() / ExamTrainer.getAmountOfItems()
			 * baseLinePass = percNeeded * itemsCalculated;
			 * if( score >= baseLinePass ) {
			 * 	if ( percCalculated > 80% ) {
			 * 		msg = "I am so confident you will pass I'll give you a balloon right now"
			 *	} else if ( percCalculated > 60% ) {
			 *		msg = "This is really looking good"
			 *	} else if ( percCalculated > 40% ) {
			 *		msg = "Looking good"
			 *	} else if ( percCalculated > 20% ) {
			 *		msg = "At this rate you will pass"
			 *	}
			 * } else {
			 * 	if ( percCalculated > 80% ) {
			 * 		msg = "This is bad. You will need a miracle to pass."
			 *	} else if ( percCalculated > 60% ) {
			 *		msg = "Not looking good at all."
			 *	} else if ( percCalculated > 40% ) {
			 *		msg = "Not looking good."
			 *	} else if ( percCalculated > 20% ) {
			 *		msg = "It's gonna be exciting"
			 *	}
			 * }
			 * 
			 * gnuplot function:
			 * plot [x=1:65] f(x) = x*(65/44) - 65 + x, f(x), g(x) = x*(65/44), g(x), h(x) = (x*44)/65, h(x)
			 */
			
			int itemsNeeded = (int) ExamTrainer.getItemsNeededToPass();
			if( score > itemsNeeded ) {
				
			}
		}
	}