package kz.flabs.dataengine.postgresql;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.*;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.ExceptionType;
import kz.flabs.runtimeobj.DocumentCollection;
import kz.flabs.runtimeobj.document.BlobField;
import kz.flabs.runtimeobj.document.BlobFile;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.project.Block;
import kz.flabs.runtimeobj.document.project.Coordinator;
import kz.flabs.runtimeobj.document.project.Project;
import kz.flabs.runtimeobj.document.project.Recipient;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.users.User;
import kz.flabs.webrule.query.QueryFieldRule;
import kz.pchelka.env.Environment;
import org.apache.commons.dbcp.DelegatingConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;

public class ProjectOnDatabase extends kz.flabs.dataengine.h2.ProjectOnDatabase  implements IProjects, Const{

	public ProjectOnDatabase(IDatabase db) {
		super(db);
	}

	@Override
	public StringBuffer getProjectsByCondition(IQueryFormula condition, Set<String> complexUserID, String absoluteUserID, QueryFieldRule[] fields, int offset, int pageSize, Set<DocID> toExpandResponses) throws DocumentException {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			
			String sql = condition.getSQL(complexUserID) + " LIMIT " + (pageSize != -1 ? pageSize : "ALL") + " OFFSET " + offset;
			ResultSet rs = s.executeQuery(sql);

			while (rs.next()) {
				int docID = rs.getInt("DOCID");
				int docType = rs.getInt("DOCTYPE");
				int attach = rs.getInt("HAS_ATTACHMENT");
                String ddbid = rs.getString("DDBID");
				String form = rs.getString("FORM");
				StringBuffer value = new StringBuffer(60);

				if (this.db.hasResponse(conn, docID, docType, complexUserID, null)) {
					value.append("<hasresponse>true</hasresponse>");
				}
		
			     Employer emp =  db.getStructure().getAppUser(absoluteUserID);
				xmlContent.append("<entry isread=\"" + usersActivity.isRead(conn, docID, docType, absoluteUserID) + "\" hasattach=\"" + Integer.toString(attach) + "\" doctype=\"" + DOCTYPE_PROJECT
						+ "\"  " + "docid=\"" + docID + "\" id=\"" + ddbid + "\" "
						+ "favourites=\"" + db.isFavourites(conn, docID, docType, emp) + "\" topicid=\"" + rs.getInt("TOPICID") + "\" "
						+ "url=\"Provider?type=edit&amp;element=project&amp;id=" + form						
						+ "&amp;key=" + docID + "&amp;docid=" + rs.getString("DDBID") + "\">" + getViewContent(rs) +  value);
				
				if (toExpandResponses.size() > 0) {
					for (DocID doc : toExpandResponses) {
						if (doc.id == docID && doc.type == DOCTYPE_PROJECT) {
							DocumentCollection responses = this.db.getDescendants(docID, DOCTYPE_PROJECT, null,1, complexUserID, absoluteUserID);
							if (responses.count > 0) {
								xmlContent.append("<responses>" + responses.xmlContent + "</responses>");
							}
						}
					}
				}
				xmlContent.append("</entry>");

			}
			s.close();
			rs.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {	
			dbPool.returnConnection(conn);
		}
		return xmlContent;
	}	
	
