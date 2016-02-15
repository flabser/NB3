package kz.nextbase.script;


import java.util.ArrayList;
import java.util.List;


public class _Validation {

    private List<Error> errors = new ArrayList<>();

    public void addError(String field, String error, String msg) {
        errors.add(new Error(field, error, msg));
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

        public String toString() {
            return field + ", " + error + ", " + message;
        }
    }
}
