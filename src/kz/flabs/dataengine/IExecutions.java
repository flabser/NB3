package kz.flabs.dataengine;

import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.document.Execution;
import kz.flabs.users.User;

import java.util.Set;

public interface IExecutions {
	Execution getExecutionByID(int docID, Set<String> complexUserID, String absoluteUserID) throws DocumentAccessException;
	int insertExecution(Execution doc, User user) throws DocumentException;
	int updateExecution(Execution doc, User user) throws DocumentAccessException, DocumentException;
	
}
