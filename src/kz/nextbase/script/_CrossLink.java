package kz.nextbase.script;

import kz.flabs.runtimeobj.CrossLink;

public class _CrossLink implements _IXMLContent {
	private CrossLink link;
	private _Session ses;

	_CrossLink(_Session ses, CrossLink l){
		this.ses = ses;
		link = l;
	}
	
	_CrossLink(_Session ses, _Document doc){
		this.ses = ses;
		link = new CrossLink();
		link.setURL(doc.getURL());
		link.setViewText(doc.getViewText());
	}
	
	_CrossLink(_Session ses, _URL url, String viewText){
		this.ses = ses;
		link = new CrossLink();
		link.setURL(url.toString());
		link.setViewText(viewText);
	}
	
	_CrossLink(_Session ses, String url, String viewText){
		this.ses = ses;
		link = new CrossLink();
		link.setURL(url);
		link.setViewText(viewText);
	}
	
	public _URL getURL() {
		return new _URL(link.getURL());
	}

	public void setURL(_URL uRL) {
		link.setURL(uRL.toString());
	}

	public void setURL(String uRL) {
		link.setURL(uRL);
	}
	
	public String getViewText() {
		return link.getViewText();
	}

	public void setViewText(String viewText) {
		link.setViewText(viewText);
	}
	
	@Override
	public String toXML() throws _Exception {
		return "<entry url=\"" + getURL().toString().replace("&", "&amp;") + "\">" + getViewText().replaceAll("&", "&amp;") + "</entry>";
	}

	public CrossLink getBaseObject() {
		return link;
	}
	
}
