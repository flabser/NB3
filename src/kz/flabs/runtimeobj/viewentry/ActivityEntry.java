package kz.flabs.runtimeobj.viewentry;

import java.sql.ResultSet;
import java.sql.SQLException;

import kz.flabs.dataengine.IDatabase;

/**
 * Created with IntelliJ IDEA. User: dzhillian Date: 23.03.13 Time: 9:51 To
 * change this template use File | Settings | File Templates.
 */
public class ActivityEntry extends ViewEntry {

	public ActivityEntry(ResultSet rs, ViewEntryType type, IDatabase db) throws SQLException {
		super("", 0);
		this.type = type;

		viewTexts.add(new ViewText(rs.getString("viewtext"), "viewtext"));

		viewTexts.add(new ViewText(rs.getString("userid"), "userid"));
		viewTexts.add(new ViewText(rs.getString("parameters"), "parameters"));
		viewTexts.add(new ViewText((rs.getBoolean("ACTIVITY_TYPE") ? 1 : 0), "type"));

		viewTexts.add(new ViewText(rs.getString("COMMENT"), "comment"));
		viewTexts.add(new ViewText(rs.getInt("PROCESSED_REC"), "processed_rec"));
		viewTexts.add(new ViewText(rs.getInt("PROCESSED_SIZE"), "processed_size"));
		viewTexts.add(new ViewText(rs.getString("TRANSACTION"), "transaction"));
		viewTexts.add(new ViewText(rs.getString("SERVICE_NAME"), "service_name"));
		viewTexts.add(new ViewText(rs.getString("METHOD_NAME"), "method_name"));
		viewTexts.add(new ViewText(rs.getString("SPRING_SERVER"), "spring_server"));
	}

}
