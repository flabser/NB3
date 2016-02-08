package kz.flabs.runtimeobj.caching;

import java.io.IOException;

import kz.flabs.exception.RuleException;
import kz.flabs.runtimeobj.page.Page;
import kz.lof.webserver.servlet.PageOutcome;
import kz.nextbase.script._WebFormData;
import net.sf.saxon.s9api.SaxonApiException;

public interface ICache {

	PageOutcome getCachedPage(Page page, _WebFormData formData) throws ClassNotFoundException, RuleException, IOException, SaxonApiException;

	void flush();
}
