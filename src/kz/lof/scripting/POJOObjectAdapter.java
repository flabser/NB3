package kz.lof.scripting;

public class POJOObjectAdapter<UUID> implements IPOJOObject {

	@Override
	public String getURL() {
		return "Provider";
	}

	@Override
	public String getFullXMLChunk(_Session ses) {
		return "<object>null</object>";
	}

	@Override
	public String getShortXMLChunk(_Session ses) {
		return getFullXMLChunk(ses);
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public Object getJSONObj(_Session ses) {
		return this;
	}

	@Override
	public String getIdentifier() {
		return "null";
	}

}
