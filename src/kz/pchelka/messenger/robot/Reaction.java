package kz.pchelka.messenger.robot;

import java.util.ArrayList;
import java.util.HashMap;
import kz.flabs.appenv.AppEnv;
import kz.flabs.localization.Language;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.ValueSourceType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Reaction {
	public boolean isOn = true; 
	public boolean isValid;	
	public ArrayList<Phrase> phrases = new ArrayList<Phrase>();
	public ValueSourceType actionSource;
	public String actionValue;
	public HashMap<AnswerResultType, Answer> answers = new HashMap<AnswerResultType, Answer>();
	
	Reaction(Node node, String primaryLang){
		try{
			if (!XMLUtil.getTextContent(node,"@mode", false).equals("on")){                    
				isOn = false;
				return;
			}
			
			NodeList phrasesList =  XMLUtil.getNodeList(node,"phrase");   
			for(int i = 0; i < phrasesList.getLength(); i++){
				Node wordNode = phrasesList.item(i);
				Language l = Language.valueOf(XMLUtil.getTextContent(wordNode,"@lang",true,"UNKNOWN", false).toUpperCase());
				Phrase c = new Phrase( l, wordNode.getTextContent());
				phrases.add(c);				
			}
			
			actionSource = ValueSourceType.valueOf(XMLUtil.getTextContent(node,"@source",true,"UNKNOWN", false).toUpperCase());
			actionValue = XMLUtil.getTextContent(node,".", false);
			
			NodeList answersList =  XMLUtil.getNodeList(node,"action/answer");   
			for(int i = 0; i < answersList.getLength(); i++){
				Node answerNode = answersList.item(i);			
				Answer a = new Answer(answerNode);
				answers.put(a.type, a);				
			}
			

		} catch(Exception e) {  
			AppEnv.logger.errorLogEntry(this.getClass().getSimpleName(),e);						
		}
	}

	public String toString(){
		return "phrases=" + phrases + ", action=" + actionValue;
	}
	
	class Phrase{
		public Language lang;
		public String value;
		
		public Phrase(Language lang, String value) {
			this.lang = lang;
			this.value = value;
		}
		
	}
	
	class Answer{
		public AnswerResultType type;
		public String value;
		//public HashMap<Language, AnswerAction> answers = new HashMap<Language, AnswerAction>();
		public ArrayList<AnswerAction> actions = new ArrayList<AnswerAction>();
		
		public Answer(Node node) {
			type = AnswerResultType.valueOf(XMLUtil.getTextContent(node,"@type",true,"UNKNOWN", false).toUpperCase());
			NodeList actionList =  XMLUtil.getNodeList(node,"action");   
			for(int i = 0; i < actionList.getLength(); i++){
				Node actionNode = actionList.item(i);			
				AnswerAction a = new AnswerAction(actionNode);
				actions.add(a);				
			}
				
		}
		
		class AnswerAction{
			public Language lang;
			public AnswerActionType type;
			public String value;
			
			public AnswerAction(Node node) {				
				this.type = AnswerActionType.valueOf(XMLUtil.getTextContent(node,"@type",true,"UNKNOWN", false).toUpperCase());;
				this.lang = lang;
				this.value = node.getTextContent();
			}			
		}
	}
	
}
