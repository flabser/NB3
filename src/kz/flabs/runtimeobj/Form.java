package kz.flabs.runtimeobj;

import java.util.ArrayList;
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
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.Execution;
import kz.flabs.runtimeobj.document.glossary.Glossary;
import kz.flabs.runtimeobj.document.project.Project;
import kz.flabs.runtimeobj.document.structure.Department;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.document.structure.Organization;
import kz.flabs.runtimeobj.document.structure.UserGroup;
import kz.flabs.runtimeobj.document.task.Task;
import kz.flabs.runtimeobj.document.task.TaskType;
import kz.flabs.runtimeobj.forum.Post;
import kz.flabs.runtimeobj.forum.Topic;
import kz.flabs.runtimeobj.outline.Outline;
import kz.flabs.runtimeobj.queries.CachePool;
import kz.flabs.scriptprocessor.form.postsave.PostSaveProcessor;
import kz.flabs.scriptprocessor.form.querysave.IQuerySaveTransaction;
import kz.flabs.scriptprocessor.form.querysave.QuerySaveProcessor;
import kz.flabs.scriptprocessor.form.querysave.QuerySaveResult;
import kz.flabs.sourcesupplier.FormDataSourceSupplier;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.users.UserSession;
import kz.flabs.util.ResponseType;
import kz.flabs.util.XMLResponse;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.form.DefaultFieldRule;
import kz.flabs.webrule.form.FormRule;
import kz.flabs.webrule.form.ShowField;
import kz.flabs.webrule.outline.OutlineRule;
import kz.nextbase.script._Exception;
import kz.pchelka.env.Environment;

public class Form extends Content implements Const {
	protected FormRule rule;
	protected UserSession userSession;
	protected AppEnv env;
	protected StringBuffer outlineContent = new StringBuffer(1000);
	protected Outline outline;

	public Form(AppEnv env, FormRule rule, UserSession user) throws RuleException, QueryFormulaParserException {
		super(env, rule);
		this.env = env;
		this.rule = rule;
		this.userSession = user;
		if (rule.isOutlineEnable) {
			OutlineRule outlineRule = (OutlineRule) env.ruleProvider.getRule(OUTLINE_RULE, rule.outlineRuleName);
			outline = new Outline(env, outlineRule, userSession.currentUser);
		}
	}

