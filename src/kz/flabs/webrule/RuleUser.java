package kz.flabs.webrule;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import kz.flabs.scriptprocessor.ScriptProcessor;
import kz.flabs.sourcesupplier.Macro;
import kz.flabs.util.XMLUtil;
import kz.flabs.webrule.constants.RunMode;
import kz.flabs.webrule.constants.ValueSourceType;
import kz.lof.appenv.AppEnv;

import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.w3c.dom.Node;

public class RuleUser {
	public RunMode isOn = RunMode.ON;
	public String value = "";
	public ValueSourceType valueSource;
	public Macro macro;
	public Class<GroovyObject> compiledClass;	
	

	public RuleUser(Node node, String description){
		try{

			if (!XMLUtil.getTextContent(node,"@mode", false).equalsIgnoreCase("ON")){                    
				isOn = RunMode.OFF;			
			}

			value = XMLUtil.getTextContent(node,".", false);
			valueSource = ValueSourceType.valueOf(XMLUtil.getTextContent(node,"./@source",true,"STATIC", false));
			
			if (valueSource == ValueSourceType.MACRO){
				if (value.equalsIgnoreCase("AUTHOR")){
					macro = Macro.AUTHOR;
				}else if (value.equalsIgnoreCase("CURRENT_USER")){
					macro = Macro.CURRENT_USER;
				}else{
					macro = Macro.UNKNOWN_MACRO;
				}
			}else if(valueSource == ValueSourceType.SCRIPT){				
				String script = ScriptProcessor.normalizeScript(value);
				ClassLoader parent = getClass().getClassLoader();
				GroovyClassLoader loader = new GroovyClassLoader(parent);
				try{
					compiledClass = loader.parseClass(script);
				}catch(MultipleCompilationErrorsException e){
					AppEnv.logger.errorLogEntry("Compilation error rule=" + description + ":" + e.getMessage());
					isOn = RunMode.OFF;	
				}
			}
		}catch(Exception e) {     
			AppEnv.logger.errorLogEntry(e);

		}
	}

	public String toString(){
		return "valuesource=" + valueSource + ", value=" + value;
	}

}
