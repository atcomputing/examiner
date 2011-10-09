package nl.atcomputing.lpic1examtrainer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * @author martijn brekhof
 *
 */
public class ExamTrainerActivity extends Activity {
	private ExamTrainerDbAdapter dbHelper;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		retrieveExam();
		setContentView(R.layout.main);		

		Button startExam = (Button) findViewById(R.id.button_start_exam);
		startExam.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ExamTrainerActivity.this, ExamQuestionsActivity.class);
				intent.putExtra("question", 1);
				startActivity(intent);
			}
		});
		
		Button updateExam = (Button) findViewById(R.id.button_get_updates);
		updateExam.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadExam("exam101.xml");
			}
		});
		
		Button reviewPreviousExam = (Button) findViewById(R.id.button_show_results);
		reviewPreviousExam.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ExamTrainerActivity.this, ExamResultsActivity.class);
				intent.putExtra("action", ExamResultsActivity.NONE);
				startActivity(intent);
			}
		});
		
		Button quitExamTrainer = (Button) findViewById(R.id.button_quit);
		quitExamTrainer.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
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
		
		try {
		    InputStream is = getAssets().open(filename);
		    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
	        XmlPullParser parser = factory.newPullParser();
		    parser.setInput(is, null);

		    int eventType = parser.getEventType();
		    while (eventType != XmlPullParser.END_DOCUMENT){
		        String name = null;
		        ExamQuestion examQuestion;
		        switch (eventType){
		            case XmlPullParser.START_TAG:
		                name = parser.getName();
		                Log.d(this.getClass().getName(), "loadExam TAG: " + name);
		                if (name.equalsIgnoreCase("item")) {
		                    examQuestion = parseItem(parser);
		                    if ( examQuestion != null ) {
		                    	addQuestionToDatabase(examQuestion);
		                    }
		                }
		                break;
		            }
		        eventType = parser.next();
		        }
		    dbHelper.close();
		} catch (FileNotFoundException e) {
			dbHelper.close();
		    Log.d(this.getClass().getName() , "Could not find " + filename);
		} catch (IOException e) {
			dbHelper.close();
			Log.d(this.getClass().getName() , "I/O problem with " + filename + "exception: " + e.getMessage());
		} catch (Exception e){
			dbHelper.close();
			Log.d(this.getClass().getName() , e.getMessage());
		}
	}

	private void addQuestionToDatabase(ExamQuestion examQuestion) {
		ArrayList<String> arrayList;
		long questionId = dbHelper.addQuestion(examQuestion);
		
		arrayList = examQuestion.getChoices();
		for( int i = 0; i < arrayList.size(); i++ ) {
			dbHelper.addChoice(questionId, arrayList.get(i));
		}
		
		arrayList = examQuestion.getCorrectAnswers();
		for( int i = 0; i < arrayList.size(); i++ ) {
			dbHelper.addCorrectAnswers(questionId, arrayList.get(i));
		}
	    
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
}