package nl.atcomputing.examtrainer.activities;

import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import android.content.Context;
import android.database.Cursor;


/**
 * @author martijn brekhof
 *
 */
public class Exam {
	private String title;
	private String category;
	private String author;
	private String URL;
	private String courseURL;
	private int numberofitems;
	private int itemsneededtopass;
	private long timelimit;
	private long installationDate;
	private String installationState;
	private long examID;
	
	public static Exam newInstance(Context context, long examID) {
		Exam exam = new Exam();
		
		exam.setExamID(examID);
		
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(context);
		examTrainerDbHelper.open();
		Cursor cursor = examTrainerDbHelper.getExam(examID);
		
		int index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE);
		exam.setTitle(cursor.getString(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_CATEGORY);
		exam.setCategory(cursor.getString(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AUTHOR);
		exam.setAuthor(cursor.getString(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_URL);
		exam.setURL(cursor.getString(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_COURSEURL);
		exam.setCourseURL(cursor.getString(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AMOUNTOFITEMS);
		exam.setNumberOfItems(cursor.getInt(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS);
		exam.setItemsNeededToPass(cursor.getInt(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_TIMELIMIT);
		exam.setTimeLimit(cursor.getLong(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_DATE);
		exam.setInstallationDate(cursor.getLong(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED);
		exam.setInstallationState(cursor.getString(index));
		
		cursor.close();
		examTrainerDbHelper.close();
		
		return exam;
	}
	
	public Exam() {
		title = null;
		category = null;
		author = null;
		URL = null;
		courseURL = null;
		numberofitems = 0;
		itemsneededtopass = 0;
		timelimit = 0;
		installationDate = 0;
		examID = 0;
	}
	
	public long getExamID() {
		return examID;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getCategory() {
		return category;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getURL() {
		return URL;
	}
	
	public String getCourseURL() {
		return courseURL;
	}
	
	public int getNumberOfItems() {
		return numberofitems;
	}
	
	public int getItemsNeededToPass() {
		return itemsneededtopass;
	}
	
	public long getTimeLimit() {
		return timelimit;
	}
	
	public long getInstallationDate() {
		return installationDate;
	}
	
	public String getInstallationState() {
		return installationState;
	}
	
	public void setExamID(long examID) {
		this.examID = examID;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public void setURL(String url) {
		this.URL = url;
	}
	
	public void setCourseURL(String url) {
		this.courseURL = url;
	}
	
	public void setNumberOfItems( int n ) {
		this.numberofitems = n;
	}
	
	public void setItemsNeededToPass( int n ) {
		this.itemsneededtopass = n;
	}
	
	public void setTimeLimit(long t) {
		this.timelimit = t;
	}
	
	public void setInstallationDate(long epoch) {
		this.installationDate = epoch;
	}
	
	public void setInstallationState(String state) {
		this.installationState = state;
	}
	
	public long addToDatabase(Context context) {
		
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(context);
		examTrainerDbHelper.open();
		
		long rowId = examTrainerDbHelper.addExam(this);

		examTrainerDbHelper.close();
		
		return rowId;
	}
}