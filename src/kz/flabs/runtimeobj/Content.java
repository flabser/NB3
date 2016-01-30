package kz.flabs.runtimeobj;

import java.util.ArrayList;

import kz.flabs.appenv.AppEnv;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.exception.RuleException;
import kz.flabs.localization.LocalizatorException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.users.User;
import kz.flabs.webrule.Caption;
import kz.flabs.webrule.Rule;
import kz.flabs.webrule.form.GlossaryRule;

public class Content {
	private AppEnv env;
	private Rule rule;
		
	public Content(AppEnv env, Rule rule){
		this.env = env;
		this.rule = rule;
		
	}

	public String getSpravFieldSet(User user, String lang) throws RuleException, DocumentException, DocumentAccessException, QueryFormulaParserException, QueryException, LocalizatorException{
		StringBuffer glossariesAsText = new StringBuffer("<glossaries>");		
		SourceSupplier ss = new SourceSupplier(user, env, lang);	
		for(GlossaryRule glos: rule.getGlossary()){
			glossariesAsText.append("<" + glos.name + ">" + ss.getDataAsXML(glos.valueSource, glos.value, glos.macro, lang) + "</" + glos.name + ">");
		}
		return glossariesAsText.append("</glossaries>").toString();
	}

	public String getCaptions(SourceSupplier captionTextSupplier, ArrayList<Caption> captions) throws DocumentException{
		StringBuffer captionsText = new StringBuffer("<captions>");
		for (Caption cap: captions){			
			captionsText.append("<" + cap.captionID + captionTextSupplier.getValueAsCaption(cap.source, cap.value).toAttrValue() + "></" + cap.captionID + ">");
		}
		return captionsText.append("</captions>").toString(); 
	}


	public String getAsXML(User user, String lang) throws RuleException, DocumentException, DocumentAccessException, QueryFormulaParserException, QueryException, LocalizatorException{	
		SourceSupplier captionTextSupplier = new SourceSupplier(env, lang);
		String captions = getCaptions(captionTextSupplier, rule.captions);
		String glossarySet = getSpravFieldSet(user, lang);
		return "<content>" + rule.getAsXML() + glossarySet + captions + "</content>";
	}

}
