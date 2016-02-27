package kz.flabs.webrule.query;

import kz.flabs.dataengine.Const;
import kz.flabs.sourcesupplier.Macro;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.IRuleValue;
import kz.flabs.webrule.constants.FieldType;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.constants.TagPublicationFormatType;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.lof.appenv.AppEnv;

import org.w3c.dom.Node;

public class QueryFieldRule implements IRuleValue, Const{
	public String name;
	public RunMode isOn = RunMode.ON; 
	public boolean isValid;
	public String value;
	public String ifErrorValue;
	public ValueSourceType valueSource;
	public Macro macro = Macro.UNDEFINED;
	public String attrValue = "\"\"";
	public TagPublicationFormatType publicationFormat;	
	
	QueryFieldRule(Node node){
		try{
			if (!XMLUtil.getTextContent(node,"@mode", false).equals("on")){                    
				isOn = RunMode.OFF;
				return;
			}
			name = XMLUtil.getTextContent(node,"name", false);
			value = XMLUtil.getTextContent(node, "value", false);
			publicationFormat = TagPublicationFormatType.valueOf(XMLUtil.getTextContent(node,"@publishas",true,"AS_IS", false));
			ifErrorValue = XMLUtil.getTextContent(node, "iferror",false,null, false);
			valueSource = ValueSourceType.valueOf(XMLUtil.getTextContent(node,"value/@source",true,"STATIC", false));
			
			if (valueSource == ValueSourceType.MACRO){				
				if(value.equalsIgnoreCase("view_text")){
					name = "viewtext";
					macro = Macro.VIEW_TEXT;
				}else if (value.equalsIgnoreCase("has_attachment")){
					name = "hasattachment";
					macro = Macro.HAS_ATTACHMENT;
				}else if (value.equalsIgnoreCase("has_response")){
					name = "hasresponse";
					macro = Macro.HAS_RESPONSE;
				}
			}/*else if(valueSource == ValueSourceType.RESULTSET){
				name = value;
			}*/
			
			attrValue = XMLUtil.getTextContent(node, "attrvalue", false);		
			
			
			isValid = true;
		} catch(Exception e) {  
			AppEnv.logger.errorLogEntry(this.getClass().getSimpleName(),e);						
		}
	}

	@Override
	public ValueSourceType getSourceType() {
		return valueSource;
	}

	@Override
	public Enum getValueType() {	
		return FieldType.TEXT;
	}

	@Override
	public String getValue() {
		return value;
	}
	
	public String toXML(){
		return "<name ison=\"" + isOn + "\">" + name + "</name><value>" + value + "</value><valuesource>" + valueSource + "</valuesource>" +
				"<macro>" + macro + "</macro><publishas>" + publicationFormat + "</publishas><iferror>" + ifErrorValue + "</iferror>";
	}
}
