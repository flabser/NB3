package reference.model.constants.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import reference.model.constants.RegionType;

@Converter(autoApply = true)
public class RegionTypeConverter implements AttributeConverter<RegionType, Integer> {

	@Override
	public Integer convertToDatabaseColumn(RegionType lt) {
		return lt.getCode();
	}

	@Override
	public RegionType convertToEntityAttribute(Integer lt) {
		return RegionType.getType(lt);
	}
}
