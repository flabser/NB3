package kz.nextbase.script;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.FTIndexEngineException;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IGlossaries;
import kz.flabs.dataengine.IQueryFormula;
import kz.flabs.dataengine.ISelectFormula;
import kz.flabs.dataengine.h2.queryformula.GlossarySelectFormula;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.ExceptionType;
import kz.flabs.exception.RuleException;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.parser.SortByBlock;
import kz.flabs.runtimeobj.constants.SortingType;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.Execution;
import kz.flabs.runtimeobj.document.glossary.Glossary;
import kz.flabs.runtimeobj.document.project.Project;
import kz.flabs.runtimeobj.document.structure.Organization;
import kz.flabs.runtimeobj.document.task.Task;
import kz.flabs.runtimeobj.queries.SimpleQueryFormula;
import kz.flabs.runtimeobj.viewentry.ViewEntry;
import kz.flabs.scriptprocessor.ScriptProcessor;
import kz.flabs.scriptprocessor.form.querysave.IQuerySaveTransaction;
import kz.flabs.users.RunTimeParameters;
import kz.flabs.users.RunTimeParameters.Sorting;
import kz.flabs.users.User;
import kz.flabs.webrule.constants.QueryType;
import kz.nextbase.script.constants.Month;
import kz.nextbase.script.constants._Direction;
import kz.nextbase.script.constants._DocumentType;
import kz.nextbase.script.constants._QueryMacroType;
import kz.nextbase.script.constants._ReadConditionType;
import kz.nextbase.script.project._Project;
import kz.nextbase.script.struct._Organization;
import kz.nextbase.script.task._Task;

public class _Database implements Const {

	IDatabase dataBase;

	private _Session session;
	private HashSet<String> userGroups = null;
	private String userID;
	private ArrayList<IQuerySaveTransaction> transactionConveyor;
	private User user;

	public _Database(IDatabase db, String userID, _Session session) {
		this.session = session;
		dataBase = db;
		user = session.getUser();
		userGroups = user.getAllUserGroups();
		this.userID = user.getUserID();
	}

	public _Session getParent() {
		return session;
	}

	public _ViewEntryCollection getCollectionOfDocuments(_ViewEntryCollectionParam param) {
		FormulaBlocks queryFormulaBlocks = new FormulaBlocks(param.getQuery(), QueryType.DOCUMENT);
		ISelectFormula sf = dataBase.getSelectFormula(queryFormulaBlocks);
		RunTimeParameters parameters = null;
		if (param.withFilter()) {
			HashMap<String, RunTimeParameters> currConditions = user.getSession().getRuntimeConditions();
			parameters = currConditions.get(getParent().getInitiator().getOwnerID());
		}
		if (parameters == null) {
			parameters = new RunTimeParameters(param.getDateFormat());
		}

		return dataBase.getCollectionByCondition(sf, user, param.getPageNum(), param.getPageSize(),
				session.getExpandedDocuments(), parameters, param.withResponse());
	}

	public _ViewEntryCollection getCollectionOfDocuments(String queryCondition, boolean checkResponse) {
		FormulaBlocks queryFormulaBlocks = new FormulaBlocks(queryCondition, QueryType.DOCUMENT);
		// ISelectFormula sf = new SelectFormula(queryFormulaBlocks);
		ISelectFormula sf = dataBase.getSelectFormula(queryFormulaBlocks);
		RunTimeParameters parameters = new RunTimeParameters();
		return dataBase.getCollectionByCondition(sf, user, 1, 0, session.getExpandedDocuments(), parameters,
				checkResponse);
	}

	public _ViewEntryCollection getCollectionOfDocuments(String queryCondition, boolean checkResponse,
			boolean checkUnread) {
		FormulaBlocks queryFormulaBlocks = new FormulaBlocks(queryCondition, QueryType.DOCUMENT);
		// ISelectFormula sf = new SelectFormula(queryFormulaBlocks);
		ISelectFormula sf = dataBase.getSelectFormula(queryFormulaBlocks);
		RunTimeParameters parameters = new RunTimeParameters();
		return dataBase.getCollectionByCondition(sf, user, 1, 0, session.getExpandedDocuments(), parameters,
				checkResponse, false, checkUnread);
	}

	public _ViewEntryCollection getCollectionOfDocuments(String queryCondition, int pageNum, boolean checkResponse) {
		FormulaBlocks queryFormulaBlocks = new FormulaBlocks(queryCondition, QueryType.DOCUMENT);
		// ISelectFormula sf = new SelectFormula(queryFormulaBlocks);
		ISelectFormula sf = dataBase.getSelectFormula(queryFormulaBlocks);
		int pageSize = user.getSession().pageSize;
		RunTimeParameters parameters = new RunTimeParameters();
		return dataBase.getCollectionByCondition(sf, user, pageNum, pageSize, session.getExpandedDocuments(),
				parameters, checkResponse);
	}

	public _ViewEntryCollection getCollectionOfDocuments(String queryCondition, int pageNum, boolean checkResponse,
			boolean useFilter) {
		FormulaBlocks queryFormulaBlocks = new FormulaBlocks(queryCondition, QueryType.DOCUMENT);
		// ISelectFormula sf = new SelectFormula(queryFormulaBlocks);
		ISelectFormula sf = dataBase.getSelectFormula(queryFormulaBlocks);
		int pageSize = user.getSession().pageSize;
		RunTimeParameters parameters = null;
		if (useFilter) {
			HashMap<String, RunTimeParameters> currConditions = user.getSession().getRuntimeConditions();
			parameters = currConditions.get(getParent().getInitiator().getOwnerID());
		}
		if (parameters == null) {
			parameters = new RunTimeParameters();
		}
		return dataBase.getCollectionByCondition(sf, user, pageNum, pageSize, session.getExpandedDocuments(),
				parameters, checkResponse);
	}

