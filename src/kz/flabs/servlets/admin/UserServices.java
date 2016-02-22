package kz.flabs.servlets.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseFactory;
import kz.flabs.dataengine.ISystemDatabase;
import kz.flabs.dataengine.h2.UserApplicationProfile;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.RuleException;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.localization.LocalizatorException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.users.User;

public class UserServices {
	private ISystemDatabase sysDatabase;
	private int count;

	public UserServices() {
		sysDatabase = DatabaseFactory.getSysDatabase();
	}

	public String getUserListWrapper(String keyWord, int pageNum, int pageSize) {
		String condition = "", xmlFragment = "";
		if (keyWord != null) {
			condition = "USERID LIKE '" + keyWord + "%'";
		}
		count = sysDatabase.getAllUsersCount(condition);
		ArrayList<User> fl = sysDatabase.getAllUsers(condition, calcStartEntry(pageNum, pageSize), pageSize);

		Iterator<User> it = fl.iterator();
		while (it.hasNext()) {
			User user = it.next();
			xmlFragment += "<entry docid=\"" + user.docID + "\" ><userid>" + user.getUserID() + "</userid>" + "<isadministrator>"
			        + user.isSupervisor() + "</isadministrator><email>" + user.getEmail() + "</email><redirecturl></redirecturl>" + "</entry>";
		}

		return xmlFragment;
	}

	public String getUserAsXML(int docID) throws RuleException, DocumentException, DocumentAccessException, QueryFormulaParserException,
	        LocalizatorException {
		String xmlContent = "", ea = "";
		User user = sysDatabase.getUser(docID);

		for (UserApplicationProfile app : user.enabledApps.values()) {
			ea += app.toXML();
		}

		if (user.getUserID() != null) {
			xmlContent += "<userid>" + user.getUserID() + "</userid>" + "<docid>" + user.docID + "</docid>" +

			"<email>" + user.getEmail() + "</email><password>" + user.getPassword() + "</password>" + "<isadmin>" + user.isSupervisor()
			        + "</isadmin>" + "<pk>" + user.getPublicKey() + "</pk>" + "<isadmin>" + user.isSupervisor() + "</isadmin>" + "<hash>"
			        + user.getHash() + "</hash>" + "<enabledapps>" + ea + "</enabledapps>";

			SourceSupplier ss = new SourceSupplier(user.getUserID());
			xmlContent += "<glossaries><apps></apps></glossaries>";
		}

		return xmlContent;
	}

	public String getBlankUserAsXML() throws RuleException, DocumentException, DocumentAccessException, QueryFormulaParserException,
	        LocalizatorException {
		String xmlContent = "";
		SourceSupplier ss = new SourceSupplier(Const.sysUser);
		xmlContent += "<glossaries><apps></apps></glossaries>";
		return xmlContent;
	}

	boolean saveUser(HashMap<String, String[]> parMap) throws WebFormValueException {
		int key = 0;
		try {
			key = Integer.parseInt(parMap.get("key")[0].toString());
		} catch (NumberFormatException nfe) {
			key = 0;
		}
		User user = sysDatabase.getUser(key);
		user.fillFieldsToSave(null, parMap);
		int docID = user.save(Const.sysGroupAsSet, Const.sysUser);
		if (docID > -1) {
			return true;
		} else {
			return false;
		}
	}

	boolean deleteUser(String id) {
		int docID = Integer.parseInt(id);
		return sysDatabase.deleteUser(docID);
	}

	public int getCount() {
		return count;
	}

	private int calcStartEntry(int pageNum, int pageSize) {
		int pageNumMinusOne = pageNum;
		pageNumMinusOne--;
		return pageNumMinusOne * pageSize;
	}

}
