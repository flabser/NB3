package kz.flabs.runtimeobj.viewentry;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Set;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.UsersActivityType;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.xml.Tag;
import kz.flabs.users.User;
import kz.flabs.util.Util;
import kz.lof.user.IUser;

public class ViewEntry implements IViewEntry, Const {
	public String ddbID;

	protected ArrayList<ViewText> viewTexts = new ArrayList<ViewText>();
	protected ViewEntryType type;

	private int docType;
	private int docID;
	private String form;
	private int hasAttachment;
	private IDatabase db;
	private int hasResp;
	private StringBuffer descendants = new StringBuffer(1000);
	private BigDecimal viewNumberValue;
	private int topicid;
	private int isread = -1;

	@Deprecated
	public ViewEntry(IDatabase db, ResultSet rs, Set<DocID> toExpandResponses, User user) throws SQLException {
		this.db = db;
		type = ViewEntryType.DOCUMENT;
		docID = rs.getInt("DOCID");
		docType = rs.getInt("DOCTYPE");
		ddbID = rs.getString("DDBID");
		form = rs.getString("FORM");
		hasAttachment = rs.getInt("HAS_ATTACHMENT");
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT"), "viewtext"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT1"), "viewtext1"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT2"), "viewtext2"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT3"), "viewtext3"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT4"), "viewtext4"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT5"), "viewtext5"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT6"), "viewtext6"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT7"), "viewtext7"));
		viewNumberValue = rs.getBigDecimal("VIEWNUMBER");
		viewTexts.add(new ViewText(viewNumberValue, "viewnumber"));
		viewTexts.add(new ViewText(rs.getString("VIEWDATE"), "viewdate"));
		try {
			if (rs.findColumn("parent_exists") > 0) {
				hasResp = rs.getInt("parent_exists") > 0 ? 1 : 0;
			}
		} catch (SQLException e) {
		}
		try {
			if (rs.findColumn("has_response") > 0) {
				hasResp = rs.getInt("has_response") > 0 ? 1 : 0;
			}
		} catch (SQLException e) {
		}
		try {
			if (rs.findColumn("isread") > 0) {
				isread = rs.getInt("isread");
			}
		} catch (SQLException e) {
		}
		if (toExpandResponses.contains(new DocID(docID, docType))) {

		}
		try {
			if (rs.findColumn("topicid") > 0) {
				topicid = rs.getInt("topicid");
			}
		} catch (SQLException e) {
		}

	}

	public boolean isRead() {
		return isread == 1;
	}

	@Deprecated
	public ViewEntry(IDatabase db, ResultSet rs, Set<DocID> toExpandResponses, User user, SimpleDateFormat simpleDateFormat,
	        String responseQueryCondition) throws SQLException {
		this.db = db;
		type = ViewEntryType.DOCUMENT;
		docID = rs.getInt("DOCID");
		docType = rs.getInt("DOCTYPE");
		ddbID = rs.getString("DDBID");
		form = rs.getString("FORM");
		hasAttachment = rs.getInt("HAS_ATTACHMENT");
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT"), "viewtext"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT1"), "viewtext1"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT2"), "viewtext2"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT3"), "viewtext3"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT4"), "viewtext4"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT5"), "viewtext5"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT6"), "viewtext6"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT7"), "viewtext7"));
		viewNumberValue = rs.getBigDecimal("VIEWNUMBER");
		viewTexts.add(new ViewText(viewNumberValue, "viewnumber"));
		viewTexts.add(new ViewText(rs.getDate("VIEWDATE"), "viewdate", simpleDateFormat));

		int total_resp = 0;
		try {
			if (rs.findColumn("parent_exists") > 0) {
				total_resp = rs.getInt("parent_exists");
			}
		} catch (SQLException e) {
		}
		try {
			if (rs.findColumn("has_response") > 0) {
				total_resp = rs.getInt("has_response");
			}
		} catch (SQLException e) {
		}
		try {
			if (rs.findColumn("isread") > 0) {
				isread = rs.getInt("isread");
			}
		} catch (SQLException e) {
		}
		if (total_resp > 0) {

		}

		if (toExpandResponses.contains(new DocID(docID, docType))) {

		}

		try {
			if (rs.findColumn("topicid") > 0) {
				topicid = rs.getInt("topicid");
			}
		} catch (SQLException e) {
		}
	}

	@Deprecated
	public ViewEntry(IDatabase db, ResultSet rs, Set<DocID> toExpandResponses, User user, SimpleDateFormat simpleDateFormat) throws SQLException {
		this.db = db;
		type = ViewEntryType.DOCUMENT;
		docID = rs.getInt("DOCID");
		docType = rs.getInt("DOCTYPE");
		ddbID = rs.getString("DDBID");
		form = rs.getString("FORM");
		hasAttachment = rs.getInt("HAS_ATTACHMENT");
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT"), "viewtext"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT1"), "viewtext1"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT2"), "viewtext2"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT3"), "viewtext3"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT4"), "viewtext4"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT5"), "viewtext5"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT6"), "viewtext6"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT7"), "viewtext7"));
		viewNumberValue = rs.getBigDecimal("VIEWNUMBER");
		viewTexts.add(new ViewText(viewNumberValue, "viewnumber"));
		viewTexts.add(new ViewText(rs.getTimestamp("VIEWDATE"), "viewdate", simpleDateFormat));
		try {
			if (rs.findColumn("parent_exists") > 0) {
				hasResp = rs.getInt("parent_exists") > 0 ? 1 : 0;
			}
		} catch (SQLException e) {
		}
		try {
			if (rs.findColumn("has_response") > 0) {
				hasResp = rs.getInt("has_response") > 0 ? 1 : 0;
			}
		} catch (SQLException e) {
		}
		try {
			if (rs.findColumn("isread") > 0) {
				isread = rs.getInt("isread");
			}
		} catch (SQLException e) {
		}

		if (toExpandResponses.contains(new DocID(docID, docType))) {

		}

		try {
			if (rs.findColumn("topicid") > 0) {
				topicid = rs.getInt("topicid");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ViewEntry(IDatabase db, ResultSet rs, ViewEntryType type) throws SQLException {
		this.db = db;
		this.type = type;
		docID = 0;
		docType = Const.DOCTYPE_UNKNOWN;
		ddbID = "";
		form = "";
		if (type == ViewEntryType.ACTIVITY) {
			viewTexts.add(new ViewText(UsersActivityType.getType(rs.getInt("TYPE")).name(), "type"));
			viewTexts.add(new ViewText(rs.getTimestamp("EVENTTIME"), "time"));
			viewTexts.add(new ViewText(rs.getString("DBID"), "dbid"));
			viewTexts.add(new ViewText(rs.getString("VIEWTEXT"), "viewtext"));
			viewTexts.add(new ViewText(rs.getString("DDBID"), "ddbid"));
		} else {
			docID = rs.getInt("DOCID");
			docType = rs.getInt("DOCTYPE");
			ddbID = rs.getString("DDBID");
			form = rs.getString("FORM");
			hasAttachment = rs.getInt("HAS_ATTACHMENT");
			viewTexts.add(new ViewText(rs.getString("VIEWTEXT"), "viewtext"));
			viewTexts.add(new ViewText(rs.getString("VIEWTEXT1"), "viewtext1"));
			viewTexts.add(new ViewText(rs.getString("VIEWTEXT2"), "viewtext2"));
			viewTexts.add(new ViewText(rs.getString("VIEWTEXT3"), "viewtext3"));
			viewTexts.add(new ViewText(rs.getString("VIEWTEXT4"), "viewtext4"));
			viewTexts.add(new ViewText(rs.getString("VIEWTEXT5"), "viewtext5"));
			viewTexts.add(new ViewText(rs.getString("VIEWTEXT6"), "viewtext6"));
			viewTexts.add(new ViewText(rs.getString("VIEWTEXT7"), "viewtext7"));
			viewNumberValue = rs.getBigDecimal("VIEWNUMBER");
			viewTexts.add(new ViewText(viewNumberValue, "viewnumber"));
		}
	}

	public <T extends Number> ViewEntry(String textValue, T value) {
		type = ViewEntryType.CATEGORY;
		viewTexts.add(new ViewText(textValue, "viewtext"));
		viewNumberValue = new BigDecimal(value.toString());
		viewTexts.add(new ViewText(viewNumberValue.stripTrailingZeros(), "viewnumber"));
	}

	public ViewEntry(ResultSet rs, String[] colsName) throws SQLException {
		docID = 0;
		docType = Const.DOCTYPE_UNKNOWN;
		ddbID = "";
		form = "";
		if (type == ViewEntryType.ACTIVITY) {
			viewTexts.add(new ViewText(UsersActivityType.getType(rs.getInt("TYPE")).name(), "type"));
			viewTexts.add(new ViewText(rs.getTimestamp("EVENTTIME"), "time"));
			viewTexts.add(new ViewText(rs.getString("DBID"), "dbid"));
			viewTexts.add(new ViewText(rs.getString("VIEWTEXT"), "viewtext"));
			viewTexts.add(new ViewText(rs.getString("DDBID"), "ddbid"));
		}
	}

	public ViewEntry(String[] cols) throws SQLException {
		type = ViewEntryType.GLOSSARY;
		docType = Const.DOCTYPE_GLOSSARY;
		ddbID = cols.toString();
		form = "";
		viewTexts.add(new ViewText(cols[0], "viewtext"));

		for (int i = 1; i < cols.length; i++) {
			viewTexts.add(new ViewText(cols[i], "viewtext" + Integer.toString(i)));
		}
		hasAttachment = 0;
	}

	public ViewEntry(ResultSet rs, IDatabase db, Set<String> toExpandResponses, User user, SimpleDateFormat simpleDateFormat) throws SQLException {
		this.db = db;
		type = ViewEntryType.DOCUMENT;
		docID = rs.getInt("DOCID");
		docType = rs.getInt("DOCTYPE");
		ddbID = rs.getString("DDBID");
		form = rs.getString("FORM");
		// hasAttachment = rs.getInt("HAS_ATTACHMENT");
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT"), "viewtext"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT1"), "viewtext1"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT2"), "viewtext2"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT3"), "viewtext3"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT4"), "viewtext4"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT5"), "viewtext5"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT6"), "viewtext6"));
		viewTexts.add(new ViewText(rs.getString("VIEWTEXT7"), "viewtext7"));
		viewNumberValue = rs.getBigDecimal("VIEWNUMBER");
		viewTexts.add(new ViewText(viewNumberValue, "viewnumber"));
		viewTexts.add(new ViewText(rs.getTimestamp("VIEWDATE"), "viewdate", simpleDateFormat));
		try {
			if (rs.findColumn("parent_exists") > 0) {
				hasResp = rs.getInt("parent_exists") > 0 ? 1 : 0;
			}
		} catch (SQLException e) {
		}

		try {
			if (rs.findColumn("has_response") > 0) {
				hasResp = rs.getInt("has_response") > 0 ? 1 : 0;
			}
		} catch (SQLException e) {
		}
		try {
			if (rs.findColumn("isread") > 0) {
				isread = rs.getInt("isread");
			}
		} catch (SQLException e) {
		}
		if (toExpandResponses.contains(ddbID)) {

		}

		try {
			if (rs.findColumn("topicid") > 0) {
				topicid = rs.getInt("topicid");
			}
		} catch (SQLException e) {
			// e.printStackTrace();
		}
	}

	public void addViewText(String value, String tagName) {
		viewTexts.add(new ViewText(value, tagName));

	}

	public ArrayList<ViewText> getViewTexts() {
		return viewTexts;

	}

	public String getViewText(int pos) {
		try {
			return viewTexts.get(pos).getValueAsText();
		} catch (Exception e) {
			return viewTexts.toString();
		}
	}

	public void setHasResponse(boolean hasResponse) {
		this.hasResp = 1;
	}

	public StringBuilder toXML(IUser user) {

		if (type == ViewEntryType.DOCUMENT) {
			Connection conn = null;
			try {
				conn = db.getConnectionPool().getConnection();
				// IUsersActivity ua = db.getUserActivity();
				StringBuilder value = new StringBuilder(1000);
				switch (docType) {
				case DOCTYPE_MAIN:
					value = new StringBuilder("<entry hasattach=\"" + Integer.toString(hasAttachment) + "\" hasresponse=\"" + hasResp + "\" id=\""
					        + ddbID + "\"  doctype=\"" + docType + "\"  " + "docid=\"" + docID + "\" topicid=\"" + topicid + "\" "
					        + "url=\"Provider?type=edit&amp;element=document&amp;id=" + form + "&amp;docid=" + ddbID + "\"><viewcontent>");
					break;
				default:
					value = new StringBuilder("<entry hasattach=\"" + Integer.toString(hasAttachment) + "\" hasresponse=\"" + hasResp + "\" id=\""
					        + ddbID + "\"  doctype=\"" + docType + "\"  " + "docid=\"" + docID + "\" topicid=\"" + topicid + "\" "
					        + "url=\"Provider?type=edit&amp;element=document&amp;id=" + form + "&amp;docid=" + ddbID + "\"><viewcontent>");
					break;
				}
				for (ViewText vt : viewTexts) {
					value.append(vt.toXML());
				}

				value.append("</viewcontent>");
				if (hasResp == 1 && descendants.length() > 0) {
					value.append("<responses>");
					value.append(descendants);
					value.append("</responses>");
				}
				return value.append("</entry>");
			} finally {
				db.getConnectionPool().returnConnection(conn);
			}
		} else {
			StringBuilder value = new StringBuilder("<entry><viewcontent>");

			for (ViewText vt : viewTexts) {
				value.append(vt.toXML());
			}
			value.append("</viewcontent>");
			return value.append("</entry>");

		}

	}

	public int hasAttachment() {
		return hasAttachment;
	}

	@Override
	public String toString() {
		String value = "";
		if (type == ViewEntryType.DOCUMENT) {

			for (ViewText vt : viewTexts) {
				value += vt.getValueAsText();
			}

			return "type=" + type + ", docid=" + docID + ", doctype=" + docType + ", value=" + value;
		} else {

			for (ViewText vt : viewTexts) {
				value += vt.getValueAsText();
			}

			return "type=" + type + " value=" + value;
		}
	}

	@Override
	public StringBuffer toXML() {
		StringBuffer value = new StringBuffer("<entry><viewcontent>");

		for (ViewText vt : viewTexts) {
			value.append(vt.toXML());
		}

		return value.append("</viewcontent></entry>");
	}

	public Tag toTag() {
		Tag entryTag = new Tag("entry");
		entryTag.addTag(new Tag("viewcontent"));
		for (ViewText vt : viewTexts) {
			entryTag.addTag(vt.getTag());
		}
		return entryTag;
	}

	@Override
	public BigDecimal getViewNumberValue() {
		return viewNumberValue;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ViewEntry)) {
			return false;
		}

		ViewEntry viewEntry = (ViewEntry) o;

		if (!ddbID.equals(viewEntry.ddbID)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		if (ddbID != null) {
			return ddbID.hashCode();
		} else {
			return Util.generateRandomAsText(db.getParent().appType).hashCode();
		}
	}

}
