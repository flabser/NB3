package kz.lof.scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import administrator.dao.ApplicationDAO;
import administrator.model.Application;
import kz.lof.appenv.AppEnv;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.scripting._Session;
import kz.lof.scriptprocessor.scheduled.IScheduledScript;
import kz.lof.user.SuperUser;
import kz.lof.util.ReflectionUtil;

/**
 *
 *
 * @author Kayra created 11-04-2016
 */

public class SchedulerHelper {

	// TODO it need to improve for checking if an application switched off
	public Map<String, IScheduledScript> getAllScheduledTasks(boolean showConsoleOutput) throws IOException {
		ZipInputStream zip = null;
		Map<String, IScheduledScript> tasks = new HashMap<String, IScheduledScript>();
		File jarFile = new File(EnvConst.NB_JAR_FILE);
		if (jarFile.exists()) {
			if (showConsoleOutput) {
				System.out.println("check " + jarFile.getAbsolutePath() + "...");
			}
			zip = new ZipInputStream(new FileInputStream(EnvConst.NB_JAR_FILE));
			for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
				String resource = entry.getName().replace("/", ".");
				for (AppEnv env : Environment.getApplications()) {
					if (!entry.isDirectory() && resource.startsWith(env.appName.toLowerCase() + ".scheduled")) {
						try {
							String name = resource.substring(0, resource.indexOf(".class"));
							Class<?> clazz = Class.forName(name);
							IScheduledScript instance = (IScheduledScript) clazz.newInstance();
							if (instance instanceof IScheduledScript) {
								_Session ses = new _Session(env, new SuperUser());
								instance.setSession(ses);
								tasks.put(name, instance);
								if (showConsoleOutput) {
									System.out.println(env.appName + ":" + name);
								}
							}
						} catch (InstantiationException e) {

						} catch (ClassNotFoundException e) {
							System.out.println(e.getMessage());
						} catch (IllegalAccessException e) {
							System.out.println(e);
						}
					}
				}
			}
		} else {
			if (showConsoleOutput) {
				System.out.println("checking class files...");
			}
			ApplicationDAO aDao = new ApplicationDAO();
			List<Application> list = aDao.findAll();
			for (Application app : list) {
				try {
					Class[] classesList = ReflectionUtil.getClasses(app.getName().toLowerCase() + ".scheduled");
					for (Class<? extends IScheduledScript> taskClass : classesList) {
						if (!taskClass.isInterface() && !Modifier.isAbstract(taskClass.getModifiers())) {
							IScheduledScript pcInstance = null;
							try {
								pcInstance = (IScheduledScript) Class.forName(taskClass.getCanonicalName()).newInstance();
								String name = pcInstance.getName();
								String packageName = taskClass.getPackage().getName();
								String p = packageName.substring(0, packageName.indexOf("."));
								AppEnv env = Environment.getAppEnv(p);
								if (env != null) {
									_Session ses = new _Session(env, new SuperUser());
									pcInstance.setSession(ses);
									tasks.put(name, pcInstance);
									if (showConsoleOutput) {
										System.out.println(env.appName + ":" + taskClass.getCanonicalName());
									}
								} else {
									if (showConsoleOutput) {
										System.out.println("null " + taskClass.getCanonicalName());
									}
								}
							} catch (InstantiationException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}

						}
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (tasks.size() == 0 && showConsoleOutput) {
			System.out.println("there is no any scheduled tasks on the server");
		}
		return tasks;
	}
}
