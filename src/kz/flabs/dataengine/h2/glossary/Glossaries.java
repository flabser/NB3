package kz.flabs.dataengine.h2.glossary;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.*;
import kz.flabs.dataengine.h2.Database;
import kz.flabs.exception.DataConversionException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.QueryExceptionType;
import kz.flabs.runtimeobj.DocumentCollection;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.runtimeobj.document.*;
import kz.flabs.runtimeobj.document.glossary.Glossary;
import kz.flabs.runtimeobj.queries.CachePool;
import kz.flabs.runtimeobj.viewentry.ViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntryCollection;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.users.RunTimeParameters;
import kz.flabs.users.User;
import kz.flabs.util.Util;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.TagPublicationFormatType;
import kz.nextbase.script._ViewEntryCollection;
import kz.pchelka.env.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;


public class Glossaries extends DatabaseCore implements IGlossaries, Const{
	protected static final String glossaryFields = "GLOSSARY.DOCID, AUTHOR, DDBID, PARENTDOCID, PARENTDOCTYPE, REGDATE, DOCTYPE, LASTUPDATE, VIEWTEXT, VIEWICON,  FORM, " + DatabaseUtil.getViewTextList("") + ", VIEWNUMBER, VIEWDATE";

	protected AppEnv env;
	protected IDatabase db;
	protected IDBConnectionPool dbPool;

    public static String baseTable = "GLOSSARY";

	public Glossaries(AppEnv env) {	
		this.env = env;
		this.db = env.getDataBase();
		this.dbPool = db.getConnectionPool();

	}
	
	@Override
	public IGlossariesTuner getGlossariesTuner() {
		return new GlossariesTuner(db);
	}

