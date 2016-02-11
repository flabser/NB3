package kz.flabs.sourcesupplier;

import groovy.lang.GroovyObject;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Localizator;
import kz.flabs.localization.LocalizatorException;
import kz.flabs.localization.SentenceCaption;
import kz.flabs.localization.Vocabulary;
import kz.flabs.parser.ComplexKeyParser;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.scriptprocessor.IScriptProcessor;
import kz.flabs.scriptprocessor.SessionScriptProcessor;
import kz.flabs.scriptprocessor.SimpleScriptProcessor;
import kz.flabs.scriptprocessor.SimpleScriptProcessorWithLang;
import kz.flabs.users.User;
import kz.flabs.util.Util;
import kz.flabs.webrule.IRuleValue;
import kz.flabs.webrule.Lang;
import kz.flabs.webrule.Role;
import kz.flabs.webrule.Skin;
import kz.flabs.webrule.constants.TagPublicationFormatType;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.nextbase.script._Session;
import kz.pchelka.scheduler.IProcessInitiator;
import kz.lof.env.Environment;
import kz.lof.server.Server;

@Deprecated
public class SourceSupplier implements IProcessInitiator, Const {
	public SourceSupplierContextType contextType;
	public HashMap<String, Vocabulary> staticGlossaries = new HashMap<String, Vocabulary>();

	protected AppEnv env;

	private IScriptProcessor scriptProcessor;
	private BaseDocument doc;
	private Vocabulary vocabulary;
	private String lang;
	private IDatabase db;
	private User user;

	public SourceSupplier(AppEnv env) {
		vocabulary = env.vocabulary;
		contextType = SourceSupplierContextType.APP_ENVIRONMENT;
		user = new User(Const.sysUser);
		_Session session = new _Session(env, user);
		scriptProcessor = new SimpleScriptProcessor(session);
		this.env = env;
	}

	public SourceSupplier(AppEnv env, String lang) {
		this.env = env;
		contextType = SourceSupplierContextType.VOCABULARY;
		this.vocabulary = env.vocabulary;
		this.lang = lang;
		user = new User(Const.sysUser);
	}

	public SourceSupplier(BaseDocument doc2, User user, AppEnv env) {
		vocabulary = env.vocabulary;
		contextType = SourceSupplierContextType.DOCUMENT;
		if (doc2 == null) {
			_Session session = new _Session(env, user);
			scriptProcessor = new SimpleScriptProcessor(session);
		} else {

			this.doc = doc2;
		}
		this.user = user;

	}

	public SourceSupplier(BaseDocument doc, User user, AppEnv env, String lang) {
		vocabulary = env.vocabulary;
		this.lang = lang;
		contextType = SourceSupplierContextType.DOCUMENT_WITH_LANG;
		if (doc == null) {
			_Session session = new _Session(env, user);
			scriptProcessor = new SimpleScriptProcessorWithLang(session, lang);
		} else {

			this.doc = doc;
		}
		this.user = user;
	}

	public SourceSupplier(User user, AppEnv env, String lang) {
		this.user = user;
		this.env = env;
		this.lang = lang;
		vocabulary = env.vocabulary;
		contextType = SourceSupplierContextType.SIMPLE_WITH_LANG;
		_Session session = new _Session(env, user);
		scriptProcessor = new SimpleScriptProcessorWithLang(session, lang);

	}

	public SourceSupplier(IDatabase db, String userID) {
		contextType = SourceSupplierContextType.DATABASE;
		this.db = db;
		User user = new User(userID);
		this.user = user;
		scriptProcessor = new SessionScriptProcessor(db, user);
		env = db.getParent();
	}

	public SourceSupplier(IDatabase db, User user) {
		contextType = SourceSupplierContextType.DATABASE;
		this.db = db;
		this.user = user;
		scriptProcessor = new SessionScriptProcessor(db, user);
	}

	public SourceSupplier(String userID) {
		contextType = SourceSupplierContextType.SIMPLE;
		scriptProcessor = new SimpleScriptProcessor();
		User user = new User(userID);
		this.user = user;
	}

