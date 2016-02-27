package kz.flabs.dataengine.h2;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.ISystemDatabase;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.runtimeobj.viewentry.IViewEntryCollection;
import kz.flabs.runtimeobj.viewentry.ViewEntryCollection;
import kz.flabs.users.User;
import kz.flabs.webrule.constants.RunMode;
import kz.lof.appenv.AppEnv;
import kz.lof.dataengine.system.IEmployee;
import kz.lof.dataengine.system.IEmployeeDAO;
import kz.lof.env.Environment;
import kz.lof.server.Server;
import kz.pchelka.scheduler.IProcessInitiator;

import org.apache.catalina.realm.RealmBase;

public class SystemDatabase implements ISystemDatabase, IProcessInitiator, Const {
	public static boolean isValid;
	public static String jdbcDriver = "org.h2.Driver";

	private IDBConnectionPool dbPool;
	private static String connectionURL = "jdbc:h2:system_data" + File.separator + "system_data;MVCC=TRUE;AUTO_SERVER="
	        + (Environment.debugMode == RunMode.ON ? "TRUE" : "FALSE");

	private IEmployeeDAO eDao;

	public SystemDatabase() throws DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		this(connectionURL);
	}

	public SystemDatabase(String connURL) throws DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		dbPool = new kz.flabs.dataengine.h2.DBConnectionPool();
		dbPool.initConnectionPool(jdbcDriver, connURL);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			createUserTable(getUsersDDE(), "USERS");

			isValid = true;
			conn.commit();
		} catch (Throwable e) {
			AppEnv.logger.errorLogEntry(e);
			e.printStackTrace();
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Override
	public void setEmployeeDAO(IEmployeeDAO dao) {
		eDao = dao;

	}

	@Override
	public void removeAppEntry(User user) {
		Connection conn = dbPool.getConnection();
		AppEnv env = user.getAppEnv();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String sql = "DELETE FROM ENABLEDAPPS WHERE DOCID = (SELECT DOCID FROM USERS WHERE USERID = '" + user.getUserID() + "') AND APP = '"
			        + env.appType + "'";
			s.execute(sql);
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Override
	public User checkUser(String login, String pwd, User user) {
		Connection conn = dbPool.getConnection();
		AppEnv env = user.getAppEnv();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String sql = "select * from USERS, ENABLEDAPPS where USERS.DOCID = ENABLEDAPPS.DOCID and USERID = '" + login + "'";
			ResultSet rs = s.executeQuery(sql);
			String password = "";

			if (rs.next()) {

				if (rs.getString("PWDHASH") != null) {
					if (!rs.getString("PWDHASH").trim().equals("")) {
						password = rs.getString("PWDHASH");
						String pwdHash = "";
						if (password.length() < 30) {
							pwdHash = pwd.hashCode() + "";
						} else {
							// pwdHash = getMD5Hash(pwd);
							RealmBase rb = null;
							pwdHash = rb.Digest(pwd, "MD5", "UTF-8");

						}

						if (pwdHash.equals(password)) {
							user = initUser(conn, rs, env, login);
							user.authorized = true;
							if (password.length() < 11) {// !!!!
								pswToPswHash(user, pwd);
							}

						}
					} else {
						password = rs.getString("PWD");
						if (pwd.equals(password)) {
							user = initUser(conn, rs, env, login);
							user.authorized = true;
							pswToPswHash(user, password);
						}
					}
				} else {
					password = rs.getString("PWD");
					if (pwd.equals(password)) {
						user = initUser(conn, rs, env, login);
						user.authorized = true;
						pswToPswHash(user, password);
					}
				}
			}
			rs.close();
			s.close();
			conn.commit();
			return user;
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
			return null;
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Override
	public User checkUser(String login, String pwd, String hashAsText, User user) {
		Connection conn = dbPool.getConnection();
		AppEnv env = user.getAppEnv();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String sql = "select * from USERS, ENABLEDAPPS where USERS.DOCID = ENABLEDAPPS.DOCID and USERID = '" + login + "'";
			ResultSet rs = s.executeQuery(sql);
			String password = "";

			if (rs.next()) {

				// .trim().equals("")
				if (rs.getString("PWDHASH") != null) {
					if (!rs.getString("PWDHASH").trim().equals("")) {
						password = rs.getString("PWDHASH");
						String pwdHash = pwd;
						int hash = rs.getInt("LOGINHASH");
						if (checkHash(hashAsText, hash)) {
							user = initUser(conn, rs, env, login);
							user.authorizedByHash = true;
							user.authorized = true;
						} else if (checkHashPSW(pwdHash, password)) {
							user = initUser(conn, rs, env, login);
							user.authorized = true;
						}
					} else {
						password = rs.getString("PWD");
						int hash = rs.getInt("LOGINHASH");
						if (checkHash(hashAsText, hash)) {
							user = initUser(conn, rs, env, login);
							user.authorizedByHash = true;
							user.authorized = true;
						} else if (pwd.equals(password)) {
							user = initUser(conn, rs, env, login);
							user.authorized = true;
						}
					}
				} else {
					password = rs.getString("PWD");
					int hash = rs.getInt("LOGINHASH");
					if (checkHash(hashAsText, hash)) {
						user = initUser(conn, rs, env, login);
						user.authorizedByHash = true;
						user.authorized = true;
					} else if (pwd.equals(password)) {
						user = initUser(conn, rs, env, login);
						user.authorized = true;
						pswToPswHash(user, password);
					}
				}
			}
			rs.close();
			s.close();
			conn.commit();
			return user;
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
			return null;
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Override
	public User checkUserHash(String login, String pwd, String hashAsText, User user) {
		Connection conn = dbPool.getConnection();
		AppEnv env = user.getAppEnv();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String sql = "select * from USERS, ENABLEDAPPS where USERS.DOCID = ENABLEDAPPS.DOCID and USERID = '" + login + "'";
			ResultSet rs = s.executeQuery(sql);
			String password = "";

			if (rs.next()) {

				// .trim().equals("")
				if (rs.getString("PWDHASH") != null) {
					if (!rs.getString("PWDHASH").trim().equals("")) {
						password = rs.getString("PWDHASH");
						String pwdHash = "";
						if (password.length() < 11) {
							pwdHash = pwd.hashCode() + "";
						} else {
							pwdHash = RealmBase.Digest(pwd, "MD5", "UTF-8");
						}
						int hash = rs.getInt("LOGINHASH");
						if (checkHash(hashAsText, hash)) {
							user = initUser(conn, rs, env, login);
							user.authorizedByHash = true;
							user.authorized = true;
						} else if (checkHashPSW(pwdHash, password)) {
							user = initUser(conn, rs, env, login);
							user.authorized = true;
							if (password.length() < 11) {
								pswToPswHash(user, pwd);
							}
						}
					} else {
						password = rs.getString("PWD");
						int hash = rs.getInt("LOGINHASH");
						if (checkHash(hashAsText, hash)) {
							user = initUser(conn, rs, env, login);
							user.authorizedByHash = true;
							user.authorized = true;
						} else if (pwd.equals(password)) {
							user = initUser(conn, rs, env, login);
							user.authorized = true;
							pswToPswHash(user, password);
							// user.setPassword("");
							// user.setPasswordHash(password.hashCode()+"");

							// checkUserHash(login, pwd, hashAsText, user);

						}
					}
				} else {
					password = rs.getString("PWD");
					int hash = rs.getInt("LOGINHASH");
					if (checkHash(hashAsText, hash)) {
						user = initUser(conn, rs, env, login);
						user.authorizedByHash = true;
						user.authorized = true;
					} else if (pwd.equals(password)) {
						user = initUser(conn, rs, env, login);
						user.authorized = true;
						pswToPswHash(user, password);
						// user.setPassword("");
						// user.setPasswordHash(password.hashCode()+"");
						// checkUserHash(login, pwd, hashAsText, user);
					}
				}
			}
			rs.close();
			s.close();
			conn.commit();
			return user;
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
			return null;
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	private void pswToPswHash(User user, String pwd) throws SQLException, WebFormValueException {
		Connection conn = dbPool.getConnection();
		conn.setAutoCommit(false);
		// String pwdHsh = pwd.hashCode()+"";
		// String pwdHsh = getMD5Hash(pwd);
		RealmBase rb = null;
		String pwdHsh = rb.Digest(pwd, "MD5", "UTF-8");

		String userUpdateSQL = "update USERS set PWD='', PWDHASH='" + pwdHsh + "'" + " where DOCID=" + user.docID;
		PreparedStatement pst = conn.prepareStatement(userUpdateSQL);
		pst.executeUpdate();
		conn.commit();
		conn.close();
		user.setPasswordHash(pwd);
		user.setPassword("");
	}

	private User initUser(Connection conn, ResultSet rs, AppEnv env, String login) throws SQLException {
		boolean isNext = true;
		User user = new User(login, env);
		if (eDao != null) {
			IEmployee emp = eDao.getEmployee(login);
			if (emp != null) {
				user.setUserName(emp.getName());
			}
			user.fill(rs);
			while (isNext || rs.next()) {
				UserApplicationProfile ap = new UserApplicationProfile(rs.getString("APP"), rs.getInt("LOGINMODE"));
				if (ap.loginMod != LoginModeType.LOGIN_AND_REDIRECT) {
					String qaSQL = "select * from QA where QA.DOCID=" + user.docID + " AND QA.APP='" + ap.appName + "'";
					Statement s1 = conn.createStatement();
					ResultSet rs1 = s1.executeQuery(qaSQL);
					while (rs1.next()) {
						ap.getQuestionAnswer().add(ap.new QuestionAnswer(rs1.getString("QUESTION"), rs1.getString("ANSWER")));
					}
					user.enabledApps.put(ap.appName, ap);
				}
				isNext = false;
			}
			return user;
		} else {
			Server.logger.errorLogEntry("Any Staff module has not been initialized");
			return new User();
		}
	}

	@Override
	public int getAllUsersCount(String condition) {
		int count = 0;
		String wherePiece = "";
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			if (!condition.equals("")) {
				wherePiece = "WHERE " + condition;
			}
			Statement s = conn.createStatement();
			String sql = "select count(*) from USERS " + wherePiece;
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();
			s.close();
			conn.commit();
			return count;
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
			return 0;
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Deprecated
	@Override
	public ArrayList<User> getAllUsers(String condition, int start, int end) {
		ArrayList<User> users = new ArrayList<User>();
		String wherePiece = "";
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			if (!condition.equals("")) {
				wherePiece = "WHERE " + condition;
			}
			Statement s = conn.createStatement();
			String sql = "select * from USERS " + wherePiece + " LIMIT " + end + " OFFSET " + start;
			ResultSet rs = s.executeQuery(sql);

			while (rs.next()) {
				User user = new User();
				user.fill(rs);
				if (user.isValid) {
					String addSQL = "select * from ENABLEDAPPS where ENABLEDAPPS.DOCID=" + user.docID;
					Statement statement = conn.createStatement();
					ResultSet resultSet = statement.executeQuery(addSQL);
					while (resultSet.next()) {
						UserApplicationProfile ap = new UserApplicationProfile(resultSet.getString("APP"), resultSet.getInt("LOGINMODE"));
						user.enabledApps.put(ap.appName, ap);
					}
					resultSet.close();
					statement.close();
				}
				user.isValid = true;
				users.add(user);
			}

			rs.close();
			s.close();
			conn.commit();
			return users;
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
			return null;
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Override
	public IViewEntryCollection getUsers(String condition, int start, int end) {
		String dbFields[] = { "userid", "isadmin", "email", "instmsgaddr" };
		ViewEntryCollection col = new ViewEntryCollection(end);
		String wherePiece = "";
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			if (!condition.equals("")) {
				wherePiece = "WHERE " + condition;
			}
			Statement s = conn.createStatement();
			String sql = "select * from USERS " + wherePiece + " ORDER BY userid asc LIMIT " + end + " OFFSET " + start;
			ResultSet rs = s.executeQuery(sql);

			while (rs.next()) {
				// IViewEntry entry = new ViewEntry(rs, dbFields);
				// col.add(entry);
			}

			rs.close();
			s.close();
			conn.commit();
			return col;
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
			return null;
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Override
	public int getUsersCount(String condition) {
		int count = 0;
		String wherePiece = "";
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			if (!condition.equals("")) {
				wherePiece = "WHERE " + condition;
			}
			Statement s = conn.createStatement();
			String sql = "select count(*) from USERS " + wherePiece;
			ResultSet rs = s.executeQuery(sql);

			if (rs.next()) {
				count = rs.getInt(1);
			}

			rs.close();
			s.close();
			conn.commit();
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return count;
	}

	@Override
	public HashMap<String, User> getAllAdministrators() {
		HashMap<String, User> users = new HashMap<String, User>();
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String sql = "select * from USERS WHERE ISADMIN = 1";
			ResultSet rs = s.executeQuery(sql);

			while (rs.next()) {
				User user = new User();
				user.fill(rs);
				user.isValid = true;
				users.put(user.getUserID(), user);
			}

			rs.close();
			s.close();
			conn.commit();
			return users;
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
			return null;
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Override
	public User getUser(String userID) {
		User user = new User();
		return reloadUserData(user, userID);
	}

	@Override
	public User reloadUserData(User user, String userID) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String sql = "select * from USERS where USERS.USERID='" + userID + "'";
			ResultSet rs = s.executeQuery(sql);

			if (rs.next()) {
				user.fill(rs);
				if (user.isValid) {
					String addSQL = "select * from ENABLEDAPPS where ENABLEDAPPS.DOCID=" + user.docID;
					Statement statement = conn.createStatement();
					ResultSet resultSet = statement.executeQuery(addSQL);
					while (resultSet.next()) {
						UserApplicationProfile ap = new UserApplicationProfile(resultSet.getString("APP"), resultSet.getInt("LOGINMODE"));
						String qaSQL = "select * from QA where QA.DOCID=" + user.docID + " AND QA.APP='" + ap.appName + "'";
						Statement s1 = conn.createStatement();
						ResultSet rs1 = s1.executeQuery(qaSQL);
						while (rs1.next()) {
							ap.getQuestionAnswer().add(ap.new QuestionAnswer(rs1.getString("QUESTION"), rs1.getString("ANSWER")));
						}
						user.enabledApps.put(ap.appName, ap);
					}
					resultSet.close();
					statement.close();
				}
			} else {
				user.setUserID(userID);
			}
			rs.close();
			s.close();
			conn.commit();
			// user.addSupervisorRole();
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return user;
	}

	@Override
	public User getUser(long docID) {
		User user = new User();
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			/*
			 * String sql =
			 * "select * from USERS, ENABLEDAPPS where USERS.DOCID=ENABLEDAPPS.DOCID and "
			 * + "USERS.DOCID=" + docID;
			 */
			String sql = "select * from USERS where USERS.DOCID=" + docID;
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				user.fill(rs);
				if (user.isValid) {
					String addSQL = "select * from ENABLEDAPPS where ENABLEDAPPS.DOCID=" + docID;
					Statement statement = conn.createStatement();
					ResultSet resultSet = statement.executeQuery(addSQL);
					while (resultSet.next()) {
						UserApplicationProfile ap = new UserApplicationProfile(resultSet.getString("APP"), resultSet.getInt("LOGINMODE"));
						String qaSQL = "select * from QA where QA.DOCID=" + docID + " AND QA.APP='" + ap.appName + "'";
						Statement s1 = conn.createStatement();
						ResultSet rs1 = s1.executeQuery(qaSQL);
						while (rs1.next()) {
							ap.getQuestionAnswer().add(ap.new QuestionAnswer(rs1.getString("QUESTION"), rs1.getString("ANSWER")));
						}
						user.enabledApps.put(ap.appName, ap);
					}
					resultSet.close();
					statement.close();
				}
			} else {
				user.setNewDoc(true);
			}
			rs.close();
			s.close();
			conn.commit();
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return user;
	}

	@Override
	public boolean deleteUser(int docID) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String delEnApp = "delete from ENABLEDAPPS where DOCID = " + docID;
			PreparedStatement pst = conn.prepareStatement(delEnApp);
			pst.executeUpdate();
			String delUserTab = "delete from USERS where DOCID = " + docID;
			pst = conn.prepareStatement(delUserTab);
			pst.executeUpdate();
			conn.commit();
			pst.close();
			return true;
		} catch (Throwable e) {
			DatabaseUtil.errorPrint(e);
			return false;
		} finally {
			dbPool.returnConnection(conn);
		}

	}

	public boolean hasUserTable(String tableName) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String sql = "select * from " + tableName;
			s.executeQuery(sql);
			s.close();
			conn.commit();
			return true;
		} catch (Throwable e) {
			return false;
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	public boolean createUserTable(String createTableScript, String tableName) {
		Connection conn = dbPool.getConnection();
		boolean createUserTab = false;
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			if (!hasUserTable(tableName)) {
				if (s.execute(createTableScript)) {
					AppEnv.logger.errorLogEntry("Unable to create table \"" + tableName + "\"");
				}
			}

			PreparedStatement upState = null;
			if (tableName.equalsIgnoreCase("USERS")) {
				upState = conn.prepareStatement("alter table users add if not exists LOGINHASH int");
				upState.executeUpdate();
				upState.close();
			}

			if (tableName.equalsIgnoreCase("USERS")) {
				upState = conn.prepareStatement("alter table users add if not exists PWDHASH VARCHAR(1024)");
				upState.executeUpdate();
				upState.close();
			}

			if (tableName.equalsIgnoreCase("USERS")) {
				upState = conn.prepareStatement("alter table users add if not exists PUBLICKEY varchar(6144)");
				upState.executeUpdate();
				upState.close();
				upState = conn.prepareStatement("ALTER TABLE users ALTER COLUMN PUBLICKEY VARCHAR(6144) DEFAULT NULL;");
				upState.executeUpdate();
				upState.close();
			}

			if (tableName.equalsIgnoreCase("ENABLEDAPPS")) {
				upState = conn.prepareStatement("alter table ENABLEDAPPS add if not exists loginmode int default 0");
				upState.executeUpdate();
				upState.close();
			}

			createUserTab = true;
			s.close();
			conn.commit();
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
			createUserTab = false;
		} finally {
			dbPool.returnConnection(conn);
		}
		return createUserTab;
	}

	private String getUsersDDE() {
		String createTable = "create table USERS(DOCID int generated by default as identity PRIMARY KEY, "
		        + "USERID VARCHAR(32) CONSTRAINT USERS_USERID_UNIQUE UNIQUE, " + "EMAIL VARCHAR(32)," + "INSTMSGADDR VARCHAR(32),"
		        + "PWD VARCHAR(32), " + "REGDATE timestamp DEFAULT now()," + "ISAPPADMIN int, " + "ISADMIN int, " + "ISOBSERVER int, "
		        + "LOGINHASH int, " + "PUBLICKEY varchar(6144)," + "PWDHASH VARCHAR(1024))";
		return createTable;
	}

	@Override
	public int insert(User user) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			int key = 0;
			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery("select max(docid) from users");

			if (rs.next()) {
				key = rs.getInt(1) + 1;
			}
			String insertUser = "insert into USERS(DOCID, USERID, EMAIL, ISADMIN, LOGINHASH, PUBLICKEY, PWDHASH )" + "values(" + key + ", " + "'"
			        + user.getUserID() + "', " + "'" + user.getEmail() + "'," + user.getIsAdmin() + ","
			        + (user.getUserID() + user.getPassword()).hashCode() + ", '" + user.getPublicKey() + "','" + user.getPasswordHash() + "')";

			PreparedStatement pst = conn.prepareStatement(insertUser/*
																	 * ,
																	 * PreparedStatement
																	 * .
																	 * RETURN_GENERATED_KEYS
																	 */);
			pst.executeUpdate();
			// ResultSet rs = pst.getGeneratedKeys();
			// while(rs.next()){
			// key = rs.getInt(1);
			// }

			/*
			 * if (user.enabledApps.isEmpty()) { for(AppEnv appEnv:
			 * Environment.getApplications()){ if
			 * (!appEnv.globalSetting.isWorkspace){ ApplicationProfile ap = new
			 * ApplicationProfile(appEnv.appType,0);
			 * user.setEnabledApp(ap.appName, ap); } } }
			 */

			for (UserApplicationProfile app : user.enabledApps.values()) {
				String insertURL = "insert into ENABLEDAPPS(DOCID, APP, LOGINMODE)values(" + key + ", '" + app.appName + "'," + app.loginMode + ")";
				pst = conn.prepareStatement(insertURL);
				pst.executeUpdate();
				if (app.loginMode == UserApplicationProfile.LOGIN_AND_QUESTION) {
					for (UserApplicationProfile.QuestionAnswer qa : app.getQuestionAnswer()) {
						insertURL = "insert into QA(DOCID, APP, QUESTION, ANSWER)values(" + key + ",'" + app.appName + "','" + qa.controlQuestion
						        + "','" + qa.answer + "')";
						pst = conn.prepareStatement(insertURL);
						pst.executeUpdate();
					}
				}
			}

			conn.commit();
			pst.close();
			stmt.close();
			return 1;
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
			return -1;
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Override
	public int update(User user) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String pwdHsh = "";
			String pwd = "";
			if (user.getPasswordHash() != null && !user.getPasswordHash().trim().equals("")) {
				pwdHsh = user.getPasswordHash();
			} else {
				pwd = user.getPassword();
			}
			String userUpdateSQL = "update USERS set USERID='" + user.getUserID() + "'," + " EMAIL='" + user.getEmail() + "'," + "PWD='" + pwd
			        + "', " + "ISADMIN = " + user.getIsAdmin() + "," + "LOGINHASH = " + (user.getUserID() + user.getPassword()).hashCode() + ", "
			        + "PUBLICKEY = '" + user.getPublicKey() + "', PWDHASH='" + pwdHsh + "'" + " where DOCID=" + user.docID;
			PreparedStatement pst = conn.prepareStatement(userUpdateSQL);
			pst.executeUpdate();
			conn.commit();
			String delSQL = "delete from ENABLEDAPPS where DOCID = " + user.docID;
			pst = conn.prepareStatement(delSQL);
			pst.executeUpdate();

			delSQL = "delete from QA where DOCID = " + user.docID;
			pst = conn.prepareStatement(delSQL);
			pst.executeUpdate();

			for (UserApplicationProfile app : user.enabledApps.values()) {
				String insertURL = "insert into ENABLEDAPPS(DOCID, APP, LOGINMODE)values (" + user.docID + ", '" + app.appName + "'," + app.loginMode
				        + ")";
				PreparedStatement pst0 = conn.prepareStatement(insertURL);
				pst0.executeUpdate();

				delSQL = "delete from QA where DOCID = " + user.docID + " AND QA.APP='" + app.appName + "'";
				PreparedStatement pst2 = conn.prepareStatement(delSQL);
				pst2.executeUpdate();

				if (app.loginMode == UserApplicationProfile.LOGIN_AND_QUESTION) {
					for (UserApplicationProfile.QuestionAnswer qa : app.getQuestionAnswer()) {
						insertURL = "insert into QA(DOCID, APP, QUESTION, ANSWER)values(" + user.docID + ",'" + app.appName + "','"
						        + qa.controlQuestion + "','" + qa.answer + "')";
						PreparedStatement pst1 = conn.prepareStatement(insertURL);
						pst1.executeUpdate();
					}
				}
			}
			conn.commit();
			pst.close();
			return 1;
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
			return -1;
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Override
	public User reloadUserData(User user, int hash) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String sql = "select * from USERS where LOGINHASH = " + hash;
			ResultSet rs = s.executeQuery(sql);

			if (rs.next()) {
				user.fill(rs);
				if (user.isValid) {
					String addSQL = "select * from ENABLEDAPPS where ENABLEDAPPS.DOCID=" + user.docID;
					Statement statement = conn.createStatement();
					ResultSet resultSet = statement.executeQuery(addSQL);
					while (resultSet.next()) {
						UserApplicationProfile ap = new UserApplicationProfile(resultSet.getString("APP"), resultSet.getInt("LOGINMODE"));
						user.enabledApps.put(ap.appName, ap);
					}
					resultSet.close();
					statement.close();
				}
			} else {
				user.setUserID("anonymous");
			}

			rs.close();
			s.close();
			conn.commit();
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return user;
	}

	@Override
	public ArrayList<User> getUsers(String keyWord) {
		ArrayList<User> users = new ArrayList<User>();

		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String sql = "select * from USERS where USERS.USERID LIKE '" + keyWord + "%'";
			ResultSet rs = s.executeQuery(sql);

			while (rs.next()) {
				User user = new User();
				user.fill(rs);
				users.add(user);
			}
			rs.close();
			s.close();
			conn.commit();
		} catch (Throwable e) {
			DatabaseUtil.debugErrorPrint(e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return users;
	}

	private boolean checkHash(String hashAsString, int hash) {
		try {
			int userHash = Integer.parseInt(hashAsString);
			if (userHash == hash) {
				return true;
			} else {
				return false;
			}
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

	private boolean checkHashPSW(String hashPSW, String hashPSWDB) {
		try {

			// int userHash = hashPSW;
			if (hashPSW.equals(hashPSWDB)) {
				return true;
			} else {
				return false;
			}
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

	private boolean dropUserTable(String tableName) {
		Connection conn = dbPool.getConnection();
		boolean dropUserTab = false;
		try {
			conn.setAutoCommit(false);
			if (hasUserTable(tableName)) {
				Statement s = conn.createStatement();
				s.execute("drop table " + tableName);
			}
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.debugErrorPrint(e);
			dropUserTab = false;
		} finally {
			dbPool.returnConnection(conn);
		}
		return dropUserTab;

	}

	@Override
	public String getOwnerID() {
		return "System";
	}

}
