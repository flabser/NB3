package kz.flabs.webrule;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.lof.appenv.AppEnv;

import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.w3c.dom.Node;

public class RuleUser {
	public RunMode isOn = RunMode.ON;
	public String value = "";
	public ValueSourceType valueSource;

	public Class<GroovyObject> compiledClass;

	public RuleUser(Node node, String description) {
		try {

			if (!XMLUtil.getTextContent(node, "@mode", false).equalsIgnoreCase("ON")) {
				isOn = RunMode.OFF;
			}

			value = XMLUtil.getTextContent(node, ".", false);
			valueSource = ValueSourceType.valueOf(XMLUtil.getTextContent(node, "./@source", true, "STATIC", false));

			if (valueSource == ValueSourceType.MACRO) {

			} else if (valueSource == ValueSourceType.SCRIPT) {

				ClassLoader parent = getClass().getClassLoader();
				GroovyClassLoader loader = new GroovyClassLoader(parent);
				try {

				} catch (MultipleCompilationErrorsException e) {
					AppEnv.logger.errorLogEntry("Compilation error rule=" + description + ":" + e.getMessage());
					isOn = RunMode.OFF;
				}
			}
		} catch (Exception e) {
			AppEnv.logger.errorLogEntry(e);

		}
	}

	@Override
	public String toString() {
		return "valuesource=" + valueSource + ", value=" + value;
	}

}
