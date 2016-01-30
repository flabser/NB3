package kz.flabs.runtimeobj.queries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.*;
import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.parser.FilterParser;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.parser.SortByBlock;
import kz.flabs.runtimeobj.DocumentCollection;
import kz.flabs.runtimeobj.Filter;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.runtimeobj.document.*;
import kz.flabs.runtimeobj.viewentry.IViewEntry;
import kz.flabs.runtimeobj.viewentry.IViewEntryCollection;
import kz.flabs.users.User;
import kz.flabs.util.Util;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.query.IQueryRule;
import kz.pchelka.server.Server;

public class Query implements Const{
	protected AppEnv env;
	protected IDatabase db;		
	protected IQueryRule rule;
	protected HashMap<String, String[]> fields;		
	protected HashSet<String> userGroups = null;
	protected String userID;
	protected int colCount;	
	protected StringBuffer xmlFragment  = new StringBuffer(10000);	
	protected User user;
	
	private DocumentCollection col = new DocumentCollection();	

	public Query(AppEnv env, IQueryRule rule, User user, HashMap<String, String[]> fields, int pageNum, int pageSize, int parentDocID, int parentDocType, Set<DocID> toExpandResp, Set<String> toExpandCategory, DocID toFlash) throws DocumentException, DocumentAccessException, QueryFormulaParserException, QueryException{
		this.env = env;
		db = env.getDataBase();	
		this.rule = rule;
		this.fields = fields;		
		userID = user.getUserID();
		userGroups =  user.getAllUserGroups();	 
		this.user = user;
		fetch(pageNum, pageSize, parentDocID, parentDocType, toExpandResp, toExpandCategory, toFlash, fields);
	}

	public Query(AppEnv env, IQueryRule rule, User user) throws DocumentException, DocumentAccessException{
		this.rule = rule;
		db = env.getDataBase();	
		userID = user.getUserID();		
		userGroups =  user.getAllUserGroups();	
		this.user = user;
	}

	public void setQiuckFilter(Map<String, String[]> fields, AppEnv env) {
		Filter filterFromFields = FilterParser.quickFilterParse(fields, this.db.getParent());
		if (filterFromFields.getEnable() == 1 ) {//|| !filterFromFields.getConditions().isEmpty()) {
			user.getSession().quickFilters.remove(filterFromFields);
			user.getSession().quickFilters.add(filterFromFields);

		} else {
		
			String[] filterName = fields.get("id");
			if (filterName != null && !"".equalsIgnoreCase(filterName[0])) {
				Filter filterFromSession = user.getSession().quickFilters.getQuickFilter(filterName[0]);
				if (filterFromSession != null) {
					rule.getQueryFormulaBlocks().setQuickFilter(filterFromSession);
					return;
				}
			}
		}
		rule.getQueryFormulaBlocks().setQuickFilter(filterFromFields);		
	}

