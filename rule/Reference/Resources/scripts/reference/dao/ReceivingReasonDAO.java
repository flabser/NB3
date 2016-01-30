package reference.dao;

import java.util.UUID;

import kz.nextbase.script._Session;
import reference.model.ReceivingReason;

public class ReceivingReasonDAO extends ReferenceDAO<ReceivingReason, UUID> {

	public ReceivingReasonDAO(_Session session) {
		super(ReceivingReason.class, session);
	}

}
