package kz.flabs.dataengine.jpa;

import java.util.UUID;

public interface ISimpleAppEntity {

	void setId(UUID id);

	UUID getId();

}
