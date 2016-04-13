package kz.lof.rule.page;

import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.w3c.dom.Node;

import kz.flabs.dataengine.Const;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.lof.appenv.AppEnv;

public class ElementRule implements Const {
	public ElementType type;
	public boolean isValid = true;
	public String name;
	public boolean hasElementName;
	public RunMode isOn;
	public String value;
	public ElementScript doClassName;

	private IElement parentRule;

	public ElementRule(Node node, IElement parent) {
		parentRule = parent;
		try {
			name = XMLUtil.getTextContent(node, "name", false);
			if (!name.equals("")) {
				hasElementName = true;
			}
			String mode = XMLUtil.getTextContent(node, "@mode", false);
			if (mode.equalsIgnoreCase("off")) {
				isOn = RunMode.OFF;
				return;
			}

			type = ElementType.valueOf(XMLUtil.getTextContent(node, "@type", true, "UNKNOWN", false));
			switch (type) {
			case SCRIPT:
				Node qoNode = XMLUtil.getNode(node, "events/doscript", false);
				doClassName = getClassName(qoNode, "doscript");
				if (doClassName == null) {
					isValid = false;
				}

				break;

			case INCLUDED_PAGE:
				value = XMLUtil.getTextContent(node, "value", false);
				break;

			default:
				break;
			}

		} catch (Exception e) {
			AppEnv.logger.errorLogEntry(e);
			isValid = false;
		}
	}

	@Override
	public String toString() {
		return "name=\"" + name + "\", value=" + value;
	}

	@SuppressWarnings({ "unchecked", "resource" })
	private ElementScript getClassName(Node node, String normailzator) {
		String value = XMLUtil.getTextContent(node, ".", true);
		ValueSourceType qsSourceType = ValueSourceType.valueOf(XMLUtil.getTextContent(node, "@source", true, "STATIC", true));
		try {
			if (qsSourceType == ValueSourceType.GROOVY_FILE || qsSourceType == ValueSourceType.FILE || qsSourceType == ValueSourceType.JAVA_CLASS) {
				return new ElementScript(qsSourceType, value);
			} else {
				AppEnv.logger.errorLogEntry("Included script did not implemented, form rule=" + parentRule.getID() + ", node=" + node.getBaseURI());
			}

		} catch (MultipleCompilationErrorsException e) {
			AppEnv.logger.errorLogEntry("Script compilation error at form rule compiling=" + parentRule.getID() + ", node=" + node.getBaseURI());
			AppEnv.logger.errorLogEntry(e.getMessage());
		}
		return null;
	}

}