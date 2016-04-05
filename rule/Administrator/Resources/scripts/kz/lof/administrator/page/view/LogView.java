package kz.lof.administrator.page.view;

import kz.flabs.runtimeobj.RuntimeObjUtil;
import kz.lof.dataengine.jpa.ViewPage;
import kz.lof.log.LogFiles;
import kz.lof.scripting._POJOListWrapper;
import kz.lof.scripting._Session;
import kz.lof.scripting._WebFormData;
import kz.lof.scripting.event._DoPage;

public class LogView extends _DoPage {

	@Override
	public void doGET(_Session session, _WebFormData formData) {
		int pageNum = formData.getNumberValueSilently("page", 1);
		int pageSize = session.pageSize;

		LogFiles logs = new LogFiles();
		long count = logs.getCount();
		int maxPage = RuntimeObjUtil.countMaxPage(count, pageSize);
		if (pageNum == 0) {
			pageNum = maxPage;
		}
		ViewPage vp = new ViewPage(logs.getLogFileList(), count, maxPage, pageNum);
		addContent(new _POJOListWrapper(vp.getResult(), vp.getMaxPage(), vp.getCount(), vp.getPageNum(), session));
	}
}