	public ArrayList<BaseDocument> getDocuments(IRuleValue sourceVal, User user) throws DocumentException, DocumentAccessException, RuleException,
	        QueryFormulaParserException, QueryException, ComplexObjectException {
		ArrayList<BaseDocument> col = new ArrayList<BaseDocument>();
		switch (sourceVal.getSourceType()) {
		case QUERY:

			break;
		case MACRO:

			break;
		case STATIC:
			break;
		case SCRIPT:
			break;
		}
		return col;

	}

	public String getValueAsIdAttr(String value) throws DocumentException {
		if (value != null && (!value.equals(""))) {
			return " id=\"" + value + "\" ";
		} else {
			return "";
		}
	}

	/** @deprecated **/
	@Deprecated
	public String[] getValueAsString(ValueSourceType sourceType, String value, Macro macro) throws DocumentException, DocumentAccessException,
	        RuleException, QueryFormulaParserException {
		String returnVal[] = { "" };

		switch (sourceType) {
		case DOCFIELD:
			return doc.getValueAsString(value);
		case MACRO:
			returnVal[0] = macroProducer(macro);
			return returnVal;
		case STATIC:
			returnVal[0] = value;
			return returnVal;
		case SCRIPT:
			return scriptProcessor.processString(value);

		}
		return returnVal;

	}

	public String[] getValueAsStr(ValueSourceType sourceType, String value, Class<GroovyObject> compiledClass, Macro macro) throws DocumentException,
	        DocumentAccessException, RuleException, QueryFormulaParserException, ComplexObjectException {
		String returnVal[] = { "" };

		switch (sourceType) {
		case SCRIPT:
			return scriptProcessor.processString(compiledClass);
		case DOCFIELD:
			return doc.getValueAsString(value);
		case MACRO:
			returnVal[0] = macroProducer(macro);
			return returnVal;
		case STATIC:
			returnVal[0] = value;
			return returnVal;
		}
		return returnVal;

	}

	public String[] getValueAsString(ValueSourceType sourceType, String value) throws DocumentException {
		String returnVal[] = { "" };

		try {
			switch (sourceType) {
			case SCRIPT:
				return scriptProcessor.processString(value);
			case KEYWORD:
				returnVal = vocabulary.getWord(value, lang);
				return returnVal;
			case DOCFIELD:
				return doc.getValueAsString(value);
			case STATIC:
				returnVal[0] = value;
				return returnVal;
			}
			return returnVal;
		} catch (Exception e) {
			return returnVal;
		}

	}

	public SentenceCaption getValueAsCaption(ValueSourceType sourceType, String value) throws DocumentException {
		try {
			switch (sourceType) {
			case KEYWORD:
				// System.out.println("value=" + value);
				return vocabulary.getSentenceCaption(value, lang);
			default:
				return new SentenceCaption(value, value);
			}
		} catch (Exception e) {
			env.logger.warningLogEntry("Sentence for value \"" + value + "\" has not found therefore return value itself");
			return new SentenceCaption(value, value);
		}
	}

