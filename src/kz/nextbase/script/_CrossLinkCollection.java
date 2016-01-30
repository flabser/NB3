package kz.nextbase.script;

import kz.flabs.runtimeobj.CrossLink;
import kz.flabs.runtimeobj.CrossLinkCollection;

public class _CrossLinkCollection implements _IXMLContent {
	private CrossLinkCollection links;
	private _Session ses;
	
	public _CrossLinkCollection(_Session ses) {
		this.links = new CrossLinkCollection();
		this.ses = ses;
	}
		
	public _CrossLinkCollection(_Session ses, CrossLinkCollection o) {
		this.links = o;
		this.ses = ses;
	}
	
	public void add(String url, String viewText){
		_CrossLink l = new _CrossLink(ses, url, viewText);
		links.add(l.getBaseObject());
		
	}
	
	public void add(_CrossLink l){
		links.add(l.getBaseObject());
		
	}

	@Override
	public String toXML() throws _Exception {
		String result = "";
		for (CrossLink link: links.getLinkCollection()){
			result += new _CrossLink(ses, link).toXML();
		}
		return result;
	}

	public CrossLinkCollection getBaseObject() {
		return links;
	}
	
}
