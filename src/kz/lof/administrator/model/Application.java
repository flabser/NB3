package kz.lof.administrator.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import kz.flabs.util.Util;
import kz.lof.administrator.dao.LanguageDAO;
import kz.lof.dataengine.jpa.AppEntity;
import kz.lof.dataengine.jpa.constants.AppCode;
import kz.lof.localization.LanguageCode;
import kz.lof.scripting._Session;

@Entity
@Table(name = "_apps")
@NamedQuery(name = "Application.findAll", query = "SELECT m FROM Application AS m ORDER BY m.regDate")
public class Application extends AppEntity<UUID> {
	@Enumerated(EnumType.STRING)
	@Column(nullable = true, length = 16)
	private AppCode code = AppCode.UNKNOWN;

	@Column(length = 128, unique = true)
	private String name;

	@ManyToMany(mappedBy = "allowedApps")
	private List<User> users;

	private List<AppCode> dependencies;

	@Column(name = "localized_name")
	private Map<LanguageCode, String> localizedName;

	private String defaultURL;

	private int position;

	@Column(name = "ftsearch_fields")
	private List<String> ftSearchFields;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<AppCode> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<AppCode> dependencies) {
		this.dependencies = dependencies;
	}

	@Override
	public String toString() {
		return name;
	}

	public Map<LanguageCode, String> getLocalizedName() {
		return localizedName;
	}

	public void setLocalizedName(Map<LanguageCode, String> name) {
		this.localizedName = name;
	}

	public String getDefaultURL() {
		return defaultURL;
	}

	public void setDefaultURL(String defaultURL) {
		this.defaultURL = defaultURL;
	}

	public AppCode getCode() {
		return code;
	}

	public void setCode(AppCode code) {
		this.code = code;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public List<String> getFtSearchFields() {
		return ftSearchFields;
	}

	public void setFtSearchFields(List<String> ftSearchFields) {
		this.ftSearchFields = ftSearchFields;
	}

	@Override
	public String getFullXMLChunk(_Session ses) {
		StringBuilder chunk = new StringBuilder(1000);
		chunk.append("<regdate>" + Util.simpleDateTimeFormat.format(regDate) + "</regdate>");
		chunk.append("<name>" + name + "</name>");
		chunk.append("<appcode>" + code + "</appcode>");
		chunk.append("<position>" + position + "</position>");
		chunk.append("<defaulturl>" + defaultURL + "</defaulturl>");
		chunk.append("<localizednames>");
		LanguageDAO lDao = new LanguageDAO(ses);
		List<Language> list = lDao.findAll();
		for (Language l : list) {
			chunk.append("<entry id=\"" + l.getCode() + "\">" + getLocalizedName(ses.getLang()) + "</entry>");
		}
		chunk.append("</localizednames>");
		return chunk.toString();
	}

	@Override
	public String getShortXMLChunk(_Session ses) {
		return "<app id=\"" + name + "\">" + localizedName.get(ses.getLang()) + "</app>" + "<pos>" + position + "</pos><url>"
		        + Util.getAsTagValue(defaultURL) + "</url>";
	}

	public String getLocalizedName(LanguageCode lang) {
		try {
			return localizedName.get(lang);
		} catch (Exception e) {
			return name;
		}
	}
}
