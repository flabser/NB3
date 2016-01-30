package kz.flabs.dataengine.mssql;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.*;
import kz.flabs.dataengine.h2.Database;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.ExceptionType;
import kz.flabs.runtimeobj.document.BlobField;
import kz.flabs.runtimeobj.document.BlobFile;
import kz.flabs.runtimeobj.document.Execution;
import kz.flabs.users.User;
import kz.pchelka.env.Environment;

import java.io.*;
import java.sql.*;
import java.util.Map.Entry;


public class ExecutionsOnDatabase extends kz.flabs.dataengine.h2.ExecutionsOnDatabase implements IExecutions, Const {

	public ExecutionsOnDatabase(IDatabase db) {
		super(db);
	}

	@Override
	public int insertExecution(Execution doc, User user) throws DocumentException {
		Connection conn = dbPool.getConnection();
		int id = doc.getDocID();
		int key = 0;
		try {
			conn.setAutoCommit(false);

			String fieldsAsText = "AUTHOR, REGDATE, EXECUTOR, REPORT, FINISHDATE, PARENTDOCID, DDBID, VIEWTEXT,"
					+ " DOCTYPE, PARENTDOCTYPE, NOMENTYPE, LASTUPDATE, FORM, DEFAULTRULEID, HAS_ATTACHMENT";
			String valuesAsText = "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '"
					+ Database.sqlDateTimeFormat.format(doc.getLastUpdate())
					+ "', '"
					+ doc.form + "', " + "?, ?";

			if (id != 0 && doc.hasField("recID")){
				fieldsAsText = "DOCID, " + fieldsAsText;
				valuesAsText = id + ", " + valuesAsText;
			}

			String sql = "insert into EXECUTIONS(" + fieldsAsText + ")" + "values(" + valuesAsText + ")";

			PreparedStatement pst = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			pst.setString(1, doc.getAuthorID());
			pst.setTimestamp(2, new java.sql.Timestamp(doc.getRegDate().getTime()));
			pst.setString(3, doc.executor);
			pst.setString(4, doc.report);
			pst.setTimestamp(5, new java.sql.Timestamp(doc.getFinishDate().getTime()));			
			pst.setInt(6, doc.parentDocID);
			pst.setString(7, doc.getDdbID());
			pst.setString(8, doc.getViewText());
			pst.setInt(9, Const.DOCTYPE_EXECUTION);
			pst.setInt(10, doc.parentDocType);
			pst.setInt(11, doc.getNomenType());
			pst.setString(12, doc.getDefaultRuleID());
			pst.setInt(13, doc.blobFieldsMap.size());
			pst.executeUpdate();
			ResultSet rs = pst.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);
			}
			/* BLOBS */
			if (id != 0 && !doc.hasField("recID")) {
				PreparedStatement s0 = conn.prepareStatement("SELECT * FROM CUSTOM_BLOBS_EXECUTIONS WHERE DOCID = " + id);
				ResultSet rs0 = s0.executeQuery();
				while (rs0.next()) {
					PreparedStatement s1 = conn.prepareStatement("INSERT INTO CUSTOM_BLOBS_EXECUTIONS (DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE)values(?, ?, ?, ?, ?)");
					s1.setInt(1, key);
					s1.setString(2, rs0.getString("NAME"));
					s1.setString(3, rs0.getString("ORIGINALNAME"));
					s1.setString(4, rs0.getString("CHECKSUM"));
					s1.setBinaryStream(5, rs0.getBinaryStream("VALUE"));
					s1.executeUpdate();
					s1.close();
				}
				rs0.close();
				s0.close();
			} else {
				for (Entry<String, BlobField> blob : doc.blobFieldsMap.entrySet()) {
					PreparedStatement ps = conn.prepareStatement("INSERT INTO CUSTOM_BLOBS_EXECUTIONS (DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE)values(?, ?, ?, ?, ?)");
					BlobField bf = blob.getValue();
					for (BlobFile bfile: bf.getFiles()) {
						ps.setInt(1, key);
						ps.setString(2, bf.name);
						ps.setString(3, bfile.originalName);
						ps.setString(4, bfile.checkHash);
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
			conn.commit();
			db.insertToAccessTables(conn, "EXECUTIONS", key, doc);
			//CachePool.flush();
			conn.commit();		
			rs.close();
			pst.close();
			if (!doc.hasField("recID")){
				IUsersActivity ua = db.getUserActivity();
				ua.postCompose(doc, user);
				ua.postMarkRead(key, doc.docType, user);
			}
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;
		} catch (FileNotFoundException fe) {
			AppEnv.logger.errorLogEntry(fe);
			return -1;
		} catch (IOException ioe) {
			AppEnv.logger.errorLogEntry(ioe);
			return -1;
		}finally{	
			dbPool.returnConnection(conn);
		}
		return key;
	}

	@Override
	public int updateExecution(Execution doc, User user) throws DocumentAccessException, DocumentException {
		if (doc.hasEditor(user.getAllUserGroups())) {
			Connection conn = dbPool.getConnection();
			try {			
				conn.setAutoCommit(false);
				String sql = "update EXECUTIONS set LASTUPDATE ='"
						+ Database.sqlDateTimeFormat.format(doc.getLastUpdate())
						+ "', AUTHOR = '" + doc.getAuthorID() + "', EXECUTOR = '"
						+ doc.executor + "', REPORT = '" + doc.report + "',"
						+ " NOMENTYPE = " + doc.getNomenType() + ", "  
						+ " PARENTDOCID = " + doc.parentDocID + ", DDBID = '"
						+ doc.getDdbID() + "', PARENTDOCTYPE = "
						+ doc.parentDocType + ", VIEWTEXT='" + doc.getViewText()
						+ "', DEFAULTRULEID = '" + doc.getDefaultRuleID()
						+ "' where DOCID = " + doc.getDocID();
				PreparedStatement pst = conn.prepareStatement(sql);
				pst.executeUpdate();
				pst.close();
				// =========== BLOBS ===========
				Statement s = conn.createStatement();
				ResultSet blobs = s.executeQuery("SELECT ID, DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE " +
						"FROM CUSTOM_BLOBS_EXECUTIONS " +
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
							"FROM CUSTOM_BLOBS_EXECUTIONS " +
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
							File file = new File(bfile.path);
							InputStream is = new FileInputStream(file);
							blobs.updateBinaryStream("VALUE", is, (int)file.length());
							blobs.insertRow();
							is.close();
						}
						Environment.fileToDelete.add(bfile.path);
					}
					ps.close();
				}
				s.close();
				db.updateAccessTables(conn, doc, baseTable);
				conn.commit();
			} catch (SQLException e) {
				DatabaseUtil.errorPrint(db.getDbID(), e);
				return -1;
			} catch (FileNotFoundException fe) {
				AppEnv.logger.errorLogEntry(fe);
				return -1;
			} catch (IOException ioe) {
				AppEnv.logger.errorLogEntry(ioe);
				return -1;
			}finally{		
				dbPool.returnConnection(conn);
			}
			return doc.getDocID();
		} else {
			throw new DocumentAccessException(ExceptionType.DOCUMENT_WRITE_ACCESS_RESTRICTED, user.getUserID());
		}
	}


}
