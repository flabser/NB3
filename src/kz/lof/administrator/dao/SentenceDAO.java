package kz.lof.administrator.dao;

import java.util.UUID;

import kz.lof.administrator.model.Sentence;
import kz.lof.appenv.AppEnv;
import kz.lof.dataengine.jpa.DAO;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.scripting._Session;
import kz.lof.user.SuperUser;

public class SentenceDAO extends DAO<Sentence, UUID> {

	public SentenceDAO() {
		super(Sentence.class, new _Session(new AppEnv(EnvConst.ADMINISTRATOR_APP_NAME, Environment.dataBase), new SuperUser()));
	}

	public SentenceDAO(_Session session) {
		super(Sentence.class, session);
	}

}