	public _ViewEntryCollection getCollectionOfDocuments(String queryCondition, int pageNum, boolean checkResponse,
			boolean useFilter, boolean expandAllResponses) {
		FormulaBlocks queryFormulaBlocks = new FormulaBlocks(queryCondition, QueryType.DOCUMENT);
		// ISelectFormula sf = new SelectFormula(queryFormulaBlocks);
		ISelectFormula sf = dataBase.getSelectFormula(queryFormulaBlocks);
		int pageSize = user.getSession().pageSize;
		RunTimeParameters parameters = null;
		if (useFilter) {
			HashMap<String, RunTimeParameters> currConditions = user.getSession().getRuntimeConditions();
			parameters = currConditions.get(getParent().getInitiator().getOwnerID());
		}
		if (parameters == null) {
			parameters = new RunTimeParameters();
		}
		return dataBase.getCollectionByCondition(sf, user, pageNum, pageSize, session.getExpandedDocuments(),
				parameters, checkResponse, expandAllResponses);
	}

	public _ViewEntryCollection getCollectionOfDocuments(String queryCondition, int pageNum, boolean checkResponse,
			boolean useFilter, boolean expandAllResponses, boolean checkRead) {
		FormulaBlocks queryFormulaBlocks = new FormulaBlocks(queryCondition, QueryType.DOCUMENT);
		// ISelectFormula sf = new SelectFormula(queryFormulaBlocks);
		ISelectFormula sf = dataBase.getSelectFormula(queryFormulaBlocks);
		int pageSize = user.getSession().pageSize;
		RunTimeParameters parameters = null;
		if (useFilter) {
			HashMap<String, RunTimeParameters> currConditions = user.getSession().getRuntimeConditions();
			parameters = currConditions.get(getParent().getInitiator().getOwnerID());
		}
		if (parameters == null) {
			parameters = new RunTimeParameters();
		}
		return dataBase.getCollectionByCondition(sf, user, pageNum, pageSize, session.getExpandedDocuments(),
				parameters, checkResponse, expandAllResponses, checkRead);
	}

	public _ViewEntryCollection getCollectionOfDocuments(String queryCondition, int pageNum, boolean checkResponse,
			boolean useFilter, boolean expandAllResponses, _ReadConditionType type) {
		FormulaBlocks queryFormulaBlocks = new FormulaBlocks(queryCondition, QueryType.DOCUMENT);
		// ISelectFormula sf = new SelectFormula(queryFormulaBlocks);
		ISelectFormula sf = dataBase.getSelectFormula(queryFormulaBlocks);
		int pageSize = user.getSession().pageSize;
		RunTimeParameters parameters = null;
		if (useFilter) {
			HashMap<String, RunTimeParameters> currConditions = user.getSession().getRuntimeConditions();
			parameters = currConditions.get(getParent().getInitiator().getOwnerID());
		}
		if (parameters == null) {
			parameters = new RunTimeParameters();
		}
		return dataBase.getCollectionByCondition(sf, user, pageNum, pageSize, session.getExpandedDocuments(),
				parameters, checkResponse, expandAllResponses, type);
	}

	public _ViewEntryCollection getCollectionOfDocuments(String queryCondition, int pageNum, boolean checkResponse,
			boolean useFilter, boolean expandAllResponses, _ReadConditionType type, String customFieldName) {
		FormulaBlocks queryFormulaBlocks = new FormulaBlocks(queryCondition, QueryType.DOCUMENT);
		// ISelectFormula sf = new SelectFormula(queryFormulaBlocks);
		ISelectFormula sf = dataBase.getSelectFormula(queryFormulaBlocks);
		int pageSize = user.getSession().pageSize;
		RunTimeParameters parameters = null;
		if (useFilter) {
			HashMap<String, RunTimeParameters> currConditions = user.getSession().getRuntimeConditions();
			parameters = currConditions.get(getParent().getInitiator().getOwnerID());
		}
		if (parameters == null) {
			parameters = new RunTimeParameters();
		}
		return dataBase.getCollectionByCondition(sf, user, pageNum, pageSize, session.getExpandedDocuments(),
				parameters, checkResponse, expandAllResponses, type, customFieldName);
	}

	public _ViewEntryCollection getCollectionOfDocuments(String queryCondition, int pageNum, boolean checkResponse,
			boolean useFilter, String responseQueryCondition) {
		FormulaBlocks queryFormulaBlocks = new FormulaBlocks(queryCondition, QueryType.DOCUMENT);
		// ISelectFormula sf = new SelectFormula(queryFormulaBlocks);
		ISelectFormula sf = dataBase.getSelectFormula(queryFormulaBlocks);

		int pageSize = user.getSession().pageSize;
		RunTimeParameters parameters = null;
		if (useFilter) {
			HashMap<String, RunTimeParameters> currConditions = user.getSession().getRuntimeConditions();
			parameters = currConditions.get(getParent().getInitiator().getOwnerID());
		}
		if (parameters == null) {
			parameters = new RunTimeParameters();
		}
		return dataBase.getCollectionByCondition(sf, user, pageNum, pageSize, session.getExpandedDocuments(),
				parameters, checkResponse, responseQueryCondition);
	}

