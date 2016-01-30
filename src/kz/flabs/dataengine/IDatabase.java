package kz.flabs.dataengine;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.fileupload.FileItem;

import kz.flabs.appenv.AppEnv;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.parser.SortByBlock;
import kz.flabs.runtimeobj.DocumentCollection;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.viewentry.IViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntry;
import kz.flabs.servlets.sitefiles.UploadedFile;
import kz.flabs.users.RunTimeParameters;
import kz.flabs.users.User;
import kz.flabs.util.XMLResponse;
import kz.flabs.webrule.Role;
import kz.flabs.webrule.constants.TagPublicationFormatType;
import kz.flabs.webrule.module.ExternalModuleType;
import kz.nextbase.script._ViewEntryCollection;
import kz.nextbase.script.constants._ReadConditionType;

public interface IDatabase {

	int getVersion();

	AppEnv getParent();

	String getDbID();

	boolean clearDocuments();

	IStructure getStructure();

	String initExternalPool(ExternalModuleType extModule);

	IGlossaries getGlossaries();

	IDBConnectionPool getConnectionPool();

	IDBConnectionPool getStructureConnectionPool();

	IFTIndexEngine getFTSearchEngine();

	ITasks getTasks();

	IExecutions getExecutions();

	IProjects getProjects();

	IUsersActivity getUserActivity();

	IActivity getActivity();

	IMyDocsProcessor getMyDocsProcessor();

	IHelp getHelp();

	IQueryFormula getQueryFormula(String id, FormulaBlocks blocks);

	IFilters getFilters();

	IForum getForum();

	HashMap<String, Role> getAppRoles();

	int shutdown();

	int parseFile(File parentDir, File dir, HashMap<Integer, Integer> linkOldNew);

	void fillAccessRelatedField(Connection conn, String tableSuffix, int docID, Document doc) throws SQLException;

	void fillBlobs(Connection conn, BaseDocument doc, String tableSuffix) throws SQLException;

	void insertToAccessTables(Connection conn, String tableSuffix, int docID, Document doc);

	void insertBlobTables(Connection conn, int id, int key, Document doc, String tableSuffix)
			throws SQLException, IOException;

	void updateBlobTables(Connection conn, Document doc, String tableSuffix) throws SQLException, IOException;

	void updateAccessTables(Connection conn, Document doc, String tableSuffix) throws SQLException;

	ArrayList<UploadedFile> insertBlobTables(List<FileItem> fileItems) throws SQLException, IOException;

	ArrayList<BaseDocument> getDocumentsForMonth(HashSet<String> userGroups, String userID, String form,
			String fieldName, int month, int offset, int pageSize);

	ArrayList<BaseDocument> getResponses(int docID, int docType, Set<String> complexUserID, String absoluteUserID)
			throws DocumentAccessException, DocumentException, ComplexObjectException;

	@Deprecated
	ArrayList<BaseDocument> getAllDocuments(int docType, Set<String> complexUserID, String absoluteUserID,
			String[] fields, int offset, int pageSize)
					throws DocumentException, DocumentAccessException, ComplexObjectException;

	@Deprecated
	ArrayList<BaseDocument> getAllDocuments(int docType, Set<String> complexUserID, String absoluteUserID, int offset,
			int pageSize) throws DocumentException, DocumentAccessException, ComplexObjectException;

	@Deprecated
	int getAllDocumentsCount(int docType, Set<String> complexUserID, String absoluteUserID);

	@Deprecated
	int getDocsCountByCondition(IQueryFormula condition, Set<String> complexUserID, String absoluteUserID);

	@Deprecated
	StringBuffer getDocsByCondition(IQueryFormula condition, Set<String> complexUserID, String absoluteUserID,
			int offset, int pageSize, String fieldCond, Set<DocID> toExpandResponses, Set<String> toExpandCategory,
			TagPublicationFormatType publishAs, int page) throws DocumentException;

	@Deprecated
	StringBuffer getDocsByCondition(String sql, Set<String> complexUserID, String absoluteUserID, String fieldCond,
			Set<DocID> toExpandResponses, Set<String> toExpandCategory, TagPublicationFormatType publishAs, int page)
					throws DocumentException;

