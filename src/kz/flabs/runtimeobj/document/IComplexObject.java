package kz.flabs.runtimeobj.document;

import kz.flabs.dataengine.IDatabase;
import kz.flabs.exception.ComplexObjectException;

public interface IComplexObject {
	
	void init(IDatabase db, String initString) throws ComplexObjectException;
	String getContent();
	String getPersistentValue();
	
}
