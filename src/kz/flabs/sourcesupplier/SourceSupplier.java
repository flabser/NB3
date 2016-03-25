package kz.flabs.sourcesupplier;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.DocumentException;
import kz.flabs.localization.SentenceCaption;
import kz.flabs.localization.Vocabulary;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.scriptprocessor.IScriptProcessor;
import kz.flabs.users.User;
import kz.flabs.util.Util;
import kz.flabs.webrule.constants.TagPublicationFormatType;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.lof.appenv.AppEnv;
import kz.lof.scripting._Session;
import kz.lof.user.AnonymousUser;

@Deprecated
public class SourceSupplier implements Const {
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
		_Session session = new _Session(env, new AnonymousUser());

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
			_Session session = new _Session(env, new AnonymousUser());

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
			_Session session = new _Session(env, new AnonymousUser());

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
		_Session session = new _Session(env, new AnonymousUser());

	}

	public SourceSupplier(IDatabase db, String userID) {
		contextType = SourceSupplierContextType.DATABASE;
		this.db = db;
		User user = new User(userID);
		this.user = user;

	}

	public SourceSupplier(IDatabase db, User user) {
		contextType = SourceSupplierContextType.DATABASE;
		this.db = db;
		this.user = user;

	}

	public SourceSupplier(String userID) {
		contextType = SourceSupplierContextType.SIMPLE;

		User user = new User(userID);
		this.user = user;
	}

	public String getValueAsIdAttr(String value) throws DocumentException {
		if (value != null && (!value.equals(""))) {
			return " id=\"" + value + "\" ";
		} else {
			return "";
		}
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
						Date ctrlDate = Util.simpleDateTimeFormat.parse(IDAsString);
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

	@Override
	public String toString() {
		return "context=" + contextType + ", currentuser=" + user.getUserID() + ", scriptprocessor = " + scriptProcessor;
	}

}
