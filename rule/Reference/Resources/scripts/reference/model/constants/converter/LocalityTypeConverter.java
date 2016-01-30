package reference.model.constants.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import reference.model.constants.LocalityType;

@Converter(autoApply = true)
public class LocalityTypeConverter implements AttributeConverter<LocalityType, Integer> {

	@Override
	public Integer convertToDatabaseColumn(LocalityType lt) {
		return lt.getCode();
	}

	@Override
	public LocalityType convertToEntityAttribute(Integer lt) {
		return LocalityType.getType(lt);
	}
}
