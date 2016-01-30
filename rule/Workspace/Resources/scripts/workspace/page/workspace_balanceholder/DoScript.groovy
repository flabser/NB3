package workspace.page.workspace_balanceholder
import kz.nextbase.script._Session
import kz.nextbase.script._WebFormData
import kz.nextbase.script.events._DoScript
import nextbase.groovy.*

class DoScript extends _DoScript {
	
	@Override
	public void doProcess(_Session session, _WebFormData formData, String lang) {
		println(formData)
		def page = 1;
		if (formData.containsField("page") && formData.getValue("page")){
			page = Integer.parseInt(formData.getValue("page"))
		}
		def formula = "form='legalentities'  or form='ao'  or form='gkkp'  or form='kgp'  or form='kgu'  or form='subsidiaries'  or form='too'";
		def db = session.getCurrentDatabase()
		def filters = []
		def sorting = []
		def col = db.getCollectionOfDocuments(formula, page, true, true)
		setContent(col)
	}
}