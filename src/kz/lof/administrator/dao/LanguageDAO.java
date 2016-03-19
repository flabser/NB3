package kz.lof.administrator.dao;

import java.util.UUID;

import kz.lof.administrator.model.Language;
import kz.lof.dataengine.jpa.DAO;
import kz.lof.scripting._Session;

public class LanguageDAO extends DAO<Language, UUID> {

	public LanguageDAO(_Session session) {
		super(Language.class, session);
	}

}
