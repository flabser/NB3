package kz.flabs.servlets;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import kz.flabs.exception.TransformatorException;
import net.sf.saxon.s9api.SaxonApiException;

public interface IServletTransformator {
	public void toTrans(HttpServletResponse response, File xslFileObj, String xmlText) throws IOException, SaxonApiException, TransformatorException;
}
