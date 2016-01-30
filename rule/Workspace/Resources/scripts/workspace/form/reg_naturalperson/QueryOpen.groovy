package workspace.form.reg_naturalperson
import kz.nextbase.script._Document
import kz.nextbase.script._Exception
import kz.nextbase.script._Session
import kz.nextbase.script._WebFormData
import kz.nextbase.script.actions._Action
import kz.nextbase.script.actions._ActionBar
import kz.nextbase.script.actions._ActionType
import kz.nextbase.script.constants._DocumentModeType
import kz.nextbase.script.events._FormQueryOpen

class QueryOpen extends _FormQueryOpen {

	
	@Override
	public void doQueryOpen(_Session ses, _WebFormData webFormData, String lang) {
		def user = ses.getCurrentAppUser()
		
		
		def actionBar = ses.createActionBar();
		actionBar.addAction(new _Action(getLocalizedWord("Сохранить и закрыть",lang),getLocalizedWord("Сохранить и закрыть",lang),_ActionType.SAVE_AND_CLOSE))
		//actionBar.addAction(new _Action(getLocalizedWord("Закрыть",lang),getLocalizedWord("Закрыть без сохранения",lang),_ActionType.CLOSE))
		publishElement(actionBar)

        publishValue("title",getLocalizedWord("Регистрация Физических лиц", lang))
		publishEmployer("author", ses.getCurrentAppUser().getUserID())
	}


	@Override
	public void doQueryOpen(_Session ses, _Document doc, _WebFormData webFormData, String lang) {
		def user = ses.getCurrentAppUser()
        publishValue("title",getLocalizedWord("Регистрация Физических лиц", lang))
		def nav = ses.getPage("outline", webFormData)
		publishElement(nav)
		
		def actionBar = new _ActionBar(ses)
		
		if(doc.getEditMode() == _DocumentModeType.EDIT){
			actionBar.addAction(new _Action(getLocalizedWord("Сохранить и закрыть",lang),getLocalizedWord("Сохранить и закрыть",lang),_ActionType.SAVE_AND_CLOSE))
		}
		
		actionBar.addAction(new _Action(getLocalizedWord("Закрыть",lang),getLocalizedWord("Закрыть без сохранения",lang),_ActionType.CLOSE))
		publishElement(actionBar)
		
		publishValue("title",getLocalizedWord("Акционерное общество", lang) + " ")
		
		publishEmployer("author",doc.getAuthorID())
		
		publishValue("orgfullname",doc.getValueString("orgfullname"))
		publishValue("orgfullnamekaz",doc.getValueString("orgfullnamekaz"))
		publishValue("fulllegaladdress",doc.getValueString("fulllegaladdress"))
		publishValue("fullactualaddress",doc.getValueString("fullactualaddress"))
		publishValue("phone",doc.getValueString("phone"))
		publishValue("fax",doc.getValueString("fax"))
		publishValue("email",doc.getValueString("email"))
		publishValue("bin",doc.getValueString("bin"))
		publishValue("rukfullname",doc.getValueString("rukfullname"))
		publishValue("glavbuhfullname",doc.getValueString("glavbuhfullname"))
		publishValue("okpo",doc.getValueString("okpo"))
        if(doc.getField('oked')){
            publishGlossaryValue("oked",doc.getValueGlossary("oked"))
        }
        if(doc.getField('typeactivity')){
            publishGlossaryValue("typeactivity",doc.getValueGlossary("typeactivity"))
        }
		publishValue("orgregdate",doc.getValueString("orgregdate"))
		publishValue("orgregnumber",doc.getValueString("orgregnumber"))
		publishValue("regdateorder",doc.getValueString("regdateorder"))
		publishValue("numberorder",doc.getValueString("numberorder"))

		publishValue("stuffcount",doc.getValueString("stuffcount"))
		publishValue("privilege",doc.getValueString("privilege"))
		
		publishValue("indexinvestments",doc.getValueString("indexinvestments"))
		publishValue("profitability",doc.getValueString("profitability"))
		publishValue("laborproductivity",doc.getValueString("laborproductivity"))
		
		
		try{
			publishAttachment("rtfcontent","rtfcontent")
		}catch(_Exception e){

		}
	}
}