package kz.flabs.runtimeobj.outline;

import kz.flabs.exception.DocumentException;

public interface IOutline {  
	public abstract String getOutlineAsXML(String lang) throws DocumentException;
}