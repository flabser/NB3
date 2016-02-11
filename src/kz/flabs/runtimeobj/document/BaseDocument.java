package kz.flabs.runtimeobj.document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IStructure;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.DocumentExceptionType;
import kz.flabs.exception.LicenseException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.runtimeobj.DocumentCollection;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.users.Reader;
import kz.flabs.users.User;
import kz.flabs.util.ListConvertor;
import kz.flabs.util.Util;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.FieldType;
import kz.flabs.webrule.form.ISaveField;
import kz.flabs.webrule.form.SaveFieldRule;
import kz.lof.env.Environment;
import kz.nextbase.script._Exception;
import kz.nextbase.script._Helper;

import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@XmlAccessorType(XmlAccessType.NONE)
public class BaseDocument extends AbstractComplexObject implements Const, Serializable {

	private static final long serialVersionUID = 1L;
	public int docType;
	@Deprecated
	public int parentDocID;
	@Deprecated
	public int parentDocType = Const.DOCTYPE_UNKNOWN;
	public int editMode = EDITMODE_NOACCESS;
	public boolean isValid;
	public String toDeleteAfterSave;
	@Deprecated
	public String viewIcon;
	public String form;
	@Deprecated
	public String defaultRuleID;
	public String dbID;
	public HashMap<String, BlobField> blobFieldsMap = new HashMap<String, BlobField>();
	public ArrayList<BaseDocument> responses = new ArrayList<BaseDocument>();
	public DocumentCollection descendants = new DocumentCollection();
	public transient IDatabase db;
	public transient AppEnv env;
	public transient IStructure struct;
	public ArrayList<BigDecimal> viewNumberList = new ArrayList<BigDecimal>();
	public ArrayList<Date> viewDateList = new ArrayList<Date>();
	public boolean hasDiscussion = false;

	protected int docID;
	public String ddbID = "";
	protected Date lastUpdate;
	protected HashMap<String, Field> fieldsMap = new HashMap<String, Field>();
	protected HashSet<Reader> readers = new HashSet<Reader>();
	protected HashSet<String> editors = new HashSet<String>();
	protected String authorID = "undefined";
	protected String currentUserID;
	protected String sign;
	protected String signedFields;

	protected ArrayList<String> viewTextList = new ArrayList<String>();
	private String parentDocumentID = "";
	private boolean isNewDoc;
	private Date regDate;

