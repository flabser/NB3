package kz.flabs.servlets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.http.HttpServletResponse;
import net.sf.saxon.s9api.SaxonApiException;
import kz.flabs.exception.TransformatorException;


public interface IServletTransformator {
	public void toTrans(HttpServletResponse response, File xslFileObj, String xmlText) throws IOException, SaxonApiException, TransformatorException;
	public ArrayList<String> getFormWords(String stylesheet);
}
