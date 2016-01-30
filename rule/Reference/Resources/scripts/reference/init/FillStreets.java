package reference.init;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import kz.flabs.dataengine.jpa.deploying.InitialDataAdapter;
import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Vocabulary;
import kz.flabs.util.Util;
import kz.nextbase.script._Session;
import kz.pchelka.env.EnvConst;
import reference.dao.LocalityDAO;
import reference.dao.StreetDAO;
import reference.model.Locality;
import reference.model.Street;

/**
 * Created by Kayra on 24/01/16.
 */

public class FillStreets extends InitialDataAdapter<Street, StreetDAO> {
	private static String excelFile = EnvConst.RESOURCES_DIR + File.separator + "streets.xls";

	@Override
	public List<Street> getData(_Session ses, LanguageType lang, Vocabulary vocabulary) {

		List<Street> entities = new ArrayList<Street>();
		LocalityDAO cDao = new LocalityDAO(ses);
		Locality d = cDao.findByName("Алматы");
		if (d != null) {
			File xf = new File(excelFile);
			if (xf.exists()) {
				WorkbookSettings ws = new WorkbookSettings();
				ws.setEncoding("Cp1252");
				Workbook workbook = null;
				try {
					workbook = Workbook.getWorkbook(xf, ws);
				} catch (BiffException | IOException e) {
					System.out.println(e);
				}
				Sheet sheet = workbook.getSheet(0);
				int rCount = sheet.getRows();
				for (int i = 2; i < rCount; i++) {
					int id = Util.convertStringToInt(sheet.getCell(0, i).getContents());
					String name = sheet.getCell(1, i).getContents();
					if (!name.equals("") && !name.equals("''") & id != 0) {
						Street entity = new Street();
						entity.setLocality(d);
						entity.setName(name);
						entity.setStreetId(id);
						entities.add(entity);
					}

				}
			} else {
				System.out.println("There is no appropriate file (" + excelFile + "). It will be loaded default data");
				String[] data = { "Champs Elysées", "La Rambla", "Fifth Avenue", "Via Appia", "Zeil", "Abbey Road", "Khao San", "Rua Augusta" };
				int count = 1;
				for (String val : data) {
					Street entity = new Street();
					entity.setLocality(d);
					entity.setName(val);
					entity.setStreetId(count);
					entities.add(entity);
					count++;
				}
			}

			Street entity = new Street();
			entity.setLocality(d);
			entity.setName("unknown");
			entity.setStreetId(0);
			entities.add(entity);
		}
		return entities;

	}

	@Override
	public Class<StreetDAO> getDAO() {
		return StreetDAO.class;
	}

}
