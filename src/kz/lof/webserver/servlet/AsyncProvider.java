package kz.lof.webserver.servlet;

import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "AsyncProvider", urlPatterns = { "/AProvider" }, asyncSupported = true)
public class AsyncProvider extends HttpServlet {
	private static final long serialVersionUID = 8193756500772011212L;

	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		System.out.println("Async Servlet with thread: " + Thread.currentThread().toString());
		AsyncContext ac = request.startAsync();
		ac.addListener(new AsyncListener() {
			@Override
			public void onComplete(AsyncEvent event) throws IOException {
				System.out.println("Async complete");
			}

			@Override
			public void onTimeout(AsyncEvent event) throws IOException {
				System.out.println("Timed out...");
			}

			@Override
			public void onError(AsyncEvent event) throws IOException {
				System.out.println("Error...");
			}

			@Override
			public void onStartAsync(AsyncEvent event) throws IOException {
				System.out.println("Starting async...");
			}
		});

		/**
		 * Scheduling our Long running process using an inbuilt ThreadPool. Note
		 * that we are passing the AsyncContext object to our long running
		 * process: DemoAsyncService.
		 */
		ScheduledThreadPoolExecutor executer = new ScheduledThreadPoolExecutor(10);
		executer.execute(new DemoAsyncService(ac));
		System.out.println("Servlet completed request handling");

	}

	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

}
