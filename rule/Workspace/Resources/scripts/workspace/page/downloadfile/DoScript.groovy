package workspace.page.downloadfile

import kz.nextbase.script._Session
import kz.nextbase.script._WebFormData
import kz.nextbase.script.events._DoScript

class DoScript extends _DoScript{
	@Override
	public void doProcess(_Session session, _WebFormData formData, String lang) {

        def returnFileName = formData.getEncodedValueSilently("filename");
        if(returnFileName == "ais_pattern.xls")
            returnFileName = "Шаблон для АИС.xls"
        else if(returnFileName == "responsible_person_manual.docx")
            returnFileName = "Инструкция для Ответственого по загрузке данных-2.docx";

		showFile(new File("").getAbsolutePath() + "${File.separator}webapps${File.separator}Workspace${File.separator}"+
                "content${File.separator}${formData.getEncodedValueSilently("filename")}", returnFileName);
        log("Выгрузка файла " + returnFileName);
	}
}
