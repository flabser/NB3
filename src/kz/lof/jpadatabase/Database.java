package kz.lof.jpadatabase;

import static kz.flabs.runtimeobj.RuntimeObjUtil.cutText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseConst;
import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.DatabaseType;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IExecutions;
import kz.flabs.dataengine.IFTIndexEngine;
import kz.flabs.dataengine.IFilters;
import kz.flabs.dataengine.IForum;
import kz.flabs.dataengine.IGlossaries;
import kz.flabs.dataengine.IProjects;
import kz.flabs.dataengine.IQueryFormula;
import kz.flabs.dataengine.ISelectFormula;
import kz.flabs.dataengine.IStructure;
import kz.flabs.dataengine.ITasks;
import kz.flabs.dataengine.IUsersActivity;
import kz.flabs.dataengine.h2.glossary.GlossaryQueryFormula;
import kz.flabs.dataengine.h2.queryformula.GroupQueryFormula;
import kz.flabs.dataengine.h2.queryformula.ProjectQueryFormula;
import kz.flabs.dataengine.h2.structure.StructQueryFormula;
import kz.flabs.dataengine.postgresql.filters.Filters;
import kz.flabs.dataengine.postgresql.forum.ForumSelectFormula;
import kz.flabs.dataengine.postgresql.glossary.Glossaries;
import kz.flabs.dataengine.postgresql.queryformula.GlossarySelectFormula;
import kz.flabs.dataengine.postgresql.queryformula.QueryFormula;
import kz.flabs.dataengine.postgresql.queryformula.SelectFormula;
import kz.flabs.dataengine.postgresql.queryformula.TaskQueryFormula;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DataConversionException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.DocumentExceptionType;
import kz.flabs.exception.ExceptionType;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.parser.ParserUtil;
import kz.flabs.parser.SortByBlock;
import kz.flabs.runtimeobj.DocumentCollection;
import kz.flabs.runtimeobj.document.AbstractComplexObject;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.BlobField;
import kz.flabs.runtimeobj.document.BlobFile;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.Field;
import kz.flabs.runtimeobj.document.coordination.Block;
import kz.flabs.runtimeobj.document.coordination.BlockCollection;
import kz.flabs.runtimeobj.document.coordination.Coordinator;
import kz.flabs.runtimeobj.viewentry.ViewEntry;
import kz.flabs.servlets.sitefiles.UploadedFile;
import kz.flabs.users.User;
import kz.flabs.util.Util;
import kz.flabs.webrule.module.ExternalModule;
import kz.flabs.webrule.module.ExternalModuleType;
import kz.lof.jpadatabase.ftengine.FTIndexEngine;
import kz.pchelka.env.Environment;
import kz.pchelka.server.Server;

import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

public class Database extends kz.flabs.dataengine.h2.Database implements IDatabase, Const {
	protected EntityManagerFactory factory;

	public Database(AppEnv env) throws DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		super(env, DatabaseType.JPA);
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(PersistenceUnitProperties.JDBC_DRIVER, env.globalSetting.driver);
		properties.put(PersistenceUnitProperties.JDBC_USER, env.globalSetting.getDbUserName());
		properties.put(PersistenceUnitProperties.JDBC_PASSWORD, env.globalSetting.getDbPassword());
		properties.put(PersistenceUnitProperties.JDBC_URL, connectionURL);

