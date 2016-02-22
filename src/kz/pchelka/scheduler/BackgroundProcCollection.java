package kz.pchelka.scheduler;

import java.util.ArrayList;
import java.util.HashMap;

import kz.flabs.util.Util;
import kz.lof.server.Server;

public class BackgroundProcCollection {
	private static ArrayList<IDaemon> activProcList = new ArrayList<IDaemon>();
	private static ArrayList<IDaemon> procList = new ArrayList<IDaemon>();
	private static HashMap<String, IDaemon> procMap = new HashMap<String, IDaemon>();

	public synchronized void addActivProcess(IDaemon daemon) {
		if (addProcess(daemon)) {
			Server.logger.debugLogEntry("Init scheduler task (process=\"" + daemon.getID() + "\")");
			activProcList.add(daemon);
		}
	}

	public boolean addProcess(IDaemon daemon) {
		String key = daemon.getID();

		if (!procMap.containsKey(key)) {
			procMap.put(key, daemon);
			procList.add(daemon);
			return true;
		} else {
			return false;
		}

	}

	public ArrayList<IDaemon> getActivProcList() {
		return activProcList;
	}

	public IDaemon getProcess(String procID) {
		return procMap.get(procID);
	}

	public String getProcessAsXMLPiece() {
		StringBuffer result = new StringBuffer(1000);
		for (IDaemon process : procList) {
			String lst = Util.convertDataTimeToString(process.getLastSuccessTime());
			String nt = Util.convertDataTimeToString(process.getStartTime());
			DaemonStatusType stat = process.getStatus();

			result.append("<entry><id>" + process.getID() + "</id>" + "<type>" + process.getDeamonType() + "</type>" +
					// "<app>" + process.getApp() + "</app>" +
					"<trigger>" + process.getTriggerType() + "</trigger>" + "<lastsuccess>" + lst + "</lastsuccess>"
					+ "<nexttime>" + nt + "</nexttime>" + "<successrun>" + process.getSuccessRunCount()
					+ "</successrun>" + "<runhistory>" + process.getSuccessRunHistory() + "</runhistory>" + "<status>"
					+ stat + "</status></entry>");
		}
		return result.toString();
	}

	public int size() {
		return procList.size();
	}
}
