package kz.flabs.parser;

import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.DatabaseUtil;
import kz.flabs.runtimeobj.Filter;
import kz.lof.appenv.AppEnv;
import kz.lof.server.Server;

public class FilterParser {

	public static HashSet<Filter> parse(Map<String, String[]> parameters, AppEnv env) {
		HashSet<Filter> filters = new HashSet<Filter>();
		try{
			Map<String, String> pars = new TreeMap<String, String>();
			for (String key : parameters.keySet()) {
				if (key.contains("filter")) {
					pars.put(key.replaceAll("filter", "_"), parameters.get(key)[0]);
				}
			}
			String curUser = parameters.get("userid")[0];
			int numOfFilter = 0;
			int tempNum = 0;
			Filter filter = new Filter(env);
			for (String key : pars.keySet()) {
				String num = key.substring(key.indexOf("_")+1, key.lastIndexOf("_"));
				try {
					numOfFilter = Integer.parseInt(num);
				} catch (NumberFormatException e) {
					Server.logger.errorLogEntry(e);
				}
				if (numOfFilter != tempNum) {
					filter = new Filter(env);
					filter.setUserID(curUser);
					filters.add(filter);
					tempNum = numOfFilter;
				}
				filter.addCondition(key.substring(key.lastIndexOf("_")+1), pars.get(key));				
			}
		}catch(Exception e){
			AppEnv.logger.errorLogEntry(e);
		}

		return filters;
	}

	public static Filter quickFilterParse(Map<String, String[]> fields, AppEnv env) {
		Filter filter = new Filter(env);
		filter.setName("quick");
		filter.setUserID(Const.sysUser);
		filter.setEnable(1);
		filter.setFilterID(0);
		if (fields != null) {
			String[] project = fields.get("filterproj");
			String[] category = fields.get("filtercat");
			String[] originPlace = fields.get("filterorigin");
			String[] id = fields.get("id");
			String[] author = fields.get("filterauthor");
			String[] responsible = fields.get("filterresp");
			String[] status = fields.get("filterstatus");
			String projectID = "";
			String categoryID = "";
			String originPlaceID = "";
			String authorID = "";
			String responsibleID = "";
			String statusID = "";
			if (id != null && !"".equalsIgnoreCase(id[0])) {
				filter.setName(id[0]);
			}

			if (project != null && project.length > 0 && project[0] != null) {
				projectID = project[0];
			}
			if (category != null && category.length > 0 && category[0] != null) {
				categoryID = category[0];
			}
			if (originPlace != null && originPlace.length > 0 && originPlace[0] != null) {
				originPlaceID = originPlace[0];
			}
			if (author != null && author.length > 0 && author[0] != null) {
				authorID = author[0];
			}
			if (responsible != null && responsible.length > 0 && responsible[0] != null) {
				responsibleID = responsible[0];
			}
			if (status != null && status.length > 0 && status[0] != null) {
				statusID = status[0];
			}
			if ("".equalsIgnoreCase(projectID) && "".equalsIgnoreCase(categoryID) && "".equalsIgnoreCase(originPlaceID) && "".equalsIgnoreCase(authorID) && "".equalsIgnoreCase(responsibleID) && "".equalsIgnoreCase(statusID)) {
				filter.setEnable(0);
				return filter;
			}
			if (!"".equalsIgnoreCase(projectID) && !"0".equalsIgnoreCase(projectID)) {
				filter.addCondition("project", projectID);
			}
			if (!"".equalsIgnoreCase(categoryID) && !"0".equalsIgnoreCase(categoryID)) {
				filter.addCondition("category", categoryID);
			}
			if (!"".equalsIgnoreCase(originPlaceID) && !"0".equalsIgnoreCase(originPlaceID)) {
				filter.addCondition("origin", originPlaceID);
			}
			if (!"".equalsIgnoreCase(authorID) && !"0".equalsIgnoreCase(authorID)) {
				filter.addCondition("author", authorID);
			}
			if (!"".equalsIgnoreCase(responsibleID) && !"0".equalsIgnoreCase(responsibleID)) {
				filter.addCondition("responsiblesection", responsibleID);
			}
			if (!"".equalsIgnoreCase(statusID) && !"0".equalsIgnoreCase(statusID)) {
				filter.addCondition("coordstatus", statusID);
			}


		}
		return filter;		
	}

}
