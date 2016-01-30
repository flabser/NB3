package kz.flabs.parser;

import java.util.StringTokenizer;
import kz.flabs.webrule.constants.FieldType;

public class ParserUtil {
	
	public static String[] resolveFiledTypeBySuffix(String field){
		String result[] = new String[2];
		StringTokenizer st = new StringTokenizer(field,"#");
		result[0] = st.nextToken();
		if(st.hasMoreTokens()){		
			switch(FieldType.valueOf(st.nextToken().toUpperCase())){
			case NUMBER:
				result[1] = "valueasnumber";
				break;
			case GLOSSARY:
				result[1] = "valueasglossary";
				break;
			case DATETIME:
				result[1] = "valueasdate";
				break;
			default:
				result[1] = "value";
			}	
		}else{
			result[1] = "value";
		}
		return result;
	}
}
