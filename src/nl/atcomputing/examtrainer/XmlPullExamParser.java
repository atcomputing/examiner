package nl.atcomputing.examtrainer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;
import android.util.Xml;

//http://www.ibm.com/developerworks/opensource/library/x-android/#download

public class XmlPullExamParser extends BaseExamParser {
	private static final String TAG = "ExamTrainerXmlParser";
	private ExaminationDbAdapter examinationDbHelper;
	private ExamTrainerDbAdapter examTrainerDbHelper;
	
	public XmlPullExamParser(String filename) {
		super(filename);
	}
	
	public boolean checkIfExamInDatabase() {
		examTrainerDbHelper = new ExamTrainerDbAdapter();
		examTrainerDbHelper.open();
		String title = null;
		
        XmlPullParser parser = Xml.newPullParser();
        try {
            // auto-detect the encoding from the stream
            parser.setInput(this.getInputStream(), null);
            int eventType = parser.getEventType();
            boolean done = false;
            String name = null;
            while (eventType != XmlPullParser.END_DOCUMENT && !done){
                switch (eventType){
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        break;
                    case XmlPullParser.TEXT:
                    	if ( name.equalsIgnoreCase(EXAM_TITLE)) {
                    		title = parser.getText();
                    		done = true;
                    	}
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
		
        if( examTrainerDbHelper.checkIfExamAlreadyInDatabase(title) ) {
        	examTrainerDbHelper.close();
        	return true;
        }
        else {
        	examTrainerDbHelper.close();
        	return false;
        }
        	
	}
	
	public void addExam() {
		examinationDbHelper = new ExaminationDbAdapter();
		examinationDbHelper.open();
		String examTitle;
		
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
		                if (name.equalsIgnoreCase("item")) {
		                    ExamQuestion examQuestion = parseItem(parser);
		                    if ( examQuestion != null ) {
		                    	addQuestionToDatabase(examQuestion);
		                    }
		                }
		                break;
		            case XmlPullParser.TEXT:
		            	if(name.equalsIgnoreCase("titel")) {
		            		examTitle=parser.getText();
		            	}
		            }
		        eventType = parser.next();
		        }
		    
		} catch (Exception e) {
			Log.d(this.getClass().getName() , e.getMessage());
		}
		
		examinationDbHelper.close();
	}

	protected ExamQuestion parseItem(XmlPullParser parser) throws FileNotFoundException, IOException, Exception {
		
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
	        		if (parser.getName().equalsIgnoreCase("item")) {
	        			return examQuestion;
	        		}
	        		start_tag = "";
	        		break;
	            case XmlPullParser.TEXT:
	                if (start_tag.equalsIgnoreCase("type")) {
	                	Log.d(this.getClass().getName(), "parseItem TEXT type: " + parser.getText());
	                	examQuestion.setType(parser.getText());
	                } else if (start_tag.equalsIgnoreCase("topic")) {
	                	Log.d(this.getClass().getName(), "parseItem TEXT topic: " + parser.getText());
	                	examQuestion.setTopic(parser.getText());
	                } else if (start_tag.equalsIgnoreCase("question")) {
	                	Log.d(this.getClass().getName(), "parseItem TEXT question: " + parser.getText());
	                	examQuestion.setQuestion(parser.getText());
	                } else if (start_tag.equalsIgnoreCase("exhibit")) {
	                	Log.d(this.getClass().getName(), "parseItem TEXT exhibit: " + parser.getText());
	                	examQuestion.setExhibit(parser.getText());
	                } else if (start_tag.equalsIgnoreCase("correct_answer")) {
	                	Log.d(this.getClass().getName(), "parseItem TEXT correct_answer: " + parser.getText());
	                	examQuestion.addCorrectAnswer(parser.getText());
	                } else if (start_tag.equalsIgnoreCase("choice")) {
	                	Log.d(this.getClass().getName(), "parseItem TEXT choice: " + parser.getText());
	                	examQuestion.addChoice(parser.getText());
	                } else if (start_tag.equalsIgnoreCase("hint")) {
	                	Log.d(this.getClass().getName(), "parseItem TEXT hint: " + parser.getText());
	                	examQuestion.setHint(parser.getText());
	                } else if (start_tag.equalsIgnoreCase("item")) {
	                	//do nothing
	                } else {
	                	Log.d(ExamTrainerActivity.class.getName(), "Unknown tag " + start_tag + " in xml file");
	                }
	                break;
	            }
	        	eventType = parser.next();
	        }
	    return null;
	}
	
	private void addQuestionToDatabase(ExamQuestion examQuestion) {
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