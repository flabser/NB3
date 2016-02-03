package kz.flabs.runtimeobj;

import java.util.HashMap;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.localization.LocalizatorException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.users.UserSession;
import kz.flabs.util.PageResponse;
import kz.flabs.util.ResponseType;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.form.FormRule;
import kz.nextbase.script._Exception;
import kz.pchelka.env.Environment;

public class Form extends Content implements Const {
	protected FormRule rule;
	protected UserSession userSession;
	protected AppEnv env;
	protected StringBuffer outlineContent = new StringBuffer(1000);

	public Form(AppEnv env, FormRule rule, UserSession user) throws RuleException, QueryFormulaParserException {
		super(env, rule);
		this.env = env;
		this.rule = rule;
		this.userSession = user;

	}

	public String getDefaultFieldsAsXML(int parentDocID, int parentDocType, int page, String lang) throws RuleException, DocumentException,
	        DocumentAccessException, QueryFormulaParserException, QueryException, LocalizatorException, ClassNotFoundException, _Exception,
	        ComplexObjectException {

		StringBuffer xmlContent = new StringBuffer(1000);
		StringBuffer fieldList = new StringBuffer(100);
		String actions = "";
		SourceSupplier captionTextSupplier = new SourceSupplier(env, lang);

		FormActions fa = new FormActions(rule, captionTextSupplier);
		actions = fa.getActions(userSession.currentUser, env);

		String captions = getCaptions(captionTextSupplier, rule.captions);
		String mode[] = getEditModeAttr(lang);

		return "<document isvalid=\"true\"  " + "parentdocid=\"" + parentDocID + "\" parentdoctype=\"" + parentDocType + "\" doctype=\""
		        + rule.docType + "\" " + "openfrompage=\"" + page + "\" status=\"new\" viewtext=\"\" " + mode[0] + ">" + outlineContent + "<actions>"
		        + actions + "</actions><fields>" + xmlContent + "</fields>" + mode[1] + captions + "</document>";

	}

	public String getFormAsXML(BaseDocument doc, int page, int parentDocID, int parentDocType, String lang) throws DocumentException,
	        DocumentAccessException, RuleException, QueryFormulaParserException, QueryException, LocalizatorException, ClassNotFoundException,
	        _Exception, ComplexObjectException {
		StringBuffer fieldsXML = new StringBuffer(1000);
		String actions = "";
		SourceSupplier captionTextSupplier = new SourceSupplier(env, lang);

		FormActions fa = new FormActions(rule, captionTextSupplier);
		actions = fa.getActions(doc, userSession.currentUser);

		String viewText = doc.getViewText();
		if (viewText != null) {
			viewText = viewText.replace("\"", "'");
		} else {
			viewText = "";
		}

		String mode[] = getEditModeAttr(doc, lang);

		return "<document isvalid=\"" + doc.isValid + "\" id=\"" + doc.getDdbID() + "\" " + "docid=\"" + doc.getDocID() + "\" doctype=\""
		        + doc.docType + "\" hastopic=\"" + (doc.hasDiscussion ? 1 : 0) + "\" " + "parentdocid=\"" + doc.parentDocID + "\" parentdoctype=\""
		        + doc.parentDocType + "\" " + "openfrompage=\"" + page + "\" status=\"existing\" viewtext=\"" + XMLUtil.getAsTagValue(viewText)
		        + "\"" + mode[0] + ">" + outlineContent + "<actions>" + actions + "</actions>" + fieldsXML + mode[1] + "</document>";

	}

	public String getFormAsXML(BaseDocument doc, String lang) throws DocumentException, DocumentAccessException, RuleException,
	        QueryFormulaParserException, QueryException, ComplexObjectException {
		StringBuffer xmlText = new StringBuffer(1000);
		SourceSupplier captionTextSupplier = new SourceSupplier(env, lang);

		String viewText = doc.getViewText();
		if (viewText != null) {
			viewText = viewText.replace("\"", "'");
		} else {
			viewText = "";
		}

		return "<document isvalid=\"" + doc.isValid + "\" id=\"" + doc.getDdbID() + "\" " + "hastopic=\"" + (doc.hasDiscussion ? 1 : 0) + "\" "
		        + "viewtext=\"" + XMLUtil.getAsTagValue(viewText) + "\" >" + xmlText + "</document>";
	}