	public _ViewEntryCollection getCollectionOfDocuments(String queryCondition, int pageNum, boolean checkResponse,
			boolean useFilter, SimpleDateFormat simpleDateFormat) {
		FormulaBlocks queryFormulaBlocks = new FormulaBlocks(queryCondition, QueryType.DOCUMENT);
		// ISelectFormula sf = new SelectFormula(queryFormulaBlocks);
		ISelectFormula sf = dataBase.getSelectFormula(queryFormulaBlocks);
		int pageSize = user.getSession().pageSize;
		RunTimeParameters parameters = null;
		if (useFilter) {
			HashMap<String, RunTimeParameters> currConditions = user.getSession().getRuntimeConditions();
			parameters = currConditions.get(getParent().getInitiator().getOwnerID());
		}
		if (parameters == null) {
			parameters = new RunTimeParameters(simpleDateFormat);
		} else {
			parameters.setSimpleDateFormat(simpleDateFormat);
		}
		return dataBase.getCollectionByCondition(sf, user, pageNum, pageSize, session.getExpandedDocuments(),
				parameters, checkResponse);
	}

	public _ViewEntryCollection getCollectionOfDocuments(String queryCondition, int pageNum, int pageSize,
			boolean checkResponse, String sortingColumnName, _Direction direction) {
		FormulaBlocks queryFormulaBlocks = new FormulaBlocks(queryCondition, QueryType.DOCUMENT);
		// ISelectFormula sf = new SelectFormula(queryFormulaBlocks);
		ISelectFormula sf = dataBase.getSelectFormula(queryFormulaBlocks);
		RunTimeParameters parameters = new RunTimeParameters();
		Sorting s = parameters.new Sorting(sortingColumnName);
		switch (direction) {
		case ASCENDING:
			s.sortingDirection = SortingType.ASC;
			break;
		case DESCENDING:
			s.sortingDirection = SortingType.DESC;
			break;
		}
		parameters.getSorting().add(s);
		parameters.sortingMap.put(sortingColumnName, s);
		return dataBase.getCollectionByCondition(sf, user, pageNum, pageSize, session.getExpandedDocuments(),
				parameters, checkResponse);
	}

	public _ViewEntryCollection getCollectionOfGlossaries(_ViewEntryCollectionParam param) {
		FormulaBlocks formulaBlocks = new FormulaBlocks(param.getQuery(), QueryType.GLOSSARY);
		ISelectFormula sf = this.dataBase.getSelectFormula(formulaBlocks);
		RunTimeParameters parameters = null;
		if (param.withFilter()) {
			HashMap<String, RunTimeParameters> currConditions = user.getSession().getRuntimeConditions();
			parameters = currConditions.get(getParent().getInitiator().getOwnerID());
		}
		if (parameters == null) {
			parameters = new RunTimeParameters(param.getDateFormat());
		}

		return dataBase.getGlossaries().getCollectionByCondition(sf, param.getPageNum(), param.getPageSize(),
				session.getExpandedDocuments(), parameters, param.withResponse());
	}

	public _ViewEntryCollection getCollectionOfGlossaries(String queryCondition, int pageNum, int pageSize) {
		FormulaBlocks formulaBlocks = new FormulaBlocks(queryCondition, QueryType.GLOSSARY);
		ISelectFormula sf = this.dataBase.getSelectFormula(formulaBlocks);
		RunTimeParameters parameters = new RunTimeParameters();
		return dataBase.getGlossaries().getCollectionByCondition(sf, pageNum, pageSize, session.getExpandedDocuments(),
				parameters, true);
	}

	public _ViewEntryCollection getCollectionOfGlossaries(String queryCondition, int pageNum, int pageSize,
			boolean checkResponse, String sortingColumnName, _Direction direction) {
		FormulaBlocks queryFormulaBlocks = new FormulaBlocks(queryCondition, QueryType.GLOSSARY);
		ISelectFormula sf = new GlossarySelectFormula(queryFormulaBlocks);
		RunTimeParameters parameters = new RunTimeParameters();
		Sorting s = parameters.new Sorting(sortingColumnName);
		switch (direction) {
		case ASCENDING:
			s.sortingDirection = SortingType.ASC;
			break;
		case DESCENDING:
			s.sortingDirection = SortingType.DESC;
			break;
		}
		if (s.isValid()) {
			parameters.getSorting().add(s);
			parameters.sortingMap.put(sortingColumnName, s);
		}

		return dataBase.getGlossaries().getCollectionByCondition(sf, pageNum, pageSize, session.getExpandedDocuments(),
				parameters, checkResponse);
	}

	public _ViewEntryCollection getCollectionOfTopics(String queryCondition, int pageNum, int pageSize) {
		FormulaBlocks queryFormulaBlocks = new FormulaBlocks(queryCondition, QueryType.DOCUMENT);
		ISelectFormula sf = this.dataBase.getForumSelectFormula(queryFormulaBlocks);
		RunTimeParameters parameters = new RunTimeParameters();
		return dataBase.getForum().getTopicsCollection(sf, user, pageNum, pageSize, parameters, Boolean.FALSE);
	}

	public _ViewEntryCollection search(String keyWord, int pageNum, String[] filter)
			throws UnsupportedEncodingException, DocumentException, FTIndexEngineException, RuleException,
			QueryFormulaParserException {
		keyWord = new String(keyWord.getBytes("ISO-8859-1"), "UTF-8");
		int pageSize = user.getSession().pageSize;
		return dataBase.getFTSearchEngine().search(keyWord, user, pageNum, pageSize, filter, new String[5]);

	}

