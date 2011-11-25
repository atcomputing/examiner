package nl.atcomputing.examtrainer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.content.Context;
import android.util.Log;


/**
 * @author martijn brekhof
 *
 */
public abstract class BaseExamParser implements ExamParser {
	private static final String TAG = "BaseExamParser";
	// names of the XML tags
	static final String EXAM = "exam";
	static final String EXAM_DB_VERSION_ATTR = "databaseversion";
	static final String EXAM_TITLE = "title";
	static final String EXAM_NUMBER_OF_ITEMS = "numberofitems";
	static final String EXAM_ITEMS_NEEDED_TO_PASS = "itemsneededtopass";
	static final String EXAM_AUTHOR = "author";
	static final String EXAM_CATEGORY = "category";
	static final String EXAM_URL = "url";
	
	static final String ITEM = "item";
	static final String ITEM_TYPE = "type";
	static final String ITEM_TOPIC = "topic";
	static final String ITEM_EXHIBIT = "exhibit";
	static final String ITEM_QUESTION = "question";
	static final String ITEM_CHOICE = "choice";
	static final String ITEM_CORRECT_ANSWER = "correct_answer";
	static final String ITEM_HINT = "hint";
	
	final URL url;
	private Context context;
	
	/**
	 * Creates a new BaseFeedParser
	 * @param is
	 */
	protected BaseExamParser(Context context, URL url){
		this.url = url;
		this.context = context;
	}

	protected URL getUrl() {
		return url;
	}
	
	protected InputStream getInputStream() {
		try {
			if(url.getProtocol().equals("file")) {
				Log.d(TAG, "getInputStream returning protocol " + url.getProtocol() +" and file "+ url.getFile());
				return context.getApplicationContext().getAssets().open(url.getFile().replaceFirst("^/", ""));
			} 
			else {
				Log.d(TAG, "getInputStream returning protocol " + url.getProtocol() +" and location "+ url.getFile());
				return url.openConnection().getInputStream();
			}
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
}
