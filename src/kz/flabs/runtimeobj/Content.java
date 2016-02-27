package kz.flabs.runtimeobj;

import java.util.ArrayList;

import kz.flabs.exception.DocumentException;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.webrule.Caption;
import kz.flabs.webrule.Rule;
import kz.lof.appenv.AppEnv;

public class Content {
	private AppEnv env;
	private Rule rule;

	public Content(AppEnv env, Rule rule) {
		this.env = env;
		this.rule = rule;

	}

	public String getCaptions(SourceSupplier captionTextSupplier, ArrayList<Caption> captions) throws DocumentException {
		StringBuffer captionsText = new StringBuffer("<captions>");
		for (Caption cap : captions) {
			captionsText.append("<" + cap.captionID + captionTextSupplier.getValueAsCaption(cap.source, cap.value).toAttrValue() + "></"
			        + cap.captionID + ">");
		}
		return captionsText.append("</captions>").toString();
	}

}
