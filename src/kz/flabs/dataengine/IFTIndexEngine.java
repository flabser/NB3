package kz.flabs.dataengine;

import kz.lof.dataengine.jpadatabase.ftengine.FTEntity;
import kz.nextbase.script._Session;

public interface IFTIndexEngine {

	void registerTable(FTEntity table);

	@SuppressWarnings("rawtypes")
	kz.lof.dataengine.jpa.ViewPage search(String keyWord, _Session ses, int pageNum, int pageSize);

}