	public ArrayList<_ViewEntry> getGroupedEntries(String fieldName, int pageNum, int pageSize)
			throws DocumentException, DocumentAccessException {
		ArrayList<_ViewEntry> col = new ArrayList<_ViewEntry>();

		ArrayList<ViewEntry> entryCollection = dataBase.getGroupedEntries(fieldName,
				dataBase.calcStartEntry(pageNum, pageSize), pageSize, user);
		for (ViewEntry ve : entryCollection) {
			col.add(new _ViewEntry(ve, session));
		}
		return col;
	}

	public ArrayList<_Document> getDocumentsForMonth(String form, String fieldName, Month month, int pageNum,
			int pageSize) throws DocumentException, DocumentAccessException {
		ArrayList<_Document> col = new ArrayList<_Document>();
		int m = 0;
		switch (month) {
		case JANUARY:
			m = 1;
			break;
		case FEBRUARY:
			m = 2;
			break;
		case MARCH:
			m = 3;
			break;
		case APRIL:
			m = 4;
			break;
		case MAY:
			m = 5;
			break;
		case JUNE:
			m = 6;
			break;
		case JULY:
			m = 7;
			break;
		case AUGUST:
			m = 8;
			break;
		case SEPTEMBER:
			m = 9;
			break;
		case OCTOBER:
			m = 10;
			break;
		case NOVEMBER:
			m = 11;
			break;
		case DECEMBER:
			m = 12;
			break;
		case CURRENT_MONTH:
			Calendar currentTime = new GregorianCalendar();
			currentTime.setTime(new Date());
			m = currentTime.get(Calendar.MONTH);
			break;
		}

		ArrayList<BaseDocument> docsCollection = dataBase.getDocumentsForMonth(userGroups, userID, form, fieldName, m,
				dataBase.calcStartEntry(pageNum, pageSize), pageSize);
		for (BaseDocument doc : docsCollection) {
			col.add(new _Document(doc));
		}
		return col;
	}

	@Deprecated
	public int getCount(_QueryMacroType macro) throws DocumentException, DocumentAccessException, _Exception {

		SimpleQueryFormula nf = new SimpleQueryFormula();

		switch (macro) {
		case TO_CONSIDER:
			return dataBase.getMyDocsProcessor().getToConsiderCount(nf, userID);
		case TASKS_FOR_ME:
			return dataBase.getMyDocsProcessor().getTasksForMeCount(nf, userID);
		case MY_TASKS:
			return dataBase.getMyDocsProcessor().getMyTasksCount(nf, userID);
		case COMPLETE_TASKS:
			return dataBase.getMyDocsProcessor().getCompleteTaskCount(nf, userID);
		case WAIT_FOR_COORD:
			return dataBase.getMyDocsProcessor().getPrjsWaitForCoordCount(nf, userID);
		case WAIT_FOR_SIGN:
			return dataBase.getMyDocsProcessor().getPrjsWaitForSignCount(nf, userID);
		case FAVOURITES:
			return dataBase.getFavoritesCount(userGroups, userID);
		default:
			throw new _Exception(_ExceptionType.UNKNOWN_MACRO,
					"unknown macro, function: _Document.getCount(" + macro + ")");

		}

	}

	public _ViewEntryCollection getCollectionOfOrganizations(String queryCondition, int pageNum, int pageSize,
			String sortingColumnName, _Direction direction) {
		// _ViewEntryCollection getOrganization(ISelectFormula sf, int pageNum,
		// int pageSize,
		// RunTimeParameters parameters);
		return null;
	}

	@Deprecated
	public ArrayList<_Document> getAllDocuments(int docType, Set<String> complexUserID, String absoluteUserID,
			String[] fields, int offset, int pageSize)
					throws DocumentException, DocumentAccessException, ComplexObjectException {
		ArrayList<BaseDocument> docs = this.dataBase.getAllDocuments(docType, complexUserID, absoluteUserID, fields,
				offset, pageSize);
		ArrayList<_Document> _docs = new ArrayList<_Document>();
		for (BaseDocument doc : docs) {
			_Document _doc = new _Document(doc);
			_docs.add(_doc);
		}
		return _docs;
	}

	@Deprecated
	public ArrayList<_Document> getAllDocuments(int docType, int offset, int pageSize)
			throws DocumentException, DocumentAccessException, ComplexObjectException {
		ArrayList<BaseDocument> docs = this.dataBase.getAllDocuments(docType, userGroups, userID, offset, pageSize);
		ArrayList<_Document> _docs = new ArrayList<_Document>();
		for (BaseDocument doc : docs) {
			_Document _doc = new _Document(doc, session);
			_docs.add(_doc);
		}
		return _docs;
	}

	@Deprecated
	public ArrayList<_Document> getDocuments(int docType, int offset, int pageSize)
			throws DocumentException, DocumentAccessException, ComplexObjectException {
		// DocumentCollection col =
		// dataBase.getCollectionByCondition(IQueryFormula condition,
		// Set<String> complexUserID, String absoluteUserID, int offset, int
		// pageSize, Set<DocID>
		// toExpandResponses,String[] filters,String[] sorting);

		ArrayList<BaseDocument> docs = this.dataBase.getAllDocuments(docType, userGroups, userID, offset, pageSize);
		ArrayList<_Document> _docs = new ArrayList<_Document>();
		for (BaseDocument doc : docs) {
			_Document _doc = new _Document(doc, session);
			_docs.add(_doc);
		}
		return _docs;
	}

