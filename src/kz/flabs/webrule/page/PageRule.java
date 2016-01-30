package kz.flabs.webrule.page;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.exception.RuleException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.Rule;
import kz.flabs.webrule.constants.RuleType;
import kz.flabs.webrule.constants.RunMode;

import org.w3c.dom.NodeList;

public class PageRule extends Rule implements IElement, Const {
	public boolean isValid;
	public ArrayList<ElementRule> elements = new ArrayList<ElementRule>();

	public boolean qoEnable;
	public String qoClassName;
	public CachingStrategyType caching = CachingStrategyType.NO_CACHING;

	public PageRule(AppEnv env, File ruleFile) throws RuleException {
		super(env, ruleFile);
		try {

			String cachingValue = XMLUtil.getTextContent(doc, "/rule/caching", false);
			if (!cachingValue.equalsIgnoreCase("")) {
				caching = CachingStrategyType.valueOf(cachingValue);
			}

			NodeList fields = XMLUtil.getNodeList(doc, "/rule/element");
			for (int i = 0; i < fields.getLength(); i++) {
				ElementRule element = new ElementRule(fields.item(i), this);
				if (element.isOn != RunMode.OFF && element.isValid) {
					elements.add(element);
				}
			}

			type = RuleType.PAGE;
			if (xsltFile.equalsIgnoreCase("default") || xsltFile.equals("*")) {
				xsltFile = type.name().toLowerCase() + File.separator + id + ".xsl";
			}
			isValid = true;
		} catch (Exception e) {
			AppEnv.logger.errorLogEntry(e);
		}
	}

	@Override
	public String toString() {
		return "PAGE id=" + id + ", ison=" + isOn;
	}

	@Override
	public boolean save() {
		return false;
	}

	@Override
	public String getRuleAsXML(String app) {
		String xmlText = "";

		xmlText = "<ison>" + isOn + "</ison>" + "<rununderuser>" + runUnderUser + "</rununderuser>" + "<cache>" + caching + "</cache>" + "<elements>"
				+ elements + "</elements>" + "<hits>" + hits + "</hits>" + "<app>" + app + "</app>";

		return xmlText;
	}

	@Override
	public void update(Map<String, String[]> fields) throws WebFormValueException {

	}

}
