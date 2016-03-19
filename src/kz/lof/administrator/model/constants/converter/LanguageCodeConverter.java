package kz.lof.administrator.model.constants.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import kz.lof.localization.LanguageCode;

@Converter(autoApply = true)
public class LanguageCodeConverter implements AttributeConverter<LanguageCode, Integer> {

	@Override
	public Integer convertToDatabaseColumn(LanguageCode lt) {
		return lt.getCode();
	}

	@Override
	public LanguageCode convertToEntityAttribute(Integer lt) {
		return LanguageCode.getType(lt);
	}
}
