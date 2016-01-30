package kz.flabs.scriptprocessor.handler;

import java.util.ArrayList;

import kz.flabs.webrule.scheduler.DaysOfWeek;
import kz.pchelka.scheduler.DaemonRule;
import kz.pchelka.scheduler.DaemonType;
import kz.pchelka.scheduler.IProcessInitiator;


public class SchedHandlerDaemonRule extends DaemonRule{

	@Override
	public void init(IProcessInitiator owner) {
		this.owner = owner;
		daemonType =  DaemonType.HANDLER;		
	}

	@Override
	public boolean scriptIsValid() {
		return true;
	}

	@Override
	public ArrayList<DaysOfWeek> getDaysOfWeek() {
		// TODO Auto-generated method stub
		return null;
	}

}
