package staff.page.action;

import kz.flabs.dataengine.jpa.ViewPage;
import kz.flabs.localization.LanguageType;
import kz.nextbase.script._POJOListWrapper;
import kz.nextbase.script._Session;
import kz.nextbase.script._WebFormData;
import kz.nextbase.script.events._DoPage;
import staff.dao.EmployeeDAO;

/**
 * 
 * 
 * @author Kayra created 09-01-2016
 */

public class GetEmployeesAction extends _DoPage {

	@Override
	public void doGET(_Session ses, _WebFormData formData, LanguageType lang) {
		String keyword = formData.getEncodedValueSilently("keyword");
		int pageNum = 1;
		int pageSize = ses.getUser().getSession().pageSize;
		if (formData.containsField("page")) {
			pageNum = formData.getNumberValueSilently("page", pageNum);
		}

		EmployeeDAO empDao = new EmployeeDAO(ses);
		ViewPage emps = empDao.findAllByName(keyword, pageNum, pageSize);
		setContent(new _POJOListWrapper(emps.getResult(), emps.getMaxPage(), emps.getCount(), emps.getPageNum(), lang));
	}

	@Override
	public void doPOST(_Session session, _WebFormData webFormData, LanguageType lang) {

	}

}
