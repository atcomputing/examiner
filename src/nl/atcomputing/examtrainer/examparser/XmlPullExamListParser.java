package nl.atcomputing.examtrainer.examparser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;


import nl.atcomputing.examtrainer.main.Exam;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.util.Xml;

/**
 * @author martijn brekhof
 *
 */

public class XmlPullExamListParser {
	private Context context;
	private ArrayList<Exam> exams;
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
	static final String EXAM_COURSE_URL = "courseurl";
	static final String EXAM_TIMELIMIT = "timelimit";
	
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
				return context.getApplicationContext().getAssets().open(url.getFile().replaceFirst("^/", ""));
			} 
			else {
				return url.openConnection().getInputStream();
			}
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}

	public ArrayList<Exam> getExamList() {
		return exams;
	}
	
	public void parse() throws RuntimeException {
		exams = new ArrayList<Exam>();
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(this.getInputStream(), null);
            int eventType = parser.getEventType();
            String name = "";
            while (eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        break;
                    case XmlPullParser.TEXT:
                    	if (name.equalsIgnoreCase(EXAM)) {
		                    Exam exam = parseExamInList(parser);
		                    if ( exam != null ) {
		                    	exams.add(exam);
		                    }
                    	}
                    	name = "";
		                break;
                    case XmlPullParser.END_TAG:
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
	
	private Exam parseExamInList(XmlPullParser parser) throws Exception {
		Exam exam = new Exam();
		String start_tag = "";
		
		int eventType = parser.getEventType();
	    while (eventType != XmlPullParser.END_DOCUMENT){
	        switch (eventType){
	        	case XmlPullParser.START_TAG:
	                start_tag = parser.getName();
	                break;
	        	case XmlPullParser.END_TAG:
	        		if (parser.getName().equalsIgnoreCase(EXAM)) {
	        			return exam;
	        		}
	        		start_tag = "";
	        		break;
	            case XmlPullParser.TEXT:
	                if (start_tag.equalsIgnoreCase(EXAM_TITLE)) {
	                	exam.setTitle(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(EXAM_AUTHOR)) {
	                	exam.setAuthor(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(EXAM_CATEGORY)) {
	                	exam.setCategory(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(EXAM_ITEMS_NEEDED_TO_PASS)) {
	                	exam.setItemsNeededToPass(Integer.parseInt(parser.getText()));
	                } else if (start_tag.equalsIgnoreCase(EXAM_NUMBER_OF_ITEMS)) {
	                	exam.setNumberOfItems(Integer.parseInt(parser.getText()));
	                } else if (start_tag.equalsIgnoreCase(EXAM_URL)) {
	                	exam.setURL(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(EXAM_COURSE_URL)) {
	                	exam.setCourseURL(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(EXAM_TIMELIMIT)) {
	                	exam.setTimeLimit(Long.parseLong(parser.getText()));
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