	public String getDefaultFieldsAsXML(int parentDocID, int parentDocType, int page, String lang) throws RuleException, DocumentException,
	        DocumentAccessException, QueryFormulaParserException, QueryException, LocalizatorException, ClassNotFoundException, _Exception,
	        ComplexObjectException {

		StringBuffer xmlContent = new StringBuffer(1000);
		StringBuffer fieldList = new StringBuffer(100);
		String actions = "";
		SourceSupplier captionTextSupplier = new SourceSupplier(env, lang);

		if (outline != null) {
			String oc = userSession.getOutline(outline.toString(), lang);
			if (oc == null) {
				String c = outline.getAsXML(lang);
				outlineContent.append(c);
				userSession.setOutline(outline.toString(), lang, c);
			} else {
				outlineContent.append(oc);
			}
		}

		FormActions fa = new FormActions(rule, captionTextSupplier);
		actions = fa.getActions(userSession.currentUser, env);

		FormDataSourceSupplier fdss = new FormDataSourceSupplier(env, userSession, parentDocID, parentDocType);
		for (DefaultFieldRule sf : rule.defaultFieldsMap.values()) {
			String attrVal = "", caption = "";
			if (sf.hasCaptionValue) {
				caption = captionTextSupplier.getValueAsCaption(sf.captionValueSource, sf.captionValue).toAttrValue();
			}
			if (sf.isOn == RunMode.HIDE) {
				xmlContent.append("<" + sf.name + caption + " mode=\"hide\"></" + sf.name + ">");
			} else {
				ArrayList<String[]> vals = fdss.getValToShow(sf);
				if (vals.size() > 1) {
					for (String val[] : vals) {
						if (val[1] != null && !val[1].equals("")) {
							attrVal = " attrval=\"" + val[1] + "\" ";
						}
						fieldList.append("<entry " + attrVal + ">" + XMLUtil.getAsTagValue(val[0]) + "</entry>");
					}
					xmlContent.append("<" + sf.name + caption + " islist=\"true\"\">" + fieldList + "</" + sf.name + ">");
				} else {
					if (vals.size() == 1) {
						String val[] = vals.get(0);
						if (val[1] != null && !val[1].equals("")) {
							attrVal = " attrval=\"" + val[1] + "\" ";
						}
						xmlContent.append("<" + sf.name + caption + attrVal + ">" + val[0] + "</" + sf.name + ">");
					} else {
						xmlContent.append("<" + sf.name + caption + "></" + sf.name + ">");
					}
				}
			}
		}

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

		if (outline != null) {
			String oc = userSession.getOutline(outline.toString(), lang);
			if (oc == null) {
				String c = outline.getAsXML(lang);
				outlineContent.append(c);
				userSession.setOutline(outline.toString(), lang, c);
			} else {
				outlineContent.append(oc);
			}
		}

		FormActions fa = new FormActions(rule, captionTextSupplier);
		actions = fa.getActions(doc, userSession.currentUser);

		FormDataSourceSupplier fdss = new FormDataSourceSupplier(env, userSession, doc, parentDocID, parentDocType);
		fieldsXML = getDocumentAsXML(fdss, captionTextSupplier);
		String captions = getCaptions(captionTextSupplier, rule.captions);

		String viewText = doc.getViewText();
		if (viewText != null) {
			viewText = viewText.replace("\"", "'");
		} else {
			viewText = "";
		}

		String mode[] = getEditModeAttr(doc, lang);

		return "<document isvalid=\"" + doc.isValid + "\" isread=\"" + doc.isRead() + "\" id=\"" + doc.getDdbID() + "\" " + "docid=\""
		        + doc.getDocID() + "\" doctype=\"" + doc.docType + "\" hastopic=\"" + (doc.hasDiscussion ? 1 : 0) + "\" " + "parentdocid=\""
		        + doc.parentDocID + "\" parentdoctype=\"" + doc.parentDocType + "\" " + "openfrompage=\"" + page
		        + "\" status=\"existing\" viewtext=\"" + XMLUtil.getAsTagValue(viewText) + "\"" + mode[0] + ">" + outlineContent + "<actions>"
		        + actions + "</actions>" + fieldsXML + mode[1] + captions + "</document>";

	}

