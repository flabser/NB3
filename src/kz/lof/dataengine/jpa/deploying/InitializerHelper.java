package kz.lof.dataengine.jpa.deploying;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import kz.flabs.dataengine.Const;
import kz.flabs.users.User;
import kz.lof.appenv.AppEnv;
import kz.lof.dataengine.jpa.IDAO;
import kz.lof.dataengine.jpa.ISimpleAppEntity;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.exception.SecureException;
import kz.lof.scripting._Session;
import kz.lof.user.AnonymousUser;

import org.eclipse.persistence.exceptions.DatabaseException;

import com.eztech.util.JavaClassFinder;

/**
 *
 *
 * @author Kayra created 28-12-2015
 */

public class InitializerHelper {

	// TODO it need to improve for checking if an application switched off
	public Map<String, Class<IInitialData>> getAllinitializers(boolean showConsoleOutput) throws IOException {
		ZipInputStream zip = null;
		Map<String, Class<IInitialData>> inits = new HashMap<String, Class<IInitialData>>();
		File jarFile = new File(EnvConst.NB_JAR_FILE);
		if (jarFile.exists()) {
			System.out.println("check " + jarFile.getAbsolutePath() + "...");
			zip = new ZipInputStream(new FileInputStream(EnvConst.NB_JAR_FILE));
			for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
				String resource = entry.getName().replace("/", ".");
				for (AppEnv env : Environment.getApplications()) {
					if (!entry.isDirectory() && resource.startsWith(env.appName.toLowerCase() + ".init")) {
						try {
							String name = resource.substring(0, resource.indexOf(".class"));
							Class<?> clazz = Class.forName(name);
							IInitialData<ISimpleAppEntity, IDAO> instance = (IInitialData<ISimpleAppEntity, IDAO>) clazz.newInstance();
							if (instance instanceof IInitialData) {
								inits.put(name, (Class<IInitialData>) instance.getClass());
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
			System.out.println("checking class files...");
			JavaClassFinder classFinder = new JavaClassFinder();
			List<Class<? extends IInitialData>> classesList = null;
			classesList = classFinder.findAllMatchingTypes(IInitialData.class);
			for (Class<?> populatingClass : classesList) {
				if (!populatingClass.isInterface() && !populatingClass.getCanonicalName().equals(InitialDataAdapter.class.getCanonicalName())) {
					IInitialData<ISimpleAppEntity, IDAO> pcInstance = null;
					try {
						pcInstance = (IInitialData<ISimpleAppEntity, IDAO>) Class.forName(populatingClass.getCanonicalName()).newInstance();
						String name = pcInstance.getName();
						String packageName = populatingClass.getPackage().getName();
						String p = packageName.substring(0, packageName.indexOf("."));
						AppEnv env = Environment.getAppEnv(p);
						if (env != null) {
							inits.put(name, (Class<IInitialData>) populatingClass);
							if (showConsoleOutput) {
								System.out.println(env.appName + ":" + populatingClass.getCanonicalName());
							}
						} else {
							if (showConsoleOutput) {
								System.out.println("null " + populatingClass.getCanonicalName());
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
		}
		if (inits.size() == 0 && showConsoleOutput) {
			System.out.println("there is no any initializer on the Server");
		}
		return inits;
	}

	public String runInitializer(String name, boolean showConsoleOutput) throws DatabaseException, SecureException {
		int count = 0;
		IInitialData<ISimpleAppEntity, IDAO> pcInstance = null;
		boolean isFound = false;
		File jarFile = new File(EnvConst.NB_JAR_FILE);
		if (jarFile.exists()) {
			try {
				Class<?> populatingClass = populatingClass = Class.forName(name);
				isFound = true;
				count = runToPopulate(populatingClass, showConsoleOutput);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			JavaClassFinder classFinder = new JavaClassFinder();
			List<Class<? extends IInitialData>> classesList = null;
			classesList = classFinder.findAllMatchingTypes(IInitialData.class);
			for (Class<?> populatingClass : classesList) {
				if (!populatingClass.isInterface() && !populatingClass.getCanonicalName().equals(InitialDataAdapter.class.getCanonicalName())) {

					if (populatingClass.getCanonicalName().equals(name) || populatingClass.getName().equals(name)) {
						isFound = true;
						count = runToPopulate(populatingClass, showConsoleOutput);
					}

				}
			}
		}
		if (isFound) {
			if (showConsoleOutput) {
				System.out.println(count + " records have been added");
			}
		} else {
			System.out.println("initializer \"" + name + "\" has not found");
		}
		return "";
	}

	private int runToPopulate(Class<?> populatingClass, boolean showConsoleOutput) throws DatabaseException, SecureException {
		int count = 0;
		IInitialData<ISimpleAppEntity, IDAO> pcInstance;
		try {
			String packageName = populatingClass.getPackage().getName();
			String p = packageName.substring(0, packageName.indexOf("."));
			AppEnv env = Environment.getAppEnv(p);
			if (env != null) {
				User user = new User(Const.sysUser, env);
				_Session ses = new _Session(env, new AnonymousUser());
				pcInstance = (IInitialData<ISimpleAppEntity, IDAO>) Class.forName(populatingClass.getCanonicalName()).newInstance();
				List<ISimpleAppEntity> entities = pcInstance.getData(ses, null, null);
				Class<?> daoClass = pcInstance.getDAO();
				IDAO dao = getDAOInstance(ses, daoClass);
				if (dao != null) {
					for (ISimpleAppEntity entity : entities) {
						if (dao.add(entity) != null) {
							if (showConsoleOutput) {
								System.out.println(entity.toString() + " added");
							}
							count++;
						}
					}
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
		return count;
	}

	private IDAO<?, ?> getDAOInstance(_Session ses, Class<?> daoClass) {
		@SuppressWarnings("rawtypes")
		Class[] intArgsClass = new Class[] { _Session.class };
		IDAO<?, ?> dao = null;

		try {
			Constructor<?> intArgsConstructor = daoClass.getConstructor(intArgsClass);
			dao = (IDAO<?, ?>) intArgsConstructor.newInstance(new Object[] { ses });
		} catch (Exception e) {
			e.printStackTrace();
		}

		return dao;
	}

}
