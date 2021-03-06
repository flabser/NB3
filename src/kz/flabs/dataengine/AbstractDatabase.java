package kz.flabs.dataengine;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.DocumentCollection;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.users.User;
import kz.flabs.util.PageResponse;
import kz.flabs.webrule.constants.TagPublicationFormatType;
import kz.lof.appenv.AppEnv;

public abstract class AbstractDatabase implements IDatabase {
	protected AppEnv env;
	protected String dbID;

	public boolean clearDocuments() {
		return false;
	}

	@Override
	public IDBConnectionPool getConnectionPool() {
		return null;
	}

	@Override
	public IFTIndexEngine getFTSearchEngine() {

		return null;
	}

	public int shutdown() {

		return 0;
	}

	public void fillAccessRelatedField(Connection conn, String tableSuffix, int docID, Document doc) throws SQLException {

	}

	public void fillBlobs(Connection conn, BaseDocument doc, String tableSuffix) throws SQLException {

	}

	public void insertToAccessTables(Connection conn, String tableSuffix, int docID, Document doc) {

	}

	public void insertBlobTables(Connection conn, int id, int key, Document doc, String tableSuffix) throws SQLException, IOException {

	}

	public void updateBlobTables(Connection conn, Document doc, String tableSuffix) throws SQLException, IOException {

	}

	public void updateAccessTables(Connection conn, Document doc, String tableSuffix) throws SQLException {

	}

	public ArrayList<BaseDocument> getDocumentsForMonth(HashSet<String> userGroups, String userID, String form, String fieldName, int month,
	        int offset, int pageSize) {

		return null;
	}

	public ArrayList<BaseDocument> getResponses(int docID, int docType, Set<String> complexUserID, String absoluteUserID) {

		return null;
	}

	public ArrayList<BaseDocument> getAllDocuments(int docType, Set<String> complexUserID, String absoluteUserID, String[] fields, int offset,
	        int pageSize) throws DocumentException {

		return null;
	}

	public ArrayList<BaseDocument> getAllDocuments(int docType, Set<String> complexUserID, String absoluteUserID, int offset, int pageSize)
	        throws DocumentException {

		return null;
	}

	public int getAllDocumentsCount(int docType, Set<String> complexUserID, String absoluteUserID) {

		return 0;
	}

	public StringBuffer getDocsByCondition(String sql, Set<String> complexUserID, String absoluteUserID, String fieldCond,
	        Set<DocID> toExpandResponses, Set<String> toExpandCategory, TagPublicationFormatType publishAs, int page) throws DocumentException {

		return null;
	}

	public ArrayList<BaseDocument> getDocumentsByCondition(String form, String query, Set<String> complexUserID, String absoluteUserID)
	        throws DocumentException {

		return null;
	}

	public ArrayList<BaseDocument> getDocumentsByCondition(String query, Set<String> complexUserID, String absoluteUserID, int limit, int offset)
	        throws DocumentException {

		return null;
	}

	public int getDocumentsCountByCondition(String query, Set<String> complexUserID, String absoluteUserID) throws DocumentException {

		return 0;
	}

	public boolean hasResponse(int docID, int docType, Set<String> complexUserID, String absoluteUserID) {

		return false;
	}

	public boolean hasResponse(Connection conn, int docID, int docType, Set<String> complexUserID, String absoluteUserID) {

		return false;
	}

	public BaseDocument getDocumentByComplexID(int docType, int docID, Set<String> complexUserID, String absoluteUserID) {

		return null;
	}

	public BaseDocument getDocumentByComplexID(int docType, int docID) throws DocumentException {

		return null;
	}

	public BaseDocument getDocumentByDdbID(String ddbID, Set<String> complexUserID, String absoluteUserID) throws DocumentException {

		return null;
	}

	public boolean hasDocumentByComplexID(int docID, int docType) {

		return false;
	}

	public void deleteDocument(int docType, int docID, User user, boolean completely) throws DocumentException, SQLException, DatabasePoolException,
	        InstantiationException, IllegalAccessException, ClassNotFoundException {

	}

	public void deleteDocument(String id, boolean completely, User user) throws DocumentException, SQLException, DatabasePoolException,
	        InstantiationException, IllegalAccessException, ClassNotFoundException {

	}

	public PageResponse deleteDocuments(List<DocID> docID, boolean completely, User user) {

		return null;
	}

	public boolean unDeleteDocument(String id, User user) {

		return false;
	}

	public boolean unDeleteDocument(int aid, User user) throws DocumentException {

		return false;
	}

	public PageResponse unDeleteDocuments(List<DocID> docID, User user) {

		return null;
	}

	public Document getMainDocumentByID(int docID, Set<String> complexUserID, String absoluteUserID) {

		return null;
	}

	public int insertMainDocument(Document doc, User user) throws DocumentException {

		return 0;
	}

	public int updateMainDocument(Document doc, User user) {

		return 0;
	}

	public int getRegNum(String key) {

		return 0;
	}

	public int postRegNum(int num, String key) {

		return 0;
	}

	public StringBuffer getCounters() {

		return null;
	}

	public StringBuffer getPatches() {

		return null;
	}

	public String getFieldByComplexID(int docID, int docType, String fieldName) {

		return null;
	}

	public String getDocumentAttach(int docID, int docType, Set<String> complexUserID, String fieldName, String fileName) {

		return null;
	}

	public int randomBinary() {

		return 0;
	}

	public ArrayList<Integer> getAllDocumentsIDS(int docType, Set<String> complexUserID, String absoluteUserID, String[] fields, int offset,
	        int pageSize) throws DocumentException {

		return null;
	}

	public ArrayList<Integer> getAllDocumentsIDS(int docType, Set<String> complexUserID, String absoluteUserID, int start, int end)
	        throws DocumentException {

		return null;
	}

	public ArrayList<Integer> getAllDocumentsIDsByCondition(String query, int docType, Set<String> complexUserID, String absoluteUserID) {

		return null;
	}

	public String getMainDocumentFieldValueByID(int docID, Set<String> complexUserID, String absoluteUserID, String fieldName) {

		return null;
	}

	public String getGlossaryCustomFieldValueByID(int docID, String fieldName) {

		return null;
	}

	public StringBuffer getUsersRecycleBin(int offset, int pageSize, String userID) {

		return null;
	}

	public ArrayList<BaseDocument> getDescendantsArray(int docID, int docType, DocID[] toExpand, int level, Set<String> complexUserID,
	        String absoluteUserID) throws DocumentException {

		return null;
	}

	public String getDocumentEntry(Connection conn, Set<String> complexUserID, String absoluteUserID, ResultSet rs, String fieldsCond,
	        Set<DocID> toExpandResponses, int page) throws SQLException, DocumentException {

		return null;
	}

	public int getDocsCountByCondition(String sql, Set<String> complexUserID, String absoluteUserID) {

		return 0;
	}

	public DocumentCollection getDiscussion(int docID, int docType, DocID[] toExpand, int level, Set<String> complexUserID, String absoluteUserID) {

		return null;
	}

	public void setTopic(int topicID, int parentdocID, int parentDocType) {

	}

	public int calcStartEntry(int pageNum, int pageSize) {

		return 0;
	}

	public int getUsersRecycleBinCount(int calcStartEntry, int pageSize, String userID) {

		return 0;
	}

	public int getFavoritesCount(Set<String> complexUserID, String absoluteUserID) {

		return 0;
	}

	public void addCounter(String key, int num) {

	}

}
