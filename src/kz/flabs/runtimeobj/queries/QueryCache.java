package kz.flabs.runtimeobj.queries;

import java.util.ArrayList;
import java.util.Date;

import kz.flabs.runtimeobj.document.Document;
import kz.flabs.util.Util;

public class QueryCache {
	int id;
	public String cacheID;
	public Date birthTime;	
	public CacheInitiatorType initiator;
	public int hits;
	
	private int intVal;
	private int count;
	private StringBuffer cacheContent;
	
	public QueryCache(String cacheID, ArrayList<Document> col,  CacheInitiatorType initiator){
		this.cacheID = cacheID;		
		this.initiator =initiator;
		birthTime = new Date();
	}
	
	public QueryCache(String cacheID, int val,  CacheInitiatorType initiator){
		this.cacheID = cacheID;
		intVal = val;
		this.initiator =initiator;
		birthTime = new Date();
	}
	
	public QueryCache(String cacheID, int count, StringBuffer content,  CacheInitiatorType initiator){
		this.cacheID = cacheID;
		this.setCount(count);
		this.cacheContent = content;
		this.initiator =initiator;
		birthTime = new Date();
	}
	
	public QueryCache(int cacheID, StringBuffer content,  CacheInitiatorType initiator){
		id = cacheID;
		this.cacheContent = content;
		this.initiator =initiator;
		birthTime = new Date();
	}
	
	public void setContent(StringBuffer c){
		cacheContent = c;
	}
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public StringBuffer getContent(){
		hits ++ ;
		return cacheContent;
	}
	
	public int getIntContent(){
		return intVal;
	}
	
	public String toXML(){
		return "<cacheid>" + cacheID + "</cacheid><initiator>" + initiator + "</initiator>" +
				"<birthtime>" + Util.convertDataTimeToString(birthTime) + "</birthtime><hits>" + hits + "</hits>";  
	}

}
