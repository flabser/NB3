package kz.flabs.dataengine;

import java.util.ArrayList;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.users.User;
import kz.pchelka.events.EventType;

public interface IQueue {
	public User postEvent(EventType event, int docID, int docType);
	public ArrayList<Document> getDocuments(EventType event);

}
