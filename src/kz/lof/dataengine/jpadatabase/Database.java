package kz.lof.dataengine.jpadatabase;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabasePoolException;
import kz.flabs.dataengine.DatabaseType;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IFTIndexEngine;
import kz.flabs.webrule.module.ExternalModule;
import kz.flabs.webrule.module.ExternalModuleType;
import kz.lof.dataengine.jpadatabase.ftengine.FTSearchEngine;
import kz.lof.env.Environment;
import kz.lof.server.Server;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.jpa.PersistenceProvider;

public class Database extends kz.flabs.dataengine.h2.Database implements IDatabase, Const {
	protected EntityManagerFactory factory;
	private FTSearchEngine ftEngine;

	public Database(AppEnv env) throws DatabasePoolException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		super(env, DatabaseType.JPA);
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(PersistenceUnitProperties.JDBC_DRIVER, env.globalSetting.driver);
		properties.put(PersistenceUnitProperties.JDBC_USER, env.globalSetting.getDbUserName());
		properties.put(PersistenceUnitProperties.JDBC_PASSWORD, env.globalSetting.getDbPassword());
		properties.put(PersistenceUnitProperties.JDBC_URL, connectionURL);

		// INFO,
		// OFF,
		// ALL,
		// CONFIG (developing)
		properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "OFF");
		properties.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.CREATE_OR_EXTEND);
		properties
		        .put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_ACTION, PersistenceUnitProperties.SCHEMA_GENERATION_DROP_AND_CREATE_ACTION);

		PersistenceProvider pp = new PersistenceProvider();
		factory = pp.createEntityManagerFactory(env.appType, properties);
		if (factory == null) {
			Server.logger.errorLogEntry("the entity manager of \"" + env.appType + "\" has not been initialized");

		}
		ftEngine = new FTSearchEngine(this);
	}

	@Override
	protected void initStructPool() {
		for (ExternalModule module : env.globalSetting.extModuleMap.values()) {
			if (module.getType() == ExternalModuleType.STAFF) {
				externalStructureApp = module.getName();
				Environment.addDelayedInit(this);
			} else {
				Environment.addDelayedInit(this);
			}
		}
		structDbPool = dbPool;
	}

	@Override
	public String toString() {
		return "version JPA";
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return factory;
	}

	@Override
	public IFTIndexEngine getFTSearchEngine() {
		return ftEngine;
	}

}
