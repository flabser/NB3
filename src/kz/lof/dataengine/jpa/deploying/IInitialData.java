package kz.lof.dataengine.jpa.deploying;

import java.util.List;

import kz.flabs.localization.LanguageCode;
import kz.flabs.localization.Vocabulary;
import kz.lof.scripting._Session;

/**
 * Created by Kayra on 30/12/15.
 */

public interface IInitialData<T, T1> {
	List<T> getData(_Session ses, LanguageCode lang, Vocabulary vocabulary);

	Class<T1> getDAO();

	String getName();
}
