package kz.flabs.dataengine.h2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseConst;
import kz.flabs.dataengine.DatabaseCore;
import kz.flabs.dataengine.DatabaseFactory;
import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.DatabaseType;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IActivity;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IExecutions;
import kz.flabs.dataengine.IFTIndexEngine;
import kz.flabs.dataengine.IFilters;
import kz.flabs.dataengine.IForum;
import kz.flabs.dataengine.IGlossaries;
import kz.flabs.dataengine.IHelp;
import kz.flabs.dataengine.IMyDocsProcessor;
import kz.flabs.dataengine.IProjects;
import kz.flabs.dataengine.IQueryFormula;
import kz.flabs.dataengine.ISelectFormula;
import kz.flabs.dataengine.IStructure;
import kz.flabs.dataengine.ISystemDatabase;
import kz.flabs.dataengine.ITasks;
import kz.flabs.dataengine.IUsersActivity;
import kz.flabs.dataengine.UsersActivityType;
import kz.flabs.dataengine.h2.filters.Filters;
import kz.flabs.dataengine.h2.forum.Forum;
import kz.flabs.dataengine.h2.forum.ForumSelectFormula;
import kz.flabs.dataengine.h2.ftengine.FTIndexEngine;
import kz.flabs.dataengine.h2.glossary.Glossaries;
import kz.flabs.dataengine.h2.glossary.GlossaryQueryFormula;
import kz.flabs.dataengine.h2.help.Help;
import kz.flabs.dataengine.h2.queryformula.GroupQueryFormula;
import kz.flabs.dataengine.h2.queryformula.ProjectQueryFormula;
import kz.flabs.dataengine.h2.queryformula.QueryFormula;
import kz.flabs.dataengine.h2.queryformula.SelectFormula;
import kz.flabs.dataengine.h2.structure.StructQueryFormula;
import kz.flabs.dataengine.h2.structure.Structure;
import kz.flabs.dataengine.h2.usersactivity.UsersActivity;
import kz.flabs.dataengine.postgresql.queryformula.TaskQueryFormula;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DataConversionException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.DocumentExceptionType;
import kz.flabs.exception.ExceptionType;
import kz.flabs.exception.LicenseException;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.parser.ParserUtil;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.parser.SortByBlock;
import kz.flabs.runtimeobj.DocumentCollection;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.runtimeobj.document.AbstractComplexObject;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.BlobField;
import kz.flabs.runtimeobj.document.BlobFile;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.Execution;
import kz.flabs.runtimeobj.document.Field;
import kz.flabs.runtimeobj.document.IComplexObject;
import kz.flabs.runtimeobj.document.coordination.Block;
import kz.flabs.runtimeobj.document.coordination.BlockCollection;
import kz.flabs.runtimeobj.document.coordination.Coordinator;
import kz.flabs.runtimeobj.document.coordination.Decision;
import kz.flabs.runtimeobj.document.glossary.Glossary;
import kz.flabs.runtimeobj.document.project.Project;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.document.task.Task;
import kz.flabs.runtimeobj.viewentry.IViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntryCollection;
import kz.flabs.servlets.sitefiles.UploadedFile;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.users.Reader;
import kz.flabs.users.RunTimeParameters;
import kz.flabs.users.User;
import kz.flabs.util.ResponseType;
import kz.flabs.util.Util;
import kz.flabs.util.XMLResponse;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.Role;
import kz.flabs.webrule.WebRuleProvider;
import kz.flabs.webrule.constants.FieldType;
import kz.flabs.webrule.constants.QueryType;
import kz.flabs.webrule.constants.TagPublicationFormatType;
import kz.flabs.webrule.module.ExternalModule;
import kz.flabs.webrule.module.ExternalModuleType;
import kz.nextbase.script._ViewEntryCollection;
import kz.nextbase.script.constants._ReadConditionType;
import kz.pchelka.env.Environment;
import kz.pchelka.log.ILogger;
import kz.pchelka.server.Server;

public class Database extends DatabaseCore implements IDatabase, Const {
	public boolean isValid;
	public WebRuleProvider ruleProvider;
	public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	public static final SimpleDateFormat sqlDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static ILogger logger = Server.logger;

	protected String connectionURL = "";
	protected IDBConnectionPool dbPool;
	protected IDBConnectionPool structDbPool;
	protected IDBConnectionPool forumDbPool;
	protected String dbID;
	protected AppEnv env;
	protected IUsersActivity usersActivity;
	protected IActivity activity;
	protected static String baseTable = "MAINDOCS";

	protected String externalStructureApp;
	private static final String maindocFields = "MAINDOCS.DOCID, DDBID, AUTHOR, PARENTDOCID, PARENTDOCTYPE, REGDATE, DOCTYPE, LASTUPDATE, VIEWTEXT, "
			+ DatabaseUtil.getViewTextList("") + ", VIEWNUMBER, VIEWDATE, VIEWICON, FORM, HAS_ATTACHMENT ";
	private boolean respUsed;

	public Database(AppEnv env, DatabaseType dbType)
			throws DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.env = env;
		if (env.globalSetting.databaseEnable) {
			dbID = env.globalSetting.databaseName;
			connectionURL = env.globalSetting.dbURL;
			dbPool = new DBConnectionPool();
			dbPool.initConnectionPool(env.globalSetting.driver, connectionURL, env.globalSetting.getDbUserName(),
					env.globalSetting.getDbPassword());

			usersActivity = new UsersActivity(this);
		}

