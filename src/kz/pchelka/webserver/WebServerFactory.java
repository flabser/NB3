package kz.pchelka.webserver;

public class WebServerFactory {

	public static IWebServer getServer(int ver) {

		return new WebServer();

	}
}
