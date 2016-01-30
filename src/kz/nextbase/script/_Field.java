package kz.nextbase.script;


public class _Field  implements _IXMLContent {
	_Document doc;



	String name;
	
	protected _Field(_Document doc, String name){
		this.doc = doc;
		this.name = name;		
	}

	public _Field() {
	}

	@Override
	public String toXML() throws _Exception {		
		return doc.getValueString(name);
	}

    @Override
    public String toString() {
        return doc.getValueString(name);
    }

}