	/**
	 * Yous hould use QuerySave groovy script to do it if possible
	 * 
	 * @throws ComplexObjectException
	 **/
	@Deprecated
	public PageResponse save(String key, HashMap<String, String[]> fields, int parentDocID, int parentDocType, int pageSize, String currentLang)
	        throws DocumentAccessException, DocumentException, RuleException, QueryFormulaParserException, ClassNotFoundException,
	        ComplexObjectException {
		IDatabase db = env.getDataBase();
		PageResponse result = new PageResponse(ResponseType.SAVE_FORM);
		String redirectView = "";
		BaseDocument doc = null;

		try {
			int docID = Integer.parseInt(key);
			switch (rule.docType) {
			case DOCTYPE_MAIN:

				break;

			}
		} catch (NumberFormatException nfe) {
			switch (rule.docType) {

			}
		}

		try {
			doc.fillFieldsToSave(rule.saveFieldsMap, fields);
			doc.setAccessRelatedFields(doc, rule.saveFieldsMapAccess, fields);
		} catch (WebFormValueException wfve) {
			result.resultFlag = false;
			result.addMessage(wfve.getMessage());
			return result;
		}

		if (rule.querySaveEnable) {

		} else {
			result.resultFlag = true;

		}

		int docID = -1;
		if (result.resultFlag) {
			try {
				docID = doc.save(userSession.currentUser);

				if (docID > -1) {
					result.setResponseStatus(true);
					// result.addMessage(Integer.toString(docID));
					result.addMessage("");
				} else {
					result.setResponseStatus(false);
					result.setMessage("Data engine error");
					AppEnv.logger.errorLogEntry("DataEngine error");
				}
			} catch (DocumentException e) {
				result.setResponseStatus(false);
				result.setResponseType(ResponseType.EXCEPTION);
				result.addMessage(e.getMessage());
				result.addMessage(e.exceptionType.toString());
				AppEnv.logger.errorLogEntry(e.getMessage());
			} catch (DocumentAccessException e) {
				result.setResponseStatus(false);
				result.setResponseType(ResponseType.EXCEPTION);
				result.addMessage(e.getMessage());
				result.addMessage(e.exceptionType.toString());
				AppEnv.logger.errorLogEntry(e.getMessage());
			} catch (Exception e) {
				result.setResponseStatus(false);
				result.setMessage("Runtime objects error");
				result.addMessage(e.getMessage());
				AppEnv.logger.errorLogEntry(e.getMessage());
				e.printStackTrace();
				AppEnv.logger.errorLogEntry("Runtime objects error");
			}

			if (doc.toDeleteAfterSave != null) {
				Environment.fileToDelete.add(doc.toDeleteAfterSave);
			}
		} else {
			// result.addMessage("Document has not been saved");
		}

		if (result.resultFlag) {
			String page[] = fields.get("page");
			try {
				int pageNum = Integer.parseInt(page[0]);
				int pagePosition = redirectView.indexOf("&amp;page");
				if (pagePosition > -1) {
					redirectView = redirectView.substring(0, pagePosition);
				}

				if (!redirectView.equals("")) {
					userSession.setCurrentPage(redirectView, pageNum);
				}
			} catch (Exception nfe) {
				if (redirectView != null && !redirectView.equals("")) {
					String redurectURL = "Provider?type=view&amp;id=" + redirectView + "&amp;page=0";

					userSession.setCurrentPage(redirectView, 0);

				}
			}

		}

		return result;
	}

	protected String[] getEditModeAttr(String lang) throws RuleException, DocumentException, DocumentAccessException, QueryFormulaParserException,
	        QueryException, LocalizatorException {
		String[] result = new String[2];
		/*
		 * if (env.globalSetting.edsSettings.isOn == RunMode.ON) { result[0] =
		 * " editmode=\"edit\" canbesign=\"1\" sign=\"0\""; } else { result[0] =
		 * " editmode=\"edit\" canbesign=\"0\" sign=\"-1\""; }
		 */
		result[1] = getSpravFieldSet(userSession.currentUser, lang);
		return result;
	}

	protected String[] getEditModeAttr(BaseDocument doc, String lang) throws RuleException, DocumentException, DocumentAccessException,
	        QueryFormulaParserException, QueryException, LocalizatorException {
		String[] result = new String[2];

		switch (doc.editMode) {
		case EDITMODE_READONLY:
			result[0] = " editmode=\"readonly\" canbesign=\"0\" ";
			result[1] = "";
			return result;
		case EDITMODE_EDIT:
			result[0] = " editmode=\"edit\" canbesign=\"0\" ";
			result[1] = getSpravFieldSet(userSession.currentUser, lang);
			return result;
		default:
			result[0] = " editmode=\"noaccess\" ";
			result[1] = "";
			return result;
		}

	}

	public class ShowValue {
		String stringVal;
		boolean isList;

		public ShowValue(String val, boolean isList) {
			this.stringVal = val;
			this.isList = isList;
		}

		@Override
		public String toString() {
			return "stringVal=" + stringVal + ", isList=" + isList;
		}
	}
}