	public StringBuffer getDataAsXML(ValueSourceType sourceType, String value, Macro macro, String lang) throws RuleException, DocumentException,
	        DocumentAccessException, QueryFormulaParserException, QueryException, LocalizatorException {
		StringBuffer xmlContent = new StringBuffer(5000);

		switch (sourceType) {
		case VOCABULARY:
			Vocabulary v = staticGlossaries.get(value);
			if (v == null) {
				Localizator l = new Localizator();
				String vocabuarFilePath = env.globalSetting.rulePath + File.separator + "Resources" + File.separator + "vocabulary.xml";
				v = l.populate(env.appType, vocabuarFilePath);
				staticGlossaries.put(value, v);
			}
			xmlContent.append(v.toXML(lang));
			break;
		case QUERY:

			break;
		case MACRO:
			switch (macro) {
			case AVAILABLE_LANGS:
				if (contextType == SourceSupplierContextType.APP_ENVIRONMENT || contextType == SourceSupplierContextType.SIMPLE_WITH_LANG) {
					for (Lang l : env.globalSetting.langsList) {
						xmlContent.append("<entry>" + l.toXML() + "</entry>");
					}
				}
				break;
			case AVAILABLE_SKINS:
				if (contextType == SourceSupplierContextType.APP_ENVIRONMENT || contextType == SourceSupplierContextType.SIMPLE_WITH_LANG) {
					for (Skin skin : env.globalSetting.skinsList) {
						xmlContent.append("<entry>" + skin.toXML() + "</entry>");
					}
				}
				break;
			case APPLICATION_ROLES:
				if (contextType == SourceSupplierContextType.APP_ENVIRONMENT || contextType == SourceSupplierContextType.SIMPLE_WITH_LANG) {
					for (Role role : env.getRolesList()) {
						xmlContent.append("<entry>" + role.toXML() + "</entry>");
					}
				}
				break;
			case APPLICATION_TYPE:
				if (contextType == SourceSupplierContextType.APP_ENVIRONMENT || contextType == SourceSupplierContextType.SIMPLE_WITH_LANG) {
					xmlContent.append(env.appType);
				}
				break;
			case APPLICATION_LOGO:
				if (contextType == SourceSupplierContextType.APP_ENVIRONMENT || contextType == SourceSupplierContextType.SIMPLE_WITH_LANG) {
					xmlContent.append(env.globalSetting.logo);
				}
				break;
			case APPLICATION_NAME:
				if (contextType == SourceSupplierContextType.APP_ENVIRONMENT || contextType == SourceSupplierContextType.SIMPLE_WITH_LANG) {
					xmlContent.append(env.globalSetting.appName);
				}
				break;
			case AVAILABLE_APPLICATIONS:
				xmlContent.append(getAvailableApps(user));
				break;
			case ALL_APPLICATIONS:
				xmlContent.append(getAllApps());
				break;
			}
			break;
		case STATIC:
			xmlContent.append(value);
			break;
		case SCRIPT:
			String result[] = scriptProcessor.processString(value);
			for (String res : result) {
				xmlContent.append(res);
			}
			break;
		default:
			xmlContent.append("unknown source " + contextType);

		}
		return xmlContent;
	}

	public ArrayList<String[]> publishAs(TagPublicationFormatType publishAs, String[] tagValue) throws DocumentException {
		ArrayList<String[]> vals = new ArrayList<String[]>();
		String result[] = new String[2];

		if (publishAs != null && tagValue != null) {
			switch (publishAs) {
			case HTML:
				for (String val : tagValue) {
					result[0] = val.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
					vals.add(result);
				}
				break;
			case USER:

				break;
			case GLOSSARY:

				break;
			case GLOSSARY_DATA:

				break;
			case EMPLOYER:

				break;
			case COMPLEX_KEY:
				for (String IDAString : tagValue) {
					List<DocID> keys = ComplexKeyParser.parse(IDAString);
					DocID subKey = keys.get(0);
					if (subKey != null) {
						String[] id = { String.valueOf(subKey.id) };
						switch (subKey.type) {
						case DOCTYPE_ORGANIZATION:
							return this.publishAs(TagPublicationFormatType.ORGANIZATION, id);
						case DOCTYPE_DEPARTMENT:
							return this.publishAs(TagPublicationFormatType.DEPARTMENT, id);
						case DOCTYPE_EMPLOYER:
							return this.publishAs(TagPublicationFormatType.EMPLOYER, id);
						}
					}
				}
			case ORGANIZATION:

				break;
			case DEPARTMENT:

				break;
			case FILTER:

				break;
			case GROUP:

				break;
			case CONTROLDAYS:
				for (String IDAsString : tagValue) {
					try {
						Date ctrlDate = Util.derbyDateTimeFormat.parse(IDAsString);
						Date now = new Date();
						long diff = (ctrlDate.getTime() - now.getTime()) / (86400000);
						result[0] = Long.toString(diff);
						result[1] = IDAsString;
						vals.add(result.clone());
					} catch (ParseException e) {
						AppEnv.logger.errorLogEntry(e);
					}
				}
				break;
			default:
				for (String val : tagValue) {
					result[0] = val.replace("&", "&amp;");
					vals.add(result.clone());
				}
			}
		} else {
			for (String val : tagValue) {
				result[0] = val;
				vals.add(result);
			}
		}
		return vals;
	}

