package kz.flabs.webrule.form;

import java.util.ArrayList;
import kz.flabs.appenv.AppEnv;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.RuleUser;
import kz.flabs.webrule.constants.ActionType;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.constants.ValueSourceType;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FormActionRule {
	public RunMode isOn = RunMode.ON;
	public ActionType type;
	public ArrayList<RuleUser> granted = new ArrayList<RuleUser>();
	public boolean hasCaptionValue;
	public String captionValue = "";
	public ValueSourceType captionValueSource;
	
	public FormActionRule(Node node){
		try{
			type = ActionType.valueOf(XMLUtil.getTextContent(node,"@type",true,"UNKNOWN", false));
			
			if (!XMLUtil.getTextContent(node,"@mode", false).equalsIgnoreCase("ON")){                    
				isOn = RunMode.OFF;			
			}
			
			try{						
				NodeList fields =  XMLUtil.getNodeList(node,"granted");   
				for(int i = 0; i < fields.getLength(); i++){
					RuleUser df = new RuleUser(fields.item(i), toString());	
					if (df.isOn == RunMode.ON){
						granted.add(df);
					}
				}
			} catch(Exception e) {                
				AppEnv.logger.errorLogEntry(e);
			}

			captionValue = XMLUtil.getTextContent(node,"caption", false);
			if (!captionValue.equalsIgnoreCase("")){
				captionValueSource = ValueSourceType.valueOf(XMLUtil.getTextContent(node,"caption/@source",true,"STATIC", false));
				hasCaptionValue = true;
			}
			
		}catch(Exception e) {     
			AppEnv.logger.errorLogEntry(e);			
		
		}
	}

	public String toString(){
		return "type=" + type + ", granted=" + granted;
	}
	
}
