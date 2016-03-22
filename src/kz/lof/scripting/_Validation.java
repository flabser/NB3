package kz.lof.scripting;

import java.util.ArrayList;
import java.util.List;

import kz.lof.scriptprocessor.page.IOutcomeObject;

/**
 * 
 * 
 * @author Medet created 15-02-2016
 */

public class _Validation implements IOutcomeObject {
	public static final String VALUE_GREATER_THAN_ZERO = "^([0-9]*[1-9][0-9]*(\\.[0-9]+)?|[0]+\\.[0-9]*[1-9][0-9]*)$";

	private List<Error> errors = new ArrayList<>();

	public void addError(String field, String error, String msg) {
		errors.add(new Error(field, error, msg));
	}

	public void addError(String field, String regex) {
		errors.add(new Error(field, regex, ""));
	}

	public List<Error> getErrors() {
		return errors;
	}

	public boolean hasError() {
		return errors.size() > 0;
	}

	public class Error {

		private String field;
		private String error;
		private String message;

		public Error(String field, String error, String message) {
			this.field = field;
			this.error = error;
			this.message = message;
		}

		public String getField() {
			return field;
		}

		public String getError() {
			return error;
		}

		public String getMessage() {
			return message;
		}

		@Override
		public String toString() {
			return field + ", " + error + ", " + message;
		}
	}

	@Override
	public String toXML() {
		StringBuilder chunk = new StringBuilder(1000);
		chunk.append("<validation>");
		for (Error e : errors) {
			chunk.append("<error>");
			chunk.append("<field>" + e.field + "</field>");
			chunk.append("<error>" + e.error + "</error>");
			chunk.append("<message>" + e.message + "</message>");
			chunk.append("</error>");
		}
		chunk.append("</validation>");
		return chunk.toString();
	}

	@Override
	public Object toJSON() {
		return this;
	}

}
