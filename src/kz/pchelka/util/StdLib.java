package kz.pchelka.util;

import java.util.*;

public class StdLib {

	public static Vector<String> replaceVector(Vector<String> v, String r){
		Vector<String> newv = new Vector<String>();
		Iterator<String> it = v.iterator();
		while (it.hasNext()){
			String or = (String)it.next();
			if (!or.equals(r)){
				newv.add(or);
			}
		}
		return newv;
	}
	public static Vector<String> getUniqStrVector(Vector<String> v){
		Vector<String> newv = new Vector<String>();

		Iterator<String> it = v.iterator();
		while (it.hasNext()){
			String or = (String)it.next();
			if (!newv.contains(or)){
				newv.add(or);
			}
		}
		return newv;
	}

	public static boolean isUniqueThread(String threadName){
		Map <Thread, StackTraceElement[]> st = Thread.getAllStackTraces();		
		for (Map.Entry <Thread, StackTraceElement[]> e: st.entrySet()) {						
			Thread t= e.getKey();
			if (t.getName().equals(threadName)){
				return false;
			}
		}
		return true;
	}

}
