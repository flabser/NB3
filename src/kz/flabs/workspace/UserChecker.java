package kz.flabs.workspace;

import java.io.IOException;

import kz.lof.env.Environment;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;



public class UserChecker {

	public String getWSessionID(){
		DefaultHttpClient httpclient = new DefaultHttpClient();
		String sesID = "";
		try {
			HttpGet httpget = new HttpGet("https://" + Environment.hostName + ":" + Environment.httpPort + "/Workspace/dt");

			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();

			System.out.println("Login form get: " + response.getStatusLine());
			EntityUtils.consume(entity);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return sesID;
	}

}
