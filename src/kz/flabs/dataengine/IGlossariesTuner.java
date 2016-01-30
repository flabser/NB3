package kz.flabs.dataengine;

import java.util.ArrayList;
import kz.flabs.webrule.Lang;

public interface IGlossariesTuner {
	ArrayList<String> getSupportedLangs();
	boolean addLang(Lang lang);
	
}
