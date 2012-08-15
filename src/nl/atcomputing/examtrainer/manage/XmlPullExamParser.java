package nl.atcomputing.examtrainer.manage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import nl.atcomputing.examtrainer.ExamQuestion;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

/**
 * @author martijn brekhof
 *
 */

public class XmlPullExamParser {
	private ArrayList<ExamQuestion> examQuestions = new ArrayList<ExamQuestion>();
	private Context context;
	final URL url;
	
	// names of the XML tags
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
				return context.getApplicationContext().getAssets().open(url.getFile().replaceFirst("^/", ""));
			} 
			else {
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
                        name = parser.getName();
                        break;
                    case XmlPullParser.TEXT:
                    	if (name.equalsIgnoreCase(ITEM)) {
		                    ExamQuestion examQuestion = parseItem(parser);
		                    if ( examQuestion != null ) {
		                    	examQuestions.add(examQuestion);
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
	
	
	
	private ExamQuestion parseItem(XmlPullParser parser) throws Exception {
		
		ExamQuestion examQuestion = new ExamQuestion(context);
		String start_tag = "";
		
		int eventType = parser.getEventType();
	    while (eventType != XmlPullParser.END_DOCUMENT){
	        switch (eventType){
	        	case XmlPullParser.START_TAG:
	                start_tag = parser.getName();
	                break;
	        	case XmlPullParser.END_TAG:
	        		if (parser.getName().equalsIgnoreCase(ITEM)) {
	        			return examQuestion;
	        		}
	        		start_tag = "";
	        		break;
	            case XmlPullParser.TEXT:
	                if (start_tag.equalsIgnoreCase(ITEM_TYPE)) {
	                	examQuestion.setType(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(ITEM_TOPIC)) {
	                	examQuestion.setTopic(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(ITEM_QUESTION)) {
	                	examQuestion.setQuestion(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(ITEM_EXHIBIT)) {
	                	examQuestion.setExhibit(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(ITEM_CORRECT_ANSWER)) {
	                	examQuestion.addAnswer(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(ITEM_CHOICE)) {
	                	examQuestion.addChoice(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(ITEM_HINT)) {
	                	examQuestion.setHint(parser.getText());
	                }
	                break;
	            }
	        	eventType = parser.next();
	        }
	    if ( eventType == XmlPullParser.END_DOCUMENT ) {
	    	throw new RuntimeException("End of document reached while parsing item");
	    }
	    Log.d("XmlPullExamParser", "parsed question: "+examQuestion.toString());
	    return null;
	}
	
}
