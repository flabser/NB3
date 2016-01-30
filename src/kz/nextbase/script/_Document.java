package kz.nextbase.script;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IGlossaries;
import kz.flabs.dataengine.IQueryFormula;
import kz.flabs.dataengine.ISelectFormula;
import kz.flabs.dataengine.h2.glossary.GlossaryQueryFormula;
import kz.flabs.dataengine.h2.queryformula.SelectFormula;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.DocumentExceptionType;
import kz.flabs.exception.LicenseException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.CrossLink;
import kz.flabs.runtimeobj.CrossLinkCollection;
import kz.flabs.runtimeobj.Form;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.BlobField;
import kz.flabs.runtimeobj.document.BlobFile;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.Execution;
import kz.flabs.runtimeobj.document.Field;
import kz.flabs.runtimeobj.document.IComplexObject;
import kz.flabs.runtimeobj.document.coordination.BlockCollection;
import kz.flabs.runtimeobj.document.glossary.Glossary;
import kz.flabs.runtimeobj.document.project.Project;
import kz.flabs.runtimeobj.document.structure.Department;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.document.structure.EmployerCollection;
import kz.flabs.runtimeobj.document.structure.Organization;
import kz.flabs.runtimeobj.document.task.Control;
import kz.flabs.runtimeobj.document.task.ExecsBlock;
import kz.flabs.runtimeobj.document.task.GrantedBlock;
import kz.flabs.runtimeobj.document.task.GrantedBlockCollection;
import kz.flabs.runtimeobj.document.task.Task;
import kz.flabs.scriptprocessor.ScriptProcessor;
import kz.flabs.scriptprocessor.ScriptProcessorUtil;
import kz.flabs.users.Reader;
import kz.flabs.users.RunTimeParameters;
import kz.flabs.users.User;
import kz.flabs.users.UserException;
import kz.flabs.users.UserSession;
import kz.flabs.webrule.constants.FieldType;
import kz.flabs.webrule.constants.QueryType;
import kz.flabs.webrule.form.FormRule;
import kz.nextbase.script.constants._DocumentModeType;
import kz.nextbase.script.constants._DocumentType;
import kz.nextbase.script.coordination._BlockCollection;
import kz.nextbase.script.project._Project;
import kz.nextbase.script.struct._Department;
import kz.nextbase.script.struct._Employer;
import kz.nextbase.script.struct._EmployerCollection;
import kz.nextbase.script.struct._Organization;
import kz.nextbase.script.task._Control;
import kz.nextbase.script.task._ExecsBlocks;
import kz.nextbase.script.task._GrantedBlock;
import kz.nextbase.script.task._GrantedBlockCollection;
import kz.nextbase.script.task._Task;
import kz.pchelka.env.Environment;

public class _Document {
	public boolean isValid;
	public boolean isNewDoc;
	protected String currentUserID;
	private BaseDocument doc;
	private _Session session;

	public _Document(_Database db) throws DocumentException {
		session = db.getParent();
		currentUserID = session.getCurrentUserID();
		Document doc = new Document(db.dataBase, currentUserID);
		this.doc = doc;
		doc.setNewDoc(true);
		isNewDoc = doc.isNewDoc();
		isValid = doc.isValid;
	}

	public _Document(BaseDocument document) {
		this.doc = document;
		isNewDoc = doc.isNewDoc();
		isValid = doc.isValid;
		currentUserID = document.getCurrentUserID();
	}

	public _Document(BaseDocument document, _Session ses) {
		this(document);
		this.session = ses;
		currentUserID = session.getCurrentUserID();
		// if (document.db != null) {
		// this.db = new _Database(document.db, "", ses);
		// }
	}

	public _Document(BaseDocument document, String user) {
		this(document);
		this.currentUserID = user;
	}

	public _DocumentModeType getEditMode() {
		switch (doc.editMode) {
		case kz.flabs.dataengine.Const.EDITMODE_READONLY:
			return _DocumentModeType.READ_ONLY;
		case kz.flabs.dataengine.Const.EDITMODE_EDIT:
			return _DocumentModeType.EDIT;
		default:
			return _DocumentModeType.NO_ACCESS;
		}
	}

	public String getAuthorID() {
		return doc.getAuthorID();
	}

	public Date getRegDate() {
		return doc.getRegDate();
	}

	public String getViewText() {
		return doc.getViewText();
	}

	public BigDecimal getViewNumber() {
		return doc.getViewNumber();
	}

	public Date getViewDate() {
		return doc.getViewDate();
	}

	public void setCurrentUserID(String userID) {
		this.currentUserID = userID;
	}

	public String getCurrentUserID() {
		return this.currentUserID;
	}

	public _Session getSession() {
		return session;
	}

	public void setSession(_Session ses) {
		session = ses;
	}

	public int getDocID() {
		return doc.getDocID();
	}

	public String getID() {
		return doc.getDdbID();
	}

	public _Field getField(String fieldName) {
		if (doc.hasField(fieldName)) {
			return new _Field(this, fieldName);
		} else {
			BlobField blobField = doc.blobFieldsMap.get(fieldName);
			if (blobField != null) {
				return new _BlobField(this, fieldName, blobField);
			}
		}
		return null;

	}

	public _Employer getCurrentUser() {
		Employer emp = doc.db.getStructure().getAppUser(currentUserID);
		if (emp != null) {
			return new _Employer(emp);
		} else {
			// return new _Employer(currentUserID);
			return null;
		}
	}

