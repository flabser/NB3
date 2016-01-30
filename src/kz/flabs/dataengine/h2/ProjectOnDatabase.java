package kz.flabs.dataengine.h2;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.*;
import kz.flabs.dataengine.h2.queryformula.ProjectQueryFormula;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.ExceptionType;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.DocumentCollection;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.project.Block;
import kz.flabs.runtimeobj.document.project.Coordinator;
import kz.flabs.runtimeobj.document.project.Project;
import kz.flabs.runtimeobj.document.project.Recipient;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.users.User;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.QueryType;
import kz.flabs.webrule.query.QueryFieldRule;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ProjectOnDatabase  extends DatabaseCore implements IProjects, Const{
	protected IDBConnectionPool dbPool;
	protected IDatabase db;
	protected IUsersActivity usersActivity;

	protected static String baseTable = "PROJECTS"; 
	private String projectFields = " p.REGDATE, p.DOCID, p.DOCTYPE, p.HAS_ATTACHMENT, p.FORM, p.VIEWTEXT, " + DatabaseUtil.getViewTextList("p") + ", p.VIEWNUMBER, p.VIEWDATE, p.VN, p.VNNUMBER, p.DOCVERSION, p.ISREJECTED, p.COORDSTATUS, p.TOPICID, p.PROJECT, p.DDBID, p.RESPONSIBLE ";
	public ProjectOnDatabase(IDatabase db) {
		this.db = db;
		this.dbPool = db.getConnectionPool();
		usersActivity = db.getUserActivity();
	}


	public Project getProjectByID(int docID, Set<String> complexUserID, String absoluteUserID)  {
		Connection conn = dbPool.getConnection();
		Project prj = new Project(db, absoluteUserID);
		try {
			conn.setAutoCommit(false);
			/*String sql = "select distinct " + projectFields + " from PROJECTS p, READERS_PROJECTS rp where p.DOCID = rp.DOCID "
			+ "and rp.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ") and p.DOCID = " + docID;
			 */
			String sql = "select * from PROJECTS p where exists (select * from readers_projects as rp where rp.docid = " + docID + " and rp.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")) and p.docid = " + docID;			
			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				prj = fillProjectDoc(rs, prj);

				ResultSet recipientResultSet = conn.createStatement().executeQuery("select * from PROJECTRECIPIENTS where DOCID=" + docID);
				while (recipientResultSet.next()) {
					Recipient recipient = new Recipient();
					recipient.setRecordID(recipientResultSet.getInt("ID"));
					recipient.setUserID(recipientResultSet.getString("RECIPIENT"));
					prj.addRecipient(recipient);
				}

				ResultSet blockResultSet = conn.createStatement().executeQuery("select * from COORDBLOCKS where DOCID=" + docID);
				while (blockResultSet.next()) {
					Block block = new Block(prj);
					int blockID = blockResultSet.getInt("ID");
					block.blockID = blockID;
					block.setBlockID(blockID);
					block.setBlockNum(blockResultSet.getInt("BLOCKNUMBER"));
					block.delayTime = blockResultSet.getInt("DELAYTIME");
					block.type = blockResultSet.getInt("TYPE");
					block.status = blockResultSet.getInt("STATUS");
					block.setCoorDate(blockResultSet.getTimestamp("COORDATE"));
										
					ResultSet coordsResultSet = conn.createStatement().executeQuery("select *  from COORDINATORS where BLOCKID=" + blockID);
					while (coordsResultSet.next()) {
						Coordinator coord = new Coordinator(block);				
						coord.userID = coordsResultSet.getString("COORDINATOR");
						coord.setCoordType(coordsResultSet.getInt("COORDTYPE"));
						coord.decision = coordsResultSet.getInt("DECISION");
						coord.comment = coordsResultSet.getString("COMMENT");
						coord.setCurrentRecipient(coordsResultSet.getInt("ISCURRENT"));
						coord.setDecisionDate(coordsResultSet.getTimestamp("DECISIONDATE"));
						coord.coorDate = coordsResultSet.getTimestamp("COORDATE");
						block.addCoordinator(coord);
					}
					coordsResultSet.close();
					prj.addBlock(block);			
				}
				blockResultSet.close();

				db.fillAccessRelatedField(conn, baseTable, prj.getDocID(), prj);		
				if (prj.hasEditor(complexUserID)) {
					prj.editMode = EDITMODE_EDIT;
				}
				
				db.fillBlobs(conn, prj, baseTable);
				
			}
			conn.commit();
			rs.close();
			pst.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		}finally{	
			dbPool.returnConnection(conn);
		}
		return prj;
	}


	public ArrayList<BaseDocument> getDocumentsByCondition(String query, HashSet<String> complexUserID, String absoluteUserID) throws QueryFormulaParserException, DocumentException, DocumentAccessException {
		FormulaBlocks preparedQueryFormula = new FormulaBlocks(query, QueryType.PROJECT);
		ProjectQueryFormula queryFormula = new ProjectQueryFormula("", preparedQueryFormula);
		return getDocumentsByCondition(queryFormula, complexUserID, absoluteUserID);
	}

	@Override
	public ArrayList<BaseDocument> getProjectsByCondition(String condition, Set<String> complexUserID, String absoluteUserID) throws DocumentException, DocumentAccessException {
		ArrayList<BaseDocument> docs = new ArrayList<BaseDocument>();	
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);	
			if (!"".equalsIgnoreCase(condition.trim())) condition = " and " + condition;
			String sql = "SELECT DISTINCT " + projectFields + " FROM PROJECTS p, READERS_PROJECTS rp WHERE p.DOCID = rp.DOCID and rp.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")" + 
					condition + " ORDER BY REGDATE";
			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				int docID = rs.getInt("DOCID");
				Document doc = getProjectByID(docID, complexUserID, absoluteUserID);			
				docs.add(doc);				
			}
			s.close();
			rs.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {	
			dbPool.returnConnection(conn);
		}
		return docs;
	}
	
	public ArrayList<BaseDocument> getDocumentsByCondition(IQueryFormula blocks, Set<String> complexUserID, String absoluteUserID) throws DocumentException, DocumentAccessException {
		ArrayList<BaseDocument> docs = new ArrayList<BaseDocument>();	
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);	
			String sql = blocks.getSQL(complexUserID);
			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				int docID = rs.getInt("DOCID");
				Document doc = getProjectByID(docID, complexUserID, absoluteUserID);			
				docs.add(doc);				
			}
			s.close();
			rs.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {	
			dbPool.returnConnection(conn);
		}
		return docs;
	}

	public int getProjectsByConditionCount(IQueryFormula condition,	Set<String> complexUserID, String absoluteUserID) {
		int count = 0;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = condition.getSQLCount(complexUserID);
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				count = rs.getInt(1);
			}
			s.close();
			rs.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {	
			dbPool.returnConnection(conn);
		}
		return count;
	}


	public int insertProject(Project doc, User user)
			throws DocumentException {
		int id = doc.getDocID();
		Connection conn = dbPool.getConnection();
		int keyProject = 0;
		try {
			conn.setAutoCommit(false);
			Date viewDate = doc.getViewDate();	
			String fieldsAsText = "LASTUPDATE, AUTHOR, AUTOSENDAFTERSIGN, "
					+ "AUTOSENDTOSIGN, BRIEFCONTENT, CONTENTSOURCE, COORDSTATUS, "
					+ "REGDATE, PROJECTDATE,  VN, VNNUMBER, DOCVERSION, ISREJECTED, DOCTYPE,"
					+ "DDBID, VIEWTEXT, VIEWICON, FORM, DOCFOLDER, DELIVERYTYPE, "
					+ "SENDER, NOMENTYPE, REGDOCID, HAR, PROJECT, CATEGORY, " + DatabaseUtil.getViewTextList("") + " ,"
					+ "VIEWNUMBER, VIEWDATE, DEFAULTRULEID, HAS_ATTACHMENT, PARENTDOCID, PARENTDOCTYPE, ORIGIN, "
					+ "COORDINATS, CITY, STREET, HOUSE, PORCH, FLOOR, APARTMENT, RESPONSIBLE, " 
					+ "CTRLDATE, SUBCATEGORY, CONTRAGENT, PODRYAD, SUBPODRYAD, EXECUTOR, RESPOST, AMOUNTDAMAGE";
			String valuesAsText = "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?";
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
			statProject.setClob(6, new StringReader(doc.getContentSource()));
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
			if (doc.getCategory() != 0) {
				statProject.setInt(26, doc.getCategory());
			} else {
				statProject.setNull(26, Types.INTEGER);
			}
			statProject.setString(27, doc.getViewTextList().get(1).replace("'", "''"));
			statProject.setString(28, doc.getViewTextList().get(2).replace("'", "''"));
			statProject.setString(29, doc.getViewTextList().get(3).replace("'", "''"));

		
			statProject.setBigDecimal(30, doc.getViewNumber());
			

			if (viewDate != null){
				statProject.setTimestamp(31, new Timestamp(viewDate.getTime()));
			}else{
				statProject.setNull(31, Types.TIMESTAMP);
			}
			
			statProject.setString(32, doc.getDefaultRuleID());
			statProject.setInt(33, doc.blobFieldsMap.size());
			statProject.setInt(34, doc.parentDocID);
			statProject.setInt(35, doc.parentDocType);
			statProject.setString(36, doc.getOrigin());
			
			statProject.setString(37, doc.getCoordinats());
			if (doc.getCity() != 0) {
				statProject.setInt(38, doc.getCity());
			} else {
				statProject.setNull(38, Types.INTEGER);
			}
			statProject.setString(39, doc.getStreet());
			statProject.setString(40, doc.getHouse());
			statProject.setString(41, doc.getPorch());
			statProject.setString(42, doc.getFloor());
			statProject.setString(43, doc.getApartment());
			statProject.setString(44, doc.getResponsibleSection());
			if (doc.getControlDate() != null) {
				statProject.setTimestamp(45, new Timestamp(doc.getControlDate().getTime()));
			} else {
				statProject.setNull(45, Types.TIMESTAMP);
			};
			if (doc.getSubcategory() != 0) {
				statProject.setInt(46, doc.getSubcategory());
			} else {
				statProject.setNull(46, Types.INTEGER);
			}
			statProject.setString(47, doc.getContragent());
			statProject.setString(48, doc.getPodryad());
			statProject.setString(49, doc.getSubpodryad());
			statProject.setString(50, doc.getExecutor());
			statProject.setString(51, doc.getResponsiblePost());
			statProject.setString(52, doc.getAmountDamage());
			
			statProject.executeUpdate();
			ResultSet rsp = statProject.getGeneratedKeys();
			if (rsp.next()) {
				keyProject = rsp.getInt(1);
			} else {
				keyProject = id;
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
					
			db.insertBlobTables(conn, id, keyProject, doc, baseTable);
			db.insertToAccessTables(conn, baseTable, keyProject, doc);
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
			try {
				Statement statProject = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
				conn.setAutoCommit(false);
				Date viewDate = doc.getViewDate();
                String viewTextStat = "";
                for (int i = 1; i <= DatabaseConst.VIEWTEXT_COUNT; i++) {
                    viewTextStat += "VIEWTEXT" + i + " = ?,";
                }
				PreparedStatement updatePrj = conn.prepareStatement("UPDATE PROJECTS set LASTUPDATE = ?, " +
						"AUTHOR = ?, AUTOSENDAFTERSIGN = ?, AUTOSENDTOSIGN = ?, BRIEFCONTENT = ?, " +
						"CONTENTSOURCE = ?, COORDSTATUS = ?, VNNUMBER = ?, DOCVERSION = ?, ISREJECTED = ?, " +
						"SENDER = ?, DOCTYPE = ?, DDBID = ?, VIEWTEXT = ?, VIEWICON = ?, " +
						"FORM = ?, " +
						"SYNCSTATUS = ?, DOCFOLDER = ?, DELIVERYTYPE = ?, REGDATE = ?, PROJECTDATE = ?, NOMENTYPE = ?, " + 
						"REGDOCID = ?, HAR = ?, PROJECT = ?, " + viewTextStat + " VIEWNUMBER = ?, " +
						"VIEWDATE = ?, DEFAULTRULEID = ?, CATEGORY = ?, " + 
						"ORIGIN = ?, COORDINATS = ?, CITY = ?, STREET = ?, HOUSE = ?, PORCH = ?, FLOOR = ?, APARTMENT = ?, RESPONSIBLE = ?, " +
						" CTRLDATE = ?, SUBCATEGORY = ?, CONTRAGENT = ?, PODRYAD = ?, SUBPODRYAD = ?, EXECUTOR = ?, VN = ?, " +
						" RESPOST = ?, AMOUNTDAMAGE = ? " + 
						"WHERE DOCID = " + doc.getDocID());
				updatePrj.setTimestamp(1, new Timestamp(doc.getLastUpdate().getTime()));
				updatePrj.setString(2, doc.getAuthorID());
				updatePrj.setInt(3, doc.getAutoSendAfterSign());
				updatePrj.setInt(4, doc.getAutoSendToSign());
				updatePrj.setString(5, doc.getBriefContent());
				updatePrj.setClob(6, new StringReader(doc.getContentSource()));
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

			
				updatePrj.setBigDecimal(29, doc.getViewNumber());

				if (viewDate != null){
					updatePrj.setTimestamp(30, new Timestamp(viewDate.getTime()));
				}else{
					updatePrj.setNull(30, Types.TIMESTAMP);
				}
				updatePrj.setString(31, doc.getDefaultRuleID());				

				if (doc.getCategory() != 0) {
					updatePrj.setInt(32, doc.getCategory());
				} else {
					updatePrj.setNull(32, Types.INTEGER);
				}
				
				updatePrj.setString(33, doc.getOrigin());
				
				updatePrj.setString(34, doc.getCoordinats());
				if (doc.getCity() != 0) { 
					updatePrj.setInt(35, doc.getCity());
				} else {
					updatePrj.setNull(35, Types.INTEGER);
				}
				updatePrj.setString(36, doc.getStreet());
				updatePrj.setString(37, doc.getHouse());
				updatePrj.setString(38, doc.getPorch());
				updatePrj.setString(39, doc.getFloor());
				updatePrj.setString(40, doc.getApartment());
				updatePrj.setString(41, doc.getResponsibleSection());
				if (doc.getControlDate() != null) {
					updatePrj.setTimestamp(42, new Timestamp(doc.getControlDate().getTime()));
				} else {
					updatePrj.setNull(42, Types.TIMESTAMP);
				}
				if (doc.getSubcategory() != 0) {
					updatePrj.setInt(43, doc.getSubcategory());
				} else {
					updatePrj.setNull(43, Types.INTEGER);
				}
				updatePrj.setString(44, doc.getContragent());
				updatePrj.setString(45, doc.getPodryad());
				updatePrj.setString(46, doc.getSubpodryad());
				updatePrj.setString(47, doc.getExecutor());
				updatePrj.setString(48, doc.getVn());
				updatePrj.setString(49, doc.getResponsiblePost());
				updatePrj.setString(50, doc.getAmountDamage());
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

	public Project fillProjectDoc(ResultSet rs, kz.flabs.runtimeobj.document.project.Project prj) {
		try {
			prj.setDocID(rs.getInt("DOCID"));
			prj.setAutoSendAfterSign(rs.getInt("AUTOSENDAFTERSIGN"));
			prj.setAutoSendToSign(rs.getInt("AUTOSENDTOSIGN"));
			prj.setBriefContent(rs.getString("BRIEFCONTENT"));
			prj.setCoordStatus(rs.getInt("COORDSTATUS"));
			prj.setVnNumber(rs.getInt("VNNUMBER"));
			prj.setVn(rs.getString("VN"));
			prj.setDocVersion(rs.getInt("DOCVERSION"));
			prj.setIsRejected(rs.getInt("ISREJECTED"));
			prj.setSender(rs.getString("SENDER"));
			prj.setDocFolder(rs.getString("DOCFOLDER"));			
			prj.setDeliveryType(rs.getString("DELIVERYTYPE"));
			prj.setNomenType(rs.getInt("NOMENTYPE"));
			prj.setProjectDate(rs.getTimestamp("PROJECTDATE"));
			prj.setRegDocID(rs.getInt("REGDOCID"));
			prj.setHar(rs.getInt("HAR"));
			prj.setProject(rs.getInt("PROJECT"));
			prj.setCategory(rs.getInt("CATEGORY"));	
			prj.setDefaultRuleID(rs.getString("DEFAULTRULEID"));
			prj.setOrigin(rs.getString("ORIGIN"));
			prj.setCoordinats(rs.getString("COORDINATS"));
			prj.setCity(rs.getInt("CITY"));
			prj.setStreet(rs.getString("STREET"));
			prj.setHouse(rs.getString("HOUSE"));
			prj.setPorch(rs.getString("PORCH"));
			prj.setFloor(rs.getString("FLOOR"));
			prj.setApartment(rs.getString("APARTMENT"));
			prj.setResponsibleSection(rs.getString("RESPONSIBLE"));
			prj.setControlDate(rs.getTimestamp("CTRLDATE"));
			prj.setSubcategory(rs.getInt("SUBCATEGORY"));
			prj.setContragent(rs.getString("CONTRAGENT"));
			prj.setPodryad(rs.getString("PODRYAD"));
			prj.setSubpodryad(rs.getString("SUBPODRYAD"));
			prj.setExecutor(rs.getString("EXECUTOR"));
			prj.setResponsiblePost(rs.getString("RESPOST"));
			prj.setAmountDamage(rs.getString("AMOUNTDAMAGE"));
			Clob clb = rs.getClob("CONTENTSOURCE");
			if (clb != null) {
				prj.setContentSource(clb.getSubString(1, (int)clb.length()));
			}
			fillViewTextData(rs, prj);
			fillSysData(rs, prj);
			prj.hasDiscussion = rs.getBoolean("HAS_TOPIC");
			prj.isValid = true;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		}

		return prj;
	}

	protected int insertRecipient(int projectKey, Recipient recipient, Connection conn){
		try{
			Statement statRecipient = conn.createStatement();
			String sqlRecipient = "insert into PROJECTRECIPIENTS(DOCID, RECIPIENT) values("
					+ projectKey + ", '" + recipient.getUserID() + "')";

			statRecipient.executeUpdate(sqlRecipient);

		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		}
		return -1;

	}

	protected int insertBlock(int projectKey, Block block, Connection conn){
		try{
			String sqlBlock = "insert into COORDBLOCKS(DOCID, TYPE, DELAYTIME, BLOCKNUMBER, STATUS, COORDATE) values(?, ?, ?, ?, ?, ?)";
			PreparedStatement statBlock = conn.prepareStatement(sqlBlock, Statement.RETURN_GENERATED_KEYS);
			statBlock.setInt(1, projectKey);
			statBlock.setInt(2, block.getType());
			statBlock.setInt(3, block.getDelayTime());
			statBlock.setInt(4, block.getBlockNumber());
			statBlock.setInt(5, block.getStatus());
			if (block.getCoorDate() != null) {
				statBlock.setTimestamp(6, new Timestamp(block.getCoorDate().getTime()));
			} else {
				statBlock.setNull(6, Types.TIMESTAMP);
			}
			statBlock.executeUpdate();
			ResultSet rsb = statBlock.getGeneratedKeys();
			block.isNew = false;
			if (rsb.next()) {
				block.setBlockID(rsb.getInt(1));
				return rsb.getInt(1);
			}
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		}
		return -1;
	}

	protected int insertCoordinator(int blockKey, Coordinator coordinator, Connection conn){
        try{
            PreparedStatement pst = conn.prepareStatement("insert into COORDINATORS(BLOCKID, COORDTYPE, COORDINATOR, COORDNUMBER, ISCURRENT, COMMENT, DECISION, COORDATE, " +
                    "DECISIONDATE) values (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            pst.setInt(1, blockKey);
            pst.setInt(2, coordinator.getCoordType());
            pst.setString(3, coordinator.getUser());
            pst.setInt(4, coordinator.num);
            pst.setInt(5, coordinator.isCurrent());
            pst.setString(6, coordinator.getComment());
            pst.setInt(7, coordinator.decision);
            pst.setString(8, coordinator.getCoorDateAsDbFormat());
            pst.setString(9, coordinator.getDecisionDateAsDbFormat());
            int res = pst.executeUpdate();
            pst.close();
            return res;
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(db.getDbID(), e);
            return -1;
        }
	}


	@Override
	public StringBuffer getProjectsByCondition(IQueryFormula condition, Set<String> complexUserID, String absoluteUserID, QueryFieldRule[] fields, int offset, int pageSize, Set<DocID> toExpandResponses) throws DocumentException {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			
			String sql = condition.getSQL(complexUserID) + " LIMIT " + pageSize + " OFFSET " + offset;
			ResultSet rs = s.executeQuery(sql);

			while (rs.next()) {
				int docID = rs.getInt("DOCID");
				int docType = rs.getInt("DOCTYPE");
				int attach = rs.getInt("HAS_ATTACHMENT");
                String ddbid = rs.getString("DDBID");
				String form = rs.getString("FORM");
				StringBuffer value = new StringBuffer(1000);
				
				for (QueryFieldRule field : fields) {
					String tmpValue = "";
					if (rs.getObject(field.value) instanceof Timestamp) {
						tmpValue = Database.sqlDateTimeFormat.format(rs.getTimestamp(field.value));
					} else {
						tmpValue = XMLUtil.getAsTagValue(rs.getString(field.value));
					}
					value.append("<" + field.name + ">" + tmpValue
							+ "</" + field.name + ">");
				}


				if (this.db.hasResponse(conn, docID, docType, complexUserID, null)) {
					value.append("<hasresponse>true</hasresponse>");
				}
			
			     Employer emp =  db.getStructure().getAppUser(absoluteUserID);
				xmlContent.append("<entry isread=\"" + usersActivity.isRead(conn, docID, docType, absoluteUserID) + "\" hasattach=\"" + Integer.toString(attach) + "\" doctype=\"" + DOCTYPE_PROJECT
						+ "\"  " + "docid=\"" + docID + "\" id=\"" + ddbid + "\" "
						+ "favourites=\"" + db.isFavourites(conn, docID, docType, emp) + "\" topicid=\"" + rs.getInt("TOPICID") + "\" "
						+ "url=\"Provider?type=edit&amp;element=project&amp;id=" + form						
						+ "&amp;key=" + docID + "&amp;docid=" + rs.getInt("DDBID") + "\">" + getViewContent(rs) + value);
				
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
	
	public void setTopic(int topicID, int projectID) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String sql = "update projects set topicid = ? where docid = ?";
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setInt(1, topicID);
			pst.setInt(2, projectID);
			pst.executeQuery();
			pst.close();
			conn.commit();
		} catch(SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
	}
	
	public void resetTopic(int projectID) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String sql = "update projects set topicid = null where docid = ?";
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setInt(1, projectID);
			pst.executeQuery();
			pst.close();
			conn.commit();
		} catch(SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
	}
	
	@Override
	public int recalculate() {	
			Connection conn = dbPool.getConnection();
			try {				
				Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);	
				conn.setAutoCommit(false);	
				String update = "update projects set dbd = DATEDIFF(DAY, now(), ctrldate)";			
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
	public  int recalculate(int docID) {
		Connection conn = dbPool.getConnection();
		try {				
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);	
			conn.setAutoCommit(false);	
			String update = "update projects set dbd = DATEDIFF(DAY, now(), ctrldate) where docid ="+docID;
			String select = "select  dbd from projects where docid ="+docID;
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

    @Override
    public StringBuffer getStatisticsByAllObjects() {
        return null;
    }

    @Override
    public StringBuffer getStatisticsByObject(int objectID) {
        return null;
    }

    @Override
    public StringBuffer getStatisticByContragent(int objectID) {
        return null;
    }

    @Override
    public StringBuffer getStatisticByContragentByProject(int contragentID, int projectID) {
        return null;
    }

}