	@Override
	public int insertProject(Project doc, User user)
			throws DocumentException {
		int id = doc.getDocID();
        Connection conn = dbPool.getConnection();
        Connection dconn = ((DelegatingConnection) conn).getInnermostDelegate();
		int keyProject = 0;
		try {
          //  LargeObjectManager lobj = jdbc4conn.getLargeObjectAPI();
			conn.setAutoCommit(false);
			Date viewDate = doc.getViewDate();	
			String fieldsAsText = "LASTUPDATE, AUTHOR, AUTOSENDAFTERSIGN, " 
					+ "AUTOSENDTOSIGN, BRIEFCONTENT, CONTENTSOURCE, COORDSTATUS, "
					+ "REGDATE, PROJECTDATE,  VN, VNNUMBER, DOCVERSION, ISREJECTED, DOCTYPE,"
					+ "DDBID, VIEWTEXT, VIEWICON, FORM, DOCFOLDER, DELIVERYTYPE, SENDER, " 
					+ "NOMENTYPE, REGDOCID, HAR, PROJECT, VIEWTEXT1, VIEWTEXT2, VIEWTEXT3, VIEWTEXT4, VIEWTEXT5, VIEWTEXT6," +
                    "  VIEWTEXT7, VIEWNUMBER, "
					+ "VIEWDATE, DEFAULTRULEID, HAS_ATTACHMENT, PARENTDOCID, PARENTDOCTYPE, CATEGORY, " 
					+ "ORIGIN, COORDINATS, CITY, STREET, HOUSE, PORCH, FLOOR, APARTMENT, RESPONSIBLE, "
					+ "CTRLDATE, SUBCATEGORY, CONTRAGENT, PODRYAD, SUBPODRYAD, EXECUTOR, RESPOST, AMOUNTDAMAGE";
			String valuesAsText = "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?";
			if (id != 0 && doc.hasField("recID")){
				fieldsAsText = "DOCID, " + fieldsAsText;
				valuesAsText = id + ", " + valuesAsText;
			}
			PreparedStatement statProject = conn.prepareStatement("INSERT INTO PROJECTS (" + fieldsAsText + ") values (" + valuesAsText + ")", Statement.RETURN_GENERATED_KEYS);
			statProject.setTimestamp(1, new Timestamp(doc.getLastUpdate().getTime()));
			statProject.setString(2, doc.getAuthorID());
			statProject.setInt(3, doc.getAutoSendAfterSign());
			statProject.setInt(4, doc.getAutoSendToSign());
			statProject.setString(5, doc.getBriefContent());

            LargeObjectManager lom = ((org.postgresql.PGConnection)dconn).getLargeObjectAPI();
            long oid = lom.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
			LargeObject lo = lom.open(oid, LargeObjectManager.WRITE);
			lo.write(doc.getContentSource().getBytes(Charset.forName("UTF-8")), 0, doc.getContentSource().getBytes(Charset.forName("UTF-8")).length); //.write(baos.toByteArray(), 0, baos.toByteArray().length);
			lo.close();
			statProject.setLong(6, oid);

			statProject.setInt(7, doc.getCoordStatus());
			statProject.setTimestamp(8, new Timestamp(new java.util.Date().getTime()));
			statProject.setTimestamp(9, new Timestamp(doc.getProjectDate().getTime()));
			statProject.setString(10, doc.getVn());
			statProject.setInt(11, doc.getVnNumber());
			statProject.setInt(12, doc.getDocVersion());
			statProject.setInt(13, doc.getIsRejected());
			statProject.setInt(14, Const.DOCTYPE_PROJECT);
			statProject.setString(15, doc.getDdbID());
			statProject.setString(16, doc.getViewText());
			statProject.setString(17, doc.getViewIcon());
			statProject.setString(18, doc.getValueAsString("form")[0]);
			statProject.setString(19, doc.getDocFolder());
			statProject.setString(20, doc.getDeliveryType());
			statProject.setString(21, doc.getSender());
			statProject.setInt(22, doc.getNomenType());

			if (doc.getRegDocID() != 0) {
				statProject.setInt(23, doc.getRegDocID());
			} else {
				statProject.setNull(23, Types.INTEGER);
			}

			if (doc.getHar() != 0) {
				statProject.setInt(24, doc.getHar());
			} else {
				statProject.setNull(24, Types.INTEGER);
			}

			if (doc.getProject() != 0) {
				statProject.setInt(25, doc.getProject());
			} else {
				statProject.setNull(25, Types.INTEGER);
			}

			statProject.setString(26, doc.getViewTextList().get(1).replace("'", "''"));
			statProject.setString(27, doc.getViewTextList().get(2).replace("'", "''"));
			statProject.setString(28, doc.getViewTextList().get(3).replace("'", "''"));
            statProject.setString(29, doc.getViewTextList().get(1).replace("'", "''"));
            statProject.setString(30, doc.getViewTextList().get(2).replace("'", "''"));
            statProject.setString(31, doc.getViewTextList().get(3).replace("'", "''"));
            statProject.setString(32, doc.getViewTextList().get(3).replace("'", "''"));
			statProject.setBigDecimal(33, doc.getViewNumber());
			

			if (viewDate != null){
				statProject.setTimestamp(34, new Timestamp(viewDate.getTime()));
			}else{
				statProject.setNull(34, Types.TIMESTAMP);
			}
			statProject.setString(35, doc.getDefaultRuleID());
			statProject.setInt(36, doc.blobFieldsMap.size());
			statProject.setInt(37, doc.parentDocID);
			statProject.setInt(38, doc.parentDocType);
			if (doc.getCategory() != 0) {
				statProject.setInt(39, doc.getCategory());
			} else {
				statProject.setNull(39, Types.INTEGER);
			}
			statProject.setString(40, doc.getOrigin());
			
			statProject.setString(41, doc.getCoordinats());
			if (doc.getCity() != 0) {
				statProject.setInt(42, doc.getCity());
			} else {
				statProject.setNull(42, Types.INTEGER);
			}
			statProject.setString(43, doc.getStreet());
			statProject.setString(44, doc.getHouse());
			statProject.setString(45, doc.getPorch());
			statProject.setString(46, doc.getFloor());
			statProject.setString(47, doc.getApartment());
			statProject.setString(48, doc.getResponsibleSection());
			if (doc.getControlDate() != null) {
				statProject.setTimestamp(49, new Timestamp(doc.getControlDate().getTime()));
			} else {
				statProject.setNull(49, Types.TIMESTAMP);
			}
		
			if (doc.getSubcategory() != 0) {
				statProject.setInt(50, doc.getSubcategory());
			} else {
				statProject.setNull(50, Types.INTEGER);
			}
			statProject.setString(51, doc.getContragent());
			statProject.setString(52, doc.getPodryad());
			statProject.setString(53, doc.getSubpodryad());
			statProject.setString(54, doc.getExecutor());
			statProject.setString(55, doc.getResponsiblePost());
			statProject.setString(56, doc.getAmountDamage());
			
			statProject.executeUpdate();
			ResultSet rsp = statProject.getGeneratedKeys();
			if (rsp.next()) {
				keyProject = rsp.getInt(1);
			}

			for (Recipient recipient : doc.getRecipients()) {
				insertRecipient(keyProject, recipient, conn);				
			}
			
			for (Block block : doc.getBlocksList()) {
				int blockID = insertBlock(keyProject, block, conn);
				for (Coordinator coordinator: block.getCoordinators()) {
					insertCoordinator(blockID, coordinator, conn);					
				}
			}

			/* BLOBS */
			if (id != 0 && !doc.hasField("recID")) { 
				PreparedStatement s0 = conn.prepareStatement("SELECT * FROM CUSTOM_BLOBS_PROJECTS WHERE DOCID = " + id);
				ResultSet rs0 = s0.executeQuery();
				while (rs0.next()) {
					PreparedStatement s1 = conn.prepareStatement("INSERT INTO CUSTOM_BLOBS_PROJECTS (DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE, COMMENT)values(?, ?, ?, ?, ?, ?)");
					s1.setInt(1, keyProject);
					s1.setString(2, rs0.getString("NAME"));
					s1.setString(3, rs0.getString("ORIGINALNAME"));
					s1.setString(4, rs0.getString("CHECKSUM"));
					s1.setString(6, rs0.getString("COMMENT"));
					InputStream is = rs0.getBinaryStream("VALUE");
					s1.setBinaryStream(5, is, is.toString().length());
					s1.executeUpdate();
					s1.close();
				}
				rs0.close();
				s0.close();
			} else {
				for (Entry<String, BlobField> blob : doc.blobFieldsMap.entrySet()) {
					PreparedStatement ps = conn
							.prepareStatement("INSERT INTO CUSTOM_BLOBS_PROJECTS (DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE, COMMENT)values(?, ?, ?, ?, ?, ?)");
					BlobField bf = blob.getValue();
					for (BlobFile bfile: bf.getFiles()) {
						ps.setInt(1, keyProject);
						ps.setString(2, bf.name);
						ps.setString(3, bfile.originalName);
						ps.setString(4, bfile.checkHash);
						ps.setString(6, bfile.comment);
						if (!bfile.path.equalsIgnoreCase("")){
							File file = new File(bfile.path);
							FileInputStream fin = new FileInputStream(file);
							ps.setBinaryStream(5, fin, (int)file.length());
							ps.executeUpdate();
							fin.close();
							Environment.fileToDelete.add(bfile.path);
						}else {
							ps.setBytes(5, bfile.getContent());
							ps.executeUpdate();
						}
					}
					ps.close();
				}
			}


			db.insertToAccessTables(conn, "PROJECTS", keyProject, doc);

			//CachePool.flush();
			conn.commit();
			statProject.close();
			if (!doc.hasField("recID")){
				IUsersActivity ua = db.getUserActivity();
				ua.postCompose(doc, user);
				ua.postMarkRead(keyProject, doc.docType, user);
			}
		} catch (FileNotFoundException fe) {
			DatabaseUtil.errorPrint(db.getDbID(), fe);
			return -1;
		} catch (DocumentException de) {
			DatabaseUtil.errorPrint(db.getDbID(), de);
			return -1;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;
		} catch (IOException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;
		} finally {	
			dbPool.returnConnection(conn);
		}
		return keyProject;
	}

