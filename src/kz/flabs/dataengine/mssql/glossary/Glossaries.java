package kz.flabs.dataengine.mssql.glossary;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.*;
import kz.flabs.dataengine.h2.Database;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.QueryExceptionType;
import kz.flabs.runtimeobj.document.*;
import kz.flabs.runtimeobj.document.glossary.Glossary;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.users.User;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.TagPublicationFormatType;
import kz.pchelka.env.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Set;


public class Glossaries extends kz.flabs.dataengine.h2.glossary.Glossaries implements IGlossaries, Const {	

	public Glossaries(AppEnv env) {	
		super(env);
	}
	
	
	@Override
	public IGlossariesTuner getGlossariesTuner() {
		return new GlossariesTuner(db);
	}

    @Override
    public ArrayList<BaseDocument> getAllGlossaryDocuments(int offset, int pageSize, String[] fields, boolean useCache) {
        Connection conn = dbPool.getConnection();
        ArrayList<BaseDocument> documents = new ArrayList<BaseDocument>();
        try {
            conn.setAutoCommit(false);
            String sql;
            if (offset == -1 && --pageSize == 0) {
                sql = "select distinct GLOSSARY.DOCID from GLOSSARY";
            } else {
                sql = " DECLARE @intStartRow int;  " +
                        " DECLARE @intEndRow int;  " +
                        " DECLARE @intPage int = " + offset + "; " +
                        " DECLARE @intPageSize int = " + pageSize + "; " +
                        " SET @intStartRow = (@intPage - 1) * @intPageSize + 1; " +
                        " SET @intEndRow = @intPage * @intPageSize; " +
                        " WITH blogs AS " +
                        " (SELECT *,  ROW_NUMBER() OVER(ORDER BY VIEWDATE ASC) as intRow,  " +
                        " COUNT(VIEWDATE) OVER() AS intTotalHits  " +
                        " FROM  GLOSSARY)  " +
                        " SELECT * FROM blogs " +
                        " WHERE intRow BETWEEN @intStartRow AND @intEndRow";
            }
            PreparedStatement psd = conn.prepareStatement(sql);
            ResultSet rs = psd.executeQuery();

            while (rs.next()) {
                BaseDocument doc = new Glossary(env, new User(Const.sysUser));
                doc.docType = rs.getInt("DOCTYPE");
                doc.setLastUpdate(rs.getTimestamp("LASTUPDATE"));
                doc.setAuthor(rs.getString("AUTHOR"));
                doc.setRegDate(rs.getTimestamp("REGDATE"));
                doc.setDocID(rs.getInt("DOCID"));
                doc.setDdbID(rs.getString("DDBID"));
                doc.setViewText(rs.getString("VIEWTEXT").replace("''", "'"));
                doc.parentDocID = rs.getInt("PARENTDOCID");
                doc.parentDocType = rs.getInt("PARENTDOCTYPE");
                doc.setForm(rs.getString("FORM"));
                doc.setDefaultRuleID(rs.getString("DEFAULTRULEID"));
                doc.isValid = true;
                for (String field : fields) {
                    String fieldSQL = "select * from CUSTOM_FIELDS_GLOSSARY where CUSTOM_FIELDS_GLOSSARY.DOCID = "
                            + doc.getDocID()
                            + " and CUSTOM_FIELDS_GLOSSARY.NAME = '"
                            + field
                            + "'";
                    Statement sFields = conn.createStatement();
                    ResultSet rsFields = sFields.executeQuery(fieldSQL);
                    if (rsFields.next()) {
                        doc.addField(field, rsFields.getString("VALUE"), rsFields.getInt("TYPE"));
                    }
                    rsFields.close();
                    sFields.close();
                }
                documents.add(doc);
            }
            rs.close();
            psd.close();
            conn.commit();
        } catch (SQLException e) {
            DatabaseUtil.debugErrorPrint(e);
        } finally {
            dbPool.returnConnection(conn);
        }
        return documents;
    }

