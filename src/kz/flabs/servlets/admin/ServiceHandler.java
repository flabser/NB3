package kz.flabs.servlets.admin;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseFactory;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.h2.holiday.Holiday;
import kz.flabs.dataengine.h2.holiday.HolidayCollection;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.BlobField;
import kz.flabs.runtimeobj.document.BlobFile;
import kz.flabs.runtimeobj.document.Field;
import kz.flabs.users.Reader;
import kz.flabs.util.Util;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.Lang;
import kz.flabs.webrule.Role;
import kz.flabs.webrule.Skin;
import kz.flabs.webrule.module.ExternalModule;
import kz.flabs.webrule.module.ExternalModuleType;
import kz.pchelka.env.Environment;
import kz.pchelka.log.LogFiles;
import kz.pchelka.server.Server;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

public class ServiceHandler implements Const {

	private static int pageSize = 20;

	public ServiceHandler(String db){

	} 

	public ServiceHandler(){

	} 

	String getCfg()  {		
		String xmlFragment = "";
		String viewText = "";
		xmlFragment += "<version>" + Server.serverVersion + "</version>";		
		xmlFragment += "<host>" + Environment.hostName+ "</host>";
		xmlFragment += "<port>" + Environment.httpPort+ "</port>";
		xmlFragment += "<sslenable>" + Environment.isSSLEnable + "</sslenable>";
		xmlFragment += "<keystore>" + Environment.keyStore+ "</keystore>";
		xmlFragment += "<keypwd>" + Environment.keyPwd + "</keypwd>";
		xmlFragment += "<tlsauth>" + Environment.isClientSSLAuthEnable+ "</tlsauth>";
		xmlFragment += "<truststore>" + Environment.trustStore+ "</truststore>";	
		xmlFragment += "<remoteconsole>" + Environment.remoteConsole + "</remoteconsole>";
		xmlFragment += "<rmiserver>" + Environment.remoteConsoleServer + "</rmiserver>";
		xmlFragment += "<rmiport>" + Environment.remoteConsolePort + "</rmiport>";
		xmlFragment += "<tmpdir>" + Environment.tmpDir + "</tmpdir>";
		xmlFragment += "<smtphost>" + Environment.SMTPHost + "</smtphost>";
		xmlFragment += "<defaultsender>" + Environment.defaultSender + "</defaultsender>";	
		xmlFragment += "<xmppserver>" + Environment.XMPPServer + "</xmppserver>";
		xmlFragment += "<xmppport>" + Environment.XMPPServerPort + "</xmppport>";
		xmlFragment += "<xmpplogin>" + Environment.XMPPLogin + "</xmpplogin>";
		xmlFragment += "<xmpppwd>" + Environment.XMPPPwd + "</xmpppwd>";		
		xmlFragment += "<verboselogging>" + Environment.verboseLogging + "</verboselogging>";	

		xmlFragment += "<applications>";
		for(AppEnv env: Environment.getApplications()){
			xmlFragment += "<entry>";
			xmlFragment += "<apptype>" + env.appType + "</apptype>";
			xmlFragment += "<orgname>" + env.globalSetting.orgName + "</orgname>";
			xmlFragment += "<liccount>" + env.globalSetting.licCount + "</liccount>";
			xmlFragment += "</entry>";
		}
		xmlFragment += "</applications>";


		xmlFragment = "<document doctype = \"system\"" +		
		" viewtext=\"" + viewText +"\" >" + xmlFragment + "</document>";
		return xmlFragment;			
	}

