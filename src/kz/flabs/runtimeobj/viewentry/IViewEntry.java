package kz.flabs.runtimeobj.viewentry;


import java.math.BigDecimal;

public interface IViewEntry {
	
	StringBuffer toXML();
	BigDecimal getViewNumberValue();
	
	
}
