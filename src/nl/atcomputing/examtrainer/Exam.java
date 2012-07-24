package nl.atcomputing.examtrainer;

import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import android.content.Context;


/**
 * @author martijn brekhof
 *
 */
public class Exam {
	private String title;
	private String category;
	private String author;
	private String URL;
	private int numberofitems;
	private int itemsneededtopass;
	private long timelimit;
	
	public Exam() {
		title = null;
		category = null;
		author = null;
		URL = null;
		numberofitems = 0;
		itemsneededtopass = 0;
		timelimit = 0;
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
	
	public int getNumberOfItems() {
		return numberofitems;
	}
	
	public int getItemsNeededToPass() {
		return itemsneededtopass;
	}
	
	public long getTimeLimit() {
		return timelimit;
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
	
	public void setNumberOfItems( int n ) {
		this.numberofitems = n;
	}
	
	public void setItemsNeededToPass( int n ) {
		this.itemsneededtopass = n;
	}
	
	public void setTimeLimit(long t) {
		this.timelimit = t;
	}
	
	public long addToDatabase(Context context) {
		
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(context);
		examTrainerDbHelper.open();
		
		long rowId = examTrainerDbHelper.addExam(this);

		examTrainerDbHelper.close();
		
		return rowId;
	}
}