	public int updateProject(Project doc, User user)throws DocumentAccessException, DocumentException {
		if (doc.hasEditor(user.getAllUserGroups())) {
			Project oldDoc = this.getProjectByID(doc.getDocID(), user.getAllUserGroups(), user.getUserID());
            Connection conn = dbPool.getConnection();
            Connection dconn = ((DelegatingConnection) conn).getInnermostDelegate();
			try {
				Statement statProject = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
				conn.setAutoCommit(false);
				PreparedStatement updatePrj = conn.prepareStatement("UPDATE PROJECTS set LASTUPDATE = ?, " +
						"AUTHOR = ?, AUTOSENDAFTERSIGN = ?, AUTOSENDTOSIGN = ?, BRIEFCONTENT = ?, " +
						"CONTENTSOURCE = ?, COORDSTATUS = ?, VNNUMBER = ?, DOCVERSION = ?, ISREJECTED = ?, " +
						"SENDER = ?, DOCTYPE = ?, DDBID = ?, VIEWTEXT = ?, VIEWICON = ?, " +
						"FORM = ?, " +
						"SYNCSTATUS = ?, DOCFOLDER = ?, DELIVERYTYPE = ?, REGDATE = ?, PROJECTDATE = ?, NOMENTYPE = ?, " +
						"REGDOCID = ?, HAR = ?, PROJECT = ?, VIEWTEXT1 = ?, VIEWTEXT2 = ?, VIEWTEXT3 = ?, VIEWTEXT4 = ?, VIEWTEXT5 = ?, VIEWTEXT6 = ?, " +
                        "VIEWTEXT7 = ?, VIEWNUMBER = ?, " +
						"VIEWDATE = ?, DEFAULTRULEID = ?, CATEGORY = ?, ORIGIN = ?, COORDINATS = ?, CITY = ?, STREET = ?, " +
						"HOUSE = ?, PORCH = ?, FLOOR = ?, APARTMENT = ?, RESPONSIBLE = ?, " +
						" CTRLDATE = ?, SUBCATEGORY = ?, CONTRAGENT = ?, PODRYAD = ?, SUBPODRYAD = ?, EXECUTOR = ?, VN = ?, " +
						" RESPOST = ?, AMOUNTDAMAGE = ? " + 
						"WHERE DOCID = " + doc.getDocID());
				updatePrj.setTimestamp(1, new Timestamp(doc.getLastUpdate().getTime()));
				updatePrj.setString(2, doc.getAuthorID());
				updatePrj.setInt(3, doc.getAutoSendAfterSign());
				updatePrj.setInt(4, doc.getAutoSendToSign());
				updatePrj.setString(5, doc.getBriefContent());

                LargeObjectManager lom = ((org.postgresql.PGConnection)dconn).getLargeObjectAPI();
                long oid = lom.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
				LargeObject lo = lom.open(oid, LargeObjectManager.WRITE);
				lo.write(doc.getContentSource().getBytes(Charset.forName("UTF-8")), 0, doc.getContentSource().getBytes(Charset.forName("UTF-8")).length);
				lo.close();
				updatePrj.setLong(6, oid);

				updatePrj.setInt(7, doc.getCoordStatus());
				updatePrj.setInt(8, doc.getVnNumber());
				updatePrj.setInt(9, doc.getDocVersion());
				updatePrj.setInt(10, doc.getIsRejected());
				updatePrj.setString(11, doc.getSender());
				updatePrj.setInt(12, Const.DOCTYPE_PROJECT);
				updatePrj.setString(13, doc.getDdbID());
				updatePrj.setString(14, doc.getViewText());
				updatePrj.setString(15, doc.getViewIcon());
				updatePrj.setString(16, doc.getValueAsString("form")[0]);
				updatePrj.setInt(17, 0);
				updatePrj.setString(18, doc.getDocFolder());
				updatePrj.setString(19, doc.getDeliveryType());
				updatePrj.setTimestamp(20, new Timestamp(doc.getRegDate().getTime()));
				updatePrj.setTimestamp(21, new Timestamp(doc.getProjectDate().getTime()));
				updatePrj.setInt(22, doc.getNomenType());

				if (doc.getRegDocID() != 0) {
					updatePrj.setInt(23, doc.getRegDocID());
				} else {
					updatePrj.setNull(23, Types.INTEGER);
				}

				if (doc.getHar() != 0) {
					updatePrj.setInt(24, doc.getHar());
				} else {
					updatePrj.setNull(24, Types.INTEGER);
				}

				if (doc.getProject() != 0) {
					updatePrj.setInt(25, doc.getProject());
				} else {
					updatePrj.setNull(25, Types.INTEGER);
				}

				updatePrj.setString(26, doc.getViewTextList().get(1).replace("'", "''"));
                updatePrj.setString(27, doc.getViewTextList().get(2).replace("'", "''"));
                updatePrj.setString(28, doc.getViewTextList().get(3).replace("'", "''"));
                updatePrj.setString(29, doc.getViewTextList().get(4).replace("'", "''"));
                updatePrj.setString(30, doc.getViewTextList().get(5).replace("'", "''"));
                updatePrj.setString(31, doc.getViewTextList().get(6).replace("'", "''"));
                updatePrj.setString(32, doc.getViewTextList().get(7).replace("'", "''"));
				updatePrj.setBigDecimal(33, doc.getViewNumber());
				updatePrj.setTimestamp(34, new Timestamp(doc.getViewDate().getTime()));
				updatePrj.setString(35, doc.getDefaultRuleID());


				if (doc.getCategory() != 0) {
					updatePrj.setInt(36, doc.getCategory());
				} else {
					updatePrj.setNull(36, Types.INTEGER);
				}
				
				updatePrj.setString(37, doc.getOrigin());
				
				updatePrj.setString(38, doc.getCoordinats());
				if (doc.getCity() != 0) {
					updatePrj.setInt(39, doc.getCity());
				} else {
					updatePrj.setNull(39, Types.INTEGER);
				}
				updatePrj.setString(40, doc.getStreet());
				updatePrj.setString(41, doc.getHouse());
				updatePrj.setString(42, doc.getPorch());
				updatePrj.setString(43, doc.getFloor());
				updatePrj.setString(44, doc.getApartment());
				updatePrj.setString(45, doc.getResponsibleSection());
				if (doc.getControlDate() != null) {
					updatePrj.setTimestamp(46, new Timestamp(doc.getControlDate().getTime()));
				} else {
					updatePrj.setNull(46, Types.TIMESTAMP);
				}
				if (doc.getSubcategory() != 0) {
					updatePrj.setInt(47, doc.getSubcategory());
				} else {
					updatePrj.setNull(47, Types.INTEGER);
				}
				updatePrj.setString(48, doc.getContragent());
				updatePrj.setString(49, doc.getPodryad());
				updatePrj.setString(50, doc.getSubpodryad());
				updatePrj.setString(51, doc.getExecutor());
				updatePrj.setString(52, doc.getVn());
				updatePrj.setString(53, doc.getResponsiblePost());
				updatePrj.setString(54, doc.getAmountDamage());
				
				updatePrj.executeUpdate();
				String deleteRecipients = "DELETE FROM PROJECTRECIPIENTS WHERE DOCID = " + doc.getDocID();
				statProject.executeUpdate(deleteRecipients);
				for (Recipient recipient : doc.getRecipients()) {
					insertRecipient(doc.getDocID(), recipient, conn);
				}
				String deleteBlocks = "DELETE FROM COORDBLOCKS WHERE DOCID = " + doc.getDocID();
				statProject.executeUpdate(deleteBlocks);
				for (Block block : doc.getBlocksList()) {
					int blockID = insertBlock(doc.getDocID(), block, conn);
					for (Coordinator coordinator: block.getCoordinators()) {
						insertCoordinator(blockID, coordinator, conn);					
					}
				}
				db.updateBlobTables(conn, doc, baseTable);
				db.updateAccessTables(conn, doc, baseTable);
				conn.commit();
				statProject.close();
				updatePrj.close();
				IUsersActivity ua = db.getUserActivity();
				ua.postModify(oldDoc, doc, user);
			} catch (DocumentException de) {
				AppEnv.logger.errorLogEntry(de);
				return -1;
			} catch (SQLException e) {
				DatabaseUtil.errorPrint(db.getDbID(), e);
				return -1;
			} catch (FileNotFoundException e) {
				DatabaseUtil.errorPrint(db.getDbID(), e);
				return -1;
			} catch (IOException e) {
				DatabaseUtil.errorPrint(db.getDbID(), e);
				return -1;
			} finally {	
				dbPool.returnConnection(conn);
			}
			return doc.getDocID();
		} else {
			throw new DocumentAccessException(ExceptionType.DOCUMENT_WRITE_ACCESS_RESTRICTED, user.getUserID());
		}		
	}
	
