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
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.viewentry.IViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntry;
import kz.flabs.users.RunTimeParameters;
import kz.flabs.users.User;
import kz.flabs.util.XMLResponse;
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

	@Override
	public boolean clearDocuments() {
		return false;
	}

	@Override
	public abstract IStructure getStructure();

	@Override
	public String initExternalPool(ExternalModuleType extModule) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
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

	@Override
	public ITasks getTasks() {
		return null;
	}

	@Override
	public IExecutions getExecutions() {
		return null;
	}

	@Override
	public IProjects getProjects() {
		return null;
	}

	@Override
	public IUsersActivity getUserActivity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IActivity getActivity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMyDocsProcessor getMyDocsProcessor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IHelp getHelp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IQueryFormula getQueryFormula(String id, FormulaBlocks blocks) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IFilters getFilters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IForum getForum() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, Role> getAppRoles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int shutdown() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void fillAccessRelatedField(Connection conn, String tableSuffix,
			int docID, Document doc) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fillBlobs(Connection conn, BaseDocument doc, String tableSuffix)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertToAccessTables(Connection conn, String tableSuffix,
			int docID, Document doc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertBlobTables(Connection conn, int id, int key,
			Document doc, String tableSuffix) throws SQLException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateBlobTables(Connection conn, Document doc,
			String tableSuffix) throws SQLException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateAccessTables(Connection conn, Document doc,
			String tableSuffix) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<BaseDocument> getDocumentsForMonth(
			HashSet<String> userGroups, String userID, String form,
			String fieldName, int month, int offset, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<BaseDocument> getResponses(int docID, int docType,
			Set<String> complexUserID, String absoluteUserID)
			throws DocumentAccessException, DocumentException,
			ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<BaseDocument> getAllDocuments(int docType,
			Set<String> complexUserID, String absoluteUserID, String[] fields,
			int offset, int pageSize) throws DocumentException,
			DocumentAccessException, ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<BaseDocument> getAllDocuments(int docType,
			Set<String> complexUserID, String absoluteUserID, int offset,
			int pageSize) throws DocumentException, DocumentAccessException,
			ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getAllDocumentsCount(int docType, Set<String> complexUserID,
			String absoluteUserID) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDocsCountByCondition(IQueryFormula condition,
			Set<String> complexUserID, String absoluteUserID) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public StringBuffer getDocsByCondition(IQueryFormula condition,
			Set<String> complexUserID, String absoluteUserID, int offset,
			int pageSize, String fieldCond, Set<DocID> toExpandResponses,
			Set<String> toExpandCategory, TagPublicationFormatType publishAs,
			int page) throws DocumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StringBuffer getDocsByCondition(String sql,
			Set<String> complexUserID, String absoluteUserID, String fieldCond,
			Set<DocID> toExpandResponses, Set<String> toExpandCategory,
			TagPublicationFormatType publishAs, int page)
			throws DocumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<BaseDocument> getDocumentsByCondition(
			IQueryFormula condition, Set<String> complexUserID,
			String absoluteUserID, int limit, int offset)
			throws DocumentException, DocumentAccessException,
			ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<BaseDocument> getDocumentsByCondition(String form,
			String query, Set<String> complexUserID, String absoluteUserID)
			throws DocumentException, DocumentAccessException,
			QueryFormulaParserException, ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<BaseDocument> getDocumentsByCondition(String query,
			Set<String> complexUserID, String absoluteUserID, int limit,
			int offset) throws DocumentException, DocumentAccessException,
			QueryFormulaParserException, ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDocumentsCountByCondition(String query,
			Set<String> complexUserID, String absoluteUserID)
			throws DocumentException, DocumentAccessException,
			QueryFormulaParserException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DocumentCollection getDescendants(int docID, int docType,
			SortByBlock sortBlock, int level, Set<String> complexUserID,
			String absoluteUserID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasResponse(int docID, int docType,
			Set<String> complexUserID, String absoluteUserID) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasResponse(Connection conn, int docID, int docType,
			Set<String> complexUserID, String absoluteUserID) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BaseDocument getDocumentByComplexID(int docType, int docID,
			Set<String> complexUserID, String absoluteUserID)
			throws DocumentAccessException, DocumentException,
			ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseDocument getDocumentByComplexID(int docType, int docID)
			throws DocumentException, DocumentAccessException,
			ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseDocument getDocumentByDdbID(String ddbID,
			Set<String> complexUserID, String absoluteUserID)
			throws DocumentException, DocumentAccessException,
			ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IViewEntry getDocumentByDocID(String docID,
			Set<String> complexUserID, String absoluteUserID)
			throws DocumentException, DocumentAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasDocumentByComplexID(int docID, int docType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void deleteDocument(int docType, int docID,
                               User user, boolean completely)
			throws DocumentException, DocumentAccessException, SQLException,
			DatabasePoolException, InstantiationException,
			IllegalAccessException, ClassNotFoundException,
			ComplexObjectException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteDocument(String id,
                               boolean completely, User user)
			throws DocumentException, DocumentAccessException, SQLException,
			DatabasePoolException, InstantiationException,
			IllegalAccessException, ClassNotFoundException,
			ComplexObjectException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public XMLResponse deleteDocuments(List<DocID> docID,
                                       boolean completely, User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean unDeleteDocument(String id, User user) throws DocumentAccessException,
			ComplexObjectException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unDeleteDocument(int aid, User user) throws DocumentException,
			DocumentAccessException, ComplexObjectException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public XMLResponse unDeleteDocuments(List<DocID> docID, User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Document getMainDocumentByID(int docID, Set<String> complexUserID,
			String absoluteUserID) throws DocumentAccessException,
			DocumentException, ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int insertMainDocument(Document doc, User user) throws DocumentException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateMainDocument(Document doc, User user) throws DocumentAccessException,
			DocumentException, ComplexObjectException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRegNum(String key) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int postRegNum(int num, String key) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public StringBuffer getCounters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StringBuffer getPatches() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFieldByComplexID(int docID, int docType, String fieldName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocumentAttach(int docID, int docType,
			Set<String> complexUserID, String fieldName, String fileName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int randomBinary() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ArrayList<Integer> getAllDocumentsIDS(int docType,
			Set<String> complexUserID, String absoluteUserID, String[] fields,
			int offset, int pageSize) throws DocumentException,
			DocumentAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Integer> getAllDocumentsIDS(int docType,
			Set<String> complexUserID, String absoluteUserID, int start, int end)
			throws DocumentException, DocumentAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Integer> getAllDocumentsIDsByCondition(String query,
			int docType, Set<String> complexUserID, String absoluteUserID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMainDocumentFieldValueByID(int docID,
			Set<String> complexUserID, String absoluteUserID, String fieldName)
			throws DocumentAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getGlossaryCustomFieldValueByID(int docID, String fieldName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StringBuffer getUsersRecycleBin(int offset, int pageSize,
			String userID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<BaseDocument> getDescendantsArray(int docID, int docType,
			DocID[] toExpand, int level, Set<String> complexUserID,
			String absoluteUserID) throws DocumentException,
			ComplexObjectException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocumentEntry(Connection conn, Set<String> complexUserID,
			String absoluteUserID, ResultSet rs, String fieldsCond,
			Set<DocID> toExpandResponses, int page) throws SQLException,
			DocumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDocsCountByCondition(String sql, Set<String> complexUserID,
			String absoluteUserID) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DocumentCollection getDiscussion(int docID, int docType,
			DocID[] toExpand, int level, Set<String> complexUserID,
			String absoluteUserID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTopic(int topicID, int parentdocID, int parentDocType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DatabaseType getRDBMSType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int calcStartEntry(int pageNum, int pageSize) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getUsersRecycleBinCount(int calcStartEntry, int pageSize,
			String userID) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFavoritesCount(Set<String> complexUserID,
			String absoluteUserID) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public StringBuffer getFavorites(IQueryFormula nf,
			Set<String> complexUserID, String absoluteUserID, int offset,
			int pageSize, String fieldsCond, Set<DocID> toExpandResponses,
			Set<String> toExpandCategory, TagPublicationFormatType publishAs,
			int page) {
		return null;
	}

	@Override
	public int isFavourites(Connection conn, int docID, int docType, Employer userName) {
		return 0;
	}

	@Override
	public int isFavourites(int docID, int docType, String userName) {
		return 0;
	}

	@Override
	public void addCounter(String key, int num) {
		
	}

	@Override
	public _ViewEntryCollection getCollectionByCondition(
			ISelectFormula condition, User user, int pageNum, int pageSize,
			Set<DocID> toExpandResponses, RunTimeParameters parameters,
			boolean checkResponse, boolean expandAllResponses) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public _ViewEntryCollection getCollectionByCondition(ISelectFormula sf,
			User user, int pageNum, int pageSize, Set<DocID> toExpandResponses,
			RunTimeParameters parameters, boolean checkResponse) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<ViewEntry> getGroupedEntries(String fieldName, int offset,
			int pageSize, User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public _ViewEntryCollection getCollectionByCondition(ISelectFormula sf,
			User user, int pageNum, int pageSize, Set<DocID> expandedDocuments,
			RunTimeParameters parameters, boolean checkResponse,
			String responseQueryCondition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DocumentCollection getDescendants(int docID, int docType,
			SortByBlock sortBlock, int level, Set<String> complexUserID,
			String absoluteUserID, String responseQueryCondition) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
