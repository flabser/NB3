package kz.flabs.runtimeobj.queries;

import java.util.*;
import kz.flabs.appenv.AppEnv;

@Deprecated
public class CachePool {
	private static HashMap<String, QueryCache> caches = new HashMap<String, QueryCache>();
	private static HashMap<Integer, QueryCache> cs = new HashMap<Integer, QueryCache>();
	
		
	public static void update(StringBuffer content, int id,CacheInitiatorType initiator){
		if (content != null){			
			QueryCache cache = new QueryCache(id, content, initiator);
			cs.put(id, cache);
		}else{
			AppEnv.logger.warningLogEntry("Cache has not been updated, collection is empty id=" + id);
		}
	}
	
	public static boolean update(String cacheID, int count, StringBuffer content, CacheInitiatorType initiator){
		if (content != null){			
			QueryCache cache = new QueryCache(cacheID, count, content, initiator);
			caches.put(cacheID, cache);
			return true;
		}else{
			AppEnv.logger.warningLogEntry("Cache has not been updated, collection is empty");
		}
		return false;
	}
	
	public static void update(StringBuffer content, String query, String user,CacheInitiatorType initiator){
		if (content != null){
			String cacheID = getCacheID(query, user);
			QueryCache cache = new QueryCache(cacheID, 0, content, initiator);
			caches.put(cacheID, cache);
		}else{
			AppEnv.logger.warningLogEntry("Cache has not been updated, collection is empty query=" + query +" user" + user);
		}
	}
	
	public static void update(int colCount, String query, String user,CacheInitiatorType initiator){
		String cacheID = getCacheID(query, user);
		QueryCache cache = new QueryCache(cacheID, colCount, initiator);
		caches.put(cacheID, cache);
	}

	public static QueryCache getQueryCache(String query, String user){	
		return caches.get(getCacheID(query, user));
		//return null;
	}
		
	public static QueryCache getQueryCache(String id){	
		return caches.get(id);
	}
	
	public static boolean resetCache(String id){	
		caches.remove(id);
		return true;		
	}
	
	
	public static QueryCache getQueryCache(int id){	
		return cs.get(id);
	}
	
	public static HashMap<String, QueryCache> getActiveCacheKeyList(){		
		return caches;
	}

	public static void flush(){
		caches.clear();
	}

	public static HashMap<String, QueryCache> getCaches(){		
		return caches;
	}
	
	public static String toXML(){
		String returnVal = "";
		for(QueryCache cache: cs.values()){
			returnVal += cache.toXML();
		}
		return "<pool>" + returnVal + "</pool>";
	}
	
	private static String getCacheID(String query, String user){
		return query + user;
	}
	
}
