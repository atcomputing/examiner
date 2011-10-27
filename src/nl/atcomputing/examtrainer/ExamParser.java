package nl.atcomputing.examtrainer;


public interface ExamParser {

	/**
	 * Adds an exam from a file or URL to the ExamTrainer database
	 * Note that this does not install the exam
	 * @return true if exam was succesfully added, false otherwise
	 */
	boolean addExamToExamTrainer();

	
	/**
	 * Installs exam available in ExamTrainer database from URL
	 * @return true if installation succeeded, false otherwise
	 */
	boolean installExam();
	
	/**
	 * Checks if exam is already in the database
	 * @return true if exam exists in database, false otherwise
	 */
	boolean checkIfExamInDatabase();
}