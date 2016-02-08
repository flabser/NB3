package kz.nextbase.script;

import kz.flabs.runtimeobj.page.Page;

public class _Page implements _IXMLContent {
	private Page baseElement;
	private _WebFormData webFormData;

	public _Page(Page page, _WebFormData webFormData) {
		baseElement = page;
		this.webFormData = webFormData;
	}

	@Override
	public String toXML() throws _Exception {
		try {
			// return baseElement.process(webFormData.getFormData(),
			// "GET").toString();
		} catch (Exception e) {
			throw new _Exception(_ExceptionType.SCRIPT_ENGINE_ERROR, e.getMessage() + " function: _Page.toXML()");
		}
		return null;
	}

	@Override
	public String toJSON() {
		// TODO Auto-generated method stub
		return null;
	}

}
