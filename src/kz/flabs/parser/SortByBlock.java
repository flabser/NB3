package kz.flabs.parser;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kz.flabs.dataengine.Const;
import kz.lof.appenv.AppEnv;

public class SortByBlock {
	public String operator;	
	public String sortByCondition;
	
	public String fieldName;
	public String order;
	public boolean paramatrizedBlock;
	
	private static final Pattern p = Pattern.compile("^'?\\$(\\S+[^'])");
		
	public SortByBlock(String sortExpression){	
		StringTokenizer st = new StringTokenizer(sortExpression,"#");
		this.fieldName = st.nextToken();
		if (st.hasMoreTokens()){
			this.order = st.nextToken();
		}else{
			this.order = Const.DEFAULT_SORT_ORDER;
		}	
	}
	
	public SortByBlock(){
		this.fieldName = Const.DEFAULT_SORT_COLUMN;
		this.order = Const.DEFAULT_SORT_ORDER;
	}
	
	public void putParameters(Map<String, String[]> fields){
				
		Matcher parFieldMatcher = p.matcher(fieldName);
		if (parFieldMatcher.find()){ 		
			fieldName = (parFieldMatcher.group(1));
			String parFieldName[] = fields.get(fieldName);
			if (parFieldName != null){		
				try {
					fieldName = new String(((String)parFieldName[0]).getBytes("ISO-8859-1"),"UTF-8");						
				} catch (UnsupportedEncodingException e) {
					AppEnv.logger.errorLogEntry(e);
				}
			}else{				
				AppEnv.logger.warningLogEntry("Parameter \"" + fieldName + "\" has not defined. It will use the default value");
				fieldName = Const.DEFAULT_SORT_COLUMN;
			}	
		}
	
		parFieldMatcher = p.matcher(order);
		if (parFieldMatcher.find()){ 		
			order = (parFieldMatcher.group(1));
			String parOrder[] = fields.get(order);
			if (parOrder != null){		
				try {
					order = new String(((String)parOrder[0]).getBytes("ISO-8859-1"),"UTF-8");						
				} catch (UnsupportedEncodingException e) {
					AppEnv.logger.errorLogEntry(e);
				}
			}else{
				AppEnv.logger.warningLogEntry("Parameter \"" + order + "\" has not defined. It will use the default value");
				order = Const.DEFAULT_SORT_ORDER;
			}
		}
					
	}	
	
}
