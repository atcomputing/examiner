package nl.atcomputing.lpic1examtrainer;

import java.util.ArrayList;

/**
 * @author martijn
 *
 */
public class ExamQuestion {
	private String type;
	private String topic;
	private String question;
	private String exhibit;
	private ArrayList<String> correctAnswers;
	private ArrayList<String> answers;
	
	protected ExamQuestion() {
		type = null;
		topic = null;
		question = null;
	}
	
	
	/**
	 * @brief Creates a new ExamQuestion object
	 * @param type
	 * @param topic
	 * @param exhibit
	 * @param question
	 * @param correct_answer
	 * @param answers
	 */
	protected ExamQuestion(String type, String topic, String exhibit, String question, 
			ArrayList<String> correctAnswers, ArrayList<String> answers) {
		this.type = type;
		this.topic = topic;
		this.question = question;
		this.correctAnswers = correctAnswers;
		this.answers = answers;
	}
	
	protected String getType() {
		return type;
	}
	
	protected String getTopic() {
		return topic;
	}
	
	protected ArrayList<String> getAnswers() {
		return answers;
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
	
	protected void setType(String str) {
		type = str;
	}
	
	protected void setTopic(String str) {
		topic = str;
	}
	
	protected void setAnswers(ArrayList<String> arrayList) {
		answers = arrayList;
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
	
	protected void addAnswer(String str) {
		str.replaceAll(", ", ",\\ ");
		answers.add(str);
	}
	
	protected void addCorrectAnswer(String str) {
		str.replaceAll(", ", ",\\ ");
		correctAnswers.add(str);
	}
}