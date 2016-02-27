package kz.flabs.runtimeobj.document;

import java.util.Date;
import java.util.HashMap;

import kz.lof.appenv.AppEnv;

public class DocumentUtils {
	public static String getValFromFormAsString(String fieldName, HashMap<String, String[]> fields){
		String value = "";
		try{
			value = ((String[])fields.get(fieldName)).toString();
		}catch(Exception e){
			AppEnv.logger.errorLogEntry("error 567: " + fieldName );
		}
		return value;
	}

	public static int getValFromFormAsNumber(String fieldName, HashMap<String, String[]> fields){
		int value = 0;
		try{
			value = Integer.parseInt(((String[])fields.get(fieldName))[0]);
		}catch(Exception e){
			AppEnv.logger.errorLogEntry("error 568: " + fieldName);
		}
		return value;
	}

	public static Date getValFromFormAsDate(String fieldName, HashMap<String, String[]> fields){
		Date value = new Date();
		try{
			value = new Date(((String[])fields.get(fieldName))[0]);
		}catch(Exception e){
			AppEnv.logger.errorLogEntry("error 569: " + fieldName);
		}
		return value;
	}
}
