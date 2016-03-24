package kz.lof.webserver.servlet;

import kz.lof.scripting.POJOObjectAdapter;
import kz.lof.scripting._Session;

public class UploadedFile extends POJOObjectAdapter {

	private String name;

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getURL() {
		return "p?id=update-file&amp;fileid=" + name;
	}

	@Override
	public String getShortXMLChunk(_Session ses) {
		StringBuilder chunk = new StringBuilder(1000);
		chunk.append("<name>" + name + "</name>");
		return chunk.toString();
	}
}
