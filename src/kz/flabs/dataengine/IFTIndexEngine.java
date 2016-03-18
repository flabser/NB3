package kz.flabs.dataengine;

import kz.lof.dataengine.jpadatabase.ftengine.FTEntity;
import kz.lof.scripting._Session;
import kz.lof.dataengine.jpa.ViewPage;

import java.util.List;

public interface IFTIndexEngine {

	void registerTable(FTEntity table);

	@SuppressWarnings("rawtypes")
	ViewPage<?> search(String keyWord, _Session ses, int pageNum, int pageSize);

}
