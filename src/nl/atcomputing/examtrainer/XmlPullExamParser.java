package nl.atcomputing.examtrainer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

public class XmlPullExamParser {
	private static final String TAG = "XmlPullExamParser";
	private ArrayList<ExamQuestion> examQuestions = new ArrayList<ExamQuestion>();
	private Context context;
	final URL url;
	
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
	
	/**
	 * Creates a new ExamParser
	 * @param context the application's context
	 * @param url location of the exam. Use file:// for local file, http:// for http access.
	 * @param is The inputstream associated with the url
	 */
	public XmlPullExamParser(Context context, URL url) {
		this.url = url;
		this.context = context;
	}
	
	private InputStream getInputStream() {
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

	public ArrayList<ExamQuestion> getExam() {
		return examQuestions;
	}
	
	public void parseExam() throws RuntimeException {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(this.getInputStream(), null);
            int eventType = parser.getEventType();
            String name = "";
            while (eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_TAG:
                    	//Log.d(TAG, "parse: START_TAG " + parser.getName());
                        name = parser.getName();
                        break;
                    case XmlPullParser.TEXT:
                    	//Log.d(TAG, "parse: TEXT " + parser.getText());
                    	if (name.equalsIgnoreCase(ITEM)) {
		                    ExamQuestion examQuestion = parseItem(parser);
		                    if ( examQuestion != null ) {
		                    	examQuestions.add(examQuestion);
		                    }
                    	}
                    	name = "";
		                break;
                    case XmlPullParser.END_TAG:
                    	//Log.d(TAG, "parse: END_TAG " + parser.getName());
                    	name = "";
    	        		break;
                }
                eventType = parser.next();
            }
            parser = null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
	
	
	
	private ExamQuestion parseItem(XmlPullParser parser) throws Exception {
		
		ExamQuestion examQuestion = new ExamQuestion();
		String start_tag = "";
		
		int eventType = parser.getEventType();
	    while (eventType != XmlPullParser.END_DOCUMENT){
	        switch (eventType){
	        	case XmlPullParser.START_TAG:
	                start_tag = parser.getName();
	                //Log.d(this.getClass().getName(), "parseItem START_TAG: " + start_tag);
	                break;
	        	case XmlPullParser.END_TAG:
	        		//Log.d(this.getClass().getName(), "parseItem END_TAG: " + parser.getName());
	        		if (parser.getName().equalsIgnoreCase(ITEM)) {
	        			return examQuestion;
	        		}
	        		start_tag = "";
	        		break;
	            case XmlPullParser.TEXT:
	                if (start_tag.equalsIgnoreCase(ITEM_TYPE)) {
	                	//Log.d(this.getClass().getName(), "parseItem TEXT type: " + parser.getText());
	                	examQuestion.setType(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(ITEM_TOPIC)) {
	                	//Log.d(this.getClass().getName(), "parseItem TEXT topic: " + parser.getText());
	                	examQuestion.setTopic(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(ITEM_QUESTION)) {
	                	//Log.d(this.getClass().getName(), "parseItem TEXT question: " + parser.getText());
	                	examQuestion.setQuestion(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(ITEM_EXHIBIT)) {
	                	//Log.d(this.getClass().getName(), "parseItem TEXT exhibit: " + parser.getText());
	                	examQuestion.setExhibit(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(ITEM_CORRECT_ANSWER)) {
	                	//Log.d(this.getClass().getName(), "parseItem TEXT correct_answer: " + parser.getText());
	                	examQuestion.addAnswer(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(ITEM_CHOICE)) {
	                	//Log.d(this.getClass().getName(), "parseItem TEXT choice: " + parser.getText());
	                	examQuestion.addChoice(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(ITEM_HINT)) {
	                	//Log.d(this.getClass().getName(), "parseItem TEXT hint: " + parser.getText());
	                	examQuestion.setHint(parser.getText());
	                }
	                break;
	            }
	        	eventType = parser.next();
	        }
	    if ( eventType == XmlPullParser.END_DOCUMENT ) {
	    	throw new RuntimeException("End of document reached while parsing item");
	    }
	    return null;
	}
	
}