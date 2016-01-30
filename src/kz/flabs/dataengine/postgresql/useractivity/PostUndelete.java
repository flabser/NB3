package kz.flabs.dataengine.postgresql.useractivity;

import kz.flabs.dataengine.IDatabase;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.users.User;

public class PostUndelete extends kz.flabs.dataengine.h2.usersactivity.PostUndelete {

	protected PostUndelete(IDatabase db, BaseDocument recoverDoc, User user){
		super(db, recoverDoc, user);
	}

}
