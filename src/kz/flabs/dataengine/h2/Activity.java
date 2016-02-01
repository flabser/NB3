package kz.flabs.dataengine.h2;

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
import java.util.HashSet;

import kz.flabs.dataengine.ActivityStatusType;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.dataengine.IActivity;
import kz.flabs.dataengine.IDBConnectionPool;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.runtimeobj.viewentry.ActivityEntry;
import kz.flabs.runtimeobj.viewentry.IViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntry;
import kz.flabs.runtimeobj.viewentry.ViewEntryCollection;
import kz.flabs.runtimeobj.viewentry.ViewEntryType;

public class Activity implements IActivity {

	protected IDatabase db;
	protected IDBConnectionPool dbPool;

	public Activity(IDatabase db) {
		this.db = db;

	}

	@Override
	public IDatabase getParentDatabase() {
		return db;
	}

	@Override
	public int postStartOfActivity(String viewText, String userID, String nameOfService, String nameOfMethod, String parameters, String springServer,
	        String transaction, Date request_time) {
		int key = 0;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String sql = "INSERT INTO ACTIVITY(viewtext, userid, service_name, method_name, parameters, spring_server, event_time, transaction, request_time) values ("
			        + " ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement pst = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			pst.setString(1, viewText);
			pst.setString(2, userID);
			pst.setString(3, nameOfService);
			pst.setString(4, nameOfMethod);
			pst.setString(5, parameters);
			pst.setString(6, springServer);
			pst.setTimestamp(7, new Timestamp(new Date().getTime()));
			pst.setString(8, transaction);
			if (request_time != null) {
				pst.setTimestamp(9, new Timestamp(request_time.getTime()));
			} else {
				pst.setNull(9, Types.TIMESTAMP);
			}

			pst.executeUpdate();
			ResultSet rs = pst.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);
			}
			conn.commit();
			pst.close();
		} catch (SQLException e) {

		} catch (Exception e) {

		} finally {
			dbPool.returnConnection(conn);
		}
		return key;
	}

	@Override
	public int postEndOfActivity(ActivityStatusType type, Date returnTime, String comment, int processedRec, int processedSize, String transaction) {
		int key = 0;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			String sql = "UPDATE ACTIVITY set activity_type = ?, return_time = ?, comment = ?, processed_rec = ?, processed_size = ? where transaction = ?";

			/*
			 * String sql =
			 * "INSERT INTO ACTIVITY(type, return_time, comment, processed_rec, processed_size, \"transaction\", event_time) values ("
			 * + "?, ?, ?, ?, ?, ?, ?)";
			 */
			PreparedStatement pst = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			pst.setBoolean(1, (type == ActivityStatusType.SUCCESS ? true : false));
			pst.setTimestamp(2, new Timestamp(returnTime.getTime()));
			pst.setString(3, comment);
			pst.setInt(4, processedRec);
			pst.setInt(5, processedSize);
			pst.setString(6, transaction);
			pst.executeUpdate();
			ResultSet rs = pst.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);
			}
			conn.commit();
			pst.close();
		} catch (SQLException e) {

		} catch (Exception e) {

		} finally {
			dbPool.returnConnection(conn);
		}
		return key;
	}

	@Override
	public int postEndOfFailureActivity(ActivityStatusType type, Date returnTime, String comment, String transaction) {
		int key = 0;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			/*
			 * String sql =
			 * "INSERT INTO ACTIVITY(type, returnTime, comment, transaction, eventtime) values ("
			 * + "?, ?, ?, ?, ?)";
			 */
			String sql = "UPDATE ACTIVITY set activity_type = ?, return_time = ?, comment = ? where transaction = ?";
			PreparedStatement pst = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			pst.setBoolean(1, (type == ActivityStatusType.SUCCESS ? true : false));
			pst.setTimestamp(2, new Timestamp(returnTime.getTime()));
			pst.setString(3, comment);
			pst.setString(4, transaction);
			pst.executeUpdate();
			ResultSet rs = pst.getGeneratedKeys();
			if (rs.next()) {
				key = rs.getInt(1);
			}
			conn.commit();
			pst.close();
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;
		} catch (Exception e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
			return -1;
		} finally {
			dbPool.returnConnection(conn);
		}
		return key;

	}

	@Override
	public ViewEntryCollection getActivities(int offset, int pageSize) {

		HashSet<IViewEntry> activityEntries = new HashSet<IViewEntry>();
		ViewEntryCollection col = null;
		int count = 0;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT * FROM ACTIVITY WHERE LIMIT ='" + pageSize + "' AND OFFSET = " + offset;

			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				ViewEntry entry = new ViewEntry(db, rs, ViewEntryType.ACTIVITY);
				// activityEntries.add(entry);
				count += rs.getInt(1);
			}
			rs.close();
			s.close();
			conn.commit();

			col.setCount(count);
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return col;
	}

	@Override
	public ViewEntryCollection getActivities(int offset, int pageSize, String userID) {

		HashSet<IViewEntry> activityEntries = new HashSet<IViewEntry>();
		ViewEntryCollection col = null;
		int count = 0;
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT * FROM ACTIVITY WHERE USERID ='" + userID + "' AND LIMIT = " + pageSize + " OFFSET " + offset;
			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				ViewEntry entry = new ViewEntry(db, rs, ViewEntryType.ACTIVITY);
				// activityEntries.add(entry);
				count += rs.getInt(1);
			}
			rs.close();
			s.close();
			conn.commit();

			col.setCount(count);
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return col;
	}

	@Override
	public ViewEntryCollection getActivities(int offset, int pageSize, String userID, String services, Date dateFrom, Date dateTo, int totalsFrom,
	        int totalsTo, boolean errorsOnly) {
		HashSet<IViewEntry> activityEntries = new HashSet<IViewEntry>();
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "select * from activity ";
			boolean and = false;
			StringBuffer conditions = new StringBuffer(5000);
			if (userID != null && !"".equalsIgnoreCase(userID)) {
				conditions.append(" userID = '");
				conditions.append(userID);
				conditions.append("' ");
				and = true;
			}

			if (services != null && !"".equalsIgnoreCase(services)) {
				String[] serviceAndMethodName = services.split("`");

				if (serviceAndMethodName != null && serviceAndMethodName.length != 0) {
					if (and) {
						conditions.append(" and ");
						and = false;
					}

					ArrayList<String> serviceConds = new ArrayList<String>();
					for (String value : serviceAndMethodName) {
						int sepIndex = value.indexOf("~");
						if (sepIndex != -1) {
							String serviceName = value.substring(0, sepIndex);
							if (value.length() > sepIndex) {
								String[] methods = value.substring(sepIndex + 1, value.length()).split(";");
								String formattedMethods = "";
								for (String methodName : methods) {
									formattedMethods += "'" + methodName + "',";
								}
								if (formattedMethods.length() > 0) {
									formattedMethods = formattedMethods.substring(0, formattedMethods.lastIndexOf(","));
								}
								serviceConds.add("( service_name = '" + serviceName + "' and method_name in(" + formattedMethods + ")" + ")");
							}
						} else {
							serviceConds.add(" (service_name = '" + value + "') ");

						}
					}

					for (int i = 0; i < serviceConds.size(); i++) {
						if (i != 0) {
							conditions.append(" or ");
						}
						conditions.append(serviceConds.get(i));
						and = true;
					}

				}
			}

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			if (dateFrom != null) {
				if (and) {
					conditions.append(" and ");
					and = false;
				}
				conditions.append(" event_time >= '" + format.format(dateFrom) + "'");
				and = true;
			}

			if (dateTo != null) {
				if (and) {
					conditions.append(" and ");
					and = false;
				}
				conditions.append(" event_time <= " + format.format(dateTo) + "'");
				and = true;
			}

			if (totalsFrom != 0) {
				if (and) {
					conditions.append(" and ");
					and = false;
				}
				conditions.append(" processed_rec >= " + totalsFrom);
				and = true;
			}

			if (totalsTo != 0) {
				if (and) {
					conditions.append(" and ");
					and = false;
				}
				conditions.append(" processed_rec <= " + totalsTo);
				and = true;
			}

			if (errorsOnly == true) {
				if (and) {
					conditions.append(" and ");
				}
				conditions.append(" activity_type = false");
			}

			sql += (conditions.length() > 0 ? "where " + conditions.toString() : "") + " limit " + pageSize + " offset " + offset;
			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				ViewEntry entry = new ActivityEntry(rs, ViewEntryType.ACTIVITY, db);
				// activityEntries.add(entry);
			}
			rs.close();
			s.close();
			conn.commit();

		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return new ViewEntryCollection(pageSize);
	}

	@Override
	public StringBuffer getActivitiesAsXML(int offset, int pageSize, String userID, String services, Date dateFrom, Date dateTo, int totalsFrom,
	        int totalsTo, boolean errorsOnly, String springServer, int diffTimeFrom, int diffTimeTo) {
		StringBuffer content = new StringBuffer(10000);
		StringBuffer parameters = new StringBuffer(1000);
		Connection conn = dbPool.getConnection();
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "select * from (select *, DATE_PART('second', return_time - request_time) as difftime from activity) as t ";
			boolean and = false;
			StringBuffer conditions = new StringBuffer(5000);
			parameters.append("<parameters>");
			parameters.append("<userid>");
			if (userID != null && !"".equalsIgnoreCase(userID)) {
				conditions.append(" userID = '");
				conditions.append(userID);
				conditions.append("' ");
				parameters.append(userID);
				and = true;
			}
			parameters.append("</userid>");
			parameters.append("<services>");
			if (services != null && !"".equalsIgnoreCase(services)) {
				String[] serviceAndMethodName = services.split("`");

				if (serviceAndMethodName != null && serviceAndMethodName.length != 0) {
					if (and) {
						conditions.append(" and ");
						and = false;
					}

					ArrayList<String> serviceConds = new ArrayList<String>();
					for (String value : serviceAndMethodName) {
						int sepIndex = value.indexOf("~");
						if (sepIndex != -1) {
							String serviceName = value.substring(0, sepIndex);
							if (value.length() > sepIndex) {
								String[] methods = value.substring(sepIndex + 1, value.length()).split(";");
								String formattedMethods = "";
								for (String methodName : methods) {
									formattedMethods += "'" + methodName + "',";
								}
								if (formattedMethods.length() > 0) {
									formattedMethods = formattedMethods.substring(0, formattedMethods.lastIndexOf(","));
								}
								serviceConds.add("( service_name = '" + serviceName + "' and method_name in(" + formattedMethods + ")" + ")");
							}
						} else {
							serviceConds.add(" (service_name = '" + value + "') ");

						}
					}

					for (int i = 0; i < serviceConds.size(); i++) {
						if (i != 0) {
							conditions.append(" or ");
						} else {
							conditions.append(" ( ");
						}
						conditions.append(serviceConds.get(i));
						and = true;
					}
					if (serviceConds.size() != 0) {
						conditions.append(" ) ");
					}
				}
			}
			parameters.append(services);
			parameters.append("</services>");

			parameters.append("<datefrom>");
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat dotformat = new SimpleDateFormat("dd.MM.yyyy");
			if (dateFrom != null) {
				if (and) {
					conditions.append(" and ");
					and = false;
				}
				conditions.append(" event_time >= '" + format.format(dateFrom) + "'");
				parameters.append(dotformat.format(dateFrom));
				and = true;
			}
			parameters.append("</datefrom>");

			parameters.append("<dateto>");
			if (dateTo != null) {
				if (and) {
					conditions.append(" and ");
					and = false;
				}
				conditions.append(" event_time <= '" + format.format(dateTo) + "'");
				parameters.append(dotformat.format(dateTo));
				and = true;
			}
			parameters.append("</dateto>");

			parameters.append("<totalsfrom>");
			if (totalsFrom != 0) {
				if (and) {
					conditions.append(" and ");
					and = false;
				}
				conditions.append(" processed_rec >= " + totalsFrom);
				parameters.append(totalsFrom);
				and = true;
			}
			parameters.append("</totalsfrom>");

			parameters.append("<totalto>");
			if (totalsTo != 0) {
				if (and) {
					conditions.append(" and ");
					and = false;
				}
				conditions.append(" processed_rec <= " + totalsTo);
				parameters.append(totalsTo);
				and = true;
			}
			parameters.append("</totalto>");

			parameters.append("<errorsonly>");
			if (errorsOnly == true) {
				if (and) {
					conditions.append(" and ");
				}
				conditions.append(" activity_type = false");
				and = true;
				parameters.append(false);
			}
			parameters.append("</errorsonly>");

			parameters.append("<difftimefrom>");
			if (diffTimeFrom != 0) {
				if (and) {
					conditions.append(" and ");
					and = false;
				}
				conditions.append(" difftime >= " + diffTimeFrom + "");
				parameters.append(diffTimeFrom);
				and = true;
			}
			parameters.append("</difftimefrom>");

			parameters.append("<difftimeto>");
			if (diffTimeTo != 0) {
				if (and) {
					conditions.append(" and ");
					and = false;
				}
				conditions.append(" difftime <= " + diffTimeTo + "");
				parameters.append(diffTimeTo);
				and = true;
			}
			parameters.append("</difftimeto>");

			parameters.append("<springserver>");
			if (springServer != null && !"".equalsIgnoreCase(springServer)) {
				/*
				 * if (and) { conditions.append(" and "); and = false; }
				 * conditions.append(" spring_server = '");
				 * conditions.append(springServer); conditions.append("' ");
				 */

				parameters.append(springServer);
				/* and = true; */
			}
			parameters.append("</springserver>");

			sql += (conditions.length() > 0 ? "where " + conditions.toString() : "") + " limit " + pageSize + " offset " + offset;
			ResultSet rs = s.executeQuery(sql);
			while (rs.next()) {
				ViewEntry entry = new ActivityEntry(rs, ViewEntryType.ACTIVITY, db);
				// content.append(entry.toXML());
			}
			parameters.append("</parameters>");
			content.append(parameters);
			rs.close();
			s.close();
			conn.commit();
			return content;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return content;
	}

	@Override
	public int getActivitiesCount(String userID, String services, Date dateFrom, Date dateTo, int totalsFrom, int totalsTo, boolean errorsOnly,
	        String springServer, int diffTimeFrom, int diffTimeTo) {
		Connection conn = dbPool.getConnection();
		int count = 0;
		try {
			conn.setAutoCommit(false);
			Statement s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			String sql = "select count(*) from (select *, DATE_PART('second', return_time - request_time) as difftime from activity) as t ";
			boolean and = false;
			StringBuffer conditions = new StringBuffer(5000);
			if (userID != null && !"".equalsIgnoreCase(userID)) {
				conditions.append(" userID = '");
				conditions.append(userID);
				conditions.append("' ");
				and = true;
			}

			if (services != null && !"".equalsIgnoreCase(services)) {
				String[] serviceAndMethodName = services.split("`");

				if (serviceAndMethodName != null && serviceAndMethodName.length != 0) {
					if (and) {
						conditions.append(" and ");
						and = false;
					}
					ArrayList<String> serviceConds = new ArrayList<String>();
					for (String value : serviceAndMethodName) {
						int sepIndex = value.indexOf("~");
						if (sepIndex != -1) {
							String serviceName = value.substring(0, sepIndex);
							if (value.length() > sepIndex) {
								String[] methods = value.substring(sepIndex + 1, value.length()).split(";");
								String formattedMethods = "";
								for (String methodName : methods) {
									formattedMethods += "'" + methodName + "',";
								}
								if (formattedMethods.length() > 0) {
									formattedMethods = formattedMethods.substring(0, formattedMethods.lastIndexOf(","));
								}
								serviceConds.add("( service_name = '" + serviceName + "' and method_name in(" + formattedMethods + ")" + ")");
							}
						} else {
							serviceConds.add(" (service_name = '" + value + "') ");

						}
					}

					for (int i = 0; i < serviceConds.size(); i++) {
						if (i != 0) {
							conditions.append(" or ");
						} else {
							conditions.append(" ( ");
						}
						conditions.append(serviceConds.get(i));
						and = true;
					}

					if (serviceConds.size() != 0) {
						conditions.append(" ) ");
					}
				}
			}

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			if (dateFrom != null) {
				if (and) {
					conditions.append(" and ");
					and = false;
				}
				conditions.append(" event_time >= '" + format.format(dateFrom) + "'");
				and = true;
			}

			if (dateTo != null) {
				if (and) {
					conditions.append(" and ");
					and = false;
				}
				conditions.append(" event_time <= '" + format.format(dateTo) + "'");
				and = true;
			}

			if (totalsFrom != 0) {
				if (and) {
					conditions.append(" and ");
					and = false;
				}
				conditions.append(" processed_rec >= " + totalsFrom);
				and = true;
			}

			if (totalsTo != 0) {
				if (and) {
					conditions.append(" and ");
					and = false;
				}
				conditions.append(" processed_rec <= " + totalsTo);
				and = true;
			}

			if (errorsOnly == true) {
				if (and) {
					conditions.append(" and ");
					and = false;
				}
				conditions.append(" activity_type = false");
				and = true;
			}

			if (diffTimeFrom != 0) {
				if (and) {
					conditions.append(" and ");
					and = false;
				}
				conditions.append(" difftime >= " + diffTimeFrom + "");
				and = true;
			}

			if (diffTimeTo != 0) {
				if (and) {
					conditions.append(" and ");
					and = false;
				}
				conditions.append(" difftime <= " + diffTimeTo + "");
				and = true;
			}

			/*
			 * if (springServer != null && !"".equalsIgnoreCase(springServer)) {
			 * if (and) { conditions.append(" and "); and = false; }
			 * conditions.append(" spring_server = '");
			 * conditions.append(springServer); conditions.append("' "); and =
			 * true; }
			 */

			sql += (conditions.length() > 0 ? "where " + conditions.toString() : "");
			ResultSet rs = s.executeQuery(sql);
			if (rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();
			s.close();
			conn.commit();
			return count;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(db.getDbID(), e);
		} finally {
			dbPool.returnConnection(conn);
		}
		return count;
	}
}