    public void insertBlobTables(Connection conn, int id, int key, Glossary doc, String tableSuffix) throws SQLException, IOException {
        if (id != 0 && !doc.hasField("recID")) {
            PreparedStatement s0 = conn.prepareStatement("SELECT * FROM CUSTOM_BLOBS_" + tableSuffix + " WHERE DOCID = " + id);
            ResultSet rs0 = s0.executeQuery();
            while (rs0.next()) {
                PreparedStatement s1 = conn.prepareStatement("INSERT INTO CUSTOM_BLOBS_" + tableSuffix + " (DOCID, NAME, ORIGINALNAME, CHECKSUM, COMMENT, VALUE)values(?, ?, ?, ?, ?, ?)");
                s1.setInt(1, key);
                s1.setString(2, rs0.getString("NAME"));
                s1.setString(3, rs0.getString("ORIGINALNAME"));
                s1.setString(4, rs0.getString("CHECKSUM"));
                s1.setString(5, rs0.getString("COMMENT"));
                s1.setBinaryStream(5, rs0.getBinaryStream("VALUE"));
                s1.executeUpdate();
                s1.close();
            }
            rs0.close();
            s0.close();
        } else {
            for (Map.Entry<String, BlobField> blob : doc.blobFieldsMap.entrySet()) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO CUSTOM_BLOBS_" + tableSuffix + " (DOCID, NAME, ORIGINALNAME, CHECKSUM, COMMENT, VALUE)values(?, ?, ?, ?, ?, ?)");
                BlobField bf = blob.getValue();
                for (BlobFile bfile : bf.getFiles()) {
                    ps.setInt(1, key);
                    ps.setString(2, bf.name);
                    ps.setString(3, bfile.originalName);
                    ps.setString(4, bfile.checkHash);
                    ps.setString(5, bfile.comment);
                    if (!bfile.path.equalsIgnoreCase("")) {
                        File file = new File(bfile.path);
                        FileInputStream fin = new FileInputStream(file);
                        ps.setBinaryStream(6, fin, (int) file.length());
                        ps.executeUpdate();
                        fin.close();
                        Environment.fileToDelete.add(bfile.path);
                    } else {
                        ps.setBytes(6, bfile.getContent());
                        ps.executeUpdate();
                    }
                }
                ps.close();
            }
        }
    }

	@Override
	public ArrayList<BaseDocument> getAllGlossaryDocuments(int offset, int pageSize, String[] fields, boolean useCache) {
		Connection conn = dbPool.getConnection();
		ArrayList<BaseDocument> documents = new ArrayList<BaseDocument>();
		try {
			conn.setAutoCommit(false);
			String sql = "select * from GLOSSARY "
					+ " LIMIT " + pageSize + " OFFSET " + offset;
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
	public int getGlossaryCount() {
		Connection conn = dbPool.getConnection();
		int size = 0;
		try {
			conn.setAutoCommit(false);
			String sql = "select count(*) from GLOSSARY";
			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				size = rs.getInt(1);
			}
			rs.close();
			pst.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		}finally{	
			dbPool.returnConnection(conn);
		}
		return size;
	}

	@Override
	public StringBuffer getGlossaryByCondition(IQueryFormula condition, int offset, int pageSize, String fieldsCond, Set<String> toExpand, TagPublicationFormatType publishAs) throws DocumentException, QueryException {
		StringBuffer xmlContent = new StringBuffer(10000);		
		Connection conn = dbPool.getConnection();		
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			//String sql = condition.getSQL(new HashSet<String>(Arrays.asList(Const.sysGroup))) + " LIMIT " + pageSize + " OFFSET " + offset;
			String sql = condition.getSQL() + " LIMIT " + pageSize + " OFFSET " + offset;
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
					String viewText = rs.getString("VIEWTEXT").replace("''", "'");
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

					String viewcontent = getViewContent(rs);
					xmlContent.append("<entry  doctype=\"" + DOCTYPE_GLOSSARY + "\"  " +
							"docid=\"" + docID + "\"" + XMLUtil.getAsAttribute("viewtext", viewText) +
							"url=\"Provider?type=edit&amp;element=glossary&amp;id=" + form + "&amp;key="	+ docID + "\" " +						 						
							"><viewcontent>" + viewcontent + "</viewcontent>" + value);

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
			String sql = condition.getSQL() + " LIMIT " + pageSize + " OFFSET " + offset;
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
	
	public StringBuffer getOneCategory(String addCondition, String fieldsCond) {
		StringBuffer xmlContent = new StringBuffer(10000);		
		Connection conn = dbPool.getConnection();
		try {	
			conn.setAutoCommit(false);
			Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

			String sql = "SELECT DISTINCT " + glossaryFields + " FROM GLOSSARY " +
					addCondition + " ORDER BY GLOSSARY.regdate";				

			ResultSet rs = statement.executeQuery(sql);

			while (rs.next()) {
				xmlContent.append(db.getGlossaries().getGlossaryEntry(conn, Const.sysGroupAsSet, Const.sysUser, rs, fieldsCond, new HashSet<DocID>()));	
			}

			rs.close();
			statement.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} catch (DocumentException e) {
			DatabaseUtil.debugErrorPrint(e);
		}finally{	
			dbPool.returnConnection(conn);
		}
		return xmlContent;
	}
	
	public String getGlossaryEntry(Connection conn, Set<String> complexUserID, String absoluteUserID, ResultSet rs, String fieldsCond, Set<DocID> toExpandResponses) throws SQLException, DocumentException{
		@Deprecated
		String customFieldsValue = "";	
		int docID = rs.getInt("DOCID");
		int docType = rs.getInt("DOCTYPE");

		if (hasResponse(conn, docID, docType, complexUserID, null)) {
			customFieldsValue += "<hasresponse>true</hasresponse>";
		}

		StringBuffer value = new StringBuffer("<entry " +
				"id=\"" + rs.getString("DDBID") + "\"  doctype=\"" + docType + "\"  " +
				"docid=\"" + docID + "\" " + 
				"url=\"Provider?type=edit&amp;element=glossary&amp;id=" + rs.getString("FORM") + "&amp;key="	+ docID + "\" " +						 						
				">" + getShortViewContent(rs) + customFieldsValue);

		if (toExpandResponses != null && toExpandResponses.size() > 0) {
			for (DocID doc : toExpandResponses) {
				if (doc.id == docID && doc.type == DOCTYPE_GLOSSARY) {
					DocumentCollection responses = getGlossaryDescendants(docID, DOCTYPE_GLOSSARY, doc.toExpand,1, complexUserID, absoluteUserID);
					if (responses.count > 0) {
						value.append("<responses>" + responses.xmlContent + "</responses>");
					}
				}
			}
		}
		return value.append("</entry>").toString();
	}

    @Override
    public boolean inUse(Connection conn, int docID, int docType) {
        try {
            conn.setAutoCommit(false);
            Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sql = "select docid from custom_fields where valueasglossary = " + docID  +
                    " union " +
                    " select docid from custom_fields_glossary where valueasglossary = " + docID;
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            DatabaseUtil.errorPrint(this.db.getDbID(), e);
        }
        return false;
    }

	@Override
	public boolean hasResponse(Connection conn, int docID, int docType, Set<String> complexUserID, String absoluteUserID) {
		try {
			conn.setAutoCommit(false);
			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "select descendant from glossary_tree_path where ancestor = " + docID + " and length > 0";
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
	
	@Override
	public ArrayList<Glossary> getGlossaryByCondition(IQueryFormula condition, int offset, int pageSize) {
		ArrayList<Glossary> documents = new ArrayList<Glossary>();
		String sql = "";

		if(pageSize>0){
			//sql = condition.getSQL(new HashSet<String>(Arrays.asList(Const.sysGroup))) + " LIMIT " + pageSize + " OFFSET " + offset;
			sql = condition.getSQL() + " LIMIT " + pageSize + " OFFSET " + offset;
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
				doc.setViewText(rs.getString("VIEWTEXT").replace("''", "'"));
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
								rsFields.getInt("VALUEASNUMBER"));
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
	public int getGlossaryByConditionCount(IQueryFormula condition) {
		Connection conn = dbPool.getConnection();
		int size = 0;
		try {
			conn.setAutoCommit(false);
			String sql;
			if (!condition.isGroupBy()) {
				//sql = condition.getSQLCount(new HashSet<String>(Arrays.asList(Const.sysGroup)));
				sql = condition.getSQLCount();
			} else {
				//sql = condition.getSQLGroupCount(new HashSet<String>(Arrays.asList(Const.sysGroup)));
				sql = condition.getSQLGroupCount();
			}

			if (!sql.equals("")){
				PreparedStatement pst = conn.prepareStatement(sql);
				ResultSet rs = pst.executeQuery();
				while (rs.next()) {
					size += rs.getInt(1);
				}
				rs.close();
				pst.close();
			}
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		}finally{		
			dbPool.returnConnection(conn);
		}
		return size;
	}
	
	@Override
	public Glossary getGlossaryDocumentByID(int docID) {
		Connection conn = dbPool.getConnection();
		Glossary doc = null;
		try {
			conn.setAutoCommit(false);
			String allDoc = "select * from GLOSSARY, CUSTOM_FIELDS_GLOSSARY where GLOSSARY.DOCID = CUSTOM_FIELDS_GLOSSARY.DOCID"
					+ " and GLOSSARY.DOCID=" + docID;
			PreparedStatement pst = conn.prepareStatement(allDoc);
			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				doc = fillGloassary(rs);
			}
			rs.close();
			pst.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return doc;
	}
	
	@Override
	public Glossary getGlossaryDocumentByID(String ddbID) {
		Connection conn = dbPool.getConnection();
		Glossary doc = null;
		try {
			conn.setAutoCommit(false);
			String allDoc = "select * from GLOSSARY, CUSTOM_FIELDS_GLOSSARY where GLOSSARY.DOCID = CUSTOM_FIELDS_GLOSSARY.DOCID"
					+ " and GLOSSARY.DDBID='" + ddbID + "'";
			PreparedStatement pst = conn.prepareStatement(allDoc);
			ResultSet rs = pst.executeQuery();			
			if (rs.next()) {
				doc = fillGloassary(rs);
			}
			rs.close();
			pst.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return doc;
	}

	
	public Glossary getGlossaryDocumentByID(int docID, boolean useCache, Set<String> complexUserID, String absoluteUserID) {
		Connection conn = dbPool.getConnection();
		Glossary doc = null;
		try {
			conn.setAutoCommit(false);
			String allDoc = "select * from GLOSSARY, CUSTOM_FIELDS_GLOSSARY where GLOSSARY.DOCID = CUSTOM_FIELDS_GLOSSARY.DOCID"
					+ " and GLOSSARY.DOCID=" + docID;
			PreparedStatement pst = conn.prepareStatement(allDoc);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				doc = fillGloassary(rs);
				doc.setEditMode(complexUserID);
                db.fillBlobs(conn, doc, "GLOSSARY");
                doc.editMode = EDITMODE_EDIT;
			}
			rs.close();
			pst.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return doc;
	}

	@Override
	public int insertGlossaryDocument(Glossary doc) throws DocumentException {
		Connection conn = dbPool.getConnection();
		int key = 0;
		try {
			conn.setAutoCommit(false);
			Date viewDate = doc.getViewDate();	
			String viewtext1 = "";
			String viewtext2 = "";
			String viewtext3 = "";
            String viewtext4 = "";
            String viewtext5 = "";
            String viewtext6 = "";
            String viewtext7 = "";

			List<String> vt = doc.getViewTextList();
			viewtext1 = vt.get(0).replace("'", "''");
			viewtext2 = vt.get(1).replace("'", "''");
			viewtext3 = vt.get(2).replace("'", "''");
            viewtext4 = vt.get(3).replace("'", "''");
            viewtext5 = vt.get(4).replace("'", "''");
            viewtext6 = vt.get(5).replace("'", "''");
            viewtext7 = vt.get(6).replace("'", "''");

			BigDecimal viewNumber = doc.getViewNumber();
			String insertGlossary = "insert into GLOSSARY(AUTHOR, REGDATE, DOCTYPE, DDBID, VIEWTEXT, LASTUPDATE, PARENTDOCID, " +
					"PARENTDOCTYPE, FORM, VIEWTEXT1, VIEWTEXT2, VIEWTEXT3, VIEWTEXT4, VIEWTEXT5, VIEWTEXT6, VIEWTEXT7, VIEWNUMBER, VIEWDATE) "
					+ "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement pst = conn.prepareStatement(insertGlossary, PreparedStatement.RETURN_GENERATED_KEYS);
			pst.setString(1, doc.getAuthorID());
			pst.setTimestamp(2, new Timestamp(doc.getRegDate().getTime()));
			pst.setInt(3, doc.docType);
			pst.setString(4, "_" + Util.generateRandomAsText(env.appType,15));
			pst.setString(5, doc.getViewText().replace("'", "''").trim());
			pst.setTimestamp(6, new Timestamp(doc.getLastUpdate().getTime()));
			pst.setInt(7, doc.parentDocID);
			pst.setInt(8, doc.parentDocType);
			pst.setString(9, doc.form);
			pst.setString(10, viewtext1);
			pst.setString(11, viewtext2);
			pst.setString(12, viewtext3);
            pst.setString(13, viewtext4);
            pst.setString(14, viewtext5);
            pst.setString(15, viewtext6);
            pst.setString(16, viewtext7);
			if (viewNumber != null) {
				pst.setBigDecimal(17, viewNumber);
			} else {
				pst.setNull(17, Types.INTEGER);
			}	
			if (viewDate != null) {
				pst.setTimestamp(18, new Timestamp(viewDate.getTime()));
			} else {
				pst.setNull(18, Types.TIMESTAMP);
			}
			pst.executeUpdate();
			ResultSet rs = pst.getGeneratedKeys();
			while (rs.next()) {
				key = rs.getInt(1);
			}
			for (Field field : doc.fields()) {			

				switch (field.getTypeAsDatabaseType()) {
				case Const.TEXT:
					pst = conn.prepareStatement("insert into CUSTOM_FIELDS_GLOSSARY(DOCID, NAME, VALUE, TYPE) values (?, ?, ?, ?)");
					pst.setInt(1, key);
					pst.setString(2, field.name);
					pst.setString(3, field.valueAsText.replace("'", "''").replace(';', ',').trim());
					pst.setInt(4, field.getTypeAsDatabaseType());		
					pst.executeUpdate();					
					break;					
				case TEXTLIST:
					pst = conn.prepareStatement("insert into CUSTOM_FIELDS_GLOSSARY(DOCID, NAME, VALUE, TYPE) values (?, ?, ?, ?)");
					pst.setInt(1, key);
					pst.setString(2, field.name);
					for (String value: field.valuesAsStringList) {
						pst.setString(3, value);
						pst.setInt(4, field.getTypeAsDatabaseType());		
						try {
							pst.executeUpdate();							
						} catch (SQLException se) {
							DatabaseUtil.debugErrorPrint(se);
							return -1;
						}
					}
					break;
				case Const.NUMBERS:				
					pst = conn.prepareStatement("insert into CUSTOM_FIELDS_GLOSSARY(DOCID, NAME, VALUEASNUMBER, TYPE) values (?, ?, ?, ?)");
					pst.setInt(1, key);
					pst.setString(2, field.name);
					pst.setBigDecimal(3, field.valueAsNumber);
					pst.setInt(4, field.getTypeAsDatabaseType());		
					pst.executeUpdate();
					break;
				case Const.DATETIMES:
					pst = conn.prepareStatement("insert into CUSTOM_FIELDS_GLOSSARY(DOCID, NAME, VALUEASDATE, TYPE) values (?, ?, ?, ?)");
					pst.setInt(1, key);
					pst.setString(2, field.name);
					pst.setTimestamp(3, new Timestamp(field.valueAsDate.getTime()));
					pst.setInt(4, field.getTypeAsDatabaseType());		
					pst.executeUpdate();
					break;
				case Const.GLOSSARY:
					pst = conn.prepareStatement("insert into CUSTOM_FIELDS_GLOSSARY(DOCID, NAME, VALUEASGLOSSARY, TYPE) values (?, ?, ?, ?)");
					pst.setInt(1, key);
					pst.setString(2, field.name);
					for (Integer value: field.valuesAsGlossaryData) {
						pst.setInt(3, value);
						pst.setInt(4, field.getTypeAsDatabaseType());		
						try {
							pst.executeUpdate();							
						} catch (SQLException se) {
							DatabaseUtil.debugErrorPrint(se);
							return -1;
						}
					}
					break;
				}					
			}
			pst = conn.prepareStatement("INSERT INTO GLOSSARY_TREE_PATH (ANCESTOR, DESCENDANT, LENGTH) " +
					" SELECT gtp.ANCESTOR, " + key + ", gtp.LENGTH + 1 FROM GLOSSARY_TREE_PATH as gtp WHERE gtp.DESCENDANT = " + doc.parentDocID + " UNION ALL SELECT " + key + ", " + key + ", 0");
			pst.executeUpdate();

            insertBlobTables(conn, doc.getDocID(), key, doc, baseTable);

            conn.commit();
			pst.close();
			CachePool.flush();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
			return -1;		
		} catch (IOException e) {
            DatabaseUtil.debugErrorPrint(e);
            return -1;
        } finally {
			dbPool.returnConnection(conn);
		}
		return key;
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
					+ "', " + viewTextList
					+ ", VIEWNUMBER = " + doc.getViewNumber()
					+ ", VIEWDATE = " + (viewDate != null ? "'" + new Timestamp(viewDate.getTime()) + "'" : "null")
					+"  where DOCID = " + doc.getDocID();
			pst = conn.prepareStatement(sql);
			pst.executeUpdate();
			for (Field field : doc.fields()) {
				switch (field.getTypeAsDatabaseType()) {
				case Const.TEXT:
                    sql = "update CUSTOM_FIELDS_GLOSSARY set value = '" + field.valueAsText.replace("'", "''").trim() +
                            "', type = " + field.getTypeAsDatabaseType() + "where docid =  " + doc.getDocID() + "and name = '" + field.name + "';" +
                            " insert into CUSTOM_FIELDS_GLOSSARY (docid, name, value, type) " +
                            " select " + doc.getDocID() + ", '" + field.name + "', '" + field.valueAsText.replace("'", "''").trim() + "', " +
                            field.getTypeAsDatabaseType() +
                            " where 1 not in (select 1 from CUSTOM_FIELDS_GLOSSARY where docid = " + doc.getDocID() + " and name = '" + field.name + "')";
					pst = conn.prepareStatement(sql);
					pst.executeUpdate();
					break;
				case Const.NUMBERS:
                    sql = "update CUSTOM_FIELDS_GLOSSARY set valueasnumber = " + field.valueAsNumber +
                            ", type = " + field.getTypeAsDatabaseType() + "where docid =  " + doc.getDocID() + "and name = '" + field.name + "';" +
                            " insert into CUSTOM_FIELDS_GLOSSARY (docid, name, valueasnumber, type) " +
                            " select " + doc.getDocID() + ", '" + field.name + "', " + field.valueAsNumber + ", " +
                            field.getTypeAsDatabaseType() +
                            " where 1 not in (select 1 from CUSTOM_FIELDS_GLOSSARY where docid = " + doc.getDocID() + " and name = '" + field.name + "')";
					pst = conn.prepareStatement(sql);
					pst.executeUpdate();
					break;
				case Const.DATETIMES:
                    if (field.valueAsDate == null) {
                        Database.logger.errorLogEntry("Unable to convert empty date to DB format: " + field.name);
                        continue;
                    }
                    try {
                        sql = "update CUSTOM_FIELDS_GLOSSARY set valueasdate = '" + Util.convertDateTimeToDerbyFormat(field.valueAsDate) +
                                "', type = " + field.getTypeAsDatabaseType() + " where docid =  " + doc.getDocID() + " and name = '" + field.name + "';" +
                                " insert into CUSTOM_FIELDS_GLOSSARY (docid, name, valueasdate, type) " +
                                " select " + doc.getDocID() + ", '" + field.name + "', '" + Util.convertDateTimeToDerbyFormat(field.valueAsDate) + "', " +
                                field.getTypeAsDatabaseType() +
                                " where 1 not in (select 1 from CUSTOM_FIELDS_GLOSSARY where docid = " + doc.getDocID() + " and name = '" + field.name + "')";
                    } catch (DataConversionException e) {
                        Database.logger.errorLogEntry(e + ", field=" + field.name);
                        continue;
                    }
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
			CachePool.flush();
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
        ResultSet blobs = s.executeQuery("SELECT ID, DOCID, NAME, ORIGINALNAME, CHECKSUM, COMMENT, VALUE " +
                "FROM CUSTOM_BLOBS_" + tableSuffix + " WHERE DOCID = " + doc.getDocID());
        while (blobs.next()) {
            if (!doc.blobFieldsMap.containsKey(blobs.getString("NAME"))) {
                blobs.deleteRow();
                continue;
            }
            BlobField existingBlob = doc.blobFieldsMap.get(blobs.getString("NAME"));
            BlobFile tableFile = new BlobFile();
            tableFile.originalName = blobs.getString("ORIGINALNAME");
            tableFile.checkHash = blobs.getString("CHECKSUM");
            tableFile.comment = blobs.getString("COMMENT");
            if (existingBlob.findFile(tableFile) == null) {
                blobs.deleteRow();
                continue;
            }
            BlobFile existingFile = existingBlob.findFile(tableFile);
            if (!existingFile.originalName.equals(tableFile.originalName)) {
                blobs.updateString("ORIGINALNAME", existingFile.originalName);
                blobs.updateRow();
            }
            if (existingFile.comment != null && (!existingFile.comment.equals(tableFile.comment))) {
                blobs.updateString("COMMENT", existingFile.comment);
                blobs.updateRow();
            }
        }
		/* now add files that are absent in database */
        for (Map.Entry<String, BlobField> blob : doc.blobFieldsMap.entrySet()) {
            PreparedStatement ps = conn.prepareStatement("SELECT ID, DOCID, NAME, ORIGINALNAME, CHECKSUM, COMMENT, VALUE " +
                    "FROM CUSTOM_BLOBS_" + tableSuffix + " WHERE DOCID = ? AND NAME = ? AND CHECKSUM = ?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            for (BlobFile bfile : blob.getValue().getFiles()) {
                ps.setInt(1, doc.getDocID());
                ps.setString(2, blob.getKey());
                ps.setString(3, bfile.checkHash);
                blobs = ps.executeQuery();
                if (blobs.next()) {
                    if (!bfile.originalName.equals(blobs.getString("ORIGINALNAME"))) {
                        blobs.updateString("ORIGINALNAME", bfile.originalName);
                        blobs.updateRow();
                    }
                    if (!bfile.comment.equals(blobs.getString("COMMENT"))) {
                        blobs.updateString("COMMENT", bfile.comment);
                        blobs.updateRow();
                    }
                } else {
                    blobs.moveToInsertRow();
                    blobs.updateInt("DOCID", doc.getDocID());
                    blobs.updateString("NAME", blob.getKey());
                    blobs.updateString("ORIGINALNAME", bfile.originalName);
                    blobs.updateString("CHECKSUM", bfile.checkHash);
                    blobs.updateString("COMMENT", bfile.comment);
                    File file = new File(bfile.path);
                    InputStream is = new FileInputStream(file);
                    blobs.updateBinaryStream("VALUE", is);
                    blobs.insertRow();
                    is.close();
                }
                Environment.fileToDelete.add(bfile.path);
            }
        }
    }

	@Override
	public boolean deleteGlossaryDocument(int docID) {
		boolean deleteGlDoc = false;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String delGlossaryTable = "delete from GLOSSARY where DOCID = "
					+ docID;
			PreparedStatement pst = conn.prepareStatement(delGlossaryTable);
			pst.executeUpdate();
			conn.commit();	
			pst.close();
			deleteGlDoc = true;
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
			deleteGlDoc = false;
		} finally {	
			dbPool.returnConnection(conn);	
		}
		return deleteGlDoc;
	}

	@Override
	public ArrayList<Glossary> getGlossaryResponses(int docID, int docType, Set<String> complexUserID, String absoluteUserID) throws DocumentException, QueryException {
		ArrayList<Glossary>  documents = new ArrayList<Glossary>();		
		if (docID != 0 && docType != Const.DOCTYPE_UNKNOWN){
			Connection conn = dbPool.getConnection();
			try {
				conn.setAutoCommit(false);
				Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				String sql = "SELECT * FROM GLOSSARY WHERE DOCID IN (SELECT DESCENDANT FROM GLOSSARY_TREE_PATH WHERE ANCESTOR = " + docID + " AND LENGTH = 1)";
				ResultSet rs = statement.executeQuery(sql);
				while (rs.next()) {				
					int respDocID = 0;
					Glossary doc = null;
					respDocID = rs.getInt("DOCID");
					doc = getGlossaryDocumentByID(respDocID, false, complexUserID, absoluteUserID);
					documents.add(doc);
				}				
				rs.close();
				statement.close();
				conn.commit();
			} catch (SQLException e) {
				DatabaseUtil.debugErrorPrint(e);
			}finally{	
				dbPool.returnConnection(conn);
			}
		}
		return documents;
	}

	@Override
	public DocumentCollection getGlossaryDescendants(int docID, int docType, DocID[] toExpand, int level, Set<String> complexUserID, String absoluteUserID) throws DocumentException {
		int col = 0;
		DocumentCollection documents = new DocumentCollection();
		StringBuffer xmlContent = new StringBuffer(10000);		
		String value = "";
		if (docID != 0 && docType != DOCTYPE_UNKNOWN){
			Connection conn = dbPool.getConnection();
			try {	
				conn.setAutoCommit(false);
				Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				String sql = "SELECT * FROM GLOSSARY WHERE DOCID IN (SELECT DESCENDANT FROM GLOSSARY_TREE_PATH WHERE ANCESTOR = " + docID + " AND LENGTH = 1)" 
						+ " ORDER BY REGDATE";

				ResultSet rs = statement.executeQuery(sql);

				while (rs.next()) {
					String viewText = rs.getString("VIEWTEXT1");					
					value = "";	
					int respDocID = rs.getInt("DOCID");
					int repsDocType = rs.getInt("DOCTYPE");
					String ddbID = rs.getString("DDBID");
					String form = rs.getString("FORM");				
					xmlContent.append("<entry doctype=\"" + repsDocType
							+ "\"  docid=\"" + respDocID + "\" id=\""+ ddbID +"\" form=\"" + form + "\" url=\"Provider?type=edit&amp;element=glossary&amp;id=" + form + "&amp;key="
							+ respDocID + "\"" + XMLUtil.getAsAttribute("viewtext", viewText));
					col ++ ;

					int l = level + 1;
					DocumentCollection responses = getGlossaryDescendants(respDocID, repsDocType, null, l, complexUserID, absoluteUserID );
					if (responses.count > 0) {
						xmlContent.append(" hasresponse=\"true\" >");
						value += responses.xmlContent;						
					} else {
						xmlContent.append(" >");
					}

					xmlContent.append(value += "<viewtext>" + XMLUtil.getAsTagValue(viewText) + "</viewtext></entry>");
					col++;

				}

				documents.xmlContent.append(xmlContent);
				rs.close();
				statement.close();
				conn.commit();
				documents.count = col;
			} catch (SQLException e) {
				DatabaseUtil.debugErrorPrint(e);
			}finally{	
				dbPool.returnConnection(conn);
			}
		}
		return documents;
	}

	private Glossary fillGloassary(ResultSet rs) throws SQLException{
		Glossary doc = new Glossary(env, new User(Const.sysUser));
		fillShortViewTextData(rs, doc);
		fillSysData(rs, doc);
		boolean isNextMain = true;
		while (isNextMain || rs.next()) {
			switch (rs.getInt("TYPE")) {
			case TEXT:
				doc.addStringField(rs.getString("NAME"),
						rs.getString("VALUE"));
				break;
			case NUMBERS:
				doc.addNumberField(rs.getString("NAME"),
						rs.getBigDecimal("VALUEASNUMBER"));
				break;
			case DATETIMES:
				doc.addDateField(rs.getString("NAME"),
						rs.getDate("VALUEASDATE"));
				break;
			case GLOSSARY:
				doc.addNumberField(rs.getString("NAME"), rs.getInt("VALUEASGLOSSARY"));
				break;
			case TEXTLIST:
				doc.addValueToList(rs.getString("NAME"), rs.getString("VALUE"));
				break;
			}
			isNextMain = false;
		}
		return doc;
	}

	@Override
	public _ViewEntryCollection getCollectionByCondition(ISelectFormula sf,	int pageNum, int pageSize, Set<DocID> expandedDocuments, RunTimeParameters parameters,	boolean checkResponse) {
        ViewEntryCollection coll = new ViewEntryCollection(pageSize, new User(Const.sysUser, env), parameters);
        Set<String> users = new HashSet<String>();
		users.add(Const.sysUser);
        Connection conn = dbPool.getConnection();
        try {
            conn.setAutoCommit(false);
            Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            if (pageNum == 0){
                String sql = sf.getCountCondition(users,parameters.getFilters());
                ResultSet rs = s.executeQuery(sql);
                if (rs.next()){
                    pageNum = RuntimeObjUtil.countMaxPage(rs.getInt(1), pageSize);
                }
            }
            int offset = db.calcStartEntry(pageNum, pageSize);
            String sql = sf.getCondition(users, pageSize, offset, parameters.getFilters(), parameters.getSorting(), checkResponse);
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()){
                ViewEntry entry = new ViewEntry(this.db, rs, expandedDocuments, new User(Const.sysUser, env),parameters.getDateFormat());
                coll.add(entry);
                coll.setCount(rs.getInt(1));
                while (rs.next()){
                    entry = new ViewEntry(this.db, rs, expandedDocuments, new User(Const.sysUser, env), parameters.getDateFormat());
                    coll.add(entry);
                }
            }
            conn.commit();
            s.close();
            rs.close();

        } catch (SQLException e) {
            DatabaseUtil.errorPrint(this.db.getDbID(), e);
        } catch (Exception e) {
            Database.logger.errorLogEntry(e);
        } finally {
            dbPool.returnConnection(conn);
        }
        coll.setCurrentPage(pageNum);
        return coll.getScriptingObj();
	}
}
