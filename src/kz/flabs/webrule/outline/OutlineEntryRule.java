package kz.flabs.webrule.outline;

import java.util.ArrayList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import kz.flabs.appenv.AppEnv;
import kz.flabs.util.Util;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.RuleUser;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.constants.ValueSourceType;


public class OutlineEntryRule {

    public RunMode isOn = RunMode.ON;
    public boolean isValid = true;
    public boolean hasCaptionValue;
    public int type;
    public String id = "";
    public String captionId = "";
    public String URL = "";
    public String captionValue = "";
    public String hintValue = "";
    public ValueSourceType captionValueSource;
    public ValueSourceType hintValueSource;
    public ArrayList <RuleUser> granted = new ArrayList <RuleUser>();
    public ArrayList <OutlineEntryRule> entry = new ArrayList <OutlineEntryRule>(); 
    public OutlineEntryRule parentEntry;

    public OutlineEntryRule(){
        
    }

    public OutlineEntryRule(Node node)
    {
        try {
            if ( XMLUtil.getTextContent(node, "@mode", false).equals("off") ) {
                isOn = RunMode.OFF;
                isValid = false;
            }
            else {
                id = XMLUtil.getTextContent(node, "@id", false, "eid-" + Util.generateRandomAsText(), false);
                captionValue = XMLUtil.getTextContent(node, "caption", false);

                if ( !captionValue.equalsIgnoreCase("") )
                {
                    captionId = XMLUtil.getTextContent(node, "caption/@id", true, "cid-" + Util.generateRandomAsText(), false);
                    captionValueSource = ValueSourceType.valueOf(XMLUtil.getTextContent(node, "caption/@source", true, "STATIC", false));
                    hintValue = XMLUtil.getTextContent(node, "hint", false);
                    hintValueSource = ValueSourceType.valueOf(XMLUtil.getTextContent(node, "hint/@source", true, "STATIC", false));
                    hasCaptionValue = true;
                }

                try {
                    NodeList fields = XMLUtil.getNodeList(node, "granted");
                    for (int i = 0; i < fields.getLength(); i++) {
                        RuleUser df = new RuleUser(fields.item(i), toString());
                        if ( df.isOn == RunMode.ON ) {
                            granted.add(df);
                        }
                    }
                } catch (Exception e) {
                    AppEnv.logger.errorLogEntry(e);
                }

                NamedNodeMap nnm = node.getAttributes();
                if ( nnm != null ) {
                    Node URLnode = nnm.getNamedItem("url");
                    if ( URLnode != null ) {
                        URL = URLnode.getTextContent();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            AppEnv.logger.errorLogEntry(e);
            isValid = false;
        }
    }


    public OutlineEntryRule addChild(Node node)
    {
        OutlineEntryRule entry = new OutlineEntryRule(node);
        if ( entry.isValid )
        {
            entry.parentEntry = this;
            this.entry.add(entry);
            return entry;
        }

        return null;
    }
}
