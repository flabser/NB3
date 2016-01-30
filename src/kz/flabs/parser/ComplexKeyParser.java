package kz.flabs.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import kz.flabs.runtimeobj.document.DocID;

public class ComplexKeyParser {

	public static List<DocID> parse(String ck) {
		if (ck == null) {
			throw new IllegalArgumentException("Complex key must not be null");
		}
		List<DocID> complexKeys = new ArrayList<DocID>();
		String[] keys = ck.split("`");
		String docID = "";
		String docType = "";
		for (String key : keys) {
			StringTokenizer token = new StringTokenizer(key, "~");
			while(token.hasMoreTokens()) {
				docType = token.nextToken();
				docID = token.nextToken();
			}
			DocID id = new DocID(docID, docType);
			complexKeys.add(id);
		}		
		return complexKeys;
	}
	
	
}