	public int isFavourite() {
		return doc.isFavourite();
	}

	/**
	 * You should use getDocumentType() if possible*
	 */
	@Deprecated
	public int getDocType() {
		return doc.docType;
	}

	public void setDocumentType(_DocumentType type) {
		switch (type) {
		case MAINDOC:
			doc.docType = Const.DOCTYPE_MAIN;
			break;
		case PROJECT:
			doc.docType = Const.DOCTYPE_PROJECT;
			break;
		case TASK:
			doc.docType = Const.DOCTYPE_TASK;
			break;
		case EXECUTION:
			doc.docType = Const.DOCTYPE_EXECUTION;
			break;
		case GLOSSARY:
			doc.docType = Const.DOCTYPE_GLOSSARY;
			break;
		case EMPLOYER:
			doc.docType = Const.DOCTYPE_EMPLOYER;
			break;
		case UNKNOWN:
			doc.docType = Const.DOCTYPE_UNKNOWN;
			break;
		case ORGANIZATION:
			doc.docType = Const.DOCTYPE_ORGANIZATION;
			break;
		case ACCOUNT:
			doc.docType = Const.DOCTYPE_ACCOUNT;
			break;
		default:
			doc.docType = Const.DOCTYPE_MAIN;
			break;
		}
	}

	public _DocumentType getDocumentType() {
		if (doc.docType == 896) {
			return _DocumentType.MAINDOC;
		} else if (doc.docType == 899) {
			return _DocumentType.PROJECT;
		} else if (doc.docType == 897) {
			return _DocumentType.TASK;
		} else if (doc.docType == 898) {
			return _DocumentType.EXECUTION;
		} else if (doc.docType == 894) {
			return _DocumentType.GLOSSARY;
		} else if (doc.docType == 889) {
			return _DocumentType.EMPLOYER;
		} else if (doc.docType == 891) {
			return _DocumentType.ORGANIZATION;
		} else if (doc.docType == 890) {
			return _DocumentType.UNKNOWN;
		} else {
			return _DocumentType.UNKNOWN;
		}

	}