	@Override
	public StringBuffer getGlossaryByCondition(IQueryFormula condition, int offset, int pageSize, String fieldsCond, Set<String> toExpand, TagPublicationFormatType publishAs) throws DocumentException, QueryException {
		StringBuffer xmlContent = new StringBuffer(10000);		
		Connection conn = dbPool.getConnection();		
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			//String sql = condition.getSQL(new HashSet<String>(Arrays.asList(Const.sysGroup))) + " LIMIT " + pageSize + " OFFSET " + offset;
			String sql = condition.getSQL(Const.sysGroupAsSet, pageSize, offset);
			ResultSet rs = s.executeQuery(sql);
			if (condition.isGroupBy()){
				SourceSupplier ss = new SourceSupplier(env);
				while (rs.next()) {	
					String categoryID = rs.getString(1);
					if (categoryID != null){
						int groupCount = rs.getInt(2);
						String categoryVal[] = {categoryID};
						String viewText = ss.publishAs(publishAs, categoryVal).get(0)[0]; 					


						xmlContent.append("<entry  doctype=\"" + CATEGORY + "\" count=\"" + groupCount + "\" " +
								" categoryid=\"" + categoryID + "\" " +
								" docid=\"" + categoryID + "\" " +
								XMLUtil.getAsAttribute("viewtext", viewText) +
								"url=\"Provider?type=view&amp;id=" + condition.getQueryID() + "&amp;command=expand`" + categoryID + "\" >" +
								"<viewtext>" + XMLUtil.getAsTagValue(viewText) + "</viewtext>");

						if (toExpand != null && toExpand.size() > 0) {
							for (String category : toExpand) {
								if (categoryID.equalsIgnoreCase(category)) {
									StringBuffer categoryValue = getOneCategory(condition.getGroupCondition(category), fieldsCond);
									xmlContent.append("<responses>" + categoryValue + "</responses>");
								}
							}
						}
					}else{
						xmlContent.append("<entry  doctype=\"" + DOCTYPE_UNKNOWN + "\" count=\"0\" " +
								" categoryid=\"null\"" + XMLUtil.getAsAttribute("viewtext", "category is null") + "><viewtext>category is null</viewtext>");
					}
					xmlContent.append("</entry>");
				}

			}else{
				while (rs.next()) {					
					int docID = rs.getInt("DOCID");				
					String form = rs.getString("FORM");
					StringBuffer value = new StringBuffer(1000);
					if (!fieldsCond.equals("")){
						Statement sFields = conn.createStatement();
						String addSQL = "select * from CUSTOM_FIELDS_GLOSSARY where CUSTOM_FIELDS_GLOSSARY.DOCID = " + docID + fieldsCond;
						ResultSet rsFields = sFields.executeQuery(addSQL);
						while (rsFields.next()) {
							String name = rsFields.getString("NAME");
							switch (rsFields.getInt("TYPE")) {
							case TEXT:
								String valueAsText = rsFields.getString("VALUE");
								value.append("<" + name + ">" + XMLUtil.getAsTagValue(valueAsText) + "</" + name + ">");
								break;
							case NUMBERS:
								int valueAsNumber = rsFields.getInt("VALUEASNUMBER");
								value.append("<" + name + ">" + valueAsNumber + "</" + name	+ ">");
								break;
							case DATETIMES:
								Date valueAsDate = rsFields.getDate("VALUEASDATE");
								value.append("<" + name + ">" + Database.dateTimeFormat.format(valueAsDate) + "</"	+ name + ">");
							}
						}
						sFields.close();	
					}

					xmlContent.append("<entry  doctype=\"" + DOCTYPE_GLOSSARY + "\" docid=\"" + docID + "\" " + 
							"url=\"Provider?type=edit&amp;element=glossary&amp;id=" + form + "&amp;key="	+ docID + "\">" + getViewContent(rs) + value);
					xmlContent.append("</entry>");
				}
			}
			s.close();
			rs.close();
			conn.commit();		
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
			throw new QueryException(QueryExceptionType.RUNTIME_ERROR);
		} finally {		
			dbPool.returnConnection(conn);
		}		
		return xmlContent;
	}

	@Override
	public StringBuffer getGlossaryByCondition(IQueryFormula condition, int offset, int pageSize, String fieldsCond, Set<String> toExpand, Set<DocID> toExpandResp, TagPublicationFormatType publishAs) throws DocumentException, QueryException {
		StringBuffer xmlContent = new StringBuffer(10000);		
		Connection conn = dbPool.getConnection();		
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			//String sql = condition.getSQL(new HashSet<String>(Arrays.asList(Const.sysGroup))) + " LIMIT " + pageSize + " OFFSET " + offset;
			String sql = condition.getSQL(Const.sysGroupAsSet, pageSize, offset);
			ResultSet rs = s.executeQuery(sql);
			if (condition.isGroupBy()){
				SourceSupplier ss = new SourceSupplier(env);
				while (rs.next()) {	
					String categoryID = rs.getString(1);
					if (categoryID != null){
						int groupCount = rs.getInt(2);
						String categoryVal[] = {categoryID};
						String viewText = ss.publishAs(publishAs, categoryVal).get(0)[0]; 					


						xmlContent.append("<entry  doctype=\"" + CATEGORY + "\" count=\"" + groupCount + "\" " +
								" categoryid=\"" + categoryID + "\" " +
								" docid=\"" + categoryID + "\" " +
								XMLUtil.getAsAttribute("viewtext", viewText) +
								"url=\"Provider?type=view&amp;id=" + condition.getQueryID() + "&amp;command=expand`" + categoryID + "\" >" +
								"<viewtext>" + XMLUtil.getAsTagValue(viewText) + "</viewtext>");

						if (toExpand != null && toExpand.size() > 0) {
							for (String category : toExpand) {
								if (categoryID.equalsIgnoreCase(category)) {
									StringBuffer categoryValue = getOneCategory(condition.getGroupCondition(category), fieldsCond);
									xmlContent.append("<responses>" + categoryValue + "</responses>");
								}
							}
						}
					}else{
						xmlContent.append("<entry  doctype=\"" + DOCTYPE_UNKNOWN + "\" count=\"0\" " +
								" categoryid=\"null\"" + XMLUtil.getAsAttribute("viewtext", "category is null") + "><viewtext>category is null</viewtext>");
					}
					xmlContent.append("</entry>");
				}

			}else{
				while (rs.next()) {					
					xmlContent.append(getGlossaryEntry(conn, Const.sysGroupAsSet, Const.sysUser, rs, fieldsCond, toExpandResp));		
				}
			}
			s.close();
			rs.close();
			conn.commit();		
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
			throw new QueryException(QueryExceptionType.RUNTIME_ERROR);
		} finally {		
			dbPool.returnConnection(conn);
		}		
		return xmlContent;
	}	
	
	@Override
	public ArrayList<Glossary> getGlossaryByCondition(IQueryFormula condition, int offset, int pageSize) {
		ArrayList<Glossary> documents = new ArrayList<Glossary>();
		String sql = "";

		if(pageSize>0){
			//sql = condition.getSQL(new HashSet<String>(Arrays.asList(Const.sysGroup))) + " LIMIT " + pageSize + " OFFSET " + offset;
			sql = condition.getSQL(Const.sysGroupAsSet, pageSize, offset);
		}else{
			//sql = condition.getSQL(new HashSet<String>(Arrays.asList(Const.sysGroup)));
			sql = condition.getSQL();
		}

		Connection conn = dbPool.getConnection();
		try {		
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);			
			ResultSet rs = s.executeQuery(sql);
			Glossary doc = null;
			while (rs.next()) {
				doc = new Glossary(env, new User(Const.sysUser));
				doc.setDocID(rs.getInt("DOCID"));
				doc.setLastUpdate(rs.getTimestamp("LASTUPDATE"));
				doc.docType = rs.getInt("DOCTYPE");
				doc.form = rs.getString("FORM");
				doc.setAuthor(rs.getString("AUTHOR"));
				doc.setRegDate(rs.getDate("REGDATE"));
				doc.docType = rs.getInt("DOCTYPE");
				doc.setDdbID(rs.getString("DDBID"));
				doc.setViewText(rs.getString("VIEWTEXT"));
				doc.parentDocID = rs.getInt("PARENTDOCID");
				doc.parentDocType = rs.getInt("PARENTDOCTYPE");
				doc.setDefaultRuleID(rs.getString("DEFAULTRULEID"));
				Statement sFields = conn.createStatement();
				ResultSet rsFields = sFields.executeQuery("select * from CUSTOM_FIELDS_GLOSSARY where CUSTOM_FIELDS_GLOSSARY.DOCID = "
						+ doc.getDocID());
				while (rsFields.next()) {
					String fieldName = rsFields.getString("NAME");
					switch (rsFields.getInt("TYPE")) {
					case TEXT:
						doc.addStringField(fieldName,
								rsFields.getString("VALUE"));
						break;
					case NUMBERS:
						doc.addNumberField(fieldName,
								rsFields.getBigDecimal("VALUEASNUMBER"));
						break;
					case DATETIMES:
						doc.addDateField(fieldName,
								rsFields.getDate("VALUEASDATE"));
						break;
					case GLOSSARY:				
						ArrayList<Integer> glosdata = new ArrayList<Integer>();
						glosdata.add(rsFields.getInt("VALUEASGLOSSARY"));
						doc.addGlossaryField(fieldName, glosdata);					
						break;						
					}
				}
				doc.isValid = true;
				documents.add(doc);
				rsFields.close();
				sFields.close();
			}
			s.close();
			rs.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(e, sql);
		} finally {		
			dbPool.returnConnection(conn);
		}
		return documents;
	}
	
	@Override
	public int updateGlossaryDocument(Glossary doc) throws DocumentException {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);

			PreparedStatement pst;
			String sql = null;
			Date viewDate = doc.getViewDate();
            String viewTextList = "";
            for (int i = 1; i <= DatabaseConst.VIEWTEXT_COUNT; i++) {
                viewTextList += "VIEWTEXT" + i + " =  '" + doc.getViewTextList().get(i-1).replaceAll("'", "''") + "',";
            }
            if (viewTextList.endsWith(",")) {
                viewTextList = viewTextList.substring(0, viewTextList.length()-1);
            }
			sql = "update GLOSSARY set LASTUPDATE = '"
					+ Database.sqlDateTimeFormat.format(doc.getLastUpdate()) + "', VIEWTEXT='"
					+ doc.getViewText().replace("'", "''").trim()  
					+ "', DDBID = '" + doc.getDdbID()
					+ "', DEFAULTRULEID = '" + doc.getDefaultRuleID() + "'"
					+ "," + viewTextList
					+ ", VIEWNUMBER = " + doc.getViewNumber()
					+ ", VIEWDATE = " + (viewDate != null ? "'" + Database.sqlDateTimeFormat.format(viewDate) + "'" : "null")
					+"  where DOCID = " + doc.getDocID();
			pst = conn.prepareStatement(sql);
			pst.executeUpdate();
			for (Field field : doc.fields()) {
				switch (field.getTypeAsDatabaseType()) {
				case Const.TEXT:
									
					sql = "MERGE CUSTOM_FIELDS_GLOSSARY AS TARGET " +
					" USING (SELECT " + doc.getDocID() + " AS DOCID, '" + field.name + "' AS NAME) AS SOURCE " +
					" ON TARGET.DOCID = SOURCE.DOCID AND TARGET.NAME = SOURCE.NAME " +
					" WHEN MATCHED THEN " +
					" UPDATE SET TARGET.VALUE='" + field.valueAsText.replace("'", "''").trim() + 
					"', " + "TARGET.TYPE=" + field.getTypeAsDatabaseType() +
					" WHEN NOT MATCHED THEN " +
					" INSERT (DOCID, NAME, VALUE, TYPE) " +
					" VALUES (" + doc.getDocID() + ", '" + field.name + "', '" +  field.valueAsText.replace("'", "''").trim() + "', " + field.getTypeAsDatabaseType() + ");";
										
					/*sql = "update CUSTOM_FIELDS_GLOSSARY set VALUE='"
							+ field.valueAsText.replace("'", "''").trim() + "', " + "TYPE=" + field.getTypeAsDatabaseType()
							+ " where DOCID=" + doc.getDocID() + " and NAME='"
							+ field.name + "'";*/
					pst = conn.prepareStatement(sql);
					pst.executeUpdate();
					break;
				case Const.NUMBERS:
					
					sql = "MERGE CUSTOM_FIELDS_GLOSSARY AS TARGET " +
							" USING (SELECT " + doc.getDocID() + " AS DOCID, '" + field.name + "' AS NAME) AS SOURCE " +
							" ON TARGET.DOCID = SOURCE.DOCID AND TARGET.NAME = SOURCE.NAME " +
							" WHEN MATCHED THEN " +
							" UPDATE SET TARGET.VALUEASNUMBER=" + field.valueAsNumber + 
							", " + "TARGET.TYPE=" + field.getTypeAsDatabaseType() +
							" WHEN NOT MATCHED THEN " +
							" INSERT (DOCID, NAME, VALUEASNUMBER, TYPE) " +
							" VALUES (" + doc.getDocID() + ", '" + field.name + "', " +  field.valueAsNumber + ", " + field.getTypeAsDatabaseType() + ");";
								
					/*sql = "update CUSTOM_FIELDS_GLOSSARY set VALUEASNUMBER="
							+ field.valueAsNumber + ", " + "TYPE=" + field.getTypeAsDatabaseType()
							+ " where DOCID=" + doc.getDocID() + " and NAME='"
							+ field.name + "'";*/
					pst = conn.prepareStatement(sql);
					pst.executeUpdate();
					break;
				case Const.DATETIMES:	
					
					sql = "MERGE CUSTOM_FIELDS_GLOSSARY AS TARGET " +
							" USING (SELECT " + doc.getDocID() + " AS DOCID, '" + field.name + "' AS NAME) AS SOURCE " +
							" ON TARGET.DOCID = SOURCE.DOCID AND TARGET.NAME = SOURCE.NAME " +
							" WHEN MATCHED THEN " +
							" UPDATE SET TARGET.VALUEASDATE='" + field.valueAsDate + 
							"', " + "TARGET.TYPE=" + field.getTypeAsDatabaseType() +
							" WHEN NOT MATCHED THEN " +
							" INSERT (DOCID, NAME, VALUEASDATE, TYPE) " +
							" VALUES (" + doc.getDocID() + ", '" + field.name + "', '" +  Database.sqlDateTimeFormat.format(field.valueAsDate) + "', " + field.getTypeAsDatabaseType() + ");";
							
					/*sql = "update CUSTOM_FIELDS_GLOSSARY set VALUEASDATE='"
							+ Database.sqlDateTimeFormat.format(field.valueAsDate) + "', "
							+ "TYPE=" + field.getTypeAsDatabaseType() + " where DOCID="
							+ doc.getDocID() + " and NAME='" + field.name + "'";*/
					pst = conn.prepareStatement(sql);
					pst.executeUpdate();
					break;
				case TEXTLIST:
					PreparedStatement ps = conn.prepareStatement("DELETE FROM CUSTOM_FIELDS_GLOSSARY"
							+ " WHERE DOCID="
							+ doc.getDocID()
							+ " and NAME='"
							+ field.name
							+ "'");
					ps.executeUpdate();
					ps = conn.prepareStatement("insert into CUSTOM_FIELDS_GLOSSARY(DOCID, NAME, VALUE, TYPE)"
							+ " values ("
							+ doc.getDocID()
							+ ", '"
							+ field.name
							+ "', ?,"
							+ field.getTypeAsDatabaseType() + ")");
					for (String value: field.valuesAsStringList) {
						ps.setString(1, value);
						ps.executeUpdate();							
					}
					break;
				case Const.GLOSSARY:
					pst = conn.prepareStatement("DELETE FROM CUSTOM_FIELDS_GLOSSARY WHERE DOCID = "	+ doc.getDocID() + " and NAME = '" + field.name + "'");
					pst.executeUpdate();
					pst = conn.prepareStatement("insert into CUSTOM_FIELDS_GLOSSARY(DOCID, NAME, VALUEASGLOSSARY, TYPE)"
							+ " values ("
							+ doc.getDocID()
							+ ", '"
							+ field.name
							+ "', ?,"
							+ field.getTypeAsDatabaseType() + ")");
					for (Integer value: field.valuesAsGlossaryData) {
						pst.setInt(1, value);
						try {
							pst.executeUpdate();
						} catch (SQLException se) {
							DatabaseUtil.debugErrorPrint(se);
							return -1;
						}
					}
					pst.close();
					break;
				}
			}			
			sql = "DELETE FROM GLOSSARY_TREE_PATH " +
					" WHERE DESCENDANT IN (SELECT DESCENDANT FROM GLOSSARY_TREE_PATH WHERE ANCESTOR = " + doc.getDocID() + ") " +
					" AND ANCESTOR IN (SELECT ANCESTOR FROM GLOSSARY_TREE_PATH WHERE DESCENDANT = " + doc.getDocID() + " AND ANCESTOR != DESCENDANT)";
			pst = conn.prepareStatement(sql);
			pst.executeUpdate();
			sql = "INSERT INTO GLOSSARY_TREE_PATH (ANCESTOR, DESCENDANT, LENGTH) " +
					" SELECT supertree.ANCESTOR, subtree.descendant, supertree.LENGTH + subtree.LENGTH + 1 as length " +
					" FROM GLOSSARY_TREE_PATH as supertree " +
					" CROSS JOIN GLOSSARY_TREE_PATH as subtree " +
					" WHERE supertree.descendant = " + doc.parentDocID +
					" AND subtree.ancestor = " + doc.getDocID();
			pst = conn.prepareStatement(sql);
			pst.executeUpdate();
            updateBlobTables(conn, doc, baseTable);
			conn.commit();
			pst.close();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
			return -1;		
		} catch (IOException e) {
            DatabaseUtil.debugErrorPrint(e);
            return -1;
        } finally {
			dbPool.returnConnection(conn);
		}
		return doc.getDocID(); 
	}

    public void updateBlobTables(Connection conn, Glossary doc, String tableSuffix) throws SQLException, IOException {
        Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        ResultSet blobs = s.executeQuery("SELECT ID, DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE " +
                "FROM CUSTOM_BLOBS_" + tableSuffix  +
                " WHERE DOCID = " + doc.getDocID());
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
        for (Map.Entry<String, BlobField> blob: doc.blobFieldsMap.entrySet()) {
            PreparedStatement ps = conn.prepareStatement("SELECT ID, DOCID, NAME, ORIGINALNAME, CHECKSUM, VALUE " +
                    "FROM CUSTOM_BLOBS_" + tableSuffix  +
                    " WHERE DOCID = ? AND NAME = ? AND CHECKSUM = ?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
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
                    FileInputStream is = new FileInputStream(file);
                    blobs.updateBinaryStream("VALUE", is, (int)file.length());
                    blobs.insertRow();
                    is.close();
                }
                Environment.fileToDelete.add(bfile.path);
            }
        }
    }
}
