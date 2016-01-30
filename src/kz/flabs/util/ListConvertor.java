package kz.flabs.util;

import java.util.*;

public class ListConvertor {
	
	public static String listToString(Collection list){
		String result = "";
		Iterator it = list.iterator();
		while (it.hasNext()){
			result += it.next();
			if (it.hasNext()) {
				result += "#";
			}
		}
		return result;
	}



	public static Collection<String> stringToList(String listAsString){
		Collection<String> list = new ArrayList<String>();
		String[] arrayList = listAsString.split("#");
		for (int i = 0; i < arrayList.length; i++) {
			if (arrayList[i].length() == 0) {
				continue;
			}
			list.add(arrayList[i]);
		}
		return list;
	}
}
