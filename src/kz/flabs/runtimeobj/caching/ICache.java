package kz.flabs.runtimeobj.caching;

import java.util.Map;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.page.Page;

public interface ICache {
	StringBuffer getPage(Page page, Map<String, String[]> formData) throws ClassNotFoundException, RuleException, QueryFormulaParserException, DocumentException, DocumentAccessException, QueryException;
	void flush();
}