	String getSettings(AppEnv env)  {		
		StringBuffer xmlFragment = new StringBuffer(1000);
		String viewText = "";
		xmlFragment.append("<application>" + env.globalSetting.id + "</application>");		
		xmlFragment.append("<mode>" + env.globalSetting.isOn + "</mode>");	
		xmlFragment.append("<description>" + env.globalSetting.description + "</description>");		
		xmlFragment.append("<entrypoint>" + env.globalSetting.id + File.pathSeparator + env.globalSetting.entryPoint.replace("&","&amp;") + "</entrypoint>");
		xmlFragment.append("<defaultredirecturl>" + env.globalSetting.id + File.pathSeparator + env.globalSetting.defaultRedirectURL.replace("&","&amp;") + "</defaultredirecturl>");
		xmlFragment.append("<markdelaysec>" + env.globalSetting.markAsReadMsDelay/1000 + "</markdelaysec>");
		xmlFragment.append("<daemons>");
		xmlFragment.append("<tempfilecleaner><mininterval>5</mininterval></tempfilecleaner>");
		xmlFragment.append("</daemons>");
		
		xmlFragment.append("<availablelangs>");
		for(Lang lang: env.globalSetting.langsList){
			xmlFragment.append("<entry>" + lang.toXML() + "</entry>");		
		}
		xmlFragment.append("</availablelangs>");

		xmlFragment.append("<skins>");
		for(Skin skin: env.globalSetting.skinsList){
			xmlFragment.append("<entry>" + skin.toXML() + "</entry>");		
		}
		xmlFragment.append("</skins>");

		xmlFragment.append("<roles>");
		for(Role role: env.globalSetting.roleCollection.getRolesMap().values()){
			xmlFragment.append("<entry>" + role.toXML() + "</entry>");		
		}
		xmlFragment.append("</roles>");

		
		xmlFragment.append("<xslt>");
		for(Entry<String, File> entry: env.xsltFileMap.entrySet()){
			xmlFragment.append("<entry><key>" + entry.getKey() + "</key><file>" + entry.getValue().getPath() + "</file></entry>");		
		}
		xmlFragment.append("</xslt>");
		
	

		xmlFragment.append("<patches>");
		xmlFragment.append(env.getDataBase().getPatches());
		xmlFragment.append("</patches>");

		xmlFragment.append("<counters>");
		xmlFragment.append(env.getDataBase().getCounters());		
		xmlFragment.append("</counters>");

		
		xmlFragment.append("<database>");
		String dbAttr = "";
		if (env.globalSetting.databaseEnable){
			dbAttr = " dbid= \""+ env.getDataBase().getDbID() + "\" type=\"" + env.getDataBase().getRDBMSType() + "\" ";
			xmlFragment.append("<id>" + env.getDataBase().getDbID() + "</id>");
			xmlFragment.append("<version>" + env.getDataBase().getVersion() + "</version>");
			xmlFragment.append("<autodeploy>" + env.globalSetting.autoDeployEnable + "</autodeploy>");
			xmlFragment.append("<daemons>");
			xmlFragment.append("<cyclecontrol><mininterval>" + env.globalSetting.cycleContrSchedSetings.minInterval + "</mininterval></cyclecontrol>");
			xmlFragment.append("</daemons>");
			xmlFragment.append("<dbpool>");
			xmlFragment.append(env.getDataBase().getConnectionPool().toXML());
			xmlFragment.append("</dbpool>");
			for(ExternalModule module:env.globalSetting.extModuleMap.values()){
				if (module.getType() == ExternalModuleType.STRUCTURE){
					AppEnv extApp = Environment.getApplication(module.getName());
					xmlFragment.append("<structdburl>" +  extApp.globalSetting.dbURL + "</structdburl>");
				}
			}
		}else{
			dbAttr = " dbid= \"\" type=\"" + env.getDataBase().getRDBMSType() + "\" ";
			xmlFragment.append("<id>" + env.getDataBase().getDbID() + "</id>");
			xmlFragment.append("<version></version>");
			xmlFragment.append("<autodeploy></autodeploy>");	
			for(ExternalModule module:env.globalSetting.extModuleMap.values()){
				if (module.getType() == ExternalModuleType.STRUCTURE){
					AppEnv extApp = Environment.getApplication(module.getName());
					xmlFragment.append("<structdburl>" +  extApp.globalSetting.dbURL + "</structdburl>");
				}
			}
		}
		xmlFragment.append("</database>");
		
		return  xmlFragment.toString();			
	}

	String getMainDoc(String dbid, String docid) throws DocumentException, DocumentAccessException, ComplexObjectException  {		
		String xmlFragment = "";
		IDatabase db = DatabaseFactory.getDatabaseByName(dbid);
		BaseDocument doc = db.getDocumentByDdbID(docid, Const.supervisorGroupAsSet, Const.sysUser);	
	
		xmlFragment += "<docid>" + doc.getDdbID() + "</docid>" +
			"<author>" + doc.getAuthorID() + "</author>" +
			"<regdate>" + Util.convertDataTimeToString(doc.getRegDate()) + "</regdate>" +
			"<lastupdate>" + Util.convertDataTimeToString(doc.getLastUpdate()) + "</lastupdate>" + 
			"<form>" + doc.form + "</form>" +
			"<parentdocid>" + doc.getParentDocumentID() + "</parentdocid>";
	
		xmlFragment += "<customfields>";
		for (Field field : doc.fields()) {
			xmlFragment += "<" + field.name + RuntimeObjUtil.getTypeAttribute(field.getTypeAsDatabaseType()) + ">" + field.valueAsText + "</"
			+ field.name + ">";
		}			
		for (BlobField field : doc.blobFieldsMap.values()) {
			xmlFragment += "<" + field.name + " type=\"files\">";
			for(BlobFile file: field.getFiles()){
				xmlFragment += "<entry>" + file.originalName + "</entry>";
			}
			xmlFragment += "</" + field.name + ">";

		}	
		xmlFragment += "</customfields>";
		
		xmlFragment += "<eds>";
		xmlFragment += "<sign>" + doc.getSign() + "</sign>";
		xmlFragment += "<srctext>" + doc.getSignedFields() + "</srctext>";
		xmlFragment += "</eds>";
		
		xmlFragment += "<viewtexts>";	
		
		for (String field : doc.getViewTextList()) {
			xmlFragment += "<viewtext>" + XMLUtil.getAsTagValue(field) + "</viewtext>";
		}
		
		for (BigDecimal field : doc.getViewNumberList()) {
			xmlFragment += "<viewnumber>" + field + "</viewnumber>";
		}
		
		for (Date field : doc.getViewDateList()) {
			xmlFragment += "<viewdate>" + field + "</viewdate>";
		}		
		
		xmlFragment += "</viewtexts>";

		xmlFragment += "<readers>";
		for(Reader r: doc.getReaders()){
			xmlFragment += "<user>" + r + "</user>"; 
		}
		xmlFragment += "</readers>";

		xmlFragment += "<editors>";
		for(String a: doc.getEditors()){
			xmlFragment += "<user>" + a + "</user>"; 
		}
		xmlFragment += "</editors>";

/*		xmlFragment = "<document doctype=\"" + doc.docType + "\" docid=\""+ doc.getDdbID() + "\" form= \""+ doc.form + "\" dbid= \""+ dbid + "\" " +
		"parentdocid = \"" + doc.getParentDocumentID() + "\" " +
		"author=\"" + doc.getAuthorID() +"\" " + 
		"regdate=\"" + Util.convertDataTimeToString(doc.getRegDate()) +"\" " + 
		"lastupdate=\"" + Util.convertDataTimeToString(doc.getLastUpdate()) +"\">" + xmlFragment + "</document>";*/
		return xmlFragment;			
	}