	ArrayList<BaseDocument> getDocumentsByCondition(IQueryFormula condition, Set<String> complexUserID,
			String absoluteUserID, int limit, int offset)
					throws DocumentException, DocumentAccessException, ComplexObjectException;

	@Deprecated
	ArrayList<BaseDocument> getDocumentsByCondition(String form, String query, Set<String> complexUserID,
			String absoluteUserID) throws DocumentException, DocumentAccessException, QueryFormulaParserException,
					ComplexObjectException;

	@Deprecated
	ArrayList<BaseDocument> getDocumentsByCondition(String query, Set<String> complexUserID, String absoluteUserID,
			int limit, int offset) throws DocumentException, DocumentAccessException, QueryFormulaParserException,
					ComplexObjectException;

	@Deprecated
	int getDocumentsCountByCondition(String query, Set<String> complexUserID, String absoluteUserID)
			throws DocumentException, DocumentAccessException, QueryFormulaParserException;

	@Deprecated
	DocumentCollection getDescendants(int docID, int docType, SortByBlock sortBlock, int level,
			Set<String> complexUserID, String absoluteUserID);

	@Deprecated
	boolean hasResponse(int docID, int docType, Set<String> complexUserID, String absoluteUserID);

	@Deprecated
	boolean hasResponse(Connection conn, int docID, int docType, Set<String> complexUserID, String absoluteUserID);

	@Deprecated
	BaseDocument getDocumentByComplexID(int docType, int docID, Set<String> complexUserID, String absoluteUserID)
			throws DocumentAccessException, DocumentException, ComplexObjectException;

	@Deprecated
	BaseDocument getDocumentByComplexID(int docType, int docID)
			throws DocumentException, DocumentAccessException, ComplexObjectException;

	BaseDocument getDocumentByDdbID(String ddbID, Set<String> complexUserID, String absoluteUserID)
			throws DocumentException, DocumentAccessException, ComplexObjectException;

	IViewEntry getDocumentByDocID(String docID, Set<String> complexUserID, String absoluteUserID)
			throws DocumentException, DocumentAccessException;

	boolean hasDocumentByComplexID(int docID, int docType);

	void deleteDocument(int docType, int docID, User user, boolean completely)
			throws DocumentException, DocumentAccessException, SQLException, DatabasePoolException,
			InstantiationException, IllegalAccessException, ClassNotFoundException, ComplexObjectException;

	void deleteDocument(String id, boolean completely, User user)
			throws DocumentException, DocumentAccessException, SQLException, DatabasePoolException,
			InstantiationException, IllegalAccessException, ClassNotFoundException, ComplexObjectException;

	XMLResponse deleteDocuments(List<DocID> docID, boolean completely, User user);

	boolean unDeleteDocument(String id, User user) throws DocumentAccessException, ComplexObjectException;

	boolean unDeleteDocument(int aid, User user)
			throws DocumentException, DocumentAccessException, ComplexObjectException;

	XMLResponse unDeleteDocuments(List<DocID> docID, User user);

	Document getMainDocumentByID(int docID, Set<String> complexUserID, String absoluteUserID)
			throws DocumentAccessException, DocumentException, ComplexObjectException;

	int insertMainDocument(Document doc, User user) throws DocumentException;

	int updateMainDocument(Document doc, User user)
			throws DocumentAccessException, DocumentException, ComplexObjectException;

	int getRegNum(String key);

	int postRegNum(int num, String key);

	StringBuffer getCounters();

	StringBuffer getPatches();

	@Deprecated
	String getFieldByComplexID(int docID, int docType, String fieldName);

	String getDocumentAttach(int docID, int docType, Set<String> complexUserID, String fieldName, String fileName);

	String getDocumentAttach(int docID, int docType, String fieldName, String fileName);

	@Deprecated
	int randomBinary();

	@Deprecated
	ArrayList<Integer> getAllDocumentsIDS(int docType, Set<String> complexUserID, String absoluteUserID,
			String[] fields, int offset, int pageSize) throws DocumentException, DocumentAccessException;

	String removeDocumentFromRecycleBin(int id);

	@Deprecated
	ArrayList<Integer> getAllDocumentsIDS(int docType, Set<String> complexUserID, String absoluteUserID, int start,
			int end) throws DocumentException, DocumentAccessException;

