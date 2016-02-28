package kz.flabs.dataengine.h2;

import kz.flabs.util.Util;
import kz.nextbase.script._IXMLContent;

import java.io.Serializable;
import java.util.ArrayList;

public class UserApplicationProfile implements Serializable, _IXMLContent{
	
	private static final long serialVersionUID = 1L;
	
	public static final int LOGIN = 0;
	public static final int LOGIN_AND_QUESTION = 1;
	public LoginModeType loginMod = LoginModeType.LOGIN_AND_REDIRECT ;
	public int loginMode = LOGIN;
	public String appName;

	private ArrayList<QuestionAnswer> questionAnswer;
	
	public UserApplicationProfile(String string, int int1) {
		appName = string;
		loginMode = int1;
		if (loginMode == 1){
			setQuestionAnswer(new ArrayList<QuestionAnswer>());
			loginMod = LoginModeType.LOGIN_AND_QUESTION; 
		}else if(loginMode == 2){
			loginMod = LoginModeType.JUST_LOGIN; 
		}
	}
	
	
	public String toXML(){
		String qaText = "";
		if (loginMode == 1){
			loginMod = LoginModeType.LOGIN_AND_QUESTION;
			for(QuestionAnswer qa : getQuestionAnswer()){
				qaText += "<entry><question>" + qa.controlQuestion + "</question><answer>" + qa.answer + "</answer></entry>";
			}
		}
		return "<entry><appname>" + appName + "</appname><loginmode>" + loginMod + "</loginmode>" + qaText + "</entry>";
	}

	public QuestionAnswer getSomeQuestion(){
		return getQuestionAnswer().get(Util.getRandomNumber(getQuestionAnswer().size()));
	}
	
	public ArrayList<QuestionAnswer> getQuestionAnswer() {
		return questionAnswer;
	}


	public void setQuestionAnswer(ArrayList<QuestionAnswer> questionAnswer) {
		this.questionAnswer = questionAnswer;
	}

	public class QuestionAnswer{
		public String controlQuestion;
		public String answer;
		
		public QuestionAnswer(String controlQuestion, String answer){
			this.controlQuestion = controlQuestion;
			this.answer = answer;
			
		}
		
	}

	@Override
    public Object toJSON() {
	    // TODO Auto-generated method stub
	    return null;
    }
	
}