	public int fetch(int pageNum, int pageSize, int parentDocID, int parentDocType, Set<DocID> toExpandResp, Set<String> toExpandCategory, DocID toFlash, Map<String, String[]> fields) throws DocumentException, DocumentAccessException, QueryFormulaParserException, QueryException{
		IQueryFormula nf = null;
		if (rule.getQueryFormulaBlocks().paramatrizedQuery){
			rule.getQueryFormulaBlocks().putParameters(fields);
			nf = db.getQueryFormula(rule.getID(), rule.getQueryFormulaBlocks());					
		}else{
			nf = rule.getQueryFormula();
		}

		String sortField = null;
		if (fields != null && fields.containsKey("sortfield")){
			sortField = fields.get("sortfield")[0];
		}
		if (sortField == null || sortField.equalsIgnoreCase("")){
			sortField = Const.DEFAULT_SORT_COLUMN;
		}

		String sortOrder = null;
		if (fields != null && fields.containsKey("order")){
			sortOrder = fields.get("order")[0];
		}
		if (sortOrder == null || sortOrder.equalsIgnoreCase("")){
			sortOrder = Const.DEFAULT_SORT_ORDER;
		}
		SortByBlock sortBlock = nf.getSortBlock();
		sortBlock.fieldName = sortField;
		sortBlock.order = sortOrder;

		StringBuffer addXml = new StringBuffer();
		addXml.append("<filtered>");
		Filter filter = nf.getQuickFilter();
		if (filter != null) {
			HashMap<String, String> conditions = filter.getConditions();
			for (String fieldName : conditions.keySet()){
				addXml.append("<condition>");
				addXml.append("<fieldname>" + fieldName + "</fieldname>");
				addXml.append("<value>" + conditions.get(fieldName) + "</value>");
				addXml.append("</condition>");
			}
		}
		addXml.append("</filtered>");
		addXml.append("<sorting>");
		addXml.append("<field>" + sortField + "</field>");
		addXml.append("<order>" + sortOrder + "</order>");
		addXml.append("</sorting>");
		switch(rule.getQuery().getSourceType()){
		case STATIC:
			switch(rule.getQueryType()){
			case PROJECT:
				colCount = db.getProjects().getProjectsByConditionCount(nf, userGroups, userID);
				if (pageNum == 0)pageNum = RuntimeObjUtil.countMaxPage(colCount, pageSize);					
				xmlFragment.append(db.getProjects().getProjectsByCondition(nf, userGroups, userID, rule.getFields(), db.calcStartEntry(pageNum, pageSize), pageSize, toExpandResp));
				break;
			case TASK:
				ITasks tasks = db.getTasks();
				colCount = tasks.getTasksCountByCondition(nf, userGroups, userID);
				if (pageNum == 0)pageNum = RuntimeObjUtil.countMaxPage(colCount, pageSize);	
				xmlFragment.append(tasks.getTasksByCondition(nf, userGroups, userID, rule.getFields(), toExpandResp, db.calcStartEntry(pageNum, pageSize), pageSize));
				break;			
			case GROUP:
				IStructure struct = db.getStructure();
				colCount = struct.getGroupsCountByCondition(nf, userID);
				if (pageNum == 0)pageNum = RuntimeObjUtil.countMaxPage(colCount, pageSize);	
				xmlFragment.append(struct.getGroupsByCondition(nf, db.calcStartEntry(pageNum, pageSize), pageSize, rule.getFieldsCondition(), toExpandCategory));
				break;
			case FORUM_THREAD:
				IForum forum = db.getForum();
				colCount = forum.getPostsCountByTopicID(parentDocID, userGroups, userID);
				xmlFragment.append(forum.getForumThreadByTopicID(parentDocID, userGroups, userID, rule.getFields(), pageNum, pageSize, toExpandResp));
				break;					
			default:
				colCount = db.getDocsCountByCondition(nf, userGroups, userID);
				if (pageNum == 0)pageNum = RuntimeObjUtil.countMaxPage(colCount, pageSize);	
				xmlFragment.append(db.getDocsByCondition(nf, userGroups, userID, db.calcStartEntry(pageNum, pageSize), pageSize, rule.getFieldsCondition(), toExpandResp, toExpandCategory, rule.getGroupByPublicationFormat(), pageNum));

			}

			break;
		case MACRO:
			if(rule.getQuery().getValue().equalsIgnoreCase("responses")){
				switch(parentDocType){
				case DOCTYPE_ORGANIZATION:
				case DOCTYPE_DEPARTMENT:
				case DOCTYPE_EMPLOYER:
					xmlFragment.append("<responses>");
					IStructure struct = db.getStructure();
					xmlFragment.append(struct.getResponses(parentDocID, parentDocType));						
					xmlFragment.append("</responses>");
					break;
				default:
					DocumentCollection docs = db.getDescendants(parentDocID, parentDocType, sortBlock, 1, userGroups, userID);
					xmlFragment.append("<responses>" + docs.xmlContent.toString() + "</responses>") ;
					colCount = docs.count;
				}
			}else if(rule.getQuery().getValue().equalsIgnoreCase("toconsider")){					
				colCount = db.getMyDocsProcessor().getToConsiderCount(nf, userID);	
				if (pageNum == 0)pageNum = RuntimeObjUtil.countMaxPage(colCount, pageSize);	
				xmlFragment.append(db.getMyDocsProcessor().getToConsider(nf, userID, db.calcStartEntry(pageNum, pageSize), pageSize, rule.getFields()));
			}else if(rule.getQuery().getValue().equalsIgnoreCase("tasksforme")){					
				colCount = db.getMyDocsProcessor().getTasksForMeCount(nf, userID);	
				if (pageNum == 0)pageNum = RuntimeObjUtil.countMaxPage(colCount, pageSize);	
				xmlFragment.append(db.getMyDocsProcessor().getTasksForMe(nf, userGroups, userID, toExpandResp, db.calcStartEntry(pageNum, pageSize), pageSize, rule.getFields()));
			}else if(rule.getQuery().getValue().equalsIgnoreCase("tasksforme_count")){	
				xmlFragment.append(Integer.toString(db.getMyDocsProcessor().getTasksForMeCount(nf, userID)));
			}else if(rule.getQuery().getValue().equalsIgnoreCase("mytasks")){				
				colCount = db.getMyDocsProcessor().getMyTasksCount(nf, userID);
				if (pageNum == 0)pageNum = RuntimeObjUtil.countMaxPage(colCount, pageSize);	
				xmlFragment.append(db.getMyDocsProcessor().getMyTasks(nf, userGroups, userID, toExpandResp, db.calcStartEntry(pageNum, pageSize), pageSize, rule.getFields()));
			}else if(rule.getQuery().getValue().equalsIgnoreCase("mytasks_count")){	
				xmlFragment.append(Integer.toString(db.getMyDocsProcessor().getMyTasksCount(nf, userID)));
			}else if(rule.getQuery().getValue().equalsIgnoreCase("completetask")){					
				colCount = db.getMyDocsProcessor().getCompleteTaskCount(nf, userID);
				if (pageNum == 0)pageNum = RuntimeObjUtil.countMaxPage(colCount, pageSize);	
				xmlFragment.append(db.getMyDocsProcessor().getCompleteTask(nf, userID, db.calcStartEntry(pageNum, pageSize), pageSize, rule.getFields()));			
			}else if(rule.getQuery().getValue().equalsIgnoreCase("completetask_count")){	
				xmlFragment.append(Integer.toString(db.getMyDocsProcessor().getCompleteTaskCount(nf, userID)));
			}else if(rule.getQuery().getValue().equalsIgnoreCase("waitforcoord")){					
				colCount = db.getMyDocsProcessor().getPrjsWaitForCoordCount(nf, userID);	
				if (pageNum == 0)pageNum = RuntimeObjUtil.countMaxPage(colCount, pageSize);	
				xmlFragment.append(db.getMyDocsProcessor().getPrjsWaitForCoord(nf, userID, db.calcStartEntry(pageNum, pageSize), pageSize, rule.getFields()));
			}else if(rule.getQuery().getValue().equalsIgnoreCase("waitforcoord_count")){	
				xmlFragment.append(Integer.toString(db.getMyDocsProcessor().getPrjsWaitForCoordCount(nf,userID)));
			}else if(rule.getQuery().getValue().equalsIgnoreCase("waitforsign")){					
				colCount = db.getMyDocsProcessor().getPrjsWaitForSignCount(nf, userID);	
				if (pageNum == 0)pageNum = RuntimeObjUtil.countMaxPage(colCount, pageSize);	
				xmlFragment.append(db.getMyDocsProcessor().getPrjsWaitForSign(nf, userID, db.calcStartEntry(pageNum, pageSize), pageSize, rule.getFields()));
			}else if(rule.getQuery().getValue().equalsIgnoreCase("waitforsign_count")){	
				xmlFragment.append(Integer.toString(db.getMyDocsProcessor().getPrjsWaitForSignCount(nf, userID)));
            }else if (rule.getQuery().getValue().equalsIgnoreCase("all_activity")) {
                colCount = db.getUserActivity().getAllActivityCount();
                xmlFragment.append(db.getUserActivity().getAllActivity(db.calcStartEntry(pageNum, pageSize), pageSize));
            }else if(rule.getQuery().getValue().equalsIgnoreCase("service_activity")) {
                String serviceAndMethods = "";
                if (fields.containsKey("serviceandmethods")) {
                    serviceAndMethods = fields.get("serviceandmethods")[0];
                }

                String user = "";
                if (fields.containsKey("user")) {
                    user = fields.get("user")[0];
                }

                SimpleDateFormat format = new SimpleDateFormat("dd.HH.yyyy");
                String dateToString = "";
                if (fields.containsKey("dateto")) {
                    dateToString = fields.get("dateto")[0];
                }

                Date dateTo = null;
                if (dateToString != null && !"".equalsIgnoreCase(dateToString)) {
                    try {
                        dateTo = format.parse(dateToString);
                    } catch (ParseException e) {
                    }
                }

                String dateFromString = "";
                if (fields.containsKey("datefrom")) {
                    dateFromString = fields.get("datefrom")[0];
                }

                Date dateFrom = null;
                if (dateFromString != null && !"".equalsIgnoreCase(dateFromString)) {
                    try {
                        dateFrom = format.parse(dateFromString);
                    } catch (ParseException e) {
                    }
                }
                String totalsFromString = "";
                if (fields.containsKey("totalsfrom")) {
                    totalsFromString = fields.get("totalsfrom")[0];
                }

                int totalsFrom = 0;
                if (totalsFromString != null && !"".equalsIgnoreCase(totalsFromString)) {
                    try {
                        totalsFrom = Integer.parseInt(totalsFromString);
                    } catch (NumberFormatException e) {
                    }
                }

                String totalsToString = "";
                if (fields.containsKey("totalsto")) {
                    totalsToString = fields.get("totalsto")[0];
                }

                int totalsTo = 0;
                if (totalsToString != null && !"".equalsIgnoreCase(totalsToString)) {
                    try {
                        totalsTo = Integer.parseInt(totalsToString);
                    } catch (NumberFormatException e) {
                    }
                }

                String errorsOnlyString = "";

                if (fields.containsKey("errorsonly")) {
                    errorsOnlyString = fields.get("errorsonly")[0];
                }

                String springServer = "";
                if (fields.containsKey("springserver")) {
                    springServer = fields.get("springserver")[0];
                }

                String diffTimeToString = "";
                if (fields.containsKey("difftimeto")) {
                    diffTimeToString = fields.get("difftimeto")[0];
                }

                int diffTimeTo = 0;
                if (diffTimeToString != null && !"".equalsIgnoreCase(diffTimeToString)) {
                    try {
                        diffTimeTo = Integer.parseInt(diffTimeToString);
                    } catch (NumberFormatException e) {
                    }
                }

                String diffTimeFromString = "";
                if (fields.containsKey("difftimefrom")) {
                    diffTimeFromString = fields.get("difftimefrom")[0];
                }

                int diffTimeFrom = 0;
                if (diffTimeFromString != null && !"".equalsIgnoreCase(diffTimeFromString)) {
                    try {
                        diffTimeFrom = Integer.parseInt(diffTimeFromString);
                    } catch (NumberFormatException e) {
                    }
                }

                boolean errorsOnly = false;
                if (errorsOnlyString != null && !"".equalsIgnoreCase(errorsOnlyString)) {
                    errorsOnly = Boolean.parseBoolean(errorsOnlyString);
                }
                colCount = db.getActivity().getActivitiesCount(user, serviceAndMethods, dateFrom, dateTo, totalsFrom, totalsTo, errorsOnly, springServer, diffTimeFrom, diffTimeTo);
                xmlFragment.append(db.getActivity().getActivitiesAsXML(db.calcStartEntry(pageNum, pageSize), pageSize, user, serviceAndMethods, dateFrom, dateTo, totalsFrom, totalsTo, errorsOnly, springServer, diffTimeFrom, diffTimeTo));
            }else if(rule.getQuery().getValue().equalsIgnoreCase("favourites")){
				colCount = db.getFavoritesCount(userGroups, userID);	
				if (pageNum == 0)pageNum = RuntimeObjUtil.countMaxPage(colCount, pageSize);	
				xmlFragment.append(db.getFavorites(nf, userGroups, userID, db.calcStartEntry(pageNum, pageSize), pageSize, rule.getFieldsCondition(), toExpandResp, toExpandCategory, rule.getGroupByPublicationFormat(), pageNum));
			}else if(rule.getQuery().getValue().equalsIgnoreCase("favourites_count")){					
				xmlFragment.append(db.getFavoritesCount(userGroups, userID));
			}else if (rule.getQuery().getValue().equalsIgnoreCase("filter")){
				int id = 0;
				try {
					id = Integer.parseInt(fields.get("filterid")[0]); 
				} catch(NumberFormatException e) {
					Server.logger.errorLogEntry(e);
				}
				Filter filt = db.getFilters().getFilterByID(id, userGroups, userID);				
				colCount = db.getFilters().getDocumentsCountByFilter(filt, userGroups, userID, db.calcStartEntry(pageNum, pageSize), pageSize);
				if (pageNum == 0)pageNum = RuntimeObjUtil.countMaxPage(colCount, pageSize);	
				xmlFragment.append(db.getFilters().getDocumentsByFilter(filt, userGroups, userID, db.calcStartEntry(pageNum, pageSize), pageSize, toExpandResp, toExpandCategory, rule.getGroupByPublicationFormat(), pageNum));
			}else if(rule.getQuery().getValue().equalsIgnoreCase("TASKS")){
				colCount = db.getTasks().getTasksCountByCondition("TASKTYPE = 3", userGroups, userID);
				if (pageNum == 0)pageNum = RuntimeObjUtil.countMaxPage(colCount, pageSize);	
				xmlFragment.append(db.getTasks().getTasksByCondition("TASKTYPE = 3", userGroups, userID, rule.getFields(), toExpandResp, db.calcStartEntry(pageNum, pageSize), pageSize));
			}else if(rule.getQuery().getValue().equalsIgnoreCase("structure")){	
				if (rule.getCacheMode() == RunMode.ON){
					int cacheID = rule.getQuery().hashCode();
					if (toExpandResp != null){
						cacheID = rule.getQuery().hashCode() + toExpandResp.toString().hashCode();
					}
					QueryCache cache = CachePool.getQueryCache(cacheID);
					if (cache != null){
						System.out.println("get from cache, cache hash = " + cacheID);
						xmlFragment = cache.getContent();
					}else{					
						StringBuffer structAsXML =  db.getStructure().getStructure(toExpandResp);
						CachePool.update(structAsXML, cacheID, CacheInitiatorType.QUERY);
						xmlFragment = structAsXML;
					}
				}else{					
					StringBuffer structAsXML =  db.getStructure().getStructure(toExpandResp);
					xmlFragment = structAsXML;
				}
			}else if(rule.getQuery().getValue().equalsIgnoreCase("EXPANDED_STRUCTURE")){					
				StringBuffer structAsXML =  db.getStructure().getExpandedStructure();				
				xmlFragment = structAsXML;
				//}
			}else if (rule.getQuery().getValue().equalsIgnoreCase("discussion")) {
				DocumentCollection docs = db.getDiscussion(parentDocID, parentDocType, null, 1, userGroups, userID);
				xmlFragment.append("<responses>" + docs.xmlContent.toString() + "</responses>") ;
				colCount = docs.count;
			}else if(rule.getQuery().getValue().equalsIgnoreCase("RECYCLE_BIN")) {
				StringBuffer recycleAsXML = db.getUsersRecycleBin(db.calcStartEntry(pageNum, pageSize), pageSize, userID);
				colCount = db.getUsersRecycleBinCount(db.calcStartEntry(pageNum, pageSize), pageSize, userID);
				xmlFragment = recycleAsXML;
			}else if(rule.getQuery().getValue().equalsIgnoreCase("SYSTEM_USERS")) {
				String condition = "";
				String keyWord[] = fields.get("keyword");
				if (keyWord != null){
					condition = "USERID LIKE '" + keyWord[0] + "%'";
				}
				IViewEntryCollection col = DatabaseFactory.getSysDatabase().getUsers(condition, db.calcStartEntry(pageNum, pageSize), pageSize);
				colCount =  DatabaseFactory.getSysDatabase().getUsersCount(condition);			
				for(IViewEntry entry:col.getEntries()){
					xmlFragment.append(entry.toXML());
				}
			
			} else if (rule.getQuery().getValue().equalsIgnoreCase("statistic")) {
                StringBuffer statXML = db.getProjects().getStatisticsByAllObjects();
                xmlFragment = statXML;
            }
			break;	
		}
		xmlFragment.append(addXml);
		xmlFragment = wrapToQuery(xmlFragment, pageNum, pageSize, toFlash);
		return 0;

	}

