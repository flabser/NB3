package kz.flabs.sourcesupplier;

import groovy.lang.GroovyObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import kz.flabs.appenv.AppEnv;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.DocumentExceptionType;
import kz.flabs.exception.RuleException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.BlobField;
import kz.flabs.runtimeobj.document.BlobFile;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.project.Project;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.document.task.Control;
import kz.flabs.runtimeobj.document.task.Task;
import kz.flabs.runtimeobj.queries.Query;
import kz.flabs.users.User;
import kz.flabs.users.UserSession;
import kz.flabs.util.Util;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.flabs.webrule.form.IShowField;
import kz.flabs.webrule.query.QueryRule;
import kz.flabs.dataengine.Const;

public class FormDataSourceSupplier extends SourceSupplier {
	private UserSession userSession;
	private BaseDocument doc;
	private int parentDocID, parentDocType;

	public FormDataSourceSupplier(AppEnv env, UserSession userSession, int parentDocID, int parentDocType) {
		super(env);
		this.userSession = userSession;	
		this.parentDocID = parentDocID;
		this.parentDocType = parentDocType;
		contextType = SourceSupplierContextType.DEFAULT_FORM;			
	}

	public FormDataSourceSupplier(AppEnv env, UserSession userSession, BaseDocument doc, int parentDocID, int parentDocType) {
		super(env);
		this.userSession = userSession;
		this.doc = doc;
		this.parentDocID = parentDocID;
		this.parentDocType = parentDocType;
		contextType = SourceSupplierContextType.SHOW_FORM;			
	}

	public FormDataSourceSupplier(AppEnv env, UserSession userSession, BaseDocument doc) {
		super(env);
		this.userSession = userSession;
		this.doc = doc;
		contextType = SourceSupplierContextType.SHOW_FORM;			
	}


	public ArrayList<String[]> getValToShow(IShowField sf) throws RuleException, DocumentAccessException, DocumentException, QueryFormulaParserException, ComplexObjectException{
		ArrayList<String[]> vals = new ArrayList<String[]>();	
		String[] tagValue = getValueAsStr(sf.getSourceType(), sf.getValue(), sf.getCompiledClass(),  sf.getMacro());	

		if (tagValue[0] != null){
			vals = publishAs(sf.getPublishAs(), tagValue);			
		}
		return vals;
	}


	public ArrayList<String[]> getValueAsList(IShowField sf) throws DocumentAccessException, RuleException, QueryFormulaParserException, ComplexObjectException{
		ArrayList<String[]> vals = new ArrayList<String[]>();	
		String result[] = new String[1];
		try{	
			switch (sf.getSourceType()){
			case DOC_FIELD:
				result = doc.getValueAsString(sf.getValue());	
				break;
			case PARENTDOCFIELD:
				if (parentDocID == 0 && parentDocType == Const.DOCTYPE_UNKNOWN){
					result[0] = "";
				}else{
					Document parentDoc = (Document)env.getDataBase().getDocumentByComplexID(parentDocType, parentDocID);	
					result = parentDoc.getValueAsString(sf.getValue());
				}
				break;	
			case MAINDOCFIELD:	/****!!!!!!!!!*/		
				if (parentDocID == 0 && parentDocType == Const.DOCTYPE_UNKNOWN){
					result[0] = "";
				}else{
					Document parentDoc = (Document)env.getDataBase().getDocumentByComplexID(parentDocType, parentDocID);	
					if (parentDoc.parentDocID == 0 && parentDoc.parentDocType == Const.DOCTYPE_UNKNOWN) {
						result = parentDoc.getValueAsString(sf.getValue());
					} else {
						BaseDocument grandParentDoc = new RuntimeObjUtil().getGrandParentDocument(env.getDataBase(), parentDoc);
						if (grandParentDoc != null){
							result = grandParentDoc.getValueAsString(sf.getValue());
						}else{
							result[0] = "";
						}
					}

				}
				//break;	
				//Document grandParentDoc = RuntimeObjUtil.getGrandParentDocument(env.getDataBase(),doc);	
				//result = grandParentDoc.getValueAsString(value);			
				break;	
			case STATIC:
				result[0] = sf.getValue();			
				break;
			case SCRIPT:	
				SourceSupplier ss = null; //need refactor 
				if (doc == null){ 
					ss = new SourceSupplier(env.getDataBase(), userSession.currentUser.getUserID());					
				}else{
					ss = new SourceSupplier((Document)doc, userSession.currentUser, this.doc.getAppEnv());
				}
				result = ss.getValueAsStr(ValueSourceType.SCRIPT, sf.getValue(), sf.getCompiledClass(), sf.getAttrMacro());				
				break;			
			case QUERY:											
				QueryRule rule = (QueryRule)env.ruleProvider.getRule(QUERY_RULE, sf.getValue());
				Query queryRule = new Query(env, rule, userSession.currentUser);
				result[0] = queryRule.toXML();				
				break;
			case MACRO:					
				result[0] = macroProducer(sf.getMacro());
				break;
			case DOC_ATTACHMENT:					
				BlobField blobField = doc.blobFieldsMap.get(sf.getValue());		
				if (blobField != null){			
					StringBuffer xmlContent = new StringBuffer(10000);
					Collection<BlobFile> files = blobField.getFiles();					
					for (BlobFile file: files){	
						xmlContent.append("<entry filename=\"" + file.originalName + "\" hash=\"" + file.checkHash + "\" ><comment>" + file.comment + "</comment></entry>");						
					}
					result[0] = xmlContent.toString();
					//return vals;
				}else{
					throw new DocumentException(DocumentExceptionType.ATTACHMENT_FIELD_NOT_FOUND, sf.getValue());
				}				
			}

			if (result != null && result[0] != null){
				vals = publishAs(sf.getPublishAs(), result);			
			}

		}catch(DocumentException de){
			AppEnv.logger.errorLogEntry(de.getMessage());
		}
		return vals;		

	}

