package kz.flabs.runtimeobj.caching;

import java.io.IOException;
import java.util.Map;

import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.page.Page;
import kz.lof.webserver.servlet.PageOutcome;
import net.sf.saxon.s9api.SaxonApiException;

public interface ICache {
	StringBuffer getPage(Page page, Map<String, String[]> formData) throws ClassNotFoundException, RuleException, QueryFormulaParserException,
	        DocumentException, DocumentAccessException, QueryException;

	PageOutcome getCachedPage(Page page, Map<String, String[]> formData) throws ClassNotFoundException, RuleException, IOException, SaxonApiException;

	void flush();
}
