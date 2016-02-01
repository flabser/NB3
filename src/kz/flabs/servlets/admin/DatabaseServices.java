package kz.flabs.servlets.admin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseFactory;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.util.Util;
import kz.flabs.util.XMLUtil;
import kz.nextbase.script._Exception;
import kz.nextbase.script._ViewEntry;
import kz.nextbase.script._ViewEntryCollection;

public class DatabaseServices implements Const {

	protected IDatabase db;
	private static final SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy kk:mm:ss");
	private int colCount;
	private int pageSize = 50;

	DatabaseServices(String dbID) {
		db = DatabaseFactory.getDatabaseByName(dbID);
	}

	public _ViewEntryCollection getAllDocuments(AppEnv env, int page, int pageSize) {
		return null;
	}

	public String wrapDocumentsList(_ViewEntryCollection col) throws DocumentException, DocumentAccessException, ComplexObjectException, _Exception {
		String xmlFragment = "";

		for (_ViewEntry entry : col.getEntries()) {

			/*
			 * xmlFragment += "<entry dbid=\"" + db.getDbID() + "\" docid=\"" +
			 * doc.getID() + "\" " + " parentdocid=\"" +
			 * doc.getParentDocumentID() + "\" " + "author=\"" +
			 * doc.getAuthorID() + "\" regdate=\"" +
			 * Util.convertDataTimeToString(doc.getRegDate()) + "\" >" +
			 * "<form>" + doc.getDocumentForm() + "</form>" + "<viewtext>" +
			 * XMLUtil.getAsTagValue(doc.getViewText()) + "</viewtext></entry>";
			 */
		}

		return xmlFragment;
	}

	@Deprecated
	public String getAllDocsAsXML(String docType, int pageNum, String app) throws DocumentException, DocumentAccessException, ComplexObjectException {
		ArrayList<BaseDocument> col = new ArrayList<BaseDocument>();
		String fields[] = { "form", "viewtext" };
		if (docType.equals("maindoc")) {

		}
		return wrapDocumentsList(col, pageNum, app);
	}

	@Deprecated
	private String wrapDocumentsList(ArrayList<BaseDocument> col, int pageNum, String app) throws DocumentException {
		StringBuffer xmlFragment = new StringBuffer(1000);

		for (BaseDocument doc : col) {

			xmlFragment.append("<entry dbid=\"" + db.getDbID() + "\" docid=\"" + doc.getDdbID() + "\" " + " parentdocid=\"" + doc.parentDocID
			        + "\" parentdoctype=\"" + doc.parentDocType + "\" " + "author=\"" + doc.getAuthorID() + "\" regdate=\""
			        + Util.convertDataTimeToString(doc.getRegDate()) + "\" " + " lastupdated=\"" + Util.convertDataTimeToString(doc.getLastUpdate())
			        + "\">");
			xmlFragment.append("<form>" + doc.form + "</form>");
			xmlFragment.append("<viewtext>" + XMLUtil.getAsTagValue(doc.getViewText()) + "</viewtext>");
			xmlFragment.append("</entry>");

		}

		// return "<query dbid=\"" + db.getDbID() +"\" count=\"" + colCount +
		// "\"  direction=\"forward\" currentpage=\"" + pageNum + "\" " +
		// " app=\"" + app + "\" " +
		// "maxpage=\"" + RuntimeObjUtil.countMaxPage(colCount, pageSize) +
		// "\""+
		// " time=\""+ dateformat.format(new Date()) + "\" >" + xmlFragment +
		// "</query>";
		return xmlFragment.toString();

	}

}
