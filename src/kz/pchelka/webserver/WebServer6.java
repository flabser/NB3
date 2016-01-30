package kz.pchelka.webserver;

import java.io.File;
import java.net.*;

import kz.pchelka.env.Environment;
import org.apache.catalina.*;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.catalina.startup.Embedded;

public class WebServer6 implements IWebServer {	
	private Engine engine;
	private Embedded container = null;	
	private String classesDir;		
	private Host defaultHost;
	private String appBase;

	@Override
	public void init(String defaultHostName) throws MalformedURLException, LifecycleException {
		kz.pchelka.server.Server.logger.verboseLogEntry("Init webserver 6 ...");
		classesDir = new File("/classes").toURI().toURL().toString() ;	
		container = new Embedded();
		//Authenticator auth = new Authenticator();	
		//AprLifecycleListener listener = new AprLifecycleListener();
		//listener.setSSLEngine("off");
		//LifecycleListener[]  listeners = container.findLifecycleListeners();
		//LifecycleListener listener = new LifecycleListener();
		//container.addLifecycleListener(listener);
		container.setCatalinaHome("webserver");
		container.setRealm(new MemoryRealm());
		appBase = new File("webapps").getAbsolutePath();
		defaultHost = container.createHost(defaultHostName, appBase);		
		defaultHost.addChild(getSharedResources());
		
		engine = container.createEngine();
	
		engine.setName("localEngine");		
		engine.setDefaultHost(defaultHost.getName());
		engine.addChild(defaultHost);
		
		container.addEngine(engine);		
		
	}
	
	public Context getSharedResources() throws LifecycleException, MalformedURLException {	
		WebappLoader loader = new WebappLoader(this.getClass().getClassLoader());
		if (classesDir != null) {
			loader.addRepository(classesDir);
		}
		Context sharedResourcesContext = container.createContext("/SharedResources",  "SharedResources");
		sharedResourcesContext.setLoader(loader);
		sharedResourcesContext.setReloadable(false);
		//kz.pchelka.server.Server.logger.verboseLogEntry("SharedResources prepared to load");	
		return sharedResourcesContext;			
	}

	public Host addApplication(String siteName, String URLPath, String docBase) throws LifecycleException, MalformedURLException {		
		Host appHost = null;

		if (siteName == null || siteName.equalsIgnoreCase("")){				
			appHost = defaultHost;				
		}else{
			URLPath = "";	
			appHost = container.createHost(siteName, appBase);			
			appHost.setName(siteName);
			appHost.addChild(getSharedResources());
			engine.addChild(appHost);
		}

		WebappLoader loader = new WebappLoader(this.getClass().getClassLoader());

		if (classesDir != null) {
			loader.addRepository(classesDir);
		}

		Context context = container.createContext(URLPath,  docBase);	
		context.setLoader(loader);
		context.setReloadable(false);
		
		
		appHost.addChild(context);

		return appHost;
	}


	public String initConnectors(){	
		String portInfo = "";
		if (Environment.isSSLEnable){
			Connector secureConnector = null;	
			kz.pchelka.server.Server.logger.normalLogEntry("TLS connector has been enabled");
			secureConnector = container.createConnector((InetAddress) null, Environment.secureHttpPort, true);
			secureConnector.setScheme("https");
			secureConnector.setProtocol("org.apache.coyote.http11.Http11Protocol");
			secureConnector.setSecure(true);
			secureConnector.setEnableLookups(false);		
			secureConnector.setSecure(true);
			secureConnector.setProperty("SSLEnabled","true");
			secureConnector.setProperty("sslProtocol", "TLS");		
			secureConnector.setProperty("keystoreFile", Environment.keyStore);
			secureConnector.setProperty("keystorePass", Environment.keyPwd);
			if (Environment.isClientSSLAuthEnable){
				secureConnector.setProperty("clientAuth", "true");
				secureConnector.setProperty("truststoreFile", Environment.trustStore);
				secureConnector.setProperty("truststorePass", Environment.trustStorePwd);
			}	
			container.addConnector(secureConnector);
			
		//	Connector connector = container.createConnector((InetAddress) null, Environment.httpPort, false);
		//	connector.setRedirectPort(Environment.secureHttpPort);
		//	container.addConnector(connector);
			portInfo = "secure port:" + Environment.secureHttpPort;
		}else{
			Connector connector = container.createConnector((InetAddress) null, Environment.httpPort, false);
			container.addConnector(connector);
			portInfo = "port:" + Environment.httpPort;
		}
		container.setAwait(true);
		return portInfo;
	}

	public void startContainer(){		
		try {
			container.start();
		} catch (LifecycleException e) {	
			kz.pchelka.server.Server.logger.errorLogEntry(e);		
		} 

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				stopContainer();
			}
		});
	}

	public synchronized void stopContainer() {
		try {
			if (container != null) {
				container.stop();
			}
		} catch (LifecycleException exception) {
			kz.pchelka.server.Server.logger.errorLogEntry("Cannot Stop WebServer" + exception.getMessage());			
		}

	}

	

}
