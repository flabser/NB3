package reference.model;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import kz.flabs.dataengine.jpa.AppEntity;
import kz.flabs.localization.LanguageType;

@MappedSuperclass
public class Reference extends AppEntity {
	@Column(length = 128, unique = true)
	private String name;

	@Column(name = "localized_name")
	private Map<LanguageType, String> localizedName;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public Map<LanguageType, String> getLocalizedName() {
		return localizedName;
	}

	public void setLocalizedName(Map<LanguageType, String> name) {
		this.localizedName = name;
	}

	@Override
	public String getShortXMLChunk(LanguageType lang) {
		return "<name>" + getName() + "</name>";
	}
}