	public String macroProducer(Macro macro) throws DocumentException {
		// if (doc == null)macro = DocumentMacros.CURRENT_USER;
		switch (macro) {
		case CURRENT_USER:
			return user.getUserID();
		case AUTHOR:
			return doc.getAuthorID();

		case SERVER_VERSION:
			return Server.serverVersion;
		case COMPILATION_TIME:
			return Server.compilationTime;
		case ORG_NAME:
			return env.globalSetting.orgName;
		case APPLICATION_TYPE:
			return env.appType;
		case APPLICATION_LOGO:
			return env.globalSetting.logo;
		default:
			return "";
		}
	}

	public String getAvailableApps(User user) {
		String result = "";
		SourceSupplier ss = new SourceSupplier(env, LanguageType.RUS.name());
		// user.getSession().lang
		for (AppEnv appEnv : Environment.getApplications()) {
			if (appEnv.isValid && !appEnv.globalSetting.isWorkspace) {
				if (user.authorized) {
					if (user.enabledApps.containsKey(appEnv.appType)) {
						if (appEnv.globalSetting.defaultRedirectURL.equalsIgnoreCase("")) {
							result += "<entry  mode=\"off\"><apptype>" + appEnv.appType + "</apptype>";
							result += "<redirect>Error?type=ws_no_redirect_url</redirect>";
						} else {
							result += "<entry  mode=\"on\"><apptype>" + appEnv.appType + "</apptype>";
							// result += "<redirect>" +
							// Environment.getFullHostName() + "/" +
							// appEnv.appType + "/" +
							// appEnv.globalSetting.defaultRedirectURL.replace("&","&amp;")
							// + "</redirect>";
							result += "<redirect>" + appEnv.appType + "/" + appEnv.globalSetting.defaultRedirectURL.replace("&", "&amp;")
							        + "</redirect>";
						}
						result += "<logo>" + appEnv.globalSetting.logo + "</logo>";
						result += "<orgname>" + appEnv.globalSetting.orgName + "</orgname>";
						// result += "<description>" +
						// appEnv.globalSetting.description +
						// "</description></entry>";
						// caption =
						// captionTextSupplier.getValueAsCaption(entry.captionValueSource,
						// entry.captionValue).toAttrValue();
						try {
							result += "<description>" + ss.getValueAsCaption(ValueSourceType.KEYWORD, appEnv.globalSetting.description).word
							        + "</description></entry>";
						} catch (DocumentException e) {
							result += "<description>" + appEnv.globalSetting.description + "</description></entry>";
						}
					}
				} else {
					/*
					 * xmlContent.append("<entry  mode=\"off\"><apptype>" +
					 * appEnv.appType + "</apptype>"); xmlContent.append(
					 * "<redirect>Error?type=ws_auth_error</redirect>");
					 * xmlContent.append("<logo>" + appEnv.globalSetting.logo +
					 * "</logo>"); xmlContent.append("<orgname>" +
					 * appEnv.globalSetting.orgName + "</orgname>");
					 * xmlContent.append("<description>" +
					 * appEnv.globalSetting.description +
					 * "</description></entry>");
					 */
				}
			}
		}
		if (user.isSupervisor()) {
			result += "<entry  mode=\"on\"><apptype>Administrator</apptype>";
			result += "<redirect>Administrator</redirect>";
			result += "<logo>nextbase_logo.png</logo>";
			result += "<orgname></orgname>";
			result += "<description>Control panel</description></entry>";
		}
		return result;
	}

	protected String getAllApps() {
		String result = "";
		for (AppEnv appEnv : Environment.getApplications()) {
			if (!appEnv.globalSetting.isWorkspace) {
				result += "<entry><apptype>" + appEnv.appType + "</apptype>";
				result += "<logo>" + appEnv.globalSetting.logo + "</logo>";
				result += "<orgname>" + appEnv.globalSetting.orgName + "</orgname>";
				result += "<description>" + appEnv.globalSetting.description + "</description></entry>";
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return "context=" + contextType + ", currentuser=" + user.getUserID() + ", scriptprocessor = " + scriptProcessor;
	}

	@Override
	public String getOwnerID() {
		return this.getClass().getSimpleName();
	}
}
