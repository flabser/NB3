package kz.flabs.webrule;

import kz.flabs.webrule.constants.*;

public interface IRuleValue {
	ValueSourceType getSourceType();
	Enum getValueType();
	String getValue();
}