	public StringBuffer getAccessList(IDatabase db, String docID) throws DocumentException, DocumentAccessException, ComplexObjectException{		
		StringBuffer xmlFragment = new StringBuffer(1000);
		BaseDocument doc = db.getDocumentByDdbID(docID, Const.supervisorGroupAsSet, Const.sysUser);
		
		xmlFragment.append("<readers>");
		HashSet<Reader> readers = doc.getReaders();
		for(Reader entry: readers){
			xmlFragment.append("<entry>" + entry.getUserID() + "</entry>");
		}
		xmlFragment.append("</readers>");
		
		xmlFragment.append("<editors>");
		HashSet<String> editors = doc.getEditors();
		for(String entry: editors){
			xmlFragment.append("<entry>" + entry + "</entry>");
		}
		xmlFragment.append("</editors>");
		
		return xmlFragment;			
	}

	String getLogsListWrapper(ArrayList<File> fl, int pageNum) {
		String fieldsAsXML = "";
		int length = fl.size();
		/*int pageNumMinusOne = pageNum;
		pageNumMinusOne--;
		int startEntry = pageNumMinusOne * pageSize;
		int endEntry = startEntry + pageSize;
		int num = 1;*/

		Iterator<File> it = fl.iterator();
		while (it.hasNext()) {
			File logFile = it.next();
			fieldsAsXML += "<entry><name>" + logFile.getName() + "</name>"
			+ "<length>" + logFile.length() + "</length>"
			+ "<lastmodified>" + logFile.lastModified()
			+ "</lastmodified>" + "</entry>";
		}

		int maxPage = length / pageSize;
		if (maxPage < 1)maxPage = 1;
		
		LogFiles logs = new LogFiles();
		return "<query count=\"" + fl.size() + "\" currentpage=\"" + pageNum
		+ "\" maxpage=\"" + maxPage + "\" path=\"" + logs.logDir
		+ "\">" + fieldsAsXML + "</query>";
	}

	public String getLogsListWrapper(LogFiles logs) {
		String fieldsAsXML = "";
		ArrayList<File> fl = logs.getLogFileList();

		Iterator<File> it = fl.iterator();
		while (it.hasNext()) {
			File logFile = it.next();
			fieldsAsXML += "<entry><name>" + logFile.getName() + "</name>"
			+ "<length>" + logFile.length() + "</length>"
			+ "<lastmodified>" + logFile.lastModified()
			+ "</lastmodified>" + "</entry>";
		}
		
		return  fieldsAsXML;
	}

	
	String getBackupListWrapper(ArrayList<File> fl) {
		String fieldsAsXML = "";
		int length = fl.size();		

		Iterator<File> it = fl.iterator();
		while (it.hasNext()) {
			File logFile = it.next();
            String fileName = logFile.getName();
			fieldsAsXML += "<entry><name>" + fileName + "</name>"
			+ "<length>" + Util.convertBytesToKilobytes(logFile.length()) + "</length>"
            + (fileName.split("_").length > 2 ? "<app>" + fileName.substring(fileName.indexOf("_") + 1, fileName.lastIndexOf("_")) + "</app>": "")
			+ "<lastmodified>" + Util.dateTimeFormat.format(logFile.lastModified())
			+ "</lastmodified>" + "</entry>";
		}

		int maxPage = length / pageSize;
		if (maxPage < 1)maxPage = 1;
		
		return fieldsAsXML;
	}

	public String getCalendarListWrapper(HolidayCollection holidays, int i, int j) {
		String fieldsAsXML = "";
		for(Holiday h:holidays.holidays){
			fieldsAsXML += "<entry><title>" + h.getTitle()	+ "</title></entry>" +
					"<startdate>" + Util.convertDataToString(h.getStartDate()) + "</startdate>" +
					"<enddate>" + Util.convertDataToString(h.getEndDate()) + "</enddate>" +
					"<country>" + h.getCountry()	+ "</country></entry>";
		}


		return  fieldsAsXML;
	}

	
	
	
	

}
