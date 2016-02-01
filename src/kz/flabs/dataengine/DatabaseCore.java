package kz.flabs.dataengine;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import kz.flabs.dataengine.h2.Database;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.util.XMLUtil;

public abstract class DatabaseCore {
	public DatabaseType databaseType = DatabaseType.DEFAULT;

	protected static final String[] systemFields = { "docid", "doctype", "author", "parentdocid", "parentdoctype", "regdate", "lastupdate",
	        "viewtext", "viewtext1", "viewtext2", "viewtext3", "viewtext4", "viewtext5", "viewtext6", "viewtext7", "viewnumber", "viewdate",
	        "viewicon", "form", "syncstatus", "has_attachment" };
	protected static final ArrayList<String> systemFieldsList = new ArrayList<String>(Arrays.asList(systemFields));

	protected String getViewContent(ResultSet rs) throws SQLException {

		String tempText = rs.getString("VIEWTEXT");
		StringBuilder viewcontent = new StringBuilder(1000);
		String viewtextName = "";
		viewcontent.append("<viewtext>" + (tempText != null ? XMLUtil.getAsTagValue(tempText) : "") + "</viewtext>");

		for (int i = 1; i <= DatabaseConst.VIEWTEXT_COUNT; i++) {
			viewtextName = "viewtext" + i;
			viewcontent.append("<" + viewtextName + ">");
			tempText = rs.getString(viewtextName);
			viewcontent.append(tempText != null ? XMLUtil.getAsTagValue(tempText) : "");
			viewcontent.append("</" + viewtextName + ">");
		}
		BigDecimal viewNumber = rs.getBigDecimal("VIEWNUMBER");
		tempText = String.valueOf((viewNumber != null ? viewNumber.stripTrailingZeros().toPlainString() : ""));

		viewcontent.append("<viewnumber>" + tempText + "</viewnumber>");

		Date tempDate = rs.getTimestamp("VIEWDATE");

		return "<viewcontent>" + viewcontent.toString() + "<viewdate>" + (tempDate != null ? Database.dateTimeFormat.format(tempDate) : "")
		        + "</viewdate></viewcontent>";
	}

	protected String getShortViewContent(ResultSet rs) throws SQLException {

		String tempText = "";
		String viewtextName = "";
		StringBuilder viewcontent = new StringBuilder(1000);
		for (int i = 1; i <= DatabaseConst.VIEWTEXT_COUNT; i++) {
			viewtextName = "viewtext" + i;
			viewcontent.append("<" + viewtextName + ">");
			tempText = rs.getString(viewtextName);
			viewcontent.append(tempText != null ? XMLUtil.getAsTagValue(tempText) : "");
			viewcontent.append("</" + viewtextName + ">");
		}
		BigDecimal viewNumber = rs.getBigDecimal("VIEWNUMBER");
		tempText = String.valueOf((viewNumber != null ? viewNumber.stripTrailingZeros().toPlainString() : ""));

		viewcontent.append("<viewnumber>" + tempText + "</viewnumber>");

		Date tempDate = rs.getTimestamp("VIEWDATE");

		return "<viewcontent>" + viewcontent.toString() + "<viewdate>" + (tempDate != null ? Database.dateTimeFormat.format(tempDate) : "")
		        + "</viewdate></viewcontent>";
	}

	@Deprecated
	protected String getSimpleViewContent(ResultSet rs) throws SQLException {

		String tempText = rs.getString("VIEWTEXT");
		String viewContent = "<viewtext1>" + (tempText != null ? XMLUtil.getAsTagValue(tempText) : "") + "</viewtext1>";

		viewContent += "<viewtext2></viewtext2>";
		viewContent += "<viewtext3></viewtext3>";
		viewContent += "<viewtext4></viewtext4>";
		viewContent += "<viewtext5></viewtext5>";
		viewContent += "<viewtext6></viewtext6>";
		viewContent += "<viewtext7></viewtext7>";
		viewContent += "<viewnumber></viewnumber>";
		return "<viewcontent>" + viewContent + "<viewdate></viewdate></viewcontent>";
	}

	protected void fillViewTextData(ResultSet rs, BaseDocument doc) {
		/*
		 * if (fillViewText(rs, "VIEWTEXT", doc)){ if (fillViewText(rs,
		 * "VIEWTEXT1", doc)){ if (fillViewText(rs, "VIEWTEXT2", doc)){
		 * fillViewText(rs, "VIEWTEXT3", doc); } } }
		 */
		int i = 0;
		boolean cont = true;
		while (i <= DatabaseConst.VIEWTEXT_COUNT && cont) {
			cont = fillViewText(rs, "VIEWTEXT" + (i != 0 ? i : ""), doc);
			i++;
		}
		try {
			doc.setViewNumber(rs.getBigDecimal("VIEWNUMBER"));
			doc.setViewDate(rs.getTimestamp("VIEWDATE"));
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(doc.dbID, e);
		}
	}

