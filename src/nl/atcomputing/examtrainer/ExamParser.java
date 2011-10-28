package nl.atcomputing.examtrainer;

import android.database.sqlite.SQLiteException;


public interface ExamParser {

	/**
	 * Adds an exam from a file or URL to the ExamTrainer database
	 * Note that this does not install the exam
	 * @return true if exam was succesfully added, false otherwise
	 * @throws SQLiteException
	 */
	boolean addExamToExamTrainer();

	
	/**
	 * Installs exam available in ExamTrainer database from URL
	 * This creates a new database file with name title-date
	 * @return true if installation succeeded, false otherwise
	 * @throws SQLiteException
	 */
	void installExam(String title, String date) throws SQLiteException;
	
	/**
	 * Checks if exam is already in the database
	 * @return true if exam exists in database, false otherwise
	 * @throws SQLiteException
	 */
	boolean checkIfExamInDatabase();
}