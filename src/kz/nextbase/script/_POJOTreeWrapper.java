package kz.nextbase.script;

import java.util.List;

import kz.lof.scripting.IPOJOObject;

public class _POJOTreeWrapper implements _IXMLContent {
	private IPOJOObject object;
	private List<IPOJOObject> list;

	public _POJOTreeWrapper(IPOJOObject parent, List<IPOJOObject> list) {
		this.object = parent;
		this.list = list;
	}

	@Override
	public String toXML() throws _Exception {
		String result = "";
		result += "<entry isread=\"1\" hasattach=\"0\" hasresponse=\"0\" id=\"" + object.getId() + "\" "
		        + "url=\"Provider?id=furniture_form&amp;docid=" + object.getId() + "\"><viewcontent>";
		result += object.getFullXMLChunk(null) + "</viewcontent></entry>";
		result += "<responses>";
		for (IPOJOObject val : list) {
			result += val.getFullXMLChunk(null);
		}
		result += "</responses>";
		return result;
	}

	@Override
	public String toJSON() {
		// TODO Auto-generated method stub
		return null;
	}
}
