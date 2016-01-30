package kz.flabs.webrule.form;

import org.w3c.dom.Node;
import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.sourcesupplier.Macro;
import kz.flabs.webrule.constants.ValueSourceType;

public class DefaultFieldRule extends ShowField implements Const{
	
	public DefaultFieldRule(Node node, String description){	
		super(node, description);
		try{	
			
			 if (valueSourceType == ValueSourceType.MACRO){
				if(value.equalsIgnoreCase("current_time")){
					macro = Macro.MACRO_CURRENT_TIME;
				}else if (value.equalsIgnoreCase("current_user")){					
					macro = Macro.CURRENT_USER;
				}else if (value.equalsIgnoreCase("current_user_department")){
					macro = Macro.CURRENT_USER_DEPARTMENT;
				}else if (value.equalsIgnoreCase("current_user_position")){
					macro = Macro.CURRENT_USER_POSITION;
				}else if (value.equalsIgnoreCase("executors")){				
					macro = Macro.EXECUTORS;
				}else if (value.equalsIgnoreCase("coord_blocks")){				
					macro = Macro.COORD_BLOCKS;
				}else if (value.equalsIgnoreCase("control")){
					macro = Macro.CONTROL;
				}else{			
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