		databaseType = dbType;

	}

	public Database(AppEnv env)
			throws DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.env = env;
		dbID = env.globalSetting.databaseName;
		connectionURL = env.globalSetting.dbURL;
		dbPool = new DBConnectionPool();
		dbPool.initConnectionPool(env.globalSetting.driver, connectionURL);
		usersActivity = new UsersActivity(this);
		activity = new Activity(this);
		initStructPool();
		forumDbPool = initForumPool();
		respUsed = hasDocsWithParent();
	}

	public Database(AppEnv env, boolean auth)
			throws DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.env = env;
		if (env.globalSetting.databaseEnable) {
			dbID = env.globalSetting.databaseName;
			connectionURL = env.globalSetting.dbURL;
			if (auth) {
				dbPool = new DBConnectionPool();
				dbPool.initConnectionPool(env.globalSetting.driver, connectionURL, env.globalSetting.getDbUserName(),
						env.globalSetting.getDbPassword());
			} else {
				dbPool = new DBConnectionPool();
				dbPool.initConnectionPool(env.globalSetting.driver, connectionURL);
			}
			usersActivity = new UsersActivity(this);
		}

		initStructPool();
		forumDbPool = initForumPool();
		databaseType = DatabaseType.H2;
		respUsed = hasDocsWithParent();
	}

	protected void initStructPool() {
		for (ExternalModule module : env.globalSetting.extModuleMap.values()) {
			if (module.getType() == ExternalModuleType.STRUCTURE) {
				externalStructureApp = module.getName();
				Environment.addDelayedInit(this);
			} else {
				Environment.addDelayedInit(this);
			}
		}
		structDbPool = dbPool;
	}

	@Override
	public String initExternalPool(ExternalModuleType extModule) {
		AppEnv extApp = Environment.getApplication(externalStructureApp);
		if (extApp != null) {
			IDBConnectionPool pool = DatabaseFactory.getDatabase(extApp.appType).getConnectionPool();
			structDbPool = pool;
			return env.appType + " has connected to >" + extApp.appType;
		} else {
			return env.appType + " has not connected to external module " + extModule;
		}

	}

	private IDBConnectionPool initForumPool() {
		return dbPool;
	}

	@Override
	public void setTopic(int topicID, int parentdocID, int parentDocType) {
		Connection conn = dbPool.getConnection();
		String tableName = DatabaseUtil.getMainTableName(parentDocType);
		String sql = "update " + tableName + " set topicid = ? where docid = ?";
		try {
			conn.setAutoCommit(false);
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setInt(1, topicID);
			pst.setInt(2, parentdocID);
			pst.execute();
			pst.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(e, sql);
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	public void resetTopic(int parentDocID, int parentDocType, User user) throws DatabasePoolException,
			InstantiationException, IllegalAccessException, ClassNotFoundException, ComplexObjectException {
		Connection conn = dbPool.getConnection();
		String tableName = DatabaseUtil.getMainTableName(parentDocType);
		String sql = "select topicid from " + tableName + " where docid = ?";
		try {
			conn.setAutoCommit(false);

			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setInt(1, parentDocID);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				this.deleteDocument(Const.DOCTYPE_TOPIC, rs.getInt("TOPICID"), user, true);
				sql = "update " + tableName + " set topicid = null where docid = ?";
				pst = conn.prepareStatement(sql);
				pst.setInt(1, parentDocID);
				pst.executeQuery();
			}
			pst.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(e, sql);
		} catch (DocumentException de) {
			DatabaseUtil.errorPrint(de, sql);
		} catch (DocumentAccessException dae) {
			DatabaseUtil.errorPrint(dae, sql);
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Override
	public int getVersion() {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String sql = "SELECT * FROM DBVERSION";
			int version;
			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				version = rs.getInt("VERSION");
				pst.close();
				return version;
			}
			rs.close();
			pst.close();
			conn.commit();
		} catch (SQLException e) {
			// Server.logger.errorLogEntry(e.getMessage());
		} finally {
			dbPool.returnConnection(conn);
		}
		return -1;
	}

	@Override
	public AppEnv getParent() {
		return env;
	}

	@Override
	public String getDbID() {
		return dbID;
	}

	@Override
	public int shutdown() {
		Database.logger
				.normalLogEntry("Pool of " + dbID + " is closing (num of connections=" + dbPool.getNumActive() + ")");
		dbPool.closeAll();
		return 0;
	}

	@Override
	public Document getMainDocumentByID(int docID, Set<String> complexUserID, String absoluteUserID)
			throws DocumentAccessException, DocumentException, ComplexObjectException {
		Document doc = new Document(this, absoluteUserID);
		Connection conn = dbPool.getConnection();
		String sql = "select * from MAINDOCS as m " + "left join CUSTOM_FIELDS as cf on cf.docid = " + docID
				+ " where exists " + "(select * from readers_maindocs as rm where rm.docid = " + docID;

		if (!complexUserID.contains("[supervisor]")) {
			sql += " and rm.username in (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";
		}

		sql += ") and m.docid = " + docID;
		try {
			conn.setAutoCommit(false);
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			boolean isNextMain = true;
			if (rs.next()) {
				doc.editMode = EDITMODE_READONLY;
				fillViewTextData(rs, doc);
				fillSysData(rs, doc);
				doc.hasDiscussion = rs.getBoolean("HAS_TOPIC");// .setTopicID(rs.getInt("TOPICID"));
				while (isNextMain || rs.next()) {
					switch (rs.getInt("TYPE")) {
					case TEXT:
						doc.addStringField(rs.getString("NAME"), rs.getString("VALUE"));
						break;
					case NUMBERS:
						doc.addNumberField(rs.getString("NAME"), rs.getBigDecimal("VALUEASNUMBER"));
						break;
					case DATETIMES:
					case DATE:
						doc.addDateField(rs.getString("NAME"), rs.getTimestamp("VALUEASDATE"));
						break;
					case RICHTEXT:
						doc.addField(rs.getString("NAME"), rs.getString("VALUEASCLOB"), FieldType.RICHTEXT);
						break;
					case COMPLEX_OBJECT:
						String fieldName = rs.getString("NAME");
						String className = "";
						String initString = "";
						String value = "";
						String valueasobject = "";

						valueasobject = rs.getString("VALUEASOBJECT");
						value = rs.getString("VALUE");

						Object object = null;
						IComplexObject complexObject = null;
						try {
							if (valueasobject != null && !"".equalsIgnoreCase(valueasobject)) {
								value = valueasobject;
							}
							if (value != null && !"".equalsIgnoreCase(value)) {
								if (value.startsWith("<")) {
									Pattern pattern = Pattern.compile("className=\\\"(.+?)\\\"");
									Matcher matcher = pattern.matcher(value);
									if (matcher.find()) {
										className = matcher.group(1);
									}
									initString = value;
									complexObject = AbstractComplexObject.unmarshall(className, initString);
								} else if (value.contains("~")) {
									int delimiterPosition = value.indexOf("~");
									className = value.substring(0, delimiterPosition);
									Class c = Class.forName(className);
									object = c.newInstance();
									initString = value.substring(delimiterPosition + 1);
									complexObject = (IComplexObject) object;
									complexObject.init(this, initString);
								}
								doc.addComplexObjectField(fieldName, complexObject);
							}
						} catch (ClassCastException e) {
							env.logger.warningLogEntry(e.getMessage());
						} catch (ClassNotFoundException e) {
							throw new DocumentException(DocumentExceptionType.COMPLEX_OBJECT_INCORRECT,
									e.getMessage() + ", field=" + fieldName);
						} catch (InstantiationException e) {
							throw new DocumentException(DocumentExceptionType.COMPLEX_OBJECT_INCORRECT,
									e.getMessage() + ", field=" + fieldName);
						} catch (IllegalAccessException e) {
							throw new DocumentException(DocumentExceptionType.COMPLEX_OBJECT_INCORRECT,
									e.getMessage() + ", field=" + fieldName);
						} catch (Exception e) {
							throw new DocumentException(DocumentExceptionType.COMPLEX_OBJECT_INCORRECT,
									e.getMessage() + ", field=" + fieldName);
						}

						break;
					case TEXTLIST:
						doc.addValueToList(rs.getString("NAME"), rs.getString("VALUE"));
						break;
					case GLOSSARY:
						ArrayList<Integer> glossData = new ArrayList<Integer>();
						glossData.add(rs.getInt("VALUEASGLOSSARY"));
						doc.addGlossaryField(rs.getString("NAME"), glossData);
						break;
					case COORDINATION:
						BlockCollection blockCollection = new BlockCollection();
						String status = rs.getString("VALUE");
						try {
							blockCollection.setStatus(Integer.parseInt(status));
						} catch (Exception e) {
						}
						PreparedStatement blockStatement = conn
								.prepareStatement("SELECT * FROM COORDBLOCKS where DOCID = ?");
						blockStatement.setInt(1, docID);
						ResultSet blockResultSet = blockStatement.executeQuery();
						while (blockResultSet.next()) {
							Block block = new Block();
							int blockID = blockResultSet.getInt("ID");
							block.blockID = blockID;
							block.setNumber(blockResultSet.getInt("BLOCKNUMBER"));
							block.delayTime = blockResultSet.getInt("DELAYTIME");
							block.type = blockResultSet.getInt("TYPE");
							block.status = blockResultSet.getInt("STATUS");
							block.setCoordDate(blockResultSet.getTimestamp("COORDATE"));

							PreparedStatement coordsPreparedStatement = conn
									.prepareStatement("select *  from COORDINATORS where BLOCKID = ?");
							coordsPreparedStatement.setInt(1, blockID);
							ResultSet coordsResultSet = coordsPreparedStatement.executeQuery();
							while (coordsResultSet.next()) {
								Coordinator coord = new Coordinator(this);
								coord.setUserID(coordsResultSet.getString("COORDINATOR"));
								coord.setType(coordsResultSet.getInt("COORDTYPE"));
								int decision = coordsResultSet.getInt("DECISION");
								String comment = coordsResultSet.getString("COMMENT");
								Date decisionDate = coordsResultSet.getTimestamp("DECISIONDATE");

								Decision d = new Decision(decision, comment, decisionDate);
								coord.setDecision(d);

								coord.setCurrent(coordsResultSet.getInt("ISCURRENT") == 1);
								coord.setCoorDate(coordsResultSet.getTimestamp("COORDATE"));
								fillBlobs(conn, coord.blobFieldsMap, "coordinators", coordsResultSet.getInt("ID"));

								for (BlobField field : coord.blobFieldsMap.values()) {
									for (BlobFile file : field.getFiles()) {
										coord.addAttachID(Integer.parseInt(file.id));
									}
								}

								block.addCoordinator(coord);
							}
							coordsResultSet.close();
							blockCollection.addBlock(block);
						}
						doc.addCoordinationField("coordination", blockCollection);
						blockResultSet.close();
						break;
					}

					isNextMain = false;
				}

				fillAccessRelatedField(conn, baseTable, docID, doc);
				if (doc.hasEditor(complexUserID)) {
					doc.editMode = EDITMODE_EDIT;
				}

				fillBlobs(conn, doc, baseTable);
			} else {
				// Database.logger.errorLogEntry(sql);
				throw new DocumentAccessException(ExceptionType.DOCUMENT_READ_RESTRICTED, absoluteUserID);
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
			doc.isValid = false;
		} finally {
			dbPool.returnConnection(conn);
		}
		return doc;
	}

	@Override
	public int calcStartEntry(int pageNum, int pageSize) {
		int pageNumMinusOne = pageNum;
		pageNumMinusOne--;
		return pageNumMinusOne * pageSize;
	}

	public void fillBlobs(Connection conn, Map<String, BlobField> blobFieldsMap, String customBlobTableSuffix,
			int docID) throws SQLException {
		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery("select * from CUSTOM_BLOBS_" + customBlobTableSuffix
				+ "  where CUSTOM_BLOBS_" + customBlobTableSuffix + ".DOCID = " + docID + " ORDER BY ID");
		HashMap<String, BlobFile> files = new HashMap<String, BlobFile>();
		String name = "";
		while (rs.next()) {
			name = rs.getString("NAME");
			BlobFile bf = new BlobFile();
			bf.originalName = rs.getString("ORIGINALNAME");
			bf.checkHash = rs.getString("CHECKSUM");
			bf.comment = rs.getString("COMMENT");
			bf.id = String.valueOf(rs.getInt("ID"));
			bf.docid = rs.getInt("DOCID");
			files.put(bf.originalName, bf);
		}
		BlobField newBlobField = new BlobField(name, files);
		blobFieldsMap.put(newBlobField.name, newBlobField);
	}

	@Override
	public void fillBlobs(Connection conn, BaseDocument doc, String customBlobTableSuffix) throws SQLException {
		doc.blobFieldsMap.clear();
		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery("select * from CUSTOM_BLOBS_" + customBlobTableSuffix
				+ "  where CUSTOM_BLOBS_" + customBlobTableSuffix + ".DOCID = " + doc.getDocID() + " ORDER BY ID");
		while (rs.next()) {
			HashMap<String, BlobFile> files = new HashMap<String, BlobFile>();
			String name = rs.getString("NAME");
			BlobFile bf = new BlobFile();
			bf.originalName = rs.getString("ORIGINALNAME");
			bf.checkHash = rs.getString("CHECKSUM");
			bf.comment = rs.getString("COMMENT");
			bf.id = String.valueOf(rs.getInt("ID"));
			bf.docid = rs.getInt("DOCID");
			files.put(bf.originalName, bf);
			doc.addBlobField(name, files);
		}
	}

	public void fillBlobsContent(BaseDocument doc) throws SQLException {
		Connection conn = dbPool.getConnection();
		try {
			doc.blobFieldsMap.clear();
			Statement statement = conn.createStatement();
			String blobsTable = DatabaseUtil.getCustomBlobsTableName(doc.docType);
			ResultSet rs = statement.executeQuery(
					"select * from " + blobsTable + " where " + blobsTable + ".DOCID = " + doc.getDocID());
			while (rs.next()) {
				HashMap<String, BlobFile> files = new HashMap<String, BlobFile>();
				String name = rs.getString("NAME");
				BlobFile bf = new BlobFile();
				bf.originalName = rs.getString("ORIGINALNAME");
				bf.checkHash = rs.getString("CHECKSUM");
				bf.comment = rs.getString("COMMENT");
				bf.setContent(rs.getBytes("VALUE"));
				files.put(bf.originalName, bf);
				doc.addBlobField(name, files);
			}
			rs.close();
			statement.close();
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	public boolean hasBlobs(BaseDocument doc) {
		switch (doc.docType) {
		case DOCTYPE_MAIN:
		case DOCTYPE_TASK:
		case DOCTYPE_EXECUTION:
		case DOCTYPE_PROJECT:
		case DOCTYPE_EMPLOYER:
			return true;
		default:
			return false;
		}
	}

	@Override
	public void addCounter(String key, int num) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String sql = "SELECT * FROM COUNTERS WHERE KEYS = '" + key + "'";
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				return;
			} else {
				sql = "INSERT INTO COUNTERS(KEYS, LASTNUM) values ('" + key + "', " + num + ")";
				s.execute(sql);
				conn.commit();
			}
			s.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Override
	public String getMainDocumentFieldValueByID(int docID, Set<String> complexUserID, String absoluteUserID,
			String fieldName) throws DocumentAccessException {

		String returnValue = "";
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement statement = conn.createStatement();

			String sql = "";
			FormulaBlocks formulaBlocks = new FormulaBlocks("", QueryType.DOCUMENT);
			Matcher sysFields = formulaBlocks.getFieldsPattern().matcher(fieldName);
			Boolean bSysField = sysFields.lookingAt();
			if (bSysField) {
				sql = "select m." + fieldName + " from MAINDOCS as m where exists "
						+ "(select docid,username from readers_maindocs as rm where" + " rm.docid = " + docID
						+ " and rm.username in (" + DatabaseUtil.prepareListToQuery(complexUserID) + "))"
						+ " and m.docid = " + docID;
			} else {
				sql = "select cf.docid, cf.type, cf.name, cf.value, cf.valueasnumber, cf.valueasdate, cf.valueasglossary from MAINDOCS as m, CUSTOM_FIELDS as cf where exists "
						+ "(select docid,username from readers_maindocs as rm where" + " rm.docid = " + docID
						+ " and rm.username in (" + DatabaseUtil.prepareListToQuery(complexUserID) + "))"
						+ " and m.docid = " + docID + " and cf.docid = " + docID + " and name='" + fieldName + "'";
			}

			ResultSet rs = statement.executeQuery(sql);

			if (rs.next()) {
				if (bSysField) {
					returnValue = rs.getString(fieldName);
				} else {
					if (rs.getString("NAME").equalsIgnoreCase(fieldName)) {
						switch (rs.getInt("TYPE")) {
						case TEXT:
							returnValue = rs.getString("VALUE");
							break;
						case NUMBERS:
							returnValue = rs.getString("VALUEASNUMBER");
							break;
						case DATETIMES:
							returnValue = rs.getString("VALUEASDATE");
							break;
						case DATE:
							returnValue = rs.getString("VALUEASDATE");
							break;
						case TEXTLIST:
							returnValue = rs.getString("VALUE");
							break;
						case GLOSSARY:
							ArrayList<Integer> glossData = new ArrayList<Integer>();
							glossData.add(rs.getInt("VALUEASGLOSSARY"));
							returnValue = glossData.get(0).toString();
							break;
						}
					}
				}
			} else {
				throw new DocumentAccessException(ExceptionType.DOCUMENT_READ_RESTRICTED, absoluteUserID);
			}

			statement.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}

		return returnValue;
	}

	@Override
	public String getGlossaryCustomFieldValueByID(int docID, String fieldName) {

		String returnValue = "";
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement statement = conn.createStatement();

			String sql = "select cfg.docid, cfg.type, cfg.name, cfg.value, cfg.valueasnumber, cfg.valueasdate, cfg.valueasglossary "
					+ "from CUSTOM_FIELDS_GLOSSARY as cfg "
					+ "where exists (select docid from GLOSSARY as g where g.docid = " + docID + " and cfg.docid = "
					+ docID + " and cfg.name='" + fieldName + "');";

			ResultSet rs = statement.executeQuery(sql);

			if (rs.next()) {
				if (rs.getString("NAME").equalsIgnoreCase(fieldName)) {
					switch (rs.getInt("TYPE")) {
					case TEXT:
						returnValue = rs.getString("VALUE");
						break;
					case NUMBERS:
						returnValue = rs.getString("VALUEASNUMBER");
						break;
					case DATETIMES:
						returnValue = rs.getString("VALUEASDATE");
						break;
					case DATE:
						returnValue = rs.getString("VALUEASDATE");
						break;
					case GLOSSARY:
						ArrayList<Integer> glossData = new ArrayList<Integer>();
						glossData.add(rs.getInt("VALUEASGLOSSARY"));
						returnValue = glossData.get(0).toString();
						break;
					}
				}
			}

			statement.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}

		return returnValue;
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
						InputStream is = rs.getBinaryStream("VALUE");
						String fullPath = Util.getFileName(originalName,
								Environment.tmpDir + "/" + Util.generateRandom() + "/");
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
	public String getDocumentAttach(int docID, int docType, Set<String> complexUserID, String fieldName,
			String fileName) {
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
					String sql = "select * from " + tableName + ", READERS_" + tableName + " where " + tableName
							+ ".DOCID = READERS_" + tableName + ".DOCID " + " and " + tableName + ".DOCID = " + docID
							+ " and READERS_" + tableName + ".USERNAME IN ("
							+ DatabaseUtil.prepareListToQuery(complexUserID) + ")";
					rs = s.executeQuery(sql);
				}
				if (docType == DOCTYPE_GLOSSARY || rs != null && rs.next()) {
					Statement attachStatement = conn.createStatement();
					ResultSet attachResultSet = attachStatement.executeQuery(
							"select * from CUSTOM_BLOBS_" + tableName + " where CUSTOM_BLOBS_" + tableName + ".DOCID = "
									+ docID + " AND CUSTOM_BLOBS_" + tableName + ".NAME = '" + fieldName + "'");
					if (attachResultSet.next()) {
						do {
							String originalName = attachResultSet.getString("ORIGINALNAME");
							if (originalName.equals(fileName)) {
								InputStream is = attachResultSet.getBinaryStream("VALUE");
								String fullPath = Util.getFileName(originalName,
										Environment.tmpDir + "/" + Util.generateRandom() + "/");
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
	public int insertMainDocument(Document doc, User user) throws DocumentException {
		int id = doc.getDocID();
		String fieldsAsText = "AUTHOR, REGDATE, DOCTYPE, LASTUPDATE, DDBID, PARENTDOCDDBID, VIEWTEXT, PARENTDOCID, PARENTDOCTYPE, "
				+ " FORM, " + DatabaseUtil.getViewTextList("")
				+ ", VIEWNUMBER, VIEWDATE, SIGN, SIGNEDFIELDS, HAS_ATTACHMENT";
		Date viewDate = doc.getViewDate();
		String viewTextList = "";
		for (int i = 1; i <= DatabaseConst.VIEWTEXT_COUNT; i++) {
			viewTextList += "'" + doc.getViewTextList().get(i).replaceAll("'", "''") + "',";
		}
		if (viewTextList.endsWith(",")) {
			viewTextList = viewTextList.substring(0, viewTextList.length() - 1);
		}
		int count_files = 0;
		for (BlobField bfield : doc.blobFieldsMap.values()) {
			count_files += bfield.getFilesCount();
		}
		String valuesAsText = "'" + doc.getAuthorID() + "', '" + sqlDateTimeFormat.format(doc.getRegDate()) + "', "
				+ doc.docType + ", " + "'" + sqlDateTimeFormat.format(doc.getLastUpdate()) + "', '" + doc.getDdbID()
				+ "', '" + doc.getParentDocumentID() + "', '" + doc.getViewText().replace("'", "''") + "', "
				+ doc.parentDocID + ", " + doc.parentDocType + ",'" + doc.form + "', " + viewTextList + ", "
				+ doc.getViewNumber() + ", "
				+ (viewDate != null ? "'" + new Timestamp(viewDate.getTime()) + "'" : "null") + ", '" + doc.getSign()
				+ "', '" + doc.getSignedFields() + "', " + count_files;
		Connection conn = dbPool.getConnection();
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
				if (rs.next()) {
					key = rs.getInt(1);
				} else {
					key = id;
				}
				for (Field field : doc.fields()) {
					switch (field.getTypeAsDatabaseType()) {
					case TEXT:
						try {
							String sqlStatement = "insert into CUSTOM_FIELDS(DOCID, NAME, VALUE, TYPE)" + "values ("
									+ key + ", '" + field.name + "', '" + field.valueAsText.replace("'", "''").trim()
									+ "', " + field.getTypeAsDatabaseType() + ")";
							pst = conn.prepareStatement(sqlStatement);
							pst.executeUpdate();
						} catch (SQLException se) {
							// System.out.println(field.name);
							DatabaseUtil.errorPrint(dbID, se);
							conn.rollback();
							return -1;
						}
						break;
					case RICHTEXT:
						try {
							String sqlStatement = "insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASCLOB, TYPE)"
									+ "values (" + key + ", '" + field.name + "', '"
									+ field.valueAsText.replace("'", "''").trim() + "', "
									+ field.getTypeAsDatabaseType() + ")";
							pst = conn.prepareStatement(sqlStatement);
							pst.executeUpdate();
						} catch (SQLException se) {
							// System.out.println(field.name);
							DatabaseUtil.errorPrint(dbID, se);
							conn.rollback();
							return -1;
						}
						break;
					case COMPLEX_OBJECT:
						try {
							String sqlStatement = "insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASOBJECT, TYPE)"
									+ "values (" + key + ", '" + field.name + "', '"
									// +
									// field.valueAsObject.getPersistentValue()
									+ AbstractComplexObject.marshall(field.valueAsObject.getClass().getName(),
											field.valueAsObject)
									+ "', " + field.getTypeAsDatabaseType() + ")";
							pst = conn.prepareStatement(sqlStatement);
							pst.executeUpdate();
						} catch (SQLException se) {
							DatabaseUtil.errorPrint(dbID, se);
							conn.rollback();
							return -1;
						}
						break;
					case TEXTLIST:
						pst = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUE, TYPE)" + " values ("
								+ key + ", '" + field.name + "', ?," + field.getTypeAsDatabaseType() + ")");
						for (String value : field.valuesAsStringList) {
							pst.setString(1, value);
							try {
								pst.executeUpdate();
							} catch (SQLException se) {
								// System.out.println(doc.getDdbID());
								// System.out.println(field.name);
								DatabaseUtil.errorPrint(dbID, se);
								return -1;
							}
						}
						break;
					case NUMBERS:
						pst = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASNUMBER, TYPE)"
								+ "values(" + key + ", '" + field.name + "', " + field.valueAsNumber + ", "
								+ field.getTypeAsDatabaseType() + ")");
						pst.executeUpdate();
						break;
					case DATETIMES:
						if (field.valueAsDate == null) {
							Database.logger.errorLogEntry("Unable to convert empty date to DB format: " + field.name);
							continue;
						}
						try {
							pst = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASDATE, TYPE)"
									+ "values(" + key + ", '" + field.name + "', '"
									+ Util.convertDateTimeToDerbyFormat(field.valueAsDate) + "', "
									+ field.getTypeAsDatabaseType() + ")");
						} catch (DataConversionException e) {
							Database.logger.errorLogEntry(e + ", field=" + field.name);
							return -1;
						}
						pst.executeUpdate();
						break;
					case DATE:
						if (field.valueAsDate == null) {
							Database.logger.errorLogEntry("Unable to convert \"null\" to date : " + field.name);
							continue;
						}
						try {
							pst = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASDATE, TYPE)"
									+ "values(" + key + ", '" + field.name + "', '"
									+ Util.convertDateTimeToDerbyFormat(field.valueAsDate) + "', "
									+ field.getTypeAsDatabaseType() + ")");
						} catch (DataConversionException e) {
							Database.logger.errorLogEntry(e + ", field=" + field.name);
							return -1;
						}
						pst.executeUpdate();
						break;
					case GLOSSARY:
						pst = conn.prepareStatement(
								"insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASGLOSSARY, TYPE)" + " values (" + key
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
								DatabaseUtil.errorPrint(dbID + " " + field.name, se);
								return -1;
							}
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
						PreparedStatement preparedStatement = conn.prepareStatement(
								"insert into CUSTOM_FIELDS(DOCID, NAME, VALUE, TYPE)" + "values (?, ?, ?, ?)");
						preparedStatement.setInt(1, key);
						preparedStatement.setString(2, field.name);
						preparedStatement.setString(3, String.valueOf(blockCollection.getStatus()));
						preparedStatement.setInt(4, field.getTypeAsDatabaseType());
						pst.executeUpdate();
						break;
					}
				}

				insertBlobTables(conn, id, key, doc, baseTable);
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
			} catch (FileNotFoundException e) {
				DatabaseUtil.errorPrint(dbID, e);
				return -1;
			} catch (IOException e) {
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

	protected int insertBlock(int key, Block block, Connection conn) {
		try {
			String sqlBlock = "insert into COORDBLOCKS(DOCID, TYPE, DELAYTIME, BLOCKNUMBER, STATUS, COORDATE) values(?, ?, ?, ?, ?, ?)";
			PreparedStatement statBlock = conn.prepareStatement(sqlBlock, Statement.RETURN_GENERATED_KEYS);
			statBlock.setInt(1, key);
			statBlock.setInt(2, block.getType());
			statBlock.setInt(3, block.delayTime);
			statBlock.setInt(4, block.getNumber());
			statBlock.setInt(5, block.getStatus());
			if (block != null && block.getCoordDate() != null) {
				statBlock.setTimestamp(6, new Timestamp(block.getCoordDate().getTime()));
			} else {
				statBlock.setNull(6, Types.TIMESTAMP);
			}
			statBlock.executeUpdate();
			ResultSet rsb = statBlock.getGeneratedKeys();
			block.isNew = false;
			if (rsb.next()) {
				block.blockID = rsb.getInt(1);
				return rsb.getInt(1);
			}
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		}
		return -1;
	}

	protected int recoverCommAttachRelations(Connection conn, int coordinatorID, ArrayList<Integer> ids) {
		try {// id not in (" + StringUtils.join(ids, ",")
			if (ids != null && ids.size() != 0) {
				PreparedStatement pst = conn
						.prepareStatement("update custom_blobs_coordinators set docid = ? where id in ("
								+ StringUtils.join(ids, ",") + ")");
				pst.setInt(1, coordinatorID);
				int res = pst.executeUpdate();
				conn.commit();
				pst.close();
				return res;
			}
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		}
		return -1;
	}

	protected int recoverCommAttachRelations(Connection conn, int coordinatorID, int attachID) {
		try {
			PreparedStatement pst = conn
					.prepareStatement("update custom_blobs_coordinators set docid = ? where id = ?");
			pst.setInt(1, coordinatorID);
			pst.setInt(2, attachID);
			int res = pst.executeUpdate();
			conn.commit();
			pst.close();
			return res;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
			return -1;
		}
	}

	protected int insertCoordinator(int blockKey, Coordinator coordinator, Connection conn) {
		try {
			PreparedStatement pst = conn.prepareStatement(
					"insert into COORDINATORS(BLOCKID, COORDTYPE, COORDINATOR, COORDNUMBER, ISCURRENT, COMMENT, DECISION, COORDATE, "
							+ "DECISIONDATE) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
					PreparedStatement.RETURN_GENERATED_KEYS);
			pst.setInt(1, blockKey);
			pst.setInt(2, coordinator.getCoordType());
			pst.setString(3, coordinator.getUserID());
			pst.setInt(4, coordinator.num);
			pst.setInt(5, coordinator.isCurrent() ? 1 : 0);
			pst.setString(6, coordinator.getDecision().getComment());
			pst.setInt(7, coordinator.getDecision().decision);
			if (coordinator != null && coordinator.getCoorDate() != null) {
				pst.setTimestamp(8, new Timestamp(coordinator.getCoorDate().getTime()));
			} else {
				pst.setNull(8, Types.TIMESTAMP);
			}
			if (coordinator != null && coordinator.getDecisionDate() != null) {
				pst.setTimestamp(9, new Timestamp(coordinator.getDecisionDate().getTime()));
			} else {
				pst.setNull(9, Types.TIMESTAMP);
			}

			pst.executeUpdate();
			int key = 0;
			ResultSet rs = pst.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);
			}
			pst.close();
			return key;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
			return -1;
		}
	}

	@Override
	public int updateMainDocument(Document doc, User user)
			throws DocumentAccessException, DocumentException, ComplexObjectException {
		if (doc.hasEditor(user.getAllUserGroups())) {
			Document oldDoc = this.getMainDocumentByID(doc.getDocID(), user.getAllUserGroups(), user.getUserID());
			Connection conn = dbPool.getConnection();
			try {
				Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
				conn.setAutoCommit(false);
				Date viewDate = doc.getViewDate();
				String viewTextList = "";
				for (int i = 1; i <= DatabaseConst.VIEWTEXT_COUNT; i++) {
					viewTextList += "VIEWTEXT" + i + " =  '" + doc.getViewTextList().get(i).replaceAll("'", "''")
							+ "',";
				}
				if (viewTextList.endsWith(",")) {
					viewTextList = viewTextList.substring(0, viewTextList.length() - 1);
				}
				int count_files = 0;
				for (BlobField bfield : doc.blobFieldsMap.values()) {
					count_files += bfield.getFilesCount();
				}
				String mainDocUpd = "update MAINDOCS set LASTUPDATE = '" + sqlDateTimeFormat.format(doc.getLastUpdate())
						+ "', VIEWTEXT='" + doc.getViewText().replace("'", "''").trim() + "', DDBID='" + doc.getDdbID()
						+ "', PARENTDOCDDBID='" + doc.getParentDocumentID() + "', DEFAULTRULEID='"
						+ doc.getDefaultRuleID() + "', " + viewTextList + ", VIEWNUMBER = " + doc.getViewNumber()
						+ ", VIEWDATE = " + (viewDate != null ? "'" + new Timestamp(viewDate.getTime()) + "'" : "null")
						+ ", SIGN = '" + doc.getSign() + "', SIGNEDFIELDS = '" + doc.getSignedFields() + "' "
						+ ", PARENTDOCID = " + doc.parentDocID + ", PARENTDOCTYPE = " + doc.parentDocType
						+ ", HAS_ATTACHMENT = " + count_files + " where DOCID = " + doc.getDocID();
				s.executeUpdate(mainDocUpd);
				for (Field field : doc.fields()) {
					String upCustomFields = "";
					switch (field.getTypeAsDatabaseType()) {
					case TEXT:
						upCustomFields = "merge into CUSTOM_FIELDS (DOCID, NAME, VALUE, TYPE) key(DOCID, NAME) values("
								+ doc.getDocID() + ", '" + field.name + "', '"
								+ field.valueAsText.replace("'", "''").trim() + "', " + field.getTypeAsDatabaseType()
								+ ")";
						break;
					case RICHTEXT:
						upCustomFields = "merge into CUSTOM_FIELDS (DOCID, NAME, VALUEASCLOB, TYPE) key(DOCID, NAME) values("
								+ doc.getDocID() + ", '" + field.name + "', '"
								+ field.valueAsText.replace("'", "''").trim() + "', " + field.getTypeAsDatabaseType()
								+ ")";
						break;
					case COMPLEX_OBJECT:
						upCustomFields = "merge into CUSTOM_FIELDS (DOCID, NAME, VALUEASOBJECT, TYPE) key(DOCID, NAME) values("
								+ doc.getDocID() + ", '" + field.name + "', '"
								// + field.valueAsObject.getPersistentValue()
								+ AbstractComplexObject.marshall(field.valueAsObject.getClass().getName(),
										field.valueAsObject)
								+ "', " + field.getTypeAsDatabaseType() + ")";
						break;
					case TEXTLIST:
						PreparedStatement ps = conn.prepareStatement("DELETE FROM CUSTOM_FIELDS" + " WHERE DOCID="
								+ doc.getDocID() + " and NAME='" + field.name + "'");
						ps.executeUpdate();
						ps = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUE, TYPE)" + " values ("
								+ doc.getDocID() + ", '" + field.name + "', ?," + field.getTypeAsDatabaseType() + ")");
						for (String value : field.valuesAsStringList) {
							ps.setString(1, value);
							ps.executeUpdate();
						}
						break;
					case NUMBERS:
						upCustomFields = "merge into CUSTOM_FIELDS (DOCID, NAME, VALUEASNUMBER, TYPE) key(DOCID, NAME) "
								+ "values (" + doc.getDocID() + ", '" + field.name + "', " + field.valueAsNumber + ", "
								+ field.getTypeAsDatabaseType() + ")";
						break;
					case DATETIMES:
						if (field.valueAsDate == null) {
							Database.logger.errorLogEntry("Unable to convert empty date to DB format: " + field.name);
							continue;
						}
						try {
							upCustomFields = "merge into CUSTOM_FIELDS (DOCID, NAME, VALUEASDATE, TYPE) KEY(DOCID, NAME)"
									+ " VALUES (" + doc.getDocID() + ", '" + field.name + "', '"
									+ Util.convertDateTimeToDerbyFormat(field.valueAsDate) + "', "
									+ field.getTypeAsDatabaseType() + ")";
						} catch (DataConversionException e) {
							Database.logger.errorLogEntry(e + ", field=" + field.name);
							return -1;
						}
						break;
					case GLOSSARY:
						PreparedStatement pst = conn.prepareStatement("DELETE FROM CUSTOM_FIELDS WHERE DOCID = "
								+ doc.getDocID() + " and NAME = '" + field.name + "'");
						pst.executeUpdate();
						pst = conn.prepareStatement("insert into CUSTOM_FIELDS(DOCID, NAME, VALUEASGLOSSARY, TYPE)"
								+ " values (" + doc.getDocID() + ", '" + field.name + "', ?,"
								+ field.getTypeAsDatabaseType() + ")");
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
						PreparedStatement preparedStatement = conn
								.prepareStatement("delete from coordblocks where docid = ? ");
						preparedStatement.setInt(1, doc.getDocID());
						preparedStatement.executeUpdate();

						BlockCollection blockCollection = (BlockCollection) field.valueAsObject;
						for (Block block : blockCollection.getBlocks()) {
							int blockID = insertBlock(doc.getDocID(), block, conn);
							for (Coordinator coordinator : block.getCoordinators()) {
								insertCoordinator(blockID, coordinator, conn);
							}
						}

						preparedStatement = conn
								.prepareStatement("delete from custom_field where name = ? and docid = ? and type = ?");
						preparedStatement.setString(1, field.name);
						preparedStatement.setInt(2, doc.getDocID());
						preparedStatement.setInt(3, field.getTypeAsDatabaseType());
						preparedStatement.executeUpdate();

						preparedStatement = conn.prepareStatement(
								"insert into CUSTOM_FIELDS(DOCID, NAME, VALUE, TYPE)" + "values (?, ?, ?, ?)");
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

				updateBlobTables(conn, doc, baseTable);
				updateAccessTables(conn, doc, baseTable);
				conn.commit();
				s.close();

				IUsersActivity ua = getUserActivity();
				ua.postModify(oldDoc, doc, user);

				return doc.getDocID();
			} catch (SQLException e) {
				DatabaseUtil.errorPrint(dbID, e);
				return -1;
			} catch (FileNotFoundException e) {
				DatabaseUtil.errorPrint(dbID, e);
				return -1;
			} catch (IOException e) {
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
	public void insertBlobTables(Connection conn, int id, int key, Document doc, String tableSuffix)
			throws SQLException, IOException {
		if (id != 0 && !doc.hasField("recID")) {
			PreparedStatement s0 = conn
					.prepareStatement("SELECT * FROM CUSTOM_BLOBS_" + tableSuffix + " WHERE DOCID = " + id);
			ResultSet rs0 = s0.executeQuery();
			while (rs0.next()) {
				PreparedStatement s1 = conn.prepareStatement("INSERT INTO CUSTOM_BLOBS_" + tableSuffix
						+ " (DOCID, NAME, ORIGINALNAME, CHECKSUM, COMMENT, VALUE)values(?, ?, ?, ?, ?, ?)");
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
			for (Entry<String, BlobField> blob : doc.blobFieldsMap.entrySet()) {
				PreparedStatement ps = conn.prepareStatement("INSERT INTO CUSTOM_BLOBS_" + tableSuffix
						+ " (DOCID, NAME, ORIGINALNAME, CHECKSUM, COMMENT, VALUE)values(?, ?, ?, ?, ?, ?)");
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
	public void updateBlobTables(Connection conn, Document doc, String tableSuffix) throws SQLException, IOException {
		Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		ResultSet blobs = s.executeQuery("SELECT ID, DOCID, NAME, ORIGINALNAME, CHECKSUM, COMMENT, VALUE "
				+ "FROM CUSTOM_BLOBS_" + tableSuffix + " WHERE DOCID = " + doc.getDocID());
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
			if (existingFile.comment != null && !existingFile.comment.equals(tableFile.comment)) {
				blobs.updateString("COMMENT", existingFile.comment);
				blobs.updateRow();
			}
		}
		/* now add files that are absent in database */
		for (Entry<String, BlobField> blob : doc.blobFieldsMap.entrySet()) {
			PreparedStatement ps = conn.prepareStatement(
					"SELECT ID, DOCID, NAME, ORIGINALNAME, CHECKSUM, COMMENT, VALUE " + "FROM CUSTOM_BLOBS_"
							+ tableSuffix + " WHERE DOCID = ? AND NAME = ? AND CHECKSUM = ?",
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			for (BlobFile bfile : blob.getValue().getFiles()) {
				// System.out.println("file=" + bfile);
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
					// System.out.println(file.getAbsolutePath());
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
	public void updateAccessTables(Connection conn, Document doc, String tableSuffix) throws SQLException {
		doc.addReaders(new ArrayList<String>(Const.supervisorGroupAsList));
		doc.addEditors(new ArrayList<String>(Const.supervisorGroupAsList));

		String[] accessTables = new String[2];
		accessTables[0] = "AUTHORS_" + tableSuffix;
		accessTables[1] = "READERS_" + tableSuffix;

		Statement deletion = conn.createStatement();
		for (String table : accessTables) {
			deletion.executeUpdate("DELETE FROM " + table + " WHERE DOCID = " + doc.getDocID());
			if (table.equals(accessTables[0])) {
				PreparedStatement insertion = conn.prepareStatement(
						"INSERT INTO " + table + " (USERNAME, DOCID) VALUES (?, " + doc.getDocID() + ")");
				for (String author : doc.getEditors()) {
					insertion.setString(1, author);
					insertion.executeUpdate();
				}
			} else {
				PreparedStatement insertion = conn
						.prepareStatement("INSERT INTO " + table + " (USERNAME, DOCID, FAVORITES) VALUES (?, ?, ?)");
				for (Reader reader : doc.getReaders()) {
					insertion.setString(1, reader.toString());
					insertion.setInt(2, doc.getDocID());
					insertion.setInt(3, reader.isFavorite() ? 1 : 0);
					insertion.executeUpdate();
				}
			}
		}
	}

	@Override
	public ArrayList<UploadedFile> insertBlobTables(List<FileItem> fileItems) throws SQLException, IOException {
		Connection conn = dbPool.getConnection();
		ArrayList<UploadedFile> files = new ArrayList<>();
		try {
			String tableSuffix = "MAINDOCS";
			for (FileItem item : fileItems) {
				if (item != null && item.getName() != null && !"".equalsIgnoreCase(item.getName())) {
					PreparedStatement ps = conn.prepareStatement(
							"INSERT INTO CUSTOM_BLOBS_" + tableSuffix
									+ " (DOCID, NAME, ORIGINALNAME, CHECKSUM, COMMENT, VALUE, REGDATE) values (?, ?, ?, ?, ?, ?, ?)",
							PreparedStatement.RETURN_GENERATED_KEYS);
					String hash = Util.getHexHash(item.getInputStream());
					ps.setInt(1, 1);
					ps.setString(2, "rtfcontent");
					ps.setString(3, FilenameUtils.getName(item.getName()));
					ps.setString(4, hash);
					ps.setString(5, "");
					ps.setBytes(6, item.get());
					ps.setTimestamp(7, new Timestamp(new Date().getTime()));
					ps.executeUpdate();
					conn.commit();
					int key = 0;
					ResultSet rs = ps.getGeneratedKeys();
					while (rs.next()) {
						key = rs.getInt(1);
					}
					String name = item.getName();
					long filelen = item.getSize();
					files.add(new UploadedFile(name, hash, filelen, item.getContentType(), String.valueOf(key)));
					ps.close();
				}
			}

		} catch (Exception e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return files;
	}

	@Override
	@Deprecated
	public ArrayList<BaseDocument> getAllDocuments(int docType, Set<String> complexUserID, String absoluteUserID,
			String[] fields, int start, int end)
					throws DocumentException, DocumentAccessException, ComplexObjectException {
		ArrayList<BaseDocument> documents = new ArrayList<BaseDocument>();
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			switch (docType) {
			case DOCTYPE_MAIN:
				String sql = "select distinct MAINDOCS.DOCID from MAINDOCS JOIN READERS_MAINDOCS on(MAINDOCS.DOCID=READERS_MAINDOCS.DOCID)"
						+ "and READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ getPageSQLPiece(start, end);

				ResultSet rs = s.executeQuery(sql);
				while (rs.next()) {
					Document doc = getMainDocumentByID(rs.getInt("DOCID"), complexUserID, absoluteUserID);
					documents.add(doc);
				}
				break;
			case DOCTYPE_TASK:
				sql = "select distinct TASKS.DOCID from TASKS,READERS_TASKS where TASKS.DOCID=READERS_TASKS.DOCID "
						+ "and READERS_TASKS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ getPageSQLPiece(start, end);
				rs = s.executeQuery(sql);
				while (rs.next()) {
					Task task = getTasks().getTaskByID(rs.getInt("DOCID"), complexUserID, absoluteUserID);
					documents.add(task);
				}
				break;
			case DOCTYPE_EXECUTION:
				sql = "select distinct EXECUTIONS.DOCID from EXECUTIONS JOIN READERS_EXECUTIONS on(EXECUTIONS.DOCID=READERS_EXECUTIONS.DOCID)"
						+ "and READERS_EXECUTIONS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ getPageSQLPiece(start, end);
				rs = s.executeQuery(sql);
				while (rs.next()) {
					Execution exec = getExecutions().getExecutionByID(rs.getInt("DOCID"), complexUserID,
							absoluteUserID);
					documents.add(exec);
				}
				break;
			case DOCTYPE_PROJECT:
				sql = "select distinct PROJECTS.DOCID from PROJECTS, READERS_PROJECTS where PROJECTS.DOCID=READERS_PROJECTS.DOCID "
						+ "and READERS_PROJECTS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ getPageSQLPiece(start, end);
				rs = s.executeQuery(sql);
				while (rs.next()) {
					Project prj = getProjects().getProjectByID(rs.getInt("DOCID"), complexUserID, absoluteUserID);
					documents.add(prj);
				}
				break;
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return documents;
	}

	@Override
	@Deprecated
	public ArrayList<BaseDocument> getAllDocuments(int docType, Set<String> complexUserID, String absoluteUserID,
			int start, int end) throws DocumentException, DocumentAccessException, ComplexObjectException {
		ArrayList<BaseDocument> documents = new ArrayList<BaseDocument>();
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			switch (docType) {
			case DOCTYPE_MAIN:
				String sql = "select distinct MAINDOCS.DOCID from MAINDOCS JOIN READERS_MAINDOCS on(MAINDOCS.DOCID=READERS_MAINDOCS.DOCID)"
						+ "and READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ getPageSQLPiece(start, end);

				ResultSet rs = s.executeQuery(sql);
				while (rs.next()) {
					Document doc = getMainDocumentByID(rs.getInt("DOCID"), complexUserID, absoluteUserID);
					documents.add(doc);
				}
				break;
			case DOCTYPE_TASK:
				sql = "select distinct TASKS.DOCID from TASKS,READERS_TASKS where TASKS.DOCID=READERS_TASKS.DOCID "
						+ "and READERS_TASKS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ getPageSQLPiece(start, end);
				rs = s.executeQuery(sql);
				while (rs.next()) {
					Task task = getTasks().getTaskByID(rs.getInt("DOCID"), complexUserID, absoluteUserID);
					documents.add(task);
				}
				break;
			case DOCTYPE_EXECUTION:
				sql = "select distinct EXECUTIONS.DOCID from EXECUTIONS JOIN READERS_EXECUTIONS on(EXECUTIONS.DOCID=READERS_EXECUTIONS.DOCID)"
						+ "and READERS_EXECUTIONS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ getPageSQLPiece(start, end);
				rs = s.executeQuery(sql);
				while (rs.next()) {
					Execution exec = getExecutions().getExecutionByID(rs.getInt("DOCID"), complexUserID,
							absoluteUserID);
					documents.add(exec);
				}
				break;
			case DOCTYPE_PROJECT:
				sql = "select distinct PROJECTS.DOCID from PROJECTS, READERS_PROJECTS where PROJECTS.DOCID=READERS_PROJECTS.DOCID "
						+ "and READERS_PROJECTS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ getPageSQLPiece(start, end);
				rs = s.executeQuery(sql);
				while (rs.next()) {
					Project prj = getProjects().getProjectByID(rs.getInt("DOCID"), complexUserID, absoluteUserID);
					documents.add(prj);
				}
				break;
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return documents;
	}

	@Override
	@Deprecated
	public ArrayList<Integer> getAllDocumentsIDS(int docType, Set<String> complexUserID, String absoluteUserID,
			String[] fields, int start, int end) throws DocumentException, DocumentAccessException {
		ArrayList<Integer> documents = new ArrayList<Integer>();
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			switch (docType) {
			case DOCTYPE_MAIN:
				String sql = "select MAINDOCS.DOCID from MAINDOCS JOIN READERS_MAINDOCS on(MAINDOCS.DOCID=READERS_MAINDOCS.DOCID)"
						+ "and READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ getPageSQLPiece(start, end);

				ResultSet rs = s.executeQuery(sql);
				while (rs.next()) {
					documents.add(rs.getInt("DOCID"));
				}
				break;
			case DOCTYPE_TASK:
				sql = "select TASKS.DOCID from TASKS,READERS_TASKS where TASKS.DOCID=READERS_TASKS.DOCID "
						+ "and READERS_TASKS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ getPageSQLPiece(start, end);
				rs = s.executeQuery(sql);
				while (rs.next()) {
					documents.add(rs.getInt("DOCID"));
				}
				break;
			case DOCTYPE_EXECUTION:
				sql = "select EXECUTIONS.DOCID from EXECUTIONS JOIN READERS_EXECUTIONS on(EXECUTIONS.DOCID=READERS_EXECUTIONS.DOCID)"
						+ "and READERS_EXECUTIONS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ getPageSQLPiece(start, end);
				rs = s.executeQuery(sql);
				while (rs.next()) {
					documents.add(rs.getInt("DOCID"));
				}
				break;
			case DOCTYPE_PROJECT:
				sql = "select PROJECTS.DOCID from PROJECTS, READERS_PROJECTS where PROJECTS.DOCID=READERS_PROJECTS.DOCID "
						+ "and READERS_PROJECTS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ getPageSQLPiece(start, end);
				rs = s.executeQuery(sql);
				while (rs.next()) {
					documents.add(rs.getInt("DOCID"));
				}
				break;
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return documents;
	}

	@Override
	public String removeDocumentFromRecycleBin(int id) {
		String viewtext = "";
		Connection conn = dbPool.getConnection();
		try {
			String sql = "select viewtext from users_activity where id in (select aid from recycle_bin where id = ?)";
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.setInt(1, id);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				viewtext = rs.getString(1);
			}
			sql = "delete from recycle_bin where id = ?";
			pst = conn.prepareStatement(sql);
			pst.setInt(1, id);
			pst.executeUpdate();
			conn.commit();
			pst.close();
		} catch (Exception e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return viewtext;
	}

	@Override
	@Deprecated
	public ArrayList<Integer> getAllDocumentsIDS(int docType, Set<String> complexUserID, String absoluteUserID,
			int start, int end) throws DocumentException, DocumentAccessException {
		ArrayList<Integer> documents = new ArrayList<Integer>();
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			switch (docType) {
			case DOCTYPE_MAIN:
				String sql = "select MAINDOCS.DOCID from MAINDOCS JOIN READERS_MAINDOCS on(MAINDOCS.DOCID=READERS_MAINDOCS.DOCID)"
						+ "and READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ getPageSQLPiece(start, end);

				ResultSet rs = s.executeQuery(sql);
				while (rs.next()) {
					documents.add(rs.getInt("DOCID"));
				}
				break;
			case DOCTYPE_TASK:
				sql = "select TASKS.DOCID from TASKS,READERS_TASKS where TASKS.DOCID=READERS_TASKS.DOCID "
						+ "and READERS_TASKS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ getPageSQLPiece(start, end);
				rs = s.executeQuery(sql);
				while (rs.next()) {
					documents.add(rs.getInt("DOCID"));
				}
				break;
			case DOCTYPE_EXECUTION:
				sql = "select EXECUTIONS.DOCID from EXECUTIONS JOIN READERS_EXECUTIONS on(EXECUTIONS.DOCID=READERS_EXECUTIONS.DOCID)"
						+ "and READERS_EXECUTIONS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ getPageSQLPiece(start, end);
				rs = s.executeQuery(sql);
				while (rs.next()) {
					documents.add(rs.getInt("DOCID"));
				}
				break;
			case DOCTYPE_PROJECT:
				sql = "select PROJECTS.DOCID from PROJECTS, READERS_PROJECTS where PROJECTS.DOCID=READERS_PROJECTS.DOCID "
						+ "and READERS_PROJECTS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ getPageSQLPiece(start, end);
				rs = s.executeQuery(sql);
				while (rs.next()) {
					documents.add(rs.getInt("DOCID"));
				}
				break;
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return documents;
	}

	@Override
	@Deprecated
	public ArrayList<Integer> getAllDocumentsIDsByCondition(String query, int docType, Set<String> complexUserID,
			String absoluteUserID) {

		FormulaBlocks preparedQueryFormula;
		ArrayList<Integer> documents = new ArrayList<Integer>();
		String sql = "";

		QueryType doctype = QueryType.DOCUMENT;
		switch (docType) {
		case DOCTYPE_MAIN:
			doctype = QueryType.DOCUMENT;
			preparedQueryFormula = new FormulaBlocks(query, doctype);
			QueryFormula queryFormula = new QueryFormula("", preparedQueryFormula);
			sql = queryFormula.getIDsSQL(complexUserID);
			break;
		case DOCTYPE_GLOSSARY:
			doctype = QueryType.GLOSSARY;
			preparedQueryFormula = new FormulaBlocks(query, doctype);
			GlossaryQueryFormula glossaryQueryFormula = new GlossaryQueryFormula("", preparedQueryFormula);
			sql = glossaryQueryFormula.getSQL(complexUserID);
			break;
		}

		Connection conn = dbPool.getConnection();

		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();

			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				documents.add(rs.getInt("DOCID"));
			}

			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return documents;

	}

	private String getPageSQLPiece(int offset, int pageSize) {
		String sql = "";
		if (pageSize > 0) {
			sql = " LIMIT " + pageSize + " OFFSET " + offset;
		}
		return sql;

	}

	@Override
	@Deprecated
	public int getAllDocumentsCount(int docType, Set<String> complexUserID, String absoluteUserID) {
		int count = 0;
		String sql = "";
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			switch (docType) {
			case DOCTYPE_MAIN:
				sql = "select count(MAINDOCS.docid) from MAINDOCS,READERS_MAINDOCS where MAINDOCS.DOCID = READERS_MAINDOCS.DOCID "
						+ "and READERS_MAINDOCS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";
				break;
			case DOCTYPE_TASK:
				sql = "select count(tasks.docid) from TASKS,READERS_TASKS where TASKS.DOCID = READERS_TASKS.DOCID "
						+ "and READERS_TASKS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";
				break;
			case DOCTYPE_EXECUTION:
				sql = "select count(EXECUTIONS.docid) from EXECUTIONS,READERS_EXECUTIONS where EXECUTIONS.DOCID = READERS_EXECUTIONS.DOCID "
						+ "and READERS_EXECUTIONS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";
				break;
			case DOCTYPE_PROJECT:
				sql = "select count(PROJECTS.docid) from PROJECTS,READERS_PROJECTS where PROJECTS.DOCID = READERS_PROJECTS.DOCID "
						+ "and READERS_PROJECTS.USERNAME IN (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")";
				break;

			}
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				count = rs.getInt(1);
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return count;
	}

	@Override
	@Deprecated
	public int getDocsCountByCondition(IQueryFormula blocks, Set<String> complexUserID, String absoluteUserID) {
		int count = 0;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = blocks.getSQLCount(complexUserID);
			// QueryCache qc = CachePool.getQueryCache(sql, userName);
			// if (qc == null){
			// System.out.println(sql);
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				count = rs.getInt(1);
			}
			s.close();
			rs.close();
			// CachePool.update(count, sql, absoluteUserID,
			// CacheInitiatorType.QUERY);
			// }else{
			// count = qc.getIntContent();
			// }

			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);

		}
		return count;
	}

	@Override
	@Deprecated
	public int getDocsCountByCondition(String sql, Set<String> complexUserID, String absoluteUserID) {
		int count = 0;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				count = rs.getInt(1);
			}
			s.close();
			rs.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);

		}
		return count;
	}

	@Override
	public StringBuffer getDocsByCondition(IQueryFormula blocks, Set<String> complexUserID, String absoluteUserID,
			int offset, int pageSize, String fieldsCond, Set<DocID> toExpandResponses, Set<String> toExpandCategory,
			TagPublicationFormatType publishAs, int page) throws DocumentException {
		StringBuffer xmlContent = new StringBuffer(10000);

		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = blocks.getSQL(complexUserID) + " LIMIT " + pageSize + " OFFSET " + offset;
			ResultSet rs = s.executeQuery(sql);
			if (blocks.isGroupBy()) {
				SourceSupplier ss = new SourceSupplier(env);
				while (rs.next()) {
					String categoryID = rs.getString(1);
					if (categoryID != null) {
						int groupCount = rs.getInt(2);
						String categoryVal[] = { categoryID };
						String viewText = ss.publishAs(publishAs, categoryVal).get(0)[0];
						xmlContent.append("<entry  doctype=\"" + CATEGORY + "\" count=\"" + groupCount + "\" "
								+ " categoryid=\"" + categoryID + "\" " + " docid=\"" + categoryID + "\" "
								+ "url=\"Provider?type=view&amp;id=" + blocks.getQueryID() + "&amp;command=expand`"
								+ categoryID + "\" ><viewtext>" + XMLUtil.getAsTagValue(viewText) + "</viewtext>");

						for (String category : toExpandCategory) {
							if (categoryID.equalsIgnoreCase(category)) {
								StringBuffer categoryValue = getGetOneCategory(complexUserID, absoluteUserID,
										blocks.getGroupCondition(category), fieldsCond, toExpandResponses, page);
								int catID;
								String catName;
								try {
									catID = Integer.valueOf(category);
									catName = this.getGlossaryCustomFieldValueByID(catID, "name");
								} catch (NumberFormatException e) {
									catID = 0;
									catName = "";
								}

								xmlContent.append("<category id=\"" + catID + "\" name=\"" + catName + "\">"
										+ categoryValue + "</category>");
								break;
							}
						}
					} else {
						xmlContent.append("<entry  doctype=\"" + DOCTYPE_UNKNOWN + "\" count=\"0\" "
								+ " categoryid=\"null\"><viewtext></viewtext>");
					}
					xmlContent.append("</entry>");
				}

			} else {
				while (rs.next()) {
					xmlContent.append(getDocumentEntry(conn, complexUserID, absoluteUserID, rs, fieldsCond,
							toExpandResponses, page));
				}
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
		return xmlContent;
	}

	@Override
	public StringBuffer getDocsByCondition(String sql, Set<String> complexUserID, String absoluteUserID,
			String fieldsCond, Set<DocID> toExpandResponses, Set<String> toExpandCategory,
			TagPublicationFormatType publishAs, int page) throws DocumentException {
		StringBuffer xmlContent = new StringBuffer(10000);

		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				xmlContent.append(
						getDocumentEntry(conn, complexUserID, absoluteUserID, rs, fieldsCond, toExpandResponses, page));
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
		return xmlContent;
	}

	public StringBuffer getGetOneCategory(Set<String> complexUserID, String absoluteUserID, String addCondition,
			String fieldsCond, Set<DocID> toExpandResponses, int page) {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

			String sql = "SELECT DISTINCT " + maindocFields
					+ " FROM MAINDOCS, READERS_MAINDOCS rm WHERE rm.docid = MAINDOCS.DOCID and " + "rm.USERNAME IN ("
					+ DatabaseUtil.prepareListToQuery(complexUserID) + ")" + addCondition
					+ " ORDER BY MAINDOCS.regdate";

			ResultSet rs = statement.executeQuery(sql);

			while (rs.next()) {
				xmlContent.append(
						getDocumentEntry(conn, complexUserID, absoluteUserID, rs, fieldsCond, toExpandResponses, page));
			}

			rs.close();
			statement.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} catch (DocumentException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return xmlContent;
	}

	@Override
	public String getDocumentEntry(Connection conn, Set<String> complexUserID, String absoluteUserID, ResultSet rs,
			String fieldsCond, Set<DocID> toExpandResponses, int page) throws SQLException, DocumentException {
		String customFieldsValue = "";
		int docID = rs.getInt("DOCID");
		int docType = rs.getInt("DOCTYPE");
		String id = rs.getString("DDBID");
		// int canDelete = 0;

		Statement sFields = conn.createStatement();
		if (docType == Const.DOCTYPE_MAIN && !fieldsCond.equalsIgnoreCase("")) {
			String addSQL = "select * from CUSTOM_FIELDS where CUSTOM_FIELDS.DOCID = " + docID + fieldsCond;
			ResultSet rsFields = sFields.executeQuery(addSQL);
			while (rsFields.next()) {
				String name = rsFields.getString("NAME");
				switch (rsFields.getInt("TYPE")) {
				case TEXT:
					customFieldsValue += "<" + name + ">" + XMLUtil.getAsTagValue(rsFields.getString("VALUE")) + "</"
							+ name + ">";
					break;
				case NUMBERS:
					customFieldsValue += "<" + name + ">" + rsFields.getInt("VALUEASNUMBER") + "</" + name + ">";
					break;
				case DATETIMES:
					customFieldsValue += "<" + name + ">"
							+ Database.dateTimeFormat.format(rsFields.getTimestamp("VALUEASDATE")) + "</" + name + ">";
					break;
				case DATE:
					customFieldsValue += "<" + name + ">"
							+ Database.dateFormat.format(rsFields.getTimestamp("VALUEASDATE")) + "</" + name + ">";
					break;
				case GLOSSARY:
					String valueAsGlossary, attr = "";
					Glossaries g = new Glossaries(env);
					int gVal = rsFields.getInt("VALUEASGLOSSARY");
					Glossary doc = g.getGlossaryDocumentByID(gVal, false, complexUserID, absoluteUserID);
					if (doc != null) {
						valueAsGlossary = doc.getViewText();
					} else {
						valueAsGlossary = Integer.toString(gVal);
						attr = " error=\"glossary not found\" ";
					}
					customFieldsValue += "<" + name + attr + ">" + XMLUtil.getAsTagValue(valueAsGlossary) + "</" + name
							+ ">";
					break;
				}
			}
		}
		boolean isResponding = false;
		if (respUsed && hasResponse(conn, docID, docType, complexUserID, null)) {
			customFieldsValue += "<hasresponse>true</hasresponse>";
			isResponding = true;
		}

		Employer emp = this.getStructure().getAppUser(absoluteUserID);

		StringBuffer value = new StringBuffer(
				"<entry isread=\"" + usersActivity.isRead(conn, docID, docType, absoluteUserID) + "\" " + "hasattach=\""
						+ Integer.toString(rs.getInt("HAS_ATTACHMENT")) + "\"  id=\"" + id + "\" doctype=\"" + docType
						+ "\"  " + "docid=\"" + docID + "\" favourites=\"" + isFavourites(conn, docID, docType, emp)
						+ "\" " + "url=\"Provider?type=edit&amp;element=" + resolveElement(docType) + "&amp;id="
						+ rs.getString("FORM") + "&amp;key=" + docID + "&amp;docid=" + rs.getString("DDBID")
						+ "&amp;page=" + page + "\"" + ">" + getViewContent(rs) + customFieldsValue);

		sFields.close();

		if (isResponding && toExpandResponses.size() > 0) {
			for (DocID doc : toExpandResponses) {
				if (doc.id == docID && doc.type == DOCTYPE_MAIN) {
					DocumentCollection responses = getDescendants(docID, DOCTYPE_MAIN, null, 1, complexUserID,
							absoluteUserID);
					if (responses.count > 0) {
						value.append("<responses>" + responses.xmlContent + "</responses>");
					}
				}
			}
		}
		return value.append("</entry>").toString();
	}

	/**
	 * @throws ComplexObjectException
	 * @deprecated*
	 */
	@Deprecated
	@Override
	public ArrayList<BaseDocument> getDocumentsByCondition(String form, String query, Set<String> complexUserID,
			String absoluteUserID) throws DocumentException, DocumentAccessException, QueryFormulaParserException,
					ComplexObjectException {
		FormulaBlocks preparedQueryFormula = new FormulaBlocks(query, QueryType.DOCUMENT);
		QueryFormula queryFormula;
		if (form.equalsIgnoreCase("workdocprj") || form.equalsIgnoreCase("outdocprj")) {
			queryFormula = new ProjectQueryFormula("", preparedQueryFormula);
		} else {
			queryFormula = new QueryFormula("", preparedQueryFormula);
		}
		return getDocumentsByCondition(queryFormula, complexUserID, absoluteUserID, 0, 0);
	}

	@Override
	public ArrayList<BaseDocument> getDocumentsByCondition(String query, Set<String> complexUserID,
			String absoluteUserID, int limit, int offset) throws DocumentException, DocumentAccessException,
					QueryFormulaParserException, ComplexObjectException {
		FormulaBlocks preparedQueryFormula = new FormulaBlocks(query, QueryType.DOCUMENT);
		QueryFormula queryFormula = new QueryFormula("", preparedQueryFormula);
		return getDocumentsByCondition(queryFormula, complexUserID, absoluteUserID, limit,
				this.calcStartEntry(offset, limit));
	}

	@Override
	public int getDocumentsCountByCondition(String query, Set<String> complexUserID, String absoluteUserID)
			throws DocumentException, DocumentAccessException, QueryFormulaParserException {
		FormulaBlocks preparedQueryFormula = new FormulaBlocks(query, QueryType.DOCUMENT);
		QueryFormula queryFormula = new QueryFormula("", preparedQueryFormula);
		return getDocsCountByCondition(queryFormula, complexUserID, absoluteUserID);
	}

	public ArrayList<BaseDocument> getDocumentsByCondition(IQueryFormula blocks, Set<String> complexUserID,
			String absoluteUserID, int limit, int offset, Set<DocID> expandedThread)
					throws DocumentException, DocumentAccessException, ComplexObjectException {
		ArrayList<BaseDocument> docs = new ArrayList<BaseDocument>();
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = blocks.getSQL(complexUserID, limit, offset);
			ResultSet rs = s.executeQuery(sql);
			// System.out.println(sql);
			while (rs.next()) {
				int docID = rs.getInt("DOCID");
				int docType = rs.getInt("DOCTYPE");
				Document doc = null;
				switch (docType) {
				case DOCTYPE_MAIN:
					doc = getMainDocumentByID(docID, complexUserID, absoluteUserID);
					break;
				case DOCTYPE_PROJECT:
					doc = this.getProjects().getProjectByID(docID, complexUserID, absoluteUserID);
					break;
				case DOCTYPE_TASK:
					doc = this.getTasks().getTaskByID(docID, complexUserID, absoluteUserID);
					break;
				case DOCTYPE_EXECUTION:
					doc = this.getExecutions().getExecutionByID(docID, complexUserID, absoluteUserID);
					break;
				}
				DocID docid = new DocID(docID, docType);
				if ("true".equalsIgnoreCase(doc.hasResponse(complexUserID, absoluteUserID))
						&& expandedThread.contains(docid)) {
					doc.responses = this.getDescendants(docID, docType, null, 0, complexUserID, absoluteUserID).col;
				}
				docs.add(doc);
			}
			s.close();
			rs.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return docs;
	}

	@Override
	public ArrayList<BaseDocument> getDocumentsByCondition(IQueryFormula blocks, Set<String> complexUserID,
			String absoluteUserID, int limit, int offset)
					throws DocumentException, DocumentAccessException, ComplexObjectException {
		ArrayList<BaseDocument> docs = new ArrayList<BaseDocument>();
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = blocks.getSQL(complexUserID, limit, offset);
			ResultSet rs = s.executeQuery(sql);
			// System.out.println(sql);
			while (rs.next()) {
				int docID = rs.getInt("DOCID");
				int docType = rs.getInt("DOCTYPE");
				Document doc = null;
				switch (docType) {
				case DOCTYPE_MAIN:
					doc = getMainDocumentByID(docID, complexUserID, absoluteUserID);
					break;
				case DOCTYPE_PROJECT:
					doc = this.getProjects().getProjectByID(docID, complexUserID, absoluteUserID);
					break;
				case DOCTYPE_TASK:
					doc = this.getTasks().getTaskByID(docID, complexUserID, absoluteUserID);
					break;
				case DOCTYPE_EXECUTION:
					doc = this.getExecutions().getExecutionByID(docID, complexUserID, absoluteUserID);
					break;
				}

				docs.add(doc);
			}
			s.close();
			rs.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return docs;
	}

	@Override
	public boolean hasResponse(Connection conn, int docID, int docType, Set<String> complexUserID,
			String absoluteUserID) {
		try {
			conn.setAutoCommit(false);
			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "select m.docid, m.doctype, m.form from maindocs as m " + " inner join readers_maindocs as rm "
					+ "     on m.docid = rm.docid " + " where m.parentdocid = " + docID + " and m.parentdoctype = "
					+ docType + " and rm.username in (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
					+ " and m.form != 'discussion' " + " union all "
					+ " select t.docid, t.doctype, t.form from tasks as t " + " inner join readers_tasks as rt "
					+ "     on t.docid = rt.docid " + " where t.parentdocid = " + docID + " and t.parentdoctype = "
					+ docType + " and rt.username in (" + DatabaseUtil.prepareListToQuery(complexUserID) + ")"
					+ " union all " + " select e.docid, e.doctype, e.form from executions as e "
					+ " inner join readers_executions as re " + "     on e.docid = re.docid "
					+ " where e.parentdocid = " + docID + " and e.parentdoctype = " + docType + " and re.username in ("
					+ DatabaseUtil.prepareListToQuery(complexUserID) + ")";
			ResultSet rs = st.executeQuery(sql);
			if (rs.next()) {
				st.close();
				return true;
			}
			rs.close();
			st.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		}
		return false;
	}

	@Override
	public boolean hasResponse(int docID, int docType, Set<String> complexUserID, String absoluteUserID) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "select m.docid, m.doctype, m.form from maindocs as m, readers_maindocs as rm "
					+ " where m.parentdocid = " + docID + " and m.parentdoctype = " + docType + " and rm.username in ("
					+ DatabaseUtil.prepareListToQuery(complexUserID) + ")" + " and m.form != 'discussion' "
					+ " union all " + " select t.docid, t.doctype, t.form from tasks as t, readers_tasks as rt"
					+ " where t.parentdocid = " + docID + " and t.parentdoctype = " + docType + " and rt.username in ("
					+ DatabaseUtil.prepareListToQuery(complexUserID) + ")" + " union all "
					+ " select e.docid, e.doctype, e.form from executions as e, readers_executions as re"
					+ " where e.parentdocid = " + docID + " and e.parentdoctype = " + docType + " and re.username in ("
					+ DatabaseUtil.prepareListToQuery(complexUserID) + ")";
			ResultSet rs = st.executeQuery(sql);
			if (rs.next()) {
				st.close();
				return true;
			}
			rs.close();
			st.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return false;
	}

	public boolean checkAuthors(int docType) {
		switch (docType) {
		case DOCTYPE_MAIN:
		case DOCTYPE_TASK:
		case DOCTYPE_EXECUTION:
		case DOCTYPE_PROJECT:
			return true;
		default:
			return false;
		}
	}

	@Override
	public void deleteDocument(String ddbId, boolean completely, User user)
			throws DocumentException, DocumentAccessException, SQLException, DatabasePoolException,
			InstantiationException, IllegalAccessException, ClassNotFoundException, ComplexObjectException {

		BaseDocument doc = getDocumentByDdbID(ddbId, user.getAllUserGroups(), user.getUserID());
		if (doc == null) {
			throw new DocumentAccessException(ExceptionType.DOCUMENT_READ_RESTRICTED, user.getUserID());
		}
		Connection conn = dbPool.getConnection();
		try {
			String sql;
			Statement statement = conn.createStatement();

			int docType = doc.docType;
			if (docType != DOCTYPE_GLOSSARY && docType != DOCTYPE_GROUP) {
				if (!doc.hasEditor(user.getAllUserGroups())) {
					throw new DocumentAccessException(ExceptionType.DOCUMENT_DELETE_RESTRICTED, user.getUserID());
				}
			} else {
				if (this.getGlossaries().hasResponse(conn, doc.getDocID(), doc.docType, user.getAllUserGroups(),
						user.getUserID()) || this.getGlossaries().inUse(conn, doc.getDocID(), doc.docType)) {
					throw new DocumentAccessException(ExceptionType.DELETING_RESTRICTED_CAUSED_RELATED_DOCUMENTS,
							user.getUserID());
				}
			}

			if (docType == DOCTYPE_POST) {
				sql = "DELETE FROM FORUM_TREE_PATH WHERE DESCENDANT IN (SELECT DESCENDANT FROM FORUM_TREE_PATH WHERE ANCESTOR = "
						+ doc.getDocID() + ")";
				statement.addBatch(sql);
				sql = "delete from posts where docid in (select docid from posts except (select ancestor from forum_tree_path union select descendant from forum_tree_path))";
				statement.addBatch(sql);
			}
			if (docType == DOCTYPE_TOPIC) {
				sql = "DELETE FROM FORUM_TREE_PATH WHERE DESCENDANT IN (SELECT DESCENDANT FROM FORUM_TREE_PATH WHERE ANCESTOR IN (SELECT DOCID FROM POSTS WHERE PARENTDOCID = "
						+ doc.getDocID() + " AND PARENTDOCTYPE = " + Const.DOCTYPE_TOPIC + "))";
				statement.addBatch(sql);
				sql = "delete from posts where docid in (select docid from posts except (select ancestor from forum_tree_path union select descendant from forum_tree_path))";
				statement.addBatch(sql);
				sql = "delete from topics where docid = " + doc.getDocID();
				statement.addBatch(sql);
			}
			ArrayList<BaseDocument> responses = doc.getDescendantsArray(doc.getDocID(), doc.docType, sysGroupAsSet,
					sysUser);
			if (!completely) {
				for (BaseDocument resp : responses) {
					if (hasBlobs(resp)) {
						fillBlobsContent(resp);
					}
					sql = "DELETE FROM " + DatabaseUtil.getMainTableName(resp.docType) + " WHERE "
							+ DatabaseUtil.getPrimaryKeyColumnName(resp.docType) + " = "
							+ Integer.toString(resp.getDocID());
					statement.addBatch(sql);
				}
				doc.responses = responses;
				if (hasBlobs(doc)) {
					fillBlobsContent(doc);
				}
			}

			for (BaseDocument resp : responses) {
				sql = "DELETE FROM " + DatabaseUtil.getMainTableName(resp.docType) + " WHERE "
						+ DatabaseUtil.getPrimaryKeyColumnName(resp.docType) + " = "
						+ Integer.toString(resp.getDocID());
				statement.addBatch(sql);
			}

			if (docType == DOCTYPE_GLOSSARY) {
				sql = "DELETE FROM GLOSSARY_TREE_PATH WHERE ANCESTOR = " + doc.getDocID() + " OR DESCENDANT = "
						+ doc.getDocID();
				statement.addBatch(sql);
			}

			if (docType == DOCTYPE_EMPLOYER) {
				ISystemDatabase sysdb = new SystemDatabase();
				Employer emp = this.getStructure().getEmployer(doc.getDocID(), new User(Const.sysUser, env));
				sysdb.removeAppEntry(emp.getUser());
				sysdb.deleteUser(emp.getUser().docID);
			}

			sql = "DELETE FROM " + DatabaseUtil.getMainTableName(doc.docType) + " WHERE "
					+ DatabaseUtil.getPrimaryKeyColumnName(doc.docType) + " = " + Integer.toString(doc.getDocID());
			statement.addBatch(sql);
			statement.executeBatch();
			statement.close();
			conn.commit();

		} finally {
			dbPool.returnConnection(conn);
		}

		IUsersActivity ua = getUserActivity();
		if (!completely) {
			ua.postDelete(doc, user);
		} else {
			ua.postCompletelyDelete(doc, user);
		}
	}

	@Override
	public void deleteDocument(int docType, int docID, User user, boolean completely)
			throws DocumentException, DocumentAccessException, SQLException, DatabasePoolException,
			InstantiationException, IllegalAccessException, ClassNotFoundException, ComplexObjectException {
		@Deprecated
		boolean flushCache = false;
		BaseDocument doc = getDocumentByComplexID(docType, docID);
		if (doc == null) {
			throw new DocumentAccessException(ExceptionType.DOCUMENT_READ_RESTRICTED, user.getUserID());
		}

		Connection conn = dbPool.getConnection();
		try {
			boolean checkAuthors = checkAuthors(docType);
			String tableName = DatabaseUtil.getMainTableName(docType);
			String columnName = DatabaseUtil.getPrimaryKeyColumnName(docType);
			String sql;
			Statement statement = conn.createStatement();

			if (checkAuthors && !doc.hasEditor(user.getAllUserGroups())) {
				throw new DocumentAccessException(ExceptionType.DOCUMENT_DELETE_RESTRICTED, user.getUserID());
			}
			if (docType == DOCTYPE_GLOSSARY) {
				if (this.getGlossaries().hasResponse(conn, docID, docID, user.getAllUserGroups(), user.getUserID())) {
					throw new DocumentAccessException(ExceptionType.DELETING_RESTRICTED_CAUSED_RELATED_DOCUMENTS,
							user.getUserID());
				}
			}
			if (docType == DOCTYPE_POST) {
				sql = "DELETE FROM FORUM_TREE_PATH WHERE DESCENDANT IN (SELECT DESCENDANT FROM FORUM_TREE_PATH WHERE ANCESTOR = "
						+ docID + ")";
				statement.addBatch(sql);
				sql = "delete from posts where docid in (select docid from posts except (select ancestor from forum_tree_path union select descendant from forum_tree_path))";
				statement.addBatch(sql);
			}
			if (docType == DOCTYPE_TOPIC) {
				sql = "DELETE FROM FORUM_TREE_PATH WHERE DESCENDANT IN (SELECT DESCENDANT FROM FORUM_TREE_PATH WHERE ANCESTOR IN (SELECT DOCID FROM POSTS WHERE PARENTDOCID = "
						+ docID + " AND PARENTDOCTYPE = " + Const.DOCTYPE_TOPIC + "))";
				statement.addBatch(sql);
				sql = "delete from posts where docid in (select docid from posts except (select ancestor from forum_tree_path union select descendant from forum_tree_path))";
				statement.addBatch(sql);
				sql = "delete from topics where docid = " + docID;
				statement.addBatch(sql);
			}
			if (!completely) {
				ArrayList<BaseDocument> responses = doc.getDescendants(docID, docType, sysGroupAsSet, sysUser).col;
				for (BaseDocument resp : responses) {
					if (hasBlobs(resp)) {
						fillBlobsContent(resp);
					}
					tableName = DatabaseUtil.getMainTableName(resp.docType);
					columnName = DatabaseUtil.getPrimaryKeyColumnName(resp.docType);
					sql = "DELETE FROM " + tableName + " WHERE " + columnName + " = "
							+ Integer.toString(resp.getDocID());
					statement.addBatch(sql);
				}
				doc.responses = responses;
				if (hasBlobs(doc)) {
					fillBlobsContent(doc);
				}
			}
			if (docType == DOCTYPE_GLOSSARY) {
				flushCache = true;
				sql = "DELETE FROM GLOSSARY_TREE_PATH WHERE ANCESTOR = " + docID + " OR DESCENDANT = " + docID;
				statement.addBatch(sql);
			}
			if (docType == DOCTYPE_EMPLOYER) {
				ISystemDatabase sysdb = new SystemDatabase();
				Employer emp = this.getStructure().getEmployer(docID, new User(Const.sysUser, env));
				sysdb.removeAppEntry(emp.getUser());
				sysdb.deleteUser(emp.getUser().docID);
			}
			sql = "DELETE FROM " + tableName + " WHERE " + columnName + " = " + Integer.toString(docID);
			statement.addBatch(sql);
			statement.executeBatch();
			statement.close();
			conn.commit();
		} finally {
			dbPool.returnConnection(conn);
		}

		IUsersActivity ua = getUserActivity();
		if (!completely) {
			ua.postDelete(doc, user);
		} else {
			ua.postCompletelyDelete(doc, user);
		}
	}

	public void recoverDocument(BaseDocument recoverDoc, int recEntryID)
			throws DocumentAccessException, DocumentException, LicenseException, ComplexObjectException {
		boolean canBeRecovered = false;

		if (recoverDoc.parentDocID == 0 && recoverDoc.parentDocType == DOCTYPE_UNKNOWN) {
			canBeRecovered = true;
		} else {
			canBeRecovered = hasDocumentByComplexID(recoverDoc.parentDocID, recoverDoc.parentDocType);
		}

		if (canBeRecovered) {
			recoverDoc.setNewDoc(true);
			recoverDoc.setDdbID(Util.generateRandomAsText());
			recoverDoc.db = this;
			recoverDoc.setEnvironment(this.env);
			recoverDoc.setStructure(new Structure(this, structDbPool));
			recoverDoc.addNumberField("recID", recEntryID);
			if (recoverDoc.docType == Const.DOCTYPE_TASK) {
				recoverDoc = recoverDoc;
			} else if (recoverDoc.docType == Const.DOCTYPE_EXECUTION) {
				recoverDoc = recoverDoc;
			} else if (recoverDoc.docType == Const.DOCTYPE_PROJECT) {
				recoverDoc = recoverDoc;
			} else if (recoverDoc.docType == Const.DOCTYPE_GLOSSARY) {
				recoverDoc = recoverDoc;
			} else if (recoverDoc.docType == Const.DOCTYPE_ORGANIZATION) {
				recoverDoc = recoverDoc;
			} else if (recoverDoc.docType == Const.DOCTYPE_DEPARTMENT) {
				recoverDoc = recoverDoc;
			} else if (recoverDoc.docType == Const.DOCTYPE_EMPLOYER) {
				recoverDoc = recoverDoc;
			} else if (recoverDoc.docType == Const.DOCTYPE_GROUP) {
				recoverDoc = recoverDoc;
			}
			int res = recoverDoc.save(new User(Const.sysUser, env));
			if (res <= 0) {
				throw new DocumentException(DocumentExceptionType.ERROR_RECOVERY_PROCESS);
			}
		} else {
			throw new DocumentException(DocumentExceptionType.ERROR_RECOVERY_PROCESS);
		}
	}

	@Override
	public boolean unDeleteDocument(String id, User user) throws DocumentAccessException, ComplexObjectException {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String sql = "SELECT * FROM RECYCLE_BIN WHERE ID = " + id;
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				ByteArrayInputStream bais = new ByteArrayInputStream(rs.getBytes("VALUE"));
				ObjectInputStream ois = new ObjectInputStream(bais);
				BaseDocument doc = (BaseDocument) ois.readObject();
				int activity_id = rs.getInt("AID");
				recoverDocument(doc, activity_id);
				for (BaseDocument response : doc.responses) {
					recoverDocument(response, activity_id);
				}
				IUsersActivity ua = getUserActivity();
				ua.postUndelete(doc, user);
			}
			rs.close();
			s.close();
		} catch (SQLException sqle) {
			DatabaseUtil.errorPrint(dbID, sqle);
			return false;
		} catch (IOException ioe) {
			AppEnv.logger.errorLogEntry(ioe);
			return false;
		} catch (LicenseException le) {
			AppEnv.logger.errorLogEntry(le);
			return false;
		} catch (ClassNotFoundException cnfe) {
			AppEnv.logger.errorLogEntry(cnfe);
			return false;
		} catch (DocumentException de) {
			AppEnv.logger.errorLogEntry(de);
			return false;
		} finally {
			dbPool.returnConnection(conn);
		}
		return true;
	}

	@Override
	public boolean unDeleteDocument(int id, User user) throws DocumentAccessException, ComplexObjectException {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String sql = "SELECT * FROM RECYCLE_BIN WHERE ID = " + id;
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				ByteArrayInputStream bais = new ByteArrayInputStream(rs.getBytes("VALUE"));
				ObjectInputStream ois = new ObjectInputStream(bais);
				BaseDocument doc = (BaseDocument) ois.readObject();
				int activity_id = rs.getInt("AID");
				recoverDocument(doc, activity_id);
				for (BaseDocument response : doc.responses) {
					recoverDocument(response, activity_id);
				}
				IUsersActivity ua = getUserActivity();
				ua.postUndelete(doc, user);
			}
			rs.close();
			s.close();
		} catch (SQLException sqle) {
			DatabaseUtil.errorPrint(dbID, sqle);
			return false;
		} catch (IOException ioe) {
			AppEnv.logger.errorLogEntry(ioe);
			return false;
		} catch (LicenseException le) {
			AppEnv.logger.errorLogEntry(le);
			return false;
		} catch (ClassNotFoundException cnfe) {
			AppEnv.logger.errorLogEntry(cnfe);
			return false;
		} catch (DocumentException de) {
			AppEnv.logger.errorLogEntry(de);
			return false;
		} finally {
			dbPool.returnConnection(conn);
		}
		return true;
	}

	@Override
	public int getRegNum(String key) {
		int lastNum = 1;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String sql = "select * from COUNTERS where KEYS='" + key + "'";
			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			String keyValue = "";
			if (rs.next()) {
				keyValue = rs.getString("KEYS");
				lastNum = rs.getInt("LASTNUM");
			}
			if (keyValue != "") {
				lastNum++;
			}
			rs.close();
			pst.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return lastNum;
	}

	@Override
	public int postRegNum(int num, String key) {
		int lastNum = 0;
		Connection conn = dbPool.getConnection();
		try {
			// conn.close();
			conn.setAutoCommit(false);
			String sql = "select *from COUNTERS where KEYS='" + key + "'";
			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			String keyValue = "";
			if (rs.next()) {
				keyValue = rs.getString("KEYS");
				lastNum = rs.getInt("LASTNUM");
			}
			rs.close();
			String getNum = null;
			conn.setAutoCommit(false);
			if (keyValue.equals("")) {
				getNum = "insert into COUNTERS(KEYS, LASTNUM)values(?,?)";
				pst = conn.prepareStatement(getNum);
				pst.setString(1, key);
				pst.setInt(2, num);
			} else {
				getNum = "update COUNTERS set LASTNUM = ? where KEYS = ? ";
				pst = conn.prepareStatement(getNum);
				lastNum++;
				pst.setInt(1, num);
				pst.setString(2, key);
			}
			pst.executeUpdate();
			conn.commit();
			pst.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
			lastNum = -1;
		} finally {
			dbPool.returnConnection(conn);
		}
		return lastNum;
	}

	@Override
	public StringBuffer getPatches() {
		StringBuffer xmlContent = new StringBuffer();
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT * FROM PATCHES";
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				String processedTime = rs.getString("PROCESSEDTIME");
				String hashCode = rs.getString("HASH");
				String description = rs.getString("DESCRIPTION");
				String name = rs.getString("NAME");
				xmlContent.append("<entry doctype=\"PATCH\" "
						+ XMLUtil.getAsAttribute("viewtext",
								"time=" + processedTime + ", hash=" + hashCode + ", descritpion=" + description
										+ ", name=" + name)
						+ ">" + "<viewtext>"
						+ XMLUtil.getAsTagValue("time=" + processedTime + ", hash=" + hashCode + ", description="
								+ description + ", name=" + name)
						+ "</viewtext>" + "<processedtime>" + processedTime + "</processedtime><hash>" + hashCode
						+ "</hash><name>" + name + "</name><description>" + XMLUtil.getAsTagValue(description)
						+ "</description></entry>");
			}
			rs.close();
			statement.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return xmlContent;
	}

	@Override
	public StringBuffer getCounters() {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT * FROM COUNTERS ORDER BY KEYS ASC";
			ResultSet rs = statement.executeQuery(sql);

			while (rs.next()) {
				String keys = rs.getString("KEYS");
				String lastNum = Integer.toString(rs.getInt("LASTNUM"));
				xmlContent.append("<entry doctype=\"COUNTER\" "
						+ XMLUtil.getAsAttribute("viewtext", "key=" + keys + ", last number=" + lastNum) + ">"
						+ "<viewtext>" + XMLUtil.getAsTagValue("key=" + keys + ", last number=" + lastNum)
						+ "</viewtext>" + "<keys>" + keys + "</keys><lastnum>" + lastNum + "</lastnum></entry>");

			}

			rs.close();
			statement.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return xmlContent;
	}

	public void showUserTable() throws SQLException {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String sql = "select * from AUTHORS_MAINDOCS";
			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				System.out.println(rs.getString("USERNAME"));
				System.out.println(rs.getInt("DOCID"));

			}
			dbPool.returnConnection(conn);
			pst.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	public static String bytesToHex(byte[] bytes, int max) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < bytes.length && i < max; i++) {
			buffer.append(Integer.toHexString(bytes[i] & 0xFF));
		}
		return buffer.toString().toUpperCase();
	}

	@Override
	public boolean hasDocumentByComplexID(int docID, int docType) {
		Connection conn = dbPool.getConnection();
		int docIDFromDB = 0;
		try {
			conn.setAutoCommit(false);
			switch (docType) {
			case DOCTYPE_MAIN:
				String sql = "select * from MAINDOCS where DOCID = " + docID + " and DOCTYPE = " + docType;
				PreparedStatement pst = conn.prepareStatement(sql);
				ResultSet rs = pst.executeQuery();
				while (rs.next()) {
					docIDFromDB = rs.getInt("DOCID");
				}
				pst.close();
				break;
			case DOCTYPE_TASK:
				sql = "select * from TASKS where DOCID = " + docID + " and DOCTYPE = " + docType;
				pst = conn.prepareStatement(sql);
				rs = pst.executeQuery();
				while (rs.next()) {
					docIDFromDB = rs.getInt("DOCID");
				}
				pst.close();
				break;
			case DOCTYPE_EXECUTION:
				sql = "select * from EXECUTIONS where DOCID = " + docID + " and DOCTYPE = " + docType;
				pst = conn.prepareStatement(sql);
				rs = pst.executeQuery();
				while (rs.next()) {
					docIDFromDB = rs.getInt("DOCID");
				}
				pst.close();
				break;
			case DOCTYPE_PROJECT:
				sql = "select * from PROJECTS where DOCID = " + docID + " and DOCTYPE = " + docType;
				pst = conn.prepareStatement(sql);
				rs = pst.executeQuery();
				while (rs.next()) {
					docIDFromDB = rs.getInt("DOCID");
				}
				pst.close();
				break;
			case DOCTYPE_GLOSSARY:
				sql = "select * from GLOSSARY where DOCID = " + docID + " and DOCTYPE = " + docType;
				pst = conn.prepareStatement(sql);
				rs = pst.executeQuery();
				while (rs.next()) {
					docIDFromDB = rs.getInt("DOCID");
				}
				pst.close();
				break;
			case DOCTYPE_ORGANIZATION:
				sql = "select * from ORGANIZATIONS where ORGID = " + docID + " and DOCTYPE = " + docType;
				pst = conn.prepareStatement(sql);
				rs = pst.executeQuery();
				while (rs.next()) {
					docIDFromDB = rs.getInt("ORGID");
				}
				pst.close();
				break;
			case DOCTYPE_DEPARTMENT:
				sql = "select * from DEPARTMENTS where DEPID = " + docID + " and DOCTYPE = " + docType;
				pst = conn.prepareStatement(sql);
				rs = pst.executeQuery();
				while (rs.next()) {
					docIDFromDB = rs.getInt("DEPID");
				}
				pst.close();
				break;
			case DOCTYPE_EMPLOYER:
				sql = "select * from EMPLOYERS where EMPID = " + docID + " and DOCTYPE = " + docType;
				pst = conn.prepareStatement(sql);
				rs = pst.executeQuery();
				while (rs.next()) {
					docIDFromDB = rs.getInt("EMPID");
				}
				pst.close();
				break;
			case DOCTYPE_TOPIC:
				sql = "select * from TOPICS where DOCID = " + docID + " and DOCTYPE = " + docType;
				pst = conn.prepareStatement(sql);
				rs = pst.executeQuery();
				while (rs.next()) {
					docIDFromDB = rs.getInt("DOCID");
				}
				pst.close();
				break;
			case DOCTYPE_POST:
				sql = "select * from POSTS where DOCID = " + docID + " and DOCTYPE = " + docType;
				pst = conn.prepareStatement(sql);
				rs = pst.executeQuery();
				while (rs.next()) {
					docIDFromDB = rs.getInt("DOCID");
				}
				pst.close();
				break;
			}
			conn.commit();
			return docIDFromDB != 0;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
			return false;
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Override
	public BaseDocument getDocumentByComplexID(int docType, int docID, Set<String> complexUserID, String absoluteUserID)
			throws DocumentAccessException, DocumentException, ComplexObjectException {
		switch (docType) {
		case DOCTYPE_MAIN:
			Document doc = getMainDocumentByID(docID, complexUserID, absoluteUserID);
			return doc;
		case DOCTYPE_TASK:
			Task task = getTasks().getTaskByID(docID, complexUserID, absoluteUserID);
			return task;
		case DOCTYPE_EXECUTION:
			Execution exec = getExecutions().getExecutionByID(docID, complexUserID, absoluteUserID);
			return exec;
		case DOCTYPE_PROJECT:
			Project prj = getProjects().getProjectByID(docID, complexUserID, absoluteUserID);
			return prj;
		case DOCTYPE_GLOSSARY:
			Glossary gl = getGlossaries().getGlossaryDocumentByID(docID, false, complexUserID, absoluteUserID);
			return gl;
		case DOCTYPE_ORGANIZATION:
			return getStructure().getOrganization(docID, new User(Const.sysUser, env));
		case DOCTYPE_DEPARTMENT:
			return getStructure().getDepartment(docID, new User(Const.sysUser, env));
		case DOCTYPE_EMPLOYER:
			return getStructure().getEmployer(docID, new User(Const.sysUser, env));
		case DOCTYPE_RECYCLE_BIN_ENTRY:
			return usersActivity.getRecycleBinEntry(docID, complexUserID, absoluteUserID);
		case DOCTYPE_GROUP:
			return getStructure().getGroup(docID, complexUserID, absoluteUserID);
		case DOCTYPE_TOPIC:
			return getForum().getTopicByID(docID, complexUserID, absoluteUserID);
		case DOCTYPE_POST:
			return getForum().getPostByID(docID, complexUserID, absoluteUserID);
		default:
			throw new DocumentException(DocumentExceptionType.UNKNOW_DOCUMENT_TYPE);
		}
	}

	@Override
	public BaseDocument getDocumentByComplexID(int docType, int docID)
			throws DocumentAccessException, DocumentException, ComplexObjectException {
		switch (docType) {
		case DOCTYPE_MAIN:
			Document doc = getMainDocumentByID(docID, Const.supervisorGroupAsSet, Const.sysUser);
			return doc;
		case DOCTYPE_TASK:
			Task task = getTasks().getTaskByID(docID, Const.sysGroupAsSet, Const.sysUser);
			return task;
		case DOCTYPE_EXECUTION:
			Execution exec = getExecutions().getExecutionByID(docID, Const.sysGroupAsSet, Const.sysUser);
			return exec;
		case DOCTYPE_PROJECT:
			Project prj = getProjects().getProjectByID(docID, Const.sysGroupAsSet, Const.sysUser);
			return prj;
		case DOCTYPE_GLOSSARY:
			Glossary gl = getGlossaries().getGlossaryDocumentByID(docID, false, Const.sysGroupAsSet, Const.sysUser);
			return gl;
		case DOCTYPE_ORGANIZATION:
			return getStructure().getOrganization(docID, new User(Const.sysUser, env));
		case DOCTYPE_DEPARTMENT:
			return getStructure().getDepartment(docID, new User(Const.sysUser, env));
		case DOCTYPE_EMPLOYER:
			return getStructure().getEmployer(docID, new User(Const.sysUser, env));
		case DOCTYPE_RECYCLE_BIN_ENTRY:
			return usersActivity.getRecycleBinEntry(docID, Const.sysGroupAsSet, Const.sysUser);
		case DOCTYPE_GROUP:
			return getStructure().getGroup(docID, Const.sysGroupAsSet, Const.sysUser);
		case DOCTYPE_TOPIC:
			return getForum().getTopicByID(docID, Const.sysGroupAsSet, Const.sysUser);
		case DOCTYPE_POST:
			return getForum().getPostByID(docID, Const.sysGroupAsSet, Const.sysUser);
		default:
			throw new DocumentException(DocumentExceptionType.UNKNOW_DOCUMENT_TYPE);
		}
	}

	public BaseDocument getDocumentByDOCID(String docID, Set<String> complexUserID, String absoluteUserID)
			throws DocumentException, DocumentAccessException, ComplexObjectException {
		Statement statement;
		BaseDocument doc = null;
		Connection conn = dbPool.getConnection();
		String sql = "select m.docid, m.doctype from maindocs as m, readers_maindocs as rm where m.docid = rm.docid and rm.username in ("
				+ DatabaseUtil.prepareListToQuery(complexUserID) + ") and m.docID='" + docID + "' limit 1 ";
		try {
			conn.setAutoCommit(false);
			statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			if (rs.next()) {
				doc = getDocumentByComplexID(rs.getInt("doctype"), rs.getInt("docid"), complexUserID, absoluteUserID);
				statement.close();
				conn.commit();
			} else {
				throw new DocumentAccessException(ExceptionType.DOCUMENT_READ_RESTRICTED, Const.sysUser);
			}
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return doc;
	}

	@Override
	public BaseDocument getDocumentByDdbID(String ddbID, Set<String> complexUserID, String absoluteUserID)
			throws DocumentException, DocumentAccessException, ComplexObjectException {
		Statement statement = null;
		BaseDocument doc = null;
		Connection conn = dbPool.getConnection();
		String sql = "select m.docid, m.doctype from maindocs as m, readers_maindocs as rm where m.docid = rm.docid and rm.username in ("
				+ DatabaseUtil.prepareListToQuery(complexUserID) + ") and ddbid='" + ddbID + "' " + "union "
				+ "select t.docid, t.doctype from tasks as t, readers_tasks as rt where t.docid = rt.docid and rt.username in ("
				+ DatabaseUtil.prepareListToQuery(complexUserID) + ") and ddbid='" + ddbID + "' " + "union "
				+ "select e.docid, e.doctype from executions as e, readers_executions as re where e.docid = re.docid and re.username in ("
				+ DatabaseUtil.prepareListToQuery(complexUserID) + ") and ddbid='" + ddbID + "' " + "union "
				+ "select p.docid, p.doctype from projects as p, readers_projects as rp where p.docid = rp.docid and rp.username in ("
				+ DatabaseUtil.prepareListToQuery(complexUserID) + ") and ddbid='" + ddbID + "' " + "union "
				+ "select g.docid, g.doctype from glossary as g where ddbid='" + ddbID + "' " + "union "
				+ "select gr.groupid, " + Const.DOCTYPE_GROUP + " from groups as gr where cast(groupid as varchar) = '"
				+ ddbID + "'" + "union " + "select e.empid as docid, e.doctype from employers as e where e.ddbid = '"
				+ ddbID + "' ";
		try {
			conn.setAutoCommit(false);
			statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			if (rs.next()) {
				doc = getDocumentByComplexID(rs.getInt("doctype"), rs.getInt("docid"), complexUserID, absoluteUserID);
				statement.close();
				conn.commit();
			} else {
				throw new DocumentAccessException(ExceptionType.DOCUMENT_READ_RESTRICTED, Const.sysUser);
			}
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return doc;
	}

	@Override
	public IViewEntry getDocumentByDocID(String ddbID, Set<String> complexUserID, String absoluteUserID)
			throws DocumentException, DocumentAccessException {
		Statement statement = null;
		ViewEntry entry = null;
		Connection conn = dbPool.getConnection();
		String sql = "select m.docid, m.doctype from maindocs as m, readers_maindocs as rm where m.docid = rm.docid and rm.username in ("
				+ DatabaseUtil.prepareListToQuery(complexUserID) + ") and ddbid='" + ddbID + "' " + "union "
				+ "select t.docid, t.doctype from tasks as t, readers_tasks as rt where t.docid = rt.docid and rt.username in ("
				+ DatabaseUtil.prepareListToQuery(complexUserID) + ") and ddbid='" + ddbID + "' " + "union "
				+ "select e.docid, e.doctype from executions as e, readers_executions as re where e.docid = re.docid and re.username in ("
				+ DatabaseUtil.prepareListToQuery(complexUserID) + ") and ddbid='" + ddbID + "' " + "union "
				+ "select p.docid, p.doctype from projects as p, readers_projects as rp where p.docid = rp.docid and rp.username in ("
				+ DatabaseUtil.prepareListToQuery(complexUserID) + ") and ddbid='" + ddbID + "' " + "union "
				+ "select g.docid, g.doctype from glossary as g where ddbid='" + ddbID + "' ";
		try {
			conn.setAutoCommit(false);
			statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			if (rs.next()) {
				User user = this.getStructure().getAppUser(absoluteUserID).getUser();
				entry = new ViewEntry(this, rs, new TreeSet<DocID>(), user);
				statement.close();
				conn.commit();
			} else {
				throw new DocumentAccessException(ExceptionType.DOCUMENT_READ_RESTRICTED, Const.sysUser);
			}
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return entry;
	}

	@Override
	public ArrayList<BaseDocument> getDescendantsArray(int docID, int docType, DocID[] toExpand, int level,
			Set<String> complexUserID, String absoluteUserID) throws DocumentException, ComplexObjectException {
		ArrayList<BaseDocument> documents = new ArrayList<BaseDocument>();
		if (docID != 0 && docType != DOCTYPE_UNKNOWN) {
			Connection conn = dbPool.getConnection();
			try {
				conn.setAutoCommit(false);
				Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				String sql = "SELECT distinct resp.docid, doctype, form, resp.viewtext, regdate, has_attachment, resp.allcontrol, ctype"
						+ " FROM TASKS resp" + " LEFT JOIN"
						+ " (SELECT g.viewtext AS ctype, t.docid AS taskid, g.docid FROM GLOSSARY AS g, TASKS AS t WHERE g.form = 'controltype' AND g.docid = t.controltype)"
						+ " ON resp.docid = taskid, READERS_TASKS r WHERE parentdocid = " + docID
						+ " AND parentdoctype = " + docType + " AND resp.docid = r.docid AND r.username IN ("
						+ DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ " UNION SELECT distinct resp.docid, doctype, form, viewtext, regdate, has_attachment, -1 as allcontrol, '' as ctype"
						+ " FROM EXECUTIONS resp, READERS_EXECUTIONS r WHERE parentdocid = " + docID
						+ " AND parentdoctype = " + docType + " AND resp.docid = r.docid AND r.username IN ("
						+ DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ " UNION SELECT distinct resp.docid, resp.doctype, resp.form, resp.viewtext, resp.regdate, resp.has_attachment, c.valueasnumber AS allcontrol, ctype"
						+ " FROM CUSTOM_FIELDS c " + " RIGHT JOIN MAINDOCS resp"
						+ " ON resp.docid = c.docid AND c.name = 'allcontrol'" + " LEFT JOIN"
						+ " (SELECT g.viewtext AS ctype, cf.docid AS cfdocid FROM GLOSSARY AS g, CUSTOM_FIELDS AS cf, MAINDOCS AS m"
						+ " WHERE g.docid = cf.valueasnumber AND cf.docid = m.docid AND cf.name = 'controltype')"
						+ " ON resp.docid = cfdocid," + " READERS_MAINDOCS r " + " WHERE parentdocid = " + docID
						+ " AND parentdoctype = " + docType + " AND resp.docid = r.docid AND r.username IN ("
						+ DatabaseUtil.prepareListToQuery(complexUserID) + ")" + " and resp.form != 'discussion' "
						+ " ORDER BY REGDATE";

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

					ArrayList<BaseDocument> responses = getDescendantsArray(respDocID, respDocType, null, l,
							complexUserID, absoluteUserID);
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
	public DocumentCollection getDescendants(int docID, int docType, SortByBlock sortBlock, int level,
			Set<String> complexUserID, String absoluteUserID) {
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
					switch (sortBlock.fieldName.toLowerCase()) {
					case "regdate":
					case "viewdate":
					case "form":
					case "viewtext":
						orderSQL = "ORDER BY " + sortBlock.fieldName + " " + sortBlock.order;
						break;
					}
				}

				String sql = "SELECT distinct resp.docid, doctype, form, resp.viewtext, resp.viewdate, regdate, has_attachment, resp.allcontrol, ctype"
						+ " FROM TASKS resp" + " LEFT JOIN"
						+ " (SELECT g.viewtext AS ctype, t.docid AS taskid, g.docid FROM GLOSSARY AS g, TASKS AS t WHERE g.form = 'controltype' AND g.docid = t.controltype) as ctype "
						+ " ON resp.docid = taskid, READERS_TASKS r WHERE parentdocid = " + docID
						+ " AND parentdoctype = " + docType + " AND resp.docid = r.docid AND r.username IN ("
						+ DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ " UNION SELECT distinct resp.docid, doctype, form, viewtext, viewdate, regdate, has_attachment, -1 as allcontrol, '' as ctype"
						+ " FROM EXECUTIONS resp, READERS_EXECUTIONS r WHERE parentdocid = " + docID
						+ " AND parentdoctype = " + docType + " AND resp.docid = r.docid AND r.username IN ("
						+ DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ " UNION SELECT distinct resp.docid, resp.doctype, resp.form, resp.viewtext, resp.viewdate, resp.regdate, resp.has_attachment, c.valueasnumber AS allcontrol, ctype"
						+ " FROM CUSTOM_FIELDS c " + " RIGHT JOIN MAINDOCS resp"
						+ " ON resp.docid = c.docid AND c.name = 'allcontrol'" + " LEFT JOIN"
						+ " (SELECT g.viewtext AS ctype, cf.docid AS cfdocid FROM GLOSSARY AS g, CUSTOM_FIELDS AS cf, MAINDOCS AS m"
						+ " WHERE g.docid = cf.valueasnumber AND cf.docid = m.docid AND cf.name = 'controltype') as ctype "
						+ " ON resp.docid = cfdocid," + " READERS_MAINDOCS r " + " WHERE parentdocid = " + docID
						+ " AND parentdoctype = " + docType + " AND resp.docid = r.docid AND r.username IN ("
						+ DatabaseUtil.prepareListToQuery(complexUserID) + ")" + " and resp.form != 'discussion' "
						+ orderSQL;

				ResultSet rs = statement.executeQuery(sql);

				while (rs.next()) {
					String viewText = rs.getString("VIEWTEXT").replace("''", "'");
					value = "";
					int respDocID = rs.getInt("DOCID");
					int repsDocType = rs.getInt("DOCTYPE");
					int allControl = rs.getInt("ALLCONTROL");
					String controlType = rs.getString("CTYPE");
					String form = rs.getString("FORM");
					xmlContent.append(
							"<entry isread=\"" + usersActivity.isRead(conn, respDocID, repsDocType, absoluteUserID)
									+ "\" hasattach=\"" + Integer.toString(rs.getInt("HAS_ATTACHMENT"))
									+ "\" doctype=\"" + repsDocType + "\"  docid=\"" + respDocID + "\" form=\"" + form
									+ "\" url=\"Provider?type=edit&amp;element=" + resolveElement(form) + "&amp;id="
									+ form + "&amp;key=" + respDocID + "\""
									+ (!form.equalsIgnoreCase("KI") ? " allcontrol =\"" + allControl + "\"" : "")
									+ (!form.equalsIgnoreCase("KI") ? " controltype =\"" + controlType + "\"" : ""));
					col++;

					int l = level + 1;
					DocumentCollection responses = getDescendants(respDocID, repsDocType, null, l, complexUserID,
							absoluteUserID);
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
				DatabaseUtil.errorPrint(dbID, e);
			} finally {
				dbPool.returnConnection(conn);
			}
		}
		return documents;

	}

	@Override
	public DocumentCollection getDiscussion(int docID, int docType, DocID[] toExpand, int level,
			Set<String> complexUserID, String absoluteUserID) {
		int col = 0;
		DocumentCollection documents = new DocumentCollection();
		StringBuffer xmlContent = new StringBuffer(10000);
		String value = "";
		if (docID != 0 && docType != DOCTYPE_UNKNOWN) {
			Connection conn = dbPool.getConnection();
			try {
				conn.setAutoCommit(false);
				Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				String sql = "SELECT distinct resp.docid, doctype, form, resp.viewtext, regdate, has_attachment, -1 as allcontrol , ctype"
						+ " FROM TASKS resp" + " LEFT JOIN"
						+ " (SELECT g.viewtext AS ctype, t.docid AS taskid, g.docid FROM GLOSSARY AS g, TASKS AS t WHERE g.form = 'controltype' AND g.docid = t.controltype) as ct"
						+ " ON resp.docid = taskid, READERS_TASKS r WHERE parentdocid = " + docID
						+ " AND parentdoctype = " + docType + " AND resp.docid = r.docid AND r.username IN ("
						+ DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ " UNION SELECT distinct resp.docid, doctype, form, viewtext, regdate, has_attachment, -1 as allcontrol, '' as ctype"
						+ " FROM EXECUTIONS resp, READERS_EXECUTIONS r WHERE parentdocid = " + docID
						+ " AND parentdoctype = " + docType + " AND resp.docid = r.docid AND r.username IN ("
						+ DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ " UNION SELECT distinct resp.docid, resp.doctype, resp.form, resp.viewtext, resp.regdate, resp.has_attachment, -1 AS allcontrol, ctype"
						+ " FROM CUSTOM_FIELDS c " + " RIGHT JOIN MAINDOCS resp"
						+ " ON resp.docid = c.docid AND c.name = 'allcontrol'" + " LEFT JOIN"
						+ " (SELECT g.viewtext AS ctype, cf.docid AS cfdocid FROM GLOSSARY AS g, CUSTOM_FIELDS AS cf, MAINDOCS AS m"
						+ " WHERE g.docid = cf.valueasnumber AND cf.docid = m.docid AND cf.name = 'controltype') as ct"
						+ " ON resp.docid = cfdocid," + " READERS_MAINDOCS r " + " WHERE parentdocid = " + docID
						+ " AND parentdoctype = " + docType + " AND resp.docid = r.docid AND r.username IN ("
						+ DatabaseUtil.prepareListToQuery(complexUserID) + ")" + " and resp.form = 'discussion' "
						+ " ORDER BY REGDATE";

				ResultSet rs = statement.executeQuery(sql);

				while (rs.next()) {
					String viewText = rs.getString("VIEWTEXT").replace("''", "'");
					value = "";
					int respDocID = rs.getInt("DOCID");
					int repsDocType = rs.getInt("DOCTYPE");
					int allControl = rs.getInt("ALLCONTROL");
					String controlType = rs.getString("CTYPE");
					String form = rs.getString("FORM");
					xmlContent.append(
							"<entry isread=\"" + usersActivity.isRead(conn, respDocID, repsDocType, absoluteUserID)
									+ "\" hasattach=\"" + Integer.toString(rs.getInt("HAS_ATTACHMENT"))
									+ "\" doctype=\"" + repsDocType + "\"  docid=\"" + respDocID + "\" form=\"" + form
									+ "\" url=\"Provider?type=edit&amp;element=document&amp;id=" + form + "&amp;key="
									+ respDocID + "\"" + XMLUtil.getAsAttribute("viewtext", viewText)
									+ (!form.equalsIgnoreCase("KI") ? " allcontrol =\"" + allControl + "\"" : "")
									+ (!form.equalsIgnoreCase("KI") ? " controltype =\"" + controlType + "\"" : ""));
					col++;

					int l = level + 1;
					DocumentCollection responses = getDescendants(respDocID, repsDocType, null, l, complexUserID,
							absoluteUserID);
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
				DatabaseUtil.errorPrint(dbID, e);
			} finally {
				dbPool.returnConnection(conn);
			}
		}
		return documents;

	}

	@Override
	public void insertToAccessTables(Connection conn, String accessTableSuffix, int docID, Document doc) {

		try {
			String authorsTable = "AUTHORS_" + accessTableSuffix;
			StringBuffer authorsUpdateSQL = new StringBuffer(100);
			HashSet<String> authors = doc.getEditors();
			authors.addAll(Const.supervisorGroupAsList);
			for (String author : authors) {
				String hasAuthorSQL = "select count(*) from " + authorsTable + " where DOCID=" + docID
						+ " and USERNAME='" + author + "'";
				ResultSet resultSet = conn.createStatement().executeQuery(hasAuthorSQL);

				if (resultSet.next()) {
					if (resultSet.getInt(1) == 0) {
						authorsUpdateSQL.append("('" + author + "', " + docID + "),");
					}
				}
			}
			if (!authorsUpdateSQL.equals("")) {
				authorsUpdateSQL.deleteCharAt(authorsUpdateSQL.length() - 1);
				// System.out.println(authorsUpdateSQL);
				conn.prepareStatement("insert into " + authorsTable + "(USERNAME, DOCID) values " + authorsUpdateSQL)
						.executeUpdate();
			}
			conn.commit();
			String readersTable = "READERS_" + accessTableSuffix;
			StringBuffer readersUpdateSQL = new StringBuffer(100);
			HashSet<Reader> readers = doc.getReaders();

			for (String value : supervisorGroupAsList) {
				readers.add(new Reader(value));
			}

			for (Reader reader : readers) {
				String hasReaderSQL = "select count(*) from " + readersTable + " where DOCID=" + docID
						+ " and USERNAME='" + reader + "'";
				ResultSet resultSet = conn.createStatement().executeQuery(hasReaderSQL);
				if (resultSet.next()) {
					if (resultSet.getInt(1) == 0) {
						readersUpdateSQL.append("('" + reader + "', " + docID + "),");
					}
				}
			}
			if (!readersUpdateSQL.equals("")) {
				readersUpdateSQL.deleteCharAt(readersUpdateSQL.length() - 1);
				conn.prepareStatement("insert into " + readersTable + "(USERNAME, DOCID) values " + readersUpdateSQL)
						.executeUpdate();
			}
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		}
	}

	/*
	 * public _ViewEntryCollection getCollectionByCondition(ISelectFormula
	 * condition, User user, int pageNum, int pageSize, Set<DocID>
	 * toExpandResponses, RunTimeParameters parameters, boolean checkResponse,
	 * String responseQueryCondition) { ViewEntryCollection coll = new
	 * ViewEntryCollection(pageSize, user, parameters); Set<String> users =
	 * user.getAllUserGroups(); Connection conn = dbPool.getConnection(); try {
	 * conn.setAutoCommit(false); Statement s =
	 * conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
	 * ResultSet.CONCUR_READ_ONLY); if (pageNum == 0) { String sql =
	 * condition.getCountCondition(users, parameters.getFilters()); ResultSet rs
	 * = s.executeQuery(sql); if (rs.next()) { pageNum =
	 * RuntimeObjUtil.countMaxPage(rs.getInt(1), pageSize); } } int offset =
	 * calcStartEntry(pageNum, pageSize); String sql =
	 * condition.getCondition(users, pageSize, offset, parameters.getFilters(),
	 * parameters.getSorting(), checkResponse, responseQueryCondition);
	 * ResultSet rs = s.executeQuery(sql); if (rs.next()) { ViewEntry entry =
	 * new ViewEntry(this, rs, toExpandResponses, user,
	 * parameters.getDateFormat(), responseQueryCondition); coll.add(entry);
	 *
	 * while (rs.next()) { entry = new ViewEntry(this, rs, toExpandResponses,
	 * user, parameters.getDateFormat(), responseQueryCondition);
	 * coll.add(entry); } } sql = condition.getCountForPaging(users,
	 * parameters.getFilters()); rs = s.executeQuery(sql); if (rs.next()) {
	 * coll.setCount(rs.getInt(1)); } conn.commit(); s.close(); rs.close();
	 */
	@Override
	public _ViewEntryCollection getCollectionByCondition(ISelectFormula condition, User user, int pageNum, int pageSize,
			Set<DocID> toExpandResponses, RunTimeParameters parameters, boolean checkResponse,
			String responseQueryCondition) {
		ViewEntryCollection coll = new ViewEntryCollection(pageSize, user, parameters);
		Set<String> users = user.getAllUserGroups();
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			if (pageNum == 0) {
				String sql = condition.getCountCondition(users, parameters.getFilters());
				ResultSet rs = s.executeQuery(sql);
				if (rs.next()) {
					pageNum = RuntimeObjUtil.countMaxPage(rs.getInt(1), pageSize);
				}
			}
			int offset = calcStartEntry(pageNum, pageSize);
			String sql = condition.getCondition(users, pageSize, offset, parameters.getFilters(),
					parameters.getSorting(), checkResponse, responseQueryCondition);
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				ViewEntry entry = new ViewEntry(this, rs, toExpandResponses, user, parameters.getDateFormat(),
						responseQueryCondition);
				coll.add(entry);
				coll.setCount(rs.getInt(1));
				while (rs.next()) {
					entry = new ViewEntry(this, rs, toExpandResponses, user, parameters.getDateFormat(),
							responseQueryCondition);
					coll.add(entry);
				}
			}
			conn.commit();
			s.close();
			rs.close();

		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} catch (Exception e) {
			Database.logger.errorLogEntry(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		coll.setCurrentPage(pageNum);
		return coll.getScriptingObj();
	}

	@Override
	public DocumentCollection getDescendants(int docID, int docType, SortByBlock sortBlock, int level,
			Set<String> complexUserID, String absoluteUserID, String responseQueryCondition) {
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
					switch (sortBlock.fieldName.toLowerCase()) {
					case "regdate":
					case "viewdate":
					case "form":
					case "viewtext":
						orderSQL = "ORDER BY " + sortBlock.fieldName + " " + sortBlock.order;
						break;
					}
				}

				String sql = "SELECT distinct resp.docid, doctype, form, resp.viewtext, resp.viewdate, regdate, has_attachment, resp.allcontrol, ctype"
						+ " FROM TASKS resp" + " LEFT JOIN"
						+ " (SELECT g.viewtext AS ctype, t.docid AS taskid, g.docid FROM GLOSSARY AS g, TASKS AS t WHERE g.form = 'controltype' AND g.docid = t.controltype) as ctype "
						+ " ON resp.docid = taskid, READERS_TASKS r WHERE parentdocid = " + docID
						+ " AND parentdoctype = " + docType + " AND resp.docid = r.docid AND r.username IN ("
						+ DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ (!"".equalsIgnoreCase(responseQueryCondition) ? " and " + responseQueryCondition : "")
						+ " UNION SELECT distinct resp.docid, doctype, form, viewtext, viewdate, regdate, has_attachment, -1 as allcontrol, '' as ctype"
						+ " FROM EXECUTIONS resp, READERS_EXECUTIONS r WHERE parentdocid = " + docID
						+ " AND parentdoctype = " + docType + " AND resp.docid = r.docid AND r.username IN ("
						+ DatabaseUtil.prepareListToQuery(complexUserID) + ")"
						+ (!"".equalsIgnoreCase(responseQueryCondition) ? " and " + responseQueryCondition : "")
						+ " UNION SELECT distinct resp.docid, resp.doctype, resp.form, resp.viewtext, resp.viewdate, resp.regdate, resp.has_attachment, c.valueasnumber AS allcontrol, ctype"
						+ " FROM CUSTOM_FIELDS c " + " RIGHT JOIN MAINDOCS resp"
						+ " ON resp.docid = c.docid AND c.name = 'allcontrol'" + " LEFT JOIN"
						+ " (SELECT g.viewtext AS ctype, cf.docid AS cfdocid FROM GLOSSARY AS g, CUSTOM_FIELDS AS cf, MAINDOCS AS m"
						+ " WHERE g.docid = cf.valueasnumber AND cf.docid = m.docid AND cf.name = 'controltype') as ctype "
						+ " ON resp.docid = cfdocid," + " READERS_MAINDOCS r " + " WHERE parentdocid = " + docID
						+ " AND parentdoctype = " + docType + " AND resp.docid = r.docid AND r.username IN ("
						+ DatabaseUtil.prepareListToQuery(complexUserID) + ")" + " and resp.form != 'discussion' "
						+ (!"".equalsIgnoreCase(responseQueryCondition) ? " and " + responseQueryCondition : "")
						+ orderSQL;

				ResultSet rs = statement.executeQuery(sql);

				while (rs.next()) {
					String viewText = rs.getString("VIEWTEXT").replace("''", "'");
					value = "";
					int respDocID = rs.getInt("DOCID");
					int repsDocType = rs.getInt("DOCTYPE");
					int allControl = rs.getInt("ALLCONTROL");
					String controlType = rs.getString("CTYPE");
					String form = rs.getString("FORM");
					xmlContent.append(
							"<entry isread=\"" + usersActivity.isRead(conn, respDocID, repsDocType, absoluteUserID)
									+ "\" hasattach=\"" + Integer.toString(rs.getInt("HAS_ATTACHMENT"))
									+ "\" doctype=\"" + repsDocType + "\"  docid=\"" + respDocID + "\" form=\"" + form
									+ "\" url=\"Provider?type=edit&amp;element=" + resolveElement(form) + "&amp;id="
									+ form + "&amp;key=" + respDocID + "\""
									+ (!form.equalsIgnoreCase("KI") ? " allcontrol =\"" + allControl + "\"" : "")
									+ (!form.equalsIgnoreCase("KI") ? " controltype =\"" + controlType + "\"" : ""));
					col++;

					int l = level + 1;
					DocumentCollection responses = getDescendants(respDocID, repsDocType, null, l, complexUserID,
							absoluteUserID);
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
				DatabaseUtil.errorPrint(dbID, e);
			} finally {
				dbPool.returnConnection(conn);
			}
		}
		return documents;
	}

	@Override
	public void removeUnrelatedAttachments() {
		Connection conn = dbPool.getConnection();
		try {
			Statement s = conn.createStatement();
			s.addBatch("delete from custom_blobs_maindocs  where DATEDIFF(DAY, now(), regdate) < 0 and docid = null");
			s.addBatch("delete from custom_blobs_glossary  where DATEDIFF(DAY, now(), regdate) < 0 and docid = null");
			s.addBatch("delete from custom_blobs_employers where DATEDIFF(DAY, now(), regdate) < 0 and docid = null");
			s.addBatch("delete from custom_blobs_topics    where DATEDIFF(DAY, now(), regdate) < 0 and docid = null");
			s.addBatch("delete from custom_blobs_posts     where DATEDIFF(DAY, now(), regdate) < 0 and docid = null");
			s.executeBatch();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Override
	public _ViewEntryCollection getCollectionByCondition(ISelectFormula condition, User user, int pageNum, int pageSize,
			Set<DocID> toExpandResponses, RunTimeParameters parameters, boolean checkResponse,
			boolean expandAllResponses, boolean checkUnread) {
		ViewEntryCollection coll = new ViewEntryCollection(pageSize, user, parameters);
		Set<String> users = user.getAllUserGroups();
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			if (pageNum == 0) {
				String sql = condition.getCountCondition(users, parameters.getFilters());
				ResultSet rs = s.executeQuery(sql);
				if (rs.next()) {
					pageNum = RuntimeObjUtil.countMaxPage(rs.getInt(1), pageSize);
				}
			}
			int offset = calcStartEntry(pageNum, pageSize);
			String sql = condition.getCondition(user, pageSize, offset, parameters.getFilters(),
					parameters.getSorting(), checkResponse, checkUnread);
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				if (expandAllResponses) {
					toExpandResponses.add(new DocID(rs.getInt("DOCID"), rs.getInt("DOCTYPE")));
				}
				ViewEntry entry = new ViewEntry(this, rs, toExpandResponses, user, parameters.getDateFormat());
				coll.add(entry);
				coll.setCount(rs.getInt(1));
				while (rs.next()) {
					if (expandAllResponses) {
						toExpandResponses.add(new DocID(rs.getInt("DOCID"), rs.getInt("DOCTYPE")));
					}
					entry = new ViewEntry(this, rs, toExpandResponses, user, parameters.getDateFormat());
					coll.add(entry);
				}
			}
			conn.commit();
			s.close();
			rs.close();

		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} catch (Exception e) {
			Database.logger.errorLogEntry(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		coll.setCurrentPage(pageNum);
		return coll.getScriptingObj();
	}

	@Override
	public _ViewEntryCollection getCollectionByCondition(ISelectFormula condition, User user, int pageNum, int pageSize,
			Set<DocID> toExpandResponses, RunTimeParameters parameters, boolean checkResponse,
			boolean expandAllResponses, _ReadConditionType type) {
		ViewEntryCollection coll = new ViewEntryCollection(pageSize, user, parameters);
		Set<String> users = user.getAllUserGroups();
		Connection conn = dbPool.getConnection();
		SelectFormula.ReadCondition cond;
		switch (type) {
		case ONLY_READ:
			cond = SelectFormula.ReadCondition.ONLY_READ;
			break;
		case ONLY_UNREAD:
			cond = SelectFormula.ReadCondition.ONLY_UNREAD;
			break;
		case ALL:
			cond = SelectFormula.ReadCondition.ALL;
			break;
		default:
			cond = SelectFormula.ReadCondition.ALL;
			break;
		}
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			if (pageNum == 0) {
				String sql = condition.getCountCondition(user, parameters.getFilters(), cond);
				ResultSet rs = s.executeQuery(sql);
				if (rs.next()) {
					pageNum = RuntimeObjUtil.countMaxPage(rs.getInt(1), pageSize);
				}
			}
			int offset = calcStartEntry(pageNum, pageSize);
			String sql = condition.getCondition(user, pageSize, offset, parameters.getFilters(),
					parameters.getSorting(), checkResponse, cond);
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				if (expandAllResponses) {
					toExpandResponses.add(new DocID(rs.getInt("DOCID"), rs.getInt("DOCTYPE")));
				}
				ViewEntry entry = new ViewEntry(this, rs, toExpandResponses, user, parameters.getDateFormat());
				coll.add(entry);
				coll.setCount(rs.getInt(1));
				while (rs.next()) {
					if (expandAllResponses) {
						toExpandResponses.add(new DocID(rs.getInt("DOCID"), rs.getInt("DOCTYPE")));
					}
					entry = new ViewEntry(this, rs, toExpandResponses, user, parameters.getDateFormat());
					coll.add(entry);
				}
			}
			conn.commit();
			s.close();
			rs.close();

		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} catch (Exception e) {
			Database.logger.errorLogEntry(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		coll.setCurrentPage(pageNum);
		return coll.getScriptingObj();
	}

	@Override
	public _ViewEntryCollection getCollectionByCondition(ISelectFormula condition, User user, int pageNum, int pageSize,
			Set<DocID> toExpandResponses, RunTimeParameters parameters, boolean checkResponse,
			boolean expandAllResponses, _ReadConditionType type, String customFieldName) {
		ViewEntryCollection coll = new ViewEntryCollection(pageSize, user, parameters);
		Set<String> users = user.getAllUserGroups();
		Connection conn = dbPool.getConnection();
		SelectFormula.ReadCondition cond;
		switch (type) {
		case ONLY_READ:
			cond = SelectFormula.ReadCondition.ONLY_READ;
			break;
		case ONLY_UNREAD:
			cond = SelectFormula.ReadCondition.ONLY_UNREAD;
			break;
		case ALL:
			cond = SelectFormula.ReadCondition.ALL;
			break;
		default:
			cond = SelectFormula.ReadCondition.ALL;
			break;
		}
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			if (pageNum == 0) {
				String sql = condition.getCountCondition(user, parameters.getFilters(), cond, customFieldName);
				ResultSet rs = s.executeQuery(sql);
				if (rs.next()) {
					pageNum = RuntimeObjUtil.countMaxPage(rs.getInt(1), pageSize);
				}
			}
			int offset = calcStartEntry(pageNum, pageSize);
			String sql = condition.getCondition(user, pageSize, offset, parameters.getFilters(),
					parameters.getSorting(), checkResponse, cond, customFieldName);
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				if (expandAllResponses) {
					toExpandResponses.add(new DocID(rs.getInt("DOCID"), rs.getInt("DOCTYPE")));
				}
				ViewEntry entry = new ViewEntry(this, rs, toExpandResponses, user, parameters.getDateFormat());
				coll.add(entry);
				coll.setCount(rs.getInt(1));
				while (rs.next()) {
					if (expandAllResponses) {
						toExpandResponses.add(new DocID(rs.getInt("DOCID"), rs.getInt("DOCTYPE")));
					}
					entry = new ViewEntry(this, rs, toExpandResponses, user, parameters.getDateFormat());
					coll.add(entry);
				}
			}
			conn.commit();
			s.close();
			rs.close();

		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} catch (Exception e) {
			Database.logger.errorLogEntry(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		coll.setCurrentPage(pageNum);
		return coll.getScriptingObj();
	}

	@Override
	public _ViewEntryCollection getCollectionByCondition(ISelectFormula condition, User user, int pageNum, int pageSize,
			Set<DocID> toExpandResponses, RunTimeParameters parameters, boolean checkResponse,
			boolean expandAllResponses) {
		ViewEntryCollection coll = new ViewEntryCollection(pageSize, user, parameters);
		Set<String> users = user.getAllUserGroups();
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			if (pageNum == 0) {
				String sql = condition.getCountCondition(users, parameters.getFilters());
				ResultSet rs = s.executeQuery(sql);
				if (rs.next()) {
					pageNum = RuntimeObjUtil.countMaxPage(rs.getInt(1), pageSize);
				}
			}
			int offset = calcStartEntry(pageNum, pageSize);
			String sql = condition.getCondition(users, pageSize, offset, parameters.getFilters(),
					parameters.getSorting(), checkResponse);
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				if (expandAllResponses) {
					toExpandResponses.add(new DocID(rs.getInt("DOCID"), rs.getInt("DOCTYPE")));
				}
				ViewEntry entry = new ViewEntry(this, rs, toExpandResponses, user, parameters.getDateFormat());
				coll.add(entry);
				coll.setCount(rs.getInt(1));
				while (rs.next()) {
					if (expandAllResponses) {
						toExpandResponses.add(new DocID(rs.getInt("DOCID"), rs.getInt("DOCTYPE")));
					}
					entry = new ViewEntry(this, rs, toExpandResponses, user, parameters.getDateFormat());
					coll.add(entry);
				}
			}
			conn.commit();
			s.close();
			rs.close();

		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} catch (Exception e) {
			Database.logger.errorLogEntry(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		coll.setCurrentPage(pageNum);
		return coll.getScriptingObj();
	}

	@Override
	public _ViewEntryCollection getCollectionByCondition(ISelectFormula condition, User user, int pageNum, int pageSize,
			Set<DocID> toExpandResponses, RunTimeParameters parameters, boolean checkResponse) {
		ViewEntryCollection coll = new ViewEntryCollection(pageSize, user, parameters);
		Set<String> users = user.getAllUserGroups();
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			if (pageNum == 0) {
				String sql = condition.getCountCondition(users, parameters.getFilters());
				ResultSet rs = s.executeQuery(sql);
				if (rs.next()) {
					pageNum = RuntimeObjUtil.countMaxPage(rs.getInt(1), pageSize);
				}
			}
			int offset = calcStartEntry(pageNum, pageSize);
			String sql = condition.getCondition(users, pageSize, offset, parameters.getFilters(),
					parameters.getSorting(), checkResponse);
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				ViewEntry entry = new ViewEntry(this, rs, toExpandResponses, user, parameters.getDateFormat());
				coll.add(entry);
				coll.setCount(rs.getInt(1));
				while (rs.next()) {
					entry = new ViewEntry(this, rs, toExpandResponses, user, parameters.getDateFormat());
					coll.add(entry);
				}
			}
			conn.commit();
			s.close();
			rs.close();

		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} catch (Exception e) {
			Database.logger.errorLogEntry(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		coll.setCurrentPage(pageNum);
		return coll.getScriptingObj();
	}

	@Override
	public int parseFile(File parentDir, File dir, HashMap<Integer, Integer> linkOldNew) {

		String docid = dir.getName().substring(4, dir.getName().length());
		if (linkOldNew.containsKey(Integer.parseInt(docid))) {
			return linkOldNew.get(Integer.parseInt(docid));
		}

		if (!dir.exists() || !parentDir.exists()) {
			Server.logger.errorLogEntry("FILE " + dir.getAbsolutePath() + " NOT EXIST");
			linkOldNew.put(Integer.parseInt(docid), -1);
			return -1;
		}

		try {

			Document doc = new Document(this, "[supervisor]");

			org.w3c.dom.Document xmlDoc = XMLUtil.getDOMDocument(dir.getAbsolutePath() + File.separator + "Doc.xml");

			int pDocId = 0;
			if (!XMLUtil.getTextContent(xmlDoc, "document/@parentdocid").trim().equals("0")) {
				int parentDocId = parseFile(parentDir, new File(parentDir.getAbsolutePath() + File.separator + "doc_"
						+ XMLUtil.getTextContent(xmlDoc, "document/@parentdocid").trim()), linkOldNew);
				if (parentDocId < 0) {
					Server.logger.errorLogEntry("PARENT DOCUMENT DOES NOT EXIST FOR DOC " + docid);
					linkOldNew.put(Integer.parseInt(docid), -1);
					return -1;
				}
				pDocId = parentDocId;
			}

			doc.parseXml(xmlDoc, pDocId);

			File[] attachments = dir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isDirectory();
				}
			});

			for (File k : attachments) {
				if (k.listFiles().length > 0) {
					File f = k.listFiles()[0];
					doc.addAttachment("rtfcontent", f, "");
				}
			}

			doc.save(new User("[supervisor]", this));
			linkOldNew.put(Integer.parseInt(docid), doc.getDocID());
		} catch (Exception e) {
			Server.logger.errorLogEntry("Exception on " + dir.getAbsolutePath() + File.separator + "Doc.xml");
			e.printStackTrace();
			linkOldNew.put(Integer.parseInt(docid), -1);
			return -1;
		}
		return linkOldNew.get(Integer.parseInt(docid));
	}

	@Override
	public void fillAccessRelatedField(Connection conn, String accessTableSuffix, int docID, Document doc)
			throws SQLException {
		conn.setAutoCommit(false);
		String getReadSQL = "select * from READERS_" + accessTableSuffix + " where DOCID = " + docID;
		Statement statement = conn.createStatement();
		ResultSet resultSet = statement.executeQuery(getReadSQL);
		while (resultSet.next()) {
			doc.replaceReader(resultSet.getString("USERNAME"), resultSet.getInt("FAVORITES") == 1);
		}

		String getAuthSQL = "select * from AUTHORS_" + accessTableSuffix + " where DOCID = " + docID;
		resultSet = statement.executeQuery(getAuthSQL);

		while (resultSet.next()) {
			doc.addEditor(resultSet.getString("USERNAME"));
		}
		resultSet.close();
		statement.close();
		conn.commit();

	}

	@Override
	public String toString() {
		return "version:" + getVersion() + ", dbid:" + dbID + ", URL:" + connectionURL;
	}

	public String toXML() {
		return dbPool.toXML();
	}

	@Override
	public IStructure getStructure() {
		if (structDbPool != null) {
			switch (structDbPool.getDatabaseType()) {
			case H2:
				return new Structure(this, structDbPool);
			case POSTGRESQL:
				return new kz.flabs.dataengine.postgresql.structure.Structure(this, structDbPool);
			case MSSQL:
				return new kz.flabs.dataengine.mssql.structure.Structure(this, structDbPool);
			default:
				return new Structure(this, structDbPool);
			}
		} else {
			return new Structure(this, dbPool);
		}
	}

	@Override
	public IGlossaries getGlossaries() {
		return new Glossaries(env);
	}

	@Override
	public IDBConnectionPool getConnectionPool() {
		return dbPool;
	}

	@Override
	public IDBConnectionPool getStructureConnectionPool() {
		return structDbPool;
	}

	@Override
	public IFTIndexEngine getFTSearchEngine() {
		return new FTIndexEngine(this);
	}

	@Override
	@Deprecated
	public ITasks getTasks() {
		return new TaskOnDatabase(this);
	}

	@Override
	@Deprecated
	public IExecutions getExecutions() {
		return new ExecutionsOnDatabase(this);
	}

	@Override
	@Deprecated
	public IProjects getProjects() {
		return new ProjectOnDatabase(this);
	}

	@Override
	@Deprecated
	public IFilters getFilters() {
		return new Filters(this);
	}

	@Override
	public IUsersActivity getUserActivity() {
		return new UsersActivity(this);
	}

	@Override
	public IActivity getActivity() {
		return new Activity(this);
	}

	@Override
	public IMyDocsProcessor getMyDocsProcessor() {
		return new MyDocsProcessor(this);
	}

	@Override
	public IHelp getHelp() {
		return new Help(this);
	}

	@Override
	public IForum getForum() {
		return new Forum(this, forumDbPool);
	}

	@Override
	public ArrayList<BaseDocument> getResponses(int docID, int docType, Set<String> complexUserID,
			String absoluteUserID) throws DocumentAccessException, DocumentException, ComplexObjectException {
		ArrayList<BaseDocument> documents = new ArrayList<BaseDocument>();
		if (docID != 0 && docType != DOCTYPE_UNKNOWN) {
			Connection conn = dbPool.getConnection();
			try {
				conn.setAutoCommit(false);
				Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				String ids = DatabaseUtil.prepareListToQuery(complexUserID);
				String sql = "select distinct resp.docid, doctype, form, viewtext, regdate, has_attachment FROM TASKS resp, READERS_TASKS r where PARENTDOCID="
						+ docID + " and PARENTDOCTYPE=" + docType + " and resp.DOCID = r.DOCID and r.USERNAME IN ("
						+ ids + ")"
						+ " UNION SELECT distinct resp.docid, doctype, form, viewtext, regdate, has_attachment FROM EXECUTIONS resp, READERS_EXECUTIONS r WHERE PARENTDOCID="
						+ docID + " and PARENTDOCTYPE=" + docType + " and resp.DOCID = r.DOCID and r.USERNAME IN ("
						+ ids + ")"
						+ " UNION SELECT distinct resp.docid, doctype, form, viewtext, regdate, has_attachment FROM MAINDOCS resp, READERS_MAINDOCS r WHERE PARENTDOCID="
						+ docID + " and PARENTDOCTYPE=" + docType + " and resp.DOCID = r.DOCID and r.USERNAME IN ("
						+ ids + ")"
						+ " UNION SELECT distinct resp.docid, doctype, form, viewtext, regdate, 0 as has_attachment from GLOSSARY resp where parentdocid="
						+ docID + " and parentdoctype=" + docType + " and resp.form != 'discussion' "
						+ " ORDER BY REGDATE";
				ResultSet rs = statement.executeQuery(sql);

				while (rs.next()) {
					int respDocType = UNKNOWN, respDocID = 0;
					BaseDocument doc = null;
					respDocID = rs.getInt("DOCID");
					respDocType = rs.getInt("DOCTYPE");

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
					case DOCTYPE_GLOSSARY:
						doc = getGlossaries().getGlossaryDocumentByID(respDocID, false, Const.sysGroupAsSet,
								Const.sysUser);
						break;
					}
					documents.add(doc);
				}
				rs.close();
				statement.close();
				conn.commit();
			} catch (SQLException e) {
				DatabaseUtil.errorPrint(dbID, e);
			} finally {
				dbPool.returnConnection(conn);
			}
		}
		return documents;
	}

	@Override
	public int randomBinary() {
		Random r = new Random();
		return r.nextInt(2);
	}

	@Override
	public StringBuffer getUsersRecycleBin(int offset, int pageSize, String userID) {
		return getUserActivity().getActivity(userID, offset, pageSize, UsersActivityType.DELETED.getCode());
	}

	@Override
	public int getUsersRecycleBinCount(int offset, int pageSize, String userID) {
		return getUserActivity().getActivitiesCount(userID, UsersActivityType.DELETED.getCode());
	}

	@Override
	public XMLResponse deleteDocuments(List<DocID> docs, boolean completely, User user) {
		XMLResponse result = new XMLResponse(ResponseType.DELETE_DOCUMENT);
		StringBuffer messages = new StringBuffer(1000);
		String listOfDeleted = "";
		String listOfNotDeleted = "";
		String currentDoc = "";
		int countDeleted = 0, countNotDeleted = 0;

		for (DocID id : docs) {
			try {
				currentDoc = this.getFieldByComplexID(id.id, id.type, "viewtext");
				this.deleteDocument(id.type, id.id, user, completely);
				countDeleted++;
				listOfDeleted += currentDoc + "||";
			} catch (Exception e) {
				DatabaseUtil.errorPrint(dbID, e);
				String viewText = this.getFieldByComplexID(id.id, id.type, "viewtext");
				messages.append(
						"<entry id=\"" + id.id + "\" type=\"" + id.type + "\" reason=\"" + e.getClass().getSimpleName()
								+ "\">" + (viewText != null ? XMLUtil.getAsTagValue(viewText) : "") + "</entry>");
				listOfNotDeleted += viewText + "||";
				countNotDeleted++;
			}
		}
		result.setMessage(countDeleted, "deleted");
		result.setMessage(countNotDeleted, "notdeleted");
		return result;
	}

	@Override
	public XMLResponse unDeleteDocuments(List<DocID> docs, User user) {
		XMLResponse result = new XMLResponse(ResponseType.UNDELETE_DOCUMENT);
		StringBuffer messages = new StringBuffer(1000);
		int countRestored = 0, countNotRestored = 0;
		for (DocID id : docs) {
			try {
				boolean isRestore = this.unDeleteDocument(id.id, user);
				if (isRestore) {
					countRestored++;
				}
			} catch (Exception e) {
				DatabaseUtil.errorPrint(dbID, e);
				String viewText = this.getFieldByComplexID(id.id, Const.DOCTYPE_ACTIVITY_ENTRY, "viewtext");
				messages.append("<entry id=\"" + id.id + "\" type=\"" + Const.DOCTYPE_ACTIVITY_ENTRY + "\" reason=\""
						+ e.getClass().getSimpleName() + "\">"
						+ (viewText != null ? XMLUtil.getAsTagValue(viewText) : "") + "</entry>");
				countNotRestored++;
			}
		}
		result.setMessage(countRestored, "restored");
		result.addMessage(countNotRestored, "notrestored");
		return result;
	}

	@Override
	public String getFieldByComplexID(int docID, int docType, String fieldName) {
		String tableName = DatabaseUtil.getMainTableName(docType);
		String columnName = DatabaseUtil.getPrimaryKeyColumnName(docType);
		String fieldValue = "";
		String sql = "SELECT " + fieldName + " from " + tableName + " where " + columnName + " = " + docID;
		Connection conn = dbPool.getConnection();
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				fieldValue = rs.getString(fieldName);
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return fieldValue;
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
	public ISelectFormula getSelectFormula(FormulaBlocks blocks) {
		switch (blocks.docType) {
		case STRUCTURE:
			return this.getStructure().getSelectFormula(blocks);
		case DOCUMENT:
		default:
			SelectFormula sf = new SelectFormula(blocks);
			return sf;
		}
	}

	@Override
	public ISelectFormula getForumSelectFormula(FormulaBlocks queryFormulaBlocks) {
		return new ForumSelectFormula(queryFormulaBlocks);
	}

	@Override
	public DatabaseType getRDBMSType() {
		return databaseType;
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
				sql = "select cf." + result[1] + ", count(cf." + result[1]
						+ ") from custom_fields as cf where cf.name = '" + result[0] + "' and " + " cf." + result[1]
						+ " is not null and "
						+ " cf.docid in (select docid from readers_maindocs as rm where rm.username in ("
						+ DatabaseUtil.prepareListToQuery(users) + ")) " + " group by cf." + result[1] + " order by cf."
						+ result[1] + " limit " + pageSize + " offset " + offset;
			} else {
				sql = "select m." + fieldName + ", count(m." + fieldName + ") from maindocs as m where m." + fieldName
						+ " is not null and "
						+ "m.docid in (select docid from readers_maindocs as rm where rm.username in ("
						+ DatabaseUtil.prepareListToQuery(users) + ")) " + " group by m." + fieldName + " order by m."
						+ fieldName + " limit " + pageSize + " offset " + offset;
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
	public ArrayList<BaseDocument> getDocumentsForMonth(HashSet<String> userGroups, String userID, String form,
			String fieldName, int month, int offset, int pageSize) {
		ArrayList<BaseDocument> vec = new ArrayList<BaseDocument>();
		String userIDs = DatabaseUtil.prepareListToQuery(userGroups);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

			String sql = "";
			if (isSystemField(fieldName)) {
				sql = "select distinct MAINDOCS.DOCTYPE, MAINDOCS.DOCID from MAINDOCS where MAINDOCS.DOCID in "
						+ "(select docid from READERS_MAINDOCS where MAINDOCS.DOCID = READERS_MAINDOCS.DOCID AND READERS_MAINDOCS.USERNAME IN ("
						+ userIDs + ")) and " + "(form= '" + form + "' and MONTH(" + fieldName + ") = " + month
						+ " ) limit " + pageSize + " offset " + offset;
			} else {
				sql = "select distinct MAINDOCS.DOCTYPE, MAINDOCS.DOCID from MAINDOCS where MAINDOCS.DOCID in "
						+ "(select docid from READERS_MAINDOCS where MAINDOCS.DOCID = READERS_MAINDOCS.DOCID and READERS_MAINDOCS.USERNAME in ("
						+ userIDs + ") and MAINDOCS.DOCID in "
						+ "(select docid from CUSTOM_FIELDS where MAINDOCS.DOCID = CUSTOM_FIELDS.DOCID AND CUSTOM_FIELDS.NAME='"
						+ fieldName + "' and MONTH(CUSTOM_FIELDS.VALUEASDATE) = " + month + ")) and " + "form= '" + form
						+ "' limit " + pageSize + " offset " + offset;
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
	public StringBuffer getFavorites(IQueryFormula nf, Set<String> complexUserID, String absoluteUserID, int offset,
			int pageSize, String fieldsCond, Set<DocID> toExpandResponses, Set<String> toExpandCategory,
			TagPublicationFormatType publishAs, int page) {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "select has_attachment, form, ddbid, docid, doctype, viewtext, "
					+ DatabaseUtil.getViewTextList("")
					+ ", viewnumber, viewdate from maindocs where docid in (select docid from readers_maindocs where username in ("
					+ DatabaseUtil.prepareListToQuery(complexUserID) + ") and favorites = 1) " + " union "
					+ " select has_attachment, form, ddbid, docid, doctype, viewtext, "
					+ DatabaseUtil.getViewTextList("")
					+ ", viewnumber, viewdate  from tasks where docid in (select docid from readers_tasks where username in ("
					+ DatabaseUtil.prepareListToQuery(complexUserID) + ") and favorites = 1) " + " union "
					+ " select has_attachment, form, ddbid, docid, doctype, viewtext, "
					+ DatabaseUtil.getViewTextList("")
					+ ", viewnumber, viewdate  from executions where docid in (select docid from readers_executions where username in ("
					+ DatabaseUtil.prepareListToQuery(complexUserID) + ") and favorites = 1) " + " union "
					+ " select has_attachment, form, ddbid, docid, doctype, viewtext, "
					+ DatabaseUtil.getViewTextList("")
					+ ", viewnumber, viewdate  from projects where docid in (select docid from readers_projects where username in ("
					+ DatabaseUtil.prepareListToQuery(complexUserID) + ") and favorites = 1) " + " order by "
					+ nf.getSortBlock().fieldName + "  " + nf.getSortBlock().order + " LIMIT " + pageSize + " OFFSET "
					+ offset;
			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				xmlContent.append(
						getDocumentEntry(conn, complexUserID, absoluteUserID, rs, fieldsCond, toExpandResponses, page));
			}
			s.close();
			rs.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} catch (DocumentException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return xmlContent;
	}

	@Override
	public int getFavoritesCount(Set<String> complexUserID, String absoluteUserID) {
		int count = 0;
		Connection conn = dbPool.getConnection();
		try {
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = " select count(*) from (select docid from maindocs where docid in (select docid from readers_maindocs where username = '"
					+ absoluteUserID + "' and favorites = 1) " + " union "
					+ " select docid  from tasks where docid in (select docid from readers_tasks where username = '"
					+ absoluteUserID + "' and favorites = 1) " + " union "
					+ " select docid  from executions where docid in (select docid from readers_executions where username = '"
					+ absoluteUserID + "' and favorites = 1) " + " union "
					+ " select docid  from projects where docid in (select docid from readers_projects where username = '"
					+ absoluteUserID + "' and favorites = 1)) cnt ";

			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				count = rs.getInt(1);
			}
			s.close();
			rs.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);

		}
		return count;
	}

	@Override
	public int isFavourites(Connection conn, int docID, int docType, Employer user) {
		int isFavour = 0;
		try {
			String sql = "select favorites from READERS_MAINDOCS where username in ("
					+ DatabaseUtil.prepareListToQuery(user.getAllUserGroups()) + ") and docid = " + docID;
			ResultSet rs = conn.createStatement().executeQuery(sql);
			while (rs.next()) {
				isFavour = rs.getInt("favorites");
			}
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		}
		return isFavour;
	}

	@Override
	@Deprecated
	public int isFavourites(int docID, int docType, String userName) {
		int isFavour = 0;
		Connection conn = dbPool.getConnection();
		try {
			// String sql = "select docid from users_activity where (type = " +
			// UsersActivityType.MARKED_AS_FAVORITE.getCode() + " or type = " +
			// UsersActivityType.UNMARKED_AS_FAVORITE.getCode() + ") and dbid =
			// '" + this.getDbID() + "' and docid = " + docID + " and doctype =
			// " + docType + " and userid = '" + userName + "' group by docid
			// having count(docid) % 2 <> 0";
			Employer user = this.getStructure().getAppUser(userName);
			String sql = "select favorites from " + DatabaseUtil.getReadersTableName(docType) + " where username  in ("
					+ DatabaseUtil.prepareListToQuery(user.getAllUserGroups()) + ") and docid = " + docID;
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			while (rs.next()) {
				isFavour = rs.getInt("favorites");
			}
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return isFavour;
	}

	@Deprecated
	private static String resolveElement(int docType) {
		switch (docType) {
		case Const.DOCTYPE_TASK:
			return "task";
		case Const.DOCTYPE_EXECUTION:
			return "executions";
		case Const.DOCTYPE_PROJECT:
			return "project";
		default:
			return "document";
		}
	}

	@Override
	public boolean clearDocuments() {
		Connection conn = dbPool.getConnection();
		try {
			String[] tables = new String[6];
			tables[0] = "projects";
			tables[1] = "tasks";
			tables[2] = "executions";
			tables[3] = "maindocs";
			tables[4] = "topics";
			tables[5] = "posts";
			String tableType = "";
			// custom_fields
			tableType = "custom_fields";
			String sql = "select CONSTRAINT_NAME, SQL from information_schema.CONSTRAINTS where LCASE(table_name) = '"
					+ tableType + "' and LCASE(column_list) = 'docid'";
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery(sql);

			if (rs.next()) {
				String constQuery = rs.getString("SQL").toLowerCase();
				if (!constQuery.contains("on delete cascade")) {
					String constraint = rs.getString("CONSTRAINT_NAME");
					sql = "ALTER TABLE " + tableType + " DROP CONSTRAINT " + constraint;
					s.execute(sql);
					sql = "ALTER TABLE " + tableType + " ADD FOREIGN KEY(DOCID) REFERENCES " + tables[0]
							+ " (DOCID) ON DELETE CASCADE";
					s.execute(sql);
				}
			} else {
				sql = "ALTER TABLE " + tableType + " ADD FOREIGN KEY(DOCID) REFERENCES " + tables[0]
						+ " (DOCID) ON DELETE CASCADE";
				s.execute(sql);
			}

			// custom_blobs
			tableType = "custom_blobs_";
			for (String tableName : tables) {
				sql = "select CONSTRAINT_NAME, SQL from information_schema.CONSTRAINTS where LCASE(table_name) = '"
						+ tableType + tableName + "' and LCASE(column_list) = 'docid'";
				rs = s.executeQuery(sql);

				if (rs.next()) {
					String constQuery = rs.getString("SQL").toLowerCase();
					if (!constQuery.contains("on delete cascade")) {
						String constraint = rs.getString("CONSTRAINT_NAME");
						sql = "ALTER TABLE " + tableType + tableName + " DROP CONSTRAINT " + constraint;
						s.execute(sql);
						sql = "ALTER TABLE " + tableType + tableName + " ADD FOREIGN KEY(DOCID) REFERENCES " + tableName
								+ " (DOCID) ON DELETE CASCADE";
						s.execute(sql);
					}
				} else {
					sql = "ALTER TABLE " + tableType + tableName + " ADD FOREIGN KEY(DOCID) REFERENCES " + tableName
							+ " (DOCID) ON DELETE CASCADE";
					s.execute(sql);
				}
			}

			// readers
			tableType = "readers_";
			for (String tableName : tables) {
				sql = "select CONSTRAINT_NAME, SQL from information_schema.CONSTRAINTS where LCASE(table_name) = '"
						+ tableType + tableName + "' and LCASE(column_list) = 'username,docid'";
				rs = s.executeQuery(sql);

				if (rs.next()) {
					String constQuery = rs.getString("SQL").toLowerCase();
					if (!constQuery.contains("on delete cascade")) {
						String constraint = rs.getString("CONSTRAINT_NAME");
						sql = "ALTER TABLE " + tableType + tableName + " DROP CONSTRAINT " + constraint;
						s.execute(sql);
						sql = "ALTER TABLE " + tableType + tableName + " ADD FOREIGN KEY(DOCID) REFERENCES " + tableName
								+ " (DOCID) ON DELETE CASCADE";
						s.execute(sql);
					}
				} else {
					sql = "ALTER TABLE " + tableType + tableName + " ADD FOREIGN KEY(DOCID) REFERENCES " + tableName
							+ " (DOCID) ON DELETE CASCADE";
					s.execute(sql);
				}
			}

			// authors
			tableType = "authors_";
			for (String tableName : tables) {
				sql = "select CONSTRAINT_NAME, SQL from information_schema.CONSTRAINTS where LCASE(table_name) = '"
						+ tableType + tableName + "' and LCASE(column_list) = 'username,docid'";
				rs = s.executeQuery(sql);

				if (rs.next()) {
					String constQuery = rs.getString("SQL").toLowerCase();
					if (!constQuery.contains("on delete cascade")) {
						String constraint = rs.getString("CONSTRAINT_NAME");
						sql = "ALTER TABLE " + tableType + tableName + " DROP CONSTRAINT " + constraint;
						s.execute(sql);
						sql = "ALTER TABLE " + tableType + tableName + " ADD FOREIGN KEY(DOCID) REFERENCES " + tableName
								+ " (DOCID) ON DELETE CASCADE";
						s.execute(sql);
					}
				} else {
					sql = "ALTER TABLE " + tableType + tableName + " ADD FOREIGN KEY(DOCID) REFERENCES " + tableName
							+ " (DOCID) ON DELETE CASCADE";
					s.execute(sql);
				}
			}

			// tasks
			tableType = "";
			sql = "select CONSTRAINT_NAME, SQL from information_schema.CONSTRAINTS where LCASE(table_name) = '"
					+ tableType + tables[4] + "' and LCASE(column_list) = 'username,docid'";
			rs = s.executeQuery(sql);

			if (rs.next()) {
				String constQuery = rs.getString("SQL").toLowerCase();
				if (!constQuery.contains("on delete cascade")) {
					String constraint = rs.getString("CONSTRAINT_NAME");
					sql = "ALTER TABLE " + tableType + tables[4] + " DROP CONSTRAINT " + constraint;
					s.execute(sql);
					sql = "ALTER TABLE " + tableType + tables[4] + " ADD FOREIGN KEY(DOCID) REFERENCES " + tables[4]
							+ " (DOCID) ON DELETE CASCADE";
					s.execute(sql);
				}
			} else {
				sql = "ALTER TABLE " + tableType + tables[4] + " ADD FOREIGN KEY(DOCID) REFERENCES " + tables[4]
						+ " (DOCID) ON DELETE CASCADE";
				s.execute(sql);
			}
			sql = "DELETE FROM PROJECTS";
			s.execute(sql);
			sql = "DELETE FROM MAINDOCS";
			s.execute(sql);
			// sql = "DELETE FROM PROJECTS WHERE DOCID IN (SELECT DOCID FROM
			// MAINDOCS)";
			sql = "DELETE FROM PROJECTS";
			s.execute(sql);
			sql = "DELETE FROM TASKS";
			s.execute(sql);

			conn.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(dbID, e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return true;
	}

	@Deprecated
	protected static String resolveElement(String f) {
		if (f.equalsIgnoreCase("kr")) {
			return "task";
		} else if (f.equalsIgnoreCase("ki")) {
			return "execution";
		} else {
			return "document";
		}
	}

	@Override
	public HashMap<String, Role> getAppRoles() {
		return env.getRolesMap();
	}

	private boolean hasDocsWithParent() {
		return env.globalSetting.databaseResponseUsed;
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		// TODO Auto-generated method stub
		return null;
	}

}