package kz.flabs.dataengine;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import kz.flabs.runtimeobj.viewentry.IViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntry;
import kz.flabs.users.RunTimeParameters;
import kz.flabs.users.User;
import kz.flabs.util.PageResponse;
import kz.flabs.webrule.Role;
import kz.flabs.webrule.constants.TagPublicationFormatType;
import kz.flabs.webrule.module.ExternalModuleType;
import kz.nextbase.script._ViewEntryCollection;

public abstract class AbstractDatabase implements IDatabase {
	protected AppEnv env;
	protected String dbID;

	@Override
	public int getVersion() {
		return 0;
	}

	@Override
	public AppEnv getParent() {
		return env;
	}

	@Override
	public String getDbID() {
		return dbID;
	}

	public boolean clearDocuments() {
		return false;
	}

	@Override
	public abstract IStructure getStructure();

	public String initExternalPool(ExternalModuleType extModule) {
		// TODO Auto-generated method stub
		return null;
	}

	public IGlossaries getGlossaries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDBConnectionPool getConnectionPool() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IFTIndexEngine getFTSearchEngine() {
		// TODO Auto-generated method stub
		return null;
	}

	public IExecutions getExecutions() {
		return null;
	}

	public IProjects getProjects() {
		return null;
	}

	public IUsersActivity getUserActivity() {
		// TODO Auto-generated method stub
		return null;
	}

	public IActivity getActivity() {
		// TODO Auto-generated method stub
		return null;
	}

	public IMyDocsProcessor getMyDocsProcessor() {
		// TODO Auto-generated method stub
		return null;
	}

	public IQueryFormula getQueryFormula(String id, FormulaBlocks blocks) {
		// TODO Auto-generated method stub
		return null;
	}

	public IFilters getFilters() {
		// TODO Auto-generated method stub
		return null;
	}

	public IForum getForum() {
		// TODO Auto-generated method stub
		return null;
	}

	public HashMap<String, Role> getAppRoles() {
		// TODO Auto-generated method stub
		return null;
	}

	public int shutdown() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void fillAccessRelatedField(Connection conn, String tableSuffix, int docID, Document doc) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void fillBlobs(Connection conn, BaseDocument doc, String tableSuffix) throws SQLException {
		// TODO Auto-generated method stub

	}

	public void insertToAccessTables(Connection conn, String tableSuffix, int docID, Document doc) {
		// TODO Auto-generated method stub

	}

	public void insertBlobTables(Connection conn, int id, int key, Document doc, String tableSuffix) throws SQLException, IOException {
		// TODO Auto-generated method stub

	}

	public void updateBlobTables(Connection conn, Document doc, String tableSuffix) throws SQLException, IOException {
		// TODO Auto-generated method stub

	}

	public void updateAccessTables(Connection conn, Document doc, String tableSuffix) throws SQLException {
		// TODO Auto-generated method stub

	}

