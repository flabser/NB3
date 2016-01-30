package kz.flabs.dataengine.postgresql.structure;

import kz.flabs.dataengine.*;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.BlobField;
import kz.flabs.runtimeobj.document.BlobFile;
import kz.flabs.util.Util;
import kz.pchelka.env.Environment;
import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.*;


public class Structure extends kz.flabs.dataengine.h2.structure.Structure implements IStructure, Const{	

	public Structure(IDatabase db, IDBConnectionPool structDbPool) {	
		super(db, structDbPool);
	}

    //TODO table name
    public void recoverRelations(Connection conn, BaseDocument doc, int key) {
        try {
            String sql;
            PreparedStatement pst;
            String tableName;
            switch (doc.docType) {
                case DOCTYPE_EMPLOYER:
                    tableName = "EMPLOYERS";
                    break;
                case DOCTYPE_DEPARTMENT:
                    tableName = "DEPARTMENTS";
                    break;
                default:
                    tableName = "MAINDOCS";
                    break;
            }

            ArrayList<String> ids = new ArrayList<>();
            for (BlobField field : doc.blobFieldsMap.values()) {
                for (BlobFile f : field.getFiles()) {
                    if (f.id == null) {
                        pst = conn.prepareStatement("INSERT INTO CUSTOM_BLOBS_" + tableName + " (DOCID, NAME, ORIGINALNAME, CHECKSUM, COMMENT, VALUE_OID, REGDATE)VALUES(?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
                        String hash = Util.getHexHash(f.path);
                        pst.setInt(1, key);
                        pst.setString(2, "rtfcontent");
                        pst.setString(3, FilenameUtils.getName(f.originalName));
                        pst.setString(4, hash);
                        pst.setString(5, "");

                        LargeObjectManager lobj = ((org.postgresql.PGConnection) ((DelegatingConnection) conn).getInnermostDelegate()).getLargeObjectAPI();
                        long oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
                        LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);
                        File file = new File(f.path);
                        FileInputStream fin = new FileInputStream(file);
                        byte buf[] = new byte[1048576];
                        int s, tl = 0;
                        while ((s = fin.read(buf, 0, 1048576)) > 0) {
                            obj.write(buf, 0, s);
                            tl += s;
                        }
                        obj.close();
                        fin.close();
                        pst.setLong(6, oid);
                        pst.setTimestamp(7, new Timestamp(new java.util.Date().getTime()));
                        pst.executeUpdate();
                        conn.commit();
                        int att_id = 0;
                        Environment.fileToDelete.add(f.path);
                        ResultSet rs = pst.getGeneratedKeys();
                        while (rs.next()) {
                            ids.add(String.valueOf(rs.getInt(1)));
                        }
                        pst.close();
                    } else {
                        ids.add(f.id);
                        sql = "UPDATE CUSTOM_BLOBS_" + tableName + " SET DOCID = ?, COMMENT = ? WHERE id = ?";
                        pst = conn.prepareStatement(sql);
                        pst.setInt(1, key);
                        pst.setString(2, f.getComment());
                        pst.setInt(3, Integer.valueOf(f.id));
                        pst.executeUpdate();
                    }
                }
            }
            conn.commit();
            sql = "UPDATE CUSTOM_BLOBS_" + tableName + " SET DOCID = ? WHERE docid = ?" + (ids.size() > 0 ? " and id not in (" + StringUtils.join(ids, ",") + ") " : "");
            pst = conn.prepareStatement(sql);
            pst.setInt(1, 0);
            pst.setInt(2, key);
            pst.executeUpdate();
            conn.commit();
            pst.close();
        } catch (Exception e) {
            DatabaseUtil.errorPrint(this.db.getDbID(), e);
            try {
                conn.rollback();
            } catch (SQLException e1) {
                DatabaseUtil.errorPrint(this.db.getDbID(), e);
            }
        }
    }

	public StringBuffer getEmployersByFrequencyExecution() {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			String sql = "select e.userid, e.viewtext, " + DatabaseUtil.getViewTextList("e") + ", e.viewnumber, e.viewdate, e.post, e.empid, e.fullname, count(cf.value) as numEntries " +
					" from employers as e " +
					" left join custom_fields as cf on cf.name = 'executer' and cf.value = e.userid " +
					" group by e.userid, e.viewtext, " + DatabaseUtil.getViewTextList("e") + ", e.viewnumber, e.viewdate, e.post, e.empid, e.fullname " +
					" order by count(cf.value) desc";
			ResultSet rs = s.executeQuery(sql);		
			while (rs.next()) {
				xmlContent.append(getEmployerEntry(rs));		
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return xmlContent;
	}

}

