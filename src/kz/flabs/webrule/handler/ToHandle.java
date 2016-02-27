package kz.flabs.webrule.handler;

import kz.flabs.sourcesupplier.DocumentCollectionMacro;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.IRuleValue;
import kz.flabs.webrule.constants.FieldType;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.lof.appenv.AppEnv;

import org.w3c.dom.Node;

public class ToHandle implements IRuleValue {
	public ValueSourceType valueSource;
	public String value;
	public DocumentCollectionMacro toHandleMacro;

	public ToHandle(Node node){		
		try{
			value = XMLUtil.getTextContent(node,".", true);
			valueSource = ValueSourceType.valueOf(XMLUtil.getTextContent(node,"@source",true,"STATIC", true));
			if (valueSource == ValueSourceType.MACRO){
				toHandleMacro = DocumentCollectionMacro.valueOf(value);
			}
		}catch(Exception e) {     
			AppEnv.logger.errorLogEntry(e);			

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
}
