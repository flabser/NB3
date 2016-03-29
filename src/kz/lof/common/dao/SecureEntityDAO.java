package kz.lof.common.dao;

import java.util.UUID;

import kz.lof.dataengine.jpa.DAO;
import kz.lof.dataengine.jpa.SecureAppEntity;
import kz.lof.scripting._Session;

public class SecureEntityDAO extends DAO<SecureAppEntity, UUID> {

	public SecureEntityDAO(_Session session) {
		super(SecureAppEntity.class, session);
	}
}
