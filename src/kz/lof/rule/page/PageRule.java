package kz.lof.rule.page;

import java.io.File;
import java.util.ArrayList;

import kz.flabs.dataengine.Const;
import kz.flabs.exception.RuleException;
import kz.flabs.servlets.PublishAsType;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.Rule;
import kz.flabs.webrule.constants.RuleType;
import kz.flabs.webrule.constants.RunMode;
import kz.lof.appenv.AppEnv;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;

import org.apache.commons.lang3.ArrayUtils;
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

			String xsltAppsPath = "";
			if (Environment.isDevMode()) {
				if (ArrayUtils.contains(EnvConst.OFFICEFRAME_APPS, env.appName)) {
					xsltAppsPath = Environment.getOfficeFrameDir() + "webapps" + File.separator + env.appName + File.separator + "xsl";
				} else {
					xsltAppsPath = "webapps" + File.separator + env.appName + File.separator + "xsl";
				}
			} else {
				xsltAppsPath = "webapps" + File.separator + env.appName + File.separator + "xsl";
			}

			type = RuleType.PAGE;

			// TODO need to improve
			xsltFile = XMLUtil.getTextContent(doc, "/rule/xsltfile");
			if (!xsltFile.equals("")) {
				publishAs = PublishAsType.HTML;
				if (xsltFile.equalsIgnoreCase("default") || xsltFile.equals("*")) {
					xsltFile = xsltAppsPath + File.separator + type.name().toLowerCase() + File.separator + id + ".xsl";
				} else if (xsltFile.equalsIgnoreCase("default_staff")) {
					String xsltStaffAppsPath = "";
					if (Environment.isDevMode()) {
						xsltStaffAppsPath = Environment.getOfficeFrameDir() + "webapps" + File.separator + EnvConst.STAFF_APP_NAME + File.separator
						        + "xsl";
					} else {
						xsltStaffAppsPath = "webapps" + File.separator + EnvConst.STAFF_APP_NAME + File.separator + "xsl";
					}
					xsltFile = xsltStaffAppsPath + File.separator + type.name().toLowerCase() + File.separator + id + ".xsl";
				} else if (xsltFile.equalsIgnoreCase("default_MunicipalProperty")) {
					String xsltStaffAppsPath = "";
					if (Environment.isDevMode()) {
						xsltStaffAppsPath = Environment.getOfficeFrameDir() + "webapps" + File.separator + "MunicipalProperty" + File.separator
						        + "xsl";
					} else {
						xsltStaffAppsPath = "webapps" + File.separator + "MunicipalProperty" + File.separator + "xsl";
					}
					xsltFile = xsltStaffAppsPath + File.separator + type.name().toLowerCase() + File.separator + id + ".xsl";
				} else {
					xsltFile = xsltAppsPath + File.separator + xsltFile;
				}
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

}