	public void getAttachments(String fieldName, String toPutFolder) {
		if (!new File(toPutFolder).exists()) {
			new File(toPutFolder).mkdirs();
		}

		int folderIdx = 0;
		Connection conn = db.getConnectionPool().getConnection();

		try {
			Statement attachStatement = conn.createStatement();
			ResultSet attachResultSet = attachStatement.executeQuery("select * from CUSTOM_BLOBS_MAINDOCS " + " where CUSTOM_BLOBS_MAINDOCS.DOCID = "
			        + docID + " AND CUSTOM_BLOBS_MAINDOCS.NAME = '" + fieldName + "' ");
			while (attachResultSet.next()) {
				if (attachResultSet.getLong("VALUE_OID") == 0) {
					continue;
				}

				folderIdx++;
				if (!new File(toPutFolder + File.separator + folderIdx).exists()) {
					new File(toPutFolder + File.separator + folderIdx).mkdirs();
				}

				LargeObjectManager lobj = ((org.postgresql.PGConnection) ((DelegatingConnection) conn).getInnermostDelegate()).getLargeObjectAPI();
				// LargeObjectManager lobj = ((org.postgresql.PGConnection)
				// ((DelegatingConnection)
				// conn).getInnermostDelegate()).getLargeObjectAPI();
				long oid = attachResultSet.getLong("VALUE_OID");
				LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);
				InputStream is = obj.getInputStream();

				FileOutputStream out = new FileOutputStream(toPutFolder + File.separator + folderIdx + File.separator
				        + attachResultSet.getString("ORIGINALNAME"));
				byte[] b = new byte[1048576];
				int len = 0;
				while ((len = is.read(b)) > 0) {
					out.write(b, 0, len);
				}
				out.close();
			}
		} catch (Exception e) {
			DatabaseUtil.errorPrint(dbID, e);
		}
		db.getConnectionPool().returnConnection(conn);
	}

	public void getAttachments(String fieldName, String toPutFolder, ArrayList<String> attachIDS) {
		if (attachIDS.isEmpty()) {
			return;
		}
		if (!new File(toPutFolder).exists()) {
			new File(toPutFolder).mkdirs();
		}

		int folderIdx = 0;
		IDBConnectionPool pool;
		String tableName = "";
		switch (this.docType) {

		default:
			tableName = "MAINDOCS";
			pool = db.getConnectionPool();
			break;
		}
		Connection conn = pool.getConnection();
		try {
			Statement attachStatement = conn.createStatement();
			ResultSet attachResultSet = attachStatement.executeQuery("select * from CUSTOM_BLOBS_" + tableName + " where CUSTOM_BLOBS_" + tableName
			        + ".ID IN (" + StringUtils.join(attachIDS, ",") + ") " + " AND CUSTOM_BLOBS_" + tableName + ".NAME = '" + fieldName + "' ");
			while (attachResultSet.next()) {
				if (attachResultSet.getLong("VALUE_OID") == 0) {
					continue;
				}

				folderIdx++;
				if (!new File(toPutFolder + File.separator + folderIdx).exists()) {
					new File(toPutFolder + File.separator + folderIdx).mkdirs();
				}

				LargeObjectManager lobj = ((org.postgresql.PGConnection) ((DelegatingConnection) conn).getInnermostDelegate()).getLargeObjectAPI();
				long oid = attachResultSet.getLong("VALUE_OID");
				LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);
				InputStream is = obj.getInputStream();

				FileOutputStream out = new FileOutputStream(toPutFolder + File.separator + folderIdx + File.separator
				        + attachResultSet.getString("ORIGINALNAME"));
				byte[] b = new byte[1048576];
				int len = 0;
				while ((len = is.read(b)) > 0) {
					out.write(b, 0, len);
				}
				out.close();
			}
		} catch (Exception e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			pool.returnConnection(conn);
		}
	}

	public void setCurrentUserID(String currentID) {
		this.currentUserID = currentID;
	}

	public void setStructure(IStructure struct) {
		this.struct = struct;
	}

	public String getCurrentUserID() {
		return this.currentUserID;
	}

	public String getSign() {
		return this.sign;
	}

	public String getSignedFields() {
		return this.signedFields;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public void setSignedFields(String signedFields) {
		this.signedFields = signedFields;
	}

	public ArrayList<String> getViewTextList() {
		return viewTextList;
	}

	public BigDecimal getViewNumber() {
		try {
			return getViewNumberList().get(0).stripTrailingZeros();
		} catch (Exception e) {
			return BigDecimal.valueOf(-1);
		}
	}

	public ArrayList<BigDecimal> getViewNumberList() {
		return this.viewNumberList;
	}

	public Date getViewDate() {
		try {
			return getViewDateList().get(0);
		} catch (Exception e) {
			return null;
		}
	}

	public ArrayList<Date> getViewDateList() {
		return this.viewDateList;
	}

	public boolean hasField(String fieldName) {
		return fieldsMap.containsKey(fieldName);
	}

	public void setNewDoc(boolean isNewDoc) {
		this.isNewDoc = isNewDoc;
	}

	public boolean isNewDoc() {
		return isNewDoc;
	}

	public int isFavourite() {
		return docID;

	}

	public Date getRegDate() {
		return regDate;
	}

	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}

	public Collection<Field> fields() {
		return fieldsMap.values();
	}

	public HashMap<String, Field> getFieldsMap() {
		return fieldsMap;
	}

	public void setDocID(int docID) {
		this.docID = docID;
		setNewDoc(false);
	}

	public int getDocID() {
		return docID;
	}

	public String getComplexID() {
		return Integer.toString(docType) + Integer.toString(docID);
	}

	public void addField(Field field) {
		if (field.getType() == FieldType.VIEWTEXT) {
			this.setViewText(field.valueAsText);
		} else {
			fieldsMap.put(field.name, field);
			if (field.name.equalsIgnoreCase("form")) {
				form = field.valueAsText;
			} else if (field.name.equalsIgnoreCase("sign")) {
				setSign(field.valueAsText);
			} else if (field.name.equalsIgnoreCase("signedfields")) {
				setSignedFields(field.valueAsText);
			}
		}
	}

	public Field addField(String name, String value, FieldType type) {
		Field field = new Field(name, value, type);
		fieldsMap.put(field.name, field);
		return field;
	}

	public Field addField(String name, int value) {
		Field field = new Field(name, value);
		fieldsMap.put(field.name, field);
		return field;
	}

	public Field addField(String name, String value, int type) {
		FieldType fieldType = FieldType.UNKNOWN;

		switch (type) {
		case TEXT:
			fieldType = FieldType.TEXT;
			break;
		case DATETIMES:
			fieldType = FieldType.DATETIME;
			break;
		case NUMBERS:
			fieldType = FieldType.NUMBER;
			break;
		case AUTHORS:
			fieldType = FieldType.AUTHOR;
			break;
		case TEXTLIST:
			fieldType = FieldType.TEXTLIST;
			break;
		case READERS:
			fieldType = FieldType.READER;
			break;
		case FILES:
			fieldType = FieldType.FILE;
			break;
		case GLOSSARY:
			fieldType = FieldType.GLOSSARY;
			break;
		case RICHTEXT:
			fieldType = FieldType.RICHTEXT;
			break;
		}

		Field field = new Field(name, value, fieldType);
		fieldsMap.put(field.name, field);
		return field;
	}

	public void removeField(String name) {
		fieldsMap.remove(name);
	}

	public void fillBlobs(HashMap<String, BlobField> blobs) {
		/*
		 * remove entries from existing fieldsMap that have not been received
		 * from blobFieldsMap first blobFieldsMap here is like a table (blobs)
		 * there and blobs here is like a blobFieldsMap there
		 */
		blobs_loop: for (Entry<String, BlobField> blob : this.blobFieldsMap.entrySet()) {
			files_loop: for (BlobFile file : blob.getValue().getFiles()) {
				if (!blobs.containsKey(blob.getKey())) {
					// this.blobFieldsMap.remove(blob.getKey());
					continue blobs_loop;
				}
				BlobField existingBlob = blobs.get(blob.getKey());
				if (existingBlob.findFile(file) == null) {
					blob.getValue().removeFile(file);
					continue files_loop;
				}
				BlobFile existingFile = existingBlob.findFile(file);
				file.path = existingFile.path;
				file.checkHash = existingFile.checkHash;
				file.originalName = existingFile.originalName;
				file.comment = existingFile.comment;
			}

		}
		/* now add files that are absent in blobFieldsMap */
		blobs_loop: for (Entry<String, BlobField> blob : blobs.entrySet()) {
			// files_loop:
			for (BlobFile file : blob.getValue().getFiles()) {
				if (!blobFieldsMap.containsKey(blob.getKey())) {
					blobFieldsMap.put(blob.getKey(), (BlobField) blob.getValue().clone());
					continue blobs_loop;
				}
				BlobField existingBlob = blobFieldsMap.get(blob.getKey());
				existingBlob.addFile(file);
				/*
				 * for (BlobFile file2:
				 * blobFieldsMap.get(blob.getKey()).getFiles()) { if
				 * (file2.equals(file)) { if
				 * (!file2.originalName.equals(file.originalName)) {
				 * file2.originalName = file.originalName; continue files_loop;
				 * } } } if (!blobFieldsMap.get(blob.getKey()).hasFileName(file.
				 * originalName)) {
				 * blobFieldsMap.get(blob.getKey()).addFile(file); }
				 */
			}
		}
	}

	public void replaceStringField(String name, String value) {
		Field field = null;
		if (name != null) {
			if (fieldsMap.containsKey(name)) {
				field = fieldsMap.get(name);
				field.valueAsText = value;
				field.setType(FieldType.TEXT);
			} else {
				addStringField(name, value);
			}
		}
	}

	public void replaceListField(String name, ArrayList<String> value) {
		Field field = null;
		if (name != null) {
			if (fieldsMap.containsKey(name)) {
				field = fieldsMap.get(name);
				field.valuesAsStringList.clear();
				field.valuesAsStringList.addAll(value);
				field.valueAsText = ListConvertor.listToString(field.valuesAsStringList);
				field.setType(FieldType.LIST);
			} else {
				addListField(name, value);
			}
		}
	}

	public <T extends Number> void replaceIntField(String name, T value) {
		Field field = null;
		if (name != null) {
			if (fieldsMap.containsKey(name)) {
				field = fieldsMap.get(name);
				field.valueAsText = value.toString();
				field.valueAsNumber = new BigDecimal(value.toString());
				field.setType(FieldType.NUMBER);
			} else {
				addNumberField(name, value);
			}
		}
	}

	public void replaceDateField(String name, Date value) {
		Field field = null;
		if (name != null) {
			if (fieldsMap.containsKey(name)) {
				field = fieldsMap.get(name);
				field.valueAsText = Util.convertDataTimeToString(value);
				field.valueAsDate = value;
				field.setType(FieldType.DATETIME);
			} else {
				addDateField(name, value);
			}
		}
	}

	public void replaceGlossaryField(String name, ArrayList<Integer> value) {
		Field field = null;
		if (name != null) {
			if (fieldsMap.containsKey(name)) {
				field = fieldsMap.get(name);
				field.valuesAsGlossaryData = value;
				field.setType(FieldType.GLOSSARY);
			} else {
				addGlossaryField(name, value);
			}
		}
	}

	public void addStringField(String name, String value) {
		if (name != null) {
			Field field = new Field(name, value);
			fieldsMap.put(field.name, field);
		}
	}

	public void addComplexObjectField(String name, IComplexObject obj) {
		if (name != null) {
			Field field = new Field(name, obj);
			fieldsMap.put(field.name, field);
		}
	}

	public void addCoordinationField(String name, IComplexObject obj) {
		if (name != null) {
			Field field = new Field(name, obj);
			field.setType(FieldType.COORDINATION);
			fieldsMap.put(field.name, field);
		}
	}

	public <T extends Number> void addNumberField(String name, T value) {
		if (name != null) {
			Field field = new Field(name, value);
			fieldsMap.put(field.name, field);
		}
	}

	public void addGlossaryField(String fieldName, int value) {
		if (fieldName != null) {
			Field field = new Field(fieldName, value, FieldType.GLOSSARY);
			fieldsMap.put(field.name, field);
		}
	}

	public void addGlossaryField(String name, ArrayList<Integer> value) {
		if (name != null) {
			Field field = new Field(name, value, true);
			fieldsMap.put(field.name, field);
		}
	}

	public void addBoolField(String name, Enum value) {
		Field field = new Field(name, value);
		fieldsMap.put(field.name, field);
	}

	public void addDateField(String name, Date value) {
		Field field = new Field(name, value);
		if (field.getType() == FieldType.DATETIME) {
			fieldsMap.put(field.name, field);
		}
	}

	public void addListField(String name, ArrayList<String> value) {
		Field field = new Field(name, value);
		fieldsMap.put(field.name, field);
	}

	public void addValueToList(String name, String value) {
		Field field = fieldsMap.get(name);
		if (field == null) {
			field = new Field(name);
			fieldsMap.put(name, field);
		}
		field.addValue(value);
	}

	public void addBlobField(String name, HashMap<String, BlobFile> files) {
		if (blobFieldsMap.containsKey(name)) {
			BlobField currentBlobField = blobFieldsMap.get(name);
			for (BlobFile file : files.values()) {
				currentBlobField.addFile(file);
			}
		} else {
			BlobField newBlobField = new BlobField(name, files);
			blobFieldsMap.put(newBlobField.name, newBlobField);
		}
	}

	public String[] getValueAsString(String fieldName) throws DocumentException {

		Field field = fieldsMap.get(fieldName);
		if (field != null) {
			switch (field.getType()) {
			case TEXTLIST:
			case VECTOR:
				int ms = field.valuesAsStringList.size();
				if (ms > 0) {
					String[] res = field.valuesAsStringList.toArray(new String[ms]);
					return res;
				} else {
					String res[] = { field.valueAsText };
					return res;
				}
			default:
				String res[] = { field.valueAsText };
				return res;
			}
		} else {
			BlobField blobField = blobFieldsMap.get(fieldName);
			if (blobField != null) {
				int ms = blobField.getFilesCount();

				String[] res = blobField.getFileNames().toArray(new String[ms]);
				return res;
			} else {
				throw new DocumentException(DocumentExceptionType.FIELD_NOT_FOUND, fieldName);
			}
		}

	}

	public String getValue(String fieldName) throws DocumentException {

		Field field = fieldsMap.get(fieldName);
		if (field != null) {
			String res[] = { field.valueAsText };
			return res[0];
		} else {
			BlobField blobField = blobFieldsMap.get(fieldName);
			if (blobField != null) {
				int ms = blobField.getFilesCount();

				String[] res = blobField.getFileNames().toArray(new String[ms]);
				return res[0];
			} else {
				return "";
			}
		}
	}

	public Collection<String> getValueAsList(String fieldName) throws DocumentException {
		Field field = fieldsMap.get(fieldName);
		if (field != null) {
			return ListConvertor.stringToList(field.valueAsText);
			// return field.valuesAsStringList;
		} else {
			return new HashSet<String>();
		}
	}

	public Object getValueAsObject(String fieldName) throws DocumentException {
		Field field = getFields().get(fieldName);
		if (field != null) {
			if (field.getType() == FieldType.COMPLEX_OBJECT || field.getType() == FieldType.COORDINATION) {
				return field.valueAsObject;
			} else {
				throw new DocumentException(DocumentExceptionType.COMPLEX_OBJECT_INCORRECT, fieldName);
			}
		} else {
			throw new DocumentException(DocumentExceptionType.FIELD_NOT_FOUND, fieldName);
		}
	}

	public int getValueAsInteger(String fieldName) throws DocumentException {
		Field field = getFields().get(fieldName);
		if (field != null) {
			if (field.getType() == FieldType.NUMBER) {
				return field.valueAsNumber.intValue();
			} else {
				throw new DocumentException(DocumentExceptionType.NUMBER_VALUE_INCORRECT, fieldName);
			}
		} else {
			throw new DocumentException(DocumentExceptionType.FIELD_NOT_FOUND, fieldName);
		}

	}

	public Date getValueAsDate(String fieldName) throws DocumentException {
		try {
			Field field = getFields().get(fieldName);
			return field.valueAsDate;
		} catch (Exception e) {
			AppEnv.logger.errorLogEntry("Cannot get value as Date: (" + fieldName + ")");
			throw new DocumentException(DocumentExceptionType.CANNOT_GET_VALUE_AS_DATA, fieldName);
		}
	}

	public void setAuthor(String author) {
		if (author != null) {
			this.authorID = author;
			readers.add(new Reader(author));
			// addStringField("author", author);
			// if (author.equalsIgnoreCase(Const.anonUser)) {
			// this.addReaders(Const.anonGroupAsList);
			// }

		}
	}

	public void setLastUpdate(Date lu) {
		this.lastUpdate = lu;
	}

	public Date getLastUpdate() {
		if (lastUpdate == null) {
			lastUpdate = Util.convertStringToDate("2000.01.01");
		}
		return lastUpdate;
	}

	public String getAuthorID() {
		return authorID;
	}

	public void setForm(String f) {
		form = f;
	}

	public void clearViewText() {
		viewTextList.clear();
	}

	public void setViewText(String vt) {
		clearViewText();
		addViewText(vt);
	}

	public String getViewText() {
		try {
			String vt = viewTextList.get(0);
			// return vt.replace("\"","'");
			return vt;
		} catch (IndexOutOfBoundsException e) {
			return "nil";
		}
	}

	public boolean replaceViewText(String vt, int index) {
		if (viewTextList != null && viewTextList.size() > index) {
			viewTextList.remove(index);
			if (index >= 1) {
				viewTextList.add(index, RuntimeObjUtil.cutHTMLText(vt, 256));
			} else {
				viewTextList.add(index, RuntimeObjUtil.cutHTMLText(vt, 2048));
			}
			return true;
		}
		return false;
	}

	public void addViewText(String vt) {
		int empty_index = viewTextList.indexOf("-");
		if (viewTextList.size() >= 1) {
			if (empty_index > -1) {
				viewTextList.remove(empty_index);
				viewTextList.add(empty_index, RuntimeObjUtil.cutHTMLText(vt, 256));
			} else {
				viewTextList.add(RuntimeObjUtil.cutHTMLText(vt, 256));
			}
		} else {
			viewTextList.add(RuntimeObjUtil.cutHTMLText(vt, 2048));
		}
	}

	public void setViewDate(Date vd) {
		viewDateList.clear();
		viewDateList.add(vd);
	}

	public void setViewNumber(BigDecimal vn) {
		if (vn != null) {
			viewNumberList.clear();
			viewNumberList.add(vn.stripTrailingZeros());
		}
	}

	public String getViewIcon() throws DocumentException {
		return viewIcon;
	}

	public void setAccessRelatedFields(kz.flabs.runtimeobj.document.BaseDocument doc, HashMap<String, SaveFieldRule> saveFieldsMap,
	        HashMap<String, String[]> fields) throws WebFormValueException {
		for (SaveFieldRule saveField : saveFieldsMap.values()) {
			Field val = null;
			switch (saveField.valueSourceType) {
			case STATIC:
				val = getStaticContent(saveField);
				break;
			case QUERY:
				break;
			case SCRIPT:
				val = getDoScriptResult(saveField);
				break;
			case WEBFORMFIELD:
				val = getWebFormValue(saveField, fields);
				break;
			case MACRO:
				switch (saveField.macro) {
				case AUTHOR:
					val = new Field(saveField.documentField, doc.authorID, saveField.type);
					break;
				}

			}
			if (saveField.type == FieldType.AUTHOR) {
				if (val.getType() == FieldType.TEXTLIST) {
					addEditors(val.valuesAsStringList);
					addReaders(val.valuesAsStringList);
				} else {
					addEditor(val.valueAsText);
					addReader(val.valueAsText);
				}
			} else if (saveField.type == FieldType.READER) {
				if (val.getType() == FieldType.TEXTLIST) {
					addReaders(val.valuesAsStringList);
				} else {
					addReader(val.valueAsText);
				}
			}
		}
	}

	public void fillFieldsToSave(HashMap<String, ISaveField> saveFieldsMap, HashMap<String, String[]> fields) throws WebFormValueException {
		for (ISaveField saveField : saveFieldsMap.values()) {
			switch (saveField.getSourceType()) {
			case STATIC:
				addField(getStaticContent(saveField));
				break;
			case QUERY:

				break;
			case SCRIPT:
				addField(getDoScriptResult(saveField));
				break;
			case WEBFORMFIELD:
				addField(getWebFormValue(saveField, fields));
				break;
			case WEBFORMFILE:
				blobDataProcess(saveField, fields);
				String filesToDelete[] = fields.get("delete" + saveField.getName() + "name");
				if (filesToDelete != null) {
					for (String fileName : filesToDelete) {
						try {
							String originalAttachName1 = new String(fileName.getBytes("ISO-8859-1"), "windows-1251");
							String attachmentName = new String(originalAttachName1.getBytes("cp1251"), "UTF-8");
							BlobField targetBlob = this.blobFieldsMap.get(saveField.getName());
							if (targetBlob == null) {
								continue;
							}
							targetBlob.removeFile(attachmentName);
						} catch (UnsupportedEncodingException ue) {

							continue;
						}
					}
				}
				String formSesID = fields.get("formsesid")[0];
				String tmpFolder = Environment.tmpDir + File.separator + formSesID + File.separator + saveField.getName() + File.separator;
				HashMap<String, BlobFile> uploadedFiles = new RuntimeObjUtil().getUploadedFiles(fields);

				if (uploadedFiles.size() == 0) {
					continue;
				}
				if (blobFieldsMap.containsKey(saveField.getName())) {
					BlobField existingBlob = blobFieldsMap.get(saveField.getName());
					existingBlob.addFiles(uploadedFiles);

				} else {
					BlobField newBlob = new BlobField(saveField.getName());
					newBlob.addFiles(uploadedFiles);
					blobFieldsMap.put(newBlob.name, newBlob);
					// replaceStringField("", (String[])fields.get(newBlob.));
				}

				// String commentField =
				// ((String[])fields.get(SiteFileUtil.getCommentFieldName(originalName,
				// fullName, size)))[0];
				toDeleteAfterSave = Environment.tmpDir + File.separator + formSesID;
				break;
			case MACRO:
			}
		}
	}

	protected void blobDataProcess(ISaveField saveField, HashMap<String, String[]> fields) {
		String filesToDelete[] = fields.get("delete" + saveField.getName() + "name");
		String formSesID = fields.get("formsesid")[0];
		String tmpFolder = Environment.tmpDir + File.separator + formSesID + File.separator + saveField.getName() + File.separator;
		if (filesToDelete != null) {
			RuntimeObjUtil.checkUploadedFiles(tmpFolder, Arrays.asList(filesToDelete));
			for (String fileName : filesToDelete) {
				BlobField targetBlob = this.blobFieldsMap.get(saveField.getName());
				if (targetBlob == null) {
					continue;
				}
				if (!targetBlob.removeFile(fileName)) {
					AppEnv.logger.warningLogEntry("Attachment \"" + fileName + "\" has not been deleted from " + getComplexID());
				}
			}
		}

		HashMap<String, BlobFile> uploadedFiles = new RuntimeObjUtil().getUploadedFiles(fields);

		for (BlobField blob : blobFieldsMap.values()) {
			for (BlobFile file : blob.getFiles()) {
				String parName = "comment" + file.checkHash;
				String parValue[] = fields.get(parName);
				if (parValue != null && parValue.length > 0) {
					file.comment = parValue[0];
				}
			}
		}

		if (uploadedFiles.size() == 0) {
			return;
		}
		if (blobFieldsMap.containsKey(saveField.getName())) {
			BlobField existingBlob = blobFieldsMap.get(saveField.getName());
			existingBlob.addFiles(uploadedFiles);

		} else {
			BlobField newBlob = new BlobField(saveField.getName());
			newBlob.addFiles(uploadedFiles);
			blobFieldsMap.put(newBlob.name, newBlob);
		}
		toDeleteAfterSave = Environment.tmpDir + File.separator + formSesID;
	}

	protected Field getValueForDoc(Map<String, ISaveField> saveFieldsMap, String ruleID, Map<String, String[]> fields) throws WebFormValueException {
		ISaveField saveField = saveFieldsMap.get(ruleID);
		Field field = null;
		if (saveField != null) {
			field = getSaveFieldValue(saveField, fields);
		} else {
			AppEnv.logger.warningLogEntry("Did not found a description of the \"" + ruleID + "\" field, during saving");
			field = new Field("error", "error", FieldType.UNKNOWN);
		}
		return field;
	}

	protected Field getSaveFieldValue(ISaveField saveField, Map<String, String[]> fields) throws WebFormValueException {
		switch (saveField.getSourceType()) {
		case STATIC:
			return getStaticContent(saveField);
		case SCRIPT:
			return getDoScriptResult(saveField);
		case WEBFORMFIELD:
			return getWebFormValue(saveField, fields);
		case MACRO:
			switch (saveField.getMacro()) {
			case AUTHOR:
				return new Field(saveField.getName(), this.getAuthorID());
			case CURRENT_USER:
				return new Field(saveField.getName(), currentUserID);
			}

		}
		return null;
	}

	protected String[] getWebFormValue(String fieldName, Map<String, String[]> fields, String defaultValue) {
		try {
			Object o = fields.get(fieldName);
			if (o != null) {
				return (String[]) o;
			} else {
				AppEnv.logger.warningLogEntry("Field \"" + fieldName + "\" has not found on webform, have to return default value");
				String val[] = { defaultValue };
				return val;
			}
		} catch (Exception e) {
			AppEnv.logger.errorLogEntry("Unable to get field \"" + fieldName + "\" from webform, have to return default value");
			String val[] = { defaultValue };
			return val;
		}
	}

	private Field getStaticContent(ISaveField saveField) {
		return new Field(saveField.getName(), saveField.getValue(), saveField.getType());
	}

	private Field getDoScriptResult(ISaveField saveField) {
		return new Field(saveField.getName(), saveField.getValue(), saveField.getType());
	}

	private Field getWebFormValue(ISaveField saveField, Map<String, String[]> fields) throws WebFormValueException {
		try {
			String valueToSave[] = fields.get(saveField.getValue());
			if (valueToSave.length > 1) {
				return new Field(saveField.getName(), valueToSave);
			} else {
				return new Field(saveField.getName(), valueToSave[0], saveField.getType());
			}

		} catch (Exception e) {
			if (saveField.getIfErrorValue().equals("")) {
				// throw new
				// WebFormValueException(WebFormValueExceptionType.FORMDATA_INCORRECT,
				// saveField.getValue());
				return new Field(saveField.getName(), "", saveField.getType());
			} else {
				return new Field(saveField.getName(), saveField.getIfErrorValue(), saveField.getType());
			}
		}
	}

	public void clearEditors() {
		editors.clear();
		addEditors(supervisorGroupAsList);
	}

	public void addEditors(List<String> listVal) {
		for (String value : listVal) {
			addEditor(value);
		}
	}

	public void addEditors(Set<String> listVal) {
		for (String value : listVal) {
			addEditor(value);
		}
	}

	public void clearReaders() {
		readers.clear();
		readers.add(new Reader(authorID));
	}

	public void addReaders(Collection<String> values) {
		for (String value : values) {
			addReader(value);
		}
	}

	public void addReaders(HashSet<Reader> r) {
		for (Reader value : r) {
			addReader(value.getUserID());
		}
	}

	public void addEditor(String name) {
		if (!name.equals("") && !name.equalsIgnoreCase("[]")) {
			editors.add(name);
			if (!name.startsWith("[") && !name.startsWith("]")) {
				editors.add("[" + name + "]");
			}
		}

	}

	public void replaceReader(String name, boolean isFavourite) {
		if (!name.equals("")) {
			Reader reader = new Reader(name, isFavourite);
			if (!readers.add(reader)) {
				readers.remove(reader);
				readers.add(reader);
			}
		}

		if (!name.startsWith("[") && !name.endsWith("]")) {
			Reader reader_group = new Reader("[" + name + "]", isFavourite);
			if (!readers.add(reader_group)) {
				readers.remove(reader_group);
				readers.add(reader_group);
			}
		}
	}

	public void addReader(String name) {
		if (!name.equals("") && !name.equalsIgnoreCase("[]")) {
			readers.add(new Reader(name));
			if (!name.startsWith("[") && !name.startsWith("]")) {
				readers.add(new Reader("[" + name + "]"));
			}
		}

	}

	public void deleteEditor(String name) {
		editors.remove(name);
		boolean isGroup = name.startsWith("[") && name.endsWith("]");
		if (!isGroup && editors.contains("[" + name + "]")) {
			editors.remove("[" + name + "]");
		}
	}

	public void deleteField(String fieldName) {
		this.fieldsMap.remove(fieldName);
	}

	public void deleteReader(String name) {
		readers.remove(name);
		boolean isGroup = name.startsWith("[") && name.endsWith("]");
		if (!isGroup && readers.contains("[" + name + "]")) {
			readers.remove("[" + name + "]");
		}
	}

	public HashSet<String> getEditors() {
		return editors;
	}

	public HashSet<Reader> getReaders() {
		return readers;
	}

	public ArrayList<BaseDocument> getResponses(int docID, int docType, Set<String> complexUserID, String absoluteUserID)
	        throws DocumentAccessException, DocumentException, ComplexObjectException {
		return responses;

	}

	public DocumentCollection getDescendants(int docID, int docType, Set<String> complexUserID, String absoluteUserID) {
		return descendants;

	}

	public ArrayList<BaseDocument> getDescendantsArray(int docID, int docType, Set<String> complexUserID, String absoluteUserID)
	        throws DocumentException, ComplexObjectException {
		return responses;

	}

	@Override
	public int hashCode() {
		return ddbID.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		Document doc = (Document) o;
		if (ddbID.equalsIgnoreCase(doc.ddbID)) {
			return true;
		} else {
			return false;
		}

	}

	@Deprecated
	public String hasAttach() {
		if (blobFieldsMap.size() > 0) {
			return "true";
		} else {
			return "false";
		}
	}

	public boolean hasAttachment() {
		if (blobFieldsMap.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean addAttachment(String fieldName, File file, String comment) {
		HashMap<String, BlobFile> files = new HashMap<String, BlobFile>();
		String fileName = file.getName();
		BlobFile bf = new BlobFile();
		bf.originalName = file.getName();
		bf.checkHash = Util.getHexHash(file.getAbsolutePath());
		bf.comment = comment;
		bf.path = file.getAbsolutePath();
		files.put(bf.originalName, bf);
		addBlobField(fieldName, files);
		return true;
	}

	public ArrayList<String> copyAttachments(BaseDocument targetDoc) {
		ArrayList<String> result = new ArrayList<String>();

		return result;
	}

	public String hasResponse(Set<String> complexUserID, String absoluteUserID) {

		return "false";

	}

	public boolean hasEditor(Set<String> complexUserID) {
		HashSet<String> editors = this.getEditors();
		for (String id : complexUserID) {
			if (editors.contains(id)) {
				return true;
			}
		}
		return false;
	}

	public String getURL() throws DocumentException {
		return "Provider?type=edit&element=document&id=" + form + "&docid=" + ddbID;
	}

	public void setDdbID(String nid) {
		if (nid != null) { // tmp
			ddbID = nid;
		}
	}

	public String getDdbID() {
		if (ddbID.equals("")) {
			ddbID = Util.generateRandomAsText(env.appType);
		}
		return ddbID;
	}

	public String getFullURL() throws DocumentException {
		String schema = "http";
		int port = Environment.httpPort;
		if (Environment.isSSLEnable) {
			schema = "https";
			port = Environment.secureHttpPort;
		}
		String httpHost = schema + "://" + Environment.hostName + ":" + port + "/" + env.appType;
		return httpHost + "/" + getURL();
	}

	public HashMap<String, Field> getFields() {
		return fieldsMap;
	}

	public int save(User user) throws DocumentAccessException, DocumentException, LicenseException, ComplexObjectException {
		return -1;
	}

	@Override
	public String toString() {
		return "form:" + form + ",docid:" + docID + ",docType:" + docType + "ddbid:" + ddbID + ":" + fieldsMap;
	}

	public void parseXml(org.w3c.dom.Document xmlDoc, int pDocId) throws ComplexObjectException, IllegalAccessException, InstantiationException,
	        ClassNotFoundException, _Exception {
		// setParentDocumentID(String.valueOf(pDocId));

		parentDocID = pDocId;
		docType = Integer.parseInt(XMLUtil.getTextContent(xmlDoc, "document/@doctype"));
		dbID = XMLUtil.getTextContent(xmlDoc, "document/@dbid");
		parentDocType = Integer.parseInt(XMLUtil.getTextContent(xmlDoc, "document/@parentdoctype"));
		setAuthor(XMLUtil.getTextContent(xmlDoc, "document/@author"));
		ddbID = XMLUtil.getTextContent(xmlDoc, "document/@ddbid");
		regDate = Util.convertStringToDateTimeSilently(XMLUtil.getTextContent(xmlDoc, "document/@regdate"));
		lastUpdate = Util.convertStringToDateTimeSilently(XMLUtil.getTextContent(xmlDoc, "document/@lastupdate"));
		defaultRuleID = XMLUtil.getTextContent(xmlDoc, "document/@defaultreuleid");
		setViewNumber(BigDecimal.valueOf(Double.valueOf(XMLUtil.getTextContent(xmlDoc, "document/@viewnumber"))));
		setViewDate(Util.convertStringToDateTimeSilently(XMLUtil.getTextContent(xmlDoc, "document/@viewdate")));
		setSign(XMLUtil.getTextContent(xmlDoc, "document/@sign"));
		// hasDiscussion = Boolean.parseBoolean(XMLUtil.getTextContent(xmlDoc,
		// "document/@topicid"));
		setSignedFields(XMLUtil.getTextContent(xmlDoc, "document/@signfields"));
		setForm(XMLUtil.getTextContent(xmlDoc, "document/@form"));
		setViewText(XMLUtil.getTextContent(xmlDoc, "document/@viewtext"));

		String viewTextValue = "empty";
		int counter = 0;
		while (viewTextValue != null && !viewTextValue.equals("")) {
			counter++;
			viewTextValue = XMLUtil.getTextContent(xmlDoc, "document/@viewtext" + counter);
			addViewText(viewTextValue);
		}

		NodeList nodeList = XMLUtil.getNode(xmlDoc, "document", false).getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node k = nodeList.item(i);
			switch (k.getNodeName().trim()) {
			case "#text":
				break;
			case "readers":
				NodeList readersList = XMLUtil.getNodeList(k, "user");
				for (int a = 0; a < readersList.getLength(); a++) {
					addReader(readersList.item(a).getTextContent());
				}
				break;
			case "editors":
				NodeList editorsList = XMLUtil.getNodeList(k, "user");
				for (int a = 0; a < editorsList.getLength(); a++) {
					addEditor(editorsList.item(a).getTextContent());
				}
				break;
			default:
				switch (XMLUtil.getTextContent(k, "@type")) {
				case "map":
					String[] values = k.getTextContent().split("#");
					for (String value : values) {
						if (value != null && value.trim().length() != 0) {
							addValueToList(k.getNodeName().trim(), value);
						}
					}
					break;
				case "datetime":
					addDateField(k.getNodeName().trim(), _Helper.convertStringToDate(k.getTextContent()));
					break;
				case "string":
					addStringField(k.getNodeName().trim(), k.getTextContent());
					break;
				case "number":
					addNumberField(k.getNodeName().trim(), Double.valueOf(k.getTextContent()));
					break;
				case "complex":
					/*
					 * Class c = Class.forName(k.getTextContent().substring(0,
					 * k.getTextContent().indexOf("~"))); IComplexObject obj =
					 * (IComplexObject)c.newInstance(); obj.init(db,
					 * k.getTextContent());
					 * addComplexObjectField(k.getNodeName().trim(), obj);
					 */

					String value = k.getTextContent();
					String className = "";
					Pattern pattern = Pattern.compile("className=\\\"(.+?)\\\"");
					Matcher matcher = pattern.matcher(value);
					if (matcher.find()) {
						className = matcher.group();
					}
					IComplexObject object = AbstractComplexObject.unmarshall(className, value);
					addComplexObjectField(k.getNodeName().trim(), object);
					break;
				case "files":
					break; // нет необходимости обрабатывать
				case "":
					addStringField(k.getNodeName().trim(), k.getTextContent());
					break;
				default:
					System.err.println("unknown type " + XMLUtil.getTextContent(k, "@type"));
					break;
				}
				break;
			}
		}
	}

	public String toXML(boolean allDescendants) throws ComplexObjectException {
		StringBuffer xmlFragment = new StringBuffer(1000);
		// String viewText = getViewText();
		String[] viewTextList = getViewTextList().toArray(new String[getViewTextList().size()]);
		String viewTexts = "";
		for (int i = 0; i < viewTextList.length; i++) {
			viewTexts += " viewtext"
			        + i
			        + "=\""
			        + (viewTextList[i] != null && viewTextList[i].trim().length() > 0 ? viewTextList[i].trim().replace("&amp;", "&")
			                .replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&", "&amp;")
			                .replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;") : "null") + "\" ";
		}
		viewTexts = viewTexts.replace("viewtext0", "viewtext");

		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><document ";
		for (Field field : fields()) {
			String value = field.getType() == FieldType.COMPLEX_OBJECT ?
			// field.valueAsObject.getPersistentValue()
			AbstractComplexObject.marshall(field.valueAsObject.getClass().getName(), field.valueAsObject)
			        : field.valueAsText;
			// TODO replace with lang3 escape method
			value = value.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'")
			        .replace("&nbsp;", " ");
			value = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
			xmlFragment.append("<" + field.name + RuntimeObjUtil.getTypeAttribute(field.getTypeAsDatabaseType()) + ">" + value + "</" + field.name
			        + ">");
		}

		for (BlobField field : blobFieldsMap.values()) {
			xmlFragment.append("<" + field.name + RuntimeObjUtil.getTypeAttribute(7) + ">");
			for (BlobFile file : field.getFiles()) {
				xmlFragment.append("<entry>");
				xmlFragment.append("<name>" + file.originalName + "</name>");
				xmlFragment.append("<comment>" + file.getComment() + "</comment>");
				xmlFragment.append("</entry>");
			}
			xmlFragment.append("</" + field.name + ">");
		}

		// if (viewText.trim().equals("")) {
		// viewText = "null";
		// } else {
		// viewText = viewText.replace("\"", "'");
		// }

		if (allDescendants == false) {
			xmlFragment.append("<readers>");
			for (Reader r : getReaders()) {
				xmlFragment.append("<user>" + r + "</user>");
			}
			xmlFragment.append("</readers>");

			xmlFragment.append("<editors>");
			for (String a : getEditors()) {
				xmlFragment.append("<user>" + a + "</user>");
			}
			xmlFragment.append("</editors>");
			xml += " doctype = \"" + docType + "\" form= \"" + form + "\" dbid= \"" + dbID + "\" docid = \"" + docID + "\" " + "parentdocid = \""
			        + parentDocID + "\" parentdoctype = \"" + parentDocType + "\"" + " author=\"" + authorID + "\" ddbid = \"" + ddbID + "\" "
			        + "regdate=\"" + Util.convertDataTimeToString(regDate) + "\" " + "lastupdate=\"" + Util.convertDataTimeToString(lastUpdate)
			        + "\" " + viewTexts + "defaultreuleid= \"" + defaultRuleID + "\" viewnumber= \"" + getViewNumber() + "\" " + "viewdate= \""
			        + Util.convertDataTimeToString(getViewDate()) + "\" sign= \"" + getSign() + "\" " + "hastopic= \"" + (hasDiscussion ? 1 : 0)
			        + "\" signfields= \"" + getSignedFields() + "\">" + xmlFragment;
		} else {
			String hasDescendant = this.hasResponse(Const.sysGroupAsSet, Const.sysUser);
			xml += " hasDescendant=\""
			        + hasDescendant
			        + "\" id=\""
			        + ddbID
			        + "\" lastupdate=\""
			        + Util.convertDataTimeToString(lastUpdate)
			        + "\" viewtext =\""
			        + (viewTextList[0] != null && viewTextList[0].trim().length() > 0 ? viewTextList[0].trim().replace("&amp;", "&")
			                .replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&", "&amp;")
			                .replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;") : "null") + "\">" + "<fields>"
			        + xmlFragment + "</fields>";

			if (hasDescendant == "true") {

				String dxml = "<descendants> ";
				ArrayList<BaseDocument> descendantDoc = null;

				try {
					descendantDoc = this.getResponses(this.getDocID(), this.docType, Const.sysGroupAsSet, Const.sysUser);
				} catch (DocumentAccessException e) {
					e.printStackTrace();
				} catch (DocumentException e) {
					e.printStackTrace();
				}
				for (int i = 0; i < descendantDoc.size(); i++) {
					dxml += descendantDoc.get(i).toXML(allDescendants);
				}
				dxml += "</descendants>";
				xml += dxml;
			}
		}

		xml += "</document>";
		return xml;
	}

	public AppEnv getAppEnv() {
		return this.env;
	}

	public void setEnvironment(AppEnv env) {
		this.env = env;
	}

	@Deprecated
	public String getDefaultRuleID() {
		return this.defaultRuleID;
	}

	@Deprecated
	public void setDefaultRuleID(String rule) {
		this.defaultRuleID = rule;
	}

	/*
	 * public int getTopicID() { return topicID; }
	 * 
	 * public void setTopicID(int topicID) { this.topicID = topicID; }
	 */

	public void setEditMode(Set<String> groups) {
		for (String group : groups) {
			if (group.equalsIgnoreCase(observerGroup[0])) {
				this.editMode = EDITMODE_EDIT;
			}
		}
	}

	public String toXMLEntry(String value) {
		return "<entry hasattach=\"" + Integer.toString(0) + "\" doctype=\"" + docType + "\"  " + "docid=\"" + docID + "\""
		        + XMLUtil.getAsAttribute("viewtext", getViewText()) + "url=\"Provider?type=edit&amp;element=document&amp;id=" + form + "&amp;key="
		        + docID + "\" " + ">" + value + "</entry>";

	}

	public String toShortXML() {
		StringBuffer xmlFragment = new StringBuffer(1000);
		String viewText = getViewText();

		for (Field field : fields()) {
			xmlFragment.append("<" + field.name + RuntimeObjUtil.getTypeAttribute(field.getTypeAsDatabaseType()) + ">" + field.valueAsText + "</"
			        + field.name + ">");
		}

		String xml = "<document doctype = \"" + docType + "\"" + "id = \"" + ddbID + "\" viewtext=\"" + viewText + "\" " + "lastupdate=\""
		        + Util.convertDataTimeToString(lastUpdate) + "\">" + xmlFragment + "</document>";
		return xml;

	}

	public void fillResponses() throws DocumentAccessException, DocumentException, ComplexObjectException {
		responses = getResponses(docID, docType, Const.supervisorGroupAsSet, Const.sysUser);
	}

	public ArrayList<BaseDocument> getResponses() {
		return responses;
	}

	protected boolean normalizeViewTexts() {
		int s = viewTextList.size();
		if (s >= 0) {
			for (int i = s; i < 8; i++) {
				viewTextList.add("-");
			}
			return true;
		} else {
			return false;
		}
	}

	public String getParentDocID() {

		return null;
	}

	public String getParentDocumentID() {

		return "";

	}

	public void setParentDocumentID(String parentDocumentID) {
		this.parentDocumentID = parentDocumentID;
	}

	@Override
	public void init(IDatabase db, String initString) throws ComplexObjectException {

	}

	@Override
	public String getContent() {
		return null;
	}
}
