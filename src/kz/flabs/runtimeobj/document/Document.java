package kz.flabs.runtimeobj.document;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.LicenseException;
import kz.flabs.users.User;

import java.util.Date;
public class Document extends BaseDocument implements Const {

	private static final long serialVersionUID = 1L;

	public Document(IDatabase db, String currentUser){
		docType = Const.DOCTYPE_MAIN;
		this.env = db.getParent();
		this.db = db;
		this.currentUserID = currentUser;
		dbID = db.getDbID();
		setNewDoc(true);
		setAuthor(currentUser);
	}


	public Document(AppEnv env, User currentUser){
		docType = Const.DOCTYPE_MAIN;
		this.env = env;
		db = env.getDataBase();
		dbID = db == null ? "" : db.getDbID();
		this.currentUserID = currentUser.getUserID();
		setNewDoc(true);
		setAuthor(currentUser.getUserID());
	}

    public int save(User user) throws DocumentAccessException, DocumentException, LicenseException, ComplexObjectException{
        computeViewText();
        normalizeViewTexts();
        int docID = 0;
        if(isNewDoc()){
            setRegDate(new Date());
            setLastUpdate(getRegDate());
            this.setDdbID("");
            docID = db.insertMainDocument(this, user);
            setDocID(docID);
            setNewDoc(false);
        }else{
            setLastUpdate(new Date());
            if (getViewDate() == null){
                setViewDate(getRegDate());
            }
            docID = db.updateMainDocument(this, user);
        }
        return docID;

    }

}
