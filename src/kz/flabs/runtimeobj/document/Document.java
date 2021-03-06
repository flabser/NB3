package kz.flabs.runtimeobj.document;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.users.User;
import kz.lof.appenv.AppEnv;

public class Document extends BaseDocument implements Const {

	private static final long serialVersionUID = 1L;

	public Document(IDatabase db, String currentUser) {
		docType = Const.DOCTYPE_MAIN;
		this.db = db;
		this.currentUserID = currentUser;
		// dbID = db.getDbID();
		setNewDoc(true);
		setAuthor(currentUser);
	}

	public Document(AppEnv env, User currentUser) {
		docType = Const.DOCTYPE_MAIN;
		this.env = env;
		db = env.getDataBase();
		// dbID = db == null ? "" : db.getDbID();
		this.currentUserID = currentUser.getUserID();
		setNewDoc(true);
		setAuthor(currentUser.getUserID());
	}

}