	public String getFormAsXML(BaseDocument doc, String lang) throws DocumentException, DocumentAccessException, RuleException,
	        QueryFormulaParserException, QueryException, ComplexObjectException {
		StringBuffer xmlText = new StringBuffer(1000);
		SourceSupplier captionTextSupplier = new SourceSupplier(env, lang);

		FormDataSourceSupplier fdss = new FormDataSourceSupplier(env, userSession, doc);
		xmlText = getDocumentAsXML(fdss, captionTextSupplier);

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
	public XMLResponse save(String key, HashMap<String, String[]> fields, int parentDocID, int parentDocType, int pageSize, String currentLang)
	        throws DocumentAccessException, DocumentException, RuleException, QueryFormulaParserException, ClassNotFoundException,
	        ComplexObjectException {
		IDatabase db = env.getDataBase();
		XMLResponse result = new XMLResponse(ResponseType.SAVE_FORM);
		String redirectView = "";
		BaseDocument doc = null;
		QuerySaveProcessor qsp = null;

		try {
			int docID = Integer.parseInt(key);
			switch (rule.docType) {
			case DOCTYPE_MAIN:
				doc = db.getMainDocumentByID(docID, userSession.currentUser.getAllUserGroups(), userSession.currentUser.getUserID());
				if (parentDocID != 0 && parentDocType != DOCTYPE_UNKNOWN) {
					if (db.hasDocumentByComplexID(parentDocID, parentDocType)) {
						if (doc.isNewDoc()) {
							doc.parentDocID = parentDocID;
							doc.parentDocType = parentDocType;
						}
					} else {
						result.resultFlag = false;
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype=" + parentDocType);
						return result;
					}
				}
				break;
			case DOCTYPE_TASK:
				if (parentDocID == 0 || parentDocType == DOCTYPE_UNKNOWN) {
					result.resultFlag = false;

					doc = db.getTasks().getTaskByID(docID, userSession.currentUser.getAllUserGroups(), userSession.currentUser.getUserID());
					doc.parentDocID = parentDocID;
					doc.parentDocType = parentDocType;
					((Task) doc).setResolType(TaskType.TASK);
					// return result;
				} else {
					if (db.hasDocumentByComplexID(parentDocID, parentDocType)) {
						doc = db.getTasks().getTaskByID(docID, userSession.currentUser.getAllUserGroups(), userSession.currentUser.getUserID());
						if (doc.isNewDoc()) {
							doc.parentDocID = parentDocID;
							doc.parentDocType = parentDocType;
						}
						if (parentDocType == Const.DOCTYPE_MAIN) {
							((Task) doc).setResolType(TaskType.RESOLUTION);
						} else {
							((Task) doc).setResolType(TaskType.CONSIGN);
						}
					} else {
						result.resultFlag = false;
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype=" + parentDocType);
						return result;
					}
				}
				break;
			case DOCTYPE_EXECUTION:
				if (parentDocID == 0 || parentDocType == DOCTYPE_UNKNOWN) {
					result.resultFlag = false;
					result.addMessage("Unable to define properties of parent document");
					return result;
				} else {
					if (db.hasDocumentByComplexID(parentDocID, parentDocType)) {
						doc = db.getExecutions().getExecutionByID(docID, userSession.currentUser.getAllUserGroups(),
						        userSession.currentUser.getUserID());
						if (doc.isNewDoc()) {
							doc.parentDocID = parentDocID;
							doc.parentDocType = parentDocType;
						}
					} else {
						result.resultFlag = false;
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype=" + parentDocType);
						return result;
					}
				}
				break;
			case DOCTYPE_PROJECT:
				doc = db.getProjects().getProjectByID(docID, userSession.currentUser.getAllUserGroups(), userSession.currentUser.getUserID());
				break;
			case DOCTYPE_GLOSSARY:
				doc = db.getGlossaries().getGlossaryDocumentByID(docID, true, userSession.currentUser.getAllUserGroups(),
				        userSession.currentUser.getUserID());
				if (parentDocID != 0 && parentDocType != DOCTYPE_UNKNOWN) {
					if (db.hasDocumentByComplexID(parentDocID, parentDocType)) {
						if (doc.isNewDoc()) {
							doc.parentDocID = parentDocID;
							doc.parentDocType = parentDocType;
						}
					} else {
						result.resultFlag = false;
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype=" + parentDocType);
						return result;
					}
				}
				break;
			case DOCTYPE_ORGANIZATION:
				doc = db.getStructure().getOrganization(docID, userSession.currentUser);
				break;
			case DOCTYPE_DEPARTMENT:
				doc = db.getStructure().getDepartment(docID, userSession.currentUser);
				break;
			case DOCTYPE_EMPLOYER:
				doc = db.getStructure().getEmployer(docID, userSession.currentUser);
				break;
			case DOCTYPE_GROUP:
				doc = db.getStructure().getGroup(docID, userSession.currentUser.getAllUserGroups(), userSession.currentUser.getUserID());
				break;
			case DOCTYPE_TOPIC:
				doc = db.getForum().getTopicByID(docID, userSession.currentUser.getAllUserGroups(), userSession.currentUser.getUserID());
				if (parentDocID != 0 && parentDocType != DOCTYPE_UNKNOWN) {
					if (db.hasDocumentByComplexID(parentDocID, parentDocType)) {
						if (doc.isNewDoc()) {
							doc.parentDocID = parentDocID;
							doc.parentDocType = parentDocType;
						}
					} else {
						result.resultFlag = false;
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype=" + parentDocType);
						return result;
					}
				}
				break;
			case DOCTYPE_POST:
				doc = db.getForum().getPostByID(docID, userSession.currentUser.getAllUserGroups(), userSession.currentUser.getUserID());
				if (parentDocID != 0 && parentDocType != DOCTYPE_UNKNOWN) {
					if (db.hasDocumentByComplexID(parentDocID, parentDocType)) {
						if (doc.isNewDoc()) {
							doc.parentDocID = parentDocID;
							doc.parentDocType = parentDocType;
						}
					} else {
						result.resultFlag = false;
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype=" + parentDocType);
						return result;
					}
				}
				break;
			}
		} catch (NumberFormatException nfe) {
			switch (rule.docType) {
			case DOCTYPE_MAIN:
				doc = new Document(env, userSession.currentUser);
				if (parentDocID != 0 && parentDocType != DOCTYPE_UNKNOWN) {
					if (db.hasDocumentByComplexID(parentDocID, parentDocType)) {
						doc.parentDocID = parentDocID;
						doc.parentDocType = parentDocType;
					} else {
						result.resultFlag = false;
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype=" + parentDocType);
						return result;
					}
				}
				break;
			case DOCTYPE_TASK:
				doc = new Task(env, userSession.currentUser);

				if (parentDocType == Const.DOCTYPE_MAIN) {
					((Task) doc).setResolType(TaskType.RESOLUTION);
				} else if (parentDocType == Const.DOCTYPE_UNKNOWN) {
					((Task) doc).setResolType(TaskType.TASK);
				} else {
					((Task) doc).setResolType(TaskType.CONSIGN);
				}
				if (db.hasDocumentByComplexID(parentDocID, parentDocType) || ((Task) doc).getResolType() == TaskType.TASK) {
					doc.parentDocID = parentDocID;
					doc.parentDocType = parentDocType;
				} else {
					result.resultFlag = false;
					result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype=" + parentDocType);
					return result;
				}
				break;
			case DOCTYPE_EXECUTION:
				if (parentDocID == 0 || parentDocType == DOCTYPE_UNKNOWN) {
					result.resultFlag = false;
					result.addMessage("Unable to define properties of parent document");
					return result;
				} else {
					if (db.hasDocumentByComplexID(parentDocID, parentDocType)) {
						doc = new Execution(env, userSession.currentUser);
						doc.parentDocID = parentDocID;
						doc.parentDocType = parentDocType;
					} else {
						result.resultFlag = false;
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype=" + parentDocType);
						return result;
					}
				}
				break;
			case DOCTYPE_PROJECT:
				doc = new Project(env, userSession.currentUser);
				break;
			case DOCTYPE_GLOSSARY:
				doc = new Glossary(env, userSession.currentUser);
				if (parentDocID != 0 && parentDocType != DOCTYPE_UNKNOWN) {
					if (db.hasDocumentByComplexID(parentDocID, parentDocType)) {
						doc.parentDocID = parentDocID;
						doc.parentDocType = parentDocType;
					} else {
						result.resultFlag = false;
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype=" + parentDocType);
						return result;
					}
				}
				break;
			case DOCTYPE_ORGANIZATION:
				doc = new Organization(env.getDataBase().getStructure());
				doc.setAuthor(userSession.currentUser.getUserID());
				break;
			case DOCTYPE_DEPARTMENT:
				if (parentDocID == 0 || parentDocType == DOCTYPE_UNKNOWN) {
					result.resultFlag = false;
					result.addMessage("Unable to define properties of parent document");
					return result;
				} else {
					if (db.hasDocumentByComplexID(parentDocID, parentDocType)) {
						doc = new Department(env.getDataBase().getStructure());
						doc.setAuthor(userSession.currentUser.getUserID());
						doc.parentDocID = parentDocID;
						doc.parentDocType = parentDocType;
					} else {
						result.resultFlag = false;
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype=" + parentDocType);
						return result;
					}
				}
				break;
			case DOCTYPE_EMPLOYER:
				if (parentDocID == 0 || parentDocType == DOCTYPE_UNKNOWN) {
					result.resultFlag = false;
					result.addMessage("Unable to define properties of parent document");
					return result;
				} else {
					if (db.hasDocumentByComplexID(parentDocID, parentDocType)) {
						doc = new Employer(env.getDataBase().getStructure());
						doc.setAuthor(userSession.currentUser.getUserID());
						doc.parentDocID = parentDocID;
						doc.parentDocType = parentDocType;
					} else {
						result.resultFlag = false;
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype=" + parentDocType);
						return result;
					}
				}
				break;
			case DOCTYPE_GROUP:
				doc = new UserGroup(env.getDataBase().getStructure());
				doc.setAuthor(userSession.currentUser.getUserID());
				break;
			case DOCTYPE_TOPIC:
				doc = new Topic(env, userSession.currentUser);
				if (parentDocID != 0 && parentDocType != DOCTYPE_UNKNOWN) {
					if (db.hasDocumentByComplexID(parentDocID, parentDocType)) {
						doc.parentDocID = parentDocID;
						doc.parentDocType = parentDocType;
					} else {
						result.resultFlag = false;
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype=" + parentDocType);
						return result;
					}
				}
				doc.setAuthor(userSession.currentUser.getUserID());
				break;
			case DOCTYPE_POST:
				doc = new Post(env, userSession.currentUser);
				if (parentDocID != 0 && parentDocType != DOCTYPE_UNKNOWN) {
					if (db.hasDocumentByComplexID(parentDocID, parentDocType)) {
						doc.parentDocID = parentDocID;
						doc.parentDocType = parentDocType;
					} else {
						result.resultFlag = false;
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype=" + parentDocType);
						return result;
					}
				}
				doc.setAuthor(userSession.currentUser.getUserID());
				break;
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
			qsp = new QuerySaveProcessor(env, doc, userSession.currentUser, currentLang);
			// qsp.setScript(rule.querySaveScript);
			QuerySaveResult qsResult = qsp.doScript(rule.querySaveClass);
			result.resultFlag = qsResult.continueSave;
			result.addMessage(qsResult.msg);
			redirectView = qsResult.redirectView;
		} else {
			result.resultFlag = true;
			qsp = new QuerySaveProcessor();
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

			if (result.resultFlag) {

				if (rule.docType == DOCTYPE_GLOSSARY) {
					CachePool.flush();
				}

				for (IQuerySaveTransaction toPostObects : qsp.transactionToPost) {
					toPostObects.post();
				}

				if (rule.postSaveEnable) {
					PostSaveProcessor psp = new PostSaveProcessor(doc, userSession.currentUser);
					psp.setClass(rule.postSaveClass);
					psp.start();
				}
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
				result.setRedirect("Provider?type=view&id=" + redirectView + "&page=" + pageNum);
				if (!redirectView.equals("")) {
					userSession.setCurrentPage(redirectView, pageNum);
				}
			} catch (Exception nfe) {
				if (redirectView != null && !redirectView.equals("")) {
					String redurectURL = "Provider?type=view&amp;id=" + redirectView + "&amp;page=0";
					result.setRedirect(redurectURL);
					userSession.setCurrentPage(redirectView, 0);

				}
			}
			userSession.addExpandedThread(new DocID(parentDocID, parentDocType));
			userSession.setFlashViewEntry(new DocID(docID, doc.docType));
		}

		return result;
	}

	private StringBuffer getDocumentAsXML(FormDataSourceSupplier fdss, SourceSupplier captionTextSupplier) throws DocumentException,
	        DocumentAccessException, RuleException, QueryFormulaParserException, QueryException, ComplexObjectException {
		String fieldList = "";
		StringBuffer xmlText = new StringBuffer(1000);
		xmlText.append("<fields>");

		for (ShowField sf : rule.showFieldsMap.values()) {
			String attrVal = "", caption = "";
			fieldList = "";

			if (sf.hasCaptionValue) {
				caption = captionTextSupplier.getValueAsCaption(sf.captionValueSource, sf.captionValue).toAttrValue();
			}

			if (sf.isOn == RunMode.HIDE) {
				xmlText.append("<" + sf.name + caption + " mode=\"hide\"></" + sf.name + ">");
			} else {
				ArrayList<String[]> vals = fdss.getValueAsList(sf);

				if (vals.size() > 1) {
					for (String val[] : vals) {
						if (val[1] != null) {
							attrVal = " attrval=\"" + val[1].replace("&", "&amp;") + "\" ";
						} else {
							attrVal = "";
						}
						fieldList += "<entry " + attrVal + ">" + XMLUtil.getAsTagValue(val[0]) + "</entry>";
					}
					xmlText.append("<" + sf.name + " islist=\"true\"" + caption + ">" + fieldList + "</" + sf.name + ">");
				} else {
					if (vals.size() == 1) {
						String val[] = vals.get(0);
						if (val[1] != null) {
							attrVal = " attrval=\"" + val[1].replace("&", "&amp;") + "\" ";
						}

						xmlText.append("<" + sf.name + caption + attrVal + ">" + val[0] + "</" + sf.name + ">");
					} else {
						xmlText.append("<" + sf.name + caption + "></" + sf.name + ">");
					}
				}
			}
		}
		return xmlText.append("</fields>");
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
