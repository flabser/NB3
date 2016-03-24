package kz.lof.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import kz.flabs.localization.Localizator;
import kz.flabs.localization.Vocabulary;
import kz.flabs.util.Util;
import kz.lof.appenv.AppEnv;
import kz.lof.dataengine.jpa.deploying.InitializerHelper;
import kz.lof.env.EnvConst;
import kz.lof.env.Environment;
import kz.lof.exception.SecureException;
import kz.lof.util.StringUtil;

import org.eclipse.persistence.exceptions.DatabaseException;

public class Console implements Runnable {

	@Override
	public void run() {

		final Scanner in = new Scanner(System.in);
		while (in.hasNext()) {
			try {
				String command = in.nextLine();
				cliHandler(command);
			} catch (Exception e) {
				Server.logger.errorLogEntry(e);
			} finally {
				// in.close();
			}
		}

	}

	private void cliHandler(String command) {
		System.out.println("> " + command);
		if (command.equalsIgnoreCase("quit") || command.equalsIgnoreCase("exit") || command.equalsIgnoreCase("q")) {
			Server.shutdown();
		} else if (command.equalsIgnoreCase("info") || command.equalsIgnoreCase("i")) {
			System.out.println("server version=" + Server.serverVersion);
			System.out.println("os=" + System.getProperty("os.name") + " " + System.getProperty("os.version") + "(" + System.getProperty("os.arch")
			        + ")");
			System.out.println("jvm=" + System.getProperty("java.version"));
			System.out.println("application server name=" + EnvConst.APP_NAME);
			System.out.println("database=" + Environment.dataBase.getInfo());
			if (Environment.isDevMode) {
				System.out.println("developer mode is on");
			}
			System.out.println("default language=" + EnvConst.DEFAULT_LANG);
			File jarFile = new File(EnvConst.NB_JAR_FILE);
			System.out.println("jar=" + EnvConst.NB_JAR_FILE + ", path=" + jarFile.getAbsolutePath() + ", exist=" + jarFile.exists());
		} else if (command.equalsIgnoreCase("show users") || command.equalsIgnoreCase("su")) {

		} else if (command.equalsIgnoreCase("reset rules") || command.equalsIgnoreCase("rr")) {
			for (AppEnv env : Environment.getApplications()) {
				env.ruleProvider.resetRules();
				env.flush();
			}
			new Environment().flush();
			Environment.flushSessionsCach();
		} else if (command.equalsIgnoreCase("show server cache") || command.equalsIgnoreCase("ssc")) {
			System.out.println(Environment.getCacheInfo());
		} else if (command.equalsIgnoreCase("show apps cache") || command.equalsIgnoreCase("sac")) {
			for (String ci : Environment.getAppsCachesInfo()) {
				System.out.println(ci);
			}
		} else if (command.equalsIgnoreCase("show users cache") || command.equalsIgnoreCase("suc")) {
			for (String ci : Environment.getSessionCachesInfo()) {
				System.out.println(ci);
			}
		} else if (command.equalsIgnoreCase("reload vocabulary") || command.equalsIgnoreCase("rv")) {
			Localizator l = new Localizator();
			Environment.vocabulary = l.populate();
			if (Environment.vocabulary == null) {
				Environment.vocabulary = new Vocabulary("system");
			}
			for (AppEnv env : Environment.getApplications()) {
				env.reloadVocabulary();
				env.flush();
			}
			new Environment().flush();
			Environment.flushSessionsCach();
		} else if (command.equalsIgnoreCase("show initializers") || command.equalsIgnoreCase("si")) {
			InitializerHelper helper = new InitializerHelper();
			try {
				helper.getAllinitializers(true);
			} catch (IOException e) {
				System.err.println(e);
			}
		} else if (command.contains("start initializer") || command.startsWith("stini")) {
			int start = 0;
			if (command.contains("start initializer")) {
				start = "start initializer".length();
			} else if (command.startsWith("stini")) {
				start = "stini".length();
			}
			String ini = command.substring(start).trim();
			if (ini.trim().equals("")) {
				System.err.println("error -initializer name is empty");
			} else {
				InitializerHelper helper = new InitializerHelper();
				try {
					helper.runInitializer(ini, true);
				} catch (DatabaseException | SecureException e) {
					System.err.println(e);
				}
				System.out.println("done");
			}
		} else if (command.contains("run batch") || command.startsWith("rubat")) {
			int start = 0;
			if (command.contains("run batch")) {
				start = "run batch".length();
			} else if (command.startsWith("rubat")) {
				start = "rubat".length();
			}

			String batch = command.substring(start).trim();
			if (batch.trim().equals("")) {
				System.err.println("error -batch name is empty");
			} else {
				try (BufferedReader br = new BufferedReader(new FileReader(EnvConst.RESOURCES_DIR + File.separator + batch))) {
					String line;
					while ((line = br.readLine()) != null) {
						if (!line.startsWith("#")) {
							cliHandler(line);
						}
					}
				} catch (FileNotFoundException e) {
					System.err.println("\"" + batch + "\" batch file not found");
				} catch (IOException e) {
					System.err.println(e);
				}
				System.out.println("done");
			}
		} else if (command.equalsIgnoreCase("show file to delete") || command.equalsIgnoreCase("sfd")) {
			if (Environment.fileToDelete.size() == 0) {
				System.out.println("there are no any files to delete");
			} else {
				for (String ci : Environment.fileToDelete) {
					System.out.println(ci);
				}
			}
		} else if (command.equalsIgnoreCase("import from h2") || command.equalsIgnoreCase("ifh2") || command.equalsIgnoreCase("ifos")) {
			try {
				Class<?> clazz = Class.forName(EnvConst.ADMINISTRATOR_SERVICE_CLASS);
				Constructor<?> contructor = clazz.getConstructor();
				Method method;
				switch (command) {
					case "ifh2":
					case "import from h2":
						method = clazz.getMethod("importFromH2");
						break;
					case "ifos":
						method = clazz.getMethod("importFromOldStructure");
						break;
					default:
						method = clazz.getMethod("importFromH2");
				}
				Object instance = contructor.newInstance();
				method.invoke(instance);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
			        | SecurityException e) {
				System.err.println(e);
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.err.println(e);
			}

		} else if (command.equals("help") || command.equalsIgnoreCase("h")) {
			System.out.println(Util.readFile(EnvConst.RESOURCES_DIR + File.separator + "console_commands.txt"));
		} else {
			if (!command.trim().equalsIgnoreCase("")) {
				System.err.println("error -command \"" + command + "\" is not recognized");
			}
		}
	}

	public static List<String> getValFromConsole(String prefix, String pattern) {
		System.out.print(prefix);
		List<String> result = new ArrayList<String>();
		String value = "";
		Scanner in = new Scanner(System.in);
		try {
			while (in.hasNext()) {
				boolean isCorrect = false;
				value = in.nextLine();
				StringTokenizer tokenizer = new StringTokenizer(value);
				while (tokenizer.hasMoreTokens()) {
					String val = tokenizer.nextToken();
					if (StringUtil.checkByPattren(val, pattern)) {
						isCorrect = true;
						result.add(val);
					}
				}
				if (isCorrect) {
					return result;
				}
				System.out.println("\"" + value + "\" is wrong value, enter another value ");
			}
		} catch (Exception e) {
			Server.logger.errorLogEntry(e);
		} finally {
			// in.close();
		}
		return null;
	}
}
