package kz.pchelka.webserver;

import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardServer;
//import org.apache.catalina.startup.Tomcat;

public class TestWebServer {
	final static  String CATALINA_HOME = "D:/workspace/NextBase/";

	public static void main(String[] args) throws Exception {
		/*String appBase = "D:/workspace/NextBase/webapps";
		Integer port = 38779;		 
		Tomcat tomcat = new Tomcat ();
		tomcat.setBaseDir (CATALINA_HOME);
		tomcat.setPort (port);
		tomcat.addWebapp ("/Avanti", CATALINA_HOME + "/webapps/Avanti");
		tomcat.start ();
		System.out.println ("Started tomcat");
		tomcat.getServer ().await ();*/
		// Webapp tomcat7demo accessible at http://localhost:8080/tomcat7demo/

	}
}
