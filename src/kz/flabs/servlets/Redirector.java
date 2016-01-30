package kz.flabs.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kz.pchelka.env.Environment;


public class Redirector extends HttpServlet {

	private static final long serialVersionUID = 2107838212730208929L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) {

		try {
			response.sendRedirect(Environment.getDefaultRedirectURL());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
