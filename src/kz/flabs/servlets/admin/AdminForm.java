package kz.flabs.servlets.admin;

import javax.servlet.http.HttpServletRequest;

import kz.flabs.dataengine.IStructure;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.users.User;

public class AdminForm {
	private Document doc;
	private int docID;
	private boolean isNewDoc;
	private DocID parentDocID;
	private IStructure struct;
	private AdminDocumentType formType;
	private User currentUser;

	public AdminForm(IStructure struct, AdminDocumentType formType, String key, DocID parentDocID, String currentUser) {
		this.struct = struct;
		this.formType = formType;
		this.currentUser = new User(currentUser, struct);
		try {
			docID = Integer.parseInt(key);
			switch (formType) {

			}
		} catch (NumberFormatException nfe) {
			isNewDoc = true;
			this.parentDocID = parentDocID;
		}
	}

	int save(HttpServletRequest request) {
		if (!isNewDoc) {
			doc.setDocID(docID);
		}

		switch (formType) {
		case ORGANIZATION:
			// doc = struct.getOrganization(docID, currentUser);
			break;
		case DEPARTMENT:
			// doc = struct.getDepartment(docID, currentUser);
			break;

		}

		return 0;
	}

}
