package kz.flabs.runtimeobj.document;

import kz.flabs.dataengine.Const;


public class DocID implements Const, Comparable<DocID> {
	public int id;
	public int type;
	public DocID toExpand[];

	
	public DocID(int di, int dt){
		id = di;
		type = dt;
	}
	
	public DocID(String di, String dt){		
		try{
			id = Integer.parseInt(di);
		}catch(NumberFormatException nfe){
			id = 0;
		}
		try{
			type = Integer.parseInt(dt);
		}catch(NumberFormatException nfe){
			type = DOCTYPE_UNKNOWN;
		}		
	}
	
	public boolean equals(Object other) {
		if (this == other)
			return true;

		if ( !(other instanceof DocID) )
			return false;
		
		DocID docid = (DocID)other;
		if (docid.id == id && docid.type == type)
			return true;
		else
			return false;
	}

	 public int hashCode() {
		 return toString().hashCode();
	 }
	
	public String toString(){
		return id + "." + type;
	}

	@Override
	public int compareTo(DocID o) {
		float idToCompare1 = id + (type/100);
		float idToCompare2 = o.id + (o.type/100);
		int res = Float.compare(idToCompare1,idToCompare2);
		return res;
	}
	
}
