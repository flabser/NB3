package kz.flabs.runtimeobj.outline;

import kz.flabs.appenv.AppEnv;
import kz.flabs.exception.DocumentException;
import kz.flabs.users.UserSession;
import kz.flabs.webrule.outline.OutlineRule;
import kz.flabs.util.XMLUtil;

public class SearchOutline extends Outline implements IOutline {

    private String XMLText = "";
    private String keyWord;

    public SearchOutline(AppEnv env, OutlineRule outlineRule, String keyWord, int page, UserSession userSession)
    {
        super(env, outlineRule, "search", "search", page, "", userSession);
        this.keyWord = keyWord;
    }

    public String getOutlineAsXML(String lang) throws DocumentException
    {
        XMLText += "<currentview type=\"search\" keyword=\"" + XMLUtil.getAsTagValue(keyWord) + "\" page=\"" + page + "\">search</currentview>";
        XMLText += "<outline>";
        XMLText += getNavigationPanel(outlineRule.getOutlineRootEntry(), lang, env.vocabulary, "Provider?type=outline&subtype=" + type + "&id=" + id);
        XMLText += getSetOfFieldsAsXML(lang);
        XMLText += "</outline>";
        XMLText += getCurrentUserProperty(lang);

        return XMLText;
    }
}
