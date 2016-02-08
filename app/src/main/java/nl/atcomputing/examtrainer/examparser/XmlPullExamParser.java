/**
 *
 * Copyright 2011 AT Computing BV
 *
 * This file is part of Examiner.
 *
 * Examiner is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Examiner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Examiner.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.atcomputing.examtrainer.examparser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import nl.atcomputing.examtrainer.main.ExamQuestion;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.util.Xml;

/**
 * @author martijn brekhof
 *
 */

public class XmlPullExamParser {
	private ArrayList<ExamQuestion> examQuestions = new ArrayList<ExamQuestion>();
	private Context context;
	private final URL url;

	// names of the XML tags
	private static final String ITEM = "item";
	private static final String ITEM_TYPE = "type";
	private static final String ITEM_TOPIC = "topic";
	private static final String ITEM_EXHIBIT = "exhibit";
	private static final String ITEM_QUESTION = "question";
	private static final String ITEM_CHOICE = "choice";
	private static final String ITEM_CORRECT_ANSWER = "correct_answer";
	private static final String ITEM_HINT = "hint";

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

	private InputStream getInputStream() throws IOException {
		if(url.getProtocol().equals("file")) {
			return context.getApplicationContext().getAssets().open(url.getFile().replaceFirst("^/", ""));
		}
		else {
			return url.openConnection().getInputStream();
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
					default: // continue
				}
				eventType = parser.next();
			}
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
				default: //continue
			}
			eventType = parser.next();
		}
		if ( eventType == XmlPullParser.END_DOCUMENT ) {
			throw new RuntimeException("End of document reached while parsing item");
		}
		return null;
	}

}
