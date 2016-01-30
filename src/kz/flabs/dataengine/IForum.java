package kz.flabs.dataengine;


import kz.flabs.exception.DocumentAccessException;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.forum.Post;
import kz.flabs.runtimeobj.forum.Topic;
import kz.flabs.users.RunTimeParameters;
import kz.flabs.users.User;
import kz.flabs.webrule.query.QueryFieldRule;
import kz.nextbase.script._ViewEntryCollection;

import java.util.Set;

public interface IForum {
	IDatabase getParent();
	
	int getPostsCountByTopicID(int topicID, Set<String> complexUserID, String absoluteUserID);
	StringBuffer getPostsByTopicID(int topicID, Set<String> complexUserID, String absoluteUserID, QueryFieldRule[] fields, Set<DocID> toExpandResponses, int offset, int pageSize);
	
	int insertTopic(Topic topic, User user);
	int updateTopic(Topic topic, User user) throws DocumentAccessException;
	int deleteTopic(Topic topic, Set<String> complexUserID, String absoluteUserID);
	int insertPost(Post post, User user);
	int updatePost(Post post, User user) throws DocumentAccessException;
	int deletePost(Post topic, Set<String> complexUserID, String absoluteUserID);
	
	void toShare(int topicID, Set<String> complexUserID, String absoluteUserID);

	Topic getTopicByID(int docID, Set<String> complexUserID, String absoluteUserID) throws DocumentAccessException;

    Document getTopicByPostID(BaseDocument post, Set<String> complexUserID, String absoluteUserID) throws DocumentAccessException;

    Post getPostByID(int docID, Set<String> complexUserID, String absoluteUserID) throws DocumentAccessException;

	StringBuffer getForumThreadByTopicID(int parentDocID, Set<String> userGroups,
			String userID, QueryFieldRule[] fields, int pageNum, int pageSize, Set<DocID> toExpandResp);
	
	_ViewEntryCollection getForumTopics(ISelectFormula condition, User user, int pageNum, int pageSize, Set<String> toExpandResp, RunTimeParameters parameters);


    _ViewEntryCollection getTopicsCollection(ISelectFormula condition, User user, int pageNum, int pageSize, RunTimeParameters parameters, boolean checkResponse);
}
