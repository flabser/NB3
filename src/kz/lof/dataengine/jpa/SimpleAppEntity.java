package kz.lof.dataengine.jpa;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import kz.lof.dataengine.jpa.util.UUIDConverter;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;
import org.eclipse.persistence.annotations.UuidGenerator;

@MappedSuperclass
@Converter(name = "uuidConverter", converterClass = UUIDConverter.class)
@UuidGenerator(name = "uuid-gen")
public abstract class SimpleAppEntity implements ISimpleAppEntity<UUID> {
	@Id
	@GeneratedValue(generator = "uuid-gen")
	@Convert("uuidConverter")
	@Column(name = "id", nullable = false)
	protected UUID id;

	@Override
	public void setId(UUID id) {
		this.id = id;
	}

	@Override
	public UUID getId() {
		return id;
	}

}
