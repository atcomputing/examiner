package nl.atcomputing.examtrainer;

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

public class XmlPullExamParser extends BaseExamParser {
	private static final String TAG = "XmlPullExamParser";
	private String title = null;
	private int itemsNeededToPass = -1;
	private ArrayList<ExamQuestion> examQuestions = new ArrayList<ExamQuestion>();
	
	private Context context;
	
	/**
	 * Creates a new ExamParser
	 * @param context the application's context
	 * @param url location of the exam. Use file:// for local file, http:// for http access.
	 * @param is The inputstream associated with the url
	 */
	public XmlPullExamParser(Context context, URL url) {
		super(context, url);
		this.context = context;
	}
	
	public void parse() throws RuntimeException {
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
                    	} else if ( name.equalsIgnoreCase(EXAM_ITEMS_NEEDED_TO_PASS)) {
                    		itemsNeededToPass = Integer.parseInt(parser.getText());
                    	} else if ( name.equalsIgnoreCase(EXAM_TITLE)) {
                    		title = parser.getText();
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
	
	public boolean checkIfExamInDatabase() {
		
		if ( title == null ) {
			return false;
		}
		
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(context);
		examTrainerDbHelper.open();
		
        if( examTrainerDbHelper.checkIfExamAlreadyInDatabase(title) ) {
        	examTrainerDbHelper.close();
        	return true;
        }
        else {
        	examTrainerDbHelper.close();
        	return false;
        }
	}
	
	public boolean addExamToExamTrainer() {
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(context);
		examTrainerDbHelper.open();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mmZ");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		String date = dateFormat.format(new Date());
		
		examTrainerDbHelper.addExam(title, date, itemsNeededToPass, examQuestions.size(), 
				false, this.getUrl().toExternalForm());

		examTrainerDbHelper.close();
		return true;
	}
	
	public void installExam(String title, String date) throws SQLiteException {
		try {
			ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(context);
			examinationDbHelper.open(title, date);
			for ( int i = 0; i < examQuestions.size(); i++ ) {
					examQuestions.get(i).addToDatabase(examinationDbHelper);
			}
			examinationDbHelper.close();
			
			ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(context);
			examTrainerDbHelper.open();
			examTrainerDbHelper.setInstalled(title, date, true);
			examTrainerDbHelper.close();
		} catch (SQLiteException e) {
			Log.d(TAG, "Cannot open database " + title + "-" + date + "for writing");
			throw e;
		}
		
		//no need for examQuestions anymore
		examQuestions = null;
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
	                	examQuestion.addCorrectAnswer(parser.getText());
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