	public void setAuthor(String author) {
		doc.setAuthor(author);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof _Document)) {
			return false;
		}

		_Document document = (_Document) o;

		if (!doc.equals(document.doc)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return doc.hashCode();
	}

	public String getValueString(String fieldName) {
		try {
			String val[] = { "" };
			if (fieldName.equalsIgnoreCase("viewtext")) {
				val[0] = doc.getViewText();
			} else {
				val = doc.getValueAsString(fieldName);
			}
			return val[0];
		} catch (Exception e) {
			ScriptProcessor.logger
					.errorLogEntry(e.getMessage() + ", function: getValueString(" + fieldName + "), returned:\"\"");
			return "";
		}
	}

	public Object getValueObject(String fieldName) throws _Exception {
		try {
			Object obj = doc.getValueAsObject(fieldName);
			if (obj instanceof EmployerCollection) {
				EmployerCollection emps = (EmployerCollection) obj;
				return new _EmployerCollection(session, emps);
			} else if (obj instanceof Control) {
				Control control = (Control) obj;
				return new _Control(session, control);
			} else if (obj instanceof ExecsBlock) {
				ExecsBlock execs = (ExecsBlock) obj;
				return new _ExecsBlocks(session, execs);
			} else if (obj instanceof CrossLink) {
				CrossLink o = (CrossLink) obj;
				return new _CrossLink(session, o);
			} else if (obj instanceof CrossLinkCollection) {
				CrossLinkCollection o = (CrossLinkCollection) obj;
				return new _CrossLinkCollection(session, o);
			} else if (obj instanceof BlockCollection) {
				BlockCollection o = (BlockCollection) obj;
				return new _BlockCollection(session, o);
			} else if (obj instanceof GrantedBlock) {
				GrantedBlock o = (GrantedBlock) obj;
				return new _GrantedBlock(session, o);
			} else if (obj instanceof GrantedBlockCollection) {
				GrantedBlockCollection o = (GrantedBlockCollection) obj;
				return new _GrantedBlockCollection(session, o);
			}
			return null;
		} catch (DocumentException e) {
			if (e.exceptionType == DocumentExceptionType.FIELD_NOT_FOUND) {
				ScriptProcessor.logger.errorLogEntry(
						e.getMessage() + ", function: _Document.getValueObject(" + fieldName + "), returned:null");
				return null;
			} else {
				throw new _Exception(_ExceptionType.COMPLEX_OBJECT_ERROR,
						e.getMessage() + ", function: _Document.getValueObject(" + fieldName + ")");
			}
		} catch (Exception e) {
			throw new _Exception(_ExceptionType.DOCUMENT_FIELD_CAUSED_ERROR,
					e.getMessage() + ", function: _Document.getValueObject(" + fieldName + ")");
		}
	}

	public String[] getValue(String fieldName) {
		try {
			String val[] = { "" };
			if (fieldName.equalsIgnoreCase("viewtext")) {
				val[0] = doc.getViewText();
			} else {
				val = doc.getValueAsString(fieldName);
			}
			return val;
		} catch (Exception e) {
			ScriptProcessor.logger.errorLogEntry(
					e.getMessage() + ", function: _Document.getValue(" + fieldName + "), returned:[\"\"]");
			String[] result = { "" };
			return result;
		}
	}

	public ArrayList<String> getValueList(String fieldName) {
		ArrayList<String> list = new ArrayList<String>();
		try {
			list.addAll(doc.getValueAsList(fieldName));
		} catch (Exception e) {
			ScriptProcessor.logger.errorLogEntry(
					"Groovy:" + e.getMessage() + ", function: getValueList(" + fieldName + "), returned:[\"\"]");
		}
		return list;
	}

	public void setValueNumber(String fieldName, int value) {
		try {
			doc.replaceIntField(fieldName, value);
		} catch (Exception e) {
			ScriptProcessor.logger
					.errorLogEntry(e.getMessage() + ", function: _Document.getValueNumber(" + fieldName + ")");
		}
	}

	public void setValueDate(String fieldName, Date value) {
		doc.replaceDateField(fieldName, value);
	}

	public void setRichText(String fieldName, String value) {
		doc.addField(fieldName, value, FieldType.RICHTEXT);
	}

	public String setValueString(String fieldName, String value) {
		try {
			doc.replaceStringField(fieldName, value);
			return doc.getValueAsString(fieldName)[0];
		} catch (Exception e) {
			ScriptProcessor.logger.errorLogEntry(e.getMessage() + ", function: _Document.setValueString(" + fieldName
					+ "," + value + "), returned:\"\"");
			return "";
		}
	}

	public void deleteField(String fieldName) {
		doc.deleteField(fieldName);
	}

	public void setLastUpdate(Date value) {
		doc.setLastUpdate(value);
	}

	@Deprecated
	public String getGlossaryValue(String condition, String fieldName) {
		AppEnv.logger
				.errorLogEntry("_Document.getGlossaryValue(" + condition + "," + fieldName + ") - obsolete method");
		return "";
	}

	public String toXML(String formRuleID, String lang)
			throws UserException, RuleException, QueryFormulaParserException, DocumentException,
			DocumentAccessException, QueryException, ComplexObjectException {
		AppEnv env = doc.getAppEnv();
		FormRule rule = (kz.flabs.webrule.form.FormRule) env.ruleProvider.getRule("edit", formRuleID);
		UserSession userSession = new UserSession(session.getUser());
		Form form = new Form(doc.getAppEnv(), rule, userSession);
		return form.getFormAsXML(doc, lang);
	}

	public String toXML() throws UserException, RuleException, QueryFormulaParserException, DocumentException,
			DocumentAccessException, QueryException, ComplexObjectException, _Exception {

		return doc.toXML(false);
	}

	public String toXMLWithDescendants(boolean allDescendants)
			throws UserException, RuleException, QueryFormulaParserException, DocumentException,
			DocumentAccessException, QueryException, ComplexObjectException {

		return doc.toXML(allDescendants);
	}

	public String getGlossaryValue(String form, String condition, String fieldName) {
		try {
			FormulaBlocks blocks = new FormulaBlocks(condition, QueryType.GLOSSARY);
			IQueryFormula queryFormula = new GlossaryQueryFormula("", blocks);
			IGlossaries glos = doc.db.getGlossaries();
			ArrayList<BaseDocument> docs = new ArrayList<BaseDocument>(glos.getGlossaryByCondition(queryFormula, 0, 1));
			Glossary doc = (Glossary) docs.get(0);
			return doc.getValueAsString(fieldName)[0];
		} catch (Exception e) {
			ScriptProcessor.logger.errorLogEntry("Groovy:" + e.getMessage() + ", function: _Document.getGlossaryValue("
					+ form + "," + condition + "," + fieldName + "), returned:\"\"");
			return "";
		}
	}

	public float getValueFloat(String fieldName) throws _Exception {
		try {
			Field field = doc.getFields().get(fieldName);
			if (field != null) {
				return field.valueAsNumber.floatValue();
			} else {
				throw new _Exception(_ExceptionType.DOCUMENT_FIELD_NOT_FOUND,
						"field not found, function: _Document.getValueNumber(" + fieldName + ")");
			}
		} catch (Exception e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR,
					e.getMessage() + " function: _Document.getValueNumber(" + fieldName + ")");
		}
	}

	public int getValueInt(String fieldName) throws _Exception {
		Field field = null;
		try {
			field = doc.getFields().get(fieldName);
			if (field != null) {
				return field.valueAsNumber.intValue();
			} else {
				return 0;
			}
		} catch (Exception e) {
			try {
				return Integer.parseInt(field.valueAsText);
			} catch (NumberFormatException e1) {
				return 0;
			}
		}
	}

	public int getValueNumber(String fieldName) throws _Exception {
		try {
			Field field = doc.getFields().get(fieldName);
			if (field != null) {
				return field.valueAsNumber.intValue();
			} else {
				throw new _Exception(_ExceptionType.DOCUMENT_FIELD_NOT_FOUND,
						"field not found, function: _Document.getValueNumber(" + fieldName + ")");
			}
		} catch (Exception e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR,
					e.getMessage() + " function: _Document.getValueNumber(" + fieldName + ")");
		}
	}

	public Date getValueDate(String fieldName) throws _Exception {
		try {
			Field field = doc.getFields().get(fieldName);
			if (field != null) {
				return field.valueAsDate;
			} else {
				throw new _Exception(_ExceptionType.DOCUMENT_FIELD_NOT_FOUND,
						"field not found, function: _Document.getValueDate(" + fieldName + ")");
			}
		} catch (Exception e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR,
					e.getMessage() + " function: _Document.getValueDate(" + fieldName + ")");
		}
	}

	public ArrayList<Integer> getValueGlossary(String fieldName) throws _Exception {
		try {
			Field field = doc.getFields().get(fieldName);
			if (field != null) {
				return field.valuesAsGlossaryData;
			} else {
				throw new _Exception(_ExceptionType.DOCUMENT_FIELD_NOT_FOUND,
						"field not found, function: _Document.getValueGlossary(" + fieldName + ")");
			}
		} catch (Exception e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR,
					e.getMessage() + " function: _Document.getValueGlossary(" + fieldName + ")");

		}
	}

	public HashSet<String> getEditors() {
		return doc.getEditors();
	}

	public HashSet<Reader> getReaders() {
		return doc.getReaders();
	}

	public String getFullURL() {
		try {
			return doc.getFullURL();
		} catch (Exception e) {
			ScriptProcessor.logger.errorLogEntry(e.getMessage() + ", function: _Document.getFullURL(), returned:\"\"");
			return "Error?type=incorrect_document_url";
		}
	}

	@Deprecated
	public String[] getForm() throws DocumentException {
		String val[] = { "" };
		val[0] = getDocumentForm();
		return val;
	}

	public String getDocumentForm() {
		return doc.form;
	}

	public String getURL() {
		try {
			return doc.getURL();
		} catch (Exception e) {
			ScriptProcessor.logger.errorLogEntry(e.getMessage() + ", function: _Document.getURL(), returned:\"\"");
			return "";
		}
	}

	public void addStringField(String fieldName, String value) throws _Exception {
		/*
		 * if (value.length() > 2046) { throw new
		 * _Exception(_ExceptionType.FORMDATA_INCORRECT,
		 * "Value too long (max=2046)", "_Document.addStringField(" + fieldName
		 * + ",value of " + value.length() + ")", session); }
		 */
		doc.addStringField(fieldName, value);
	}

	public void addField(String fieldName, Object value) throws _Exception {
		if (value instanceof _EmployerCollection) {
			EmployerCollection o = ((_EmployerCollection) value).getBaseObject();
			if (o instanceof IComplexObject) {
				doc.addComplexObjectField(fieldName, o);
			} else {
				throw new _Exception(_ExceptionType.FORMDATA_INCORRECT,
						"Object is not implementation of IComplexObject interface, function: _Document.addStringField("
								+ fieldName + "," + value + ")");
			}
		} else if (value instanceof _Control) {
			Control control = ((_Control) value).getBaseObject();
			if (control instanceof IComplexObject) {
				doc.addComplexObjectField(fieldName, control);
			} else {
				throw new _Exception(_ExceptionType.FORMDATA_INCORRECT,
						"Object is not implementation of IComplexObject interface, function: _Document.addStringField("
								+ fieldName + "," + value + ")");
			}
		} else if (value instanceof _ExecsBlocks) {
			ExecsBlock execs = ((_ExecsBlocks) value).getBaseObject();
			if (execs instanceof IComplexObject) {
				doc.addComplexObjectField(fieldName, execs);
			} else {
				throw new _Exception(_ExceptionType.FORMDATA_INCORRECT,
						"Object is not implementation of IComplexObject interface, function: _Document.addStringField("
								+ fieldName + "," + value + ")");
			}
		} else if (value instanceof _CrossLink) {
			CrossLink o = ((_CrossLink) value).getBaseObject();
			if (o instanceof IComplexObject) {
				doc.addComplexObjectField(fieldName, o);
			} else {
				throw new _Exception(_ExceptionType.FORMDATA_INCORRECT,
						"Object is not implementation of IComplexObject interface, function: _Document.addStringField("
								+ fieldName + "," + value + ")");
			}
		} else if (value instanceof _CrossLinkCollection) {
			CrossLinkCollection o = ((_CrossLinkCollection) value).getBaseObject();
			if (o instanceof IComplexObject) {
				doc.addComplexObjectField(fieldName, o);
			} else {
				throw new _Exception(_ExceptionType.FORMDATA_INCORRECT,
						"Object is not implementation of IComplexObject interface, function: _Document.addStringField("
								+ fieldName + "," + value + ")");
			}
		} else if (value instanceof _BlockCollection) {
			BlockCollection o = ((_BlockCollection) value).getBaseObject();
			if (o instanceof IComplexObject) {
				doc.addCoordinationField(fieldName, o);
			} else {
				throw new _Exception(_ExceptionType.FORMDATA_INCORRECT,
						"Object is not implementation of IComplexObject interface, function: _Document.addStringField("
								+ fieldName + "," + value + ")");
			}
		} else if (value instanceof _GrantedBlock) {
			GrantedBlock o = ((_GrantedBlock) value).getBaseObject();
			if (o instanceof IComplexObject) {
				doc.addComplexObjectField(fieldName, o);
			} else {
				throw new _Exception(_ExceptionType.FORMDATA_INCORRECT,
						"Object is not implementation of IComplexObject interface, function: _Document.addStringField("
								+ fieldName + "," + value + ")");
			}
		} else if (value instanceof _GrantedBlockCollection) {
			GrantedBlockCollection o = ((_GrantedBlockCollection) value).getBaseObject();
			if (o instanceof IComplexObject) {
				doc.addComplexObjectField(fieldName, o);
			} else {
				throw new _Exception(_ExceptionType.FORMDATA_INCORRECT,
						"Object is not implementation of IComplexObject interface, function: _Document.addStringField("
								+ fieldName + "," + value + ")");
			}
		} else {
			addStringField(fieldName, value.toString());
		}
	}

	@Deprecated
	public void addGlossaryField(String fieldName, int value) {
		ArrayList<Integer> glossData = new ArrayList<Integer>();
		glossData.add(value);
		doc.addGlossaryField(fieldName, glossData);
	}

	public <T extends Number> void addNumberField(String fieldName, T value) {
		doc.addNumberField(fieldName, value);
	}

	public void addNumberField(String fieldName, String value) throws _Exception {
		try {
			doc.addNumberField(fieldName, new BigDecimal(value.toString()));
		} catch (NumberFormatException e) {
			throw new _Exception(_ExceptionType.FORMDATA_INCORRECT,
					"Value has not converted to number format or has been empty, function: _Document.addNumberField("
							+ fieldName + "," + value + ")");
		}
	}

	public void addValueToList(String fieldName, String value) {
		doc.addValueToList(fieldName, value);
	}

	public void addFile(String fieldName, String fileName, String comment) throws _Exception {
		if (session != null) {
			String formSesID = session.getFormSesID();
			String tmpFolder = Environment.tmpDir + File.separator + formSesID + File.separator + fieldName
					+ File.separator;
			File dir = new File(tmpFolder);
			if (dir.exists() && dir.isDirectory()) {
				int folderNum = 1;
				File folder = new File(tmpFolder + Integer.toString(folderNum));
				while (folder.exists()) {
					File[] list = folder.listFiles();
					for (int i = list.length; --i >= 0;) {
						if (list[i].getName().equalsIgnoreCase(fileName)) {
							this.getBaseObject().addAttachment(fieldName, list[i], comment);
						}
					}
					folderNum++;
					folder = new File(tmpFolder + Integer.toString(folderNum));
				}
			}
		} else {
			throw new _Exception(_ExceptionType.DOCUMENT_FIELD_CAUSED_ERROR,
					"The attachment cannot be found, function: _Document.addFile(" + fieldName + "," + fileName + ","
							+ comment + ")");
		}
	}

	public void addFile(String fieldName, _WebFormData webFormData) throws _Exception {
		String filesToDelete[] = webFormData.getListOfValuesSilently("delete" + fieldName + "name");
		if (filesToDelete != null) {
			for (String fileName : filesToDelete) {
				if (!"".equalsIgnoreCase(fileName)) {
					BlobField targetBlob = doc.blobFieldsMap.get(fieldName);
					if (targetBlob == null) {
						continue;
					}
					if (!targetBlob.removeFile(fileName)) {
						throw new _Exception(_ExceptionType.DOCUMENT_FIELD_CAUSED_ERROR,
								"Attachment \"" + fileName + "\" has not been deleted from " + this.getDocType()
										+ this.getDocID() + ": _Document.addFile(" + fieldName + "," + webFormData
										+ ")");
					}
				}
			}
		}

		HashMap<String, BlobFile> uploadedFiles = new RuntimeObjUtil().getUploadedFiles(webFormData.getFormData());
		if (uploadedFiles.size() > 0) {
			if (doc.blobFieldsMap.containsKey(fieldName)) {
				BlobField existingBlob = doc.blobFieldsMap.get(fieldName);
				existingBlob.addFiles(uploadedFiles);
			} else {
				BlobField newBlob = new BlobField(fieldName);
				newBlob.addFiles(uploadedFiles);
				doc.blobFieldsMap.put(newBlob.name, newBlob);
			}
		}

	}

	/**
	 * @deprecated*
	 */
	@Deprecated
	public void addFile(String fieldName, HashMap<String, String[]> fields) {
		String filesToDelete[] = fields.get("delete" + fieldName + "name");
		String formSesID = fields.get("formsesid")[0];
		String tmpFolder = Environment.tmpDir + File.separator + formSesID + File.separator + fieldName
				+ File.separator;
		if (filesToDelete != null) {
			RuntimeObjUtil.checkUploadedFiles(tmpFolder, Arrays.asList(filesToDelete));
			for (String fileName : filesToDelete) {
				BlobField targetBlob = doc.blobFieldsMap.get(fieldName);
				if (targetBlob == null) {
					continue;
				}
				if (!targetBlob.removeFile(fileName)) {
					System.out.println("Attachment \"" + fileName + "\" has not been deleted from " + this.getDocType()
							+ this.getDocID());
				}

			}
		}

		HashMap<String, BlobFile> uploadedFiles = new RuntimeObjUtil().getUploadedFiles(fields);

		if (uploadedFiles.size() > 0) {
			if (doc.blobFieldsMap.containsKey(fieldName)) {
				BlobField existingBlob = doc.blobFieldsMap.get(fieldName);
				existingBlob.addFiles(uploadedFiles);
			} else {
				BlobField newBlob = new BlobField(fieldName);
				newBlob.addFiles(uploadedFiles);
				doc.blobFieldsMap.put(newBlob.name, newBlob);
			}
			doc.toDeleteAfterSave = Environment.tmpDir + File.separator + formSesID;
		}
	}

	public void addDateField(String fieldName, Date value) {
		doc.addDateField(fieldName, value);
	}

	public void addReader(String reader) {
		if (reader == null) {
			ScriptProcessor.logger.errorLogEntry("reader = null, function: _Document.addReader(String reader)");
		} else {
			if (reader.length() > 0) {
				doc.addReader(reader);
			}
		}
	}

	public void addReader(Reader reader) {
		doc.addReader(reader.getUserID());
	}

	public void addEditor(HashSet<String> editors) {
		for (String e : editors) {
			doc.addReader(e);
			doc.addEditor(e);
		}
	}

	public void addReader(HashSet<String> readers) {
		for (String r : readers) {
			doc.addReader(r);
		}
	}

	public void addReaders(HashSet<Reader> readers) {
		doc.addReaders(readers);
	}

	public void addReaders(ArrayList<String> readers) {
		doc.addReaders(readers);
	}

	public boolean addAttachment(String fieldName, File file) {
		return getBaseObject().addAttachment(fieldName, file, "");
	}

	public ArrayList<String> copyAttachments(_Document targetDoc) {
		return getBaseObject().copyAttachments(targetDoc.getBaseObject());
	}

	public void addEditor(String author) {
		if (author == null) {
			ScriptProcessor.logger.errorLogEntry("editor = null, function: _Document.addEditor(String author)");
		} else {
			if (author.length() > 0) {
				doc.addReader(author);
				doc.addEditor(author);
			}
		}
	}

	public void deleteReader(String reader) {
		doc.deleteReader(reader);
	}

	public void deleteAuthor(String author) {
		doc.deleteEditor(author);
	}

	public _Document getParentDocument() throws DocumentException, DocumentAccessException, _Exception {
		IDatabase db = doc.db;
		if (doc.parentDocID == 0 && doc.parentDocType == Const.DOCTYPE_UNKNOWN) {
			return null;
		}

		BaseDocument parentDoc = null;
		try {
			parentDoc = db.getDocumentByComplexID(doc.parentDocType, doc.parentDocID);
		} catch (ComplexObjectException e) {
			throw new _Exception(_ExceptionType.COMPLEX_OBJECT_ERROR, e.getMessage());
		}

		if (parentDoc == null) {
			return null;
		}
		switch (parentDoc.docType) {
		case Const.DOCTYPE_MAIN:
			return new _Document(parentDoc, session);
		case Const.DOCTYPE_PROJECT:
			return new _Project((Project) parentDoc, session);
		case Const.DOCTYPE_TASK:
			return new _Task((Task) parentDoc, session);
		case Const.DOCTYPE_EXECUTION:
			return new _Execution((Execution) parentDoc, session);
		case Const.DOCTYPE_GLOSSARY:
			return new _Glossary((Glossary) parentDoc, session);
		case Const.DOCTYPE_ORGANIZATION:
			return new _Organization((Organization) parentDoc, session);
		case Const.DOCTYPE_DEPARTMENT:
			return new _Department((Department) parentDoc, session);
		case Const.DOCTYPE_EMPLOYER:
			return new _Employer((Employer) parentDoc, session);
		default:
			return new _Document(parentDoc, session);
		}
	}

	public ArrayList<_Document> getAllParentDocuments() throws DocumentException, DocumentAccessException, _Exception {
		IDatabase db = doc.db;
		ArrayList<_Document> col = new ArrayList<_Document>();
		BaseDocument parentDoc;
		try {
			parentDoc = db.getDocumentByComplexID(doc.parentDocType, doc.parentDocID);

			while (parentDoc != null) {
				col.add(ScriptProcessorUtil.getScriptingDocument(parentDoc, currentUserID));
				parentDoc = db.getDocumentByComplexID(parentDoc.parentDocType, parentDoc.parentDocID);
			}
			return col;
		} catch (ComplexObjectException e) {
			throw new _Exception(_ExceptionType.COMPLEX_OBJECT_ERROR, e.getMessage());
		}
	}

	public _ViewEntryCollection getAncestry() throws DocumentException, DocumentAccessException {
		FormulaBlocks queryFormulaBlocks = new FormulaBlocks("maindocs.ddbid=\"" + doc.getDdbID() + "\"",
				QueryType.DOCUMENT);
		ISelectFormula sf = new SelectFormula(queryFormulaBlocks);
		RunTimeParameters parameters = new RunTimeParameters();
		return doc.db.getCollectionByCondition(sf, session.getUser(), 1, 0, session.getExpandedDocuments(), parameters,
				true);
	}

	public ArrayList<_Document> getParents(Document doc) throws DocumentException, DocumentAccessException, _Exception {
		try {
			IDatabase db = doc.db;
			ArrayList<_Document> col = new ArrayList<_Document>();
			Document parentDoc = (Document) db.getDocumentByComplexID(doc.parentDocType, doc.parentDocID);
			while (parentDoc != null) {
				col.add(ScriptProcessorUtil.getScriptingDocument(parentDoc, currentUserID));
				if (parentDoc.parentDocID != 0 && parentDoc.parentDocType != Const.DOCTYPE_UNKNOWN) {
					parentDoc = (Document) db.getDocumentByComplexID(parentDoc.parentDocType, parentDoc.parentDocID);
				} else {
					parentDoc = null;
				}
			}
			return col;
		} catch (ComplexObjectException e) {
			throw new _Exception(_ExceptionType.COMPLEX_OBJECT_ERROR, e.getMessage());
		}
	}

	public _Document getGrandParentDocument()
			throws DocumentException, DocumentAccessException, ComplexObjectException {
		IDatabase db = doc.db;
		BaseDocument grandParentDocument = new RuntimeObjUtil().getGrandParentDocument(db, doc);
		if (grandParentDocument == null) {
			return null;
		}
		switch (grandParentDocument.docType) {
		case Const.DOCTYPE_MAIN:
			return new _Document(grandParentDocument);
		case Const.DOCTYPE_TASK:
			return new _Task((Task) grandParentDocument);
		case Const.DOCTYPE_EXECUTION:
			return new _Execution((Execution) grandParentDocument);
		case Const.DOCTYPE_PROJECT:
			return new _Project((Project) grandParentDocument);
		case Const.DOCTYPE_ORGANIZATION:
			return new _Organization((Organization) grandParentDocument, session);
		case Const.DOCTYPE_GLOSSARY:
			return new _Glossary((Glossary) grandParentDocument, session);
		default:
			return new _Document(grandParentDocument);
		}
	}

	public LinkedHashSet<_Document> getResponses() throws DocumentAccessException, DocumentException, _Exception {
		try {
			IDatabase db = doc.db;
			LinkedHashSet<_Document> col = new LinkedHashSet<_Document>();
			Set<String> userGroups = new HashSet<String>();
			if (currentUserID != null) {
				if (currentUserID.equalsIgnoreCase("[observer]")) {
					userGroups = Const.sysGroupAsSet;
				} else {
					Employer user = db.getStructure().getAppUser(currentUserID);
					userGroups = user.getAllUserGroups();
				}
			}
			ArrayList<BaseDocument> documents = db.getResponses(doc.getDocID(), doc.docType, userGroups, currentUserID);
			for (BaseDocument doc : documents) {
				col.add(ScriptProcessorUtil.getScriptingDocument(doc, session));
			}
			return col;
		} catch (ComplexObjectException e) {
			throw new _Exception(_ExceptionType.COMPLEX_OBJECT_ERROR, e.getMessage());
		}
	}

	public ArrayList<_Document> getDescendants() throws DocumentAccessException, DocumentException, _Exception {
		try {
			IDatabase db = doc.db;
			ArrayList<_Document> col = new ArrayList<_Document>();
			Set<String> userGroups = new HashSet<String>();
			if (currentUserID != null) {
				if (currentUserID.equalsIgnoreCase("[" + Const.sysUser + "]")
						|| currentUserID.equalsIgnoreCase("server") || currentUserID.equalsIgnoreCase(Const.sysUser)
						|| currentUserID.equalsIgnoreCase("admin")) {
					userGroups = Const.supervisorGroupAsSet;
				} else {
					Employer user = db.getStructure().getAppUser(currentUserID);
					if (user != null) {
						userGroups = user.getAllUserGroups();
					}
				}
			}
			ArrayList<BaseDocument> documents = db.getResponses(doc.getDocID(), doc.docType, userGroups, currentUserID);
			for (BaseDocument doc : documents) {
				_Document sdoc = null;
				switch (doc.docType) {
				case Const.DOCTYPE_TASK:
					sdoc = new _Task((Task) doc, currentUserID);
					break;
				case Const.DOCTYPE_EXECUTION:
					sdoc = new _Execution((Execution) doc);
					break;
				default:
					sdoc = new _Document(doc);
				}
				col.add(sdoc);
				getResponsesRecursive(col, doc);
			}
			return col;
		} catch (ComplexObjectException e) {
			throw new _Exception(_ExceptionType.COMPLEX_OBJECT_ERROR, e.getMessage());
		}
	}

	private void getResponsesRecursive(ArrayList<_Document> col, BaseDocument doc)
			throws DocumentAccessException, DocumentException, _Exception {
		try {
			IDatabase db = doc.db;
			Set<String> userGroups = new HashSet<String>();
			if (currentUserID != null) {
				if (currentUserID.equalsIgnoreCase("[" + Const.sysUser + "]")
						|| currentUserID.equalsIgnoreCase("server") || currentUserID.equalsIgnoreCase(Const.sysUser)
						|| currentUserID.equalsIgnoreCase("admin")) {
					userGroups = Const.supervisorGroupAsSet;
				} else {
					Employer user = db.getStructure().getAppUser(currentUserID);
					userGroups = user.getAllUserGroups();
				}
			}
			ArrayList<BaseDocument> documents = db.getResponses(doc.getDocID(), doc.docType, userGroups, currentUserID);
			for (BaseDocument nestedDoc : documents) {
				_Document sdoc = null;
				switch (nestedDoc.docType) {
				case Const.DOCTYPE_TASK:
					sdoc = new _Task((Task) nestedDoc, currentUserID);
					break;
				case Const.DOCTYPE_EXECUTION:
					sdoc = new _Execution((Execution) nestedDoc);
					break;
				default:
					sdoc = new _Document(nestedDoc);
				}
				col.add(sdoc);
				getResponsesRecursive(col, nestedDoc);
			}
		} catch (ComplexObjectException e) {
			throw new _Exception(_ExceptionType.COMPLEX_OBJECT_ERROR, e.getMessage());
		}
	}

	public boolean hasAttachment() {
		return doc.hasAttachment();
	}

	public void getAttachments(String fieldName, String toPutFolder) {
		doc.getAttachments(fieldName, toPutFolder);
	}

	public void getAttachments(String fieldName, String toPutFolder, ArrayList<String> ids) {
		doc.getAttachments(fieldName, toPutFolder, ids);
	}

	public boolean save() throws ComplexObjectException {
		try {
			User user = new User(currentUserID);
			int result = doc.save(user);
			if (result > -1) {
				return true;
			} else {
				return false;
			}
		} catch (DocumentAccessException e) {
			ScriptProcessor.logger.errorLogEntry(
					"DocumentAccessException " + e.getMessage() + ", function: _Document.save(), returned:false");
			return false;
		} catch (DocumentException e) {
			ScriptProcessor.logger.errorLogEntry(
					"DocumentException " + e.getMessage() + ", function: _Document.save(), returned:false");
			return false;
		} catch (LicenseException e) {
			ScriptProcessor.logger.errorLogEntry(
					"LicenseException " + e.getMessage() + ", function: _Document.save(), returned:false");
			return false;
		}
	}

	@Deprecated
	public String getDefaultRuleID() {
		return this.doc.getDefaultRuleID();
	}

	@Deprecated
	public void setDefaultRuleID(String ruleID) {
		this.doc.setDefaultRuleID(ruleID);
	}

	public boolean save(String absoluteUserID) throws LicenseException, ComplexObjectException {
		try {
			User user = new User(absoluteUserID);
			int result = doc.save(user);
			if (result > -1) {
				return true;
			} else {
				return false;
			}
		} catch (DocumentAccessException e) {
			ScriptProcessor.logger.errorLogEntry(e);
		} catch (DocumentException e) {
			ScriptProcessor.logger.errorLogEntry(e);
		}
		return false;
	}

	BaseDocument getBaseObject() {
		return doc;
	}

	public void setNewDoc() {
		isNewDoc = true;
		doc.setNewDoc(true);
	}

	public void setForm(String form) {
		doc.setForm(form);
	}

	public void setViewText(String viewtext) {
		doc.setViewText(viewtext);
	}

	public boolean setViewText(Integer vt, int index) {
		return doc.replaceViewText(vt.toString(), index);
	}

	public boolean setViewText(String vt, int index) {
		return doc.replaceViewText(vt, index);
	}

	public void setViewText(Enum viewtext) {
		doc.setViewText(viewtext.name());
	}

	public void addViewText(String vt) {
		doc.addViewText(vt);
	}

	public void addViewText(int vt) {
		doc.addViewText(Integer.toString(vt));
	}

	public void addViewText(Enum vt) {
		doc.addViewText(vt.name());
	}

	public boolean replaceViewText(String vt, int index) {
		return doc.replaceViewText(vt, index);
	}

	public void setViewDate(Date vd) {
		doc.setViewDate(vd);
	}

	public void setViewNumber(BigDecimal vn) {
		doc.setViewNumber(vn);
	}

	public void clearReaders() {
		doc.clearReaders();
	}

	public void clearEditors() {
		doc.clearEditors();
	}

	public void replaceStringField(String name, String value) {
		doc.replaceStringField(name, value);
	}

	public void replaceIntField(String name, int value) {
		doc.replaceIntField(name, value);
	}

	public void replaceDateField(String name, Date value) {
		doc.replaceDateField(name, value);
	}

	public void replaceListField(String name, List<String> values) {
		doc.replaceListField(name, (ArrayList<String>) values);
	}

	@Deprecated
	public void setParentDocType(int pdoctype) {
		doc.parentDocType = pdoctype;
	}

	@Deprecated
	public void setParentDocID(int pdocid) {
		doc.parentDocID = pdocid;
		if (doc.parentDocType == Const.DOCTYPE_UNKNOWN && doc.parentDocID > 0) {
			doc.parentDocType = Const.DOCTYPE_MAIN;
		}
	}

	public String getSign() {
		return doc.getSign();
	}

	public String getSignedFields() {
		return doc.getSignedFields();
	}

	public void setSign(String sign) {
		doc.setSign(sign);
	}

	public void setSignedFields(String signedFields) {
		doc.setSignedFields(signedFields);
	}

	public void setParentDoc(_Document pdoc) {
		int parentDocID = pdoc.getDocID();
		if (parentDocID > 0) {
			doc.parentDocID = parentDocID;
			doc.setParentDocumentID(pdoc.getID());
			int docType = pdoc.getDocType();
			if (docType == Const.DOCTYPE_UNKNOWN) {
				doc.parentDocType = Const.DOCTYPE_MAIN;
			} else {
				doc.parentDocType = docType;
			}
		}
	}

	@Deprecated
	public int getParentDocType() {
		return doc.parentDocType;
	}

	@Deprecated
	public int getParentDocID() {
		return doc.parentDocID;
	}

	public String getParentDocumentID() {
		return doc.getParentDocID();
	}

	public void makeResponse(_Document pdoc) {
		doc.setParentDocumentID(pdoc.getID());
		doc.parentDocType = pdoc.doc.docType;
		doc.parentDocID = pdoc.doc.getDocID();
	}

	/*
	 * public String toString(){ return doc.toString(); }
	 */

}
