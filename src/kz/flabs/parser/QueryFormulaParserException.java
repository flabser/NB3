package kz.flabs.parser;

public class QueryFormulaParserException extends Exception{
	public FormulaParserErrorType id;
	public String user;
	
	private static final long serialVersionUID = 4762010135613823296L;
	private String errorTextRus;
	
	
	public QueryFormulaParserException(FormulaParserErrorType error, String parameter, String ruleID) {
		super();
		id = error;
		String rule = "";
		if (ruleID != null)rule = "(ruleid=" + ruleID + ")";
		switch(id){ 
		case PARAMETER_IS_NULL:
			errorTextRus = "Query parameter \"" + parameter + "\" has not resolved" + rule;
			break;
		case PARAMETERS_SET_ERROR:
			errorTextRus = "Query parameter \"" + parameter + "\" has not resolved, parameters set error" + rule;
			break;
		case UNKNOWN_ERROR:
			errorTextRus = "Unknown parser error";
		}		
	}
	
	
	public String getMessage(){
		return errorTextRus;
	}
	
	public String toString(){
		return errorTextRus;
	}
}
