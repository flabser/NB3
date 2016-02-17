package kz.flabs.scriptprocessor.page.doscript;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Vocabulary;
import kz.flabs.scriptprocessor.ScriptEvent;
import kz.flabs.scriptprocessor.ScriptShowField;
import kz.flabs.servlets.PublishAsType;
import kz.flabs.servlets.pojo.OutcomeType;
import kz.lof.dataengine.jpa.IAppEntity;
import kz.lof.scripting.POJOObjectAdapter;
import kz.lof.scripting.IPOJOObject;
import kz.lof.scripting._POJOListWrapper;
import kz.lof.scripting._POJOObjectWrapper;
import kz.lof.webserver.servlet.IOutcomeObject;
import kz.lof.webserver.servlet.PageOutcome;
import kz.nextbase.script._Exception;
import kz.nextbase.script._Helper;
import kz.nextbase.script._IXMLContent;
import kz.nextbase.script._Session;
import kz.nextbase.script._Validation;
import kz.nextbase.script._WebFormData;

public abstract class AbstractPage extends ScriptEvent implements IPageScript {
	private _WebFormData formData;
	private PageOutcome result = new PageOutcome();

	@Override
	public void setSession(_Session ses) {
		this.ses = ses;
		result.setSession(ses);
	}

	@Deprecated
	public void publishElement(String entryName, String value) {
		result.addContent(new ScriptShowField(entryName, value));
	}

	@Deprecated
	public void publishElement(String entryName, Object value) throws _Exception {
		if (value == null) {
			result.addContent(new ScriptShowField(entryName, ""));
		} else if (value instanceof String) {
			result.addContent(new ScriptShowField(entryName, (String) value));
		} else if (value instanceof _IXMLContent) {
			result.addContent(new ScriptShowField(entryName, (_IXMLContent) value));
		} else if (value instanceof Date) {
			result.addContent(new ScriptShowField(entryName, _Helper.getDateAsString((Date) value)));
		} else if (value instanceof Enum) {
			result.addContent(new ScriptShowField(entryName, ((Enum) value).name()));
		} else if (value instanceof BigDecimal) {
			result.addContent(new ScriptShowField(entryName, value.toString()));
		}
	}

	@Deprecated
	public void publishElement(_IXMLContent value) {
		toPublishElement.add(value);
	}

	public void addMsg(String m) {
		result.addMessage(m);
	}

	public void setError(String m) {
		setBadRequest();
		result.setType(OutcomeType.SERVER_ERROR);
		result.setMessage(m);
	}

	public void setPublishAsType(PublishAsType respType) {
		result.publishAs = respType;
	}

	public <T extends Enum<?>> String[] getLocalizedWord(T[] enumObj, String lang) {
		String[] array = new String[enumObj.length];
		try {
			for (int i = 0; i < enumObj.length; i++) {
				array[i] = vocabulary.getSentenceCaption(enumObj[i].name(), lang).word;
			}

			return array;

		} catch (Exception e) {
			return array;
		}
	}

	public void showFile(String filePath, String fileName) {
		result.publishAs = PublishAsType.OUTPUTSTREAM;
		result.setFile(filePath, fileName);
	}

	@Override
	public void setFormData(_WebFormData formData) {
		this.formData = formData;
	}

	@Override
	public void setCurrentLang(Vocabulary vocabulary, LanguageType lang) {
		this.lang = lang;
		this.vocabulary = vocabulary;
	}

	protected void setContent(String elementName, List<?> langs) {
		result.addObject(new _POJOObjectWrapper(new POJOObjectAdapter() {
			@Override
			public String getFullXMLChunk(LanguageType lang) {
				StringBuffer val = new StringBuffer(500);
				val.append("<" + elementName + ">");
				for (Object obj : langs) {
					val.append("<entry>" + obj.toString() + "</entry>");
				}
				return val.append("</" + elementName + ">").toString();
			}
		}, lang));
	}

	protected void setValidation(_Validation obj) {
		result.setType(OutcomeType.VALIDATION_ERROR);
		result.setValidation(obj);
	}

	protected void setValidation(String localizedMessage) {
		_Validation ve = new _Validation();
		ve.addError("", "", localizedMessage);
		setValidation(ve);
	}

	protected void setContent(List<IOutcomeObject> list) {
		result.addContent(list);

	}

	protected void setContent(IPOJOObject document) {
		result.addObject(new _POJOObjectWrapper(document, lang));
	}

	@Deprecated
	protected void setContent(_POJOObjectWrapper _POJOObjectWrapper) {
		result.addContent(_POJOObjectWrapper);

	}

	// @Deprecated
	protected void setContent(_POJOListWrapper _POJOListWrapper) {
		result.addContent(_POJOListWrapper);

	}

	protected void startSaveFormTransact(IAppEntity entity) {
		ses.addFormTransaction(entity, formData.getReferrer());

	}

	protected void finishSaveFormTransact(IAppEntity entity) {
		result.setRedirectURL(ses.getTransactRedirect(entity));
		result.setFlash(entity.getId().toString());
		result.setType(OutcomeType.DOCUMENT_SAVED);

	}

	protected void setBadRequest() {
		result.setBadRequest();
	}

	@Override
	public PageOutcome processCode(String method) {
		try {
			if (method.equalsIgnoreCase("POST")) {
				doPOST(ses, formData, lang);
			} else {
				doGET(ses, formData, lang);
			}
		} catch (Exception e) {
			result.setError(e);
			error(e);
		}
		return result;

	}

	public abstract void doGET(_Session session, _WebFormData formData, LanguageType lang) throws _Exception;

	public abstract void doPOST(_Session session, _WebFormData formData, LanguageType lang) throws _Exception;
}
