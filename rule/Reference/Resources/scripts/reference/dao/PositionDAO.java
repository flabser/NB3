package reference.dao;

import java.util.UUID;

import kz.nextbase.script._Session;
import reference.model.Position;

public class PositionDAO extends ReferenceDAO<Position, UUID> {

	public PositionDAO(_Session session) {
		super(Position.class, session);
	}

}
