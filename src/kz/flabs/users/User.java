package kz.flabs.users;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import kz.flabs.dataengine.Const;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.exception.WebFormValueExceptionType;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.BlobFile;
import kz.flabs.util.Util;
import kz.lof.appenv.AppEnv;

import org.apache.catalina.realm.RealmBase;

@Deprecated
public class User extends BaseDocument implements Const {
	public int docID;
	public boolean authorized;
	public boolean authorizedByHash;

	private static final long serialVersionUID = 1L;
	public final static String ANONYMOUS_USER = "anonymous";

	private String userID;

	private String password;
	private String passwordHash = "";
	private String email = "";
	private boolean isSupervisor;
	private int hash;
	private String publicKey = "";
	private String userName;

	public User() {

		userID = ANONYMOUS_USER;
	}

	public User(AppEnv env) {
		this.env = env;

		userID = ANONYMOUS_USER;
	}

	public User(String u) {

		setUserID(u);
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publickey) {
		this.publicKey = publickey;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		try {
			this.userID = userID;
		} catch (Exception e) {

		}
	}

	public HashSet<String> getAllUserGroups() {
		HashSet<String> userGroups = new HashSet<String>();

		return userGroups;
	}

	public void fill(ResultSet rs) throws SQLException {
		try {
			docID = rs.getInt("DOCID");
			userID = rs.getString("USERID");
			setEmail(rs.getString("EMAIL"));
			password = rs.getString("PWD");
			passwordHash = rs.getString("PWDHASH");
			publicKey = rs.getString("PUBLICKEY");
			int isa = rs.getInt("ISADMIN");
			if (isa == 1) {
				isSupervisor = true;
			}
			setHash(rs.getInt("LOGINHASH"));
			isValid = true;
		} catch (Exception e) {
			isValid = false;
		}
	}

	public String getPassword() {
		return password;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPassword(String password) throws WebFormValueException {
		if (!("".equalsIgnoreCase(password))) {
			if (Util.pwdIsCorrect(password)) {
				this.password = password;
			} else {
				throw new WebFormValueException(WebFormValueExceptionType.FORMDATA_INCORRECT, "password");
			}
		}
	}

	public void setPasswordHash(String password) throws WebFormValueException {
		if (!("".equalsIgnoreCase(password))) {
			if (Util.pwdIsCorrect(password)) {
				// this.passwordHash = password.hashCode()+"";
				// this.passwordHash = getMD5Hash(password);
				RealmBase rb = null;
				this.passwordHash = rb.Digest(password, "MD5", "UTF-8");
			} else {
				throw new WebFormValueException(WebFormValueExceptionType.FORMDATA_INCORRECT, "password");
			}
		}
	}

	/*
	 * public void setPassword(String oldPassword, String newPassword) throws
	 * WebFormValueException { if (!newPassword.equals("")){ if (isNewDoc()){
	 * this.password = newPassword; }else{ if ((!isNewDoc()) &&
	 * oldPassword.equals(oldPassword)){ this.password = newPassword; }else{
	 * throw new
	 * WebFormValueException(WebFormValueExceptionType.OLD_PWD_INCORRECT, ""); }
	 * } } }
	 */

	@Override
	public String getCurrentUserID() {
		return userID;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) throws WebFormValueException {
		if (email != null) {
			if (email.equalsIgnoreCase("")) {
				this.email = "";
			} else if (Util.addrIsCorrect(email)) {
				this.email = email;
			} else {
				throw new WebFormValueException(WebFormValueExceptionType.FORMDATA_INCORRECT, "email");
			}
		}
	}

	public boolean isSupervisor() {
		return isSupervisor;
	}

	public int getIsAdmin() {
		if (isSupervisor) {
			return 1;
		} else {
			return 0;
		}
	}

	public void setAdmin(boolean isAdmin) {
		this.isSupervisor = isAdmin;
	}

	public void setAdmin(String isAdmin) {
		if (isAdmin.equalsIgnoreCase("1")) {
			this.isSupervisor = true;
		} else {
			this.isSupervisor = false;
		}
	}

	public void setAdmin(String[] isAdmin) {
		try {
			String value = isAdmin[0];
			setAdmin(value);
		} catch (Exception e) {
			this.isSupervisor = false;
		}
	}

	public void setHash(int hash) {
		this.hash = hash;
	}

	public int getHash() {
		return hash;
	}

	public void fillFieldsToSaveLight(HashMap<String, String[]> fields) throws WebFormValueException {
		setUserID(getWebFormValue("userid", fields, userID)[0]);
		setEmail(getWebFormValue("email", fields, email)[0]);
		setPassword(getWebFormValue("pwd", fields, password)[0]);
		setPasswordHash(getWebFormValue("pwd", fields, password)[0]);
		String p_eds = getWebFormValue("p_eds", fields, "")[0];
		for (String key : fields.keySet()) {
			String formSesID = getWebFormValue("formsesid", fields, "")[0];
			if (!"".equalsIgnoreCase(formSesID)) {
				HashMap<String, BlobFile> uploadedFiles = new RuntimeObjUtil().getUploadedFiles(fields);
				if (uploadedFiles.size() > 0) {
					for (Map.Entry<String, BlobFile> file_entry : uploadedFiles.entrySet()) {

					}
				}
			}

		}
		// this.appUser.setReplacers(fields);
	}

	@Override
	public String toString() {
		return "userID=" + userID + ", email=" + email;
	}

	public String toXML() {
		return "<userid>" + userID + "</userid>";
	}

	public String usersByKeytoXML() {
		return "<userid>" + userID + "</userid>" + "<key>" + docID + "</key>" + "<email>" + email + "</email>";
	}

	public void setUserName(String name) {
		userName = name;
	}

	public String getUserName() {
		return userName;
	}

	public String getLogin() {
		return userID;
	}
}
