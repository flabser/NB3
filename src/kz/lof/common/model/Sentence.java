package kz.lof.common.model;

import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import kz.lof.dataengine.jpa.AppEntity;
import kz.lof.localization.LanguageCode;

@Entity
@Table(name = "_sentences")
@NamedQuery(name = "Sentence.findAll", query = "SELECT m FROM Sentence AS m ORDER BY m.regDate")
public class Sentence extends AppEntity<UUID> {

	@Column(length = 128, unique = true)
	private String keyword;

	@Column(name = "localized_name")
	private Map<LanguageCode, String> localizedName;

	private int hits;

}
