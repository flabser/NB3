package kz.flabs.parser;

import kz.flabs.webrule.constants.FieldType;

public class CustomFieldExpression extends FieldExpression {
	private String customFieldsTable;

	CustomFieldExpression(String text, String customFieldsTable) {
		super(text);	
		this.customFieldsTable = customFieldsTable;
	}

	public String getContent(){	
		String formula = openingElement;
		if (valueIsQuoted){
			if (isSuggestionRequest){
			    if(fieldType == FieldType.DATETIME){
			        formula += customFieldsTable + ".name = '" + fieldName + "' and cast (" + customFieldsTable + ".valueasdate as text) LIKE '%" + fieldValue + "%' ";
			    } else if (fieldType == FieldType.GLOSSARY) {
			    	formula += customFieldsTable + ".name = '" + fieldName + "' and CAST(" + customFieldsTable + ".valueasglossary as text) like '%" + fieldValue + "%' ";
			    
			    }else{
			        formula += customFieldsTable + ".name = '" + fieldName + "' and " + customFieldsTable + ".value LIKE '%" + fieldValue + "%' ";
			    }
			}else{
				//formula += customFieldsTable + ".name = '" + fieldName + "' and " + customFieldsTable + ".value " + operand + "'" + fieldValue + "'";
				if(fieldType == FieldType.DATETIME){
				    String sqlfunc = "";
				    int indexBracket = fieldName.indexOf("(");

				    if( indexBracket != -1 ){
	                    sqlfunc = fieldName.substring(0, indexBracket);
	                    fieldName = fieldName.substring(indexBracket+1, fieldName.length()-1);
	                    
	                    formula += customFieldsTable + ".name = '" + fieldName + "' and " + sqlfunc +"("+ customFieldsTable + ".valueasdate)" + operand + "'" + fieldValue + "'";
	                }else{
	                    formula += customFieldsTable + ".name = '" + fieldName + "' and " + customFieldsTable + ".valueasdate " + operand + "'" + fieldValue + "'";
	                }
				}else{
					if(operand.trim().equalsIgnoreCase("in")){
						formula += customFieldsTable + ".name = '" + fieldName + "' and " + customFieldsTable + ".value " + operand + "(" + fieldValue + ")";
					}else{
						formula += customFieldsTable + ".name = '" + fieldName + "' and " + customFieldsTable + ".value " + operand + "'" + fieldValue + "'";
					}
	            }
			}
		}else{
			if(fieldType == FieldType.NUMBER){
				formula += customFieldsTable + ".name = '" + fieldName + "' and " + customFieldsTable + ".valueasnumber" + operand + fieldValue + " ";
			}else if(fieldType == FieldType.GLOSSARY){
				formula += customFieldsTable + ".name = '" + fieldName + "' and " + customFieldsTable + ".valueasglossary" + operand + fieldValue + " ";
			}else if(fieldType == FieldType.DATETIME){
			    formula += customFieldsTable + ".name = '" + fieldName + "' and " + customFieldsTable + ".valueasdate " + operand + "'" + fieldValue + "' ";
			}else{
				formula += customFieldsTable + ".name = '" + fieldName + "' and " + customFieldsTable + ".value" + operand + fieldValue + " ";
			}
		}
		return formula + closingElement;
	}	
}
