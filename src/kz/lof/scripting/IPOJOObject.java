package kz.lof.scripting;

import java.util.UUID;

public interface IPOJOObject {
	UUID getId();

	String getURL();

	String getFullXMLChunk(_Session ses);

	String getShortXMLChunk(_Session ses);

	public boolean isEditable();

}
