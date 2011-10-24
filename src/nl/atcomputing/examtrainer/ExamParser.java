package nl.atcomputing.examtrainer;


public interface ExamParser {

	/**
	 * Adds an exam from a file or URL to the database
	 * @param databaseName name of the database that will hold the exam
	 */
	boolean addExam();

	/**
	 * Checks if feed (be it a file or URL) is already in the database
	 */
	boolean checkIfExamInDatabase();
}