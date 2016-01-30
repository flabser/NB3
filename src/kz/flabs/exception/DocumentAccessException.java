package kz.flabs.exception;

import kz.flabs.users.User;

public class DocumentAccessException extends Exception {
	public int id;
	public String user;
	public ExceptionType exceptionType;
	
	private static final long serialVersionUID = 4762010135613823296L;
	private String errorText;
	
		
	public DocumentAccessException(ExceptionType error, String user) {
		super();
		exceptionType = error;
		this.user = user;
		switch(error){ 
		case DOCUMENT_DELETE_RESTRICTED:		
			errorText = "No permissions to delete the document , user=" + user + "(" + new User(user).getAllUserGroups() + ")";
			break;
		case DOCUMENT_READ_RESTRICTED:		
			errorText = "No permissions to read the document or document deleted , user=" + user + "(" + new User(user).getAllUserGroups() + ")";
			break;
		case DOCUMENT_WRITE_ACCESS_RESTRICTED:
			errorText = "No permissions to update the document , user=" + user + "(" + new User(user).getAllUserGroups() + ")";
			break;		
		}		
	}
	
	public String getMessage(){
		return errorText;
	}
	
	public String toString(){
		return errorText;
	}
	
}
