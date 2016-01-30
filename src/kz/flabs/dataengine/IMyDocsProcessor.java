package kz.flabs.dataengine;

import java.util.Set;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.webrule.query.QueryFieldRule;

public interface IMyDocsProcessor {
	int getTasksForMeCount(String userName);
	StringBuffer getTasksForMe(IQueryFormula nf, Set<String> complexUserID, String userName, Set<DocID> toExpandResponses, int offset, int pageSize, QueryFieldRule[] fields) throws DocumentException;
	
	int getMyTasksCount(String userName);
	StringBuffer getMyTasks(IQueryFormula nf, Set<String> complexUserID, String userName, Set<DocID> toExpandResponses, int offset, int pageSize, QueryFieldRule[] fields) throws DocumentException;

	int getCompleteTaskCount(String userName);
	StringBuffer getCompleteTask(IQueryFormula nf, String userName, int offset, int pageSize, QueryFieldRule[] fields) throws DocumentException;
	
	int getPrjsWaitForCoordCount(String userName);
	StringBuffer getPrjsWaitForCoord(IQueryFormula nf, String userName, int offset, int pageSize, QueryFieldRule[] fields) throws DocumentException;
	
	int getPrjsWaitForSignCount(String userName);
	StringBuffer getPrjsWaitForSign(IQueryFormula nf, String userName, int offset, int pageSize, QueryFieldRule[] fields) throws DocumentException;
	
	StringBuffer getToConsider(IQueryFormula nf, String userName, int offset, int pageSize, QueryFieldRule[] fields) throws DocumentException;
	int getToConsiderCount(String userName);
	
	
	//StringBuffer getFavorites(IQueryFormula nf, Set<String> complexUserID, String absoluteUserID, int offset, int pageSize, String fieldsCond, Set<DocID> toExpandResponses, Set<String> toExpandCategory, TagPublicationFormatType publishAs);
	//int getFavoritesCount(Set<String> complexUserID, String absoluteUserID);
	
	//int isFavourites(Connection conn, int docID, int docType, String userName);
	int getTasksForMeCount(IQueryFormula nf, String userID);
	int getToConsiderCount(IQueryFormula nf, String userID);
	int getMyTasksCount(IQueryFormula nf, String userID);
	int getCompleteTaskCount(IQueryFormula nf, String userID);
	int getPrjsWaitForCoordCount(IQueryFormula nf, String userID);
	int getPrjsWaitForSignCount(IQueryFormula nf, String userID);

}
