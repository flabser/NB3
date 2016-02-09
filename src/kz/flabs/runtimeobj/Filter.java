package kz.flabs.runtimeobj;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import kz.flabs.appenv.AppEnv;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.lof.server.Server;

public class Filter extends BaseDocument {
	private static final long serialVersionUID = 1L;
	private String name = "";
	private String userid;
	private int enable;
	private int filterID;
	private HashMap<String, String> conditions = new HashMap<String, String>();

	public Filter(AppEnv env) {
		this.env = env;
	}

	public HashMap<String, String> getConditions() {
		return conditions;
	}

	public void addCondition(String name, String value) {
		if (name == null || value == null) {
			return;
		}
		if (name.equalsIgnoreCase("name")) {
			this.name = value;
			return;
		} else if (name.equalsIgnoreCase("mode")) {
			try {
				this.enable = Integer.parseInt(value);
				return;
			} catch (NumberFormatException e) {
				Server.logger.errorLogEntry(e);
			}
		} else if (name.equalsIgnoreCase("id") && !value.equalsIgnoreCase("")) {
			try {
				this.filterID = Integer.parseInt(value);
				return;
			} catch (NumberFormatException e) {
				Server.logger.errorLogEntry(e);
			}
		}
		conditions.put(name, value);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		if (name != null) {
			this.name = name;
		}
	}

	public int getFilterID() {
		return filterID;
	}

	public void setFilterID(int id) {
		this.filterID = id;
	}

	public String getUserID() {
		return this.userid;
	}

	public void setUserID(String userid) {
		this.userid = userid;
	}

	public int getEnable() {
		return enable;
	}

	public void setEnable(int value) {
		if (value == 1) {
			this.enable = 1;
		} else {
			this.enable = 0;
		}
	}

	public int save(Set<String> complexUserID, String absoluteUserID) throws DocumentAccessException, DocumentException {
		int docID = 0;
		if (isNewDoc()) {
			setRegDate(new Date());
			setLastUpdate(getRegDate());

			setDocID(docID);
			setNewDoc(false);
		} else {
			setLastUpdate(new Date());

		}

		return docID;

	}

	@Override
	public String getURL() {
		return "/" + env.appType + "/Provider?type=outline&id=outline&subtype=filter&subid=" + this.filterID;
	}

	@Override
	public int hashCode() {
		int hashCode = this.name.hashCode() + this.userid.hashCode();
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		Filter filter = (Filter) obj;
		return this.name.equalsIgnoreCase(filter.name) && this.userid.equalsIgnoreCase(filter.userid);
	}

	@Override
	public String toString() {
		return name;
	}

}
