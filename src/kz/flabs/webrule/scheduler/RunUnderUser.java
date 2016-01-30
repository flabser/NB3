package kz.flabs.webrule.scheduler;

import org.w3c.dom.Node;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.IRuleValue;
import kz.flabs.webrule.constants.FieldType;
import kz.flabs.webrule.constants.ValueSourceType;

public class RunUnderUser implements IRuleValue {
	public ValueSourceType valueSource;
	public String value;
	public String macro;

	public RunUnderUser(Node node){		
		try{
			value = XMLUtil.getTextContent(node,".", true);
			valueSource = ValueSourceType.valueOf(XMLUtil.getTextContent(node,"./@source",true,"STATIC", true));
		}catch(Exception e) {     
			value = "CURRENT_USER";
			valueSource = ValueSourceType.MACRO;
			macro = "CURRENT_USER";
			
		}
	}

	@Override
	public ValueSourceType getSourceType() {	
		return valueSource;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public Enum getValueType() {	
		return FieldType.TEXT;
	}
	
	public String toString(){
		return "source=" + valueSource + ", value=" + value;
	}
	
}
