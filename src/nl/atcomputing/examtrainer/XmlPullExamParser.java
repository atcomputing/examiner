package nl.atcomputing.examtrainer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.util.Xml;

//http://www.ibm.com/developerworks/opensource/library/x-android/#download

public class XmlPullExamParser extends BaseExamParser {
	private static final String TAG = "XmlPullExamParser";
	private String title = null;
	private int itemsNeededToPass = -1;
	private int amountOfItems = 0;
	private String date = null;
	
	private Context context;
	
	public XmlPullExamParser(Context context, InputStream is) {
		super(is);
		this.context = context;
	}
	
	private void getExamMetaData() throws RuntimeException {
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(this.getInputStream(), null);
            int eventType = parser.getEventType();
            int done = 0;
            String name = null;
            while (eventType != XmlPullParser.END_DOCUMENT && ( done < 2 )){
                switch (eventType){
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        break;
                    case XmlPullParser.TEXT:
                    	if ( name.equalsIgnoreCase(EXAM_TITLE)) {
                    		title = parser.getText();
                    		done++;
                    	} else if ( name.equalsIgnoreCase(EXAM_ITEMS_NEEDED_TO_PASS)) {
                    		itemsNeededToPass = Integer.parseInt(parser.getText());
                    		done++;
                    	}
                    	break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
	
	private long addExamToExamTrainerDB() {
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(context);
		examTrainerDbHelper.open();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mmZ");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		date = dateFormat.format(new Date(0));
		
		long rowId = examTrainerDbHelper.addExam(title, date, itemsNeededToPass, amountOfItems);
		examTrainerDbHelper.close();
		
		return rowId;
	}
	
	private void delExamFromExamTrainerDB(long rowId) {
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(context);
		examTrainerDbHelper.open();
		
		examTrainerDbHelper.deleteExam(rowId);
		
		examTrainerDbHelper.close();
	}
	
	public boolean checkIfExamInDatabase() throws RuntimeException {
		
		if ( title == null ) {
			getExamMetaData();
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
	
	public boolean addExam() {
		
		if ( title == null ) {
			getExamMetaData();
		}
		
		long rowId = addExamToExamTrainerDB();
		
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter();
		
		try {
			examinationDbHelper.open(title + "-" + date);
		} catch (SQLiteException e) {
			Log.d(TAG, "Cannot open database " + title + "-" + date + "for writing");
			delExamFromExamTrainerDB(rowId);
			return false;
		}
		
		try {
		    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
	        XmlPullParser parser = factory.newPullParser();
		    parser.setInput(this.getInputStream(), null);

		    int eventType = parser.getEventType();
		    String name = null;
		    while (eventType != XmlPullParser.END_DOCUMENT){
		        switch (eventType) {
		            case XmlPullParser.START_TAG:
		                name = parser.getName();
		                Log.d(this.getClass().getName(), "loadExam TAG: " + name);
		                if (name.equalsIgnoreCase(ITEM)) {
		                    ExamQuestion examQuestion = parseItem(parser);
		                    if ( examQuestion != null ) {
		                    	addQuestionToDatabase(examQuestion, examinationDbHelper);
		                    	amountOfItems++;
		                    }
		                }
		                break;
		        }
		        eventType = parser.next();
		        }
		    
		} catch (Exception e) {
			Log.d(this.getClass().getName() , e.getMessage());
		}
		
		examinationDbHelper.close();
		return true;
	}

	
	
	private ExamQuestion parseItem(XmlPullParser parser) throws FileNotFoundException, IOException, Exception {
		
		ExamQuestion examQuestion = new ExamQuestion();
		String start_tag = null;
		
		int eventType = parser.getEventType();
	    while (eventType != XmlPullParser.END_DOCUMENT){
	        switch (eventType){
	        	case XmlPullParser.START_TAG:
	                start_tag = parser.getName();
	                Log.d(this.getClass().getName(), "parseItem START_TAG: " + start_tag);
	                break;
	        	case XmlPullParser.END_TAG:
	        		Log.d(this.getClass().getName(), "parseItem END_TAG: " + parser.getName());
	        		if (parser.getName().equalsIgnoreCase(ITEM)) {
	        			return examQuestion;
	        		}
	        		start_tag = "";
	        		break;
	            case XmlPullParser.TEXT:
	                if (start_tag.equalsIgnoreCase(ITEM_TYPE)) {
	                	Log.d(this.getClass().getName(), "parseItem TEXT type: " + parser.getText());
	                	examQuestion.setType(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(ITEM_TOPIC)) {
	                	Log.d(this.getClass().getName(), "parseItem TEXT topic: " + parser.getText());
	                	examQuestion.setTopic(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(ITEM_QUESTION)) {
	                	Log.d(this.getClass().getName(), "parseItem TEXT question: " + parser.getText());
	                	examQuestion.setQuestion(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(ITEM_EXHIBIT)) {
	                	Log.d(this.getClass().getName(), "parseItem TEXT exhibit: " + parser.getText());
	                	examQuestion.setExhibit(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(ITEM_CORRECT_ANSWER)) {
	                	Log.d(this.getClass().getName(), "parseItem TEXT correct_answer: " + parser.getText());
	                	examQuestion.addCorrectAnswer(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(ITEM_CHOICE)) {
	                	Log.d(this.getClass().getName(), "parseItem TEXT choice: " + parser.getText());
	                	examQuestion.addChoice(parser.getText());
	                } else if (start_tag.equalsIgnoreCase(ITEM_HINT)) {
	                	Log.d(this.getClass().getName(), "parseItem TEXT hint: " + parser.getText());
	                	examQuestion.setHint(parser.getText());
	                }
	                break;
	            }
	        	eventType = parser.next();
	        }
	    return null;
	}
	
	private void addQuestionToDatabase(ExamQuestion examQuestion, ExaminationDbAdapter examinationDbHelper) {
		ArrayList<String> arrayList;
		long questionId = examinationDbHelper.addQuestion(examQuestion);
		
		arrayList = examQuestion.getChoices();
		for( int i = 0; i < arrayList.size(); i++ ) {
			examinationDbHelper.addChoice(questionId, arrayList.get(i));
		}
		
		arrayList = examQuestion.getCorrectAnswers();
		for( int i = 0; i < arrayList.size(); i++ ) {
			examinationDbHelper.addCorrectAnswers(questionId, arrayList.get(i));
		}
	    
	}
}