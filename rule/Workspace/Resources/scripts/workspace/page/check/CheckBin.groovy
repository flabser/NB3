package workspace.page.check

import kz.nextbase.script._Session
import kz.nextbase.script._WebFormData
import kz.nextbase.script.events._DoScript
import tender.webservices.ul.*

class CheckBin extends _DoScript{
	@Override
	public void doProcess(_Session session, _WebFormData formData, String lang) {
        String bin = formData.getValue("bin")
        String gos_reg_num = formData.getValue("gos_reg_num")

        ULServiceExport_GbdulDataServiceHttpServiceLocator l = new ULServiceExport_GbdulDataServiceHttpServiceLocator();
        GbdulDataService service = l.getULServiceExport_GbdulDataServiceHttpPort();
        GBDULJurInfoResponse response = service.getJurInfoByBin(new GBDULJurInfoByBinRequest(bin, "", new SystemInfoType()));
        System.out.println();
        if(gos_reg_num.trim().toLowerCase().equals(response.jurInfo.certNumber.trim().toLowerCase())){
            publishElement("msg", getLocalizedWord("'Номер свидетельства о гос.регистрации' не верный", lang))
            return;
        }
        publishElement("msg", "");
        publishElement("valid", response ? "true": "false");
        publishElement("certdate", response.jurInfo.regDate.format("dd.MM.yyyy"));
        publishElement("orgfullname", response.jurInfo.fullName.nameRu);
        publishElement("orgfullnamekaz", response.jurInfo.fullName.nameKz);
        publishElement("fulllegaladdress", response.jurInfo.jurAddress.country + ", " + response.jurInfo.jurAddress.region + ", " +
                response.jurInfo.jurAddress.city + ", " +  response.jurInfo.jurAddress.street + ", " +
                response.jurInfo.jurAddress.house + ", " + response.jurInfo.jurAddress.apartment);
        publishElement("rukfullname", response.jurInfo.headInfo.fullName);
        publishElement("certSeries", response.jurInfo.certSeries);
	}
}
