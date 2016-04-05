package kz.lof.administrator.dao;

import java.util.UUID;

import kz.lof.administrator.model.Language;
import kz.lof.appenv.AppEnv;
import kz.lof.dataengine.jpa.DAO;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.scripting._Session;
import kz.lof.user.SuperUser;

public class LanguageDAO extends DAO<Language, UUID> {

	public LanguageDAO() {
		super(Language.class, new _Session(new AppEnv(EnvConst.ADMINISTRATOR_APP_NAME, Environment.dataBase), new SuperUser()));
	}

	public LanguageDAO(_Session session) {
		super(Language.class, session);
	}

}