	public String[] getValueAsStr(ValueSourceType sourceType, String value, Class<GroovyObject> compiledClass, Macro macro) throws DocumentAccessException, RuleException, QueryFormulaParserException, ComplexObjectException{

		String result[] = new String[1];
		try{	
			switch (sourceType){
			case DOC_FIELD:
				result = doc.getValueAsString(value);				
				break;
			case PARENTDOCFIELD:
				if (parentDocID == 0 && parentDocType == Const.DOCTYPE_UNKNOWN){
					result[0] = "";
				}else{
					Document parentDoc = (Document)env.getDataBase().getDocumentByComplexID(parentDocType, parentDocID);	
					result = parentDoc.getValueAsString(value);
				}
				break;	
			case MAINDOCFIELD:	/****!!!!!!!!!*/		
				if (parentDocID == 0 && parentDocType == Const.DOCTYPE_UNKNOWN){
					result[0] = "";
				}else{
					Document parentDoc = (Document)env.getDataBase().getDocumentByComplexID(parentDocType, parentDocID);	
					if (parentDoc.parentDocID == 0 && parentDoc.parentDocType == Const.DOCTYPE_UNKNOWN) {
						result = parentDoc.getValueAsString(value);
					} else {
						BaseDocument grandParentDoc = new RuntimeObjUtil().getGrandParentDocument(env.getDataBase(), parentDoc);
						if (grandParentDoc != null){
							result = grandParentDoc.getValueAsString(value);
						}else{
							result[0] = "";
						}
					}

				}
				//break;	
				//Document grandParentDoc = RuntimeObjUtil.getGrandParentDocument(env.getDataBase(),doc);	
				//result = grandParentDoc.getValueAsString(value);			
				break;	
			case STATIC:
				result[0] = value;			
				break;
			case SCRIPT:	
				SourceSupplier ss = null; //need refactor 
				if (doc == null){ 
					ss = new SourceSupplier(env.getDataBase(), userSession.currentUser.getUserID());					
				}else{
					ss = new SourceSupplier((Document)doc, userSession.currentUser, this.doc.getAppEnv());
				}
				result = ss.getValueAsStr(ValueSourceType.SCRIPT, value, compiledClass, macro);				
				break;			
			case QUERY:											
				QueryRule rule = (QueryRule)env.ruleProvider.getRule(QUERY_RULE, value);
				Query queryRule = new Query(env, rule, userSession.currentUser);
				result[0] = queryRule.toXML();				
				break;
			case MACRO:					
				result[0] = macroProducer(macro);
				break;
			}

		}catch(DocumentException de){
			AppEnv.logger.errorLogEntry(de.getMessage());
		}
		return result;
	}

	public String macroProducer(Macro macro) throws DocumentException{
		switch(macro){
		case VIEW_TEXT:
			return doc.getViewText();						
		case AUTHOR:
			return doc.getAuthorID();						
		case HAS_ATTACHMENT:
			return doc.hasAttach();							
		case HAS_RESPONSE:
			return doc.hasResponse(userSession.currentUser.getAllUserGroups(), userSession.currentUser.getUserID());					
		case MACRO_CURRENT_TIME:
			return Util.dateTimeFormat.format(new Date());					
		case CURRENT_USER:
			return userSession.currentUser.getUserID();			
		case CURRENT_USER_DEPARTMENT:
			Employer empl = userSession.currentUser.getAppUser();
			if (empl != null){
				return Integer.toString(empl.getDepID());
			}else{
				return "";
			}			
		case CURRENT_USER_POSITION:
			empl = userSession.currentUser.getAppUser();
			if (empl != null){
				return Integer.toString(empl.getPostID());
			}else{
				return "";
			}			
		case EXECUTORS:
			Task task = (Task)doc;
			return task.getExecutorsAsXML().toString();			
		case RECIPIENTS:
			if (doc instanceof Project){
				Project prj = (Project)doc; 
				return prj.getRecipientsAsXML().toString();					
			}else{
				return "";
			}			
		case CONTROL:
			Control ctrl;
			if (doc instanceof Task){
				Task t = (Task)doc;
				ctrl = t.getControl();
			}else{
				ctrl = new Control();
			}
			return ctrl.getContent();			
		case FILTER:
			Employer employer = (Employer) doc;
			return employer.getFiltersAsXML().toString();	
		case COORD_BLOCKS:
			Project prj = (Project)doc;
			return prj.getBlocksAsXML().toString();				
		case MACRO_CURRENT_USER_ID:
			return userSession.currentUser.getUserID();		
		case APP_URL:
			Employer emp = (Employer)doc;
			User user = emp.getUser();
			return user.getAppURLAsXml();
		case AVAILABLE_APPLICATIONS:
			user = userSession.currentUser;
			return getAvailableApps(user);			
		case ALL_APPLICATIONS: 
			return getAllApps();
		default:				
			return "";
		}				
	}
}