	@Deprecated
	ArrayList<Integer> getAllDocumentsIDsByCondition(String query, int docType, Set<String> complexUserID,
			String absoluteUserID);

	@Deprecated
	String getMainDocumentFieldValueByID(int docID, Set<String> complexUserID, String absoluteUserID, String fieldName)
			throws DocumentAccessException;

	@Deprecated
	String getGlossaryCustomFieldValueByID(int docID, String fieldName);

	StringBuffer getUsersRecycleBin(int offset, int pageSize, String userID);

	ArrayList<BaseDocument> getDescendantsArray(int docID, int docType, DocID[] toExpand, int level,
			Set<String> complexUserID, String absoluteUserID) throws DocumentException, ComplexObjectException;

	@Deprecated
	String getDocumentEntry(Connection conn, Set<String> complexUserID, String absoluteUserID, ResultSet rs,
			String fieldsCond, Set<DocID> toExpandResponses, int page) throws SQLException, DocumentException;

	@Deprecated
	int getDocsCountByCondition(String sql, Set<String> complexUserID, String absoluteUserID);

	DocumentCollection getDiscussion(int docID, int docType, DocID[] toExpand, int level, Set<String> complexUserID,
			String absoluteUserID);

	void setTopic(int topicID, int parentdocID, int parentDocType);

	DatabaseType getRDBMSType();

	int calcStartEntry(int pageNum, int pageSize);

	int getUsersRecycleBinCount(int calcStartEntry, int pageSize, String userID);

	int getFavoritesCount(Set<String> complexUserID, String absoluteUserID);

	StringBuffer getFavorites(IQueryFormula nf, Set<String> complexUserID, String absoluteUserID, int offset,
			int pageSize, String fieldsCond, Set<DocID> toExpandResponses, Set<String> toExpandCategory,
			TagPublicationFormatType publishAs, int page);

	int isFavourites(Connection conn, int docID, int docType, Employer user);

	int isFavourites(int docID, int docType, String userName);

	void addCounter(String key, int num);

	_ViewEntryCollection getCollectionByCondition(ISelectFormula condition, User user, int pageNum, int pageSize,
			Set<DocID> toExpandResponses, RunTimeParameters parameters, boolean checkResponse,
			boolean expandAllResponses, boolean checkUnread);

	_ViewEntryCollection getCollectionByCondition(ISelectFormula condition, User user, int pageNum, int pageSize,
			Set<DocID> toExpandResponses, RunTimeParameters parameters, boolean checkResponse,
			boolean expandAllResponses, _ReadConditionType type);

	_ViewEntryCollection getCollectionByCondition(ISelectFormula condition, User user, int pageNum, int pageSize,
			Set<DocID> toExpandResponses, RunTimeParameters parameters, boolean checkResponse,
			boolean expandAllResponses, _ReadConditionType type, String customFieldName);

	_ViewEntryCollection getCollectionByCondition(ISelectFormula condition, User user, int pageNum, int pageSize,
			Set<DocID> toExpandResponses, RunTimeParameters parameters, boolean checkResponse,
			boolean expandAllResponses);

	_ViewEntryCollection getCollectionByCondition(ISelectFormula sf, User user, int pageNum, int pageSize,
			Set<DocID> toExpandResponses, RunTimeParameters parameters, boolean checkResponse);

	ArrayList<ViewEntry> getGroupedEntries(String fieldName, int offset, int pageSize, User user);

	_ViewEntryCollection getCollectionByCondition(ISelectFormula sf, User user, int pageNum, int pageSize,
			Set<DocID> expandedDocuments, RunTimeParameters parameters, boolean checkResponse,
			String responseQueryCondition);

	DocumentCollection getDescendants(int docID, int docType, SortByBlock sortBlock, int level,
			Set<String> complexUserID, String absoluteUserID, String responseQueryCondition);

	void removeUnrelatedAttachments();

	ISelectFormula getSelectFormula(FormulaBlocks fb);

	ISelectFormula getForumSelectFormula(FormulaBlocks queryFormulaBlocks);

	EntityManagerFactory getEntityManagerFactory();
}
