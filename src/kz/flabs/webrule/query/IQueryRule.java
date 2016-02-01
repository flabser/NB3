package kz.flabs.webrule.query;

import kz.flabs.dataengine.IQueryFormula;
import kz.flabs.parser.FormulaBlocks;
import kz.flabs.webrule.RuleValue;
import kz.flabs.webrule.constants.QueryType;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.constants.TagPublicationFormatType;
import kz.flabs.webrule.page.IElement;

public interface IQueryRule extends IElement {
	FormulaBlocks getQueryFormulaBlocks();

	IQueryFormula getQueryFormula();

	QueryType getQueryType();

	QueryFieldRule[] getFields();

	String getFieldsCondition();

	TagPublicationFormatType getGroupByPublicationFormat();

	RuleValue getQuery();

	RunMode getCacheMode();

}
