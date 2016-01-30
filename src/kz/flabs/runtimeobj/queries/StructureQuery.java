package kz.flabs.runtimeobj.queries;

import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.IQueryFormula;
import kz.flabs.dataengine.IStructure;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.parser.QueryFormulaParserException;
import kz.flabs.runtimeobj.DocumentCollection;
import kz.flabs.runtimeobj.document.DocID;
import kz.flabs.users.User;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.query.IQueryRule;

import java.util.Map;
import java.util.Set;

public class StructureQuery extends Query {

    public StructureQuery(AppEnv env, IQueryRule rule, User u) throws DocumentException, DocumentAccessException {
        super(env, rule, u);
    }

    public int fetch(int pageNum, int pageSize, int parentDocID, int parentDocType, Set<DocID> toExpandResp, Set<String> toExpandCategory, DocID toFlash, Map<String, String[]> fields) throws DocumentException, DocumentAccessException, QueryFormulaParserException {
        switch (rule.getQuery().getSourceType()) {
            case STATIC:
                IQueryFormula queryFormula = null;
                if (rule.getQueryFormulaBlocks().paramatrizedQuery) {
                    rule.getQueryFormulaBlocks().putParameters(fields);
                    queryFormula = db.getQueryFormula(rule.getID(), rule.getQueryFormulaBlocks());
                } else {
                    queryFormula = rule.getQueryFormula();
                }

                colCount = db.getStructure().getStructObjByConditionCount(queryFormula);
                xmlFragment.append(db.getStructure().getStructObjByCondition(queryFormula, db.calcStartEntry(pageNum, pageSize), pageSize));

                break;
            case MACRO:
                if (rule.getQuery().getValue().equalsIgnoreCase("responses")) {
                    if (parentDocType == DOCTYPE_ORGANIZATION) {
                        IStructure struct = db.getStructure();
                        xmlFragment.append("<responses>");
                        xmlFragment.append(struct.getEmployers(parentDocID, parentDocType, toExpandResp));
                        xmlFragment.append(struct.getDepartments(parentDocID, parentDocType, toExpandResp));
                        xmlFragment.append("</responses>");
                    } else if (parentDocType == DOCTYPE_DEPARTMENT) {
                        IStructure struct = db.getStructure();
                        xmlFragment.append("<responses>");
                        xmlFragment.append(struct.getEmployers(parentDocID, parentDocType, toExpandResp));
                        xmlFragment.append("</responses>");
                    } else {
                        DocumentCollection docs = db.getDescendants(parentDocID, parentDocType, null, 1, userGroups, userID);
                        xmlFragment.append("<responses>" + docs.xmlContent.toString() + "</responses>");
                        colCount = docs.count;
                    }
                } else if (rule.getQuery().getValue().equalsIgnoreCase("structure")) {
                    if (rule.getCacheMode() == RunMode.ON) {
                        int cacheID = rule.getQuery().hashCode();
                        if (toExpandResp != null) {
                            cacheID = rule.getQuery().hashCode() + toExpandResp.toString().hashCode();
                        }
                        QueryCache cache = CachePool.getQueryCache(cacheID);
                        if (cache != null) {
                            System.out.println("get from cache, cache hash = " + cacheID);
                            xmlFragment.append(cache.getContent());
                        } else {
                            IStructure struct = db.getStructure();
                            StringBuffer structAsXML = struct.getStructure(toExpandResp);
                            CachePool.update(structAsXML, cacheID, CacheInitiatorType.QUERY);
                            xmlFragment.append(structAsXML);
                        }
                    } else {
                        IStructure struct = db.getStructure();
                        StringBuffer structAsXML = struct.getStructure(toExpandResp);
                        xmlFragment.append(structAsXML);
                    }
                } else if (rule.getQuery().getValue().equalsIgnoreCase("EXPANDED_STRUCTURE")) {
                    IStructure struct = db.getStructure();
                    StringBuffer structAsXML = struct.getExpandedStructure();
                    xmlFragment.append(structAsXML);
                    //}
                } else if (rule.getQuery().getValue().equalsIgnoreCase("executors_by_freq")) {
                    xmlFragment.append(db.getStructure().getEmployersByFrequencyExecution());
                } else {
                    xmlFragment.append(db.getStructure().getEmployersByRoles(rule.getQuery().getValue()));
                }
                break;
        }
        xmlFragment = wrapToQuery(xmlFragment, pageNum, pageSize, toFlash);
        return 0;
    }
}
