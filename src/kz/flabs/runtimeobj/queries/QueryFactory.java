package kz.flabs.runtimeobj.queries;

import kz.flabs.appenv.AppEnv;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.users.User;
import kz.flabs.webrule.query.IQueryRule;

public class QueryFactory {
	
	public static Query getQuery(AppEnv env,IQueryRule rule, User user) throws DocumentException, DocumentAccessException{
		switch(rule.getQueryType()){
		case STRUCTURE:
			return new StructureQuery(env, rule, user);
		case GLOSSARY:
			return new GlossaryQuery(env, rule, user);
		case GROUP:
			return new GroupQuery(env, rule, user);
		default:
			return new Query(env, rule, user);		
		}
	}
}
