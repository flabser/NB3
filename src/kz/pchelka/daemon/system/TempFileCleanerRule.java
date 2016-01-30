package kz.pchelka.daemon.system;

import kz.flabs.webrule.scheduler.DaysOfWeek;

import java.util.ArrayList;
import java.util.Calendar;

public class TempFileCleanerRule extends SystemDaemonRule{
	Calendar nextStart;
	

	@Override
	public int getMinuteInterval() {	
		return 30;
	}

	@Override
	public String getClassName() {
		return "kz.pchelka.daemon.system.TempFileCleaner";
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
