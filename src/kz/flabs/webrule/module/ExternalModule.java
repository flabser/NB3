package kz.flabs.webrule.module;

import org.w3c.dom.Node;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.RunMode;

public class ExternalModule {
	ExternalModuleType type;
	String name;
	public RunMode isOn = RunMode.ON;
	
	public ExternalModule(Node node){
		type = ExternalModuleType.valueOf(XMLUtil.getTextContent(node,"@type", false));
		name = XMLUtil.getTextContent(node,".", false);
	}

	public ExternalModuleType getType() {
		return type;
	}

	public String getName() {
		return name;
	}
}
