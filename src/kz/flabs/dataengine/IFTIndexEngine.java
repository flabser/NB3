package kz.flabs.dataengine;

import kz.lof.dataengine.jpadatabase.ftengine.ToFTIndex;
import kz.nextbase.script._Session;

public interface IFTIndexEngine {

	void registerTable(ToFTIndex table);

	@SuppressWarnings("rawtypes")
	kz.lof.dataengine.jpa.ViewPage search(String keyWord, _Session ses, int pageNum, int pageSize);

}
