package kz.flabs.dataengine.jpa.deploying;

import java.util.List;

import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Vocabulary;
import kz.nextbase.script._Session;

/**
 * Created by Kayra on 30/12/15.
 */

public interface IInitialData<T, T1> {
	List<T> getData(_Session ses, LanguageType lang, Vocabulary vocabulary);

	Class<T1> getDAO();

	String getName();
}