	public ArrayList<BaseDocument> getDocumentsForMonth(HashSet<String> userGroups, String userID, String form, String fieldName, int month,
	        int offset, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<BaseDocument> getResponses(int docID, int docType, Set<String> complexUserID, String absoluteUserID)
	        throws DocumentAccessException, DocumentException, ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<BaseDocument> getAllDocuments(int docType, Set<String> complexUserID, String absoluteUserID, String[] fields, int offset,
	        int pageSize) throws DocumentException, DocumentAccessException, ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<BaseDocument> getAllDocuments(int docType, Set<String> complexUserID, String absoluteUserID, int offset, int pageSize)
	        throws DocumentException, DocumentAccessException, ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getAllDocumentsCount(int docType, Set<String> complexUserID, String absoluteUserID) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getDocsCountByCondition(IQueryFormula condition, Set<String> complexUserID, String absoluteUserID) {
		// TODO Auto-generated method stub
		return 0;
	}

	public StringBuffer getDocsByCondition(IQueryFormula condition, Set<String> complexUserID, String absoluteUserID, int offset, int pageSize,
	        String fieldCond, Set<DocID> toExpandResponses, Set<String> toExpandCategory, TagPublicationFormatType publishAs, int page)
	        throws DocumentException {
		// TODO Auto-generated method stub
		return null;
	}

	public StringBuffer getDocsByCondition(String sql, Set<String> complexUserID, String absoluteUserID, String fieldCond,
	        Set<DocID> toExpandResponses, Set<String> toExpandCategory, TagPublicationFormatType publishAs, int page) throws DocumentException {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<BaseDocument> getDocumentsByCondition(IQueryFormula condition, Set<String> complexUserID, String absoluteUserID, int limit,
	        int offset) throws DocumentException, DocumentAccessException, ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<BaseDocument> getDocumentsByCondition(String form, String query, Set<String> complexUserID, String absoluteUserID)
	        throws DocumentException, DocumentAccessException, QueryFormulaParserException, ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<BaseDocument> getDocumentsByCondition(String query, Set<String> complexUserID, String absoluteUserID, int limit, int offset)
	        throws DocumentException, DocumentAccessException, QueryFormulaParserException, ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getDocumentsCountByCondition(String query, Set<String> complexUserID, String absoluteUserID) throws DocumentException,
	        DocumentAccessException, QueryFormulaParserException {
		// TODO Auto-generated method stub
		return 0;
	}

	public DocumentCollection getDescendants(int docID, int docType, SortByBlock sortBlock, int level, Set<String> complexUserID,
	        String absoluteUserID) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasResponse(int docID, int docType, Set<String> complexUserID, String absoluteUserID) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasResponse(Connection conn, int docID, int docType, Set<String> complexUserID, String absoluteUserID) {
		// TODO Auto-generated method stub
		return false;
	}

	public BaseDocument getDocumentByComplexID(int docType, int docID, Set<String> complexUserID, String absoluteUserID)
	        throws DocumentAccessException, DocumentException, ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	public BaseDocument getDocumentByComplexID(int docType, int docID) throws DocumentException, DocumentAccessException, ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	public BaseDocument getDocumentByDdbID(String ddbID, Set<String> complexUserID, String absoluteUserID) throws DocumentException,
	        DocumentAccessException, ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	public IViewEntry getDocumentByDocID(String docID, Set<String> complexUserID, String absoluteUserID) throws DocumentException,
	        DocumentAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasDocumentByComplexID(int docID, int docType) {
		// TODO Auto-generated method stub
		return false;
	}

	public void deleteDocument(int docType, int docID, User user, boolean completely) throws DocumentException, DocumentAccessException,
	        SQLException, DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException, ComplexObjectException {
		// TODO Auto-generated method stub

	}

	public void deleteDocument(String id, boolean completely, User user) throws DocumentException, DocumentAccessException, SQLException,
	        DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException, ComplexObjectException {
		// TODO Auto-generated method stub

	}

	public PageResponse deleteDocuments(List<DocID> docID, boolean completely, User user) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean unDeleteDocument(String id, User user) throws DocumentAccessException, ComplexObjectException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean unDeleteDocument(int aid, User user) throws DocumentException, DocumentAccessException, ComplexObjectException {
		// TODO Auto-generated method stub
		return false;
	}

	public PageResponse unDeleteDocuments(List<DocID> docID, User user) {
		// TODO Auto-generated method stub
		return null;
	}

	public Document getMainDocumentByID(int docID, Set<String> complexUserID, String absoluteUserID) throws DocumentAccessException,
	        DocumentException, ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	public int insertMainDocument(Document doc, User user) throws DocumentException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int updateMainDocument(Document doc, User user) throws DocumentAccessException, DocumentException, ComplexObjectException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getRegNum(String key) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int postRegNum(int num, String key) {
		// TODO Auto-generated method stub
		return 0;
	}

	public StringBuffer getCounters() {
		// TODO Auto-generated method stub
		return null;
	}

	public StringBuffer getPatches() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFieldByComplexID(int docID, int docType, String fieldName) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDocumentAttach(int docID, int docType, Set<String> complexUserID, String fieldName, String fileName) {
		// TODO Auto-generated method stub
		return null;
	}

	public int randomBinary() {
		// TODO Auto-generated method stub
		return 0;
	}

	public ArrayList<Integer> getAllDocumentsIDS(int docType, Set<String> complexUserID, String absoluteUserID, String[] fields, int offset,
	        int pageSize) throws DocumentException, DocumentAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Integer> getAllDocumentsIDS(int docType, Set<String> complexUserID, String absoluteUserID, int start, int end)
	        throws DocumentException, DocumentAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Integer> getAllDocumentsIDsByCondition(String query, int docType, Set<String> complexUserID, String absoluteUserID) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMainDocumentFieldValueByID(int docID, Set<String> complexUserID, String absoluteUserID, String fieldName)
	        throws DocumentAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getGlossaryCustomFieldValueByID(int docID, String fieldName) {
		// TODO Auto-generated method stub
		return null;
	}

	public StringBuffer getUsersRecycleBin(int offset, int pageSize, String userID) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<BaseDocument> getDescendantsArray(int docID, int docType, DocID[] toExpand, int level, Set<String> complexUserID,
	        String absoluteUserID) throws DocumentException, ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDocumentEntry(Connection conn, Set<String> complexUserID, String absoluteUserID, ResultSet rs, String fieldsCond,
	        Set<DocID> toExpandResponses, int page) throws SQLException, DocumentException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getDocsCountByCondition(String sql, Set<String> complexUserID, String absoluteUserID) {
		// TODO Auto-generated method stub
		return 0;
	}

	public DocumentCollection getDiscussion(int docID, int docType, DocID[] toExpand, int level, Set<String> complexUserID, String absoluteUserID) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setTopic(int topicID, int parentdocID, int parentDocType) {
		// TODO Auto-generated method stub

	}

	public DatabaseType getRDBMSType() {
		// TODO Auto-generated method stub
		return null;
	}

	public int calcStartEntry(int pageNum, int pageSize) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getUsersRecycleBinCount(int calcStartEntry, int pageSize, String userID) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getFavoritesCount(Set<String> complexUserID, String absoluteUserID) {
		// TODO Auto-generated method stub
		return 0;
	}

	public StringBuffer getFavorites(IQueryFormula nf, Set<String> complexUserID, String absoluteUserID, int offset, int pageSize, String fieldsCond,
	        Set<DocID> toExpandResponses, Set<String> toExpandCategory, TagPublicationFormatType publishAs, int page) {
		return null;
	}

	public void addCounter(String key, int num) {

	}

	public _ViewEntryCollection getCollectionByCondition(ISelectFormula condition, User user, int pageNum, int pageSize,
	        Set<DocID> toExpandResponses, RunTimeParameters parameters, boolean checkResponse, boolean expandAllResponses) {
		// TODO Auto-generated method stub
		return null;
	}

	public _ViewEntryCollection getCollectionByCondition(ISelectFormula sf, User user, int pageNum, int pageSize, Set<DocID> toExpandResponses,
	        RunTimeParameters parameters, boolean checkResponse) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<ViewEntry> getGroupedEntries(String fieldName, int offset, int pageSize, User user) {
		// TODO Auto-generated method stub
		return null;
	}

	public _ViewEntryCollection getCollectionByCondition(ISelectFormula sf, User user, int pageNum, int pageSize, Set<DocID> expandedDocuments,
	        RunTimeParameters parameters, boolean checkResponse, String responseQueryCondition) {
		// TODO Auto-generated method stub
		return null;
	}

	public DocumentCollection getDescendants(int docID, int docType, SortByBlock sortBlock, int level, Set<String> complexUserID,
	        String absoluteUserID, String responseQueryCondition) {
		// TODO Auto-generated method stub
		return null;
	}

}
