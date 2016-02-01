package kz.flabs.scriptprocessor;

import kz.nextbase.script._Tag;

public class ScriptProcessorUtil {
	public final static String packageList = "import kz.nextbase.script.*;" + "import kz.nextbase.script.task.*;"
	        + "import kz.nextbase.script.project.*;" + "import kz.nextbase.script.mail.*;" + "import kz.nextbase.script.struct.*;"
	        + "import kz.nextbase.script.constants.*;" +

	        "import kz.flabs.runtimeobj.document.structure.*;" + "import kz.flabs.runtimeobj.document.glossary.*;"
	        + "import kz.flabs.runtimeobj.document.project.*;" +

	        "import kz.flabs.users.User;" +

	        "import net.sf.jasperreports.engine.*;" + "import net.sf.jasperreports.engine.export.*;"
	        + "import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;" + "import net.sf.jasperreports.engine.data.JsonDataSource;"
	        + "import net.sf.jasperreports.engine.fill.JRFileVirtualizer;" + "import net.sf.jasperreports.engine.design.JRDesignConditionalStyle;" +

	        "import net.sf.jasperreports.engine.design.JRDesignStyle;" + "import net.sf.jasperreports.engine.export.JExcelApiExporter;"
	        + "import net.sf.jasperreports.engine.export.JRHtmlExporter;" + "import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;"
	        + "import net.sf.jasperreports.engine.data.*;" +

	        "import java.text.*;" + "import java.util.*;" + "import java.sql.*;" +

	        "import org.apache.commons.lang3.StringUtils;" +

	        "import kz.flabs.dataengine.Const;" + "import kz.flabs.dataengine.IDatabase;" + "import kz.flabs.runtimeobj.document.task.Task;"
	        + "import kz.pchelka.env.Environment;" + "import kz.flabs.dataengine.h2.SystemDatabase;";

	public static _Tag getScriptError(StackTraceElement stack[]) {
		_Tag tag = new _Tag("stack", "");
		for (int i = 0; i < stack.length; i++) {
			tag.addTag("entry", stack[i].getClassName() + "(" + stack[i].getMethodName() + ":" + Integer.toString(stack[i].getLineNumber()) + ")");
		}
		return tag;
	}

	public static String getGroovyError(StackTraceElement stack[]) {
		for (int i = 0; i < stack.length; i++) {
			if (stack[i].getClassName().contains("Foo")) {
				return stack[i].getMethodName() + ", > " + Integer.toString(stack[i].getLineNumber() - 3) + "\n";
			}
		}
		return "";
	}

}
