package kz.flabs.runtimeobj;

import java.util.ArrayList;
import java.util.HashMap;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.localization.LocalizatorException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.glossary.Glossary;
import kz.flabs.runtimeobj.document.structure.Department;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.document.structure.Organization;
import kz.flabs.runtimeobj.document.structure.UserGroup;
import kz.flabs.runtimeobj.forum.Post;
import kz.flabs.runtimeobj.forum.Topic;
import kz.flabs.scriptprocessor.form.postsave.PostSaveProcessor;
import kz.flabs.scriptprocessor.form.queryopen.PublishResult;
import kz.flabs.scriptprocessor.form.queryopen.QueryOpenProcessor;
import kz.flabs.scriptprocessor.form.querysave.IQuerySaveTransaction;
import kz.flabs.scriptprocessor.form.querysave.QuerySaveProcessor;
import kz.flabs.scriptprocessor.form.querysave.QuerySaveResult;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.users.UserSession;
import kz.flabs.util.ResponseType;
import kz.flabs.util.XMLResponse;
import kz.flabs.webrule.Caption;
import kz.flabs.webrule.form.FormRule;
import kz.flabs.webrule.page.ElementRule;
import kz.flabs.webrule.page.PageRule;
import kz.nextbase.script._Exception;
import kz.nextbase.script._IXMLContent;
import kz.pchelka.env.Environment;

public class DocumentForm extends Form {
	private HashMap<String, String[]> fields;

	public DocumentForm(HashMap<String, String[]> fields, AppEnv env, FormRule rule, UserSession user)
			throws RuleException, QueryFormulaParserException {
		super(env, rule, user);
		this.fields = fields;
	}

	@Override
	public String getDefaultFieldsAsXML(int parentDocID, int parentDocType, int page, String lang)
			throws RuleException, DocumentException, DocumentAccessException, QueryFormulaParserException,
			QueryException, LocalizatorException, ClassNotFoundException, _Exception, ComplexObjectException {
		StringBuffer elementsContent = new StringBuffer(1000);
		StringBuffer fieldsContent = new StringBuffer(1000);
		@Deprecated
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

		PublishResult qoResult = null;
		if (rule.qoEnable) {
			QueryOpenProcessor qop = new QueryOpenProcessor(env, userSession.currentUser, userSession.lang, fields);
			qoResult = qop.processScript(rule.qoClassName.getClassName());
		}

		for (_IXMLContent c : qoResult.toPublishElement) {
			elementsContent.append(c.toXML());
		}

		for (_IXMLContent sf : qoResult.toPublish) {
			fieldsContent.append(sf.toXML());
		}

		ArrayList<Caption> c = (ArrayList<Caption>) rule.getCaptions().clone();
		for (ElementRule e : rule.elements) {
			PageRule rule = (PageRule) env.ruleProvider.getRule(PAGE_RULE, e.value);
			// IncludedPage inclPage = new IncludedPage(env, userSession, rule);
			// output.append(inclPage.process(fields));
			c.addAll(rule.getCaptions());
		}
		String captions = getCaptions(captionTextSupplier, c);
		String mode[] = getEditModeAttr(lang);

		return "<document isvalid=\"true\"  " + "parentdocid=\"" + parentDocID + "\" parentdoctype=\"" + parentDocType
				+ "\" doctype=\"" + rule.docType + "\" " + "openfrompage=\"" + page + "\" status=\"new\" " + mode[0]
				+ ">" + outlineContent + "<actions>" + actions + "</actions>" + elementsContent + "<fields>"
				+ fieldsContent + "</fields>" + mode[1] + captions + "</document>";
	}

