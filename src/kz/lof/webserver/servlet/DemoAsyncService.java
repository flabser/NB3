package kz.lof.webserver.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.AsyncContext;

/**
 * Long running task which is to be invoked by our servlet.
 */
public class DemoAsyncService implements Runnable {

	/**
	 * The AsyncContext object for getting the request and response objects.
	 * Provided by the servlet.
	 */
	private AsyncContext aContext;

	public DemoAsyncService(AsyncContext aContext) {
		this.aContext = aContext;
	}

	@Override
	public void run() {
		PrintWriter out = null;

		try {
			// Retrieve the type parameter from the request. This is passed in
			// the URL.
			String typeParam = aContext.getRequest().getParameter("type");

			// Retrieve the counter paramter from the request. This is passed in
			// the URL.
			String counter = aContext.getRequest().getParameter("counter");

			System.out.println("Starting the long running task: Type=" + typeParam + ", Counter: " + counter);

			out = aContext.getResponse().getWriter();

			// Sleeping the thread so as to mimic long running task.
			Thread.sleep(5000);

			/**
			 * Doing some operation based on the type parameter.
			 */
			switch (typeParam) {
			case "1":
				out.println("This process invoked for " + counter + " times.");
				break;
			case "2":
				out.println("Some other process invoked for " + counter + " times.");
				break;
			default:
				out.println("Ok... nothing asked of.");
				break;
			}

			System.out.println("Done processing the long running task: Type=" + typeParam + ", Counter: " + counter);

			/**
			 * Intimating the Web server that the asynchronous task is complete
			 * and the response to be sent to the client.
			 */
			aContext.complete();

		} catch (IOException | InterruptedException ex) {
			ex.printStackTrace();
		} finally {
			out.close();
		}
	}
}
