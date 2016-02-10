package kz.flabs.dataengine;

import java.util.Set;

import kz.flabs.exception.ComplexObjectException;
import kz.flabs.exception.DocumentException;
import kz.flabs.users.User;
import kz.nextbase.script._Session;
import kz.nextbase.script._ViewEntryCollection;

public interface IFTIndexEngine {

	kz.lof.dataengine.jpa.ViewPage search(String keyWord, _Session ses, int pageNum, int pageSize);

	@Deprecated
	int ftSearchCount(Set<String> complexUserID, String absoluteUserID, String keyWord) throws DocumentException, ComplexObjectException;

	@Deprecated
	StringBuffer ftSearch(Set<String> complexUserID, String absoluteUserID, String keyWord, int offset, int pageSize) throws DocumentException,
	FTIndexEngineException, ComplexObjectException;

	@Deprecated
	int updateFTIndex() throws FTIndexEngineException;

	@Deprecated
	_ViewEntryCollection search(String keyWord, User user, int pageNum, int pageSize, String[] sorting, String[] filters)
			throws FTIndexEngineException;
}