	@Deprecated
	public _DocumentCollection getAllDocuments(_DocumentType docType, int offset, int pageSize)
			throws DocumentException, DocumentAccessException, ComplexObjectException {
		switch (docType) {
		case MAINDOC:
			ArrayList<BaseDocument> docs = this.dataBase.getAllDocuments(DOCTYPE_MAIN, userGroups, userID, offset,
					pageSize);
			return new _DocumentCollection(docs, session);
		case PROJECT:
			ArrayList<BaseDocument> prjs = this.dataBase.getAllDocuments(DOCTYPE_PROJECT, userGroups, userID, offset,
					pageSize);
			return new _DocumentCollection(prjs, session);
		case TASK:
			ArrayList<BaseDocument> tasks = this.dataBase.getAllDocuments(DOCTYPE_TASK, userGroups, userID, offset,
					pageSize);
			return new _DocumentCollection(tasks, session);
		case EXECUTION:
			ArrayList<BaseDocument> execs = this.dataBase.getAllDocuments(DOCTYPE_EXECUTION, userGroups, userID, offset,
					pageSize);
			return new _DocumentCollection(execs, session);
		}
		return null;
	}

	@Deprecated
	public ArrayList<_Document> getAllDocuments(int docType, Set<String> complexUserID, String absoluteUserID,
			int offset, int pageSize) throws DocumentException, DocumentAccessException, ComplexObjectException {
		ArrayList<BaseDocument> docs = this.dataBase.getAllDocuments(docType, complexUserID, absoluteUserID, offset,
				pageSize);
		ArrayList<_Document> _docs = new ArrayList<_Document>();
		for (BaseDocument doc : docs) {
			_Document _doc = new _Document(doc);
			_docs.add(_doc);
		}
		return _docs;
	}

	@Deprecated
	public ArrayList<Integer> getAllDocumentsIDS(int docType, Set<String> complexUserID, String absoluteUserID,
			String[] fields, int offset, int pageSize) throws DocumentException, DocumentAccessException {
		return this.dataBase.getAllDocumentsIDS(docType, complexUserID, absoluteUserID, fields, offset, pageSize);
	}

	@Deprecated
	public ArrayList<Integer> getAllDocumentsIDS(int docType, Set<String> complexUserID, String absoluteUserID,
			int offset, int pageSize) throws DocumentException, DocumentAccessException {
		return this.dataBase.getAllDocumentsIDS(docType, complexUserID, absoluteUserID, offset, pageSize);
	}

	@Deprecated
	public ArrayList<Integer> getAllDocumentsIDsByCondition(String query, int docType, Set<String> complexUserID,
			String absoluteUserID) throws DocumentException, DocumentAccessException {
		return this.dataBase.getAllDocumentsIDsByCondition(query, docType, complexUserID, absoluteUserID);
	}

	@Deprecated
	public ArrayList<_Document> getAllDocsByReader(String query, Set<String> complexUserID, String absoluteUserID)
			throws DocumentException, QueryFormulaParserException, DocumentAccessException, ComplexObjectException {

		ArrayList<BaseDocument> docs = this.dataBase.getDocumentsByCondition(query, complexUserID, absoluteUserID, 0,
				0);
		ArrayList<_Document> _docs = new ArrayList<_Document>();
		for (BaseDocument doc : docs) {
			_Document _doc = new _Document(doc);
			_docs.add(_doc);
		}
		return _docs;
	}

