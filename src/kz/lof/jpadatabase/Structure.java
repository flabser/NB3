package kz.lof.jpadatabase;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseConst;
import kz.flabs.dataengine.DatabaseCore;
import kz.flabs.dataengine.DatabaseFactory;
import kz.flabs.dataengine.DatabaseType;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IQueryFormula;
import kz.flabs.dataengine.ISelectFormula;
import kz.flabs.dataengine.IStructure;
import kz.flabs.dataengine.ISystemDatabase;
import kz.flabs.dataengine.h2.Database;
import kz.flabs.dataengine.h2.queryformula.StructSelectFormula;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.LicenseException;
import kz.flabs.exception.LicenseExceptionType;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.runtimeobj.Application;
import kz.flabs.runtimeobj.Filter;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.BlobField;
import kz.flabs.runtimeobj.document.BlobFile;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.glossary.Glossary;
import kz.flabs.runtimeobj.document.structure.Department;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.document.structure.Organization;
import kz.flabs.runtimeobj.document.structure.UserGroup;
import kz.flabs.runtimeobj.document.structure.UserRole;
import kz.flabs.runtimeobj.document.structure.UserRoleType;
import kz.flabs.runtimeobj.viewentry.ViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntryCollection;
import kz.flabs.runtimeobj.viewentry.ViewEntryType;
import kz.flabs.users.RunTimeParameters;
import kz.flabs.users.User;
import kz.flabs.util.Util;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.Role;
import kz.flabs.webrule.constants.FieldType;
import kz.nextbase.script._Session;
import kz.nextbase.script._ViewEntryCollection;
import kz.pchelka.env.Environment;

import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import staff.dao.EmployeeDAO;

public class Structure extends DatabaseCore implements IStructure, Const {
	public int numberOfLicense = 200;
	public IDBConnectionPool dbPool;

	protected IDatabase db;

	private static final String empSelectFields = "e.EMPID, e.DEPID, e.BOSSID, e.ORGID, e.USERID,"
	        + " e.DOCTYPE, e.LASTUPDATE, e.AUTHOR, e.REGDATE, e.DDBID,  e.VIEWTEXT,"
	        + " e.VIEWICON, e.FORM, e.SYNCSTATUS, e.FULLNAME, e.SHORTNAME, e.COMMENT, e.POST,"
	        + " e.PARENTDOCID, e.PARENTDOCTYPE, e.ISBOSS, e.INDEXNUMBER, e.RANK, e.PHONE, e.SENDTO, e.OBL, e.REGION, e.VILLAGE, e.BIRTHDATE, e.STATUS, e.VIEWNUMBER, e.VIEWDATE, "
	        + DatabaseUtil.getViewTextList("e");

	public Structure(IDatabase db, IDBConnectionPool dbPool) {
		this.db = db;
		this.dbPool = dbPool;
	}

	@Override
	public IDatabase getParent() {
		return db;
	}

	@Override
	public void setConnectionPool(IDBConnectionPool pool) {
		this.dbPool = pool;

	}

