package kz.lof.webserver.valve;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import kz.pchelka.log.Log4jLogger;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

public class Logging extends ValveBase {
	private Log4jLogger logger;

	public Logging() {
		super();
		logger = new Log4jLogger(getClass().getSimpleName());
	}

	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {
		HttpServletRequest http = request;
		String requestURI = http.getRequestURI();
		String params = http.getQueryString();

		if (params != null) {
			requestURI = requestURI + "?" + http.getQueryString();
		}

		RequestURL ru = new RequestURL(requestURI);

		// System.out.println("-------------" + ru.getUrl());
		// Enumeration<String> headerNames = request.getHeaderNames();
		// while (headerNames.hasMoreElements()) {
		// String key = headerNames.nextElement();
		// String value = request.getHeader(key);
		// System.out.println(key + "=" + value);
		// }
		// System.out.println("-------------");

		// Server.logger.normalLogEntry(ru.getUrl() + " ---- ispage=" +
		// ru.isPage() + ", isprotected=" + ru.isProtected() + ", isdeafult=" +
		// ru.isDefault() + ", isauth=" + ru.isAuthRequest());

		String clientIpAddress = request.getHeader("X-FORWARDED-FOR");

		if (clientIpAddress == null) {
			clientIpAddress = request.getRemoteAddr();
		}

		logger.infoLogEntry(clientIpAddress + " " + ru.toString() + ", apptype=" + ru.getAppType() + ", servername=" + request.getServerName());
		// com.flabser.server.Server.logger.infoLogEntry(clientIpAddress + " " +
		// ru.toString() + ", apptype="
		// + ru.getAppType() + ", servername=" + request.getServerName());
		((Unsecure) getNext()).invoke(request, response, ru);
		return;
	}

}
