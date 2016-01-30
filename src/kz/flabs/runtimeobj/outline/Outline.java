package kz.flabs.runtimeobj.outline;

import java.util.ArrayList;
import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IFilters;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.RuleException;
import kz.flabs.localization.Vocabulary;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.Document;
import kz.flabs.runtimeobj.document.structure.Employer;
import kz.flabs.sourcesupplier.SourceSupplier;
import kz.flabs.users.User;
import kz.flabs.users.UserSession;
import kz.flabs.webrule.RuleUser;
import kz.flabs.webrule.form.DefaultFieldRule;
import kz.flabs.webrule.form.IShowField;
import kz.flabs.webrule.outline.OutlineEntryRule;
import kz.flabs.webrule.outline.OutlineRule;
import kz.pchelka.server.Server;

public class Outline implements IOutline, Const {

	protected int page;
	protected AppEnv env;
//	protected UserSession userSession;
	protected OutlineRule outlineRule;
	protected IDatabase db;
	private StringBuffer XMLTextEntry = new StringBuffer(1000);

	private String XMLText = "";
	protected static final String prefix = Server.serverTitle + " - ";
	protected String type;
	protected String id;
	private String command;
	private User user;

	public Outline(AppEnv env, OutlineRule outlineRule, User user) {
		this.env = env;
		this.outlineRule = outlineRule;
		this.db = env.getDataBase();
		this.user = user;
	}
	
	/**@deprecated**/
	public Outline(AppEnv env, OutlineRule outlineRule, String type, String id,	int page, UserSession userSession, String element) {
		this.env = env;
		this.type = type;
		this.id = id;
		this.page = page;
		//this.userSession = userSession;
		user = userSession.currentUser;
		this.db = env.getDataBase();
		this.outlineRule = outlineRule;
	}

	/**@deprecated**/
	public Outline(AppEnv env, OutlineRule outlineRule, String type, String id,	int page, String command, UserSession userSession) {
		this.env = env;
		this.type = type;
		this.id = id;
		this.page = page;
		this.command = command;
		//this.userSession = userSession;
		user = userSession.currentUser;
		this.db = env.getDataBase();
		this.outlineRule = outlineRule;
	}

	/**@deprecated**/
	public String getOutlineAsXML(String lang) throws DocumentException {
		XMLText += "<currentview type=\"" + type + "\" id=\"" + id + "\" page=\"" + page + "\" command=\"" + command + "\">"
				+ getTitle(type) + "</currentview>";
		XMLText += "<outline>";
		IFilters filters = db.getFilters();
		String filtersXML = filters.getFiltersByUser(user.getAllUserGroups(), user.getUserID()).toString();
		if (!"".equalsIgnoreCase(filtersXML)) {
			XMLText += "<entry id=\"filters\" caption=\"*\" hint=\"*\">";
			XMLText += filtersXML;
			XMLText += "</entry>";
		}
		XMLText += getNavigationPanel(outlineRule.getOutlineRootEntry(), lang,
				env.vocabulary, "Provider?type=outline&id=outline&subtype="
						+ type + "&subid=" + id);
		
		XMLText += getSetOfFieldsAsXML(lang);
		XMLText += "</outline>";
		XMLText += getCurrentUserProperty(lang);
		return XMLText;
	}

	public String getAsXML(String lang) throws DocumentException {	
		XMLText += "<outline>";
	//	XMLText += "<currentview type=\"" + type + "\" id=\"" + id + "\" page=\"" + page + "\" command=\"" + command + "\">"
	//			+ getTitle(type) + "</currentview>";
		IFilters filters = db.getFilters();
		StringBuffer filtersData = filters.getFiltersByUser(user.getAllUserGroups(), user.getUserID());
		
	/*	if(filtersData.length() > 0){
			XMLText += "<entry id=\"filters\">";		
			XMLText += filtersData;
			XMLText += "</entry>";
		}*/
		
		XMLText += getNavigationPanel(outlineRule.getOutlineRootEntry(), lang,
				env.vocabulary, "Provider?type=outline&id=outline&subtype="
						+ type + "&subid=" + id);
		
		XMLText += getSetOfFieldsAsXML(lang);
		XMLText += "</outline>";
		XMLText += getCurrentUserProperty(lang);
		return XMLText;
	}
	
	protected String getCurrentUserProperty(String lang) {
		String result = "";
		Employer emp = user.getAppUser();
		if (emp != null) {
			result = "<currentuser>" + emp.getFullName() + "</currentuser>"
					+ "<currentlang>" + lang + "</currentlang>";
		}
		return result;
	}

	public void addCommand(String addCommand) {
		command += ":" + addCommand;
	}

