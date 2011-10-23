package nl.atcomputing.examtrainer;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


/**
 * @author martijn brekhof
 *
 */
public abstract class BaseExamParser implements ExamParser {

	// names of the XML tags
	static final String EXAM = "exam";
	static final String EXAM_DB_VERSION_ATTR = "databaseversion";
	static final String EXAM_TITLE = "title";
	static final String EXAM_NUMBER_OF_ITEMS = "numberofitems";
	static final String EXAM_ITEMS_NEEDED_TO_PASS = "itemsneededtopass";
	
	static final String ITEM = "item";
	static final String ITEM_TYPE = "type";
	static final String ITEM_TOPIC = "topic";
	static final String ITEM_EXHIBIT = "exhibit";
	static final String ITEM_QUESTION = "question";
	static final String ITEM_CHOICE = "choice";
	static final String ITEM_CORRECT_ANSWER = "correct_answer";
	static final String ITEM_HINT = "hint";
	
	final InputStream inputStream;

	/**
	 * Creates a new BaseFeedParser
	 * @param is
	 */
	protected BaseExamParser(InputStream is){
		this.inputStream = is;
	}

	protected InputStream getInputStream() {
		return inputStream;
	}
}
