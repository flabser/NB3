package kz.lof.scripting;

import java.util.UUID;

public class POJOObjectAdapter implements IPOJOObject {

	@Override
	public UUID getId() {
		return null;
	}

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

}
