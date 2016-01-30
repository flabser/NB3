package kz.flabs.dataengine;

import kz.flabs.exception.LicenseException;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.runtimeobj.Filter;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.glossary.Glossary;
import kz.flabs.runtimeobj.document.structure.Department;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.runtimeobj.document.structure.Organization;
import kz.flabs.runtimeobj.document.structure.UserGroup;
import kz.flabs.users.RunTimeParameters;
import kz.flabs.users.User;
import kz.nextbase.script._ViewEntryCollection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public interface IStructure {
	
	void setConnectionPool(IDBConnectionPool pool);
	StringBuffer getExpandedStructure();
	StringBuffer getStructure(Set<DocID> toExpand);
	Employer getAppUser(String user);
	Employer getAppUserByCondition(String condition);
	ArrayList<Employer> getAppUsersByRoles(String rolename);
	
	int getStructObjByConditionCount(IQueryFormula nf);
	StringBuffer getStructObjByCondition(IQueryFormula queryCondition, int offset,int pageSize);
	StringBuffer getResponses(int parentDocID, int parentDocType);
	
	int insertOrganization(Organization doc);
	int updateOrganization(Organization doc);
	int deleteOrganization(int id);
	Organization getOrganization(int id, User user);

    _ViewEntryCollection getOrganization(ISelectFormula sf, User user, int pageNum, int pageSize, RunTimeParameters parameters);
	
	int insertDepartment(Department doc);
	int updateDepartment(Department doc);
	StringBuffer getDepartments(int parentDocID, int parentDocType, Set<DocID> toExpand);
	Department getDepartment(int id, User user);
	
	int insertEmployer(Employer doc) throws LicenseException;
	int updateEmployer(Employer doc);
	StringBuffer getEmployers(int parentDocID, int parentDocType, Set<DocID> toExpand);
	Employer getEmployer(int id, User user);
	ArrayList<Employer> getAllEmployers();
	StringBuffer getEmployersByRoles(String rolename);
	

	int insertGroup(UserGroup doc);
	UserGroup getGroup(int id, Set<String> complexUserID, String absoluteUserID);
	UserGroup getGroup(String groupName, Set<String> complexUserID, String absoluteUserID);
	int updateGroup(UserGroup doc) ;
	StringBuffer getGroupsByCondition(IQueryFormula queryCondition, int offset,int pageSize, String fieldsCond, Set<String> toExpand);
	StringBuffer getGroupsByCondition(IQueryFormula queryCondition);
	int getGroupsCountByCondition(IQueryFormula nf, String userID);

    UserGroup getGroupByParent(int parentDocID, int parentDocType);

    ArrayList<BaseDocument> getAllGroups();
	int getGroupsCount();
	IDatabase getParent();
	Filter getFilter(int filterID, HashSet<String> allUserGroups, String userID);
	@Deprecated
	Filter fillFilterDoc(Connection conn, ResultSet rs);
	StringBuffer getEmployersByFrequencyExecution();
	int deleteEmployer(int id);
	boolean hasGroup(String name, Set<String> complexUserID, String absoluteUserID);
	
	Glossary getGlossaryDocumentByID(int docID);

    ISelectFormula getSelectFormula(FormulaBlocks fb);
    DatabaseType getRDBMSType();

    _ViewEntryCollection getStructureCollection(ISelectFormula sf, User user, int pageNum, int pageSize, RunTimeParameters parameters);
}