package workspace.page.workspace

import kz.nextbase.script.*
import kz.nextbase.script.events._DoScript

class DoScript extends _DoScript{
	@Override
	public void doProcess(_Session session, _WebFormData formData, String lang) {

		def ent = session.getAppEntourage()		
		publishElement("serverversion", ent.getServerVersion())
		publishElement("build", ent.getBuildTime())
		publishElement("org", ent.getGeneralName())
		publishElement("img", ent.getLogoImg())
		publishElement("eds", ent.edsIsOn())
		publishElement("appname", ent.getAppName())		
		publishElement("availablelangs", ent.getAvailableLangs())
		publishElement("availableskins", ent.getAvailableSkins())
		publishElement("availableapps", ent.getAvailableApps())
		
	}
}
