package kz.nextbase.script.struct;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.ISelectFormula;
import kz.flabs.dataengine.IStructure;
import kz.flabs.exception.DocumentException;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.runtimeobj.document.structure.Department;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.document.structure.UserGroup;
import kz.flabs.users.RunTimeParameters;
import kz.flabs.users.User;
import kz.flabs.webrule.constants.QueryType;
import kz.nextbase.script._Session;
import kz.nextbase.script._ViewEntryCollection;

import java.util.ArrayList;
import java.util.Set;

public class _Structure {
	private IDatabase db;
	private IStructure struct;
	private User user;

	public _Structure(IDatabase db, User currentUser) {
		this.db = db;
		struct = db.getStructure();
		this.user = currentUser;
	}

	public _Employer getUserByCondition(String condition)
			throws DocumentException {
		Employer emp = struct.getAppUserByCondition(condition);
		if (emp != null) {
			return new _Employer(emp);
		} else {
			return null;
		}
	}

	public String getUserProperty(String userID, String field)
			throws DocumentException {
		AppEnv.logger
		.errorLogEntry("Groovy - use _User getUser(String userID)");
		return "";
	}

	public ArrayList<_Employer> getAllEmployers() {
		ArrayList<_Employer> result = new ArrayList<_Employer>();
		for (Employer emp: struct.getAllEmployers()) {
			result.add(new _Employer(emp));
		}
		return result;
	}

	public ArrayList<_Employer> getAppUsersByRoles(String rolename) {
		ArrayList<_Employer> result = new ArrayList<_Employer>();
		for (Employer emp: struct.getAppUsersByRoles(rolename)) {
			result.add(new _Employer(emp));
		}
		return result;
	}

	/**You should use public _Employer getEmployer(String userID) instead of this one**/
	@Deprecated 
	public _Employer getUser(String userID) throws DocumentException {
		User user = new User(userID, db);	
		return new _Employer(user.getAppUser());
	}

    public _UserGroup getGroupByParent(_Session session, int parentDocID, int parentDocType) {
       UserGroup group = this.struct.getGroupByParent(parentDocID, parentDocType);
       return new _UserGroup(group, session);
    }
    public _UserGroup getGroup(_Session session, String name, Set<String> complexUserID, String absoluteUserID) {
        UserGroup group = this.struct.getGroup(name, complexUserID, absoluteUserID);
        return new _UserGroup(group, session);
    }

	public _Employer getEmployer(String userID) throws DocumentException {
		User user = new User(userID, db);	
		return new _Employer(user.getAppUser());
	}

	public _Department getDepartment(int depID, _Session session) {
		Department department = this.struct.getDepartment(depID, new User(Const.sysUser));
		return new _Department(department, session);
	}

    public _ViewEntryCollection getOrganization(String queryCondition, int pageNum, boolean checkResponse) {
        FormulaBlocks queryFormulaBlocks = new FormulaBlocks(queryCondition, QueryType.STRUCTURE);
        ISelectFormula sf = this.db.getSelectFormula(queryFormulaBlocks);
        int pageSize = user.getSession().pageSize;
        RunTimeParameters parameters = new RunTimeParameters();
        return this.struct.getOrganization(sf, user, pageNum, pageSize, parameters);
    }

    public _ViewEntryCollection getStructureEntries(String queryCondition, int pageNum, boolean checkResponse) {
        FormulaBlocks queryFormulaBlocks = new FormulaBlocks(queryCondition, QueryType.STRUCTURE);
        ISelectFormula sf = this.db.getSelectFormula(queryFormulaBlocks);
        int pageSize = user.getSession().pageSize;
        RunTimeParameters parameters = new RunTimeParameters();
        return this.struct.getStructureCollection(sf, user, pageNum, pageSize, parameters);
    }


}