	@Override
	public String getFormAsXML(BaseDocument doc, int page, int parentDocID, int parentDocType, String lang)
			throws DocumentException, DocumentAccessException, RuleException, QueryFormulaParserException,
			ClassNotFoundException, QueryException, LocalizatorException, _Exception, ComplexObjectException {
		StringBuffer elementsContent = new StringBuffer(1000);
		StringBuffer fieldsContent = new StringBuffer(1000);
		@Deprecated
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

		PublishResult qoResult = null;
		if (rule.qoEnable) {
			QueryOpenProcessor qop = new QueryOpenProcessor(env, doc, userSession, fields);
			qoResult = qop.processScript(rule.qoClassName.getClassName());
		}

		// long start_time = System.currentTimeMillis(); // for speed debuging

		for (_IXMLContent c : qoResult.toPublishElement) {
			elementsContent.append(c.toXML());
		}

		for (_IXMLContent sf : qoResult.toPublish) {
			fieldsContent.append(sf.toXML());
		}

		ArrayList<Caption> c = (ArrayList<Caption>) rule.getCaptions().clone();
		for (ElementRule e : rule.elements) {
			PageRule rule = (PageRule) env.ruleProvider.getRule(PAGE_RULE, e.value);
			// IncludedPage inclPage = new IncludedPage(env, userSession, rule);
			// output.append(inclPage.process(fields));
			c.addAll(rule.getCaptions());
		}
		String captions = getCaptions(captionTextSupplier, c);

		// String captions = getCaptions(captionTextSupplier, rule.captions);
		String mode[] = getEditModeAttr(doc, lang);

		return "<document isvalid=\"true\"  " + "parentdocid=\"" + parentDocID + "\" parentdoctype=\"" + parentDocType
				+ "\" id=\"" + doc.getDdbID() + "\" " + "docid=\"" + doc.getDocID() + "\" doctype=\"" + rule.docType
				+ "\" " + "hastopic=\"" + (doc.hasDiscussion ? 1 : 0) + "\" " + "openfrompage=\"" + page
				+ "\" status=\"existing\" isread=\"" + doc.isRead() + "\" " + mode[0] + ">" + outlineContent
				+ "<actions>" + actions + "</actions>" + elementsContent + "<fields>" + fieldsContent + "</fields>"
				+ mode[1] + captions + "</document>";
	}

