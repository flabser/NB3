package kz.flabs.scriptprocessor;

import org.codehaus.groovy.control.MultipleCompilationErrorsException;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import kz.pchelka.log.ILogger;
import kz.pchelka.scheduler.IProcessInitiator;
import kz.pchelka.server.Server;

public class ScriptProcessor implements IScriptProcessor, IProcessInitiator {
	public static ILogger logger = Server.logger;

	@Override
	public String[] processString(String script) {
		Server.logger.errorLogEntry("method 4563 has not reloaded");
		return null;
	}

	@Override
	public String process(String script) {
		Server.logger.errorLogEntry("method 4564 has not reloaded");
		return "";
	}

	@Override
	public String[] processString(Class<GroovyObject> compiledClass) {
		Server.logger.errorLogEntry("method 4565 has not reloaded");
		return null;
	}

	public IScriptSource setScriptLauncher(String userScript, boolean debug) {
		GroovyObject groovyObject = null;
		String script = "";
		if (!debug) {
			script = normalizeScript(userScript);
		} else {
			script = normalizeDebugScript(userScript);
		}
		ClassLoader parent = getClass().getClassLoader();
		GroovyClassLoader loader = new GroovyClassLoader(parent);
		Class<GroovyObject> groovyClass = null;
		try {
			groovyClass = loader.parseClass(script);

			try {
				groovyObject = groovyClass.newInstance();
			} catch (InstantiationException e) {
				if (!debug) {
					Server.logger.errorLogEntry(e);
				}
			} catch (IllegalAccessException e) {
				if (!debug) {
					Server.logger.errorLogEntry(e);
				}
			}

			IScriptSource sciptObject = (IScriptSource) groovyObject;
			return sciptObject;

		} catch (MultipleCompilationErrorsException mcee) {
			// logger.errorLogEntry(script);
			if (!debug) {
				Server.logger.errorLogEntry(mcee.getMessage());
			}
			return new ScriptSource();
		}
	}

	public static String normalizeScript(String script) {
		String beforeScript =

		"import java.io.File;" +

		"import java.io.BufferedReader;" + "import java.io.FileNotFoundException;" + "import java.io.FileReader;"
				+ "import java.io.IOException;" + "import java.util.ArrayList;" + "import java.util.Random;"
				+ "import java.util.HashSet;" + "import java.util.Calendar;" + "import java.util.Date;"
				+ "import java.util.Random;" + "import java.sql.Connection;" + "import java.sql.Statement;"
				+ "import java.sql.PreparedStatement;" + "import java.sql.ResultSet;" + "import java.sql.Timestamp;"
				+ "import java.sql.*;" + "import kz.flabs.dataengine.Const;" + "import kz.flabs.util.*;"
				+ ScriptProcessorUtil.packageList + "import kz.flabs.scriptprocessor.*;"
				+ "import kz.flabs.runtimeobj.document.task.TaskType;" + "import jxl.*;" + "import jxl.format.*;"
				+ "import jxl.write.*;" + "import org.apache.xml.serialize.*;" + "import org.w3c.dom.*;"
				+ "import javax.xml.parsers.DocumentBuilderFactory;" + "import javax.xml.parsers.DocumentBuilder;"
				+ "import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;"
				+ "import org.apache.xerces.jaxp.DocumentBuilderImpl;" + "import com.itextpdf.text.DocumentException;"
				+ "import com.itextpdf.text.pdf.PdfReader;" + "import com.itextpdf.text.pdf.PdfStamper;"
				+ "import java.io.FileOutputStream;" + "import java.io.IOException;"
				+ "class Foo extends ScriptSource{";
		String afterScript = "}";
		return beforeScript + script + afterScript;
	}

	public static String normalizeDebugScript(String script) {
		String beforeScript = "import java.io.File;" + "import java.io.BufferedReader;"
				+ "import java.io.FileNotFoundException;" + "import java.io.FileReader;" + "import java.io.IOException;"
				+ "import java.util.ArrayList;" + "import java.util.Random;" + "import java.util.HashSet;"
				+ "import java.util.Calendar;" + "import java.util.Date;" + "import java.util.Random;"
				+ "import java.sql.Connection;" + "import java.sql.Statement;" + "import java.sql.PreparedStatement;"
				+ "import java.sql.ResultSet;" + "import java.sql.Timestamp;" + "import kz.flabs.dataengine.Const;"
				+ "import kz.flabs.util.*;" + ScriptProcessorUtil.packageList + "import kz.flabs.scriptprocessor.*;"
				+ "import kz.flabs.runtimeobj.document.task.TaskType;" + "import jxl.*;" + "import jxl.format.*;"
				+ "import jxl.write.*;" + "import org.apache.xml.serialize.*;" + "import org.w3c.dom.*;"
				+ "import javax.xml.parsers.DocumentBuilderFactory;" + "import javax.xml.parsers.DocumentBuilder;"
				+ "import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;"
				+ "import org.apache.xerces.jaxp.DocumentBuilderImpl;" + "import com.itextpdf.text.DocumentException;"
				+ "import com.itextpdf.text.pdf.PdfReader;" + "import com.itextpdf.text.pdf.PdfStamper;"
				+ "import java.io.FileOutputStream;" + "import java.io.IOException;"
				+ "class Foo extends DebugScriptSource{";
		String afterScript = "}";
		return beforeScript + script + afterScript;
	}

	@Override
	public String toString() {
		return "ScriptProcessorType=" + ScriptProcessorType.UNDEFINED;
	}

	@Override
	public String getOwnerID() {
		return this.getClass().getSimpleName();
	}

}
