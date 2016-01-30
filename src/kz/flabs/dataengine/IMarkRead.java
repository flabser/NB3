package kz.flabs.dataengine;


public interface IMarkRead {	
	int isRead(int docID, int docType, String userName);
	boolean markDocument(int docID, int docType, String userName);
	boolean unmarkDocument(int docID, int docType, String userName);
}