	@Override
	public int recalculate() {	
			Connection conn = dbPool.getConnection();
			try {				
				Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);	
				conn.setAutoCommit(false);	
				String update = "update projects set dbd = (select ROUND(EXTRACT(EPOCH from ctrldate - current_timestamp) / 86400))";
				s.executeUpdate(update);
				conn.commit();
				s.close();
			} catch (SQLException e) {
				AppEnv.logger.errorLogEntry(e);
				return -1;
			} finally {
				dbPool.returnConnection(conn);
			}
			return 0;		
	}
	
	public int recalculate(int docID) {	
		Connection conn = dbPool.getConnection();
		try {				
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);	
			conn.setAutoCommit(false);	
			
			String update = "update projects set dbd = (select ROUND(EXTRACT(DAY from (ctrldate - current_timestamp)))) where docid ="+docID;
			String select = "select dbd from projects where docid ="+docID;
	
			s.executeUpdate(update);
			conn.commit();
			ResultSet rs = s.executeQuery(select);
			conn.commit();
			if(rs.next()){
				return rs.getInt(1);
			} else {
				return 0;
			}
		} catch (SQLException e) {
			AppEnv.logger.errorLogEntry(e);
			return -1;
		} finally {
			dbPool.returnConnection(conn);
		}	
	}

}
