package nl.atcomputing.examtrainer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.util.Xml;

//http://www.ibm.com/developerworks/opensource/library/x-android/#download

public class XmlPullExamListParser {
	private static final String TAG = "XmlPullExamListParser";
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
	
	/**
	 * Creates a new ExamParser
	 * @param context the application's context
	 * @param url location of the exam. Use file:// for local file, http:// for http access.
	 * @param is The inputstream associated with the url
	 */
	public XmlPullExamListParser(Context context, URL url) {
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

	public ArrayList<Exam> parseList() throws RuntimeException {
		ArrayList<Exam> exams = new ArrayList<Exam>();
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
                    	if (name.equalsIgnoreCase(EXAM)) {
		                    Exam exam = parseExamInList(parser);
		                    if ( exam != null ) {
		                    	exams.add(exam);
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
            return exams;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
	
	private Exam parseExamInList(XmlPullParser parser) throws Exception {
		Exam exam = new Exam();
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
	        		if (parser.getName().equalsIgnoreCase(EXAM)) {
	        			return exam;
	        		}
	        		start_tag = "";
	        		break;
	            case XmlPullParser.TEXT:
	                if (start_tag.equalsIgnoreCase(EXAM_TITLE)) {
	                	//Log.d(this.getClass().getName(), "parseItem TEXT type: " + parser.getText());
	                	exam.setTitle(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(EXAM_AUTHOR)) {
	                	//Log.d(this.getClass().getName(), "parseItem TEXT topic: " + parser.getText());
	                	exam.setAuthor(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(EXAM_CATEGORY)) {
	                	//Log.d(this.getClass().getName(), "parseItem TEXT question: " + parser.getText());
	                	exam.setCategory(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(EXAM_ITEMS_NEEDED_TO_PASS)) {
	                	//Log.d(this.getClass().getName(), "parseItem TEXT exhibit: " + parser.getText());
	                	exam.setItemsNeededToPass(Integer.parseInt(parser.getText()));
	                } else if (start_tag.equalsIgnoreCase(EXAM_NUMBER_OF_ITEMS)) {
	                	//Log.d(this.getClass().getName(), "parseItem TEXT correct_answer: " + parser.getText());
	                	exam.setNumberOfItems(Integer.parseInt(parser.getText()));
	                } else if (start_tag.equalsIgnoreCase(EXAM_URL)) {
	                	//Log.d(this.getClass().getName(), "parseItem TEXT choice: " + parser.getText());
	                	exam.setURL(parser.getText());
	                }
	                break;
	            }
	        	eventType = parser.next();
	        }
	    if ( eventType == XmlPullParser.END_DOCUMENT ) {
	    	throw new RuntimeException("End of document reached while parsing exam");
	    }
	    return null;
	}
	
}