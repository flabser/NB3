package kz.lof.administrator.model.constants.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import kz.lof.dataengine.jpa.constants.AppCode;

@Converter(autoApply = true)
public class AppCodeConverter implements AttributeConverter<AppCode, Integer> {

	@Override
	public Integer convertToDatabaseColumn(AppCode lt) {
		return lt.getCode();
	}

	@Override
	public AppCode convertToEntityAttribute(Integer lt) {
		return AppCode.getType(lt);
	}
}
