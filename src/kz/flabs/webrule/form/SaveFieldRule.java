package kz.flabs.webrule.form;

import org.w3c.dom.Node;
import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.sourcesupplier.Macro;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.FieldType;
import kz.flabs.webrule.constants.ValueSourceType;

public class SaveFieldRule implements ISaveField, Const{
	public String name;
	public String value;
	public boolean isOn = true; 
	public boolean isValid = true;
	public ValueSourceType valueSourceType;
	public String description;
	public String documentField;
	public FieldType type = FieldType.TEXT;
	public Macro macro;
	public String fieldSaveCondition = "";	
	public String ifErrorValue;

	SaveFieldRule(Node node){		
		try{
			value = XMLUtil.getTextContent(node,"value", false);

			if (!XMLUtil.getTextContent(node,"@mode", false).equals("on")){                    
				isOn = false;
				return;
			}			
			
			valueSourceType = ValueSourceType.valueOf(XMLUtil.getTextContent(node,"value/@source",true,"STATIC", false));
			
			if (valueSourceType == ValueSourceType.MACRO){		
				if (value.equalsIgnoreCase("has_attachment")){
					name = "hasresponse";
					macro = Macro.HAS_ATTACHMENT;
				}else if (value.equalsIgnoreCase("has_response")){
					name = "hasresponse";
					macro = Macro.HAS_RESPONSE;
				}else if (value.equalsIgnoreCase("author")){
					name = "author";
					macro = Macro.AUTHOR;
				}else if (value.equalsIgnoreCase("current_user")) {
					name = "current_user";
					macro = Macro.CURRENT_USER;
				}
			}
					
			documentField = XMLUtil.getTextContent(node,"docfield", false);			
			description = XMLUtil.getTextContent(node,"description", false);
			name = documentField;
			ifErrorValue = XMLUtil.getTextContent(node,"iferror", false);		
			
			type = FieldType.valueOf(XMLUtil.getTextContent(node,"docfield/@type",true,"TEXT", false));

		}catch(Exception e) {     
			AppEnv.logger.errorLogEntry(e);			
			isValid = false;
		}
		
		
	}
	
	
	public String toString(){
		return "name:" + name + ",ison:" + isOn + ",value:" + value + ",iferror:" + ifErrorValue;
	}

	@Override
	public String getName() {	
		return documentField;
	}
	

	@Override
	public FieldType getType() {
		return type;
	}
	
	@Override
	public ValueSourceType getSourceType() {	
		return valueSourceType;
	}


	@Override
	public String getValue() {	
		return value;
	}


	@Override
	public Macro getMacro() {	
		return macro;
	}

	@Override
	public String getIfErrorValue() {	
		return ifErrorValue;
	}



}