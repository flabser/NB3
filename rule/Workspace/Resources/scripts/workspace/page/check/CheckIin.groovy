package workspace.page.check

import kz.nextbase.script._Session
import kz.nextbase.script._WebFormData
import kz.nextbase.script.events._DoScript
import tender.webservices.ip.FullResponse_
import tender.webservices.ip.GBDFL2009Service_PortType
import tender.webservices.ip.GBDFL2009Service_ServiceLocator
import tender.webservices.ip.IINRequest_
import tender.webservices.ip.SystemInfo_

class CheckIin extends _DoScript{
	@Override
	public void doProcess(_Session session, _WebFormData formData, String lang) {
        String iin = formData.getValue("iin")
        GBDFL2009Service_ServiceLocator l = new GBDFL2009Service_ServiceLocator();
        GBDFL2009Service_PortType service = l.getGBDFL2009ServiceBinding();
        FullResponse_ fullResponse_ = service.getPersonByIIN(new IINRequest_("$iin", new SystemInfo_("1", "1", Calendar.getInstance(), "1", "1", "1", "")));

        publishElement("valid", fullResponse_ ? "true": "false");
        publishElement("fio", fullResponse_.currentFIO.getSurName() + " " + fullResponse_.currentFIO.getMiddleName() +
                fullResponse_.currentFIO.getFirstName())
        publishElement("org_name", fullResponse_.commonInfo.capableCourtCode)
        publishElement("address", fullResponse_.currentAddress.countryName + ", " + fullResponse_.currentAddress.districtName +
                ", " + fullResponse_.currentAddress.regionName + ", " + fullResponse_.currentAddress.cityName + ", " +
                ", " + fullResponse_.currentAddress.streetName + ", " + fullResponse_.currentAddress.buildingNumber +
                ", " + fullResponse_.currentAddress.flatNumber);
        publishElement("phone", "")

	}
}
