package kz.flabs.webrule.view;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.exception.RuleException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.FieldRule;
import kz.flabs.webrule.Rule;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.form.FormActionRule;
import kz.flabs.webrule.form.GlossaryRule;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Deprecated
public class ViewRule extends Rule implements Const {
	public boolean isValid;
	public String outlineRuleName;
	public boolean isOutlineEnable;
	public String queryRuleName;
	public boolean isQueryEnable;
	public ArrayList<String> countRuleName = new ArrayList<String>();
	public boolean isCountEnable;
	public String staticXMLContent = "";
	public HashMap<String, ViewColumnRule> cols = new HashMap<String, ViewColumnRule>();

	public ViewRule(AppEnv env, File ruleFile) throws RuleException {
		super(env, ruleFile);

		try {
			queryRuleName = XMLUtil.getTextContent(doc, "/rule/query");
			if (queryRuleName.trim().equals("")) {
				AppEnv.logger.warningLogEntry("Query not defined in View rule");
				isValid = false;
			} else {
				isQueryEnable = true;
			}

			NodeList counts = XMLUtil.getNodeList(doc, "/rule/count");
			for (int i = 0; i < counts.getLength(); i++) {
				countRuleName.add(XMLUtil.getTextContent(counts.item(i), ".", false));
				isCountEnable = true;
			}

			outlineRuleName = XMLUtil.getTextContent(doc, "/rule/outline");
			if (!outlineRuleName.trim().equals("")) {
				isOutlineEnable = true;
			}

			try {
				org.w3c.dom.Element root = doc.getDocumentElement();
				NodeList nodename = root.getElementsByTagName("field");
				for (int i = 0; i < nodename.getLength(); i++) {
					FieldRule f = new FieldRule(nodename.item(i));
					if (f.isOn) {
						String attr = "";
						if ((!f.attrValue.equals("")) && (!f.attrName.equals(""))) {
							attr = f.attrName + "=\"" + f.attrValue + "\"";
						}
						staticXMLContent += "<" + f.name + " " + attr + " >" + f.value + "</" + f.name + ">";
					}
				}
			} catch (Exception e) {
				AppEnv.logger.errorLogEntry(e);
				// e.printStackTrace();
			}

			NodeList columns = XMLUtil.getNodeList(doc, "/rule/column");
			for (int i = 0; i < columns.getLength(); i++) {
				ViewColumnRule vcr = new ViewColumnRule(columns.item(i));
				cols.put(vcr.name, vcr);
			}

			NodeList defaultActions = XMLUtil.getNodeList(doc, "/rule/action");
			for (int i = 0; i < defaultActions.getLength(); i++) {
				FormActionRule df = new FormActionRule(defaultActions.item(i));
				if (df.isOn == RunMode.ON) {
					defaultActionsMap.put(df.type, df);
				}
			}

			NodeList glossaries = XMLUtil.getNodeList(doc, "/rule/glossary");
			for (int i = 0; i < glossaries.getLength(); i++) {
				Node node = glossaries.item(i);
				GlossaryRule g = new GlossaryRule(node);
				if (g.isOn && g.isValid) {
					addGlossary(g.name, g);
				}
			}

		} catch (Exception e) {
			AppEnv.logger.errorLogEntry(e);
		}

	}

	@Override
	public String toString() {
		return "id=" + id + ", xslt=" + xsltFile + ", query=" + queryRuleName;
	}

	public int getCacheMethod() {
		return 0;
	}

	@Override
	public boolean save() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getRuleAsXML(String app) {
		String xmlText = "<rule id=\"" + id + "\" isvalid=\"" + isValid + "\" app=\"" + app + "\" ison=\"" + isOn + "\">" + "<description>"
		        + description + "</description>" + "<doctype>" + docType + "</doctype>" + "<hits>" + hits + "</hits>" + "<rununderuser>"
		        + runUnderUser.toXML() + "</rununderuser>" + "<query>" + queryRuleName + "</query>" + "<fields>" + staticXMLContent
		        + "</fields></rule>";
		return xmlText;

	}

	@Override
	public void update(Map<String, String[]> fields) throws WebFormValueException {
		// TODO Auto-generated method stub

	}

}
