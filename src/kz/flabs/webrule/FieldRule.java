package kz.flabs.webrule;

import kz.flabs.util.XMLUtil;
import kz.lof.appenv.AppEnv;

public class FieldRule{
	public String name;
	public boolean isOn = true; 
	public boolean isValid;		
	public String value = "";
	public String attrName = "attr";
	public String attrValue = "";		
	public String description;

	public FieldRule(org.w3c.dom.Node subRuledDoc){
		try{
			name = XMLUtil.getTextContent(subRuledDoc,"name", false);

			if (XMLUtil.getTextContent(subRuledDoc,"../@mode", false).equals("off")){                    
				isOn = false;
				return;
			}
			
			value = XMLUtil.getTextContent(subRuledDoc,"value", false);		
			description = XMLUtil.getTextContent(subRuledDoc,"description", false);
			
			try{
				attrName = XMLUtil.getTextContent(subRuledDoc, "attrname", false);
				attrValue = XMLUtil.getTextContent(subRuledDoc, "attrvalue", false);		
			}catch(Exception e){
				attrName = ""; attrValue = "";
			}

			isValid = true;
		} catch(Exception ne) {         
			AppEnv.logger.errorLogEntry(ne);				
		}

	}	
	public String toString(){
		return "name=" + name;
	}
}