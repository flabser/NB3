package kz.lof.scripting;


public interface IPOJOObject {
	String getIdentifier();

	String getURL();

	String getFullXMLChunk(_Session ses);

	String getShortXMLChunk(_Session ses);

	Object getJSONObj(_Session ses);

	public boolean isEditable();

}
