package kz.flabs.webrule.synchronizer;

import kz.flabs.webrule.ValueAdapter;
import kz.flabs.webrule.constants.FieldType;
import kz.flabs.webrule.constants.ValueSourceType;

public class DropEventField extends ValueAdapter {
	
	public DropEventField(String value, String st, String ft){
		this.value = value;
		sourceType = ValueSourceType.valueOf(st);
		fieldType = FieldType.valueOf(ft);

		if(sourceType == ValueSourceType.MACRO){
			/*if(value.equalsIgnoreCase("view_text")){				
				macro = DocumentMacro.VIEW_TEXT;
			}else if (value.equalsIgnoreCase("has_attachment")){			
				macro = DocumentMacro.HAS_ATTACHMENT;
			}else if (value.equalsIgnoreCase("has_response")){			
				macro = DocumentMacro.HAS_RESPONSE;
			}else if (value.equalsIgnoreCase("author")){		
				macro = DocumentMacro.AUTHOR;
			}*/
		}

	}	
	
}