	protected String getSetOfFieldsAsXML(String lang) {
		try {
			StringBuffer xmlText = new StringBuffer(1000);
			String fieldList = "";
			SourceSupplier captionTextSupplier = new SourceSupplier(env, lang);
			xmlText.append("<fields>");

			for (DefaultFieldRule sf : outlineRule.defaultFieldsMap.values()) {
				String attrVal = "";
				String caption = "";

				if (sf.hasCaptionValue) {
					caption = captionTextSupplier.getValueAsCaption(
							sf.captionValueSource, sf.captionValue)
							.toAttrValue();
				}

				ArrayList<String[]> vals = getValToShow(sf);
				if (vals.size() > 1) {
					for (String val[] : vals) {
						if (val[1] != null && (!val[1].equals(""))) {
							attrVal = " attrval=\"" + val[1] + "\" ";
						}
						fieldList += "<entry " + caption + attrVal + ">"
								+ val[0] + "</entry>";
					}
					xmlText.append("<" + sf.name + caption
							+ " islist=\"true\"\">" + fieldList + "</"
							+ sf.name + ">");
				} else {
					if (vals.size() == 1) {
						String val[] = vals.get(0);
						if (val[1] != null && (!val[1].equals(""))) {
							attrVal = " attrval=\"" + val[1] + "\" ";
						}
						xmlText.append("<" + sf.name + caption + attrVal + ">"
								+ val[0] + "</" + sf.name + ">");
					} else {
						xmlText.append("<" + sf.name + caption + "></"
								+ sf.name + ">");
					}
				}
			}

			return xmlText.append("</fields>").toString();

		} catch (Exception e) {
			AppEnv.logger.errorLogEntry(e);
			return "";
		}
	}

	public String getNavigationPanel(OutlineEntryRule entryRule, String lang, Vocabulary vocabulary, String currentPlace) {
		SourceSupplier captionTextSupplier = new SourceSupplier(env,	lang);

		for (int k = 0; k < entryRule.entry.size(); k++) {
			try {
				OutlineEntryRule entry = entryRule.entry.get(k);

				if (isGranted(entry, null, user, env)) {
					String id = getValueAsIdAttr(entry.id);
					String typeAttr = "";
					String urlAttr = "";
					String caption = "";
					String url = currentPlace;

					if (entry.URL.length() > 0) {
						if (entry.URL.split("&page")[0].equals(url
								.split("&page")[0])) {
							urlAttr = "url=\""
									+ entry.URL.replace("&", "&amp;")
									+ "\" current=\"1\"";
						} else {
							urlAttr = "url=\""
									+ entry.URL.replace("&", "&amp;") + "\"";
						}
					}

					if (entry.hasCaptionValue) {
						caption = captionTextSupplier.getValueAsCaption(entry.captionValueSource, entry.captionValue).toAttrValue();
					}

					
						XMLTextEntry.append("<entry " + typeAttr + " " + urlAttr + id + caption + ">");
					//}

					if (entry.entry.size() > 0) {
						getNavigationPanel(entry, lang, vocabulary,	currentPlace);
					}

					XMLTextEntry.append("</entry>");
				}
			} catch (Exception e) {
				AppEnv.logger.errorLogEntry(e);
				return "";
			}
		}

		return XMLTextEntry.toString();
	}

	public String toString(){
		return outlineRule.id;
	}
	
	protected boolean isGranted(OutlineEntryRule entry, BaseDocument doc, User user, AppEnv env) throws DocumentException,DocumentAccessException, RuleException,QueryFormulaParserException, ComplexObjectException {
		SourceSupplier ss = new SourceSupplier((Document) doc, user, env);
		ArrayList<RuleUser> g = entry.granted;
		if (g.size() > 0) {
			for (RuleUser grant : entry.granted) {
				String value = ss.getValueAsStr(grant.valueSource, grant.value, grant.compiledClass, grant.macro)[0];
				//String value = ss.getValueAsString(grant.valueSource, grant.value, grant.macro)[0];
				if (user.getUserID().equalsIgnoreCase(value)) {
					return true;
				}
			}
		} else {
			return true;
		}
		return false;
	}

	/**@deprecated**/
	protected String getTitle(String type) {
		if (type.equalsIgnoreCase("view")) {
			return prefix + "view";
		} else if (type.equalsIgnoreCase("edit")) {
			return prefix + "edit";
		} else if (type.equalsIgnoreCase("get_logs_list")) {
			return prefix + "Server logs";
		} else if (type.equalsIgnoreCase("get_users_list")) {
			return prefix + "Users list";
		} else if (type.equalsIgnoreCase("get_queries_list")) {
			return prefix + "Queries";
		} else if (type.equalsIgnoreCase("get_handlers_list")) {
			return prefix + "Handlers";
		} else if (type.equalsIgnoreCase("get_maindocs_list")) {
			return prefix + "Main documents";
		} else {
			return prefix;
		}
	}

	private ArrayList<String[]> getValToShow(IShowField sf)
			throws RuleException {
		ArrayList<String[]> vals = new ArrayList<String[]>();
		String[] result = new String[2];
		switch (sf.getSourceType()) {		
		case STATIC:
			result[0] = sf.getValue();
			String val = sf.getAttrValue();
			if (!val.equals("")) {
				result[1] = sf.getAttrValue();
			}
			vals.add(result);
			break;
		case SCRIPT:

			break;
		case QUERY:

			break;
		case MACRO:

			break;
		}

		switch (sf.getPublishAs()) {
		

		}
		return vals;
	}

	private String getValueAsIdAttr(String value) throws DocumentException {
		if (value != null && (!value.equals(""))) {
			return " id=\"" + value + "\" ";
		} else {
			return "";
		}
	}
}
