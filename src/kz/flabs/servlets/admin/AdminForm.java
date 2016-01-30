package kz.flabs.servlets.admin;

import kz.flabs.dataengine.IStructure;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.users.User;

import javax.servlet.http.HttpServletRequest;

public class AdminForm {	
	private Document doc;
	private int docID;
	private boolean isNewDoc;
	private DocID parentDocID;
	private IStructure struct;
	private AdminDocumentType formType;
	private User currentUser;
	
	public AdminForm(IStructure struct, AdminDocumentType formType, String key, DocID parentDocID, String currentUser){
		this.struct = struct;
		this.formType = formType;
		this.currentUser = new User(currentUser, struct);
		try{
			docID = Integer.parseInt(key);			
			switch(formType){
			case ORGANIZATION:				
				doc = struct.getOrganization(docID, this.currentUser);
				break;
			case DEPARTMENT:				
				doc = struct.getDepartment(docID, this.currentUser);
				break;
			case EMPLOYER:				
				doc = struct.getEmployer(docID, this.currentUser);
				break;
			}
		}catch( NumberFormatException nfe){
			isNewDoc = true;
			this.parentDocID = parentDocID;
		}
	}

	int save(HttpServletRequest request){
		if(!isNewDoc){
			doc.setDocID(docID);
		}
		
		switch(formType){
		case ORGANIZATION:				
		//	doc = struct.getOrganization(docID, currentUser);
			break;
		case DEPARTMENT:				
	//		doc = struct.getDepartment(docID, currentUser);
			break;
		case EMPLOYER:
			Employer doc = struct.getEmployer(docID, this.currentUser);
			doc.setViewText(request.getParameter("viewtext"));
			doc.setFullName(request.getParameter("fullname"));
			doc.setShortName(request.getParameter("shortname"));
			//doc.setUserID(request.getParameter("userid"));
			doc.setComment(request.getParameter("comment"));
			//doc = struct.getEmployer(docID, currentUser);
			break;
		}
		
		return 0;
	}
	
	
}
