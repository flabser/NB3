package kz.flabs.webrule.form;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import kz.flabs.sourcesupplier.Macro;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.constants.TagPublicationFormatType;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.lof.appenv.AppEnv;

import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.w3c.dom.Node;

public class ShowField implements IShowField {
	public String value = "";
	public Class<GroovyObject> scriptClass;
	public ValueSourceType valueSourceType;
	public TagPublicationFormatType publicationFormat;
	public String attrValue = "";
	public TagPublicationFormatType attrValueType;
	public Macro macro;
	public Macro attrMacro;
	public String name;
	public RunMode isOn = RunMode.ON;
	public boolean isValid = true;
	public String description;
	public boolean hasAttrValue;
	public boolean hasCaptionValue;
	public String captionValue = "";
	public ValueSourceType captionValueSource;
	public boolean toSign;

	protected ValueSourceType attrValueSource;

	public ShowField(Node node, String description) {
		try {
			name = XMLUtil.getTextContent(node, "name", false);
			String mode = XMLUtil.getTextContent(node, "@mode", false);
			if (mode.equalsIgnoreCase("off")) {
				isOn = RunMode.OFF;
				return;
			} else if (mode.equalsIgnoreCase("hide")) {
				isOn = RunMode.HIDE;
			}

			if (XMLUtil.getTextContent(node, "@tosign", false).equals("on")) {
				toSign = true;
			}

			description = XMLUtil.getTextContent(node, "description", false);

			publicationFormat = TagPublicationFormatType.valueOf(XMLUtil.getTextContent(node, "@publishas", true, "AS_IS", false));
			value = XMLUtil.getTextContent(node, "value", false);
			valueSourceType = ValueSourceType.valueOf(XMLUtil.getTextContent(node, "value/@source", true, "STATIC", false));
			if (valueSourceType == ValueSourceType.MACRO) {
				macro = Macro.valueOf(value.toUpperCase());
			} else if (valueSourceType == ValueSourceType.SCRIPT) {
				ClassLoader parent = getClass().getClassLoader();
				GroovyClassLoader loader = new GroovyClassLoader(parent);
				try {

				} catch (MultipleCompilationErrorsException e) {
					AppEnv.logger.errorLogEntry("Show field script compilation error at form rule compiling=" + description + ":" + e.getMessage());
				}
			} else if (valueSourceType == ValueSourceType.DOCFIELD) {
				valueSourceType = ValueSourceType.DOC_FIELD;
			}

			captionValue = XMLUtil.getTextContent(node, "caption", false);
			if (!captionValue.equalsIgnoreCase("")) {
				captionValueSource = ValueSourceType.valueOf(XMLUtil.getTextContent(node, "caption/@source", true, "STATIC", false));
				hasCaptionValue = true;
			}

			attrValue = XMLUtil.getTextContent(node, "attrvalue", false);
			attrValueType = TagPublicationFormatType.valueOf(XMLUtil.getTextContent(node, "attrvalue/@type", true, "AS_IS", false));
			if (!attrValue.equals("")) {
				hasAttrValue = true;
				attrValueSource = ValueSourceType.valueOf(XMLUtil.getTextContent(node, "attrvalue/@source", true, "STATIC", false));
				if (attrValueSource == ValueSourceType.MACRO) {
					if (value.equalsIgnoreCase("view_text")) {
						attrMacro = Macro.VIEW_TEXT;
					} else if (value.equalsIgnoreCase("has_attachment")) {
						attrMacro = Macro.HAS_ATTACHMENT;
					} else if (value.equalsIgnoreCase("has_response")) {
						attrMacro = Macro.HAS_RESPONSE;
					} else if (value.equalsIgnoreCase("author")) {
						attrMacro = Macro.AUTHOR;
					} else if (value.equalsIgnoreCase("filter")) {
						attrMacro = Macro.FILTER;
					}
				}
			}

		} catch (Exception e) {
			AppEnv.logger.errorLogEntry(e);
			isValid = false;
		}
	}

	@Override
	public ValueSourceType getSourceType() {
		return valueSourceType;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public Class<GroovyObject> getCompiledClass() {
		return scriptClass;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public TagPublicationFormatType getAttrValueType() {
		return attrValueType;
	}

	@Override
	public TagPublicationFormatType getPublishAs() {
		return publicationFormat;
	}

	@Override
	public Macro getMacro() {
		return macro;
	}

	@Override
	public Macro getAttrMacro() {
		return attrMacro;
	}

	@Override
	public ValueSourceType getAttrSourceType() {
		return attrValueSource;
	}

	@Override
	public String getAttrValue() {
		return attrValue;
	}

	@Override
	public String getCaptionValue() {
		return captionValue;
	}

	@Override
	public ValueSourceType getCaptionValueSource() {
		return captionValueSource;
	}

	@Override
	public boolean hasAttrValue() {
		return hasAttrValue;
	}

}
