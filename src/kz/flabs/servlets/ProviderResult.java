package kz.flabs.servlets;

import kz.flabs.servlets.pojo.Outcome;

import org.apache.http.HttpStatus;

public class ProviderResult {
	public StringBuffer output = new StringBuffer(10000);
	public PublishAsType publishAs = PublishAsType.XML;
	public String forwardTo;
	public String xslt;
	@Deprecated
	public String title;
	public boolean disableClientCache;
	public String filePath;
	public String originalAttachName;
	@Deprecated
	public boolean addHistory;
	public int httpStatus = HttpStatus.SC_OK;
	public Outcome jsonOutput;

	ProviderResult(PublishAsType publishAs, String xslt) {
		this.publishAs = publishAs;
		this.xslt = xslt;
	}

	public ProviderResult() {

	}

}