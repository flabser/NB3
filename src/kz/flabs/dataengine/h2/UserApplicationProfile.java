package kz.flabs.dataengine.h2;

import java.io.Serializable;
import java.util.ArrayList;

import kz.flabs.util.Util;
import kz.nextbase.script._IXMLContent;

public class UserApplicationProfile implements Serializable, _IXMLContent {

	private static final long serialVersionUID = 1L;

	public static final int LOGIN = 0;
	public static final int LOGIN_AND_QUESTION = 1;

	public int loginMode = LOGIN;
	public String appName;

	private ArrayList<QuestionAnswer> questionAnswer;

	public UserApplicationProfile(String string, int int1) {
		appName = string;
		loginMode = int1;

	}

	@Override
	public String toXML() {
		String qaText = "";
		if (loginMode == 1) {

			for (QuestionAnswer qa : getQuestionAnswer()) {
				qaText += "<entry><question>" + qa.controlQuestion + "</question><answer>" + qa.answer + "</answer></entry>";
			}
		}
		return "<entry><appname>" + appName + "</appname><loginmode></loginmode>" + qaText + "</entry>";
	}

	public QuestionAnswer getSomeQuestion() {
		return getQuestionAnswer().get(Util.getRandomNumber(getQuestionAnswer().size()));
	}

	public ArrayList<QuestionAnswer> getQuestionAnswer() {
		return questionAnswer;
	}

	public void setQuestionAnswer(ArrayList<QuestionAnswer> questionAnswer) {
		this.questionAnswer = questionAnswer;
	}

	public class QuestionAnswer {
		public String controlQuestion;
		public String answer;

		public QuestionAnswer(String controlQuestion, String answer) {
			this.controlQuestion = controlQuestion;
			this.answer = answer;

		}

	}

	@Override
	public Object toJSON() {
		return null;
	}

}