	protected void fillShortViewTextData(ResultSet rs, BaseDocument doc) {
		/*
		 * if (fillViewText(rs, "VIEWTEXT1", doc)){ if (fillViewText(rs,
		 * "VIEWTEXT2", doc)){ fillViewText(rs, "VIEWTEXT3", doc); } }
		 */
		int i = 1;
		boolean cont = true;
		while (i <= DatabaseConst.VIEWTEXT_COUNT && cont) {
			cont = fillViewText(rs, "VIEWTEXT" + i, doc);
			i++;
		}
		try {
			doc.setViewNumber(rs.getBigDecimal("VIEWNUMBER"));
			doc.setViewDate(rs.getTimestamp("VIEWDATE"));
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(doc.dbID, e);
		}
	}

	private boolean fillViewText(ResultSet rs, String vtName, BaseDocument doc) {
		try {
			String val = rs.getString(vtName).replace("''", "'").trim();
			// if (!val.equals("-") && !val.equals("")) {
			doc.addViewText(val);
			return true;
			// } else {
			// return false;
			// }
		} catch (Exception e) {
			return false;
		}

	}

	protected void fillSysData(ResultSet rs, BaseDocument doc) {

		try {
			doc.parentDocID = rs.getInt("PARENTDOCID");
			doc.parentDocType = rs.getInt("PARENTDOCTYPE");
		} catch (SQLException e) {
			doc.parentDocID = 0;
			doc.parentDocType = Const.DOCTYPE_UNKNOWN;
		}

		try {
			try {
				doc.setDocID(rs.getInt("DOCID"));
			} catch (SQLException e) {
				try {
					doc.setDocID(rs.getInt("ID"));
				} catch (SQLException e1) {
					try {
						doc.setDocID(rs.getInt("ORGID"));
					} catch (SQLException e2) {

					}
				}
			}
			doc.docType = rs.getInt("DOCTYPE");
			doc.setLastUpdate(rs.getTimestamp("LASTUPDATE"));
			doc.setAuthor(rs.getString("AUTHOR"));
			doc.setRegDate(rs.getTimestamp("REGDATE"));
			doc.setDdbID(rs.getString("DDBID"));
			doc.form = rs.getString("FORM");
			doc.addStringField("form", rs.getString("FORM"));
			doc.isValid = true;
			doc.setSign(rs.getString("SIGN"));
			doc.setSignedFields(rs.getString("SIGNEDFIELDS"));

		} catch (SQLException e) {
			DatabaseUtil.errorPrint(doc.dbID, e);
		}
	}

	protected void fillSysDataSimple(ResultSet rs, BaseDocument doc) {
		try {
			doc.docType = rs.getInt("DOCTYPE");
			switch (doc.docType) {
			case Const.DOCTYPE_ORGANIZATION:
				doc.setDocID(rs.getInt("ORGID"));
				break;
			case Const.DOCTYPE_DEPARTMENT:
				doc.setDocID(rs.getInt("DEPID"));
				break;
			case Const.DOCTYPE_EMPLOYER:
				doc.setDocID(rs.getInt("EMPID"));
				break;
			case Const.DOCTYPE_GROUP:
				doc.setDocID(rs.getInt("GROUPID"));
				break;
			default:
				try {
					doc.setDocID(rs.getInt("DOCID"));
				} catch (SQLException e) {
					try {
						doc.setDocID(rs.getInt("ID"));
					} catch (SQLException e1) {

					}
				}
			}
			doc.setLastUpdate(rs.getTimestamp("LASTUPDATE"));
			doc.setAuthor(rs.getString("AUTHOR"));
			doc.setRegDate(rs.getTimestamp("REGDATE"));
			doc.setDdbID(rs.getString("DDBID"));
			doc.setViewText(rs.getString("VIEWTEXT").replace("''", "'"));
			doc.viewIcon = rs.getString("VIEWICON");
			doc.form = rs.getString("FORM");
			doc.isValid = true;
		} catch (SQLException e) {
			DatabaseUtil.errorPrint(doc.dbID, e);
		}
	}

	protected boolean isSystemField(String fieldName) {
		return systemFieldsList.contains(fieldName);
	}

}
