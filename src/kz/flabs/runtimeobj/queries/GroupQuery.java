package kz.flabs.runtimeobj.queries;

import java.util.Map;
import java.util.Set;
import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.IQueryFormula;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.users.User;
import kz.flabs.webrule.query.IQueryRule;

public class GroupQuery extends Query{

	public GroupQuery(AppEnv env, IQueryRule rule, User u) throws DocumentException,	DocumentAccessException {
		super(env, rule, u);
	}

	public int fetch(int pageNum, int pageSize, int parentDocID, int parentDocType, Set<DocID> toExpandResp, Set<String> toExpandCategory, DocID toFlash, Map<String, String[]> fields) throws DocumentException, DocumentAccessException, QueryFormulaParserException{
		IQueryFormula queryFormula = rule.getQueryFormula();
		colCount = db.getStructure().getStructObjByConditionCount(queryFormula);		
		xmlFragment = wrapToQuery(xmlFragment.append(db.getStructure().getGroupsByCondition(queryFormula, db.calcStartEntry(pageNum, pageSize), pageSize,"",toExpandCategory)),pageNum, pageSize, toFlash);
		return 0;
	}
}
