package kz.flabs.webrule;


public interface IRule {
	boolean isAnonymousAccessAllowed();

	void plusHit();

	String getRuleID();

}
