package kz.flabs.webrule.form;

import org.w3c.dom.Node;
import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.sourcesupplier.Macro;
import kz.flabs.webrule.constants.ValueSourceType;

public class ShowFieldRule extends ShowField implements Const{
	public String fieldSaveCondition = "";
	
	ShowFieldRule(Node node, String description){
		super(node, description);
		try{
			
		
		 if (valueSourceType == ValueSourceType.MACRO){			
				if(value.equalsIgnoreCase("view_text")){
					macro = Macro.VIEW_TEXT;
				}else if (value.equalsIgnoreCase("has_attachment")){
					macro = Macro.HAS_ATTACHMENT;
				}else if (value.equalsIgnoreCase("has_response")){
					macro = Macro.HAS_RESPONSE;
				}else if (value.equalsIgnoreCase("author")){
					macro = Macro.AUTHOR;
				}else if (value.equalsIgnoreCase("executors")){
					macro = Macro.EXECUTORS;
				}else if (value.equalsIgnoreCase("recipients")){
					macro = Macro.RECIPIENTS;
				}else if (value.equalsIgnoreCase("coord_blocks")){
					macro = Macro.COORD_BLOCKS;
				}else if (value.equalsIgnoreCase("versions")){
					macro = Macro.VERSIONS;
				}else if (value.equalsIgnoreCase("control")){
					macro = Macro.CONTROL;				
				}else if (value.equalsIgnoreCase("app_url")) {
					macro = Macro.APP_URL;
				}else if (value.equalsIgnoreCase("filter")) {
					macro = Macro.FILTER;
				}else if (value.equalsIgnoreCase("current_user_position")){
					macro = Macro.CURRENT_USER_POSITION;
				}else{
					name = "error";
					macro = Macro.UNKNOWN_MACRO;
				}
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