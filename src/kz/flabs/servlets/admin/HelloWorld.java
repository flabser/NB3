package kz.flabs.servlets.admin;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

 public class HelloWorld extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
   static final long serialVersionUID = 1L;
   
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
	 	out.println( "<html><head>" );
		out.println( "<title>A Sample Servlet!</title>" );
		out.println( "</head>" );
		out.println( "<body>" );
		out.println( "<h1>Hello, World!</h1>" );
		out.println( "</body></html>" );
		out.close(); 
		
	}  	  	  	    
}