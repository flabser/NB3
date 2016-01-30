package kz.flabs.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

public class Block implements IQueryFormulaBlocks{	
	public String blockText;
	public FormulaBlockType blockType;	
	public boolean paramatrized;		
	private ArrayList<FieldExpression>  expressionsList = new ArrayList<FieldExpression>();
	private HashMap<String, FieldExpression>  paramatrizedExpressList = new HashMap<String, FieldExpression>();	

	Block(FormulaBlocks formulaBlocks, String text){	
		Matcher sysFields = formulaBlocks.getFieldsPattern().matcher(text);
		FieldExpression fieldExpr = null;
		if (sysFields.lookingAt() || !formulaBlocks.hasCustomTable){
			fieldExpr = new FieldExpression(text);
			expressionsList.add(fieldExpr);
			blockType = FormulaBlockType.SYSTEM_FIELD_CONDITION;
		}else{ 
			fieldExpr = new CustomFieldExpression(text, formulaBlocks.customFieldsTable);
			expressionsList.add(fieldExpr);
			blockType = FormulaBlockType.CONDITION;					
		}	
		if(fieldExpr.paramatrized){
			paramatrizedExpressList.put(fieldExpr.parameterName, fieldExpr);
			paramatrized = true;
		}
		
	}

	public HashMap<String, FieldExpression> getParamatrizedExpressList() {
		return paramatrizedExpressList;
	}

    public List<FieldExpression> getExpressionList() {
        return expressionsList;
    }

	public String getContent(){
		String formula = "";
		for(FieldExpression value:expressionsList){
			formula += value.getContent() + " ";
		}
		return formula;
	 }
	
	public String toString(){
		return blockText + " ";
	}		
}