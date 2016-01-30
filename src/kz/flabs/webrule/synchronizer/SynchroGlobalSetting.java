package kz.flabs.webrule.synchronizer;

import kz.flabs.appenv.AppEnv;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.RunMode;
import org.w3c.dom.Node;

public class SynchroGlobalSetting {
	public RunMode isOn = RunMode.ON;
	public RunMode isSneakernetOn = RunMode.ON;
	public RunMode isEmailOn = RunMode.ON;
	public RunMode isOnlineOn = RunMode.ON;
	public String name;
	public String sneakernetPriority;
	public String exportdir;
	public String importdir;
	public String emailPriority;
	public String onlinePriority;
	public String host;
	public String port;
	

	public SynchroGlobalSetting() {
		isOn = RunMode.OFF;	
	}

	public SynchroGlobalSetting(Node node){		
		try{
			
			if (XMLUtil.getTextContent(node,"@mode", false).equals("off")){                    
				isOn = RunMode.OFF;				
			}
			
			name = XMLUtil.getTextContent(node, "name", false);
			
			if(XMLUtil.getTextContent(node, "sync/sneakernet/@mode", false).equals("off")){
				isSneakernetOn = RunMode.OFF;
			}
			
			sneakernetPriority = XMLUtil.getTextContent(node, "sync/sneakernet/priority", false);
			exportdir = XMLUtil.getTextContent(node, "sync/sneakernet/exportdir", false);
			importdir = XMLUtil.getTextContent(node, "sync/sneakernet/importdir", false);
			
			if(XMLUtil.getTextContent(node, "sync/email/@mode", false).equals("off")){
				isEmailOn = RunMode.OFF;
			}
			
			emailPriority = XMLUtil.getTextContent(node, "sync/sneakernet/priority", false);
			
			if(XMLUtil.getTextContent(node, "sync/online/@mode", false).equals("off")){
				isOnlineOn = RunMode.OFF;
			}
			
			onlinePriority = XMLUtil.getTextContent(node, "sync/online/priority", false);
			host = XMLUtil.getTextContent(node, "sync/online/host", false);
			port = XMLUtil.getTextContent(node, "sync/online/port", false);


		}catch(Exception e) {     
			AppEnv.logger.errorLogEntry(e);			
		}
	}



}
