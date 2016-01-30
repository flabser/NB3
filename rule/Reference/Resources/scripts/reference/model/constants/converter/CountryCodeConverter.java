package reference.model.constants.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import reference.model.constants.CountryCode;

@Converter(autoApply = true)
public class CountryCodeConverter implements AttributeConverter<CountryCode, Integer> {

	@Override
	public Integer convertToDatabaseColumn(CountryCode lt) {
		return lt.getCode();
	}

	@Override
	public CountryCode convertToEntityAttribute(Integer lt) {
		return CountryCode.getType(lt);
	}
}
