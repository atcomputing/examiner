package nl.atcomputing.examtrainer;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

public class CalculateScore extends AsyncTask<Object, Integer, Integer> {
		private Context context;
		private ShowScoreView showScoreView;
		private TextView textView;
		
		CalculateScore(Context context, ShowScoreView showScoreView) {
			this.context = context;
			this.showScoreView = showScoreView;
			this.textView = showScoreView.getShowScoreTextView();
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
				
				//publishProgress((int) answers_correct);
				publishProgress(answers_correct);
				
			}
			for (int i = 0; i < 1000; i++) {
				publishProgress(i);
			}
			
			return answers_correct;
		}
		
		protected void onProgressUpdate(Integer... score) {
	        this.showScoreView.setScore(score[0]);
	     }
		
		protected void onPostExecute(Long answersCorrect) {
			this.showScoreView.setScore(answersCorrect.intValue());
			
			//Update score in database
			ExaminationDbAdapter examinationDbHelper;
			examinationDbHelper = new ExaminationDbAdapter(context);
			try {
				examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
				examinationDbHelper.updateScore(ExamTrainer.getExamId(), answersCorrect);
				examinationDbHelper.close();
			} catch (SQLiteException e) {
				throw(e);
			}
		}
	}