	@Override
	public StringBuffer getStructure(Set<DocID> toExpand) {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT * FROM ORGANIZATIONS";
			ResultSet rs = s.executeQuery(sql);

			while (rs.next()) {
				String respAttr = "";
				int orgID = rs.getInt("ORGID");
				String viewText = rs.getString("VIEWTEXT");
				if (hasResponse(orgID, DOCTYPE_ORGANIZATION)) {
					respAttr = " hasresponse=\"true\"";
				}
				xmlContent.append("<entry  doctype=\"" + DOCTYPE_ORGANIZATION + "\" " + "docid=\"" + orgID + "\" "
				        + XMLUtil.getAsAttribute("viewtext", viewText) + "url=\"Provider?type=organization&amp;id=o&amp;key=" + orgID + "\""
				        + respAttr + "><viewtext>" + XMLUtil.getAsTagValue(viewText) + "</viewtext><responses>");

				if (toExpand != null && toExpand.size() > 0) {
					for (DocID doc : toExpand) {
						if (doc.type == DOCTYPE_ORGANIZATION) {
							if (doc.id == orgID) {
								xmlContent.append(getEmployers(orgID, DOCTYPE_ORGANIZATION, toExpand));
								xmlContent.append(getDepartments(orgID, DOCTYPE_ORGANIZATION, toExpand));
							}
						}
					}
				}
				xmlContent.append("</responses></entry>");
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
	public StringBuffer getExpandedStructure() {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT * FROM ORGANIZATIONS";
			ResultSet rs = s.executeQuery(sql);

			while (rs.next()) {
				int orgID = rs.getInt("ORGID");
				String viewText = rs.getString("VIEWTEXT");
				xmlContent.append("<entry  doctype=\"" + DOCTYPE_ORGANIZATION + "\" " + "dbid=\"" + db.getDbID() + "\" docid=\"" + orgID + "\" "
				        + XMLUtil.getAsAttribute("viewtext", viewText) + "url=\"Provider?type=structure&amp;id=organization&amp;key=" + orgID
				        + "&amp;dbid=" + db.getDbID() + "\"><viewtext>" + XMLUtil.getAsTagValue(viewText) + "</viewtext><responses>");
				xmlContent.append(getAllEntries(orgID, DOCTYPE_ORGANIZATION, conn));
				xmlContent.append("</responses></entry>");
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

	private StringBuffer getAllEntries(int docID, int docType, Connection conn) {
		StringBuffer xmlContent = new StringBuffer(10000);
		try {
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT DEPID id, NULL userid, DOCTYPE, VIEWTEXT, RANK, VIEWTEXT1, VIEWTEXT2 " + " FROM DEPARTMENTS d"
			        + " WHERE d.PARENTDOCID = " + docID + " AND d.PARENTDOCTYPE = " + docType + " UNION"
			        + " SELECT EMPID id, USERID, DOCTYPE, VIEWTEXT, RANK, VIEWTEXT1, VIEWTEXT2 " + " FROM EMPLOYERS e" + " WHERE e.PARENTDOCID = "
			        + docID + " AND e.PARENTDOCTYPE = " + docType + " ORDER BY DOCTYPE DESC, RANK, VIEWTEXT";
			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				int id = rs.getInt("ID");
				int type = rs.getInt("DOCTYPE");
				String typeStr = "";
				String viewText = rs.getString("VIEWTEXT");
				String viewText1 = rs.getString("VIEWTEXT1");
				String viewText2 = rs.getString("VIEWTEXT2");
				String userID = rs.getString("USERID");
				// String ddbID = rs.getString("DDBID");
				if (type == DOCTYPE_EMPLOYER) {
					typeStr = "employer";
				} else if (type == DOCTYPE_DEPARTMENT) {
					typeStr = "department";
				}
				xmlContent.append("<entry doctype=\"" + type + "\" " + "dbid=\"" + db.getDbID() + "\" docid=\"" + id + "\" "
				        + XMLUtil.getAsAttribute("viewtext", viewText) + "url=\"Provider?type=structure&amp;id=" + typeStr + "&amp;key=" + id
				        + "\"><viewtext>" + XMLUtil.getAsTagValue(viewText) + "</viewtext><viewtext1>" + XMLUtil.getAsTagValue(viewText1)
				        + "</viewtext1><viewtext2>" + XMLUtil.getAsTagValue(viewText2) + "</viewtext2>");
				if (type == DOCTYPE_EMPLOYER) {
					xmlContent.append("<userid>" + userID + "</userid>");
				}
				xmlContent.append(getAllEntries(id, type, conn));
				xmlContent.append("</entry>");
			}
		} catch (SQLException se) {
			DatabaseUtil.errorPrint(db.getDbID(), se);
		}
		return xmlContent;
	}

	@Override
	public Employer getAppUser(String user) {

		EmployeeDAO dao = new EmployeeDAO(new _Session(db.getParent(), new User(User.sysUser), db.getParent()));
		staff.model.Employee e = dao.findByLogin(user);
		if (e != null) {
			Employer emp = new Employer(this);
			emp.setDdbID(e.getId().toString());
			emp.setFullName(e.getName());
			emp.setShortName(e.getName());
			return emp;
		} else {
			return null;
		}

	}

	@Override
	public Employer getAppUserByCondition(String condition) {
		Employer emp = null;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT " + empSelectFields + " FROM EMPLOYERS e WHERE e." + condition;
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				emp = fillEmpDoc(rs, conn);
				fillBlobs(conn, emp, "EMPLOYERS");
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return emp;

	}

	@Override
	public ArrayList<Employer> getAppUsersByRoles(String rolename) {
		ArrayList<Employer> emps = new ArrayList<Employer>();
		Employer emp = null;
		Connection conn = dbPool.getConnection();

		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT distinct " + empSelectFields + " FROM EMPLOYERS e, USER_ROLES ur " + " WHERE e.empid=ur.empid and ur.name='"
			        + rolename + "'";
			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				emp = fillEmpDoc(rs, conn);
				emps.add(emp);
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}

		return emps;
	}

	@Override
	public int getStructObjByConditionCount(IQueryFormula nf) {
		int count = 0;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = nf.getSQLCount(new HashSet<String>(Arrays.asList(Const.observerGroup)));
			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				count += rs.getInt(1);
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return count;
	}

	@Override
	public StringBuffer getStructObjByCondition(IQueryFormula queryCondition, int offset, int pageSize) {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = queryCondition.getSQL(new HashSet<String>(Arrays.asList(Const.observerGroup)));
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

	@Override
	public int insertOrganization(Organization doc) {
		int key = 0;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String orgSQL = "INSERT INTO ORGANIZATIONS (" + " AUTHOR," + " REGDATE, " + " DOCTYPE," + " PARENTDOCID," + " PARENTDOCTYPE,"
			        + " LASTUPDATE," + " VIEWTEXT," + " VIEWICON," + " FORM," + " DDBID, " + " FULLNAME," + " SHORTNAME," + " ADDRESS,"
			        + " DEFAULTSERVER," + " COMMENT," + " ISMAIN, " + "VIEWNUMBER," + "VIEWDATE, " + "BIN,"
			        + DatabaseUtil.getViewTextList("")
			        + ") VALUES ('"
			        + doc.getAuthorID()
			        + "', '"
			        + Database.sqlDateTimeFormat.format(new Date())
			        + "', "
			        + doc.docType
			        + ", "
			        + doc.parentDocID
			        + ", "
			        + doc.parentDocType
			        + ", '"
			        + Database.sqlDateTimeFormat.format(new Date())
			        + "', '"
			        + doc.getViewText().replace("'", "''")
			        + "', '"
			        + doc.getViewIcon()
			        + "', 'O', '"
			        + doc.getDdbID()
			        + "', '"
			        + doc.getFullName().replace("'", "''")
			        + "', '"
			        + doc.getShortName().replace("'", "''")
			        + "', '"
			        + doc.getAddress().replace("'", "''")
			        + "', '"
			        + doc.getDefaultServer()
			        + "', '"
			        + doc.getComment().replace("'", "''")
			        + "', "
			        + doc.getIsMain()
			        + ", "
			        + doc.getViewNumber()
			        + ", "
			        + (doc.getViewDate() != null ? "'" + Database.sqlDateTimeFormat.format(doc.getViewDate()) + "'" : "null")
			        + ", '"
			        + doc.getBIN()
			        + "'," + DatabaseUtil.getViewTextValues(doc) + ")";
			PreparedStatement pst = conn.prepareStatement(orgSQL, PreparedStatement.RETURN_GENERATED_KEYS);
			pst.executeUpdate();
			ResultSet rs = pst.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);
			}
			String ddbid = doc.getDdbID();
			pst = conn.prepareStatement("INSERT INTO STRUCTURE_TREE_PATH (ANCESTOR, DESCENDANT, LENGTH) " + " SELECT stp.ANCESTOR, '" + ddbid
			        + "', stp.LENGTH + 1 FROM STRUCTURE_TREE_PATH as stp WHERE stp.DESCENDANT = '" + doc.getParentDocumentID()
			        + "' UNION ALL SELECT '" + ddbid + "', '" + ddbid + "', 0");
			pst.executeUpdate();
			conn.commit();
			pst.close();
			return key;
		} catch (DocumentException de) {
			AppEnv.logger.errorLogEntry(de);
			return -1;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Override
	public int updateOrganization(Organization doc) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement statProject = conn.createStatement();
			String viewTextList = "";
			for (int i = 1; i <= DatabaseConst.VIEWTEXT_COUNT; i++) {
				viewTextList += "VIEWTEXT" + i + " = '" + doc.getViewTextList().get(i).replaceAll("'", "''") + "',";
			}
			if (viewTextList.endsWith(",")) {
				viewTextList = viewTextList.substring(0, viewTextList.length() - 1);
			}
			String updateEmp = "update ORGANIZATIONS set " + "LASTUPDATE = '" + Database.sqlDateTimeFormat.format(new Date()) + "', "
			        + "VIEWTEXT = '" + doc.getViewText().replace("'", "''") + "', VIEWICON = '" + doc.getViewIcon() + "', " + "FULLNAME = '"
			        + doc.getFullName().replace("'", "''") + "', " + "DDBID = '" + doc.getDdbID() + "', " + "ISMAIN = " + doc.getIsMain() + ", "
			        + "BIN = '" + doc.getBIN() + "', " + "SHORTNAME = '" + doc.getShortName().replace("'", "''") + "', COMMENT = '"
			        + doc.getComment().replace("'", "''") + "', " + "ADDRESS = '" + doc.getAddress().replace("'", "''") + "', " + "VIEWNUMBER = "
			        + doc.getViewNumber() + ", " + "VIEWDATE = "
			        + (doc.getViewDate() != null ? "'" + Database.sqlDateTimeFormat.format(doc.getViewDate()) + "'" : "null") + ", " + viewTextList
			        + " where ORGID = " + doc.getDocID();
			statProject.executeUpdate(updateEmp);
			statProject.close();
			updateEmp = "DELETE FROM STRUCTURE_TREE_PATH " + " WHERE DESCENDANT IN (SELECT DESCENDANT FROM STRUCTURE_TREE_PATH WHERE ANCESTOR = '"
			        + doc.getDdbID() + "') " + " AND ANCESTOR IN (SELECT ANCESTOR FROM STRUCTURE_TREE_PATH WHERE DESCENDANT = '" + doc.getDdbID()
			        + "' AND ANCESTOR != DESCENDANT)";
			PreparedStatement pst = conn.prepareStatement(updateEmp);
			pst.executeUpdate();
			updateEmp = "INSERT INTO STRUCTURE_TREE_PATH (ANCESTOR, DESCENDANT, LENGTH) "
			        + " SELECT supertree.ANCESTOR, subtree.descendant, supertree.LENGTH + subtree.LENGTH + 1 as length "
			        + " FROM STRUCTURE_TREE_PATH as supertree " + " CROSS JOIN STRUCTURE_TREE_PATH as subtree " + " WHERE supertree.descendant = '"
			        + doc.getParentDocumentID() + "' AND subtree.ancestor = '" + doc.getDdbID() + "'";
			pst = conn.prepareStatement(updateEmp);
			pst.executeUpdate();
			pst.close();
			conn.commit();
		} catch (DocumentException de) {
			AppEnv.logger.errorLogEntry(de);
			return -1;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;
		} finally {
			dbPool.returnConnection(conn);
		}
		return doc.getDocID();
	}

