package nl.atcomputing.lpic1examtrainer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;

public class ExamTrainerActivity extends Activity {
	private ExamTrainerDbAdapter dbHelper;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		retrieveExam();
		setContentView(R.layout.main);

		loadExam("exam101.xml");

		

		Button startExam = (Button) findViewById(R.id.button_start_exam);
		startExam.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ExamTrainerActivity.this, ExamQuestionsActivity.class);
				intent.putExtra("question", 1);
				startActivity(intent);
			}
		});
	}
	
	
	protected void retrieveExam() {
		Intent intent = new Intent(this, RetrieveExamQuestions.class);
		startService(intent);
	}
	
	protected void loadExam(String filename) {
		dbHelper = new ExamTrainerDbAdapter(this);
		dbHelper.open();
		dbHelper.upgrade();
		
		XmlPullParser parser = Xml.newPullParser();
		try {
		    InputStream is = getAssets().open(filename);
		    
		    // auto-detect the encoding from the stream
		    parser.setInput(is, null);
		    int eventType = parser.getEventType();
		    while (eventType != XmlPullParser.END_DOCUMENT){
		        String name = null;
		        ExamQuestion examQuestion;
		        switch (eventType){
		            case XmlPullParser.START_TAG:
		                name = parser.getName();
		                if (name.equalsIgnoreCase("item")) {
		                    examQuestion = parseItem(parser);
		                    if ( examQuestion != null ) {
		                    	dbHelper.addQuestion(examQuestion);
		                    }
		                }
		                break;
		            }
		        eventType = parser.next();
		        }
		} catch (FileNotFoundException e) {
		    Log.d(this.getClass().getName() , "Could not find " + filename);
		} catch (IOException e) {
			Log.d(this.getClass().getName() , "I/O problem with " + filename + "exception: " + e.getMessage());
		} catch (Exception e){
			Log.d(this.getClass().getName() , e.getMessage());
		}
		dbHelper.close();
	}

	private ExamQuestion parseItem(XmlPullParser parser) throws FileNotFoundException, IOException, Exception {
		
		ExamQuestion examQuestion = new ExamQuestion();
		String start_tag = null;
		
		int eventType = parser.getEventType();
	    while (eventType != XmlPullParser.END_DOCUMENT){
	        switch (eventType){
	            case XmlPullParser.START_TAG:
	                start_tag = parser.getName();
	                break;
	            case XmlPullParser.TEXT:
	                if (start_tag.equalsIgnoreCase("type")) {
	                	examQuestion.setType(parser.getText());
	                } else if (start_tag.equalsIgnoreCase("topic")) {
	                	examQuestion.setTopic(parser.getText());
	                } else if (start_tag.equalsIgnoreCase("question")) {
	                	examQuestion.setQuestion(parser.getText());
	                } else if (start_tag.equalsIgnoreCase("exhibit")) {
	                	examQuestion.setExhibit(parser.getText());
	                } else if (start_tag.equalsIgnoreCase("correct_answer")) {
	                	examQuestion.addCorrectAnswer(parser.getText());
	                } else if (start_tag.equalsIgnoreCase("answer")) {
	                	examQuestion.addAnswer(parser.getText());
	                } else {
	                	Log.d(ExamTrainerActivity.class.getName(), "Unknown tag " + start_tag + " in xml file");
	                }
	                break;
	            case XmlPullParser.END_TAG:
	            	return examQuestion;
	            }
	        	eventType = parser.next();
	        }
	    return null;
	}
}