package kz.flabs.webrule.query;

import kz.flabs.dataengine.IQueryFormula;
import kz.flabs.webrule.RuleValue;
import kz.flabs.webrule.constants.QueryType;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.constants.TagPublicationFormatType;
import kz.lof.rule.page.IElement;

public interface IQueryRule extends IElement {

	IQueryFormula getQueryFormula();

	QueryType getQueryType();

	QueryFieldRule[] getFields();

	String getFieldsCondition();

	TagPublicationFormatType getGroupByPublicationFormat();

	RuleValue getQuery();

	RunMode getCacheMode();

}
