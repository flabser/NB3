package kz.nextbase.script.struct;

import kz.flabs.dataengine.h2.LoginModeType;
import kz.flabs.dataengine.h2.UserApplicationProfile;

public class _UserApplicationProfile {
	UserApplicationProfile profile;
	
	public _UserApplicationProfile(String string, String lm) {
		 int loginMode = 0;
         try{
             loginMode = Integer.parseInt(lm);
         }catch(NumberFormatException e){
             loginMode = 0;
         }
		profile = new UserApplicationProfile(string, loginMode);
	}
	
	public void addQuestionAnswer(String[] q, String[] a){
		for(int i1 = 0; i1 < q.length; i1 ++){
			UserApplicationProfile.QuestionAnswer qa = profile.new QuestionAnswer(q[i1].trim(), a[i1].trim());
			profile.getQuestionAnswer().add(qa);
		}		
	}
	
	public String getAppName(){
		return profile.appName;
	}
	
	public boolean needQuestAnsw(){
		if (profile.loginMod == LoginModeType.LOGIN_AND_QUESTION){
			return true;
		}else{
			return false;
		}
	}
	
}
