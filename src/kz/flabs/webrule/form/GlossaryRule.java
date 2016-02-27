package kz.flabs.webrule.form;

import org.w3c.dom.Node;

import kz.flabs.dataengine.Const;
import kz.flabs.sourcesupplier.Macro;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.lof.appenv.AppEnv;

public class GlossaryRule implements Const{
	public String name;
	public ValueSourceType valueSource;
	public String value;
	public boolean isOn = true; 
	public boolean isValid = true;	
	public String description;
	public Macro macro;
	   
	public GlossaryRule(Node node){		
		try{			
			name = XMLUtil.getTextContent(node,"@id", false);
			value = XMLUtil.getTextContent(node,"value", false);	
			description = XMLUtil.getTextContent(node,"description", false);
			
			if (!XMLUtil.getTextContent(node,"@mode", false).equals("on")){                    
				isOn = false;
				return;
			}							
			
			valueSource = ValueSourceType.valueOf(XMLUtil.getTextContent(node,"@source",true,"STATIC", false));
			if (valueSource == ValueSourceType.MACRO){
				macro = Macro.valueOf(XMLUtil.getTextContent(node,".",true,Macro.UNKNOWN_MACRO.toString(), false));
			}
			
			
		}catch(Exception e) {     
			AppEnv.logger.errorLogEntry(e);			
			isValid = false;
		}
	}
	
	
	public String toString(){
		return "name=" + name + ", ison=" + isOn + ", value=" + value;
	}
}