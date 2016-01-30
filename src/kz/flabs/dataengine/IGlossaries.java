package kz.flabs.dataengine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.runtimeobj.DocumentCollection;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.glossary.Glossary;
import kz.flabs.users.RunTimeParameters;
import kz.flabs.webrule.constants.TagPublicationFormatType;
import kz.nextbase.script._ViewEntryCollection;

public interface IGlossaries {
	
	IGlossariesTuner getGlossariesTuner();
	
	@Deprecated
	public ArrayList<BaseDocument> getAllGlossaryDocuments(int start, int end, String[] fields, boolean useCache);
	@Deprecated
	public int getGlossaryCount();
	@Deprecated
	public StringBuffer getGlossaryByCondition(IQueryFormula condition, int offset, int pageSize, String fieldsCond, Set<String> toExpand, TagPublicationFormatType publishAs) throws DocumentException, QueryException;
	@Deprecated
	public ArrayList<Glossary> getGlossaryByCondition(IQueryFormula condition, int offset, int pageSize);
	@Deprecated
	public int getGlossaryByConditionCount(IQueryFormula condition);
	@Deprecated
	public Glossary getGlossaryDocumentByID(int docID, boolean useCache, Set<String> complexUserID, String absoluteUserID);
	@Deprecated
	public Glossary getGlossaryDocumentByID(int docID);
	public Glossary getGlossaryDocumentByID(String ddbID);
	public int insertGlossaryDocument(Glossary doc) throws DocumentException;
	public int updateGlossaryDocument(Glossary doc) throws DocumentException;
	public boolean deleteGlossaryDocument(int docID);
	@Deprecated
	ArrayList<Glossary> getGlossaryResponses(int docID, int docType,Set<String> complexUserID, String absoluteUserID)throws DocumentException, QueryException;
	@Deprecated
	DocumentCollection getGlossaryDescendants(int docID, int docType, DocID[] toExpand, int level, Set<String> complexUserID, String absoluteUserID) throws DocumentException, QueryException;
	Object getGlossaryEntry(Connection conn, Set<String> sysgroupasset,	String sysuser, ResultSet rs, String fieldsCond, Set<DocID> hashSet) throws SQLException, DocumentException;
	boolean hasResponse(Connection conn, int docID, int docType, Set<String> complexUserID, String absoluteUserID);

	@Deprecated
	StringBuffer getGlossaryByCondition(IQueryFormula condition, int offset,
			int pageSize, String fieldsCond, Set<String> toExpand,
			Set<DocID> toExpandResp, TagPublicationFormatType publishAs)
			throws DocumentException, QueryException;

    boolean inUse(Connection conn, int docID, int docType);

	_ViewEntryCollection getCollectionByCondition(ISelectFormula sf, int pageNum,	int pageSize, Set<DocID> expandedDocuments, RunTimeParameters parameters,boolean checkResponse);
}
