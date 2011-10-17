package nl.atcomputing.examtrainer;

import java.util.ArrayList;

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
	private ArrayList<String> correctAnswers;
	private ArrayList<String> choices;
	
	protected ExamQuestion() {
		type = null;
		topic = null;
		question = null;
		correctAnswers = new ArrayList<String>();
		choices = new ArrayList<String>();
		hint = null;
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
			ArrayList<String> correctAnswers, ArrayList<String> choices, String hint) {
		this.type = type;
		this.topic = topic;
		this.question = question;
		this.correctAnswers = correctAnswers;
		this.choices = choices;
		this.hint = hint;
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
	
	protected ArrayList<String> getCorrectAnswers() {
		return correctAnswers;
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
		correctAnswers = arrayList;
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
	
	protected void addCorrectAnswer(String str) {
		correctAnswers.add(str);
	}
}