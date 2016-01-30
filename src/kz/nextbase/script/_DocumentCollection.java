package kz.nextbase.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.Execution;
import kz.flabs.runtimeobj.document.project.Project;
import kz.flabs.runtimeobj.document.task.Task;
import kz.flabs.scriptprocessor.ScriptProcessor;
import kz.flabs.util.XMLUtil;
import kz.nextbase.script.project._Project;
import kz.nextbase.script.task._Task;

public class _DocumentCollection implements _IXMLContent {

    private _Session session;
    private ArrayList<BaseDocument> col;
    private HashMap<String, Properties> parameters = new HashMap<String, Properties>();

    _DocumentCollection(_Session session) {
        this.session = session;
        this.col = new ArrayList<BaseDocument>();
    }

    public _DocumentCollection(ArrayList<BaseDocument> col, _Session session) {
        this.session = session;
        if (col == null) {
            this.col = new ArrayList<BaseDocument>();
        } else {
            this.col = col;
        }
    }

    public void add(_Document doc) {
        col.add(doc.getBaseObject());
    }

    public ArrayList<BaseDocument> getBaseCollection() {
        return col;
    }

    public void  setParameter(String name, Properties params){
        parameters.put(name,params);
    }

    public void add(Document doc) {
        col.add(doc);
    }

    public void addAll(_DocumentCollection col) {
        col.addAll(col);
    }

    public _Document getNthDocument(int index) {
        try {
            BaseDocument doc = col.get(index);
            _Document _doc = new _Document(col.get(index), session);
            switch (doc.docType) {
                case Const.DOCTYPE_MAIN:
                    return new _Document(doc, session);
                case Const.DOCTYPE_PROJECT:
                    return new _Project((Project) doc);
                case Const.DOCTYPE_TASK:
                    return new _Task((Task) doc);
                case Const.DOCTYPE_EXECUTION:
                    return new _Execution((Execution) doc);
                default:
                    return _doc;
            }
        } catch (IndexOutOfBoundsException e) {
            ScriptProcessor.logger.errorLogEntry(e);
            return null;
        }

    }

    public int getCount() {
        return col.size();
    }


    @Override
    public String toXML() throws _Exception {
        StringBuilder xmlContent = new StringBuilder(10000);

        /*HashMap<String, String> pars = new HashMap<String, String>();
        pars.put("count", "17");
        pars.put("ruleid", "demand-view");
        pars.put("currentpage", "1");
        pars.put("maxpage", "1");
        pars.put("maxpage", "1");
        pars.put("time", "16-05-2013 17:10:00");
        pars.put("userid", "wish");

        this.setParameter("query", pars);*/

        try {
            for (BaseDocument doc : col) {
                xmlContent.append("<entry isread=\"" + doc.isRead() + "\" id=\"" + doc.getDdbID() + "\" hasattach=\"" + doc.hasAttach() +  "\" hasresponse=\"" + doc.hasResponse(Const.sysGroupAsSet, Const.sysUser) + "\" doctype=\"" + doc.docType + "\" docid=\"" + doc.getDocID() + "\" favourites=\"" + doc.isFavourite() + "\"" + XMLUtil.getAsAttribute("url",doc.getURL()) + ">");
                xmlContent.append("<viewcontent>");
                int i = 0;
                for (String vt : doc.getViewTextList()) {
                    xmlContent.append("<viewtext" + (i != 0 ? i : "") + ">");
                    xmlContent.append(XMLUtil.getAsTagValue(vt));
                    xmlContent.append("</viewtext" + (i != 0 ? i : "") + ">");
                    i++;
                }
                xmlContent.append("<viewnumber>");
                xmlContent.append(doc.getViewNumber());
                xmlContent.append("</viewnumber>");
                xmlContent.append("<viewdate>");
                xmlContent.append((doc.getViewDate() != null ? _Helper.getDateAsString(doc.getViewDate()): ""));
                xmlContent.append("</viewdate>");
                xmlContent.append("</viewcontent>");
                xmlContent.append("</entry>");
            }

            if (parameters.containsKey("query") ) {
                StringBuilder queryTag = new StringBuilder(1000);
                queryTag.append("<query ");
                Properties prop = parameters.get("query");
                for (String key : prop.stringPropertyNames()) {
                    queryTag.append(key + "=\"" + prop.getProperty(key) + "\" ");
                }
                queryTag.append(">" + xmlContent + "</query>");
                xmlContent = queryTag;
            }
            //<query count="17" ruleid="demand-view" currentpage="1" maxpage="1" time="16-05-2013 17:10:00" userid="wish">
        } catch (DocumentException de) {
            DatabaseUtil.errorPrint(de);
        }
        return xmlContent.toString();
    }
}