	@Override
	public int deleteOrganization(int id) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String delSQL = "DELETE FROM ORGANIZATIONS WHERE ORGID = " + Integer.toString(id);
			s.executeUpdate(delSQL);
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return 0;
	}

	@Override
	public int deleteEmployer(int id) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement();
			String delSQL = "DELETE FROM EMPLOYERS WHERE EMPID = " + Integer.toString(id);
			s.executeUpdate(delSQL);
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return 0;
	}

	public int hasUnit(String unitName, int unitType) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String tableName = DatabaseUtil.getMainTableName(unitType);
			String sql = "SELECT * FROM " + tableName + " WHERE FULLNAME LIKE '" + unitName + "' or SHORTNAME LIKE '" + unitName + "'";
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				return rs.getInt(1);
			}
			conn.commit();
			rs.close();
			s.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return 0;
	}

	@Override
	public Organization getOrganization(int id, User user) {
		Organization org = null;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT * FROM ORGANIZATIONS o WHERE o.ORGID=" + id;
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				org = fillOrgDoc(rs);
				if (user.isSupervisor()) {
					org.editMode = EDITMODE_EDIT;
				} else {
					org.editMode = EDITMODE_READONLY;
				}
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return org;
	}

	@Override
	public _ViewEntryCollection getOrganization(ISelectFormula sf, User user, int pageNum, int pageSize, RunTimeParameters parameters) {
		ViewEntryCollection coll = new ViewEntryCollection(pageSize, user, parameters);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			if (pageNum == 0) {
				String sql = sf.getCountCondition(Const.supervisorGroupAsSet, parameters.getFilters());
				ResultSet rs = s.executeQuery(sql);
				if (rs.next()) {
					pageNum = RuntimeObjUtil.countMaxPage(rs.getInt(1), pageSize);
				}
			}
			int offset = db.calcStartEntry(pageNum, pageSize);
			String sql = sf.getCondition(Const.supervisorGroupAsSet, pageSize, offset, parameters.getFilters(), parameters.getSorting(), true);
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				ViewEntry entry = new ViewEntry(this.db, rs, ViewEntryType.ORGANIZATION);
				coll.add(entry);
				coll.setCount(rs.getInt(1));
				while (rs.next()) {
					entry = new ViewEntry(this.db, rs, new HashSet<DocID>(), new User(Const.sysUser), parameters.getDateFormat());
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

	@Override
	public int insertDepartment(Department doc) {
		int key = 0;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String depSQL = "INSERT INTO DEPARTMENTS (" + " AUTHOR," + " REGDATE, " + " DOCTYPE," + " PARENTDOCID," + " PARENTDOCTYPE,"
			        + " LASTUPDATE," + " VIEWTEXT," + " VIEWICON," + " DDBID," + " FORM," + " SYNCSTATUS," + " FULLNAME," + " SHORTNAME,"
			        + " COMMENT," + " INDEXNUMBER," + " RANK," + " TYPE," + " ORGID," + " EMPID," + " MAINID, " + " VIEWNUMBER, " + " VIEWDATE, "
			        + DatabaseUtil.getViewTextList("")
			        + ") VALUES ('"
			        + doc.getAuthorID()
			        + "', '"
			        + Database.sqlDateTimeFormat.format(new Date())
			        + "', "
			        + doc.docType
			        + ", "
			        + doc.parentDocID
			        + ", "
			        + doc.parentDocType
			        + ", '"
			        + Database.sqlDateTimeFormat.format(new Date())
			        + "', '"
			        + doc.getViewText().replace("'", "''")
			        + "', '"
			        + doc.getViewIcon()
			        + "', '"
			        + doc.getDdbID()
			        + "', 'D', 0, '"
			        + doc.getFullName().replace("'", "''")
			        + "', '"
			        + doc.getShortName().replace("'", "''")
			        + "', '"
			        + doc.getComment().replace("'", "''")
			        + "', '"
			        + doc.getIndex()
			        + "', "
			        + doc.getRank()
			        + ", "
			        + doc.getType()
			        + ", "
			        + (doc.getOrgID() == 0 ? "null" : doc.getOrgID())
			        + ", "
			        + (doc.getEmpID() == 0 ? "null" : doc.getEmpID())
			        + ", "
			        + (doc.getMainID() == 0 ? "null" : doc.getMainID())
			        + ", "
			        + doc.getViewNumber()
			        + ", "
			        + (doc.getViewDate() != null ? "'" + Database.sqlDateTimeFormat.format(doc.getViewDate()) + "'" : "null")
			        + ", "
			        + DatabaseUtil.getViewTextValues(doc) + ")";
			PreparedStatement pst = conn.prepareStatement(depSQL, PreparedStatement.RETURN_GENERATED_KEYS);
			pst.executeUpdate();
			ResultSet rs = pst.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);
			}
			pst = conn.prepareStatement("INSERT INTO USER_GROUPS (EMPID, GROUPID, TYPE) VALUES (?, ?, ?)");
			for (UserGroup group : doc.getGroups()) {
				pst.setInt(1, key);
				pst.setInt(2, group.getDocID());
				pst.setInt(3, Const.DOCTYPE_DEPARTMENT);
				pst.executeUpdate();
			}
			String ddbid = doc.getDdbID();
			pst = conn.prepareStatement("INSERT INTO STRUCTURE_TREE_PATH (ANCESTOR, DESCENDANT, LENGTH) " + " SELECT stp.ANCESTOR, '" + ddbid
			        + "', stp.LENGTH + 1 FROM STRUCTURE_TREE_PATH as stp WHERE stp.DESCENDANT = '" + doc.getParentDocumentID()
			        + "' UNION ALL SELECT '" + ddbid + "', '" + ddbid + "', 0");
			pst.executeUpdate();
			rs.close();
			pst.close();
			pst.close();
			conn.commit();
			return key;
		} catch (DocumentException de) {
			AppEnv.logger.errorLogEntry(de);
			return -1;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Override
	public int updateDepartment(Department doc) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement statProject = conn.createStatement();
			String viewTextList = "";
			for (int i = 1; i <= DatabaseConst.VIEWTEXT_COUNT; i++) {
				viewTextList += "VIEWTEXT" + i + " = '" + doc.getViewTextList().get(i).replaceAll("'", "''") + "',";
			}
			if (viewTextList.endsWith(",")) {
				viewTextList = viewTextList.substring(0, viewTextList.length() - 1);
			}
			String updateEmp = "update DEPARTMENTS set " + "LASTUPDATE = '" + Database.sqlDateTimeFormat.format(doc.getLastUpdate()) + "', "
			        + "VIEWTEXT = '" + doc.getViewText().replace("'", "''") + "', VIEWICON = '" + doc.getViewIcon() + "', ORGID = "
			        + (doc.getOrgID() == 0 ? "null" : doc.getOrgID()) + ",  " + "MAINID = " + (doc.getMainID() == 0 ? "null" : doc.getMainID())
			        + ", EMPID = " + (doc.getEmpID() == 0 ? "null" : doc.getEmpID()) + ", " + "FULLNAME = '" + doc.getFullName().replace("'", "''")
			        + "', " + "SHORTNAME = '" + doc.getShortName().replace("'", "''") + "', COMMENT = '" + doc.getComment().replace("'", "''")
			        + "', " + "INDEXNUMBER = '" + doc.getIndex().replace("'", "''") + "', RANK = " + doc.getRank() + ", " + "TYPE = " + doc.getType()
			        + ", " + "HITS = " + doc.getHits() + ", " + "PARENTDOCID = " + doc.parentDocID + ", PARENTDOCTYPE = " + doc.parentDocType + ", "
			        + "VIEWNUMBER = " + doc.getViewNumber() + ", " + "VIEWDATE = "
			        + (doc.getViewDate() != null ? "'" + Database.sqlDateTimeFormat.format(doc.getViewDate()) + "'" : "null") + ", " + viewTextList
			        + " where DEPID = " + doc.getDocID();
			statProject.executeUpdate(updateEmp);
			updateEmp = "DELETE FROM USER_GROUPS WHERE EMPID = " + doc.getDocID() + " AND TYPE = " + Const.DOCTYPE_DEPARTMENT;
			statProject.executeUpdate(updateEmp);
			PreparedStatement pst = conn.prepareStatement("INSERT INTO USER_GROUPS (EMPID, GROUPID, TYPE) VALUES (?, ?, ?)");
			for (UserGroup group : doc.getGroups()) {
				pst.setInt(1, doc.getDocID());
				pst.setInt(2, group.getDocID());
				pst.setInt(3, Const.DOCTYPE_DEPARTMENT);
				pst.executeUpdate();
			}
			updateEmp = "DELETE FROM STRUCTURE_TREE_PATH " + " WHERE DESCENDANT IN (SELECT DESCENDANT FROM STRUCTURE_TREE_PATH WHERE ANCESTOR = '"
			        + doc.getDdbID() + "') " + " AND ANCESTOR IN (SELECT ANCESTOR FROM STRUCTURE_TREE_PATH WHERE DESCENDANT = '" + doc.getDdbID()
			        + "' AND ANCESTOR != DESCENDANT)";
			pst = conn.prepareStatement(updateEmp);
			pst.executeUpdate();
			updateEmp = "INSERT INTO STRUCTURE_TREE_PATH (ANCESTOR, DESCENDANT, LENGTH) "
			        + " SELECT supertree.ANCESTOR, subtree.descendant, supertree.LENGTH + subtree.LENGTH + 1 as length "
			        + " FROM STRUCTURE_TREE_PATH as supertree " + " CROSS JOIN STRUCTURE_TREE_PATH as subtree " + " WHERE supertree.descendant = '"
			        + doc.getParentDocumentID() + "' AND subtree.ancestor = '" + doc.getDdbID() + "'";
			pst = conn.prepareStatement(updateEmp);
			pst.executeUpdate();
			conn.commit();
			statProject.close();
			pst.close();
		} catch (DocumentException de) {
			AppEnv.logger.errorLogEntry(de);
			return -1;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;
		} finally {
			dbPool.returnConnection(conn);
		}
		return doc.getDocID();
	}

	@Override
	public Department getDepartment(int id, User user) {
		Department dep = null;
		UserGroup group = null;
		ArrayList<UserGroup> groups = new ArrayList<>();
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT * FROM GROUPS g, USER_GROUPS ug WHERE ug.EMPID = " + Integer.toString(id) + " AND ug.TYPE = "
			        + Const.DOCTYPE_DEPARTMENT + " AND ug.GROUPID = g.GROUPID";
			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				group = fillGroupDoc(rs);
				if (group.isValid) {
					groups.add(group);
				}
			}
			sql = "SELECT * FROM DEPARTMENTS WHERE DEPID=" + id;
			rs = s.executeQuery(sql);
			if (rs.next()) {
				dep = fillDepDoc(rs);
				dep.setGroups(groups);
				if (user.isSupervisor()) {
					dep.editMode = EDITMODE_EDIT;
				} else {
					dep.editMode = EDITMODE_READONLY;
				}
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return dep;
	}

	@Override
	public StringBuffer getResponses(int docID, int docType) {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "select depid, doctype, viewtext, form, 'department' as name from departments where parentdocid = " + docID
			        + " and parentdoctype = " + docType + " union "
			        + " select empid, doctype, viewtext, form, 'employer' as name from employers where parentdocid = " + docID
			        + " and parentdoctype = " + docType;

			ResultSet rs = s.executeQuery(sql);

			while (rs.next()) {
				String respAttr = "";
				int id = rs.getInt(1);
				int type = rs.getInt("DOCTYPE");
				String form = rs.getString("FORM").toLowerCase();
				String viewText = rs.getString("VIEWTEXT");
				String name = rs.getString("NAME");
				if (hasResponse(id, type)) {
					respAttr = " hasresponse=\"true\"";
				}
				xmlContent.append("<entry doctype=\"" + type + "\" " + "docid=\"" + id + "\" " + XMLUtil.getAsAttribute("viewtext", viewText)
				        + "url=\"Provider?type=" + name + "&amp;id=" + form + "&amp;key=" + id + "\"" + respAttr + ">");
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
	public StringBuffer getDepartments(int docID, int docType, Set<DocID> toExpand) {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT * FROM DEPARTMENTS d WHERE d.PARENTDOCID = " + docID + " AND d.PARENTDOCTYPE = " + docType;
			ResultSet rs = s.executeQuery(sql);

			while (rs.next()) {
				String respAttr = "";
				int depID = rs.getInt("DEPID");
				String viewText = rs.getString("VIEWTEXT");
				if (hasResponse(depID, DOCTYPE_DEPARTMENT)) {
					respAttr = " hasresponse=\"true\"";
				}
				xmlContent.append("<entry  doctype=\"" + DOCTYPE_DEPARTMENT + "\" " + "docid=\"" + depID + "\" "
				        + XMLUtil.getAsAttribute("viewtext", viewText) + "url=\"Provider?type=department&amp;id=d&amp;key=" + depID + "\"" + respAttr
				        + ">" + "<viewtext>" + XMLUtil.getAsTagValue(viewText) + "</viewtext>");

				if (toExpand != null && toExpand.size() > 0) {
					for (DocID doc : toExpand) {
						if (doc.id == depID && doc.type == DOCTYPE_DEPARTMENT) {
							StringBuffer responses = getEmployers(depID, doc.type, toExpand);
							xmlContent.append(responses);
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
	public int insertEmployer(Employer doc) throws LicenseException {
		int key = 0;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String empCount = "select count(empid) from employers";
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ResultSet rse = s.executeQuery(empCount);
			if (rse.next()) {
				int count = rse.getInt(1);
				rse.close();
				s.close();
				Application app = this.db.getParent().application;
				if (count > app.getLicenceCount()) {
					throw new LicenseException(LicenseExceptionType.NUMBER_OF_LICENSE_HAS_ENDED);
				}
			}
			String empSQL = "insert into EMPLOYERS (" + " AUTHOR," + " REGDATE, " + " DOCTYPE," + " PARENTDOCID," + " PARENTDOCTYPE,"
			        + " LASTUPDATE," + " VIEWTEXT," + " VIEWICON," + " FORM," + " DDBID," + " SYNCSTATUS," + " USERID," + " FULLNAME,"
			        + " SHORTNAME," + " COMMENT," + " INDEXNUMBER, " + " RANK, " + " PHONE, " + " SENDTO, " + " DEPID," + " ORGID," + " BOSSID,"
			        + " POST," + " ISBOSS, " + "OBL, " + "REGION, " + "VILLAGE, " + "BIRTHDATE, " + "STATUS, " + "VIEWNUMBER," + "VIEWDATE, "
			        + DatabaseUtil.getViewTextList("")
			        + ") values ('"
			        + doc.getAuthorID()
			        + "', '"
			        + Database.sqlDateTimeFormat.format(new Date())
			        + "', "
			        + doc.docType
			        + ", "
			        + doc.parentDocID
			        + ", "
			        + doc.parentDocType
			        + ", '"
			        + Database.sqlDateTimeFormat.format(new Date())
			        + "', '"
			        + doc.getViewText()
			        + "', '"
			        + doc.getViewIcon()
			        + "', '"
			        + doc.form
			        + "', '"
			        + doc.getDdbID()
			        + "', 0 , '"
			        + doc.getUserID()
			        + "','"
			        + doc.getFullName()
			        + "', '"
			        + doc.getShortName()
			        + "', '"
			        + doc.getComment()
			        + "', '"
			        + doc.getIndex()
			        + "', "
			        + doc.getRank()
			        + ", '"
			        + doc.getPhone()
			        + "', "
			        + doc.getSendto()
			        + ", "
			        + (doc.getDepID() == 0 ? "null" : doc.getDepID())
			        + ", "
			        + (doc.getOrgID() == 0 ? "null" : doc.getOrgID())
			        + ", "
			        + (doc.getBossID() == 0 ? "null" : doc.getBossID())
			        + ", "
			        + (doc.getPostID() == 0 || doc.getPostID() == 999 ? "null" : doc.getPostID())
			        + ", "
			        + (doc.isBoss() ? "1" : "0")
			        + ", "
			        + doc.getObl()
			        + ", "
			        + doc.getRegion()
			        + ", "
			        + doc.getVillage()
			        + ", "
			        + (doc.getBirthDate() != null ? "'" + Database.sqlDateTimeFormat.format(doc.getBirthDate()) + "'" : "null")
			        + ", "
			        + doc.getStatus()
			        + ", "
			        + doc.getViewNumber()
			        + ", "
			        + (doc.getViewDate() != null ? "'" + Database.sqlDateTimeFormat.format(doc.getViewDate()) + "'" : "null")
			        + ", "
			        + DatabaseUtil.getViewTextValues(doc) + ")";
			PreparedStatement pst = conn.prepareStatement(empSQL, PreparedStatement.RETURN_GENERATED_KEYS);
			pst.executeUpdate();

			ResultSet rs = pst.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);
				// doc.setDocID(key);
			}

			pst = conn.prepareStatement("INSERT INTO USER_ROLES (empid, name, type, appid) VALUES (?, ?, ?, ?)");
			for (UserRole role : doc.getRoles()) {
				pst.setInt(1, key);
				pst.setString(2, role.getName());
				pst.setInt(3, doc.docType);
				pst.setString(4, role.getApplication());
				pst.executeUpdate();
			}
			pst = conn.prepareStatement("INSERT INTO USER_GROUPS (empid, groupid) VALUES (?, ?)");
			for (UserGroup group : doc.getGroups()) {
				pst.setInt(1, key);
				pst.setInt(2, group.getDocID());
				pst.executeUpdate();
			}
			pst = conn.prepareStatement("DELETE FROM FILTER WHERE USERID = '" + doc.getUserID() + "'");
			pst.executeUpdate();
			String sql = "INSERT INTO FILTER (USERID, NAME, ENABLE) VALUES (?, ?, ?)";
			for (Filter filter : doc.getFilters()) {
				int filterID = filter.getFilterID();
				if (filterID != 0) {
					sql = "INSERT INTO FILTER (ID, USERID, NAME, ENABLE) VALUES (" + filterID + ", ?, ?, ?)";
				}
				pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				pst.setString(1, doc.getUserID());
				pst.setString(2, filter.getName());
				pst.setInt(3, filter.getEnable());
				pst.executeUpdate();
				rs = pst.getGeneratedKeys();
				int filterKey;
				if (rs.next()) {
					filterKey = rs.getInt(1);
					filterID = filterKey;
				} else {
					filterKey = filterID;
				}
				HashMap<String, String> conds = filter.getConditions();
				PreparedStatement conpst = conn.prepareStatement("INSERT INTO CONDITION (FID, NAME, VALUE) VALUES (?, ?, ?)");
				for (String conName : conds.keySet()) {
					conpst.setInt(1, filterKey);
					conpst.setString(2, conName);
					conpst.setString(3, conds.get(conName));
					conpst.executeUpdate();
				}
				conpst.close();
			}
			String ddbid = doc.getDdbID();
			pst = conn.prepareStatement("INSERT INTO STRUCTURE_TREE_PATH (ANCESTOR, DESCENDANT, LENGTH) " + " SELECT stp.ANCESTOR, '" + ddbid
			        + "', stp.LENGTH + 1 FROM STRUCTURE_TREE_PATH as stp WHERE stp.DESCENDANT = '" + doc.getParentDocumentID()
			        + "' UNION ALL SELECT '" + ddbid + "', '" + ddbid + "', 0");
			pst.executeUpdate();
			pst.close();
			recoverRelations(conn, doc, key);
			conn.commit();
			return key;
		} catch (DocumentException de) {
			AppEnv.logger.errorLogEntry(de);
			return -1;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	// TODO rewrite code for h2
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
						pst = conn.prepareStatement("INSERT INTO CUSTOM_BLOBS_" + tableName
						        + " (DOCID, NAME, ORIGINALNAME, CHECKSUM, COMMENT, VALUE_OID, REGDATE)VALUES(?, ?, ?, ?, ?, ?, ?)",
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
			sql = "UPDATE CUSTOM_BLOBS_" + tableName + " SET DOCID = ? WHERE docid = ?"
			        + (ids.size() > 0 ? " and id not in (" + StringUtils.join(ids, ",") + ") " : "");
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

	@Override
	public int updateEmployer(Employer doc) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement statement = conn.createStatement();
			String viewTextList = "";
			for (int i = 1; i <= DatabaseConst.VIEWTEXT_COUNT; i++) {
				viewTextList += "VIEWTEXT" + i + " = '" + doc.getViewTextList().get(i).replaceAll("'", "''") + "',";
			}
			if (viewTextList.endsWith(",")) {
				viewTextList = viewTextList.substring(0, viewTextList.length() - 1);
			}
			String updateEmp = "UPDATE EMPLOYERS SET " + "LASTUPDATE = '" + Database.sqlDateTimeFormat.format(new Date()) + "', " + "VIEWTEXT = '"
			        + doc.getViewText() + "', VIEWICON = '" + doc.getViewIcon() + "', " + "FULLNAME = '" + doc.getFullName() + "', " + "FORM = '"
			        + doc.form + "', " + "DDBID = '" + doc.getDdbID() + "', " + "INDEXNUMBER = '" + doc.getIndex() + "', PHONE = '" + doc.getPhone()
			        + "', " + "RANK = " + doc.getRank() + ", SENDTO = " + doc.getSendto() + ", " + "PARENTDOCTYPE = " + doc.parentDocType
			        + ", PARENTDOCID = " + doc.parentDocID + ", " + "OBL = " + doc.getObl() + ", REGION = " + doc.getRegion() + ", VILLAGE = "
			        + doc.getVillage() + ", " + "ORGID = " + (doc.getOrgID() == 0 ? "null" : doc.getOrgID()) + ", DEPID = "
			        + (doc.getDepID() == 0 ? "null" : doc.getDepID()) + ", " + "BOSSID = " + (doc.getBossID() == 0 ? "null" : doc.getBossID())
			        + ", POST = " + (doc.getPostID() == 0 || doc.getPostID() == 999 ? "null" : doc.getPostID()) + ", " + "SHORTNAME = '"
			        + doc.getShortName() + "', COMMENT = '" + doc.getComment() + "', USERID = '" + doc.getUserID() + "', " + "BIRTHDATE = "
			        + (doc.getBirthDate() != null ? "'" + Database.sqlDateTimeFormat.format(doc.getBirthDate()) + "'" : "null") + ", " + "HITS = "
			        + doc.getHits() + ", STATUS = " + doc.getStatus() + ", " + "VIEWNUMBER = " + doc.getViewNumber() + ", " + "VIEWDATE = "
			        + (doc.getViewDate() != null ? "'" + Database.sqlDateTimeFormat.format(doc.getViewDate()) + "'" : "null") + ", " + viewTextList
			        + " WHERE EMPID = " + doc.getDocID();
			statement.executeUpdate(updateEmp);

			for (String appID : doc.getUser().enabledApps.keySet()) {
				updateEmp = "DELETE FROM USER_ROLES WHERE EMPID = " + doc.getDocID() + " AND TYPE = " + Const.DOCTYPE_EMPLOYER + " AND APPID = '"
				        + appID + "'";
				statement.executeUpdate(updateEmp);
				// updateEmp = "DELETE FROM USER_ROLES WHERE EMPID = " +
				// doc.getDocID() + " AND TYPE = " + Const.DOCTYPE_EMPLOYER;
				// statement.executeUpdate(updateEmp);
			}

			PreparedStatement pst = conn.prepareStatement("INSERT INTO USER_ROLES (empid, name, type, appid) VALUES (?, ?, ?, ?)");
			for (UserRole role : doc.getRoles()) {
				String roleName = role.getName();
				if (!roleName.equalsIgnoreCase("supervisor")) {
					pst.setInt(1, doc.getDocID());
					pst.setString(2, roleName);
					pst.setInt(3, doc.docType);
					pst.setString(4, role.getApplication());
					try {
						pst.executeUpdate();
					} catch (Exception e) {
						DatabaseUtil.errorPrint(db.getDbID(), e);
					}
					conn.commit();
				}
			}
			updateEmp = "DELETE FROM USER_GROUPS WHERE EMPID = " + doc.getDocID() + " AND TYPE = " + Const.DOCTYPE_EMPLOYER;
			statement.executeUpdate(updateEmp);
			pst = conn.prepareStatement("INSERT INTO USER_GROUPS (empid, groupid, type) VALUES (?, ?, ?)");
			for (UserGroup group : doc.getGroups()) {
				pst.setInt(1, doc.getDocID());
				pst.setInt(2, group.getDocID());
				pst.setInt(3, Const.DOCTYPE_EMPLOYER);
				pst.executeUpdate();
				conn.commit();
			}
			pst = conn.prepareStatement("DELETE FROM FILTER WHERE USERID = '" + doc.getUserID() + "'");
			pst.executeUpdate();
			String sql;
			for (Filter filter : doc.getFilters()) {
				int filterID = filter.getFilterID();
				if (filterID != 0) {
					sql = "INSERT INTO FILTER (ID, USERID, NAME, ENABLE) VALUES (" + filterID + ", ?, ?, ?)";
				} else {
					sql = "INSERT INTO FILTER (USERID, NAME, ENABLE) VALUES (?, ?, ?)";
				}
				pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				pst.setString(1, doc.getUserID());
				pst.setString(2, filter.getName());
				pst.setInt(3, filter.getEnable());
				pst.executeUpdate();
				conn.commit();
				ResultSet rs = pst.getGeneratedKeys();
				int filterKey;
				if (rs.next()) {
					filterKey = rs.getInt(1);
					filter.setFilterID(filterKey);
				} else {
					filterKey = filterID;
				}
				HashMap<String, String> conds = filter.getConditions();
				PreparedStatement conpst = conn.prepareStatement("INSERT INTO CONDITION (FID, NAME, VALUE) VALUES (?, ?, ?)");
				for (String conName : conds.keySet()) {
					conpst.setInt(1, filterKey);
					conpst.setString(2, conName);
					conpst.setString(3, conds.get(conName));
					conpst.executeUpdate();
					conn.commit();
				}
				conpst.close();
			}

			updateEmp = "DELETE FROM STRUCTURE_TREE_PATH " + " WHERE DESCENDANT IN (SELECT DESCENDANT FROM STRUCTURE_TREE_PATH WHERE ANCESTOR = '"
			        + doc.getDdbID() + "') " + " AND ANCESTOR IN (SELECT ANCESTOR FROM STRUCTURE_TREE_PATH WHERE DESCENDANT = '" + doc.getDdbID()
			        + "' AND ANCESTOR != DESCENDANT)";
			pst = conn.prepareStatement(updateEmp);
			pst.executeUpdate();
			updateEmp = "INSERT INTO STRUCTURE_TREE_PATH (ANCESTOR, DESCENDANT, LENGTH) "
			        + " SELECT supertree.ANCESTOR, subtree.descendant, supertree.LENGTH + subtree.LENGTH + 1 as length "
			        + " FROM STRUCTURE_TREE_PATH as supertree " + " CROSS JOIN STRUCTURE_TREE_PATH as subtree " + " WHERE supertree.descendant = '"
			        + doc.getParentDocumentID() + "' AND subtree.ancestor = '" + doc.getDdbID() + "'";
			pst = conn.prepareStatement(updateEmp);
			pst.executeUpdate();
			conn.commit();
			pst.close();
		} catch (DocumentException de) {
			AppEnv.logger.errorLogEntry(de);
			return -1;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;
		} finally {
			dbPool.returnConnection(conn);
		}
		return doc.getDocID();
	}

	public String getEmployerEntryByID(int empID) {
		String xmlContent = "";
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT * FROM EMPLOYERS WHERE EMPID = " + empID;
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				xmlContent = getEmployerEntry(rs);
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

	public String getUserID(int id) {
		Connection conn = dbPool.getConnection();
		String userID = "";
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT USERID FROM EMPLOYERS WHERE EMPID = " + id;
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				userID = rs.getString("USERID");
			}
			rs.close();
			s.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return userID;
	}

	@Override
	public Employer getEmployer(int id, User user) {
		Employer emp = null;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT * FROM EMPLOYERS WHERE EMPID = " + Integer.toString(id);
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				emp = fillEmpDoc(rs, conn);
				emp.setEnvironment(this.db.getParent());
				emp.getUser().setEnvironment(this.db.getParent());
				if (user.isSupervisor()) {
					emp.editMode = EDITMODE_EDIT;
				} else {
					emp.editMode = EDITMODE_READONLY;
				}
				fillBlobs(conn, emp, "EMPLOYERS");
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return emp;
	}

	public void fillBlobs(Connection conn, BaseDocument doc, String customBlobTableSuffix) throws SQLException {
		doc.blobFieldsMap.clear();
		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery("select * from CUSTOM_BLOBS_" + customBlobTableSuffix + "  where CUSTOM_BLOBS_" + customBlobTableSuffix
		        + ".DOCID = " + doc.getDocID() + " ORDER BY ID");
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

	@Override
	public int insertGroup(UserGroup doc) {
		int key = 0;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Date viewDate = doc.getViewDate();
			String groupSQL = "INSERT INTO GROUPS (" + "FORM, " + "GROUPNAME, " + "OWNER, " + "DESCRIPTION, " + "DEFAULTRULEID, " + "VIEWTEXT1, "
			        + "VIEWTEXT2, " + "VIEWTEXT3, " + "VIEWTEXT4, " + "VIEWTEXT5, " + "VIEWTEXT6, " + "VIEWTEXT7, " + "VIEWNUMBER, " + "VIEWDATE, "
			        + "PARENTDOCID," + "PARENTDOCTYPE," + "VIEWTEXT) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement pst = conn.prepareStatement(groupSQL, PreparedStatement.RETURN_GENERATED_KEYS);
			pst.setString(1, "Group");
			pst.setString(2, doc.getName());
			pst.setString(3, doc.getOwner());
			pst.setString(4, doc.getDescription() != null ? doc.getDescription().replace("'", "''") : "");
			pst.setString(5, doc.getDefaultRuleID());
			pst.setString(6, doc.getViewTextList().size() >= 2 ? doc.getViewTextList().get(1) : "");
			pst.setString(7, doc.getViewTextList().size() >= 3 ? doc.getViewTextList().get(2) : "");
			pst.setString(8, doc.getViewTextList().size() >= 4 ? doc.getViewTextList().get(3) : "");
			pst.setString(9, doc.getViewTextList().size() >= 5 ? doc.getViewTextList().get(4) : "");
			pst.setString(10, doc.getViewTextList().size() >= 6 ? doc.getViewTextList().get(5) : "");
			pst.setString(11, doc.getViewTextList().size() >= 7 ? doc.getViewTextList().get(6) : "");
			pst.setString(12, doc.getViewTextList().size() >= 8 ? doc.getViewTextList().get(7) : "");
			pst.setBigDecimal(13, doc.getViewNumber());

			if (viewDate != null) {
				pst.setTimestamp(14, new Timestamp(viewDate.getTime()));
			} else {
				pst.setNull(14, Types.TIMESTAMP);
			}
			pst.setInt(15, doc.parentDocID);
			pst.setInt(16, doc.parentDocType);
			pst.setString(17, doc.getViewText());
			pst.executeUpdate();
			ResultSet rs = pst.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);
			}
			pst = conn.prepareStatement("INSERT INTO USER_ROLES (EMPID, NAME, TYPE) VALUES (?, ?, ?) ");
			for (UserRole role : doc.getRoles()) {
				pst.setInt(1, key);
				pst.setString(2, role.getName());
				pst.setInt(3, doc.docType);
				pst.executeUpdate();
			}

			pst = conn.prepareStatement("INSERT INTO USER_GROUPS (EMPID, GROUPID, TYPE) VALUES (?, ?, ?) ");
			for (String memberID : doc.getMembers()) {
				Employer member = this.getAppUser(memberID);
				if (member != null) {
					pst.setInt(1, member.getDocID());
					pst.setInt(2, key);
					pst.setInt(3, member.docType);
					pst.executeUpdate();
					conn.commit();
				}
			}

			conn.commit();
			rs.close();
			pst.close();
			return key;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;
		} catch (DocumentException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;
		} finally {
			dbPool.returnConnection(conn);
		}
	}

	@Override
	public int updateGroup(UserGroup doc) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Date viewDate = doc.getViewDate();
			/*
			 * String updateGroup = "UPDATE GROUPS SET " + "GROUPNAME = '" +
			 * doc.getName() + "', DESCRIPTION = '" +
			 * doc.getDescription().replace("'", "''") + "', OWNER = '" +
			 * doc.getOwner() + "', DEFAULTRULEID = '" + doc.getDefaultRuleID()
			 * + "', VIEWTEXT1 = '" + doc.getViewTextList().get(0).replace("'",
			 * "''") + "', VIEWTEXT2 = '"
			 * +doc.getViewTextList().get(1).replace("'", "''") +
			 * "', VIEWTEXT3 = '" + doc.getViewTextList().get(2).replace("'",
			 * "''") + "', VIEWNUMBER = " + doc.getViewNumber() +
			 * ", VIEWDATE = " + (viewDate != null ? "'" + new
			 * Timestamp(viewDate.getTime()) + "'" : "null") +
			 * " WHERE GROUPID = " + doc.getDocID();
			 */
			String updateGroup = "UPDATE GROUPS SET GROUPNAME = ?, " + " DESCRIPTION = ?, " + " OWNER = ?, " + " DEFAULTRULEID = ?, "
			        + " VIEWTEXT1 = ?, " + " VIEWTEXT2 = ?, " + " VIEWTEXT3 = ?, " + " VIEWTEXT4 = ?, " + " VIEWTEXT5 = ?, " + " VIEWTEXT6 = ?, "
			        + " VIEWTEXT7 = ?, " + " VIEWNUMBER = ?, " + " VIEWDATE = ?, " + " PARENTDOCID = ?, " + " PARENTDOCTYPE = ?, " + " VIEWTEXT = ? "
			        + " WHERE GROUPID = ?";
			PreparedStatement pst = conn.prepareStatement(updateGroup);
			pst.setString(1, doc.getName());
			pst.setString(2, doc.getDescription());
			pst.setString(3, doc.getOwner());
			pst.setString(4, doc.getDefaultRuleID());
			pst.setString(5, doc.getViewTextList().size() >= 2 ? doc.getViewTextList().get(1) : "");
			pst.setString(6, doc.getViewTextList().size() >= 3 ? doc.getViewTextList().get(2) : "");
			pst.setString(7, doc.getViewTextList().size() >= 4 ? doc.getViewTextList().get(3) : "");
			pst.setString(8, doc.getViewTextList().size() >= 5 ? doc.getViewTextList().get(4) : "");
			pst.setString(9, doc.getViewTextList().size() >= 6 ? doc.getViewTextList().get(5) : "");
			pst.setString(10, doc.getViewTextList().size() >= 7 ? doc.getViewTextList().get(6) : "");
			pst.setString(11, doc.getViewTextList().size() >= 8 ? doc.getViewTextList().get(7) : "");
			pst.setBigDecimal(12, doc.getViewNumber());
			if (viewDate != null) {
				pst.setTimestamp(13, new Timestamp(viewDate.getTime()));
			} else {
				pst.setNull(13, Types.TIMESTAMP);
			}
			pst.setInt(14, doc.parentDocID);
			pst.setInt(15, doc.parentDocType);
			pst.setString(16, doc.getViewText());
			pst.setInt(17, doc.getDocID());
			pst.executeUpdate();
			conn.commit();

			updateGroup = "DELETE FROM USER_ROLES WHERE EMPID = " + doc.getDocID() + " AND TYPE = " + doc.docType;
			pst = conn.prepareStatement(updateGroup);
			pst.executeUpdate();
			conn.commit();
			pst = conn.prepareStatement("INSERT INTO USER_ROLES (EMPID, TYPE, NAME, APPID) VALUES (?, ?, ?, ?) ");
			for (UserRole role : doc.getRoles()) {
				pst.setInt(1, doc.getDocID());
				pst.setInt(2, doc.docType);
				pst.setString(3, role.getName());
				pst.setString(4, role.getApplication());
				pst.executeUpdate();
				conn.commit();
			}

			updateGroup = "DELETE FROM USER_GROUPS WHERE GROUPID = " + doc.getDocID();
			pst = conn.prepareStatement(updateGroup);
			pst.executeUpdate();
			conn.commit();
			pst = conn.prepareStatement("INSERT INTO USER_GROUPS (EMPID, GROUPID, TYPE) VALUES (?, ?, ?) ");
			for (String memberID : doc.getMembers()) {
				Employer member = this.getAppUser(memberID);
				if (member != null) {
					pst.setInt(1, member.getDocID());
					pst.setInt(2, doc.getDocID());
					pst.setInt(3, member.docType);
					pst.executeUpdate();
					conn.commit();
				}
			}
			conn.commit();
			pst.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;
		} catch (DocumentException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;
		} finally {
			dbPool.returnConnection(conn);
		}
		return doc.getDocID();
	}

	@Override
	public boolean hasGroup(String name, Set<String> complexUserID, String absoluteUserID) {
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT * FROM GROUPS WHERE GROUPNAME = '" + name + "'";
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				return true;
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return false;
	}

	@Override
	public UserGroup getGroup(String name, Set<String> complexUserID, String absoluteUserID) {
		UserGroup group = null;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT * FROM GROUPS WHERE GROUPNAME = '" + name + "'";
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				group = fillGroupDoc(rs);
				group.setEditMode(complexUserID);
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);

		} finally {
			dbPool.returnConnection(conn);
		}
		return group;
	}

	@Override
	public UserGroup getGroup(int id, Set<String> complexUserID, String absoluteUserID) {
		UserGroup group = null;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT * FROM GROUPS WHERE GROUPID = " + Integer.toString(id);
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				group = fillGroupDoc(rs);
				fillViewTextData(rs, group);
				group.setEditMode(complexUserID);
				group.editMode = EDITMODE_EDIT;
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);

		} finally {
			dbPool.returnConnection(conn);
		}
		return group;
	}

	@Override
	public Filter getFilter(int id, HashSet<String> complexUserID, String absoluteUserID) {
		Filter filter = null;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT * FROM FILTER WHERE ID = " + Integer.toString(id);
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				filter = fillFilterDoc(conn, rs);
				filter.setEditMode(complexUserID);
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return filter;
	}

	@Override
	public UserGroup getGroupByParent(int parentDocID, int parentDocType) {
		Connection conn = dbPool.getConnection();
		UserGroup group = new UserGroup(this);
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "select * from GROUPS g where g.PARENTDOCID = " + parentDocID + " and g.PARENTDOCTYPE = " + parentDocType;
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				group = fillGroupDoc(rs);
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return group;
	}

	@Override
	public ArrayList<BaseDocument> getAllGroups() {
		Connection conn = dbPool.getConnection();
		ArrayList<BaseDocument> groups = new ArrayList<BaseDocument>();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "select * from groups";
			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				groups.add(fillGroupDoc(rs));
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return groups;
	}

	@Override
	public int getGroupsCount() {
		Connection conn = dbPool.getConnection();
		int count = 0;
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT COUNT(*) FROM GROUPS";
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				count = rs.getInt(1);
			}
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return count;
	}

	@Override
	public StringBuffer getGroupsByCondition(IQueryFormula queryCondition) {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			FormulaBlocks blocks = queryCondition.getBlocks();
			String sql;
			if (blocks.isDirectQuery && !"".equalsIgnoreCase(blocks.sql)) {
				sql = blocks.sql;
			} else {
				sql = "SELECT * FROM GROUPS";
			}
			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				int docID = rs.getInt("GROUPID");
				String form = rs.getString("FORM");

				xmlContent.append("<entry doctype=\"" + DOCTYPE_GROUP + "\" docid=\"" + docID + "\" " + "url=\"Provider?type=structure&amp;id="
				        + form + "&amp;key=" + docID + "\"" + ">" + getShortViewContent(rs) + "</entry>");
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
	public StringBuffer getGroupsByCondition(IQueryFormula queryCondition, int offset, int pageSize, String fieldsCond, Set<String> toExpand) {
		return getGroupsByCondition(queryCondition);
	}

	@Override
	public int getGroupsCountByCondition(IQueryFormula blocks, String userName) {
		int count = 0;
		Connection conn = dbPool.getConnection();
		try {
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = blocks.getSQLCount(sysGroupAsSet);

			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();

			s.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return count;
	}

	@Override
	public StringBuffer getEmployersByFrequencyExecution() {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

			String sql = "select e.userid, e.viewtext, " + DatabaseUtil.getViewTextList("e")
			        + ", e.viewnumber, e.viewdate, e.post, e.empid, e.fullname, count(cf.value) as numEntries " + " from employers as e "
			        + " left join custom_fields as cf on cf.name = 'executer' and cf.value = e.userid " + " group by e.userid, e.viewtext, "
			        + DatabaseUtil.getViewTextList("e") + ", e.viewnumber, e.viewdate, e.post, e.empid, e.fullname "
			        + " order by count(cf.value) desc";
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

	@Override
	public StringBuffer getEmployersByRoles(String rolename) {
		StringBuffer xmlContent = new StringBuffer(10000);
		Set<Integer> employersID = new HashSet<Integer>();
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT * FROM USER_ROLES AS ur WHERE NAME = '" + rolename + "'";
			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				switch (rs.getInt("TYPE")) {
				case Const.DOCTYPE_EMPLOYER:
					employersID.add(rs.getInt("EMPID"));
					break;
				case Const.DOCTYPE_GROUP:
					Statement gs = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
					ResultSet grs = gs.executeQuery("SELECT * FROM USER_GROUPS WHERE GROUPID = " + rs.getInt("EMPID"));
					while (grs.next()) {
						switch (grs.getInt("TYPE")) {
						case Const.DOCTYPE_EMPLOYER:
							employersID.add(grs.getInt("EMPID"));
							break;
						case Const.DOCTYPE_DEPARTMENT:
							String dsql = "SELECT EMPID FROM EMPLOYERS WHERE DEPID = " + grs.getInt("EMPID");
							Statement ds = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
							ResultSet drs = ds.executeQuery(dsql);
							while (drs.next()) {
								employersID.add(drs.getInt("EMPID"));
							}
							ds.close();
							break;
						}
					}
					gs.close();
					break;
				}
			}
			xmlContent.append("<count>" + employersID.size() + "</count>");
			for (Integer id : employersID) {
				xmlContent.append(getEmployerEntryByID(id));
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

	@Override
	public StringBuffer getEmployers(int docID, int docType, Set<DocID> toExpand) {
		StringBuffer xmlContent = new StringBuffer(10000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT * FROM EMPLOYERS e WHERE e.PARENTDOCID = " + docID + " AND e.PARENTDOCTYPE = " + docType;
			ResultSet rs = s.executeQuery(sql);

			while (rs.next()) {
				xmlContent.append(getEmployerEntry(rs));
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
	public ArrayList<Employer> getAllEmployers() {
		ArrayList<Employer> result = new ArrayList<Employer>();
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = s.executeQuery("SELECT " + empSelectFields + " FROM EMPLOYERS e ORDER BY e.EMPID");
			while (rs.next()) {
				result.add(fillEmpDoc(rs, conn));
			}
			rs.close();
			s.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return result;
	}

	public Organization fillOrgDoc(ResultSet rs) {
		Organization org = new Organization(this);
		try {
			fillSysDataSimple(rs, org);
			org.setFullName(rs.getString("FULLNAME"));
			org.setShortName(rs.getString("SHORTNAME"));
			org.setAddress(rs.getString("ADDRESS"));
			org.setDefaultServer(rs.getString("DEFAULTSERVER"));
			org.setIsMain(rs.getInt("ISMAIN"));
			org.setComment(rs.getString("COMMENT"));
			org.setBIN(rs.getString("BIN"));
			fillViewTextData(rs, org);
			org.isValid = true;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		}
		return org;
	}

	public Department fillDepDoc(ResultSet rs) {
		Department doc = new Department(this);
		fillSysDataSimple(rs, doc);
		try {
			doc.setOrgID(rs.getInt("ORGID"));
			doc.setEmpID(rs.getInt("EMPID"));
			doc.setMainID(rs.getInt("MAINID"));
			doc.setFullName(rs.getString("FULLNAME"));
			doc.setShortName(rs.getString("SHORTNAME"));
			doc.setComment(rs.getString("COMMENT"));
			doc.parentDocID = rs.getInt("PARENTDOCID");
			doc.parentDocType = rs.getInt("PARENTDOCTYPE");
			doc.setIndex(rs.getString("INDEXNUMBER"));
			doc.setRank(rs.getInt("RANK"));
			doc.setType(rs.getInt("TYPE"));
			fillViewTextData(rs, doc);
			doc.isValid = true;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		}
		return doc;
	}

	public Employer fillEmpDoc(ResultSet rs, Connection conn) throws SQLException {
		UserGroup group = null;
		Employer emp = new Employer(this);
		fillSysDataSimple(rs, emp);

		emp.setDepID(rs.getInt("DEPID"));
		emp.setOrgID(rs.getInt("ORGID"));
		emp.setBossID(rs.getInt("BOSSID"));
		String userID = rs.getString("USERID");
		emp.setUserID(userID);
		emp.setFullName(rs.getString("FULLNAME"));
		emp.setShortName(rs.getString("SHORTNAME"));
		emp.setPostID(rs.getInt("POST"));
		emp.setComment(rs.getString("COMMENT"));
		emp.parentDocID = rs.getInt("PARENTDOCID");
		emp.parentDocType = rs.getInt("PARENTDOCTYPE");
		emp.setBoss(rs.getInt("ISBOSS"));
		emp.setIndex(rs.getString("INDEXNUMBER"));
		emp.setRank(rs.getInt("RANK"));
		emp.setPhone(rs.getString("PHONE"));
		emp.setSendTo(rs.getInt("SENDTO"));
		emp.setObl(rs.getInt("OBL"));
		emp.setRegion(rs.getInt("REGION"));
		emp.setVillage(rs.getInt("VILLAGE"));
		emp.setBirthDate(rs.getTimestamp("BIRTHDATE"));
		emp.setStatus(rs.getInt("STATUS"));
		fillViewTextData(rs, emp);
		emp.isValid = true;

		ISystemDatabase sysDB = DatabaseFactory.getSysDatabase();
		User user = sysDB.getUser(userID);
		emp.setUser(user);
		user.setAppUser(emp);
		emp.addField("email", user.getEmail(), FieldType.TEXT);
		emp.addField("instmsgaddress", user.getInstMsgAddress(), FieldType.TEXT);

		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		String sql = "SELECT * FROM USER_ROLES ur WHERE ur.EMPID = " + emp.getDocID() + " AND ur.TYPE = " + Const.DOCTYPE_EMPLOYER;
		ResultSet roleResultSet = st.executeQuery(sql);
		while (roleResultSet.next()) {
			String empRole = roleResultSet.getString("NAME");
			String app = roleResultSet.getString("APPID");
			Role roleObj = db.getAppRoles().get(empRole + "#" + app);

			if (roleObj != null) {
				UserRole userRole = new UserRole(roleObj);
				emp.addRole(userRole);
			}
		}
		roleResultSet.close();
		conn.commit();

		if (user.isSupervisor()) {
			Role role = new Role("supervisor", "");
			UserRole ur = new UserRole(role);
			emp.addRole(ur);
		}

		st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		sql = "SELECT * FROM GROUPS g, USER_GROUPS ug WHERE ug.EMPID = " + emp.getDocID() + " AND g.GROUPID = ug.GROUPID AND ug.TYPE = "
		        + Const.DOCTYPE_EMPLOYER;
		ResultSet groupResultSet = st.executeQuery(sql);
		while (groupResultSet.next()) {
			group = getGroup(groupResultSet.getInt("GROUPID"), sysGroupAsSet, sysUser);
			if (group.isValid) {
				emp.addGroup(group);
			}
		}
		groupResultSet.close();
		conn.commit();
		sql = "SELECT * FROM FILTER f WHERE USERID = '" + emp.getUserID() + "'";
		ResultSet filterResultSet = st.executeQuery(sql);
		Filter filter = null;
		while (filterResultSet.next()) {
			filter = getFilter(filterResultSet.getInt("ID"), sysGroupAsSet, sysUser);
			if (filter.isValid) {
				emp.addFilter(filter);
			}
		}
		filterResultSet.close();
		conn.commit();
		return emp;
	}

	protected String getEmployerEntry(ResultSet rs) throws SQLException {
		int empID = rs.getInt("EMPID");
		int postID = rs.getInt("POST");
		String postEntry = "";

		if (postID != 0) {
			Glossary post = this.db.getGlossaries().getGlossaryDocumentByID(postID);
			if (post != null) {
				postEntry = "<post>" + post.getViewText() + "</post>";
			}
		}
		String value = "<entry  doctype=\"" + DOCTYPE_EMPLOYER + "\" " + "docid=\"" + empID + "\" "
		        + "url=\"Provider?type=structure&amp;id=employer&amp;key=" + empID + "\">" + getViewContent(rs) + "<userid>" + rs.getString("USERID")
		        + "</userid>" + postEntry + "</entry>";
		return value;
	}

	private UserGroup fillGroupDoc(ResultSet rs) {
		UserGroup group = new UserGroup(this);
		Connection conn = dbPool.getConnection();
		try {
			group.setDocID(rs.getInt("GROUPID"));
			group.setName(rs.getString("GROUPNAME"));
			group.setDescription(rs.getString("DESCRIPTION"));
			group.form = rs.getString("FORM");
			group.setOwner(rs.getString("OWNER"));
			group.setDefaultRuleID(rs.getString("DEFAULTRULEID"));
			group.parentDocID = rs.getInt("PARENTDOCID");
			group.parentDocType = rs.getInt("PARENTDOCTYPE");
			group.setViewText(rs.getString("VIEWTEXT"));

			Set<String> members = new HashSet<String>();
			conn.setAutoCommit(false);
			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ResultSet res = st.executeQuery("SELECT EMPID FROM USER_GROUPS WHERE GROUPID = " + group.getDocID() + " AND TYPE = "
			        + Const.DOCTYPE_EMPLOYER);
			while (res.next()) {
				members.add(getUserID(res.getInt("EMPID")));
			}
			res.close();
			group.setMembers(members);

			Statement rolest = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ResultSet roleResultSet = rolest.executeQuery("SELECT * FROM USER_ROLES ur WHERE ur.EMPID = " + group.getDocID() + " AND ur.TYPE = "
			        + group.docType);
			while (roleResultSet.next()) {
				String role = roleResultSet.getString("NAME");
				String app = roleResultSet.getString("APPID");
				Role roleObj = db.getAppRoles().get(role + "#" + app);
				if (roleObj != null) {
					roleObj.setRuleProvider(UserRoleType.PROVIDED_BY_GROUP);
					UserRole userRole = new UserRole(roleObj);
					group.addRole(userRole);
				}
			}
			roleResultSet.close();
			conn.commit();

			group.isValid = true;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return group;
	}

	@Override
	public Filter fillFilterDoc(Connection conn, ResultSet rs) {
		Filter filter = new Filter(db.getParent());
		try {
			filter.setName(rs.getString("NAME"));
			filter.setUserID(rs.getString("USERID"));
			filter.setFilterID(rs.getInt("ID"));
			String consql = "SELECT * FROM CONDITION WHERE FID = " + rs.getInt("ID");
			Statement st = conn.createStatement();
			ResultSet conrs = st.executeQuery(consql);
			while (conrs.next()) {
				filter.addCondition(conrs.getString("NAME"), conrs.getString("VALUE"));
			}
			filter.isValid = true;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		}
		return filter;
	}

	private boolean hasResponse(int docID, int docType) {
		boolean result = false;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			/*
			 * if (docType == DOCTYPE_ORGANIZATION){ String sql =
			 * "select count(EMPID) FROM EMPLOYERS e where e.PARENTDOCID=" +
			 * docID + " AND e.PARENTDOCTYPE=" + docType +
			 * " UNION SELECT count(DEPID) FROM DEPARTMENTS d WHERE d.PARENTDOCID="
			 * + docID + " AND d.PARENTDOCTYPE=" + docType; Statement statement
			 * = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
			 * ResultSet.CONCUR_READ_ONLY); ResultSet rs =
			 * statement.executeQuery(sql); while (rs.next()) { int count =
			 * rs.getInt(1); if (count > 0) result = true; } statement.close();
			 * }else if(docType == DOCTYPE_DEPARTMENT){ String sql =
			 * "select count(EMPID) FROM EMPLOYERS e where e.PARENTDOCID=" +
			 * docID + " AND e.PARENTDOCTYPE=" + docType; Statement statement =
			 * conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
			 * ResultSet.CONCUR_READ_ONLY); ResultSet rs =
			 * statement.executeQuery(sql); if (rs.next()) { int count =
			 * rs.getInt(1); if (count > 0) result = true; } statement.close();
			 * }else if(docType == DOCTYPE_EMPLOYER) { String sql =
			 * "select count(DEPID) FROM DEPARTMENTS d where d.PARENTDOCID=" +
			 * docID + " AND d.PARENTDOCTYPE=" + docType; Statement statement =
			 * conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
			 * ResultSet.CONCUR_READ_ONLY); ResultSet rs =
			 * statement.executeQuery(sql); if (rs.next()) { int count =
			 * rs.getInt(1); if (count > 0) result = true; } statement.close();
			 * }
			 */

			String sql = "select count(EMPID) FROM EMPLOYERS e where e.PARENTDOCID=" + docID + " AND e.PARENTDOCTYPE=" + docType
			        + " UNION SELECT count(DEPID) FROM DEPARTMENTS d WHERE d.PARENTDOCID=" + docID + " AND d.PARENTDOCTYPE=" + docType;
			Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				int count = rs.getInt(1);
				if (count > 0) {
					result = true;
				}
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return result;
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

	private Glossary fillGloassary(ResultSet rs) throws SQLException {
		Glossary doc = new Glossary(db.getParent(), new User(Const.sysUser));
		fillShortViewTextData(rs, doc);
		fillSysData(rs, doc);
		boolean isNextMain = true;
		while (isNextMain || rs.next()) {
			switch (rs.getInt("TYPE")) {
			case TEXT:
				doc.addStringField(rs.getString("NAME"), rs.getString("VALUE"));
				break;
			case NUMBERS:
				doc.addNumberField(rs.getString("NAME"), rs.getBigDecimal("VALUEASNUMBER"));
				break;
			case DATETIMES:
				doc.addDateField(rs.getString("NAME"), rs.getDate("VALUEASDATE"));
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
	public ISelectFormula getSelectFormula(FormulaBlocks blocks) {
		ISelectFormula sf = new StructSelectFormula(blocks);
		return sf;
	}

	@Override
	public DatabaseType getRDBMSType() {
		return dbPool.getDatabaseType();
	}

	@Override
	public _ViewEntryCollection getStructureCollection(ISelectFormula sf, User user, int pageNum, int pageSize, RunTimeParameters parameters) {
		ViewEntryCollection coll = new ViewEntryCollection(pageSize, user, parameters);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			if (pageNum == 0) {
				String sql = sf.getCountCondition(Const.supervisorGroupAsSet, parameters.getFilters());
				ResultSet rs = s.executeQuery(sql);
				if (rs.next()) {
					pageNum = RuntimeObjUtil.countMaxPage(rs.getInt(1), pageSize);
				}
			}
			int offset = db.calcStartEntry(pageNum, pageSize);
			String sql = sf.getCondition(Const.supervisorGroupAsSet, pageSize, offset, parameters.getFilters(), parameters.getSorting(), true);
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				Set<DocID> emptyDocIDSet = new HashSet<>();
				ViewEntry entry = new ViewEntry(this.db, rs, emptyDocIDSet, Const.supervisorUser, parameters.getDateFormat());
				coll.add(entry);
				coll.setCount(rs.getInt(1));
				while (rs.next()) {
					entry = new ViewEntry(this.db, rs, emptyDocIDSet, Const.supervisorUser, parameters.getDateFormat());
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
