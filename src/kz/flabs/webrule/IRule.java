package kz.flabs.webrule;

import java.util.Map;

import kz.flabs.exception.WebFormValueException;

public interface IRule {
	boolean isAnonymousAccessAllowed();

	void update(Map<String, String[]> fields) throws WebFormValueException;

	boolean save();

	String getRuleAsXML(String app);

	void plusHit();

	String getRuleID();

}
