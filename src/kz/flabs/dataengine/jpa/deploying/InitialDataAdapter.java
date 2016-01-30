package kz.flabs.dataengine.jpa.deploying;

import java.util.List;

import kz.flabs.localization.LanguageType;
import kz.flabs.localization.Vocabulary;
import kz.nextbase.script._Session;

/**
 * Created by Kayra on 30/12/15.
 */

public abstract class InitialDataAdapter<T, T1> implements IInitialData<T, T1> {

	@Override
	public abstract List<T> getData(_Session ses, LanguageType lang, Vocabulary vocabulary);

	@Override
	public abstract Class<T1> getDAO();

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
