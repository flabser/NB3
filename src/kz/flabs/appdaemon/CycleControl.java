package kz.flabs.appdaemon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import kz.flabs.appenv.AppEnv;
import kz.flabs.dataengine.Const;
import kz.flabs.exception.DocumentAccessException;
import kz.flabs.exception.DocumentException;
import kz.flabs.runtimeobj.document.task.Control;
import kz.flabs.runtimeobj.document.task.Task;
import kz.pchelka.scheduler.AbstractDaemon;
import kz.pchelka.scheduler.IProcessInitiator;
import kz.pchelka.server.Server;

public class CycleControl extends AbstractDaemon implements Const {
		

	@Override
	public int process(IProcessInitiator processOwner) {
		AppEnv env = (AppEnv) processOwner;
		try {
			ArrayList<Task> col = env.getDataBase().getTasks().getTasksByCondition("(TASKTYPE = 1 OR TASKTYPE = 3) AND CYCLECONTROL > 1 AND ISOLD = 0 AND CTRLDATE < NOW()", new HashSet<String>(Arrays.asList(Const.observerGroup)), null);		
			for (Task task : col){
				Control ctrl = task.getControl();
				ctrl.setOld(1);
				task.save(new HashSet<String>(Arrays.asList(Const.observerGroup)), Const.sysUser);	
				task.setNewDoc(true);			
				ctrl.setOld(0);
				ctrl.setAllControl(1);
				Calendar cal = Calendar.getInstance();
				cal.setTime(ctrl.getExecDate());
				switch (ctrl.getCycle()){
					case 2 : 
						cal.add(Calendar.DAY_OF_MONTH, 1);
						ctrl.setPrimaryCtrlDate(cal.getTime());
						break;
					case 3 :
						cal.add(Calendar.DAY_OF_MONTH, 7);
						ctrl.setPrimaryCtrlDate(cal.getTime());
						break;
					case 4 :
						cal.add(Calendar.MONTH, 1);
						ctrl.setPrimaryCtrlDate(cal.getTime());
						break;
					case 5 :
						cal.add(Calendar.MONTH, 3);
						ctrl.setPrimaryCtrlDate(cal.getTime());
						break;
					case 6 :
						cal.add(Calendar.MONTH, 6);
						ctrl.setPrimaryCtrlDate(cal.getTime());
						break;
					case 7 :
						cal.add(Calendar.YEAR, 1);
						ctrl.setPrimaryCtrlDate(cal.getTime());
						break;
				}
				task.setTaskDate(new Date());				
				task.save(sysGroupAsSet, sysUser);
			}
		} catch (DocumentAccessException e) {
			Server.logger.errorLogEntry(env.appType, e);
			return -1;
		} catch (DocumentException e) {
			Server.logger.errorLogEntry(env.appType, e);
			return -1;
		} catch (Exception e) {
			Server.logger.errorLogEntry(env.appType, e);
			return -1;
		}
		return 0;
	}

}
