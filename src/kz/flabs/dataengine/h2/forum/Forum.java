package kz.flabs.dataengine.h2.forum;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.*;
import kz.flabs.dataengine.h2.Database;
import kz.flabs.exception.*;
import kz.flabs.runtimeobj.DocumentCollection;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.runtimeobj.document.*;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.forum.Post;
import kz.flabs.runtimeobj.forum.Topic;
import kz.flabs.runtimeobj.viewentry.ViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntryCollection;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.users.Reader;
import kz.flabs.users.RunTimeParameters;
import kz.flabs.users.User;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.query.QueryFieldRule;
import kz.nextbase.script._ViewEntryCollection;
import kz.pchelka.env.Environment;

import org.h2.jdbc.JdbcSQLException;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.Map.Entry;

public class Forum extends DatabaseCore implements IForum, Const {
	protected IDatabase db;
	protected IDBConnectionPool dbPool;

	private static String baseTable = "TOPICS"; 
	
	public Forum(Database database, IDBConnectionPool forumDbPool) {
		db = database;
		dbPool = forumDbPool;
	}

	@Override
	public IDatabase getParent() {		
		return db;
	}

	@Override
	public int getPostsCountByTopicID(int topicID, Set<String> complexUserID, String absoluteUserID) {
		int count = 0;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			String sql = "select count(descendant) from forum_tree_path where ancestor in (select p.docid from posts p, readers_posts rp where parentdocid = " + topicID + " and parentdoctype = " + Const.DOCTYPE_TOPIC + " and p.docid = rp.docid and rp.username in (" + DatabaseUtil.prepareListToQuery(complexUserID) + "))";
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();	
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(),e);
		} finally {	
			dbPool.returnConnection(conn);
		}
		return count;
	}

	public boolean hasResponse(Connection conn, int docID, int docType, Set<String> complexUserID, String absoluteUserID) {
		try {
			conn.setAutoCommit(false);
			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "select descendant from forum_tree_path where ancestor in (select p.docid from posts p, readers_posts rp where parentdocid = " + docID + " and parentdoctype = " + docType + "  and rp.username in (" + DatabaseUtil.prepareListToQuery(complexUserID) + "))";
			ResultSet rs = st.executeQuery(sql);
			if (rs.next()) {
				st.close();
				return true;
			}
			rs.close();
			st.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(this.db.getDbID(), e);
		} 
		return false;
	}

	public DocumentCollection getDescendants(int docID, int docType, Set<String> complexUserID, String absoluteUserID, int level) {
		int col = 0;
		DocumentCollection documents = new DocumentCollection();
		StringBuffer xmlContent = new StringBuffer(10000);		
		String value = "";
		if (docID != 0 && docType != DOCTYPE_UNKNOWN){
			Connection conn = dbPool.getConnection();
			try {	
				conn.setAutoCommit(false);
				Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				//String sql = "select * from posts p where p.docid in (select descendant from forum_tree_path where ancestor in (select p.docid from posts p, readers_posts rp where parentdocid = " + docID + " and parentdoctype = " + docType + "))";
				String sql = "select *, case(parentdoctype) when " + Const.DOCTYPE_TOPIC + " then 1 else ftp.length + 1 end as length from posts p left join FORUM_TREE_PATH as ftp on p.DOCID = ftp.DESCENDANT and p.PARENTDOCID = ftp.ANCESTOR where parentdocid = " + docID + " and parentdoctype = " + docType + " and p.docid in (select docid from readers_posts rp where rp.username in (" + DatabaseUtil.prepareListToQuery(complexUserID) + "))";
				ResultSet rs = statement.executeQuery(sql);

				while (rs.next()) {
					String viewText = rs.getString("VIEWTEXT");
                    String viewContent = getViewContent(rs);
					value = "";	
					int respDocID = rs.getInt("DOCID");
					int repsDocType = rs.getInt("DOCTYPE");
					String form = rs.getString("FORM");				
					xmlContent.append("<entry doctype=\"" + repsDocType
							+ "\"  parentdocid=\"" + docID + "\" docid=\"" + respDocID + "\" level=\"" + level + "\" form=\"" + form + "\" url=\"Provider?type=edit&amp;element=comment&amp;id=" + form + "&amp;key="
							+ respDocID + "\"" + XMLUtil.getAsAttribute("viewtext", viewText));
					col ++ ;
					String authorID = rs.getString("AUTHOR");
					if (authorID != null && !"".equalsIgnoreCase(authorID)) {
						Employer author = this.db.getStructure().getAppUser(authorID);
						value += "<author userid=\"" + author.getUserID() + "\">" + author.getShortName() + "</author>";						
					}
					Date postDate = rs.getDate("POSTDATE");
					if (postDate != null) {
						value += "<viewdate>" + Database.dateTimeFormat.format(postDate) + "</viewdate>";
					}
					int l = level + 1;
					DocumentCollection responses = getDescendants(respDocID, repsDocType, complexUserID, absoluteUserID, l );
					if (responses.count > 0) {
						xmlContent.append(" hasresponse=\"true\" >");
						value += responses.xmlContent;						
					} else {
						xmlContent.append(" >");
					}
                    xmlContent.append(viewContent);
					xmlContent.append(value += "<viewtext>" + XMLUtil.getAsTagValue(viewText) + "</viewtext></entry>");
					col++;

				}

				documents.xmlContent.append(xmlContent);
				rs.close();
				statement.close();
				conn.commit();
				documents.count = col;
			} catch (SQLException e) {
				DatabaseUtil.errorPrint(this.db.getDbID(), e);
			}finally{	
				dbPool.returnConnection(conn);
			}
		}
		return documents;
	}

	public String getDocumentEntry(Connection conn, Set<String> complexUserID, String absoluteUserID, ResultSet rs, Set<DocID> toExpandResponses) throws SQLException, DocumentException{
		String customFieldsValue = "";	
		int docID = rs.getInt("DOCID");
		int docType = rs.getInt("DOCTYPE");
		//	int canDelete = 0; 

		Statement sFields = conn.createStatement();
		boolean isResponding = false;
		if (hasResponse(conn, docID, docType, complexUserID, null)) {
			customFieldsValue += "<hasresponse>true</hasresponse>";
			isResponding = true;
		}

		String authorID = rs.getString("AUTHOR");
		if (authorID != null && !"".equalsIgnoreCase(authorID)) {
			Employer author = this.db.getStructure().getAppUser(authorID);
			customFieldsValue += "<author userid=\"" + author.getUserID() + "\">" + author.getShortName() + "</author>";						
		}

		StringBuffer value = new StringBuffer("<entry doctype=\"" + docType + "\" level=\"" + rs.getString("LENGTH") + "\" " + 
				"docid=\"" + docID + "\" " +
				"url=\"Provider?type=edit&amp;element=post&amp;id=" + rs.getString("FORM") + "&amp;key="	+ docID + "\" " +						 						
				">" + getViewContent(rs) + customFieldsValue);


		sFields.close();

		if(isResponding == true){			
			DocumentCollection responses = getDescendants(docID, docType, complexUserID, absoluteUserID, 2);
			if (responses.count > 0) {
				value.append("<responses>" + responses.xmlContent + "</responses>");
			}	
		}
		return value.append("</entry>").toString();
	}

    @Override
    public Document getTopicByPostID(BaseDocument doc, Set<String> complexUserID, String absoluteUserID) throws DocumentAccessException {
        Document parentDoc = null;
        if (!doc.isNewDoc()){
            switch (doc.parentDocType) {
                case Const.DOCTYPE_POST:
                    parentDoc = getPostByID(doc.parentDocID, complexUserID, absoluteUserID);
                    break;
                case Const.DOCTYPE_TOPIC:
                    parentDoc = getTopicByID(doc.parentDocID, complexUserID, absoluteUserID);
                    break;
            }

            if(parentDoc != null && parentDoc.parentDocID != 0 && parentDoc.parentDocType != Const.DOCTYPE_UNKNOWN && parentDoc.docType != Const.DOCTYPE_TOPIC){
                parentDoc =  getTopicByPostID(parentDoc, complexUserID, absoluteUserID);
            }
        }
        return parentDoc;
    }

	public StringBuffer getForumThreadByTopicID(int topicID, Set<String> complexUserID, String absoluteUserID, QueryFieldRule[] fields, int offset, int pageSize, Set<DocID> toExpandResp) {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			//String sql = "select * from posts p where parentdocid = " + topicID + " and parentdoctype = " + Const.DOCTYPE_TOPIC + " and p.docid in (select docid from readers_posts rp where rp.username in (" + DatabaseUtil.prepareListToQuery(complexUserID) + "))";
			String sql = "select *, case(parentdoctype) when " + Const.DOCTYPE_TOPIC + " then 1 else ftp.length + 1 end as length from posts p left join FORUM_TREE_PATH as ftp on p.DOCID = ftp.DESCENDANT and p.PARENTDOCID = ftp.ANCESTOR where parentdocid = " + topicID + " and parentdoctype = " + Const.DOCTYPE_TOPIC + " and p.docid in (select docid from readers_posts rp where rp.username in (" + DatabaseUtil.prepareListToQuery(complexUserID) + "))";
			ResultSet rs = s.executeQuery(sql);		
			while (rs.next()) {
				xmlContent.append(getDocumentEntry(conn, complexUserID, absoluteUserID, rs, toExpandResp));
			}
			conn.commit();
			s.close();
			rs.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(this.db.getDbID(), e);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {	
			dbPool.returnConnection(conn);
		}
		return xmlContent;
	}

	public StringBuffer getForumThreadByPostID(int postID, Set<String> complexUserID, String absoluteUserID, QueryFieldRule[] fields, int offset, int pageSize) {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			String sql = "select * from posts p, readers_posts rp where parentdocid = " + postID + " and parentdoctype = " + Const.DOCTYPE_POST + " and p.docid = rp.docid and rp.username in (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")) ORDER BY REGDATE LIMIT " + pageSize + " OFFSET " + offset;

			ResultSet rs = s.executeQuery(sql);

			while (rs.next()) {
				int docID = rs.getInt("DOCID");			
				String form = rs.getString("FORM");
				StringBuffer value = new StringBuffer(1000);
	

				if (db.hasResponse(conn, docID, DOCTYPE_TOPIC, complexUserID, absoluteUserID)) {
					value.append("<hasresponse>true</hasresponse>");
				}

				for(QueryFieldRule field: fields){
					try{
						ArrayList<String[]> tmpValues = new SourceSupplier(db.getParent()).publishAs(field.publicationFormat, new String[] {rs.getString(field.value)});
						for (String[] a : tmpValues) {
							value.append("<" + field.name + ">" + XMLUtil.getAsTagValue(a[0]) + "</" + field.name + ">");
						}						
					}catch(JdbcSQLException e){
						Database.logger.errorLogEntry(e);
					} catch (DocumentException e) {
						Database.logger.errorLogEntry(e);
					}
				}


				DocumentCollection responses = db.getDescendants(docID, DOCTYPE_TASK, null, 1, complexUserID, absoluteUserID);
				value.append("<responses>" + responses.xmlContent + "</responses>");

				xmlContent.append("<entry hasattach=\"" + Integer.toString(rs.getInt("HAS_ATTACHMENT")) + "\" doctype=\"" + DOCTYPE_TASK + "\"  " +
						"docid=\"" + docID + "\" "  +
						"url=\"Provider?type=edit&amp;element=task&amp;id=" + form + "&amp;key="	+ docID + "\" " +						 						
						"><dbd>" + rs.getInt("DBD") + "</dbd>" + getViewContent(rs) + value);
				xmlContent.append("</entry>");	
			}
			s.close();
			rs.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(),e);
		} finally {		
			dbPool.returnConnection(conn);
		}
		return xmlContent;
	}

	public Topic getTopicByParentDocument(int docID, int docType, Set<String> complexUserID, String absoluteUserID) throws DocumentAccessException{
		Topic topic = new Topic(db, absoluteUserID);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement statement = conn.createStatement();		
			//String sql = "select * from TOPICS as t where exists (select * from readers_topics rt where rt.docid = " + docID + " and rt.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + "))" + " and t.docid = " + docID;
			String sql = "select * from topics where exists (select * from readers_topics rt where rt.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")) and parentdocid = " + docID + " parentdoctype = " + docType;
			ResultSet rs = statement.executeQuery(sql);
			if (rs.next()) {
				fillTopicData(rs, topic);
				rs = conn.createStatement()
						.executeQuery("select * from CUSTOM_BLOBS_TOPICS where CUSTOM_BLOBS_TOPICS.DOCID = "
								+ docID);
				while (rs.next()) {
					HashMap<String, BlobFile> files = new HashMap<String, BlobFile>();
					String name = rs.getString("NAME");
					BlobFile bf = new BlobFile();
					bf.originalName = rs.getString("ORIGINALNAME");
					bf.checkHash = rs.getString("CHECKSUM");
					files.put(bf.originalName, bf);
					topic.addBlobField(name, files);
				}
				rs.close();
				db.fillAccessRelatedField(conn, "TOPICS", docID, topic);
				if (topic.hasEditor(complexUserID)) {
					topic.editMode = Const.EDITMODE_EDIT;
				}
			}else{			
				throw new DocumentAccessException(ExceptionType.DOCUMENT_READ_RESTRICTED, absoluteUserID);
			}
			conn.commit();
			rs.close();
			statement.close();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {		
			dbPool.returnConnection(conn);
		}
		return topic;

	}

	public StringBuffer getForumThreadByTopicID(int parentDocID, int parentDocType, Set<String> complexUserID, String absoluteUserID, QueryFieldRule[] fields, int offset, int pageSize) {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			String sql = "select * from topics t, readers_topics rt where parentdocid = " + parentDocID + " and parentdoctype = " + parentDocType + " and t.docid = rt.docid and rt.username in (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")) ORDER BY REGDATE LIMIT " + pageSize + " OFFSET " + offset;

			ResultSet rs = s.executeQuery(sql);

			while (rs.next()) {
				int docID = rs.getInt("DOCID");			
				String form = rs.getString("FORM");
				StringBuffer value = new StringBuffer(1000);
				String viewText = rs.getString("VIEWTEXT");

				if (db.hasResponse(conn, docID, DOCTYPE_TOPIC, complexUserID, absoluteUserID)) {
					value.append("<hasresponse>true</hasresponse>");
				}

				for(QueryFieldRule field: fields){
					try{
						ArrayList<String[]> tmpValues = new SourceSupplier(db.getParent()).publishAs(field.publicationFormat, new String[] {rs.getString(field.value)});
						for (String[] a : tmpValues) {
							value.append("<" + field.name + ">" + XMLUtil.getAsTagValue(a[0]) + "</" + field.name + ">");
						}						
					}catch(JdbcSQLException e){
						Database.logger.errorLogEntry(e);
					} catch (DocumentException e) {
						Database.logger.errorLogEntry(e);
					}
				}


				DocumentCollection responses = db.getDescendants(docID, DOCTYPE_TASK, null, 1, complexUserID, absoluteUserID);
				value.append("<responses>" + responses.xmlContent + "</responses>");


				


				xmlContent.append("<entry hasattach=\"" + Integer.toString(rs.getInt("HAS_ATTACHMENT")) + "\" doctype=\"" + DOCTYPE_TASK + "\"  " +
						"docid=\"" + docID + "\" " +
						"url=\"Provider?type=edit&amp;element=task&amp;id=" + form + "&amp;key="	+ docID + "\" " +						 						
						"><dbd>" + rs.getInt("DBD") + "</dbd>" + getViewContent(rs) + value);
				xmlContent.append("</entry>");	
			}
			s.close();
			rs.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(),e);
		} finally {		
			dbPool.returnConnection(conn);
		}
		return xmlContent;
	}

	public StringBuffer getForumThreadByTopicID(int topicID, Set<String> complexUserID, String absoluteUserID, QueryFieldRule[] fields, int offset, int pageSize) {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			//String sql = "select * from topics t, readers_topics rt where parentdocid = " + topicID + " and parentdoctype = " + Const.DOCTYPE_TOPIC + " and t.docid = rt.docid and rt.username in (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"; 
			String sql = "select * from posts p, readers_posts rp where parentdocid = " + topicID + " and parentdoctype = " + Const.DOCTYPE_TOPIC + " and p.docid = rp.docid and rp.username in (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";

			ResultSet rs = s.executeQuery(sql);

			while (rs.next()) {
				int docID = rs.getInt("DOCID");			
				String form = rs.getString("FORM");
				StringBuffer value = new StringBuffer(1000);
				
				if (db.hasResponse(conn, docID, DOCTYPE_TOPIC, complexUserID, absoluteUserID)) {
					value.append("<hasresponse>true</hasresponse>");
				}

				for(QueryFieldRule field: fields){
					try{
						ArrayList<String[]> tmpValues = new SourceSupplier(db.getParent()).publishAs(field.publicationFormat, new String[] {rs.getString(field.value)});
						for (String[] a : tmpValues) {
							value.append("<" + field.name + ">" + XMLUtil.getAsTagValue(a[0]) + "</" + field.name + ">");
						}						
					}catch(JdbcSQLException e){
						Database.logger.errorLogEntry(e);
					} catch (DocumentException e) {
						Database.logger.errorLogEntry(e);
					}
				}


				DocumentCollection responses = db.getDescendants(docID, DOCTYPE_TASK, null, 1, complexUserID, absoluteUserID);
				value.append("<responses>" + responses.xmlContent + "</responses>");
				

				xmlContent.append("<entry doctype=\"" + DOCTYPE_POST + "\"  " +
						"docid=\"" + docID + "\" " +
						"url=\"Provider?type=edit&amp;element=post&amp;id=" + form + "&amp;key="	+ docID + "\" " +						 						
						">" + getViewContent(rs) + value);
				xmlContent.append("</entry>");	
			}
			s.close();
			rs.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(),e);
		} finally {		
			dbPool.returnConnection(conn);
		}
		return xmlContent;
	}

	@Override
	public StringBuffer getPostsByTopicID(int topicID, Set<String> complexUserID, String absoluteUserID, QueryFieldRule[] fields, Set<DocID> toExpandResponses, int offset, int pageSize) {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			String sql = "select count(descendant) from forum_tree_path where ancestor in (select docid from posts p, readers_posts rp where parentdocid = " + topicID + " and parentdoctype = " + Const.DOCTYPE_TOPIC + " and p.docid = rp.docid and rp.username in (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")) ORDER BY REGDATE LIMIT " + pageSize + " OFFSET " + offset;

			ResultSet rs = s.executeQuery(sql);

			while (rs.next()) {
				int docID = rs.getInt("DOCID");				
				String form = rs.getString("FORM");
				StringBuffer value = new StringBuffer(1000);
			
				if (db.hasResponse(conn, docID, DOCTYPE_TASK, complexUserID, absoluteUserID)) {
					value.append("<hasresponse>true</hasresponse>");
				}

				for(QueryFieldRule field: fields){
					try{
						ArrayList<String[]> tmpValues = new SourceSupplier(db.getParent()).publishAs(field.publicationFormat, new String[] {rs.getString(field.value)});
						for (String[] a : tmpValues) {
							value.append("<" + field.name + ">" + XMLUtil.getAsTagValue(a[0]) + "</" + field.name + ">");
						}						
					}catch(JdbcSQLException e){
						Database.logger.errorLogEntry(e);
					} catch (DocumentException e) {
						Database.logger.errorLogEntry(e);
					}
				}

				if (toExpandResponses.size() > 0) {
					for (DocID doc : toExpandResponses) {
						if (doc.id == docID && doc.type == DOCTYPE_TASK) {
							DocumentCollection responses = db.getDescendants(docID, DOCTYPE_TASK, null, 1, complexUserID, absoluteUserID);
							value.append("<responses>" + responses.xmlContent + "</responses>");
						}
					}
				}

				xmlContent.append("<entry hasattach=\"" + Integer.toString(rs.getInt("HAS_ATTACHMENT")) + "\" doctype=\"" + DOCTYPE_TASK + "\"  " +
						"docid=\"" + docID + "\" "  +
						"url=\"Provider?type=edit&amp;element=task&amp;id=" + form + "&amp;key="	+ docID + "\" " +						 						
						"><dbd>" + rs.getInt("DBD") + "</dbd>" + getViewContent(rs) + value);
				xmlContent.append("</entry>");	
			}
			s.close();
			rs.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(),e);
		} finally {		
			dbPool.returnConnection(conn);
		}
		return xmlContent;
	}

	@Override
	public void toShare(int topicID, Set<String> complexUserID, String absoluteUserID) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement st = conn.createStatement();
			String sql = "update topics set public = 1 where docid = " + topicID + " and authors_topics in (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")" ;
			ResultSet rs = st.executeQuery(sql);
			if (rs.next()) {
				sql = "update posts set public = 1 where docid in (select descendant from forum_tree_path where ancestor in (select docid from posts p, readers_posts rp where parentdocid = " + topicID + " and parentdoctype = " + Const.DOCTYPE_TOPIC + "))";
				st.executeQuery(sql);
			}
			rs.close();
			st.close();
		} catch(SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {		
			dbPool.returnConnection(conn);
		}

	}

	@Override
	public Topic getTopicByID(int docID, Set<String> complexUserID, String absoluteUserID) throws DocumentAccessException{
		Topic topic = new Topic(db, absoluteUserID);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement statement = conn.createStatement();		
			String sql = "select * from TOPICS as t where exists (select * from readers_topics rt where rt.docid = " + docID + " and rt.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + "))" + " and t.docid = " + docID;
			ResultSet rs = statement.executeQuery(sql);
			if (rs.next()) {
				fillTopicData(rs, topic);
				rs = conn.createStatement()
						.executeQuery("select * from CUSTOM_BLOBS_TOPICS where CUSTOM_BLOBS_TOPICS.DOCID = "
								+ docID);
				while (rs.next()) {
					HashMap<String, BlobFile> files = new HashMap<String, BlobFile>();
					String name = rs.getString("NAME");
					BlobFile bf = new BlobFile();
					bf.originalName = rs.getString("ORIGINALNAME");
					bf.checkHash = rs.getString("CHECKSUM");
					files.put(bf.originalName, bf);
					topic.addBlobField(name, files);
				}
				rs.close();
				db.fillAccessRelatedField(conn, baseTable, docID, topic);
				if (topic.hasEditor(complexUserID)) {
					topic.editMode = Const.EDITMODE_EDIT;
				}
			}else{
				throw new DocumentAccessException(ExceptionType.DOCUMENT_READ_RESTRICTED, absoluteUserID);
			}
			conn.commit();
			rs.close();
			statement.close();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {		
			dbPool.returnConnection(conn);
		}
		return topic;
	}


   /* public Topic getTopicByPostID(BaseDocument post, Set<String> complexUserID, String absoluteUserID) throws DocumentAccessException{
        Topic topic = new Topic(db, absoluteUserID);
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement statement = conn.createStatement();
            String sql = "select * from TOPICS as t where exists (select * from readers_topics rt where rt.docid = " + post.parentDocID + " and rt.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + "))" + " and t.docid = " + post.parentDocID;
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()) {
                fillTopicData(rs, topic);
                rs = conn.createStatement()
                        .executeQuery("select * from CUSTOM_BLOBS_TOPICS where CUSTOM_BLOBS_TOPICS.DOCID = "
                                + post.parentDocID);
                while (rs.next()) {
                    HashMap<String, BlobFile> files = new HashMap<String, BlobFile>();
                    String name = rs.getString("NAME");
                    BlobFile bf = new BlobFile();
                    bf.originalName = rs.getString("ORIGINALNAME");
                    bf.checkHash = rs.getString("CHECKSUM");
                    files.put(bf.originalName, bf);
                    topic.addBlobField(name, files);
                }
                rs.close();
                db.fillAccessRelatedField(conn, baseTable, post.parentDocID, topic);
                if (topic.hasEditor(complexUserID)) {
                    topic.editMode = Const.EDITMODE_EDIT;
                }
            }else{
                throw new DocumentAccessException(ExceptionType.DOCUMENT_READ_RESTRICTED, absoluteUserID);
            }
            conn.commit();
            rs.close();
            statement.close();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return topic;
    }*/

	private Topic fillTopicData(ResultSet rs, Topic topic) {
		try {
			topic.setTheme(rs.getString("THEME"));
			topic.setShared(rs.getInt("ISPUBLIC"));
			topic.setContent(rs.getString("CONTENT"));
			topic.setStatus(rs.getInt("STATUS"));
			topic.setCitationIndex(rs.getInt("CITATIONINDEX"));
			topic.setTopicDate(rs.getDate("TOPICDATE"));
			topic.docType = Const.DOCTYPE_TOPIC;
			topic.isValid = true;	
			fillViewTextData(rs, topic);
			fillSysData(rs, topic);
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return topic;
	}

	private Post fillPostData(ResultSet rs, Post post) {
		try {
			post.setShared(rs.getInt("ISPUBLIC"));
			post.setContent(rs.getString("CONTENT"));
			post.setPostDate(rs.getDate("POSTDATE"));
			post.docType = Const.DOCTYPE_POST;
			post.isValid = true;
			fillViewTextData(rs, post);
			fillSysData(rs, post);
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return post;
	}

	@Override
	public Post getPostByID(int docID, Set<String> complexUserID, String absoluteUserID) throws DocumentAccessException {
		Post post = new Post(db, absoluteUserID);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement statement = conn.createStatement();		
			String sql = "select * from POSTS as t where exists (select * from readers_posts rt where rt.docid = " + docID + " and rt.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + "))" + " and t.docid = " + docID;
			ResultSet rs = statement.executeQuery(sql);
			if (rs.next()) {
				fillPostData(rs, post);
				rs = conn.createStatement().executeQuery("select * from CUSTOM_BLOBS_POSTS where CUSTOM_BLOBS_POSTS.DOCID = " + docID);
				while (rs.next()) {
					HashMap<String, BlobFile> files = new HashMap<String, BlobFile>();
					String name = rs.getString("NAME");
					BlobFile bf = new BlobFile();
					bf.originalName = rs.getString("ORIGINALNAME");
					bf.checkHash = rs.getString("CHECKSUM");
					files.put(bf.originalName, bf);
					post.addBlobField(name, files);
				}
				rs.close();
				db.fillAccessRelatedField(conn, "POSTS", docID, post);
				if (post.hasEditor(complexUserID)) {
					post.editMode = Const.EDITMODE_EDIT;
				}
			}else{
				throw new DocumentAccessException(ExceptionType.DOCUMENT_READ_RESTRICTED, absoluteUserID);
			}
			conn.commit();
			rs.close();
			statement.close();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {		
			dbPool.returnConnection(conn);
		}
		return post;
	}

	protected void insertAccessRelatedRec(String accessTableSuffix, int docID, Connection conn, Document doc) {
		try {
			String authorsTable = "AUTHORS_" + accessTableSuffix, authorsUpdateSQL = "";
			HashSet<String> authors = doc.getEditors();
			authors.add(Const.sysUser);
			authors.addAll(Arrays.asList(Const.observerGroup));
			for (String author : authors) {
				String hasAuthorSQL = "select count(*) from " + authorsTable
						+ " where DOCID=" + docID + " and USERNAME='" + author
						+ "'";
				ResultSet resultSet = conn.createStatement().executeQuery(
						hasAuthorSQL);

				if (resultSet.next()) {
					if (resultSet.getInt(1) == 0) {
						authorsUpdateSQL += "('" + author + "', " + docID
								+ "),";
					}
				}
			}
			if (!authorsUpdateSQL.equals("")) {
				authorsUpdateSQL = authorsUpdateSQL.substring(0,
						authorsUpdateSQL.length() - 1);
				conn.prepareStatement(
						"insert into " + authorsTable
						+ "(USERNAME, DOCID) values "
						+ authorsUpdateSQL).executeUpdate();
			}

			String readersTable = "READERS_" + accessTableSuffix, readersUpdateSQL = "";
			HashSet<Reader> readers = doc.getReaders();
			readers.add(new Reader(Const.sysUser));

            for (String value : observerGroupAsList) {
                readers.add(new Reader(value));
            }

			for (Reader reader : readers) {
				String hasReaderSQL = "select count(*) from " + readersTable
						+ " where DOCID=" + docID + " and USERNAME='" + reader
						+ "'";
				ResultSet resultSet = conn.createStatement().executeQuery(
						hasReaderSQL);
				if (resultSet.next()) {
					if (resultSet.getInt(1) == 0) {
						readersUpdateSQL += "('" + reader + "', " + docID
								+ "),";
					}
				}
			}
			if (!readersUpdateSQL.equals("")) {
				readersUpdateSQL = readersUpdateSQL.substring(0,
						readersUpdateSQL.length() - 1);
				conn.prepareStatement(
						"insert into " + readersTable
						+ "(USERNAME, DOCID) values "
						+ readersUpdateSQL).executeUpdate();
			}

		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		}
	}

	@Override
	public int insertTopic(Topic topic, User user) {
		int key = 0;
		Connection conn = dbPool.getConnection();		
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			Date viewDate = topic.getViewDate();	
			String fieldsAsText = "PARENTDOCID, PARENTDOCTYPE, AUTHOR, STATUS, ISPUBLIC, THEME, " +
					" CONTENT, CITATIONINDEX, REGDATE, SIGN, SIGNEDFIELDS, FORM, DDBID, VIEWNUMBER, VIEWDATE, " +
                    DatabaseUtil.getViewTextList("") + ", DOCTYPE, LASTUPDATE, TOPICDATE, VIEWTEXT";

            String valuesAsText = topic.parentDocID +
					", " + topic.parentDocType + 
					", '" + topic.getAuthorID() + "'" +
					", " + topic.getStatus() + 
					", " + topic.isShared() +
					", '" + topic.getTheme() + "'" +
					", '" + topic.getContent() + "'" +
					", " + topic.getCitationIndex() + 
					", '" + Database.sqlDateTimeFormat.format(new java.util.Date()) + "'" +
					", '" + topic.getSign() + "'" +
					", '" + topic.getSignedFields() + "'" +
					", '" + topic.form + "'" +
					", '" + topic.getDdbID() + "'" +
					", " + topic.getViewNumber() + 
					", " + (viewDate != null ? "'" + Database.sqlDateTimeFormat.format(viewDate) + "'": "null") +
					", " + DatabaseUtil.getViewTextValues(topic) +
					", " + Const.DOCTYPE_TOPIC +
					", '" + Database.sqlDateTimeFormat.format(new java.util.Date()) + "'" +
					", " + (topic.getTopicDate() != null ? "'" + Database.sqlDateTimeFormat.format(topic.getTopicDate()) + "'": "null") +
					", '" + (topic.getViewText() != null ? topic.getViewText().replace("'", "''") : "") + "'";
			String sql = "INSERT INTO TOPICS (" + fieldsAsText + ") VALUES (" + valuesAsText + ")";
			s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = s.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);
			}
			insertAccessRelatedRec("TOPICS", key, conn, (Document)topic);
			conn.commit();
			s.close();
			//CachePool.flush();
			IUsersActivity ua = db.getUserActivity();
			ua.postCompose(topic, user);
			ua.postMarkRead(key, topic.docType, user);
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
			return -1;
		} catch (Exception e) {
			DatabaseUtil.debugErrorPrint(e);
			return -1;
		} finally {			
			dbPool.returnConnection(conn);
		}
		return key;
	}

	@Override
	public int updateTopic(Topic doc, User user) throws DocumentAccessException {
		if (doc.hasEditor(user.getAllUserGroups())) {
			Topic oldDoc = this.getTopicByID(doc.getDocID(), user.getAllUserGroups(), user.getUserID());

			Connection conn = dbPool.getConnection();
			try {
				Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);	
				conn.setAutoCommit(false);
				Date viewDate = doc.getViewDate();
                String viewTextList = "";
                for (int i = 1; i <= DatabaseConst.VIEWTEXT_COUNT; i++) {
                    viewTextList += "'" + doc.getViewTextList().get(i).replaceAll("'", "''") + "',";
                }
                if (viewTextList.endsWith(",")) {
                    viewTextList = viewTextList.substring(0, viewTextList.length()-1);
                }
				String update = "UPDATE TOPICS SET " +
						" PARENTDOCID = " + doc.parentDocID + 
						", PARENTDOCTYPE = " + doc.parentDocType + 
						", AUTHOR = '" + doc.getAuthorID() + "'" +
						", STATUS = " + doc.getStatus() +
						", ISPUBLIC = " + doc.isShared() +
						", THEME = '" + doc.getTheme() + "'" +
						", CONTENT = '" + doc.getContent() + "'" +
						", CITATIONINDEX = " + doc.getCitationIndex() +
						", SIGN = '" + doc.getSign() + "'" +
						", SIGNEDFIELDS = '" + doc.getSignedFields() + "'" +
						", FORM = '" + doc.form + "'" +
						", DEFAULTRULEID = '" + doc.getDefaultRuleID() + "'" +
						", VIEWNUMBER = " + doc.getViewNumber() +
						", VIEWDATE = " + (viewDate != null ? "'" + new Timestamp(viewDate.getTime()) + "'": "null") + "" +
						", " + DatabaseUtil.getViewTextValues(doc) + ", DOCTYPE = " + Const.DOCTYPE_TOPIC +
						", LASTUPDATE = '" + Database.sqlDateTimeFormat.format(new java.util.Date()) + "'" +
						", TOPICDATE = '" + new Timestamp(doc.getTopicDate().getTime()) + "'" +
						", VIEWTEXT = '" + doc.getViewText().replace("'", "''") + "'" + 
						" where DOCID=" + doc.getDocID();
				s.executeUpdate(update);

				db.updateBlobTables(conn, doc, baseTable);
				db.updateAccessTables(conn, doc, baseTable);
				
				conn.commit();
				s.close();
				IUsersActivity ua = db.getUserActivity();
				ua.postModify(oldDoc, doc, user);
			} catch (SQLException e) {
				DatabaseUtil.debugErrorPrint(e);
				return -1;
			} catch (FileNotFoundException fe) {
				AppEnv.logger.errorLogEntry(fe);
				return -1;
			} catch (IOException ioe) {
				AppEnv.logger.errorLogEntry(ioe);
				return -1;
			} catch (DocumentException e) {
				AppEnv.logger.errorLogEntry(e);
			} finally {
				dbPool.returnConnection(conn);
			}
			return doc.getDocID();
		} else {
			throw new DocumentAccessException(ExceptionType.DOCUMENT_WRITE_ACCESS_RESTRICTED, user.getUserID());
		}	
	}

	@Override
	public int deleteTopic(Topic topic, Set<String> complexUserID, String absoluteUserID) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int insertPost(Post post, User user) {
		int key = 0;
		Connection conn = dbPool.getConnection();		
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			Date viewDate = post.getViewDate();	
			String fieldsAsText = "PARENTDOCID, PARENTDOCTYPE, AUTHOR, ISPUBLIC, " +
					" CONTENT, REGDATE, SIGN, SIGNEDFIELDS, FORM, DEFAULTRULEID, VIEWNUMBER, VIEWDATE, " +
                    DatabaseUtil.getViewTextList("") + ", DOCTYPE, LASTUPDATE, POSTDATE, VIEWTEXT";
			String valuesAsText = post.parentDocID + 
					", " + post.parentDocType + 
					", '" + post.getAuthorID() + "'" +
					", " + post.isShared() +
					", '" + post.getContent() + "'" +
					", '" + Database.sqlDateTimeFormat.format(new java.util.Date()) + "'" +
					", '" + post.getSign() + "'" +
					", '" + post.getSignedFields() + "'" +
					", '" + post.form + "'" +
					", '" + post.getDefaultRuleID() + "'" +
					", " + post.getViewNumber() + 
					", " + (viewDate != null ? "'" + Database.sqlDateTimeFormat.format(viewDate) + "'": "null") +
                    ", " + DatabaseUtil.getViewTextValues(post) +
					", " + Const.DOCTYPE_POST + 
					", '" + Database.sqlDateTimeFormat.format(new java.util.Date()) + "'" +
					", " + (post.getPostDate() != null ? "'" + Database.sqlDateTimeFormat.format(post.getPostDate()) + "'": "null") +
					", '" + post.getViewText().replace("'", "''") + "'"; 
			String sql = "INSERT INTO POSTS (" + fieldsAsText + ") VALUES (" + valuesAsText + ")";
			s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = s.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);
			}
			insertAccessRelatedRec("POSTS", key, conn, (Document)post);
			PreparedStatement pst = conn.prepareStatement("INSERT INTO FORUM_TREE_PATH (ANCESTOR, DESCENDANT, LENGTH) " +
					" SELECT gtp.ANCESTOR, " + key + ", gtp.LENGTH + 1 FROM FORUM_TREE_PATH as gtp WHERE gtp.DESCENDANT = " + post.parentDocID + " UNION ALL SELECT " + key + ", " + key + ", 0");
			pst.executeUpdate();
			pst.close();
			conn.commit();
			s.close();
			IUsersActivity ua = db.getUserActivity();
			ua.postCompose(post, user);
			ua.postMarkRead(key, post.docType, user);
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
			return -1;
		} catch (Exception e) {
			DatabaseUtil.debugErrorPrint(e);
			return -1;
		} finally {			
			dbPool.returnConnection(conn);
		}
		return key;
	}

	@Override
	public int updatePost(Post doc, User user) throws DocumentAccessException {
		if (doc.hasEditor(user.getAllUserGroups())) {
			Topic oldDoc = this.getTopicByID(doc.getDocID(), user.getAllUserGroups(), user.getUserID());

			Connection conn = dbPool.getConnection();
			try {
				Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);	
				conn.setAutoCommit(false);
				Date viewDate = doc.getViewDate();
                String viewTextList = "";
                for (int i = 1; i <= DatabaseConst.VIEWTEXT_COUNT; i++) {
                    viewTextList += "'" + doc.getViewTextList().get(i).replaceAll("'", "''") + "',";
                }
                if (viewTextList.endsWith(",")) {
                    viewTextList = viewTextList.substring(0, viewTextList.length()-1);
                }
              /*  String update = "UPDATE POSTS SET " +
						" PARENTDOCID = " + doc.parentDocID + 
						", PARENTDOCTYPE = " + doc.parentDocType + 
						", AUTHOR = '" + doc.getAuthorID() + "'" +
						", ISPUBLIC = " + doc.isShared() +
						", CONTENT = '" + doc.getContent() + "'" +
						", SIGN = '" + doc.getSign() + "'" +
						", SIGNEDFIELDS = '" + doc.getSignedFields() + "'" +
						", FORM = '" + doc.form + "'" +
						", DEFAULTRULEID = '" + doc.getDefaultRuleID() + "'" +
						", VIEWNUMBER = " + doc.getViewNumber() +
						", VIEWDATE = " + (viewDate != null ? "'" + new Timestamp(viewDate.getTime()) + "'": "null") + "" +
						", VIEWTEXT1 = '" + doc.getViewTextList().get(1).replace("'", "''") + "'" +
						", VIEWTEXT2 = '" + doc.getViewTextList().get(2).replace("'", "''") + "'" +
						", VIEWTEXT3 = '" + doc.getViewTextList().get(3).replace("'", "''") + "'" +
						", DOCTYPE = " + Const.DOCTYPE_TOPIC +
						", LASTUPDATE = '" + Database.sqlDateTimeFormat.format(new java.util.Date()) + "'" +
						", TOPICDATE = '" + new Timestamp(doc.getPostDate().getTime()) + "'" +
						", VIEWTEXT = '" + doc.getViewText().replace("'", "''") + "'" + 
						" where DOCID=" + doc.getDocID();*/
                String update = "UPDATE POSTS SET " +
                        " PARENTDOCID = " + doc.parentDocID +
                        ", PARENTDOCTYPE = " + doc.parentDocType +
                        ", AUTHOR = '" + doc.getAuthorID() + "'" +
                        ", ISPUBLIC = " + doc.isShared() +
                        ", CONTENT = '" + doc.getContent() + "'" +
                        ", SIGN = '" + doc.getSign() + "'" +
                        ", SIGNEDFIELDS = '" + doc.getSignedFields() + "'" +
                        ", FORM = '" + doc.form + "'" +
                        ", DEFAULTRULEID = '" + doc.getDefaultRuleID() + "'" +
                        ", VIEWNUMBER = " + doc.getViewNumber() +
                        ", VIEWDATE = " + (viewDate != null ? "'" + new Timestamp(viewDate.getTime()) + "'": "null") + "" +
                        ", " + DatabaseUtil.getViewTextValues(doc) + ", DOCTYPE = " + Const.DOCTYPE_TOPIC +
                        ", LASTUPDATE = '" + Database.sqlDateTimeFormat.format(new java.util.Date()) + "'" +
                        ", TOPICDATE = '" + new Timestamp(doc.getPostDate().getTime()) + "'" +
                        ", VIEWTEXT = '" + doc.getViewText().replace("'", "''") + "'" +
                        " where DOCID=" + doc.getDocID();
				s.executeUpdate(update);

				// =========== BLOBS ===========
				ResultSet blobs = s.executeQuery("SELECT ID, DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE " +
						"FROM CUSTOM_BLOBS_POSTS " +
						"WHERE DOCID = " + doc.getDocID());
				while (blobs.next()) {
					if (!doc.blobFieldsMap.containsKey(blobs.getString("NAME"))) {
						blobs.deleteRow();
						continue;
					}
					BlobField existingBlob = doc.blobFieldsMap.get(blobs.getString("NAME"));
					BlobFile tableFile = new BlobFile();
					tableFile.originalName = blobs.getString("ORIGINALNAME");
					tableFile.checkHash = blobs.getString("CHECKSUM");
					if (existingBlob.findFile(tableFile) == null) {
						blobs.deleteRow();
						continue;
					}
					BlobFile existingFile = existingBlob.findFile(tableFile);
					if (!existingFile.originalName.equals(tableFile.originalName)) {
						blobs.updateString("ORIGINALNAME", existingFile.originalName);
						blobs.updateRow();
					}
				}
				/* now add files that are absent in database */
				for (Entry<String, BlobField> blob: doc.blobFieldsMap.entrySet()) {
					PreparedStatement ps = conn.prepareStatement("SELECT ID, DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE " +
							"FROM CUSTOM_BLOBS_TASKS " +
							"WHERE DOCID = ? AND NAME = ? AND CHECKSUM = ?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
					for (BlobFile bfile: blob.getValue().getFiles()) {
						ps.setInt(1, doc.getDocID());
						ps.setString(2, blob.getKey());
						ps.setString(3, bfile.checkHash);
						blobs = ps.executeQuery();
						if (blobs.next()) {
							if (!bfile.originalName.equals(blobs.getString("ORIGINALNAME"))) {
								blobs.updateString("ORIGINALNAME", bfile.originalName);
								blobs.updateRow();
							}
						} else {
							blobs.moveToInsertRow();
							blobs.updateInt("DOCID", doc.getDocID());
							blobs.updateString("NAME", blob.getKey());
							blobs.updateString("ORIGINALNAME", bfile.originalName);
							blobs.updateString("CHECKSUM", bfile.checkHash);
							InputStream is = new FileInputStream(new File(bfile.path));
							blobs.updateBinaryStream("VALUE", is);
							blobs.insertRow();
							is.close();
						}
						Environment.fileToDelete.add(bfile.path);
					}
					ps.close();
				}
				db.updateAccessTables(conn, doc, baseTable);
				update = "DELETE FROM FORUM_TREE_PATH " +
						" WHERE DESCENDANT IN (SELECT DESCENDANT FROM FORUM_TREE_PATH WHERE ANCESTOR = " + doc.getDocID() + ") " +
						" AND ANCESTOR IN (SELECT ANCESTOR FROM FORUM_TREE_PATH WHERE DESCENDANT = " + doc.getDocID() + " AND ANCESTOR != DESCENDANT)";
				PreparedStatement pst = conn.prepareStatement(update);
				pst.executeUpdate();
				update = "INSERT INTO FORUM_TREE_PATH (ANCESTOR, DESCENDANT, LENGTH) " +
						" SELECT supertree.ANCESTOR, subtree.descendant, supertree.LENGTH + subtree.LENGTH + 1 as length " +
						" FROM FORUM_TREE_PATH as supertree " +
						" CROSS JOIN FORUM_TREE_PATH as subtree " +
						" WHERE supertree.descendant = " + doc.parentDocID +
						" AND subtree.ancestor = " + doc.getDocID();
				pst = conn.prepareStatement(update);
				pst.executeUpdate();
				conn.commit();
				pst.close();
				s.close();
				IUsersActivity ua = db.getUserActivity();
				ua.postModify(oldDoc, doc, user);
			} catch (SQLException e) {
				DatabaseUtil.debugErrorPrint(e);
				return -1;
			} catch (FileNotFoundException fe) {
				AppEnv.logger.errorLogEntry(fe);
				return -1;
			} catch (IOException ioe) {
				AppEnv.logger.errorLogEntry(ioe);
				return -1;
			} catch (DocumentException e) {
				AppEnv.logger.errorLogEntry(e);
			} finally {
				dbPool.returnConnection(conn);
			}
			return doc.getDocID();
		} else {
			throw new DocumentAccessException(ExceptionType.DOCUMENT_WRITE_ACCESS_RESTRICTED, user.getUserID());
		}	
	}

	@Override
	public int deletePost(Post topic, Set<String> complexUserID, String absoluteUserID) {
		// TODO Auto-generated method stub
		return 0;
	}

	

	@Override
	public _ViewEntryCollection getForumTopics(ISelectFormula condition, User user, int pageNum, int pageSize, Set<String> toExpandResp, RunTimeParameters parameters) {
		 ViewEntryCollection coll = new ViewEntryCollection(pageSize, user, parameters);
	        Set<String> users = user.getAllUserGroups();
	        Connection conn = dbPool.getConnection();
	        try {
	            conn.setAutoCommit(false);
	            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	            if (pageNum == 0){
	                String sql = condition.getCountCondition(users,parameters.getFilters());
	                ResultSet rs = s.executeQuery(sql);
	                if (rs.next()){
	                    pageNum = RuntimeObjUtil.countMaxPage(rs.getInt(1), pageSize);
	                }
	            }
	            int offset = db.calcStartEntry(pageNum, pageSize);
	            String sql = condition.getCondition(users, pageSize, offset, parameters.getFilters(), parameters.getSorting(), true);
	            ResultSet rs = s.executeQuery(sql);
	            if (rs.next()){
	                ViewEntry entry = new ViewEntry(rs, db, toExpandResp, user,parameters.getDateFormat());
	                coll.add(entry);
	                coll.setCount(rs.getInt(1));
	                while (rs.next()){
	                    entry = new ViewEntry(rs, db, toExpandResp, user, parameters.getDateFormat());
	                    coll.add(entry);
	                }
	            }
	            conn.commit();
	            s.close();
	            rs.close();

	        } catch (SQLException e) {
	            DatabaseUtil.errorPrint(db.getDbID(), e);
	        } catch (Exception e) {
	            Database.logger.errorLogEntry(e);
	        } finally {
	            dbPool.returnConnection(conn);
	        }
	        coll.setCurrentPage(pageNum);
	        return coll.getScriptingObj();
		
	}

    @Override
    public _ViewEntryCollection getTopicsCollection(ISelectFormula condition, User user, int pageNum, int pageSize, RunTimeParameters parameters, boolean checkResponse) {
        return null;
    }


}
