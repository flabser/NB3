package kz.flabs.runtimeobj.queries;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.dataengine.IGlossaries;
import kz.flabs.dataengine.IQueryFormula;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.parser.SortByBlock;
import kz.flabs.runtimeobj.DocumentCollection;
import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.flabs.runtimeobj.document.BaseDocument;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.users.User;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.query.IQueryRule;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class GlossaryQuery extends Query{


	public GlossaryQuery(AppEnv env, IQueryRule rule, User u) throws DocumentException, DocumentAccessException {
		super(env, rule, u);
	}

	public int fetch(int pageNum, int pageSize, int parentDocID, int parentDocType, Set<DocID> toExpandResp, Set<String> toExpandCategory, DocID toFlash, Map<String, String[]> fields) throws DocumentException, DocumentAccessException, QueryFormulaParserException, QueryException{
		String sortField = Const.DEFAULT_SORT_COLUMN;
        String sortOrder = Const.DEFAULT_SORT_ORDER;
		String userid = (this.user != null ? user.getUserID() : "");
		String cacheID = (pageNum + "_" + pageSize + "_" + rule.getID() + "_" + toExpandResp + "_" + toExpandCategory + "_" + sortField + "_" + sortOrder + "_" + userid).toString();
		StringBuffer sortXml = new StringBuffer();
		QueryCache cache = CachePool.getQueryCache(cacheID);
		//cache = null;
		if (cache == null){
			switch(rule.getQuery().getSourceType()){
			case STATIC:
				IQueryFormula queryFormula = null;
				if (rule.getQueryFormulaBlocks().paramatrizedQuery){
					rule.getQueryFormulaBlocks().putParameters(fields);
					queryFormula = db.getQueryFormula(rule.getID(), rule.getQueryFormulaBlocks());		
				}else{
					queryFormula = rule.getQueryFormula();
				}
	            
	            SortByBlock sortBlock = queryFormula.getSortBlock();
                if (!"".equalsIgnoreCase(sortBlock.fieldName)){
                    sortField = sortBlock.fieldName;
                }
                if (!"".equalsIgnoreCase(sortBlock.order)){
                    sortOrder = sortBlock.order;
                }

	            sortXml.append("<sorting>");
	            sortXml.append("<field>" + sortField + "</field>");
	            sortXml.append("<order>" + sortOrder + "</order>");
	            sortXml.append("</sorting>");
				
				IGlossaries glos = db.getGlossaries();
	//			System.out.println(queryFormula.isGroupBy());
				colCount = glos.getGlossaryByConditionCount(queryFormula);
			
				if (pageNum == 0)pageNum = RuntimeObjUtil.countMaxPage(colCount, pageSize);		
				xmlFragment.append(glos.getGlossaryByCondition(queryFormula, db.calcStartEntry(pageNum, pageSize), pageSize, rule.getFieldsCondition(), toExpandCategory, toExpandResp, rule.getGroupByPublicationFormat()));
				if (rule.getCacheMode() == RunMode.ON) {
					CachePool.update(cacheID, colCount, xmlFragment, CacheInitiatorType.GLOSSARY_QUERY);
				}
				break;
			case MACRO:
				switch(rule.getMacro()){
				case RESPONSES:				
					DocumentCollection docs = db.getGlossaries().getGlossaryDescendants(parentDocID, parentDocType, null, 1, userGroups, userID);
					xmlFragment.append("<responses>" + docs.xmlContent.toString() + "</responses>") ;
					colCount = docs.count;							
				}

				break;
			case SCRIPT:

				break;
			}
		}else{		
			colCount = cache.getCount();			
			xmlFragment = cache.getContent();
		}

		xmlFragment.append(sortXml);
		xmlFragment = wrapToQuery(xmlFragment, pageNum, pageSize, toFlash);
		return 0;

	}
	
	public ArrayList<BaseDocument> fetch(Map<String, String[]> fields) throws DocumentException, DocumentAccessException, QueryFormulaParserException, QueryException{
		IQueryFormula nf = null;
		if (rule.getQueryFormulaBlocks().paramatrizedQuery){
			rule.getQueryFormulaBlocks().putParameters(fields);
			nf = db.getQueryFormula(rule.getID(), rule.getQueryFormulaBlocks());					
		}else{
			nf = rule.getQueryFormula();
		}
	

		IGlossaries glos = db.getGlossaries();
		ArrayList result = glos.getGlossaryByCondition(nf, 0, 0);	
	
		return result;
	}
}
