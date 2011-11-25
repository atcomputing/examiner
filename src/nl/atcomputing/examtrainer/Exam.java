package nl.atcomputing.examtrainer;

import java.util.ArrayList;

import android.database.sqlite.SQLiteException;

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
	
	protected Exam() {
		title = null;
		category = null;
		author = null;
		URL = null;
		numberofitems = 0;
		itemsneededtopass = 0;
	}
	
	protected String getTitle() {
		return title;
	}
	
	protected String getCategory() {
		return category;
	}
	
	protected String getAuthor() {
		return author;
	}
	
	protected String getURL() {
		return URL;
	}
	
	protected int getNumberOfItems() {
		return numberofitems;
	}
	
	protected int getItemsNeededToPass() {
		return itemsneededtopass;
	}
	
	protected void setTitle(String title) {
		this.title = title;
	}
	
	protected void setCategory(String category) {
		this.category = category;
	}
	
	protected void setAuthor(String author) {
		this.author = author;
	}
	
	protected void setURL(String url) {
		this.URL = url;
	}
	
	protected void setNumberOfItems( int n ) {
		this.numberofitems = n;
	}
	
	protected void setItemsNeededToPass( int n ) {
		this.itemsneededtopass = n;
	}
	
	protected void addToDatabase(ExamTrainerDbAdapter examTrainerDbHelper) {
		long questionId = examTrainerDbHelper.addExam(title, "", itemsneededtopass, numberofitems, false, URL);
	}
}