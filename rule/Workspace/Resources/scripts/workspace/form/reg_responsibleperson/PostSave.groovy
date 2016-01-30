package workspace.form.reg_responsibleperson

import kz.nextbase.script._Document
import kz.nextbase.script._Session
import kz.nextbase.script.events._FormPostSave
import kz.nextbase.script.mail._Memo
import kz.nextbase.script.struct._Employer

class PostSave extends _FormPostSave {


    @Override
    void doPostSave(_Session ses, _Document doc) {

        log("Зарегистрировано ответственное лицо по загрузке объектов: " + doc.getViewText())

        def cdb = ses.getCurrentDatabase();
        def ma = ses.getMailAgent();
        def str = ses.getStructure();
        def supervisor = str.getAppUsersByRoles("administrator")
        def recipients = supervisor*.getEmail();

        def url = ses.getCurrentHost() + "/Structure/Provider?type=structure&id=responsibleperson&key=" + doc.getDocID();

        def body = "Зарегистрировано ответственное лицо по загрузке объектов. Перейдите по <a href='$url'>ссылке</a>  чтобы подтвердить регистрацию.";
        def memo = new _Memo("Уведомление", "Уведомление о регистрации ответственного лица по загрузке объектов", body, doc, false);
        ma.sendMail(recipients, memo);
    }
}
