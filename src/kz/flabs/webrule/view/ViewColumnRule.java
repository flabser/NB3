package kz.flabs.webrule.view;

import org.w3c.dom.Node;

import kz.flabs.appenv.AppEnv;
import kz.flabs.util.Util;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.ValueSourceType;

public class ViewColumnRule {
	public String name;
	public boolean hasCaptionValue;
	public String captionValue = "";
	public String captionId = "";
	public ValueSourceType captionValueSource;
	
	ViewColumnRule(Node node){
		try{		
		
			name = Util.generateRandomAsText();

			captionValue = XMLUtil.getTextContent(node,"caption", false);
			if (!captionValue.equalsIgnoreCase("")){
				captionId = XMLUtil.getTextContent(node,"caption/@id",true,Util.generateRandomAsText(), false);
				captionValueSource = ValueSourceType.valueOf(XMLUtil.getTextContent(node,"caption/@source",true,"STATIC", false));
				hasCaptionValue = true;
			}

		}catch(Exception e) {     
			AppEnv.logger.errorLogEntry(e);			
			
		}
	}
}
