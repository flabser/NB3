package kz.flabs.webrule.outline;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.exception.RuleException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.Rule;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.form.DefaultFieldRule;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OutlineRule extends Rule implements Const {

    public String id = "";
    public String defaultURL;
    public HashMap <String, DefaultFieldRule> defaultFieldsMap = new HashMap <String, DefaultFieldRule> ();
    protected OutlineEntryRule rootEntry = new OutlineEntryRule();

    public OutlineRule(AppEnv env, File ruleFile) throws RuleException {
        super(env, ruleFile);

        id = XMLUtil.getTextContent(doc, "/rule/@id");
        AppEnv.logger.infoLogEntry("Loading: " + this.getClass().getSimpleName() + ", id=" + id);

        NodeList fields = XMLUtil.getNodeList(doc, "/rule/outline/default/field");
        for (int i = 0; i < fields.getLength(); i++)
        {
            DefaultFieldRule df = new DefaultFieldRule(fields.item(i), toString());

            if ( df.isOn != RunMode.OFF && df.isValid )
            {
                defaultFieldsMap.put(df.name, df);
            }
        }

        NodeList entriesNode = XMLUtil.getNodeList(doc, "/rule/outline/entry");
        addEntrys(entriesNode, rootEntry);
    }

   
    public static void addEntrys(NodeList nodelist, OutlineEntryRule outlineParentEntry) {
        for (int i = 0; i < nodelist.getLength(); i++) {
            Node node = nodelist.item(i);
            if ( node.getNodeName().equalsIgnoreCase("entry")) {
                OutlineEntryRule ent = outlineParentEntry.addChild(node);
                NodeList childNodes = node.getChildNodes();
                if (ent != null && childNodes != null ) {
                    addEntrys(childNodes, ent);
                }
            }
        }
    }

    public OutlineEntryRule getOutlineRootEntry() {
        return rootEntry;
    }

    public void update(Map <String, String[]> fields)
            throws WebFormValueException {
        
    }

    public boolean save() {
        return false;
    }
    
	public String toString(){
		return "OUTLINE id=" + id + ", ison=" + isOn;
	}
}
