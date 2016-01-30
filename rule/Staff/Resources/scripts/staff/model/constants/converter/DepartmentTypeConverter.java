package staff.model.constants.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import staff.model.constants.DepartmentType;

@Converter(autoApply = true)
public class DepartmentTypeConverter implements AttributeConverter<DepartmentType, Integer> {

	@Override
	public Integer convertToDatabaseColumn(DepartmentType lt) {
		return lt.getCode();
	}

	@Override
	public DepartmentType convertToEntityAttribute(Integer lt) {
		return DepartmentType.getType(lt);
	}
}
