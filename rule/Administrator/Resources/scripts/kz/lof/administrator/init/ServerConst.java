package kz.lof.administrator.init;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import kz.lof.administrator.model.Application;
import kz.lof.administrator.model.Language;
import kz.lof.dataengine.jpa.constants.AppCode;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.env.Site;
import kz.lof.localization.LanguageCode;
import kz.lof.server.Server;

public class ServerConst {

	public static Language getLanguage(LanguageCode code) {
		Map<LanguageCode, String> lName = new HashMap<LanguageCode, String>();
		lName.put(LanguageCode.ENG, "English");
		lName.put(LanguageCode.KAZ, "Қазақша");
		lName.put(LanguageCode.RUS, "Русский");
		lName.put(LanguageCode.BUL, "Български");
		lName.put(LanguageCode.CHI, "中文");
		lName.put(LanguageCode.DEU, "Deutsche");
		lName.put(LanguageCode.POR, "Português");
		lName.put(LanguageCode.SPA, "Español");

		Language entity = new Language();
		entity.setName(code.toString());
		Map<LanguageCode, String> name = new HashMap<LanguageCode, String>();
		for (LanguageCode lc1 : Environment.langs) {
			name.put(lc1, lName.get(lc1));
		}
		entity.setLocalizedName(name);
		entity.setCode(code);

		return entity;
	}

	public static Application getApplication(Site site) {
		// System.out.println(site);
		Application entity = new Application();
		try {
			Class<?> c = Class.forName(site.name.toLowerCase() + ".init.AppConst");
			try {
				Field f = c.getDeclaredField("NAME");
				entity.setName((String) f.get(null));
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				entity.setName(site.name);
			}

			try {
				Field f = c.getDeclaredField("CODE");
				entity.setCode((AppCode) f.get(null));
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				entity.setCode(AppCode.CUSTOM);
			}

			try {
				Field f = c.getDeclaredField("DEFAULT_URL");
				entity.setDefaultURL((String) f.get(null));
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				entity.setDefaultURL("p");
			}

			Map<LanguageCode, String> localizedName = new HashMap<LanguageCode, String>();
			for (LanguageCode lc1 : Environment.langs) {
				try {
					Field f = c.getDeclaredField("NAME_" + lc1.name());
					localizedName.put(lc1, (String) f.get(null));
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
					localizedName.put(lc1, entity.getName());
				}
			}
			entity.setLocalizedName(localizedName);
		} catch (ClassNotFoundException e) {
			Map<LanguageCode, String> localizedName = new HashMap<LanguageCode, String>();
			for (LanguageCode lc1 : Environment.langs) {
				localizedName.put(lc1, site.name);
			}
			entity.setLocalizedName(localizedName);
			entity.setName(site.name);
			if (site.name.equalsIgnoreCase(EnvConst.ADMINISTRATOR_APP_NAME)) {
				entity.setCode(AppCode.ADMINISTRATOR);
				entity.setDefaultURL("p?id=user-view");
			} else {
				Server.logger.warningLogEntry(e.toString() + "=. Server will use default parameters for the application");
				entity.setCode(AppCode.CUSTOM);
			}
		}
		return entity;
	}
}
