package kz.flabs.dataengine;

import java.util.ArrayList;
import java.util.Set;
import kz.flabs.exception.DocumentException;
import kz.flabs.exception.QueryException;
import kz.flabs.runtimeobj.document.*;
import kz.flabs.runtimeobj.document.glossary.Glossary;
import kz.flabs.webrule.constants.TagPublicationFormatType;

public interface IPlainGlossaries {
	int getGlossaryElementID(String keyWord, String form);
	
	public ArrayList<BaseDocument> getAllGlossaryDocuments(int start, int end, String[] fields, boolean useCache);
	public int getGlossaryCount();
	public StringBuffer getGlossaryByCondition(IQueryFormula condition, int offset, int pageSize, String fieldsCond, Set<String> toExpand, TagPublicationFormatType publishAs) throws DocumentException, QueryException;
	public ArrayList<Glossary> getGlossaryByCondition(IQueryFormula condition, int offset, int pageSize);
	public int getGlossaryByConditionCount(IQueryFormula condition);
	public Glossary getGlossaryDocumentByID(int docID, boolean useCache, Set<String> complexUserID, String absoluteUserID);
	public int insertGlossaryDocument(Glossary doc) throws DocumentException;
	public int updateGlossaryDocument(Glossary doc) throws DocumentException;
	public boolean deleteGlossaryDocument(int docID);	
	
}