	public ArrayList<BaseDocument> fetch(Map<String, String[]> fields) throws DocumentException, DocumentAccessException, QueryFormulaParserException, QueryException, ComplexObjectException{
		IQueryFormula nf = null;
		if (rule.getQueryFormulaBlocks().paramatrizedQuery){
			rule.getQueryFormulaBlocks().putParameters(fields);
			nf = db.getQueryFormula(rule.getID(), rule.getQueryFormulaBlocks());					
		}else{
			nf = rule.getQueryFormula();
		}
		
		SortByBlock sortBlock = nf.getSortBlock();
		sortBlock.fieldName = Const.DEFAULT_SORT_COLUMN;
		sortBlock.order = Const.DEFAULT_SORT_ORDER;
		
		switch(rule.getQuery().getSourceType()){
		case STATIC:
			switch(rule.getQueryType()){
			case PROJECT:
				xmlFragment.append(rule.toString());
				xmlFragment.append(" not implemented");
				break;
			case TASK:		
				xmlFragment.append(rule.toString());
				xmlFragment.append(" not implemented");
				break;
			case ROLE:					
				xmlFragment.append(rule.toString());
				xmlFragment.append(" not implemented");
				break;
			case GROUP:
				xmlFragment.append(rule.toString());
				xmlFragment.append(" not implemented");
				break;
			case FORUM_THREAD:
				xmlFragment.append(rule.toString());
				xmlFragment.append(" not implemented");
				break;			
			default:
				Set<String> complexUserID = new HashSet<String>();
				complexUserID.add(userID);
				ArrayList<BaseDocument> result = db.getDocumentsByCondition(nf, complexUserID, userID, 0, 0);
				xmlFragment.append(rule.toString());
				xmlFragment.append(" count=" + result.size());
				return result;		
			}

			break;
		case MACRO:
			xmlFragment.append(rule.toString());
			xmlFragment.append(" not implemented");
			break;	
		}

		return null;
	}


