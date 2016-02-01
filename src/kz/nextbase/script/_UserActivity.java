package kz.nextbase.script;

import java.util.Date;

import kz.flabs.dataengine.ActivityStatusType;
import kz.flabs.dataengine.IActivity;
import kz.flabs.dataengine.IDatabase;
import kz.flabs.dataengine.IUsersActivity;
import kz.flabs.users.User;

public class _UserActivity {
	private IUsersActivity act;
	private IActivity service_activity;
	private User user;

	public _UserActivity(IDatabase db, User currentUser) {

		this.user = currentUser;
	}

	public void postActivity(String agent, String text) {
		act.postSomeActivity("[" + agent + "] " + text, user);
	}

	public void postActivity(String agent, Exception e) {
		act.postSomeActivity("[" + agent + "] " + e.toString(), user);
	}

	public void postActivity(String text) {
		act.postSomeActivity(text, user);
	}

	public void postStartActivity(String viewtext, String userid, String service, String method, String parameters, String springServer,
	        String transaction, Date request_time) {
		service_activity.postStartOfActivity(viewtext, userid, service, method, parameters, springServer, transaction, request_time);
	}

	public void postEndOfActivity(ActivityStatusType type, Date returnTime, String comment, int processedRec, int processedSize, String transaction) {
		service_activity.postEndOfActivity(type, returnTime, comment, processedRec, processedSize, transaction);
	}

	public void postEndOfFailureActivity(ActivityStatusType type, Date returnTime, String comment, String transaction) {
		service_activity.postEndOfFailureActivity(type, returnTime, comment, transaction);
	}

}
