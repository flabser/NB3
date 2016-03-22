package kz.flabs.dataengine;

import java.sql.ResultSet;
import java.sql.SQLException;

import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.util.XMLUtil;

public abstract class DatabaseCore {

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

}
