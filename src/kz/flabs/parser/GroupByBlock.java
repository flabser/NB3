package kz.flabs.parser;

import java.util.StringTokenizer;

import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.webrule.constants.QueryType;
import kz.flabs.webrule.constants.FieldType;
import kz.flabs.webrule.constants.TagPublicationFormatType;

public class GroupByBlock {
	public String operator;
	public String groupByCondition;

	public String fieldName;
	private FieldType type = FieldType.TEXT;
	private String customFieldsTable;

	public GroupByBlock(String fieldName, QueryType docType, TagPublicationFormatType publishAs){	
		StringTokenizer st = new StringTokenizer(fieldName,"#");
		this.fieldName = st.nextToken();
		if(st.hasMoreTokens()){    		
			type = FieldType.valueOf(st.nextToken().toUpperCase());
		}

		customFieldsTable = DatabaseUtil.getCustomTableName(docType);
		
		if (publishAs == TagPublicationFormatType.GLOSSARY){			
			operator = customFieldsTable + ".valueasglossary";			
		}else{		
			switch(type){
			case NUMBER:
				operator = customFieldsTable + ".valueasnumber";
				break;
			case GLOSSARY:
				operator = customFieldsTable + ".valueasglossary";
				break;	
			case DATETIME:
				operator = customFieldsTable + ".valueasdate";
				break;		
			default:
				operator = customFieldsTable + ".value";
			}			
		}
	}

	public String getGroup(){		
		return customFieldsTable + ".name = '" + fieldName + "'";		

	}	

	public String getCondition(String value){
		switch(type){
		case NUMBER:
			return operator + "=" + value;				
		case DATETIME:
			return operator + "=" + value;		
		default:
			return operator + "='" + value + "'";	
		}
	}	
}
