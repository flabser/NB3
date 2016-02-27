package kz.flabs.webrule;

import java.io.File;
import java.util.Map;

import kz.flabs.dataengine.Const;
import kz.flabs.exception.RuleException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.form.GlossaryRule;
import kz.flabs.webrule.form.ShowField;
import kz.lof.appenv.AppEnv;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Deprecated
public class StaticContentRule extends Rule implements Const {
	private String XMLBody = "";

	public StaticContentRule(AppEnv env, File ruleFile) throws RuleException {
		super(env, ruleFile);

		try {
			org.w3c.dom.Element root = doc.getDocumentElement();
			NodeList nodename = root.getElementsByTagName("field");
			SourceSupplier ss = new SourceSupplier(env);

			for (int i = 0; i < nodename.getLength(); i++) {
				ShowField sf = new ShowField(nodename.item(i), toString());
				if (sf.isOn != RunMode.OFF) {
					String caption = "";
					if (sf.hasCaptionValue) {
						caption = ss.getValueAsCaption(sf.captionValueSource, sf.captionValue).toAttrValue();
					}

					String val[] = ss.getValueAsStr(sf.valueSourceType, sf.value, sf.scriptClass, sf.macro);
					XMLBody += "<" + sf.name + caption + ">" + val[0] + "</" + sf.name + ">";

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
			e.printStackTrace();

		}
	}

	@Override
	public String getAsXML() {
		return "<content>" + XMLBody + "</content>";
	}

	@Override
	public String toString() {
		return "STATIC id=" + id + ",xslt=" + xsltFile;
	}

	@Override
	public boolean save() {
		return false;
	}

	@Override
	public String getRuleAsXML(String app) {
		return null;
	}

	@Override
	public void update(Map<String, String[]> fields) throws WebFormValueException {

	}
}
