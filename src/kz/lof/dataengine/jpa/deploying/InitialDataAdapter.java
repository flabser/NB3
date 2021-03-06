package kz.lof.dataengine.jpa.deploying;

import java.util.List;

import kz.flabs.localization.Vocabulary;
import kz.lof.localization.LanguageCode;
import kz.lof.scripting._Session;

/**
 * Created by Kayra on 30/12/15.
 */

public abstract class InitialDataAdapter<T, T1> implements IInitialData<T, T1> {

	@Override
	public abstract List<T> getData(_Session ses, LanguageCode lang, Vocabulary vocabulary);

	@Override
	public abstract Class<T1> getDAO();

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