	@Deprecated
	public _DocumentCollection getDocsCollection(String condition, int offset, int limit) throws _Exception {
		try {
			ArrayList<BaseDocument> docs = dataBase.getDocumentsByCondition(condition, userGroups, userID, limit,
					offset);
			_DocumentCollection col = new _DocumentCollection(docs, session);
			int docscount = dataBase.getDocumentsCountByCondition(condition, userGroups, userID);
			Properties params = new Properties();
			params.setProperty("count", String.valueOf(docscount));
			params.setProperty("currentpage", String.valueOf(offset));
			params.setProperty("maxpage", String.valueOf(_Helper.countMaxPage(docscount, limit)));
			col.setParameter("query", params);
			return col;
		} catch (Exception e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR,
					e.getMessage() + " function: _Document.getDocsCollection(" + condition + ")");
		}
	}

	@Deprecated
	public _DocumentCollection getDocsCollection(String form, int docType, String condition)
			throws DocumentException, DocumentAccessException, QueryFormulaParserException {
		AppEnv.logger.errorLogEntry(
				"Groovy - _Database.getDocsCollection(String form, String docType, String condition) is obsolete method. Use _Database.getDocsCollection(String condition)(getTasksCollection, getPrjsCollection)");
		return null;
	}

	@Deprecated
	public _DocumentCollection getTasksCollection(String condition)
			throws DocumentException, DocumentAccessException, QueryFormulaParserException {
		ArrayList<BaseDocument> docs = new ArrayList<BaseDocument>();
		ArrayList<Task> tasks = dataBase.getTasks().getTasksByCondition(condition, userGroups, userID);
		for (Task t : tasks) {
			docs.add(t);
		}
		return new _DocumentCollection(docs, session);
	}

	@Deprecated
	public _DocumentCollection getTasksCollection(String condition, Set<String> complexUserID, String absoluteUserID)
			throws DocumentException, DocumentAccessException, QueryFormulaParserException {
		ArrayList<BaseDocument> docs = new ArrayList<BaseDocument>();
		ArrayList<Task> tasks = dataBase.getTasks().getTasksByCondition(condition, complexUserID, absoluteUserID);
		for (Task t : tasks) {
			docs.add(t);
		}
		return new _DocumentCollection(docs, session);
	}

	@Deprecated
	public _DocumentCollection getPrjsCollection(String condition)
			throws DocumentException, DocumentAccessException, QueryFormulaParserException {
		ArrayList<BaseDocument> docs = dataBase.getProjects().getProjectsByCondition(condition, userGroups, userID);
		return new _DocumentCollection(docs, session);
	}

	@Deprecated
	public _DocumentCollection getPrjsCollection(String condition, HashSet<String> complexUserID, String absoluteUserID)
			throws DocumentException, DocumentAccessException, QueryFormulaParserException {
		ArrayList<BaseDocument> docs = dataBase.getProjects().getDocumentsByCondition(condition, complexUserID,
				absoluteUserID);
		return new _DocumentCollection(docs, session);
	}

	public void setTransConveyor(ArrayList<IQuerySaveTransaction> tc) {
		transactionConveyor = tc;
	}

	public int getRegNumber(String key) {
		RegNum rn = new RegNum();
		transactionConveyor.add(rn);
		return rn.getRegNumber(key);
	}

	public void addCounter(String key, int num) {
		this.dataBase.addCounter(key, num);
	}

	@Deprecated
	public String getMainDocumentFieldValueByDOCID(int docID, Set<String> complexUserID, String absoluteUserID,
			String fieldName) throws DocumentAccessException {
		return dataBase.getMainDocumentFieldValueByID(docID, complexUserID, absoluteUserID, fieldName);
	}

	@Deprecated
	public String getGlossaryCustomFieldValueByDOCID(int docID, String fieldName) {
		return dataBase.getGlossaryCustomFieldValueByID(docID, fieldName);
	}

	public _Organization getOrganization(int orgID) {
		Organization org = dataBase.getStructure().getOrganization(orgID, new User(Const.sysUser));
		if (org == null) {
			return null;
		}

		return new _Organization(org, session);
	}

	public _Document getDocumentByID(String docID) throws _Exception {
		try {
			if (docID == null) {
				throw new _Exception("document ID is null");
			}

			if (docID.trim().length() == 0) {
				throw new _Exception("document ID is empty");
			}

			BaseDocument doc = dataBase.getDocumentByDdbID(docID, userGroups, userID);
			return new _Document(doc, session);
		} catch (DocumentAccessException e) {
			throw new _Exception(e.getMessage(), session);
		} catch (DocumentException e) {
			throw new _Exception(e.getMessage(), session);
		} catch (ComplexObjectException e) {
			throw new _Exception(e.getMessage(), session);
		}
	}

	public _Glossary getGlossaryByID(String docID) throws _Exception {
		if (docID == null) {
			throw new _Exception("document ID is null");
		}

		if (docID.trim().length() == 0) {
			throw new _Exception("document ID is empty");
		}

		Glossary glos = dataBase.getGlossaries().getGlossaryDocumentByID(docID);
		return new _Glossary(glos, session);
	}

	public String removeDocumentFromRecycleBin(int id) {
		return dataBase.removeDocumentFromRecycleBin(id);
	}

	@Deprecated
	public _Document getDocumentByComplexID(int docType, int docID) throws _Exception {
		BaseDocument doc;
		try {
			doc = dataBase.getDocumentByComplexID(docType, docID);
			_Document _doc = new _Document(doc, session);
			return _doc;
		} catch (DocumentException e) {
			throw new _Exception(e.getMessage());
		} catch (DocumentAccessException e) {
			throw new _Exception(e.getMessage());
		} catch (ComplexObjectException e) {
			throw new _Exception(e.getMessage());
		}
	}

	@Deprecated
	public _Document getDocumentByComplexID(String docType, String docID) throws _Exception {
		try {
			int dt = Integer.parseInt(docType);
			int di = Integer.parseInt(docID);
			return getDocumentByComplexID(dt, di);
		} catch (NumberFormatException e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR,
					"Value has not converted to number: getDocumentByComplexID(" + docType + "," + docID + ")");
		}

	}

	@Deprecated
	public _Document getDocumentByComplexID(int[] docID) throws _Exception {
		Document doc;
		try {
			doc = (Document) dataBase.getDocumentByComplexID(docID[0], docID[1]);
			_Document _doc = new _Document(doc, userID);
			return _doc;
		} catch (DocumentException e) {
			throw new _Exception(e.getMessage());
		} catch (DocumentAccessException e) {
			throw new _Exception(e.getMessage());
		} catch (ComplexObjectException e) {
			throw new _Exception(e.getMessage());
		}
	}

	@Deprecated
	public _Document getDocumentByID(int docID) throws _Exception {
		try {
			Document doc = dataBase.getMainDocumentByID(docID, userGroups, userID);
			_Document _doc = new _Document(doc, session);
			return _doc;
		} catch (DocumentAccessException e) {
			throw new _Exception(e.getMessage());
		} catch (DocumentException e) {
			throw new _Exception(e.getMessage());
		} catch (ComplexObjectException e) {
			throw new _Exception(e.getMessage());
		}
	}

	public boolean deleteDocument(String ddbId, boolean completely) throws _Exception {
		try {
			dataBase.deleteDocument(ddbId, completely, user);
		} catch (DocumentAccessException e) {
			if (e.exceptionType == ExceptionType.DOCUMENT_DELETE_RESTRICTED) {
				throw new _Exception(_ExceptionType.DOCUMENT_DELETING_ERROR,
						"Unable to delete document. Document is read only", this.session);
			} else if (e.exceptionType == ExceptionType.DELETING_RESTRICTED_CAUSED_RELATED_DOCUMENTS) {
				throw new _Exception(_ExceptionType.DOCUMENT_DELETING_ERROR,
						"Unable to delete document. Document has some relations", this.session);
			}
		} catch (InstantiationException e) {
			throw new _Exception(e.getMessage(), this.session);
		} catch (IllegalAccessException e) {
			throw new _Exception(e.getMessage(), this.session);
		} catch (ClassNotFoundException e) {
			throw new _Exception(e.getMessage(), this.session);
		} catch (DocumentException e) {
			throw new _Exception(e.getMessage(), this.session);
		} catch (SQLException e) {
			if (e.getMessage().contains("Call getNextException to see the cause")) {
				SQLException next = e.getNextException();
				// Нарушение ограничения целостности
				if (next.getSQLState().equalsIgnoreCase("23503")) {
					throw new _Exception(_ExceptionType.DOCUMENT_DELETING_ERROR,
							"Unable to delete document. Document has some relations", this.session);
				}
				throw new _Exception(next.getMessage(), this.session);
			}
			throw new _Exception(e.getMessage(), this.session);
		} catch (DatabasePoolException e) {
			throw new _Exception(e.getMessage(), this.session);
		} catch (ComplexObjectException e) {
			throw new _Exception(e.getMessage(), this.session);
		}
		return true;
	}

	// You should use delete method with String docID: deleteDocument(String
	// docID, boolean
	// completely)
	@Deprecated
	public boolean deleteDocument(int docType, int docID, boolean completely) throws _Exception {
		try {
			dataBase.deleteDocument(docType, docID, user, completely);
		} catch (DocumentAccessException e) {
			// it is to extend to separate error type
			throw new _Exception(_ExceptionType.NO_PERMISSION_TO_READ_THE_DOCUMENT,
					"Unable to delete document. Security reason: deleteDocument(int docType, int docID, boolean completely)");
		} catch (InstantiationException e) {
			throw new _Exception(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new _Exception(e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new _Exception(e.getMessage());
		} catch (DocumentException e) {
			throw new _Exception(e.getMessage());
		} catch (SQLException e) {
			throw new _Exception(e.getMessage());
		} catch (DatabasePoolException e) {
			throw new _Exception(e.getMessage());
		} catch (ComplexObjectException e) {
			throw new _Exception(e.getMessage());
		}
		return true;
	}

	public boolean unDeleteDocument(String docID) throws _Exception {
		try {
			dataBase.unDeleteDocument(docID, user);
		} catch (DocumentAccessException e) {
			// it is to extend to separate error type
			throw new _Exception(_ExceptionType.NO_PERMISSION_TO_READ_THE_DOCUMENT,
					"Unable to undelete document. Security reason: unDeleteDocument(int docType, int docID, boolean completely)");
		} catch (ComplexObjectException e) {
			throw new _Exception(e.getMessage());
		}
		return true;
	}

	public boolean unDeleteDocument(int docID) throws _Exception {
		try {
			dataBase.unDeleteDocument(docID, user);
		} catch (DocumentAccessException e) {
			// it is to extend to separate error type
			throw new _Exception(_ExceptionType.NO_PERMISSION_TO_READ_THE_DOCUMENT,
					"Unable to undelete document. Security reason: unDeleteDocument(int docType, int docID, boolean completely)");
		} catch (DocumentException e) {
			throw new _Exception(e.getMessage());
		} catch (ComplexObjectException e) {
			throw new _Exception(e.getMessage());
		}
		return true;
	}

	@Deprecated
	public _Document getTaskByID(int docID) throws _Exception {
		try {
			Task doc = dataBase.getTasks().getTaskByID(docID, userGroups, userID);
			_Task _task = new _Task(doc, userID);
			return _task;
		} catch (DocumentAccessException e) {
			throw new _Exception(e.getMessage());
		}
	}

	@Deprecated
	public _Document getExecutionByID(int docID) throws _Exception {
		try {
			Execution doc = dataBase.getExecutions().getExecutionByID(docID, userGroups, userID);
			_Execution _execution = new _Execution(doc, userID);
			return _execution;
		} catch (DocumentAccessException e) {
			throw new _Exception(e.getMessage());
		}
	}

	/**
	 * @deprecated
	 **/
	@Deprecated
	public _Document getTaskByID(int docID, String userName) throws DocumentException, DocumentAccessException {
		AppEnv.logger.errorLogEntry(
				"Groovy - _Database.getTaskByID(int docID, String userName) is obsolete method. Use _Database.getTaskByID(int docID)");
		return null;
	}

	public _Project getProjectByID(int docID) throws _Exception {
		try {
			Project prj = dataBase.getProjects().getProjectByID(docID, userGroups, userID);
			_Project _prj = new _Project(prj, userID);
			return _prj;
		} catch (DocumentAccessException e) {
			throw new _Exception(e.getMessage());
		}
	}

	/**
	 * @deprecated
	 **/
	@Deprecated
	public _Project getProjectByID(int docID, String userName) throws DocumentException, DocumentAccessException {
		AppEnv.logger.errorLogEntry(
				"Groovy - _Database.getProjectByID(int docID, String userName) is obsolete method. Use _Database.getProjectByID(int docID)");
		return null;
	}

	@Deprecated
	public _Glossary getGlossaryDocument(int docID) throws DocumentException {
		IGlossaries glossaries = dataBase.getGlossaries();
		Glossary glos = glossaries.getGlossaryDocumentByID(docID, true, userGroups, userID);
		if (glos != null) {
			_Glossary _glos = new _Glossary(glos, Const.sysUser);
			return _glos;
		} else {
			return null;
		}

	}

	public void setTopic(int topicID, int parentDocID, int parentDocType) {
		dataBase.setTopic(topicID, parentDocID, parentDocType);
	}

	@Deprecated
	public _Document getGlossaryDocumentByField(String condition, String returnFieldName) throws DocumentException {
		AppEnv.logger.errorLogEntry("Groovy - Obsolete method");
		return null;
	}

	/**
	 * @deprecated
	 **/
	@Deprecated
	public _Glossary getGlossaryDocumentByField(String form, String condition, String returnFieldName)
			throws DocumentException {
		ScriptProcessor.logger.warningLogEntry(
				"method: _Database.getGlossaryDocumentByField(" + form + "," + condition + "," + returnFieldName
						+ ") deprecated, use _Database.getGlossaryDocument(String form, String condition)");
		FormulaBlocks blocks = new FormulaBlocks(condition, QueryType.DOCUMENT);
		IQueryFormula queryFormula = dataBase.getQueryFormula("", blocks);
		IGlossaries glos = dataBase.getGlossaries();
		ArrayList<Glossary> docs = glos.getGlossaryByCondition(queryFormula, 0, 1);
		Glossary doc = docs.get(0);
		_Glossary _glos = new _Glossary(doc, Const.sysUser);
		return _glos;
	}

	public Document getTopicByPostID(BaseDocument post, Set<String> complexUserID, String absoluteUserID)
			throws DocumentAccessException {
		return this.dataBase.getForum().getTopicByPostID(post, complexUserID, absoluteUserID);
	}

	public _Glossary getGlossaryDocument(String form, String condition)
			throws DocumentException, QueryFormulaParserException {
		FormulaBlocks blocks = new FormulaBlocks(condition, QueryType.GLOSSARY);
		IQueryFormula queryFormula = dataBase.getQueryFormula("_Database", blocks);
		IGlossaries glos = dataBase.getGlossaries();
		ArrayList<Glossary> docs = glos.getGlossaryByCondition(queryFormula, 0, 1);
		if (docs.size() > 0) {
			Glossary doc = docs.get(0);
			return new _Glossary(doc, session);
		} else {
			return null;
		}
	}

	@Deprecated
	public List<_Glossary> getGlossaryDocs(String condition, int pageNum)
			throws DocumentException, QueryFormulaParserException {
		FormulaBlocks blocks = new FormulaBlocks(condition, QueryType.GLOSSARY);
		blocks.setSortByBlock(new SortByBlock("ddbid"));
		IQueryFormula queryFormula = dataBase.getQueryFormula("_Database", blocks);
		IGlossaries glos = dataBase.getGlossaries();
		int pageSize = user.getSession().pageSize;
		ArrayList<Glossary> docs = glos.getGlossaryByCondition(queryFormula, (pageNum - 1) * pageSize, pageSize);
		ArrayList<_Glossary> documents = new ArrayList<_Glossary>();
		for (Glossary g : docs) {
			documents.add(new _Glossary(g, session));
		}
		return documents;
	}

	@Deprecated
	public List<_Glossary> getGlossaryDocs(String form, String condition)
			throws DocumentException, QueryFormulaParserException {
		FormulaBlocks blocks = new FormulaBlocks(condition, QueryType.GLOSSARY);
		IQueryFormula queryFormula = dataBase.getQueryFormula("_Database", blocks);
		IGlossaries glos = dataBase.getGlossaries();
		ArrayList<Glossary> docs = glos.getGlossaryByCondition(queryFormula, 0, 0);
		ArrayList<_Glossary> documents = new ArrayList<_Glossary>();
		for (Glossary g : docs) {
			documents.add(new _Glossary(g, session));
		}
		return documents;
	}

	@Deprecated
	public int getGlossaryDocsCount(String condition) throws DocumentException, QueryFormulaParserException {
		FormulaBlocks blocks = new FormulaBlocks(condition, QueryType.GLOSSARY);
		IQueryFormula queryFormula = dataBase.getQueryFormula("_Database", blocks);
		IGlossaries glos = dataBase.getGlossaries();
		int count = glos.getGlossaryByConditionCount(queryFormula);
		return count;
	}

	@Deprecated
	public List<_Glossary> getAllGlossaries(int offset, int pageSize, String[] fields) {
		IGlossaries glos = dataBase.getGlossaries();
		ArrayList<BaseDocument> docs = glos.getAllGlossaryDocuments(offset, pageSize, fields, false);
		ArrayList<_Glossary> documents = new ArrayList<_Glossary>();
		for (BaseDocument g : docs) {
			documents.add(new _Glossary((Glossary) g, Const.sysUser));
		}
		return documents;
	}

	// DocumentCollection responses = getGlossaryDescendants(docID,
	// DOCTYPE_GLOSSARY,
	// doc.toExpand,1, complexUserID, absoluteUserID);

	@Override
	public String toString() {
		return "database=" + dataBase.toString() + ", user=" + userID;
	}

	class RegNum implements IQuerySaveTransaction {

		String key;
		int num;

		public int getRegNumber(String key) {
			this.key = key;
			num = dataBase.getRegNum(key);
			return num;
		}

		@Override
		public void post() {
			dataBase.postRegNum(num, key);

		}

	}

	public StringBuffer getStatisticsByAllObjects() {
		return dataBase.getProjects().getStatisticsByAllObjects();
	}

	public StringBuffer getStatisticsByObject(int objectID) {
		return dataBase.getProjects().getStatisticsByObject(objectID);
	}

	public StringBuffer getStatisticByContragent(int objectID) {
		return dataBase.getProjects().getStatisticByContragent(objectID);
	}

	public StringBuffer getStatisticByContragentByProject(int contragentID, int projectID) {
		return dataBase.getProjects().getStatisticByContragentByProject(contragentID, projectID);
	}

	public IDatabase getBaseObject() {
		return dataBase;
	}

	public EntityManagerFactory getEntityManagerFactory() {
		// TODO Auto-generated method stub
		return dataBase.getEntityManagerFactory();
	}
}
