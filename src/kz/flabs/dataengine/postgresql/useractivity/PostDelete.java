package kz.flabs.dataengine.postgresql.useractivity;

import kz.flabs.dataengine.IDatabase;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.users.User;

public class PostDelete extends kz.flabs.dataengine.h2.usersactivity.PostDelete {

	PostDelete(IDatabase db, BaseDocument deletedDoc, User user){
		super(db,deletedDoc, user);
	}
	
}
