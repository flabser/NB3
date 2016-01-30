package kz.flabs.dataengine.jpa.util;

import java.sql.Types;
import java.util.UUID;

import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.DirectCollectionMapping;
import org.eclipse.persistence.mappings.ManyToOneMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;

public class UUIDConverter implements Converter {
	private static final long serialVersionUID = 1L;

	@Override
	public Object convertObjectValueToDataValue(Object objectValue, Session session) {
		UUID id = (UUID) objectValue;

		return id;
	}

	@Override
	public UUID convertDataValueToObjectValue(Object dataValue, Session session) {
		if (dataValue instanceof String) {
			return UUID.fromString((String) dataValue);
		} else {
			return (UUID) dataValue;
		}
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public void initialize(DatabaseMapping mapping, Session session) {
		final DatabaseField field;
		if (mapping instanceof DirectCollectionMapping) {
			field = ((DirectCollectionMapping) mapping).getDirectField();
		} else {
			field = mapping.getField();
		}

		field.setSqlType(Types.OTHER);
		field.setColumnDefinition("UUID");

		for (DatabaseMapping m : mapping.getDescriptor().getMappings()) {
			assert OneToOneMapping.class.isAssignableFrom(ManyToOneMapping.class);
			if (m instanceof OneToOneMapping) {
				for (DatabaseField field1 : ((OneToOneMapping) m).getForeignKeyFields()) {
					field1.setSqlType(Types.OTHER);
					field.setColumnDefinition("UUID");
					// field.setTypeName("uuid");
				}
			}
		}
	}
}
