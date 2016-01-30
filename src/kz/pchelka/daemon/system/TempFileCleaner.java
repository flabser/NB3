package kz.pchelka.daemon.system;

import java.io.File;
import java.util.ArrayList;

import kz.flabs.webrule.scheduler.DaysOfWeek;
import kz.pchelka.env.Environment;
import kz.pchelka.scheduler.AbstractDaemon;
import kz.pchelka.scheduler.IProcessInitiator;
import kz.pchelka.server.Server;

public class TempFileCleaner extends AbstractDaemon {
	private int ac;

	@Override
	public int process(IProcessInitiator processOwner) {
		ac = 0;
		File folder = new File(Environment.tmpDir);
		File trashFolder = new File(Environment.trash);
		if (isFirstStart) {
			if (folder.exists()) {
				File[] list = folder.listFiles();
				for (int i = list.length; --i >= 0;) {
					File file = list[i];
					if (!file.getName().equalsIgnoreCase("trash")) {
						delete(file);
					}
				}
			}
			if (trashFolder.exists()) {
				File[] list = trashFolder.listFiles();
				for (int i = list.length; --i >= 0;) {
					File file = list[i];
					delete(file);
				}
			}
			isFirstStart = false;
		} else {
			for (String filePath : Environment.fileToDelete) {
				File file = new File(filePath);
				while (file.getParentFile() != null && !file.getParentFile().getName().equals("tmp")) {
					file = file.getParentFile();
				}
				if (file.getParentFile() == null) {
					file = null;
				}
				delete(file);
			}

			File[] list = trashFolder.listFiles();
			for (int i = list.length; --i >= 0;) {
				File file = list[i];
				Environment.fileToDelete.add(file.getAbsolutePath());
			}
		}
		if (ac > 0) {
			Server.logger.verboseLogEntry(getID() + " " + ac + " temporary files were deleted");
		}
		return 0;
	}

	public void delete(File file) {
		if (file == null || !file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				delete(f);
			}
			if (file.delete()) {
				ac++;
			}
		} else {
			if (file.delete()) {
				ac++;
			}
		}
	}

	@Override
	public ArrayList<DaysOfWeek> getDaysOfWeek() {

		ArrayList<DaysOfWeek> daysOfWeek = new ArrayList<DaysOfWeek>();
		daysOfWeek.add(DaysOfWeek.ALL_WEEK);
		return daysOfWeek;
	}

}
