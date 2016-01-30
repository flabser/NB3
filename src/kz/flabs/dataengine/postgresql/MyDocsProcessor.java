package kz.flabs.dataengine.postgresql;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IMyDocsProcessor;

public class MyDocsProcessor extends kz.flabs.dataengine.h2.MyDocsProcessor implements IMyDocsProcessor, Const {
	
	public MyDocsProcessor(IDatabase db) {	
		super(db);	
	}

}
