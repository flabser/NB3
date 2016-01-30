package kz.flabs.dataengine;

import java.util.Set;
import kz.flabs.runtimeobj.Filter;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.webrule.constants.TagPublicationFormatType;

public interface IFilters {
	StringBuffer getDocumentsByFilter(Filter filter, Set<String> complexUserID, String absoluteUserID, int offset, int pageSize, Set<DocID> toExpandResponses, Set<String> toExpandCategory, TagPublicationFormatType publishAs, int page);
	StringBuffer getFiltersByUser(Set<String> complexUserID, String absoluteUserID);
		
	int insertFilter(Filter filter, Set<String> complexUserID, String absoluteUserID);
	int updateFilter(Filter filter, Set<String> complexUserID, String absoluteUserID);
	int getDocumentsCountByFilter(Filter filter, Set<String> complexUserID,
			String absoluteUserID, int offset, int pageSize);
	
	Filter getFilterByID(int id, Set<String> complexUserID,
			String absoluteUserID);
}
