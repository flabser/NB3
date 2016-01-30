package kz.pchelka.messenger.robot;

import java.util.*;
import kz.flabs.localization.Language;
import kz.flabs.util.XMLUtil;
import kz.pchelka.messenger.robot.Reaction.Phrase;

import org.w3c.dom.*;

public class Robot {
	public HashMap<String, Reaction> reactions = new HashMap<String, Reaction>();

	public Robot(Document doc) {
		org.w3c.dom.Element root = doc.getDocumentElement();
		Language primaryLang = Language.valueOf(XMLUtil.getTextContent(doc,"/robot/@primary",true,"UNKNOWN", false).toUpperCase());


		NodeList reactionList = root.getElementsByTagName("reaction");		
		for(int i = 0; i < reactionList.getLength();i++){			
			Reaction reaction = new Reaction(reactionList.item(i), primaryLang.toString());			
			if (reaction.isOn){
				for(Phrase phrase:reaction.phrases){
					reactions.put(phrase.value, reaction);
				}
			}
		}
	}


}
