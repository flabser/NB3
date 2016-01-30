package kz.flabs.dataengine.h2.glossary;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseCore;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IPlainGlossaries;
import kz.flabs.dataengine.IQueryFormula;
import kz.flabs.dataengine.h2.Database;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.QueryExceptionType;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.Field;
import kz.flabs.runtimeobj.document.glossary.Glossary;
import kz.flabs.runtimeobj.queries.CachePool;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.users.User;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.TagPublicationFormatType;

public class PlainGlossaries   {}	/*
	private static final String glossaryFields = "GLOSSARY.DOCID, AUTHOR, NOTESURL, PARENTDOCID, PARENTDOCTYPE, REGDATE, DOCTYPE, LASTUPDATE, VIEWTEXT, VIEWICON, NOTESID, FORM";

	protected AppEnv env;
	protected IDatabase db;
	protected IDBConnectionPool dbPool;	

	public PlainGlossaries(AppEnv env) {	
		this.env = env;
		this.db = env.getDataBase();
		this.dbPool = db.getConnectionPool();

	}

	@Override
	public int getGlossaryElementID(String keyWord, String form) {
		Glossary doc = new Glossary(env, new User(Const.sysUser));
		int docid;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String allDoc = "SELECT * FROM GLOSSARY, CUSTOM_FIELDS_GLOSSARY where GLOSSARY.DOCID = CUSTOM_FIELDS_GLOSSARY.DOCID and GLOSSARY.FORM = '" +  
					form + "' and CUSTOM_FIELDS_GLOSSARY.NAME='name' and LOWER(CUSTOM_FIELDS_GLOSSARY.VALUE)='" + keyWord.toLowerCase() + "'";
			PreparedStatement pst = conn.prepareStatement(allDoc);
			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				docid = rs.getInt("DOCID");
				pst.close();
				return docid;
			}
			rs.close();
			pst.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {	
			dbPool.returnConnection(conn);
			doc.isValid = false;
		}
		return 0;
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
			DatabaseUtil.errorPrint(db.getDbID(), e);
			deleteGlDoc = false;
		} finally {	
			dbPool.returnConnection(conn);	
		}
		return deleteGlDoc;
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
				doc.setNotesID(rs.getString("NOTESID"));
				doc.setViewText(rs.getString("VIEWTEXT"));
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
						doc.addField(field, rsFields.getString("VALUE"),
								rsFields.getInt("TYPE"));
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
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {			
			dbPool.returnConnection(conn);		
		}
		return documents;
	}

	@Override
	public StringBuffer getGlossaryByCondition(IQueryFormula blocks, int offset, int pageSize, String fieldsCond, Set<String> toExpand, TagPublicationFormatType publishAs) throws DocumentException, QueryException {
		StringBuffer xmlContent = new StringBuffer(10000);		
		Connection conn = dbPool.getConnection();		
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,	ResultSet.CONCUR_READ_ONLY);
			String sql = blocks.getSQL(new HashSet<String>(Arrays.asList(Const.sysGroup))) + " LIMIT " + pageSize + " OFFSET " + offset;
			ResultSet rs = s.executeQuery(sql);
			if (blocks.isGroupBy()){
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
								"url=\"Provider?type=view&amp;id=" + blocks.getQueryID() + "&amp;command=expand`" + categoryID + "\" >" +
								"<viewtext>" + XMLUtil.getAsTagValue(viewText) + "</viewtext>");

						if (toExpand != null && toExpand.size() > 0) {
							for (String category : toExpand) {
								if (categoryID.equalsIgnoreCase(category)) {
									StringBuffer categoryValue = getGetOneCategory(blocks.getGroupCondition(category), fieldsCond);
									//StringBuffer categoryValue = getGetOneCategory(queryCondition.getCategoryName(), category, fieldsCond);
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
					String viewText = rs.getString("VIEWTEXT");
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

					String tempText = rs.getString("VIEWTEXT1");
					String viewcontent = "<viewtext1>" + (tempText != null ? XMLUtil.getAsTagValue(tempText) : "") + "</viewtext1>";

					tempText = rs.getString("VIEWTEXT2");
					viewcontent += "<viewtext2>" + (tempText != null ? XMLUtil.getAsTagValue(tempText) : "") + "</viewtext2>";

					tempText = rs.getString("VIEWTEXT3");
					viewcontent += "<viewtext3>" + (tempText != null ? XMLUtil.getAsTagValue(tempText) : "") + "</viewtext3>";

					tempText = String.valueOf(rs.getInt("VIEWNUMBER"));
					if (rs.wasNull()){
						tempText = "";
					}

					viewcontent += "<viewnumber>" + tempText + "</viewnumber>";

					Date tempDate = rs.getTimestamp("VIEWDATE");
					viewcontent += "<viewdate>" + (tempDate != null ? Database.dateTimeFormat.format(tempDate) : "") + "</viewdate>";

					xmlContent.append("<entry  doctype=\"" + DOCTYPE_GLOSSARY + "\"  " +
							"docid=\"" + docID + "\"" + XMLUtil.getAsAttribute("viewtext", viewText) +
							"url=\"Provider?type=glossary&amp;id=" + form + "&amp;key="	+ docID + "\" " +						 						
							"><viewcontent>" + viewcontent + "</viewcontent><viewtext>" + XMLUtil.getAsTagValue(viewText) + "</viewtext>" + value);

					xmlContent.append("</entry>");
				}
			}
			s.close();
			rs.close();
			conn.commit();		
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			throw new QueryException(QueryExceptionType.RUNTIME_ERROR);
		} finally {		
			dbPool.returnConnection(conn);
		}		
		return xmlContent;
	}

	public StringBuffer getGetOneCategory(String addCondition, String fieldsCond) {
		StringBuffer xmlContent = new StringBuffer(10000);		
		Connection conn = dbPool.getConnection();
		try {	
			conn.setAutoCommit(false);
			Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

			String sql = "SELECT DISTINCT " + glossaryFields + " FROM GLOSSARY " +
					addCondition + " ORDER BY GLOSSARY.regdate";				

			ResultSet rs = statement.executeQuery(sql);

			while (rs.next()) {
				xmlContent.append(getDocumentEntry(conn, rs, fieldsCond));	
			}

			rs.close();
			statement.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} catch (DocumentException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		}finally{	
			dbPool.returnConnection(conn);
		}
		return xmlContent;
	}


	private String getDocumentEntry(Connection conn, ResultSet rs, String fieldsCond) throws SQLException, DocumentException{
		String customFieldsValue = "";	
		int docID = rs.getInt("DOCID");

		Statement sFields = conn.createStatement();
		String addSQL = "select * from CUSTOM_FIELDS_GLOSSARY where CUSTOM_FIELDS_GLOSSARY.DOCID = " + docID + fieldsCond;
		ResultSet rsFields = sFields.executeQuery(addSQL);
		while (rsFields.next()) {
			String name = rsFields.getString("NAME");
			String valueAsGlossary, attr = "";			
			int gVal = rsFields.getInt("VALUEASGLOSSARY");				
			Glossary doc = getGlossaryDocumentByID(gVal, false, Const.sysGroupAsSet, Const.sysUser);
			if (doc != null){
				valueAsGlossary = doc.getViewText();
			}else{
				valueAsGlossary = Integer.toString(gVal);
				attr = " error=\"glossary not found\" ";
			}
			customFieldsValue += "<" + name + attr + ">" + XMLUtil.getAsTagValue(valueAsGlossary) + "</" + name + ">";
			break;

		}

		String value = "<entry docid=\"" + docID + "\"" + XMLUtil.getAsAttribute("viewtext", rs.getString("VIEWTEXT")) +
				"url=\"Provider?type=glossary&amp;id=" + rs.getString("FORM") + "&amp;key="	+ docID + "\" " +						 						
				"><viewtext>" + XMLUtil.getAsTagValue(rs.getString("VIEWTEXT")) + "</viewtext>" + customFieldsValue;

		sFields.close();		
		return value += "</entry>";
	}


	@Override
	public ArrayList<Glossary> getGlossaryByCondition(IQueryFormula condition, int offset, int pageSize) {
		ArrayList<Glossary> documents = new ArrayList<Glossary>();
		String sql = "";

		if(pageSize>0){
			sql = condition.getSQL(new HashSet<String>(Arrays.asList(Const.sysGroup))) + " LIMIT " + pageSize + " OFFSET " + offset;
		}else{
			sql = condition.getSQL(new HashSet<String>(Arrays.asList(Const.sysGroup)));
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
				doc.docType = DOCTYPE_GLOSSARY;
				doc.form = rs.getString("FORM");
				doc.setAuthor(rs.getString("AUTHOR"));
				doc.setRegDate(rs.getDate("REGDATE"));
				doc.docType = rs.getInt("DOCTYPE");
				doc.setNotesID(rs.getString("NOTESID"));
				doc.setViewText(rs.getString("VIEWTEXT"));
				doc.parentDocID = rs.getInt("PARENTDOCID");
				doc.parentDocType = rs.getInt("PARENTDOCTYPE");
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
						doc.addIntField(fieldName,
								rsFields.getInt("VALUEASNUMBER"));
						break;
					case DATETIMES:
						doc.addDateField(fieldName,
								rsFields.getDate("VALUEASDATE"));
						break;
					case GLOSSARY:						
						doc.addGlossaryField(fieldName, rsFields.getInt("VALUEASGLOSSARY"));					
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
	public int getGlossaryCount() {
		Connection conn = dbPool.getConnection();
		int size = 0;
		try {
			conn.setAutoCommit(false);
			String sql = "select * from GLOSSARY";
			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				size++;
			}
			rs.close();
			pst.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		}finally{	
			dbPool.returnConnection(conn);
		}
		return size;
	}

	@Override
	public int getGlossaryByConditionCount(IQueryFormula condition) {
		Connection conn = dbPool.getConnection();
		int size = 0;
		try {
			conn.setAutoCommit(false);
			String sql;
			if (!condition.isGroupBy()) {
				sql = condition.getSQLCount(new HashSet<String>(Arrays.asList(Const.sysGroup)));
			} else {
				sql = condition.getSQLGroupCount(new HashSet<String>(Arrays.asList(Const.sysGroup)));
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
			DatabaseUtil.errorPrint(db.getDbID(), e);
		}finally{		
			dbPool.returnConnection(conn);
		}
		return size;
	}

	@Override
	public Glossary getGlossaryDocumentByID(int docID, boolean useCache, Set<String> complexUserID, String absoluteUserID) {
		Glossary doc = null;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String allDoc = "select * from GLOSSARY, CUSTOM_FIELDS_GLOSSARY where GLOSSARY.DOCID = CUSTOM_FIELDS_GLOSSARY.DOCID"
					+ " and GLOSSARY.DOCID=" + docID;
			PreparedStatement pst = conn.prepareStatement(allDoc);
			ResultSet rs = pst.executeQuery();
			boolean isNextMain = true;
			if (rs.next()) {
				doc = new Glossary(env, new User(Const.sysUser));
				fillSysData(rs, doc);
				while (isNextMain || rs.next()) {
					switch (rs.getInt("TYPE")) {
					case TEXT:
						doc.addStringField(rs.getString("NAME"),
								rs.getString("VALUE"));
						break;
					case NUMBERS:
						doc.addIntField(rs.getString("NAME"),
								rs.getInt("VALUEASNUMBER"));
						break;
					case DATETIMES:
						doc.addDateField(rs.getString("NAME"),
								rs.getDate("VALUEASDATE"));
						break;
					case GLOSSARY:
						doc.addIntField(rs.getString("NAME"), rs.getInt("VALUEASGLOSSARY"));
						break;
					}
					isNextMain = false;
				}
				doc.setEditMode(complexUserID);

			}
			rs.close();
			pst.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
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
			Date viewDate = null;
			ArrayList<Date> dates = doc.getViewDateList();
			if (dates != null && !dates.isEmpty()) {
				viewDate = dates.get(0);
			}
			String viewtext1 = "";
			String viewtext2 = "";
			String viewtext3 = "";
			ArrayList<String> vt = doc.getViewTextList();
			if (vt != null && !vt.isEmpty()) {
				if (vt.get(0) != null) {
					viewtext1 = vt.get(0).replace("'", "''");
				}
				if (vt.get(1) != null) {
					viewtext2 = vt.get(1).replace("'", "''");
				}
				if (vt.get(2) != null) {
					viewtext3 = vt.get(2).replace("'", "''");
				}
			}
			Integer viewNumber = 0;
			ArrayList<Integer> numbers = doc.getViewNumberList();
			if (numbers != null && !numbers.isEmpty()) {
				viewNumber = numbers.get(0);
			}
			
			String insertGlossary = "insert into GLOSSARY(AUTHOR, REGDATE, DOCTYPE, NOTESID, VIEWTEXT, LASTUPDATE, PARENTDOCID, " +
					"PARENTDOCTYPE, NOTESURL, FORM, DEFAULTRULEID, VIEWTEXT1, VIEWTEXT2, VIEWTEXT3, VIEWNUMBER, VIEWDATE) "
					+ "values('"
					+ doc.getAuthor()
					+ "', '"
					+ Database.sqlDateTimeFormat.format(doc.getRegDate())
					+ "', "
					+ Const.DOCTYPE_GLOSSARY
					+ ", '"
					+ doc.getNotesID()
					+ "', "
					+ "'"
					+ doc.getViewText().replace("'", "''").trim()
					+ "', "
					+ "'"
					+ Database.sqlDateTimeFormat.format(doc.getLastUpdate())
					+ "', "
					+ ""
					+ doc.parentDocID
					+ ", "
					+ doc.parentDocType
					+ ",' " + doc.notesURL + "','" + doc.form.toLowerCase() + "'"
					+ ", '" + doc.getDefaultRuleID() + "'"
					+ ", '" + viewtext1 + "'"
					+ ", '" + viewtext2 + "'"
					+ ", '" + viewtext3 + "'"
					+ ", " + viewNumber 
					+ ", " + (viewDate != null ? "'" + new Timestamp(viewDate.getTime()) + "'" : "null")
					+ ")";
			PreparedStatement pst = conn.prepareStatement(insertGlossary,
					PreparedStatement.RETURN_GENERATED_KEYS);
			pst.executeUpdate();

			ResultSet rs = pst.getGeneratedKeys();
			while (rs.next()) {
				key = rs.getInt(1);
			}
			for (Field field : doc.fields()) {
				switch (field.getTypeAsDatabaseType()) {
				case TEXT:
					pst = conn
					.prepareStatement("insert into CUSTOM_FIELDS_GLOSSARY(DOCID, NAME, VALUE, TYPE)"
							+ "values ("
							+ key
							+ ", '"
							+ field.name
							+ "', '"
							+ field.valueAsText.replace("'", "''").replace(';', ',').trim()
							+ "', "
							+ field.getTypeAsDatabaseType() + ")");				
					pst.executeUpdate();
					pst.close();
					break;
				case NUMBERS:
					pst = conn
					.prepareStatement("insert into CUSTOM_FIELDS_GLOSSARY(DOCID, NAME, VALUEASNUMBER, TYPE)"
							+ "values("
							+ key
							+ ", '"
							+ field.name
							+ "', "
							+ Long.toString(field.valueAsNumber)
							+ ", "
							+ field.getTypeAsDatabaseType() + ")");
					pst.executeUpdate();
					pst.close();
					break;
				case DATETIMES:

					pst = conn
					.prepareStatement("insert into CUSTOM_FIELDS_GLOSSARY(DOCID, NAME, VALUEASDATE, TYPE)"
							+ "values("
							+ key
							+ ", '"
							+ field.name
							+ "', '"
							+ Database.sqlDateTimeFormat.format(field.valueAsDate)
							+ "', " + field.getTypeAsDatabaseType() + ")");
					pst.executeUpdate();
					pst.close();
					break;
				case GLOSSARY:
					pst = conn.prepareStatement("insert into CUSTOM_FIELDS_GLOSSARY(DOCID, NAME, VALUEASGLOSSARY, TYPE)"
							+ " values ("
							+ key
							+ ", '"
							+ field.name
							+ "', ?,"
							+ field.getTypeAsDatabaseType() + ")");
					for (Integer value: field.valuesAsGlossaryData) {
						pst.setInt(1, value);
						try {
							pst.executeUpdate();
						} catch (SQLException se) {
							System.out.println(doc.getNotesID());
							System.out.println(field.name);
							DatabaseUtil.errorPrint(db.getDbID(), se);
							return -1;
						}
					}
					pst.close();
					break;
				}				
			}
			CachePool.flush();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
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
			Date viewDate = doc.getViewDateList().get(0);
			sql = "update GLOSSARY set LASTUPDATE = '"
					+ Database.sqlDateTimeFormat.format(doc.getLastUpdate()) + "', VIEWTEXT='"
					+ doc.getViewText().replace("'", "''").trim()  
					+ "', DEFAULTRULEID = '" + doc.getDefaultRuleID() + "'"
					+ ", VIEWTEXT1 = '" + doc.getViewTextList().get(0).replace("'", "''") + "'"
					+ ", VIEWTEXT2 = '" + doc.getViewTextList().get(1).replace("'", "''") + "'"
					+ ", VIEWTEXT3 = '" + doc.getViewTextList().get(2).replace("'", "''") + "'"
					+ ", VIEWNUMBER = " + doc.getViewNumberList().get(0)
					+ ", VIEWDATE = " + (viewDate != null ? "'" + new Timestamp(viewDate.getTime()) + "'" : "null")
					+"  where DOCID = " + doc.getDocID();
			pst = conn.prepareStatement(sql);
			pst.executeUpdate();
			for (Field field : doc.fields()) {
				switch (field.getTypeAsDatabaseType()) {
				case TEXT:
					sql = "update CUSTOM_FIELDS_GLOSSARY set VALUE='"
							+ field.valueAsText.replace("'", "''").trim() + "', " + "TYPE=" + field.getTypeAsDatabaseType()
							+ " where DOCID=" + doc.getDocID() + " and NAME='"
							+ field.name + "'";
					pst = conn.prepareStatement(sql);
					pst.executeUpdate();
					break;
				case NUMBERS:
					sql = "update CUSTOM_FIELDS_GLOSSARY set VALUEASNUMBER="
							+ field.valueAsNumber + ", " + "TYPE=" + field.getTypeAsDatabaseType()
							+ " where DOCID=" + doc.getDocID() + " and NAME='"
							+ field.name + "'";
					pst = conn.prepareStatement(sql);
					pst.executeUpdate();
					break;
				case DATETIMES:					
					sql = "update CUSTOM_FIELDS_GLOSSARY set VALUEASDATE='"
							+ Database.sqlDateTimeFormat.format(field.valueAsDate) + "', "
							+ "TYPE=" + field.getTypeAsDatabaseType() + " where DOCID="
							+ doc.getDocID() + " and NAME='" + field.name + "'";
					pst = conn.prepareStatement(sql);
					pst.executeUpdate();
					break;
				case GLOSSARY:
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
							//		System.out.println(doc.getNotesID());
							//		System.out.println(field.name);
							DatabaseUtil.errorPrint(db.getDbID(), se);
							return -1;
						}
					}
					pst.close();
					break;
				}
			}
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;		
		} finally {		
			dbPool.returnConnection(conn);
		}
		return doc.getDocID();
	}

}
*/