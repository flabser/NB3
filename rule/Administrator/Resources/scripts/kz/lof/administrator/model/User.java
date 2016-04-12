package kz.lof.administrator.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

import kz.flabs.util.Util;
import kz.lof.localization.LanguageCode;
import kz.lof.scripting.IPOJOObject;
import kz.lof.scripting._Session;
import kz.lof.user.IUser;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.config.CacheIsolationType;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "_users")
@NamedQuery(name = "User.findAll", query = "SELECT m FROM User AS m ORDER BY m.regDate")
@Cache(isolation = CacheIsolationType.ISOLATED)
public class User implements IUser<Long>, IPOJOObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
	protected Long id;

	@Column(name = "reg_date", nullable = false, updatable = false)
	protected Date regDate;

	@Transient
	private String userName;

	@Transient
	private List<String> roles;

	@Column(length = 64, unique = true)
	private String login;

	@Column(length = 64)
	private String email = "";

	private String pwd;

	private String pwdHash;

	@ManyToMany
	@JoinTable(name = "_allowed_apps", joinColumns = @JoinColumn(name = "app_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
	private List<Application> allowedApps;

	private int status;

	private String theme;

	@Column(name = "default_lang")
	private LanguageCode defaultLang;

	@Column(name = "i_su")
	private boolean isSuperUser;

	@Transient
	private boolean isAuthorized;

	@JsonIgnore
	@Transient
	protected boolean isEditable;

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public Long getId() {
		return id;
	}

	@PrePersist
	private void prePersist() {
		regDate = new Date();
	}

	public Date getRegDate() {
		return regDate;
	}

	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}

	@Override
	public String getUserName() {
		return userName;
	}

	@Override
	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public List<String> getRoles() {
		return roles;
	}

	@Override
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	@Override
	public String getPwdHash() {
		return pwdHash;
	}

	public void setPwdHash(String pwdHash) {
		this.pwdHash = pwdHash;
	}

	@Override
	public List<Application> getAllowedApps() {
		return allowedApps;
	}

	public void setAllowedApps(List<Application> allowedApps) {
		this.allowedApps = allowedApps;
	}

	public int getStatus() {
		return status;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	@Override
	public LanguageCode getDefaultLang() {
		return defaultLang;
	}

	public void setDefaultLang(LanguageCode defaultLang) {
		this.defaultLang = defaultLang;
	}

	@Override
	public boolean isSuperUser() {
		return isSuperUser;
	}

	public void setSuperUser(boolean isSuperUser) {
		this.isSuperUser = isSuperUser;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public boolean isAuthorized() {
		return isAuthorized;
	}

	@Override
	public void setAuthorized(boolean isAuthorized) {
		this.isAuthorized = isAuthorized;
	}

	@Override
	public boolean isEditable() {
		return isEditable;
	}

	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}

	@Override
	public String getUserID() {
		return login;
	}

	@Override
	public String getURL() {
		return "Provider?id=user-form&amp;docid=" + getId();
	}

	@Override
	public String getFullXMLChunk(_Session ses) {
		StringBuilder chunk = new StringBuilder(1000);
		chunk.append("<regdate>" + Util.simpleDateTimeFormat.format(regDate) + "</regdate>");
		chunk.append("<login>" + login + "</login>");
		chunk.append("<email>" + email + "</email>");
		chunk.append("<issuperuser>" + isSuperUser + "</issuperuser>");
		chunk.append("<apps>");
		try {
			String asText = "";
			for (Application a : allowedApps) {
				asText += "<entry id=\"" + a.getId() + "\">" + a.getLocalizedName().get(ses.getLang()) + "</entry>";
			}
			chunk.append("<apps>" + asText + "</apps>");
		} catch (NullPointerException e) {
			chunk.append("<apps></apps>");
		}
		chunk.append("</apps>");
		return chunk.toString();
	}

	@Override
	public String getShortXMLChunk(_Session ses) {
		StringBuilder chunk = new StringBuilder(1000);
		chunk.append("<regdate>" + Util.simpleDateTimeFormat.format(regDate) + "</regdate>");
		chunk.append("<login>" + login + "</login>");
		chunk.append("<email>" + email + "</email>");
		chunk.append("<issuperuser>" + isSuperUser + "</issuperuser>");
		return chunk.toString();
	}

	@Override
	public Object getJSONObj(_Session ses) {
		return this;
	}

	@Override
	public String getIdentifier() {
		Long id = getId();
		if (id == null) {
			return "null";
		} else {
			return getId().toString();
		}
	}

	@Override
	public void setRoles(List<String> allRoles) {
		roles = allRoles;

	}

}