		// INFO,
		// OFF,
		// ALL,
		// CONFIG (developing)
		properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "OFF");
		properties.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.CREATE_OR_EXTEND);
		properties
		        .put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_ACTION, PersistenceUnitProperties.SCHEMA_GENERATION_DROP_AND_CREATE_ACTION);

		PersistenceProvider pp = new PersistenceProvider();
		factory = pp.createEntityManagerFactory(env.appType, properties);
		if (factory == null) {
			Server.logger.errorLogEntry("the entity manager of \"" + env.appType + "\" has not been initialized");

		}
	}

	@Override
	protected void initStructPool() {
		for (ExternalModule module : env.globalSetting.extModuleMap.values()) {
			if (module.getType() == ExternalModuleType.STAFF) {
				externalStructureApp = module.getName();
				Environment.addDelayedInit(this);
			} else {
				Environment.addDelayedInit(this);
			}
		}
		structDbPool = dbPool;
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return factory;

	}

	@Override
	public IStructure getStructure() {
		return new Structure(this, dbPool);
	}

	@Override
	public IFTIndexEngine getFTSearchEngine() {
		return new FTIndexEngine(this);
	}

	@Override
	public IGlossaries getGlossaries() {
		return new Glossaries(env);
	}

	@Override
	public String toString() {
		return "version: with JPA, URL:" + connectionURL;
	}

	@Override
	public ArrayList<ViewEntry> getGroupedEntries(String fieldName, int offset, int pageSize, User user) {
		ArrayList<ViewEntry> vec = new ArrayList<ViewEntry>();
		String result[] = ParserUtil.resolveFiledTypeBySuffix(fieldName);
		Set<String> users = user.getAllUserGroups();

		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "";
			if (!fieldName.contains("view")) {
				sql = "select unnest(string_to_array(cf." + result[1] + "::text, '#')) as val, count(cf." + result[1]
				        + ") from custom_fields as cf where cf.name = '" + result[0] + "' and " + " cf." + result[1] + " is not null and "
				        + " cf.docid in (select docid from readers_maindocs as rm where rm.username in (" + DatabaseUtil.prepareListToQuery(users)
				        + ")) " + " group by val order by val limit " + pageSize + " offset " + offset;
			} else {
				sql = "select m." + fieldName + ", count(m." + fieldName + ") from maindocs as m where m." + fieldName + " is not null and "
				        + "m.docid in (select docid from readers_maindocs as rm where rm.username in (" + DatabaseUtil.prepareListToQuery(users)
				        + ")) " + " group by m." + fieldName + " order by m." + fieldName + " limit " + pageSize + " offset " + offset;
			}

			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				vec.add(new ViewEntry(rs.getString(1), rs.getInt(2)));
			}
			conn.commit();
			s.close();
			rs.close();

		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbPool.returnConnection(conn);
		}
		return vec;
	}

	@Override
	public void fillBlobsContent(BaseDocument doc) throws SQLException {
		Connection conn = dbPool.getConnection();

		try {
			doc.blobFieldsMap.clear();
			Statement statement = conn.createStatement();
			String blobsTable = DatabaseUtil.getCustomBlobsTableName(doc.docType);
			ResultSet rs = statement.executeQuery("select * from " + blobsTable + " where " + blobsTable + ".DOCID = " + doc.getDocID());
			while (rs.next()) {
				HashMap<String, BlobFile> files = new HashMap<String, BlobFile>();
				String name = rs.getString("NAME");
				BlobFile bf = new BlobFile();
				bf.originalName = rs.getString("ORIGINALNAME");
				bf.checkHash = rs.getString("CHECKSUM");
				bf.comment = rs.getString("COMMENT");
				LargeObjectManager lobj = ((org.postgresql.PGConnection) ((DelegatingConnection) conn).getInnermostDelegate()).getLargeObjectAPI();
				long oid = rs.getLong("VALUE_OID");
				LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);
				bf.setContent(obj.read(obj.size()));
				files.put(bf.originalName, bf);
				doc.addBlobField(name, files);
			}
			rs.close();
			statement.close();
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Override
	public IQueryFormula getQueryFormula(String id, FormulaBlocks blocks) {
		switch (blocks.docType) {
		case STRUCTURE:
			StructQueryFormula sqf = new StructQueryFormula(id, blocks);
			return sqf;
		case GLOSSARY:
			return new GlossaryQueryFormula(id, blocks);
		case PROJECT:
			return new ProjectQueryFormula(id, blocks);
		case TASK:
			return new TaskQueryFormula(id, blocks);
		case GROUP:
			return new GroupQueryFormula(id, blocks);
		default:
			QueryFormula queryFormula = new QueryFormula(id, blocks);
			return queryFormula;
		}
	}

	@Override
	public ArrayList<BaseDocument> getDocumentsForMonth(HashSet<String> userGroups, String userID, String form, String fieldName, int month,
	        int offset, int pageSize) {
		ArrayList<BaseDocument> vec = new ArrayList<BaseDocument>();
		String userIDs = DatabaseUtil.prepareListToQuery(userGroups);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

			String sql = "";
			if (isSystemField(fieldName)) {
				sql = "select distinct MAINDOCS.DOCTYPE, MAINDOCS.DOCID from MAINDOCS where MAINDOCS.DOCID in "
				        + "(select docid from READERS_MAINDOCS where MAINDOCS.DOCID = READERS_MAINDOCS.DOCID and READERS_MAINDOCS.USERNAME in ("
				        + userIDs + ")) and " + "(form= '" + form + "' and EXTRACT(MONTH FROM " + fieldName + ") = " + month + " ) limit " + pageSize
				        + " offset " + offset;
			} else {
				sql = "select distinct MAINDOCS.DOCTYPE, MAINDOCS.DOCID from MAINDOCS where MAINDOCS.DOCID in "
				        + "(select docid from READERS_MAINDOCS where MAINDOCS.DOCID = READERS_MAINDOCS.DOCID and READERS_MAINDOCS.USERNAME in ("
				        + userIDs + ") and MAINDOCS.DOCID in "
				        + "(select docid from CUSTOM_FIELDS where MAINDOCS.DOCID = CUSTOM_FIELDS.DOCID AND CUSTOM_FIELDS.NAME='" + fieldName
				        + "' and EXTRACT(MONTH FROM CUSTOM_FIELDS.VALUEASDATE) = " + month + ")) and " + "form= '" + form + "' limit " + pageSize
				        + " offset " + offset;
			}

			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				vec.add(getDocumentByComplexID(rs.getInt(1), rs.getInt(2)));
			}
			conn.commit();
			s.close();
			rs.close();

		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbPool.returnConnection(conn);
		}
		return vec;
	}

	@Override
	public int insertMainDocument(Document doc, User user) throws DocumentException {
		Date viewDate = doc.getViewDate();
		String fieldsAsText = "AUTHOR, REGDATE, DOCTYPE, LASTUPDATE, DDBID, PARENTDOCDDBID, VIEWTEXT, PARENTDOCID, PARENTDOCTYPE,  " + " FORM, "
		        + DatabaseUtil.getViewTextList("") + ", VIEWNUMBER, VIEWDATE, SIGN, SIGNEDFIELDS, HAS_ATTACHMENT";
		int count_files = 0;
		for (BlobField bfield : doc.blobFieldsMap.values()) {
			count_files += bfield.getFilesCount();
		}
		String valuesAsText = "'" + doc.getAuthorID() + "', '" + sqlDateTimeFormat.format(doc.getRegDate()) + "', " + doc.docType + ", '"
		        + sqlDateTimeFormat.format(doc.getLastUpdate()) + "', '" + doc.getDdbID() + "', '" + doc.getParentDocumentID() + "', '"
		        + doc.getViewText().replace("'", "''") + "', " + doc.parentDocID + ", " + doc.parentDocType + ",'" + doc.form + "', "
		        + DatabaseUtil.getViewTextValues(doc) + ", " + doc.getViewNumber() + ", "
		        + (viewDate != null ? "'" + new Timestamp(viewDate.getTime()) + "'" : "null") + ", '" + doc.getSign() + "', '"
		        + doc.getSignedFields() + "', " + count_files;
		Connection conn = dbPool.getConnection();
		int id = doc.getDocID();

		if (id != 0 && doc.hasField("recID")) {
			fieldsAsText = "DOCID, " + fieldsAsText;
			valuesAsText = id + ", " + valuesAsText;
		}
		try {
			conn.setAutoCommit(false);
			PreparedStatement pst;
			String sql = "";
			try {
				sql = "insert into MAINDOCS(" + fieldsAsText + ") values (" + valuesAsText + ")";
				pst = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
				pst.executeUpdate();
				int key = 0;
				ResultSet rs = pst.getGeneratedKeys();
				while (rs.next()) {
					key = rs.getInt(1);
				}
				for (Field field : doc.fields()) {
					switch (field.getTypeAsDatabaseType()) {
					case TEXT:
						try {
							String sqlStatement = "insert into CUSTOM_FIELDS(DOCID, NAME, VALUE, TYPE)" + "values (" + key + ", '" + field.name
							        + "', '" + field.valueAsText.replace("'", "''").trim() + "', " + field.getTypeAsDatabaseType() + ")";
							pst = conn.prepareStatement(sqlStatement);
							pst.executeUpdate();
							pst.close();
						} catch (SQLException se) {
							if (se.getMessage().contains(" value too long")) {
								throw new DocumentException(DocumentExceptionType.VALUE_TOO_LONG, "field=\"" + field.name + "\", max=2046");
							} else {
								DatabaseUtil.errorPrint(dbID, se);
							}
							conn.rollback();
							return -1;
						}
						break;
					case COMPLEX_OBJECT:
						try {
							/*
							 * String sqlStatement =
							 * "insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASOBJECT, TYPE)"
							 * + "values (" + key + ", '" + field.name + "', '"
							 * //+ field.valueAsObject.getPersistentValue() +
							 * AbstractComplexObject.marshall(field.
							 * valueAsObject.getClass().getName(),
							 * field.valueAsObject) + "', " +
							 * field.getTypeAsDatabaseType() + ")"; pst = conn
							 * .prepareStatement(sqlStatement);
							 * pst.executeUpdate(); pst.close();
							 */

							String sqlStatement = "INSERT INTO CUSTOM_FIELDS(DOCID, NAME, VALUEASOBJECT, TYPE)"
							        + "VALUES (?, ?, XMLPARSE(DOCUMENT ?), ?)";
							pst = conn.prepareStatement(sqlStatement);
							pst.setInt(1, key);
							pst.setString(2, field.name);
							pst.setString(3, AbstractComplexObject.marshall(field.valueAsObject.getClass().getName(), field.valueAsObject));
							pst.setInt(4, field.getTypeAsDatabaseType());
							pst.executeUpdate();
							pst.close();

						} catch (SQLException se) {
							if (se.getMessage().contains(" value too long")) {
								throw new DocumentException(DocumentExceptionType.VALUE_TOO_LONG, "field=\"" + field.name + "\", max=2046");
							} else {
								DatabaseUtil.errorPrint(dbID, se);
							}
							conn.rollback();
							return -1;
						}
						break;
					case TEXTLIST:
						pst = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUE, TYPE)" + " values (" + key + ", '" + field.name
						        + "', ?," + field.getTypeAsDatabaseType() + ")");
						for (String value : field.valuesAsStringList) {
							pst.setString(1, value);
							try {
								pst.executeUpdate();

							} catch (SQLException se) {
								DatabaseUtil.errorPrint(dbID, se);
								return -1;
							}
						}
						pst.close();
						break;
					case NUMBERS:
						pst = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASNUMBER, TYPE)" + "values(" + key + ", '"
						        + field.name + "', " + field.valueAsNumber + ", " + field.getTypeAsDatabaseType() + ")");
						pst.executeUpdate();
						pst.close();
						break;
					case DATETIMES:
						if (field.valueAsDate == null) {
							Database.logger.errorLogEntry("Unable to convert empty date to DB format: " + field.name);
							continue;
						}
						try {
							pst = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASDATE, TYPE)" + "values(" + key + ", '"
							        + field.name + "', '" + Util.convertDateTimeToDerbyFormat(field.valueAsDate) + "', "
							        + field.getTypeAsDatabaseType() + ")");
						} catch (DataConversionException e) {
							Database.logger.errorLogEntry(e + ", field=" + field.name);
							return -1;
						}
						pst.executeUpdate();
						pst.close();
						break;
					case DATE:
						if (field.valueAsDate == null) {
							Database.logger.errorLogEntry("Unable to convert \"null\" to date : " + field.name);
							continue;
						}
						try {
							pst = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASDATE, TYPE)" + "values(" + key + ", '"
							        + field.name + "', '" + Util.convertDateTimeToDerbyFormat(field.valueAsDate) + "', "
							        + field.getTypeAsDatabaseType() + ")");
						} catch (DataConversionException e) {
							Database.logger.errorLogEntry(e + ", field=" + field.name);
							return -1;
						}
						pst.executeUpdate();
						pst.close();
						break;
					case GLOSSARY:
						pst = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASGLOSSARY, TYPE)" + " values (" + key + ", '"
						        + field.name + "', ?," + field.getTypeAsDatabaseType() + ")");
						for (Integer value : field.valuesAsGlossaryData) {
							if (value == 0) {
								pst.setNull(1, Types.INTEGER);
							} else {
								pst.setInt(1, value);
							}

							try {
								pst.executeUpdate();
							} catch (SQLException se) {
								DatabaseUtil.errorPrint(dbID, se);
								return -1;
							}
						}
						pst.close();
						break;
					case RICHTEXT:
						try {
							String sqlStatement = "insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASCLOB, TYPE)" + "values (" + key + ", '" + field.name
							        + "', '" + field.valueAsText.replace("'", "''").trim() + "', " + field.getTypeAsDatabaseType() + ")";
							pst = conn.prepareStatement(sqlStatement);
							pst.executeUpdate();
							pst.close();
						} catch (SQLException se) {
							if (se.getMessage().contains(" value too long")) {
								throw new DocumentException(DocumentExceptionType.VALUE_TOO_LONG, "field=\"" + field.name + "\", max=2046");
							} else {
								DatabaseUtil.errorPrint(dbID, se);
							}
							conn.rollback();
							return -1;
						}
						break;
					case COORDINATION:
						BlockCollection blockCollection = (BlockCollection) field.valueAsObject;
						for (Block block : blockCollection.getBlocks()) {
							int blockID = insertBlock(key, block, conn);
							for (Coordinator coordinator : block.getCoordinators()) {
								insertCoordinator(blockID, coordinator, conn);
							}
						}
						PreparedStatement preparedStatement = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUE, TYPE)"
						        + "values (?, ?, ?, ?)");
						preparedStatement.setInt(1, key);
						preparedStatement.setString(2, field.name);
						preparedStatement.setString(3, String.valueOf(blockCollection.getStatus()));
						preparedStatement.setInt(4, field.getTypeAsDatabaseType());
						preparedStatement.executeUpdate();
						break;
					}
				}
				// insertBlobTables(conn, id, key, doc, baseTable);
				recoverRelations(conn, doc, key);
				insertToAccessTables(conn, baseTable, key, doc);
				conn.commit();
				pst.close();
				if (!doc.hasField("recID")) {
					IUsersActivity ua = getUserActivity();
					ua.postCompose(doc, user);
					ua.postMarkRead(key, doc.docType, user);
				}
				return key;
			} catch (SQLException e) {
				conn.rollback();
				DatabaseUtil.errorPrint(dbID, e);
				return -1;
			}
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
			return -1;
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Override
	public IFilters getFilters() {
		return new Filters(this);
	}

	@Override
	public IForum getForum() {
		return null;

	}

	@Override
	public ISelectFormula getForumSelectFormula(FormulaBlocks preparedBlocks) {
		return new ForumSelectFormula(preparedBlocks);
	}

	@Override
	public void removeUnrelatedAttachments() {
		Connection conn = dbPool.getConnection();
		try {
			Statement s = conn.createStatement();
			s.addBatch("DELETE FROM custom_blobs_maindocs  WHERE (SELECT ROUND(EXTRACT(EPOCH FROM regdate - current_timestamp) / 86400)) < 0 AND docid IS NULL");
			s.addBatch("DELETE FROM custom_blobs_glossary  WHERE (SELECT ROUND(EXTRACT(EPOCH FROM regdate - current_timestamp) / 86400)) < 0 AND docid IS NULL");
			s.addBatch("DELETE FROM custom_blobs_employers WHERE (SELECT ROUND(EXTRACT(EPOCH FROM regdate - current_timestamp) / 86400)) < 0 AND docid IS NULL");
			s.addBatch("DELETE FROM custom_blobs_topics    WHERE (SELECT ROUND(EXTRACT(EPOCH FROM regdate - current_timestamp) / 86400)) < 0 AND docid IS NULL");
			s.addBatch("DELETE FROM custom_blobs_posts     WHERE (SELECT ROUND(EXTRACT(EPOCH FROM regdate - current_timestamp) / 86400)) < 0 AND docid IS NULL");
			s.executeBatch();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Override
	public int updateMainDocument(Document doc, User user) throws DocumentAccessException, DocumentException, ComplexObjectException {
		if (doc.hasEditor(user.getAllUserGroups())) {
			Document oldDoc = this.getMainDocumentByID(doc.getDocID(), user.getAllUserGroups(), user.getUserID());
			Connection conn = dbPool.getConnection();
			try {
				Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
				conn.setAutoCommit(false);
				String viewTextList = "";
				int fieldSize = 0;
				for (int i = 1; i <= DatabaseConst.VIEWTEXT_COUNT; i++) {
					fieldSize = i < 4 ? 256 : 128;
					viewTextList += "VIEWTEXT" + i + " =  '" + cutText(doc.getViewTextList().get(i).replaceAll("'", "''"), fieldSize) + "',";
				}
				if (viewTextList.endsWith(",")) {
					viewTextList = viewTextList.substring(0, viewTextList.length() - 1);
				}
				int count_files = 0;
				for (BlobField bfield : doc.blobFieldsMap.values()) {
					count_files += bfield.getFilesCount();
				}
				String mainDocUpd = "update MAINDOCS set LASTUPDATE = '" + sqlDateTimeFormat.format(doc.getLastUpdate()) + "', VIEWTEXT='"
				        + doc.getViewText().replace("'", "''") + "', DDBID='" + doc.getDdbID() + "', PARENTDOCDDBID='" + doc.getParentDocumentID()
				        + "', DEFAULTRULEID='" + doc.getDefaultRuleID() + "', " + viewTextList + ", VIEWNUMBER = " + doc.getViewNumber()
				        + ", VIEWDATE = '" + new Timestamp(doc.getViewDate().getTime()) + "'" + ", SIGN = '" + doc.getSign() + "', SIGNEDFIELDS = '"
				        + doc.getSignedFields() + "' " + ", PARENTDOCID = " + doc.parentDocID + ", PARENTDOCTYPE = " + doc.parentDocType
				        + ", HAS_ATTACHMENT = " + count_files + " where DOCID = " + doc.getDocID();
				s.executeUpdate(mainDocUpd);
				for (Field field : doc.fields()) {

					String upCustomFields = "";
					switch (field.getTypeAsDatabaseType()) {
					case TEXT:
						upCustomFields = "update custom_fields set value = '" + field.valueAsText.replace("'", "''").trim() + "', type = "
						        + field.getTypeAsDatabaseType() + "where docid =  " + doc.getDocID() + "and name = '" + field.name + "';"
						        + " insert into custom_fields (docid, name, value, type) " + " select " + doc.getDocID() + ", '" + field.name
						        + "', '" + field.valueAsText.replace("'", "''").trim() + "', " + field.getTypeAsDatabaseType()
						        + " where 1 not in (select 1 from custom_fields where docid = " + doc.getDocID() + " and name = '" + field.name
						        + "')";
						break;
					case RICHTEXT:
						upCustomFields = "update custom_fields set valueasclob = '" + field.valueAsText.replace("'", "''").trim() + "', type = "
						        + field.getTypeAsDatabaseType() + "where docid =  " + doc.getDocID() + "and name = '" + field.name + "';"
						        + " insert into custom_fields (docid, name, valueasclob, type) " + " select " + doc.getDocID() + ", '" + field.name
						        + "', '" + field.valueAsText.replace("'", "''").trim() + "', " + field.getTypeAsDatabaseType()
						        + " where 1 not in (select 1 from custom_fields where docid = " + doc.getDocID() + " and name = '" + field.name
						        + "')";
						break;
					case COMPLEX_OBJECT:
						PreparedStatement pdst = conn
						        .prepareStatement(" update custom_fields set valueasobject = XMLPARSE(DOCUMENT ?), type = ? where docid =  ? and name = ?;"
						                + " insert into custom_fields (docid, name, valueasobject, type) "
						                + " select ?, ?, XMLPARSE(DOCUMENT ?), ? where 1 not in (select 1 from custom_fields where docid = ? and name = ?)");
						pdst.setString(1, AbstractComplexObject.marshall(field.valueAsObject.getClass().getName(), field.valueAsObject));
						pdst.setInt(2, field.getTypeAsDatabaseType());
						pdst.setInt(3, doc.getDocID());
						pdst.setString(4, field.name);
						pdst.setInt(5, doc.getDocID());
						pdst.setString(6, field.name);
						pdst.setString(7, AbstractComplexObject.marshall(field.valueAsObject.getClass().getName(), field.valueAsObject));
						pdst.setInt(8, field.getTypeAsDatabaseType());
						pdst.setInt(9, doc.getDocID());
						pdst.setString(10, field.name);
						try {
							pdst.executeUpdate();
						} catch (SQLException e) {
							DatabaseUtil.errorPrint(dbID, e);
							return -1;
						}
						break;
					case TEXTLIST:
						PreparedStatement ps = conn.prepareStatement("DELETE FROM CUSTOM_FIELDS" + " WHERE DOCID=" + doc.getDocID() + " and NAME='"
						        + field.name + "'");
						ps.executeUpdate();
						ps = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUE, TYPE)" + " values (" + doc.getDocID() + ", '"
						        + field.name + "', ?," + field.getTypeAsDatabaseType() + ")");
						for (String value : field.valuesAsStringList) {
							ps.setString(1, value);
							ps.executeUpdate();
						}
						break;
					case NUMBERS:
						upCustomFields = "update custom_fields set valueasnumber = " + field.valueAsNumber + ", type = "
						        + field.getTypeAsDatabaseType() + "where docid =  " + doc.getDocID() + "and name = '" + field.name + "';"
						        + " insert into custom_fields (docid, name, valueasnumber, type) " + " select " + doc.getDocID() + ", '" + field.name
						        + "', " + field.valueAsNumber + ", " + field.getTypeAsDatabaseType()
						        + " where 1 not in (select 1 from custom_fields where docid = " + doc.getDocID() + " and name = '" + field.name
						        + "')";
						break;
					case DATETIMES:
						if (field.valueAsDate == null) {
							Database.logger.errorLogEntry("Unable to convert empty date to DB format: " + field.name);
							continue;
						}
						try {
							upCustomFields = "update custom_fields set valueasdate = '" + Util.convertDateTimeToDerbyFormat(field.valueAsDate)
							        + "', type = " + field.getTypeAsDatabaseType() + " where docid =  " + doc.getDocID() + " and name = '"
							        + field.name + "';" + " insert into custom_fields (docid, name, valueasdate, type) " + " select "
							        + doc.getDocID() + ", '" + field.name + "', '" + Util.convertDateTimeToDerbyFormat(field.valueAsDate) + "', "
							        + field.getTypeAsDatabaseType() + " where 1 not in (select 1 from custom_fields where docid = " + doc.getDocID()
							        + " and name = '" + field.name + "')";
						} catch (DataConversionException e) {
							Database.logger.errorLogEntry(e + ", field=" + field.name);
							return -1;
						}
						break;
					case GLOSSARY:
						PreparedStatement pst = conn.prepareStatement("DELETE FROM CUSTOM_FIELDS WHERE DOCID = " + doc.getDocID() + " and NAME = '"
						        + field.name + "'");
						pst.executeUpdate();
						pst = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASGLOSSARY, TYPE)" + " values (" + doc.getDocID()
						        + ", '" + field.name + "', ?," + field.getTypeAsDatabaseType() + ")");
						for (Integer value : field.valuesAsGlossaryData) {
							if (value == 0) {
								pst.setNull(1, Types.INTEGER);
							} else {
								pst.setInt(1, value);
							}

							try {
								pst.executeUpdate();
							} catch (SQLException se) {
								DatabaseUtil.errorPrint(dbID, se);
								return -1;
							}
						}
						break;
					case COORDINATION:
						PreparedStatement preparedStatement = conn.prepareStatement("delete from coordblocks where docid = ? ");
						preparedStatement.setInt(1, doc.getDocID());
						preparedStatement.executeUpdate();

						BlockCollection blockCollection = (BlockCollection) field.valueAsObject;
						for (Block block : blockCollection.getBlocks()) {
							int blockID = insertBlock(doc.getDocID(), block, conn);
							for (Coordinator coordinator : block.getCoordinators()) {
								int coordID = insertCoordinator(blockID, coordinator, conn);
								recoverCommAttachRelations(conn, coordID, coordinator.getAttachID());
								/*
								 * sql =
								 * "UPDATE CUSTOM_BLOBS_MAINDOCS SET DOCID = ? WHERE docid = ?"
								 * + (ids.size() > 0 ? " and id not in (" +
								 * StringUtils.join(ids, ",") + ") " : ""); pst
								 * = conn.prepareStatement(sql); pst.setInt(1,
								 * 0); pst.setInt(2, key); pst.executeUpdate();
								 * conn.commit(); pst.close();
								 */
							}
						}

						preparedStatement = conn.prepareStatement("delete from custom_fields where name = ? and docid = ? and type = ?");
						preparedStatement.setString(1, field.name);
						preparedStatement.setInt(2, doc.getDocID());
						preparedStatement.setInt(3, field.getTypeAsDatabaseType());
						preparedStatement.executeUpdate();

						preparedStatement = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUE, TYPE)" + "values (?, ?, ?, ?)");
						preparedStatement.setInt(1, doc.getDocID());
						preparedStatement.setString(2, field.name);
						preparedStatement.setString(3, String.valueOf(blockCollection.getStatus()));
						preparedStatement.setInt(4, field.getTypeAsDatabaseType());
						preparedStatement.executeUpdate();

						break;
					}
					if (upCustomFields != "") {
						s.executeUpdate(upCustomFields);
					}
				}
				conn.commit();
				// updateBlobTables(conn, doc, baseTable);
				recoverRelations(conn, doc, doc.getDocID());
				updateAccessTables(conn, doc, baseTable);
				conn.commit();
				s.close();

				IUsersActivity ua = getUserActivity();
				ua.postModify(oldDoc, doc, user);

				return doc.getDocID();
			} catch (SQLException e) {
				DatabaseUtil.errorPrint(dbID, e);
				return -1;
			} finally {
				dbPool.returnConnection(conn);
			}
		} else {
			throw new DocumentAccessException(ExceptionType.DOCUMENT_WRITE_ACCESS_RESTRICTED, user.getUserID());
		}
	}

	@Override
	public DocumentCollection getDescendants(int docID, int docType, SortByBlock sortBlock, int level, Set<String> complexUserID,
	        String absoluteUserID, String responseQueryCondition) {
		int col = 0;
		DocumentCollection documents = new DocumentCollection();
		StringBuffer xmlContent = new StringBuffer(10000);
		String value = "";
		if (docID != 0 && docType != DOCTYPE_UNKNOWN) {
			Connection conn = dbPool.getConnection();
			try {
				conn.setAutoCommit(false);
				Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

				String orderSQL = "ORDER BY " + Const.DEFAULT_SORT_COLUMN + " " + Const.DEFAULT_SORT_ORDER;
				if (sortBlock != null) {
					switch (sortBlock.fieldName) {
					case "regdate":
					case "viewdate":
					case "form":
					case "viewtext":
						orderSQL = "ORDER BY " + sortBlock.fieldName + " " + sortBlock.order;
						break;
					}
				}

				String sql = "SELECT distinct resp.docid, resp.ddbid, doctype, form, resp.viewtext, "
				        + DatabaseUtil.getViewTextList("resp")
				        + ", resp.viewnumber, resp.viewdate, regdate, has_attachment, resp.allcontrol, ctype"
				        + " FROM TASKS as resp"
				        + " LEFT JOIN"
				        + " (SELECT g.viewtext AS ctype, t.docid AS taskid, g.docid FROM GLOSSARY AS g, TASKS AS t WHERE g.form = 'controltype' AND g.docid = t.controltype) as temp"
				        + " ON resp.docid = taskid, READERS_TASKS r WHERE parentdocid = " + docID + " AND parentdoctype = " + docType
				        + " AND resp.docid = r.docid AND r.username IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
				        + (!"".equalsIgnoreCase(responseQueryCondition) ? " and " + responseQueryCondition : "")
				        + " UNION SELECT distinct resp.docid, resp.ddbid, doctype, form, viewtext, " + DatabaseUtil.getViewTextList("resp")
				        + ", resp.viewnumber, viewdate, regdate, has_attachment, -1 as allcontrol, '' as ctype"
				        + " FROM EXECUTIONS as resp, READERS_EXECUTIONS r WHERE parentdocid = " + docID + " AND parentdoctype = " + docType
				        + " AND resp.docid = r.docid AND r.username IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
				        + (!"".equalsIgnoreCase(responseQueryCondition) ? " and " + responseQueryCondition : "")
				        + " UNION SELECT distinct resp.docid, resp.ddbid, resp.doctype, resp.form, resp.viewtext, "
				        + DatabaseUtil.getViewTextList("resp")
				        + ", resp.viewnumber, resp.viewdate, resp.regdate, resp.has_attachment, c.valueasnumber AS allcontrol, ctype"
				        + " FROM CUSTOM_FIELDS as c " + " RIGHT JOIN MAINDOCS resp" + " ON resp.docid = c.docid AND c.name = 'allcontrol'"
				        + " LEFT JOIN" + " (SELECT g.viewtext AS ctype, cf.docid AS cfdocid FROM GLOSSARY AS g, CUSTOM_FIELDS AS cf, MAINDOCS AS m"
				        + " WHERE g.docid = cf.valueasnumber AND cf.docid = m.docid AND cf.name = 'controltype') as temp1"
				        + " ON resp.docid = cfdocid," + " READERS_MAINDOCS r " + " WHERE parentdocid = " + docID + " AND parentdoctype = " + docType
				        + " AND resp.docid = r.docid AND r.username IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
				        + " and resp.form != 'discussion' " + (!"".equalsIgnoreCase(responseQueryCondition) ? " and " + responseQueryCondition : "")
				        + orderSQL;

				ResultSet rs = statement.executeQuery(sql);

				while (rs.next()) {
					value = "";
					int respDocID = rs.getInt("DOCID");
					int repsDocType = rs.getInt("DOCTYPE");
					int allControl = rs.getInt("ALLCONTROL");
					String controlType = rs.getString("CTYPE");
					String ddbid = rs.getString("DDBID");
					String form = rs.getString("FORM");
					xmlContent.append("<entry isread=\"" + usersActivity.isRead(conn, respDocID, repsDocType, absoluteUserID) + "\" hasattach=\""
					        + Integer.toString(rs.getInt("HAS_ATTACHMENT")) + "\" doctype=\"" + repsDocType + "\"  docid=\"" + respDocID + "\" id=\""
					        + ddbid + "\" form=\"" + form + "\" url=\"Provider?type=edit&amp;element=" + resolveElement(form) + "&amp;id=" + form
					        + "&amp;docid=" + ddbid + "\" " + (!form.equalsIgnoreCase("KI") ? " allcontrol =\"" + allControl + "\"" : "")
					        + (!form.equalsIgnoreCase("KI") ? " controltype =\"" + controlType + "\"" : ""));
					col++;

					int l = level + 1;
					DocumentCollection responses = getDescendants(respDocID, repsDocType, null, l, complexUserID, absoluteUserID,
					        responseQueryCondition);
					if (responses.count > 0) {
						xmlContent.append(" hasresponse=\"true\" >");
						value += responses.xmlContent;
					} else {
						xmlContent.append(" >");
					}

					xmlContent.append(value += getViewContent(rs) + "</entry>");
					col++;

				}

				documents.xmlContent.append(xmlContent);
				rs.close();
				statement.close();
				conn.commit();
				documents.count = col;
			} catch (SQLException e) {
				DatabaseUtil.errorPrint(dbID, e);
			} finally {
				dbPool.returnConnection(conn);
			}
		}
		return documents;
	}

	@Override
	public DocumentCollection getDescendants(int docID, int docType, SortByBlock sortBlock, int level, Set<String> complexUserID,
	        String absoluteUserID) {
		int col = 0;
		DocumentCollection documents = new DocumentCollection();
		StringBuffer xmlContent = new StringBuffer(10000);
		String value = "";
		if (docID != 0 && docType != DOCTYPE_UNKNOWN) {
			Connection conn = dbPool.getConnection();
			try {
				conn.setAutoCommit(false);
				Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

				String orderSQL = "ORDER BY " + Const.DEFAULT_SORT_COLUMN + " " + Const.DEFAULT_SORT_ORDER;
				if (sortBlock != null) {
					switch (sortBlock.fieldName) {
					case "regdate":
					case "viewdate":
					case "form":
					case "viewtext":
						orderSQL = "ORDER BY " + sortBlock.fieldName + " " + sortBlock.order;
						break;
					}
				}

				String sql = "SELECT distinct resp.docid, resp.ddbid, doctype, form, resp.viewtext, "
				        + DatabaseUtil.getViewTextList("resp")
				        + ", resp.viewnumber, resp.viewdate, regdate, has_attachment, resp.allcontrol, ctype"
				        + " FROM TASKS as resp"
				        + " LEFT JOIN"
				        + " (SELECT g.viewtext AS ctype, t.docid AS taskid, g.docid FROM GLOSSARY AS g, TASKS AS t WHERE g.form = 'controltype' AND g.docid = t.controltype) as temp"
				        + " ON resp.docid = taskid, READERS_TASKS r WHERE parentdocid = " + docID + " AND parentdoctype = " + docType
				        + " AND resp.docid = r.docid AND r.username IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
				        + " UNION SELECT distinct resp.docid, resp.ddbid, doctype, form, viewtext, " + DatabaseUtil.getViewTextList("resp")
				        + ", resp.viewnumber, viewdate, regdate, has_attachment, -1 as allcontrol, '' as ctype"
				        + " FROM EXECUTIONS as resp, READERS_EXECUTIONS r WHERE parentdocid = " + docID + " AND parentdoctype = " + docType
				        + " AND resp.docid = r.docid AND r.username IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
				        + " UNION SELECT distinct resp.docid, resp.ddbid, resp.doctype, resp.form, resp.viewtext, "
				        + DatabaseUtil.getViewTextList("resp")
				        + ", resp.viewnumber, resp.viewdate, resp.regdate, resp.has_attachment, c.valueasnumber AS allcontrol, ctype"
				        + " FROM CUSTOM_FIELDS as c " + " RIGHT JOIN MAINDOCS resp" + " ON resp.docid = c.docid AND c.name = 'allcontrol'"
				        + " LEFT JOIN" + " (SELECT g.viewtext AS ctype, cf.docid AS cfdocid FROM GLOSSARY AS g, CUSTOM_FIELDS AS cf, MAINDOCS AS m"
				        + " WHERE g.docid = cf.valueasnumber AND cf.docid = m.docid AND cf.name = 'controltype') as temp1"
				        + " ON resp.docid = cfdocid," + " READERS_MAINDOCS r " + " WHERE parentdocid = " + docID + " AND parentdoctype = " + docType
				        + " AND resp.docid = r.docid AND r.username IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
				        + " and resp.form != 'discussion' " + orderSQL;

				ResultSet rs = statement.executeQuery(sql);

				while (rs.next()) {
					value = "";
					int respDocID = rs.getInt("DOCID");
					int repsDocType = rs.getInt("DOCTYPE");
					int allControl = rs.getInt("ALLCONTROL");
					String controlType = rs.getString("CTYPE");
					String ddbid = rs.getString("DDBID");
					String form = rs.getString("FORM");
					xmlContent.append("<entry isread=\"" + usersActivity.isRead(conn, respDocID, repsDocType, absoluteUserID) + "\" hasattach=\""
					        + Integer.toString(rs.getInt("HAS_ATTACHMENT")) + "\" doctype=\"" + repsDocType + "\"  docid=\"" + respDocID + "\" id=\""
					        + ddbid + "\" form=\"" + form + "\" url=\"Provider?type=edit&amp;element=" + resolveElement(form) + "&amp;id=" + form
					        + "&amp;docid=" + ddbid + "\" " + (!form.equalsIgnoreCase("KI") ? " allcontrol =\"" + allControl + "\"" : "")
					        + (!form.equalsIgnoreCase("KI") ? " controltype =\"" + controlType + "\"" : ""));
					col++;

					int l = level + 1;
					DocumentCollection responses = getDescendants(respDocID, repsDocType, null, l, complexUserID, absoluteUserID);
					if (responses.count > 0) {
						xmlContent.append(" hasresponse=\"true\" >");
						value += responses.xmlContent;
					} else {
						xmlContent.append(" >");
					}

					xmlContent.append(value += getViewContent(rs) + "</entry>");
					col++;

				}

				documents.xmlContent.append(xmlContent);
				rs.close();
				statement.close();
				conn.commit();
				documents.count = col;
			} catch (SQLException e) {
				DatabaseUtil.errorPrint(dbID, e);
			} finally {
				dbPool.returnConnection(conn);
			}
		}
		return documents;
	}

	@Override
	public ArrayList<BaseDocument> getDescendantsArray(int docID, int docType, DocID[] toExpand, int level, Set<String> complexUserID,
	        String absoluteUserID) throws DocumentException, ComplexObjectException {
		ArrayList<BaseDocument> documents = new ArrayList<BaseDocument>();
		if (docID != 0 && docType != DOCTYPE_UNKNOWN) {
			Connection conn = dbPool.getConnection();
			try {
				conn.setAutoCommit(false);
				Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				String sql = "SELECT distinct resp.docid, doctype, form, resp.viewtext, regdate, has_attachment, resp.allcontrol, ctype"
				        + " FROM TASKS as resp"
				        + " LEFT JOIN"
				        + " (SELECT g.viewtext AS ctype, t.docid AS taskid, g.docid FROM GLOSSARY AS g, TASKS AS t WHERE g.form = 'controltype' AND g.docid = t.controltype) as temp"
				        + " ON resp.docid = taskid, READERS_TASKS r WHERE parentdocid = "
				        + docID
				        + " AND parentdoctype = "
				        + docType
				        + " AND resp.docid = r.docid AND r.username IN ("
				        + DatabaseUtil.prepareListToQuery(complexUserID)
				        + ")"
				        + " UNION SELECT distinct resp.docid, doctype, form, viewtext, regdate, has_attachment, -1 as allcontrol, '' as ctype"
				        + " FROM EXECUTIONS as resp, READERS_EXECUTIONS r WHERE parentdocid = "
				        + docID
				        + " AND parentdoctype = "
				        + docType
				        + " AND resp.docid = r.docid AND r.username IN ("
				        + DatabaseUtil.prepareListToQuery(complexUserID)
				        + ")"
				        + " UNION SELECT distinct resp.docid, resp.doctype, resp.form, resp.viewtext, resp.regdate, resp.has_attachment, c.valueasnumber AS allcontrol, ctype"
				        + " FROM CUSTOM_FIELDS as c "
				        + " RIGHT JOIN MAINDOCS resp"
				        + " ON resp.docid = c.docid AND c.name = 'allcontrol'"
				        + " LEFT JOIN"
				        + " (SELECT g.viewtext AS ctype, cf.docid AS cfdocid FROM GLOSSARY AS g, CUSTOM_FIELDS AS cf, MAINDOCS AS m"
				        + " WHERE g.docid = cf.valueasnumber AND cf.docid = m.docid AND cf.name = 'controltype') as temp1"
				        + " ON resp.docid = cfdocid,"
				        + " READERS_MAINDOCS r "
				        + " WHERE parentdocid = "
				        + docID
				        + " AND parentdoctype = "
				        + docType
				        + " AND resp.docid = r.docid AND r.username IN ("
				        + DatabaseUtil.prepareListToQuery(complexUserID)
				        + ")"
				        + " and resp.form != 'discussion' " + " ORDER BY REGDATE";

				ResultSet rs = statement.executeQuery(sql);

				while (rs.next()) {
					BaseDocument doc = null;
					int respDocID = rs.getInt("DOCID");
					int respDocType = rs.getInt("DOCTYPE");

					switch (respDocType) {
					case DOCTYPE_MAIN:
						doc = getMainDocumentByID(respDocID, complexUserID, absoluteUserID);
						break;
					case DOCTYPE_TASK:
						doc = getTasks().getTaskByID(respDocID, complexUserID, absoluteUserID);
						break;
					case DOCTYPE_EXECUTION:
						doc = getExecutions().getExecutionByID(respDocID, complexUserID, absoluteUserID);
						break;
					case DOCTYPE_PROJECT:
						doc = getProjects().getProjectByID(respDocID, complexUserID, absoluteUserID);
						break;
					}

					if (doc != null) {
						documents.add(doc);
					}

					int l = level + 1;

					ArrayList<BaseDocument> responses = getDescendantsArray(respDocID, respDocType, null, l, complexUserID, absoluteUserID);
					if (responses.size() > 0) {
						documents.addAll(responses);
					}
				}
				rs.close();
				statement.close();
				conn.commit();
			} catch (SQLException e) {
				DatabaseUtil.errorPrint(dbID, e);
			} catch (DocumentAccessException e) {
				DatabaseUtil.errorPrint(dbID, e);
			} finally {
				dbPool.returnConnection(conn);
			}
		}
		return documents;
	}

	@Override
	public ITasks getTasks() {
		return new kz.flabs.dataengine.postgresql.TaskOnDatabase(this);
	}

	@Override
	public IExecutions getExecutions() {
		return new kz.flabs.dataengine.postgresql.ExecutionsOnDatabase(this);
	}

	@Override
	public IProjects getProjects() {
		return null;

	}

	@Override
	public void insertBlobTables(Connection conn, int id, int key, Document doc, String tableSuffix) throws SQLException, IOException {

		if (id != 0 && !doc.hasField("recID")) {
			PreparedStatement s0 = conn.prepareStatement("SELECT * FROM CUSTOM_BLOBS_" + tableSuffix + " WHERE DOCID = " + id);
			ResultSet rs0 = s0.executeQuery();
			while (rs0.next()) {
				PreparedStatement s1 = conn.prepareStatement("INSERT INTO CUSTOM_BLOBS_" + tableSuffix
				        + " (DOCID, NAME, ORIGINALNAME, CHECKSUM, COMMENT, VALUE_OID)values(?, ?, ?, ?, ?, ?)");
				s1.setInt(1, key);
				s1.setString(2, rs0.getString("NAME"));
				s1.setString(3, rs0.getString("ORIGINALNAME"));
				s1.setString(4, rs0.getString("CHECKSUM"));
				s1.setString(5, rs0.getString("COMMENT"));
				s1.setLong(6, rs0.getLong("VALUE_OID"));
				s1.executeUpdate();
				s1.close();
			}
			rs0.close();
			s0.close();

		} else {
			for (Entry<String, BlobField> blob : doc.blobFieldsMap.entrySet()) {
				PreparedStatement ps = conn.prepareStatement("INSERT INTO CUSTOM_BLOBS_" + tableSuffix
				        + " (DOCID, NAME, ORIGINALNAME, CHECKSUM, COMMENT, VALUE_OID)values(?, ?, ?, ?, ?, ?)");
				BlobField bf = blob.getValue();
				for (BlobFile bfile : bf.getFiles()) {
					if (bfile != null) {
						LargeObjectManager lobj = ((org.postgresql.PGConnection) ((DelegatingConnection) conn).getInnermostDelegate())
						        .getLargeObjectAPI();
						long oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
						LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);
						ps.setInt(1, key);
						ps.setString(2, bf.name);
						ps.setString(3, bfile.originalName);
						ps.setString(4, bfile.checkHash);
						ps.setString(5, bfile.comment);
						if (!bfile.path.equalsIgnoreCase("")) {
							File file = new File(bfile.path);
							FileInputStream fin = new FileInputStream(file);
							byte buf[] = new byte[1048576];
							int s, tl = 0;
							while ((s = fin.read(buf, 0, 1048576)) > 0) {
								obj.write(buf, 0, s);
								tl += s;
							}
							obj.close();
							ps.setLong(6, oid);
							ps.executeUpdate();
							fin.close();
							Environment.fileToDelete.add(bfile.path);
						} else if (bfile.getContent() != null) {
							obj.write(bfile.getContent());
							obj.close();
							ps.setLong(6, oid);
							ps.executeUpdate();
						}
					}
				}
				ps.close();
			}
		}
	}

	@Override
	public void updateBlobTables(Connection conn, Document doc, String tableSuffix) throws SQLException, IOException {

		Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		ResultSet blobs = s.executeQuery("SELECT ID, DOCID, NAME, ORIGINALNAME, COMMENT, CHECKSUM, VALUE_OID " + "FROM CUSTOM_BLOBS_" + tableSuffix
		        + " WHERE DOCID = " + doc.getDocID());
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
		}
		/* now add files that are absent in database */
		for (Entry<String, BlobField> blob : doc.blobFieldsMap.entrySet()) {
			PreparedStatement ps = conn.prepareStatement("SELECT ID, DOCID, NAME, ORIGINALNAME, COMMENT, CHECKSUM, VALUE_OID " + "FROM CUSTOM_BLOBS_"
			        + tableSuffix + " WHERE DOCID = ? AND NAME = ? AND CHECKSUM = ?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			for (BlobFile bfile : blob.getValue().getFiles()) {
				LargeObjectManager lobj = ((org.postgresql.PGConnection) ((DelegatingConnection) conn).getInnermostDelegate()).getLargeObjectAPI();
				long oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
				LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);
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
					blobs.updateString("COMMENT", bfile.comment);
					File file = new File(bfile.path);
					InputStream is = new FileInputStream(file);
					byte buf[] = new byte[1048576];
					int sl, tl = 0;
					while ((sl = is.read(buf, 0, 1048576)) > 0) {
						obj.write(buf, 0, sl);
						tl += sl;
					}
					obj.close();
					blobs.updateLong("VALUE_OID", oid);
					blobs.insertRow();
					is.close();
				}
				Environment.fileToDelete.add(bfile.path);
			}
			ps.close();
		}
	}

	// TODO table name
	public void recoverRelations(Connection conn, BaseDocument doc, int key) {
		try {
			String sql;
			PreparedStatement pst;
			ArrayList<String> ids = new ArrayList<>();
			for (BlobField field : doc.blobFieldsMap.values()) {
				for (BlobFile f : field.getFiles()) {
					if (f.id == null) {
						pst = conn
						        .prepareStatement(
						                "INSERT INTO CUSTOM_BLOBS_MAINDOCS (DOCID, NAME, ORIGINALNAME, CHECKSUM, COMMENT, VALUE_OID, REGDATE)VALUES(?, ?, ?, ?, ?, ?, ?)",
						                PreparedStatement.RETURN_GENERATED_KEYS);
						String hash = Util.getHexHash(f.path);
						pst.setInt(1, key);
						pst.setString(2, "rtfcontent");
						pst.setString(3, FilenameUtils.getName(f.originalName));
						pst.setString(4, hash);
						pst.setString(5, "");

						LargeObjectManager lobj = ((org.postgresql.PGConnection) ((DelegatingConnection) conn).getInnermostDelegate())
						        .getLargeObjectAPI();
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
						pst.setTimestamp(7, new Timestamp(new Date().getTime()));
						pst.executeUpdate();
						conn.commit();
						int att_id = 0;
						Environment.fileToDelete.add(f.path);
						ResultSet rs = pst.getGeneratedKeys();
						while (rs.next()) {
							ids.add(String.valueOf(rs.getInt(1)));
						}
						pst.close();
					} else if (f.id != null && doc.getDocID() != 0 && doc.getDocID() != key) {
						sql = "INSERT INTO CUSTOM_BLOBS_MAINDOCS (DOCID, NAME, ORIGINALNAME, CHECKSUM, COMMENT, VALUE_OID, REGDATE) SELECT " + key
						        + ", NAME, ORIGINALNAME, CHECKSUM, COMMENT, VALUE_OID, REGDATE FROM CUSTOM_BLOBS_MAINDOCS WHERE DOCID = ? AND ID = ?";
						pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
						pst.setInt(1, doc.getDocID());
						pst.setInt(2, Integer.valueOf(f.id));
						pst.executeUpdate();
						ResultSet rs = pst.getGeneratedKeys();
						if (rs.next()) {
							ids.add(String.valueOf(rs.getInt(1)));
						}
					} else {
						ids.add(f.id);
						sql = "UPDATE CUSTOM_BLOBS_MAINDOCS SET DOCID = ?, COMMENT = ? WHERE id = ?";
						pst = conn.prepareStatement(sql);
						pst.setInt(1, key);
						pst.setString(2, f.getComment());
						pst.setInt(3, Integer.valueOf(f.id));
						pst.executeUpdate();
					}
				}
			}
			conn.commit();
			sql = "UPDATE CUSTOM_BLOBS_MAINDOCS SET DOCID = ? WHERE docid = ?"
			        + (ids.size() > 0 ? " and id not in (" + StringUtils.join(ids, ",") + ") " : "");
			pst = conn.prepareStatement(sql);
			pst.setInt(1, 0);
			pst.setInt(2, key);
			pst.executeUpdate();
			conn.commit();
			pst.close();
		} catch (Exception e) {
			DatabaseUtil.errorPrint(this.dbID, e);
			try {
				conn.rollback();
			} catch (SQLException e1) {
				DatabaseUtil.errorPrint(this.dbID, e);
			}
		}
	}

	@Override
	public ArrayList<UploadedFile> insertBlobTables(List<FileItem> fileItems) throws SQLException, IOException {
		IDBConnectionPool pool = this.dbPool;
		Connection conn = null;
		ArrayList<UploadedFile> files = new ArrayList<>();
		String tableSuffix;
		try {
			for (FileItem item : fileItems) {
				if (item != null && item.getName() != null && !"".equalsIgnoreCase(item.getName())) {
					/*
					 * switch (item.getFieldName().toLowerCase(Locale.ENGLISH))
					 * { case "decision_comment_uploadfield": tableSuffix =
					 * "COORDINATORS"; break; case "employer_uploadfield":
					 * tableSuffix = "EMPLOYERS"; pool = this.structDbPool;
					 * break; default: tableSuffix = "MAINDOCS"; break; } conn =
					 * pool.getConnection(); PreparedStatement ps =
					 * conn.prepareStatement( "INSERT INTO CUSTOM_BLOBS_" +
					 * tableSuffix +
					 * " (DOCID, NAME, ORIGINALNAME, CHECKSUM, COMMENT, VALUE_OID, REGDATE)values(?, ?, ?, ?, ?, ?, ?)"
					 * , PreparedStatement.RETURN_GENERATED_KEYS);
					 */
					String hash = Util.getHexHash(item.getInputStream());
					/*
					 * ps.setInt(1, 1); ps.setString(2, "rtfcontent");
					 * ps.setString(3, FilenameUtils.getName(item.getName()));
					 * ps.setString(4, hash); ps.setString(5, "");
					 * LargeObjectManager lobj = ((org.postgresql.PGConnection)
					 * ((DelegatingConnection) conn)
					 * .getInnermostDelegate()).getLargeObjectAPI(); long oid =
					 * lobj.createLO(LargeObjectManager.READ |
					 * LargeObjectManager.WRITE); LargeObject obj =
					 * lobj.open(oid, LargeObjectManager.WRITE);
					 * obj.write(item.get()); obj.close(); ps.setLong(6, oid);
					 * ps.setTimestamp(7, new Timestamp(new Date().getTime()));
					 * ps.executeUpdate(); conn.commit();
					 */
					int key = 0;
					/*
					 * ResultSet rs = ps.getGeneratedKeys(); while (rs.next()) {
					 * key = rs.getInt(1); }
					 */
					String name = item.getName();
					long filelen = item.getSize();
					files.add(new UploadedFile(name, hash, filelen, item.getContentType(), String.valueOf(key)));
					// ps.close();
				}
			}
		} catch (Exception e) {
			DatabaseUtil.errorPrint(dbID, e);
			AppEnv.logger.errorLogEntry(e);
		} finally {
			pool.returnConnection(conn);
		}
		return files;
	}

	@Override
	public String getDocumentAttach(int attachID, int docType, String fieldName, String fileName) {
		Connection conn = dbPool.getConnection();
		String tableName = "";
		switch (docType) {
		case DOCTYPE_MAIN:
			tableName = "CUSTOM_BLOBS_MAINDOCS";
			break;
		case DOCTYPE_EMPLOYER:
			tableName = "CUSTOM_BLOBS_EMPLOYERS";
			break;
		case DOCTYPE_GLOSSARY:
			tableName = "CUSTOM_BLOBS_GLOSSARY";
			break;
		case DOCTYPE_COORD_COMMENT:
			tableName = "CUSTOM_BLOBS_COORDINATORS";
			break;
		default:
			tableName = "CUSTOM_BLOBS_MAINDOCS";
			break;
		}
		try {
			conn.setAutoCommit(false);
			PreparedStatement ps = conn.prepareStatement("select * from " + tableName + " where id = ?");
			ps.setInt(1, attachID);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				do {
					String originalName = rs.getString("ORIGINALNAME");
					if (originalName.equals(fileName)) {
						LargeObjectManager lobj = ((org.postgresql.PGConnection) ((DelegatingConnection) conn).getInnermostDelegate())
						        .getLargeObjectAPI();
						long oid = rs.getLong("VALUE_OID");
						LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);
						InputStream is = obj.getInputStream();
						String fullPath = Util.getFileName(originalName, Environment.tmpDir + "/" + Util.generateRandom() + "/");
						FileOutputStream out = new FileOutputStream(fullPath);
						byte[] b = new byte[1048576];
						int len = 0;
						while ((len = is.read(b)) > 0) {
							out.write(b, 0, len);
						}
						out.close();
						rs.close();
						ps.close();
						return fullPath;
					}
				} while (rs.next() && fieldName.equals(rs.getString("NAME")));
			}
			rs.close();
			ps.close();
			conn.commit();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			dbPool.returnConnection(conn);
		}
		return "";
	}

	@Override
	public String getDocumentAttach(int docID, int docType, Set<String> complexUserID, String fieldName, String fileName) {
		String tableName = "";
		switch (docType) {
		case DOCTYPE_MAIN:
			tableName = "MAINDOCS";
			break;
		case DOCTYPE_TASK:
			tableName = "TASKS";
			break;
		case DOCTYPE_EXECUTION:
			tableName = "EXECUTIONS";
			break;
		case DOCTYPE_PROJECT:
			tableName = "PROJECTS";
			break;
		case DOCTYPE_EMPLOYER:
			tableName = "EMPLOYERS";
			break;
		case DOCTYPE_GLOSSARY:
			tableName = "GLOSSARY";
			break;
		}
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			ResultSet rs = null;
			try {
				if (docType != DOCTYPE_GLOSSARY) {
					String sql = "select * from " + tableName + ", READERS_" + tableName + " where " + tableName + ".DOCID = READERS_" + tableName
					        + ".DOCID " + " and " + tableName + ".DOCID = " + docID + " and READERS_" + tableName + ".USERNAME IN ("
					        + DatabaseUtil.prepareListToQuery(complexUserID) + ")";
					rs = s.executeQuery(sql);
				}
				if (docType == DOCTYPE_GLOSSARY || rs != null && rs.next()) {
					Statement attachStatement = conn.createStatement();
					ResultSet attachResultSet = attachStatement.executeQuery("select * from CUSTOM_BLOBS_" + tableName + " where CUSTOM_BLOBS_"
					        + tableName + ".DOCID = " + docID + " AND CUSTOM_BLOBS_" + tableName + ".NAME = '" + fieldName + "'");
					if (attachResultSet.next()) {
						do {
							String originalName = attachResultSet.getString("ORIGINALNAME");
							if (originalName.equals(fileName)) {
								LargeObjectManager lobj = ((org.postgresql.PGConnection) ((DelegatingConnection) conn).getInnermostDelegate())
								        .getLargeObjectAPI();
								long oid = attachResultSet.getLong("VALUE_OID");
								LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);
								InputStream is = obj.getInputStream();
								String fullPath = Util.getFileName(originalName, Environment.tmpDir + "/" + Util.generateRandom() + "/");
								FileOutputStream out = new FileOutputStream(fullPath);
								byte[] b = new byte[1048576];
								int len = 0;
								while ((len = is.read(b)) > 0) {
									out.write(b, 0, len);
									// out.write(b);
								}
								out.close();
								attachStatement.close();
								s.close();
								return fullPath;
							}
						} while (attachResultSet.next() && fieldName.equals(attachResultSet.getString("NAME")));
					}
					attachResultSet.close();
					attachStatement.close();
				}
				s.close();
				conn.commit();
			} catch (FileNotFoundException e) {
				DatabaseUtil.errorPrint(dbID, e);
			} catch (IOException ioe) {
				DatabaseUtil.errorPrint(dbID, ioe);
			}
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return "";
	}

	@Override
	public ISelectFormula getSelectFormula(FormulaBlocks blocks) {
		switch (blocks.docType) {
		case GLOSSARY:
			return new GlossarySelectFormula(blocks);
		case STRUCTURE:
			return this.getStructure().getSelectFormula(blocks);
		case DOCUMENT:
		default:
			SelectFormula sf = new SelectFormula(blocks);
			return sf;
		}
	}
}
