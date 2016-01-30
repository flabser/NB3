package workspace.form.reg_legalentity

import kz.nextbase.script.*
import kz.nextbase.script.events.*
import kz.nextbase.script.struct._Employer
import kz.nextbase.script.struct._UserApplicationProfile

class QuerySave extends _FormQuerySave {

    @Override
    public void doQuerySave(_Session session, _Document doc, _WebFormData webFormData, String lang) {

        println(webFormData)

        boolean v = validate(webFormData)
        if(v == false){
            stopSave()
            return
        }

        def emp  = (_Employer)doc;

        emp.setForm("legalentity")
        emp.setFullName(webFormData.getValueSilently("orgfullname"));
        emp.setShortName(webFormData.getValueSilently("rukfullname"));
        emp.setSign(webFormData.getValueSilently("executive"))
        emp.setUserID(webFormData.getValueSilently("login"));
        emp.setPassword(webFormData.getValueSilently("password"));
        emp.setPasswordHash(webFormData.getValueSilently("password"))
        emp.setPhone(webFormData.getValueSilently("phone"));
        emp.setEmail(webFormData.getValueSilently("email"));
        emp.setMessenger("");
        // ? Номер свидетельства гос. регистрации
        emp.setIndex(webFormData.getValueSilently("gos_reg_num"));
        //  Дата гос. регистрации
        emp.setBirthDate(_Helper.convertStringToDate(webFormData.getValueSilently("gos_reg_date")));
        // Полный юр адрес
        emp.setComment(webFormData.getValueSilently("fulllegaladdress"));
        emp.setDepID("1");

        emp.addFile("rtfcontent", webFormData);

        localizedMsgBox(getLocalizedWord("Документ сохранен",lang))
        //returnURL.changeParameter("page", "0");

        emp.setViewText(webFormData.getValueSilently("orgfullname"))
        emp.addViewText(webFormData.getValueSilently("orgfullnamekaz"))
        emp.addViewText(webFormData.getValueSilently("bin"));
        emp.addViewText(webFormData.getValueSilently("fulllegaladdress"))
        emp.addViewText(webFormData.getValueSilently("gos_reg_num"));
        emp.setViewNumber(webFormData.getValueSilently("bin") as BigDecimal);

        emp.clearEnabledAppsList();
        emp.addEnabledApp(new _UserApplicationProfile("Tender","0"));

        emp.setListOfRoles(["rent#Tender", "tender#Tender", "user#Tender"] as String[]);
        def str = session.getStructure();
        def db = session.getCurrentDatabase();

        setRedirectURL("Provider?type=page&id=workspace") ;
    }

    def validate(_WebFormData webFormData){

        if (webFormData.getValueSilently("password") != webFormData.getValueSilently("reenterpassword")){
            localizedMsgBox("Введенные пароли не совпадают")
            return false
        }
        if (webFormData.getValueSilently("orgfullname") == ""){
            localizedMsgBox("Поле \"Наименование\" не заполнено.")
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
        if (webFormData.getValueSilently("gos_reg_num") == ""){
            localizedMsgBox("Поле \"Номер свидетельства о гос.регистрации\" не заполнено.")
            return false
        }
        return true
    }
}
