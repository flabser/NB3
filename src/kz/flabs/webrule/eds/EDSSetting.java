package kz.flabs.webrule.eds;

import kz.flabs.appenv.AppEnv;
import kz.flabs.servlets.eds.DigetsAlgType;
import kz.flabs.servlets.eds.ProviderType;
import kz.flabs.servlets.eds.SignAlgType;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.RunMode;
import org.w3c.dom.Node;

public class EDSSetting {
	public RunMode isOn;
	public String ca;
	public ProviderType provider;
	public DigetsAlgType digestAlg;
	public SignAlgType signAlg;
	public String defaultTokenPath = "";
    public String ocsp_url = "";

	public EDSSetting(Node node){		
		try{
			if (XMLUtil.getTextContent(node,"@mode", false).equalsIgnoreCase("off")){
				isOn = RunMode.OFF;					
			}else{
				isOn = RunMode.ON;	
				ca = XMLUtil.getTextContent(node, "ca", false);
                ocsp_url = XMLUtil.getTextContent(node, "ocsp");
				provider = ProviderType.valueOf(XMLUtil.getTextContent(node,"provider", true, "unknown", true));
				switch(provider){
				case JAVA_PROVIDER:
					digestAlg = DigetsAlgType.SHA1;
					signAlg = SignAlgType.SHA1withRSA;
					break;
				case IOLA:
					digestAlg = DigetsAlgType.SHA1;
					signAlg = SignAlgType.SHA1withRSA;
					break;
				default:
					AppEnv.logger.warningLogEntry("Support of digital signature has switched on, but provider has unsupported. Server have to switch off it");	
					isOn = RunMode.OFF;
				}
				defaultTokenPath = XMLUtil.getTextContent(node, "defaulttokenpath", true);
			}
		}catch(Exception e) {
			AppEnv.logger.warningLogEntry("Support of digital signature has switched on, but it has not correctly set. Server have to switch off it");		
		}
	}

}
