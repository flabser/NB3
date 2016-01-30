package workspace.page.institution
import kz.nextbase.script._Session
import kz.nextbase.script._WebFormData
import kz.nextbase.script.events._DoScript
import nextbase.groovy.*

class DoScript extends _DoScript {
	
	@Override
	public void doProcess(_Session session, _WebFormData formData, String lang) {
		//println(formData)
        def db = session.getCurrentDatabase()
        def keyword = formData.getValue("keyword");
        def page = formData.getNumberValueSilently("page", 1);
        def col = null;
        def formula = "(form='kgu' or form='kgp' or form='gkkp' or form='ao' or form='too' or form='subsidiaries') and registration='1'";
        def search_condition = "form in ('kgu', 'kgp', 'gkkp', 'ao', 'too', 'subsidiaries')";
        String[] filters = ["","", "","","","", search_condition]

        if (formData.containsField("keyword") && keyword.trim() != ""){
            col = db.search(keyword, page, filters)
        } else{
            col = db.getCollectionOfDocuments(formula, page, false)
        }

        setContent(col)
	}
}