	@Override
	public XMLResponse save(String key, HashMap<String, String[]> fields, int parentDocID, int parentDocType,
			int pageSize, String currentLang) throws DocumentAccessException, DocumentException, RuleException,
					QueryFormulaParserException, ClassNotFoundException, ComplexObjectException {
		IDatabase db = env.getDataBase();
		XMLResponse result = new XMLResponse(ResponseType.SAVE_FORM);
		String redirectURL = null;
		BaseDocument doc = null;
		QuerySaveProcessor qsp = null;
		PostSaveProcessor psp = null;
		try {
			int docID = Integer.parseInt(key);
			switch (rule.docType) {
			case DOCTYPE_MAIN:
				doc = db.getMainDocumentByID(docID, userSession.currentUser.getAllUserGroups(),
						userSession.currentUser.getUserID());
				if (parentDocID != 0 && parentDocType != DOCTYPE_UNKNOWN) {
					if (db.hasDocumentByComplexID(parentDocID, parentDocType)) {
						if (doc.isNewDoc()) {
							doc.parentDocID = parentDocID;
							doc.parentDocType = parentDocType;
						}
					} else {
						result.resultFlag = false;
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype="
								+ parentDocType);
						return result;
					}
				}
				break;
			case DOCTYPE_GLOSSARY:
				doc = db.getGlossaries().getGlossaryDocumentByID(docID, true,
						userSession.currentUser.getAllUserGroups(), userSession.currentUser.getUserID());
				if (parentDocID != 0 && parentDocType != DOCTYPE_UNKNOWN) {
					if (db.hasDocumentByComplexID(parentDocID, parentDocType)) {
						if (doc.isNewDoc()) {
							doc.parentDocID = parentDocID;
							doc.parentDocType = parentDocType;
						}
					} else {
						result.resultFlag = false;
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype="
								+ parentDocType);
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
				doc = db.getStructure().getGroup(docID, userSession.currentUser.getAllUserGroups(),
						userSession.currentUser.getUserID());
				break;
			case DOCTYPE_TOPIC:
				doc = db.getForum().getTopicByID(docID, userSession.currentUser.getAllUserGroups(),
						userSession.currentUser.getUserID());
				if (parentDocID != 0 && parentDocType != DOCTYPE_UNKNOWN) {
					if (db.hasDocumentByComplexID(parentDocID, parentDocType)) {
						if (doc.isNewDoc()) {
							doc.parentDocID = parentDocID;
							doc.parentDocType = parentDocType;
						}
					} else {
						result.resultFlag = false;
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype="
								+ parentDocType);
						return result;
					}
				}
				break;
			case DOCTYPE_POST:
				doc = db.getForum().getPostByID(docID, userSession.currentUser.getAllUserGroups(),
						userSession.currentUser.getUserID());
				if (parentDocID != 0 && parentDocType != DOCTYPE_UNKNOWN) {
					if (db.hasDocumentByComplexID(parentDocID, parentDocType)) {
						if (doc.isNewDoc()) {
							doc.parentDocID = parentDocID;
							doc.parentDocType = parentDocType;
						}
					} else {
						result.resultFlag = false;
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype="
								+ parentDocType);
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
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype="
								+ parentDocType);
						return result;
					}
				}
				break;
			case DOCTYPE_GLOSSARY:
				doc = new Glossary(env, userSession.currentUser);
				if (parentDocID != 0 && parentDocType != DOCTYPE_UNKNOWN) {
					if (db.hasDocumentByComplexID(parentDocID, parentDocType)) {
						doc.parentDocID = parentDocID;
						doc.parentDocType = parentDocType;
					} else {
						result.resultFlag = false;
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype="
								+ parentDocType);
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
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype="
								+ parentDocType);
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
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype="
								+ parentDocType);
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
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype="
								+ parentDocType);
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
						result.addMessage("Unable to get parent document parentid=" + parentDocID + ", parentdoctype="
								+ parentDocType);
						return result;
					}
				}
				doc.setAuthor(userSession.currentUser.getUserID());
				break;
			}
		}

		if (rule.advancedQSEnable) {
			qsp = new QuerySaveProcessor(env, doc, userSession.currentUser, currentLang, fields);
			QuerySaveResult qsResult = qsp.processScript(rule.qsClassName.getClassName());
			result.resultFlag = qsResult.continueSave;
			result.addMessage(qsResult.msg);
			redirectURL = qsResult.redirectURL;
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
					result.addMessage(Integer.toString(docID));
					// result.addMessage("");
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
				result.setMessage(e.getMessage());
				result.addMessage(e.exceptionType.toString());
				AppEnv.logger.errorLogEntry(e.getMessage());
			} catch (Exception e) {
				result.setResponseStatus(false);
				result.setMessage("Runtime objects error");
				result.addMessage(e.getMessage());
				for (StackTraceElement element : e.getStackTrace()) {
					AppEnv.logger.errorLogEntry(element.toString());
				}
				AppEnv.logger.errorLogEntry(e.getMessage());
				e.printStackTrace();
				AppEnv.logger.errorLogEntry("Runtime objects error");
			}

			if (result.resultFlag) {
				for (IQuerySaveTransaction toPostObects : qsp.transactionToPost) {
					toPostObects.post();
				}

				if (rule.advancedPSEnable) {
					if (doc instanceof Glossary) {
						psp = new PostSaveProcessor(doc, userSession.currentUser);
					} else {
						psp = new PostSaveProcessor(doc, userSession.currentUser);
					}
					psp.setClass(rule.psClassName);
					psp.start();
					for (IQuerySaveTransaction toPostObects : psp.transactionToPost) {
						toPostObects.post();
					}
				}
			}
			if (doc.toDeleteAfterSave != null) {
				Environment.fileToDelete.add(doc.toDeleteAfterSave);
			}

			if (redirectURL != null) {
				/*
				 * try{ String page[] = fields.get("page"); int pageNum =
				 * Integer.parseInt(page[0]); redirectURL = redirectURL +
				 * "&amp;page=" + page[0]; }catch(Exception nfe){
				 *
				 * }
				 */
				result.setRedirect(redirectURL);
			}

			userSession.addExpandedThread(new DocID(parentDocID, parentDocType));
			userSession.setFlashViewEntry(new DocID(docID, doc.docType));

		}

		return result;
	}
}
