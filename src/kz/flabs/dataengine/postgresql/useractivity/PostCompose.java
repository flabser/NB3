package kz.flabs.dataengine.postgresql.useractivity;

import kz.flabs.dataengine.IDatabase;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.users.User;


public class PostCompose extends kz.flabs.dataengine.h2.usersactivity.PostCompose{
	
	protected PostCompose(IDatabase db, Document doc, User user){
		super(db, doc, user);
	}
	
}
