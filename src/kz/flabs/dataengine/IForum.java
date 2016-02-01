package kz.flabs.dataengine;

import java.util.Set;

import kz.flabs.exception.DocumentAccessException;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.users.RunTimeParameters;
import kz.flabs.users.User;
import kz.flabs.webrule.query.QueryFieldRule;
import kz.nextbase.script._ViewEntryCollection;

public interface IForum {
	IDatabase getParent();

	int getPostsCountByTopicID(int topicID, Set<String> complexUserID, String absoluteUserID);

	StringBuffer getPostsByTopicID(int topicID, Set<String> complexUserID, String absoluteUserID, QueryFieldRule[] fields,
	        Set<DocID> toExpandResponses, int offset, int pageSize);

	void toShare(int topicID, Set<String> complexUserID, String absoluteUserID);

	Document getTopicByPostID(BaseDocument post, Set<String> complexUserID, String absoluteUserID) throws DocumentAccessException;

	StringBuffer getForumThreadByTopicID(int parentDocID, Set<String> userGroups, String userID, QueryFieldRule[] fields, int pageNum, int pageSize,
	        Set<DocID> toExpandResp);

	_ViewEntryCollection getForumTopics(ISelectFormula condition, User user, int pageNum, int pageSize, Set<String> toExpandResp,
	        RunTimeParameters parameters);

	_ViewEntryCollection getTopicsCollection(ISelectFormula condition, User user, int pageNum, int pageSize, RunTimeParameters parameters,
	        boolean checkResponse);
}
