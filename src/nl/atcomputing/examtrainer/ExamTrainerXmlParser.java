package nl.atcomputing.examtrainer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.res.AssetManager;
import android.util.Log;

public class ExamTrainerXmlParser {
	private static final String TAG = "ExamTrainerXmlParser";
	private ExaminationDbAdapter examinationDbHelper;
	private ExamTrainerDbAdapter examTrainerDbHelper;
	
	public ExamTrainerXmlParser() {}
	
	protected void checkDatabaseXmlFiles(XmlPullParser xmlParser, AssetManager assetManager) {
		examTrainerDbHelper = new ExamTrainerDbAdapter();
		examTrainerDbHelper.open();
		
		if( assetManager != null ) {
			try {
				String[] filenames = assetManager.list("");
				int size = filenames.length;
				for( int i = 0; i < size; i++) {
					if(filenames[i].matches("exam..*.xml")) {
						Log.d(TAG, "Found databasefile " + filenames[i]);
					}
				}
			} catch (IOException e) {
				Log.d(this.getClass().getName() , e.getMessage());
			}
		}
		examTrainerDbHelper.close();
	}
	
	protected void loadExam(InputStream is) {
		try {
			String examTitle;
			
			examinationDbHelper = new ExaminationDbAdapter();
			examinationDbHelper.open();
			
		    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
	        XmlPullParser parser = factory.newPullParser();
		    parser.setInput(is, null);

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