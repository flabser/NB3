package kz.flabs.servlets;

import javax.xml.transform.*; 

public class TransformatorErrorListener implements ErrorListener{

	public void warning(TransformerException e) throws TransformerException { 

		System.err.println("Error message: " + e.getMessageAndLocation()); 
	} 

	public void error(TransformerException e) throws TransformerException {

		System.err.println("Error message: " + e.getMessageAndLocation()); 
		throw e; 
	} 
	public void fatalError(TransformerException e) throws TransformerException { 

		System.err.println("Error message: " + e.getMessageAndLocation()); 
		throw e; 
	} 

}
