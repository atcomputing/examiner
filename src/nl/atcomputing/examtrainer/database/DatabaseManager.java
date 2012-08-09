package nl.atcomputing.examtrainer.database;

import nl.atcomputing.examtrainer.R;
import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

public class DatabaseManager {
	private Context context;
	
	public DatabaseManager(Context context) {
		this.context = context;
	}
	
	public void deleteExam(long examID) {
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this.context);
		examTrainerDbHelper.open();
		
		Cursor cursor = examTrainerDbHelper.getExam(examID);
		int index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE);
		String examTitle = cursor.getString(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_DATE);
		Long examDate = cursor.getLong(index);
		
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this.context);
		
		if( examinationDbHelper.delete(examTitle, examDate) )  {
			if( ! examTrainerDbHelper.setInstallationState(examID, ExamTrainerDbAdapter.State.NOT_INSTALLED) ) {
				Toast.makeText(this.context, this.context.getString(R.string.Failed_to_uninstall_exam) + 
						examTitle, Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(context, context.getString(R.string.Could_not_remove_exam_database_file) + 
					examTitle, Toast.LENGTH_LONG).show();
		}
		examTrainerDbHelper.close();
	}
}
