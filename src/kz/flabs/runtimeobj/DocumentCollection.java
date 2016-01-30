package kz.flabs.runtimeobj;

import kz.flabs.runtimeobj.document.BaseDocument;

import java.io.Serializable;
import java.util.ArrayList;

public class DocumentCollection implements Serializable {

    private static final long serialVersionUID = 1L;

	public StringBuffer xmlContent = new StringBuffer(10000);
	public ArrayList<BaseDocument> col = new ArrayList<BaseDocument>();
	public String xmlContentl;
	public int count;
	public int level;
	
}
