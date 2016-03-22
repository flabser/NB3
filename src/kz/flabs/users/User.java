package kz.flabs.users;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.ISystemDatabase;
import kz.flabs.dataengine.h2.UserApplicationProfile;
import kz.flabs.exception.WebFormValueException;
import kz.flabs.exception.WebFormValueExceptionType;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.BlobFile;
import kz.flabs.util.Util;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.form.ISaveField;
import kz.lof.appenv.AppEnv;

import org.apache.catalina.realm.RealmBase;

@Deprecated
public class User extends BaseDocument implements Const {
	public int docID;
	public HashMap<String, UserApplicationProfile> enabledApps = new HashMap<String, UserApplicationProfile>();
	public boolean authorized;
	public boolean authorizedByHash;

	private static final long serialVersionUID = 1L;
	public final static String ANONYMOUS_USER = "anonymous";
	private transient ISystemDatabase sysDatabase;
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

	public User(String u, AppEnv env) {
		this(u, env.getDataBase());
		this.env = env;
	}

	public User(String u, IDatabase db) {

		sysDatabase.reloadUserData(this, u);
		if (db != null) {
			/*
			 * struct = db.getStructure(); appUser = struct.getAppUser(userID);
			 * if (appUser == null) { appUser = new Employer(struct); }
			 * appUser.setUser(this);
			 */
		}

	}

	public User(int userHash, AppEnv env) throws AuthFailedException {
		this.env = env;

		try {
			sysDatabase.reloadUserData(this, userHash);
		} catch (Exception e) {
			throw new AuthFailedException(AuthFailedExceptionType.SYSTEM_DATABASE_HAS_NOT_ANSWERED, "app=" + env);
		}
		IDatabase db = env.getDataBase();
		if (db != null) {
			/*
			 * struct = env.getDataBase().getStructure(); appUser =
			 * struct.getAppUser(userID); if (appUser == null) { appUser = new
			 * Employer(struct); } appUser.setUser(this);
			 */
		}
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
		if (userID.equals(sysUser)) {
			userGroups = supervisorGroupAsSet;
			userGroups.addAll(observerGroupAsList);
		}
		try {
			// userGroups.addAll(appUser.getAllUserGroups());
		} catch (Exception e) {
			userGroups.add(userID);
		}
		return userGroups;
	}

	public boolean addEnabledApp(String app, UserApplicationProfile ap) {
		enabledApps.put(app, ap);
		return true;
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

	@Override
	public void fillFieldsToSave(HashMap<String, ISaveField> saveFieldsMap, HashMap<String, String[]> fields) throws WebFormValueException {
		setUserID(getWebFormValue("userid", fields, userID)[0]);
		setEmail(getWebFormValue("email", fields, email)[0]);
		setPassword(getWebFormValue("pwd", fields, password)[0]);
		setPasswordHash(getWebFormValue("pwd", fields, password)[0]);
		setAdmin(getWebFormValue("isadmin", fields, "0"));
		String app[] = fields.get("enabledapps");
		String lm[] = fields.get("loginmode");
		this.enabledApps.clear();
		if (app != null) {
			for (int i = 0; i < app.length; i++) {
				if (!app[i].equals("")) {
					int loginMode = 0;
					try {
						loginMode = Integer.parseInt(lm[i]);
					} catch (NumberFormatException e) {
						loginMode = 0;
					}
					UserApplicationProfile ap = new UserApplicationProfile(app[i], loginMode);
					if (loginMode == 1) {
						String[] q = fields.get("question_" + ap.appName);
						String[] a = fields.get("answer_" + ap.appName);
						for (int i1 = 0; i1 < q.length; i1++) {
							UserApplicationProfile.QuestionAnswer qa = ap.new QuestionAnswer(q[i1].trim(), a[i1].trim());
							ap.getQuestionAnswer().add(qa);
						}
					}
					addEnabledApp(ap.appName, ap);
				}
			}
		}
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

	public int save(Set<String> complexUserID, String absoluteUserID) {
		if (docID == 0) {
			return sysDatabase.insert(this);
		} else {
			return sysDatabase.update(this);
		}
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

	public String getAppURLAsXml() {
		StringBuffer xmlContent = new StringBuffer(1000);
		for (Map.Entry<String, UserApplicationProfile> app : enabledApps.entrySet()) {
			xmlContent.append("<entry " + "url=\"" + enabledApps.get(app) + "\">" + XMLUtil.getAsTagValue(app.getKey()));
			if (app.getValue() != null) {
				xmlContent.append(app.getValue().toXML());
			}
			xmlContent.append("</entry>");
		}
		return xmlContent.toString();
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
