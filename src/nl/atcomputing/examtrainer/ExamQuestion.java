package nl.atcomputing.examtrainer;

import java.util.ArrayList;

import android.content.Context;

/**
 * @author martijn brekhof
 *
 */
public class ExamQuestion {
	public static final String TYPE_OPEN = "open";
	public static final String TYPE_MULTIPLE_CHOICE = "multiple choice";
	
	private String type;
	private String topic;
	private String question;
	private String exhibit;
	private String hint;
	private ArrayList<String> answers;
	private ArrayList<String> choices;
	
	protected ExamQuestion(Context context) {
		type = TYPE_MULTIPLE_CHOICE;
		topic = null;
		question = context.getString(R.string.No_question_available);
		answers = new ArrayList<String>();
		choices = new ArrayList<String>();
		hint = context.getString(R.string.hint_not_available);
		exhibit = null;
	}
	
	
	/**
	 * @brief Creates a new ExamQuestion object
	 * @param type
	 * @param topic
	 * @param exhibit
	 * @param question
	 * @param correct_answer
	 * @param choices
	 */
	protected ExamQuestion(String type, String topic, String exhibit, String question, 
			ArrayList<String> answers, ArrayList<String> choices, String hint) {
		this.type = type;
		this.topic = topic;
		this.question = question;
		this.answers = answers;
		this.choices = choices;
		this.hint = hint;
		this.exhibit = exhibit;
	}
	
	protected String convertArrayListToString(ArrayList<String> choices) {
		int size = choices.size();
		StringBuilder stringBuilder = new StringBuilder();
		
		for ( int i = 0; i < size; i++ ) {
			stringBuilder.append(choices.get(i) + ", ");
		}
		return stringBuilder.toString().replaceAll(", ^", "");
	}
	
	protected String getType() {
		return type;
	}
	
	protected String getTopic() {
		return topic;
	}
	
	protected ArrayList<String> getChoices() {
		return choices;
	}
	
	protected ArrayList<String> getAnswers() {
		return answers;
	}
	
	protected String getQuestion() {
		return question;
	}
	
	protected String getExhibit() {
		return exhibit;
	}
	
	protected String getHint() {
		return hint;
	}
	
	protected void setType(String str) {
		type = str;
	}
	
	protected void setTopic(String str) {
		topic = str;
	}
	
	protected void setChoices(ArrayList<String> arrayList) {
		choices = arrayList;
	}
	
	protected void setCorrectAnswers(ArrayList<String> arrayList) {
		answers = arrayList;
	}
	
	protected void setQuestion(String str) {
		question = str;
	}

	protected void setExhibit(String str) {
		exhibit = str;
	}
	
	protected void setHint(String str) {
		hint = str;
	}
	protected void addChoice(String str) {
		choices.add(str);
	}
	
	protected void addAnswer(String str) {
		answers.add(str);
	}
	
	protected void addToDatabase(ExaminationDbAdapter examinationDbHelper) {
		ArrayList<String> arrayList;
		long questionId = examinationDbHelper.addQuestion(this);
		
		arrayList = this.getChoices();
		for( int i = 0; i < arrayList.size(); i++ ) {
			examinationDbHelper.addChoice(questionId, arrayList.get(i));
		}
		
		arrayList = this.getAnswers();
		for( int i = 0; i < arrayList.size(); i++ ) {
			examinationDbHelper.addAnswer(questionId, arrayList.get(i));
		}
	}
}