	public int count( Map<String, String[]> fields) throws DocumentException, DocumentAccessException, QueryFormulaParserException, QueryException{
		IQueryFormula nf = null;
		if (rule.getQueryFormulaBlocks().paramatrizedQuery){
			rule.getQueryFormulaBlocks().putParameters(fields);
			nf = db.getQueryFormula(rule.getID(), rule.getQueryFormulaBlocks());					
		}else{
			nf = rule.getQueryFormula();
		}

		SortByBlock sortBlock = nf.getSortBlock();
		sortBlock.fieldName = Const.DEFAULT_SORT_COLUMN;
		sortBlock.order = Const.DEFAULT_SORT_ORDER;
	
		
		switch(rule.getQuery().getSourceType()){
		case STATIC:
			switch(rule.getQueryType()){
			case PROJECT:
				return db.getProjects().getProjectsByConditionCount(nf, userGroups, userID);			
			case TASK:
				ITasks tasks = db.getTasks();
				return tasks.getTasksCountByCondition(nf, userGroups, userID);			
			case ROLE:					
				return 0;
			case GROUP:
				IStructure struct = db.getStructure();
				return struct.getGroupsCountByCondition(nf, userID);			
			case FORUM_THREAD:
				return 0;		
			default:
				return db.getDocsCountByCondition(nf, userGroups, userID);		
			}

			
		case MACRO:
			if(rule.getQuery().getValue().equalsIgnoreCase("toconsider")){					
				return db.getMyDocsProcessor().getToConsiderCount(nf, userID);
			}else if(rule.getQuery().getValue().equalsIgnoreCase("tasksforme")){					
				return db.getMyDocsProcessor().getTasksForMeCount(nf, userID);				
			}else if(rule.getQuery().getValue().equalsIgnoreCase("mytasks")){				
				return db.getMyDocsProcessor().getMyTasksCount(nf, userID);
			}else if(rule.getQuery().getValue().equalsIgnoreCase("completetask")){					
				return db.getMyDocsProcessor().getCompleteTaskCount(nf, userID);
			}else if(rule.getQuery().getValue().equalsIgnoreCase("waitforcoord")){					
				return db.getMyDocsProcessor().getPrjsWaitForCoordCount(nf, userID);	
			}else if(rule.getQuery().getValue().equalsIgnoreCase("waitforsign")){					
				return db.getMyDocsProcessor().getPrjsWaitForSignCount(nf, userID);
			}else if(rule.getQuery().getValue().equalsIgnoreCase("favourites")){					
				return db.getFavoritesCount(userGroups, userID);	
			}				
			
			break;	
		}
		
		return 0;

	}
	
	public String toXML(){
		return xmlFragment.toString();
	}

	public String toString(){
		return "count=" + col.col.size() + ", xmlfragment=" + xmlFragment;
	}

	protected StringBuffer wrapToQuery(StringBuffer xmlFragment, int pageNum, int pageSize, DocID toFlash){
		StringBuffer wrapped  = new StringBuffer(10500);		
		String flashAttr = "";
		if(toFlash != null){
			flashAttr =  "flashdocid=\"" + toFlash.id + "\" flashdoctype=\"" + toFlash.type + "\"";
		}
		wrapped.append("<query count=\"" + colCount + "\" ruleid=\"" + rule.getID() +"\" " + flashAttr + 
				" currentpage=\"" + pageNum + "\" maxpage=\"" + RuntimeObjUtil.countMaxPage(colCount, pageSize) + "\""+
				" time=\"" + Util.convertDataTimeToProvider(new Date()) + "\" userid=\""+ userID + "\">" + xmlFragment + "</query>");
		return wrapped;
	}


}
