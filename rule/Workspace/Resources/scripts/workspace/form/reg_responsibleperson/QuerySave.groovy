package workspace.form.reg_responsibleperson

import kz.flabs.exception.WebFormValueException
import kz.nextbase.script._Document
import kz.nextbase.script._Exception
import kz.nextbase.script._ExceptionType
import kz.nextbase.script._Session
import kz.nextbase.script._WebFormData
import kz.nextbase.script.events._FormQuerySave
import kz.nextbase.script.struct._Employer
import kz.nextbase.script.struct._EmployerStatusType
import kz.nextbase.script.struct._UserApplicationProfile

class QuerySave extends _FormQuerySave {

	@Override
	public void doQuerySave(_Session session, _Document doc, _WebFormData webFormData, String lang) {

		def cdb = session.getCurrentDatabase();
		boolean v = validate(webFormData)
		if(v == false){
			stopSave()
			return
		}

        def emp  = (_Employer)doc;

        emp.setForm("responsibleperson")
        emp.setFullName(webFormData.getValueSilently("fio"));
        emp.setShortName(webFormData.getValueSilently("institution_name").replace("'","\""));
        emp.setUserID(webFormData.getValueSilently("login"));
        emp.setPassword(webFormData.getValueSilently("password"));
        emp.setPasswordHash(webFormData.getValueSilently("password"))
        emp.setPhone(webFormData.getValueSilently("phone"));
        emp.setEmail(webFormData.getValueSilently("email"));
        emp.setMessenger("");
        emp.setObl(webFormData.getValueSilently("institution"));
        emp.setBirthDate(new Date());
        emp.setDepID("1");
        emp.addFile("rtfcontent", webFormData);

        // неподтвержденный  админом, не имеет доступа к системе
        if(emp.isNewDoc){
            emp.setStatus(_EmployerStatusType.FIRED);
            emp.clearEnabledAppsList();
            //emp.addEnabledApp(new _UserApplicationProfile("Accountant","0"));
            //emp.setListOfRoles(["contractor#Accountant"] as String[]);
        }

        emp.setViewText(webFormData.getValueSilently("fio"))
        emp.addViewText(webFormData.getValueSilently("phone"))
        emp.addViewText("inactive");
        emp.addViewText(emp.getShortName())

        localizedMsgBox(getLocalizedWord("Регистрация прошла успешно. <br/> Ждите подтверждения администратора.",lang))
        setRedirectURL("Provider?type=page&id=workspace");
	}

	def validate(_WebFormData webFormData){

        if (webFormData.getValueSilently("password") != webFormData.getValueSilently("reenterpassword")){
            localizedMsgBox("Введенные пароли не совпадают")
            return false
        }
		if (webFormData.getValueSilently("fio") == ""){
            localizedMsgBox("Поле \"Ф.И.О\" не заполнено.")
            return false
        }
        if (webFormData.getValueSilently("login") == ""){
            localizedMsgBox("Поле \"Логин\" не заполнено.")
            return false
        }

        if (webFormData.getValueSilently("password") == ""){
            localizedMsgBox("Поле \"Пароль\" не заполнено.")
            return false
        }
        if (webFormData.getValueSilently("email") == ""){
            localizedMsgBox("Поле \"Электронный адрес\" не заполнено.")
            return false
        }

        if (webFormData.getValueSilently("institution") == ""){
            localizedMsgBox("Поле \"Наименование учреждения\" не выбрано.")
           // return false
        }

		return true
	}
}
