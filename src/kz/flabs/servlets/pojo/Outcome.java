package kz.flabs.servlets.pojo;

import java.util.ArrayList;
import java.util.List;

public class Outcome {
	private OutcomeType type = OutcomeType.OK;
	private List<String> message = new ArrayList<String>();

	public OutcomeType getType() {
		return type;
	}

	public Outcome setType(OutcomeType type) {
		this.type = type;
		return this;
	}

	public Outcome addMessage(String message) {
		this.message.add(message);
		return this;
	}

	public Outcome setErrorMessage(Exception e) {
		return setMessage(e.getLocalizedMessage());
	}

	public Outcome setMessage(String s) {
		message.clear();
		addMessage(s);
		return this;
	}

}
