package kz.pchelka.webserver;

public class WebServerFactory {
	
	public static IWebServer getServer(int ver){
		if (ver == 6){
			return new WebServer6();
		}else{
			return new WebServer();
		}
		
	}
}
