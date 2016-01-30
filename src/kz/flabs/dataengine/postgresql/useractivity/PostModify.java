package kz.flabs.dataengine.postgresql.useractivity;

import kz.flabs.dataengine.IDatabase;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.users.User;

public class PostModify extends kz.flabs.dataengine.h2.usersactivity.PostModify {
	
	PostModify(IDatabase db, Document oldDoc, Document modifiedDoc, User user){
		super(db, oldDoc, modifiedDoc, user);
	}


	
}
