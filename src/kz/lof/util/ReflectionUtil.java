package kz.lof.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

public class ReflectionUtil {

	public static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}

		ArrayList classes = new ArrayList();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}

		return (Class[]) classes.toArray(new Class[classes.size()]);
	}

	private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class> classes = new ArrayList<Class>();
		if (!directory.exists()) {
			return classes;
		}

		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert!file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;

	}

	public static Collection getFiles() {
		File cur = new File("bin\\staff\\scheduled");

		System.out.println(cur.getAbsolutePath());
		Collection s = FileUtils.listFiles(cur, FileFilterUtils.suffixFileFilter("groovy"), FileFilterUtils.nameFileFilter("*"));
		return s;

	}

	public static void main(String[] args) {

		Collection v = getFiles();
		for (Object val : v) {
			System.out.println(val.getClass().getName() + "  " + val.toString());
		}

	}
}
