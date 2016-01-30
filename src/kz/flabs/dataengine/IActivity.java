package kz.flabs.dataengine;

import java.util.Date;

import kz.flabs.runtimeobj.viewentry.ViewEntryCollection;


public interface IActivity {
	IDatabase getParentDatabase();
	
	int postStartOfActivity(String viewText, String userID, String nameOfService, String nameOfMethod, String parameters, String springServer, String transaction, Date request_time);
	
	int postEndOfActivity(ActivityStatusType type, Date returnTime,  String comment, int processedRec, int processedSize, String transaction);
	int postEndOfFailureActivity(ActivityStatusType type, Date returnTime,  String comment, String transaction);
	
	
	ViewEntryCollection getActivities(int offset, int pageSize);
	ViewEntryCollection getActivities(int offset, int pageSize, String userID);
    ViewEntryCollection getActivities(int offset, int pageSize, String userID, String serviceAndMethodName, Date dateFrom, Date dateTo, int totalsFrom, int totalsTo, boolean errorsOnly);
    StringBuffer getActivitiesAsXML(int offset, int pageSize, String userID, String serviceAndMethodName, Date dateFrom, Date dateTo, int totalsFrom, int totalsTo, boolean errorsOnly, String springServer, int diffTimeFrom, int diffTimeTo);
    int getActivitiesCount(String userID, String serviceAndMethodName, Date dateFrom, Date dateTo, int totalsFrom, int totalsTo, boolean errorsOnly, String springServer, int diffTimeFrom, int diffTimeTo);

}
