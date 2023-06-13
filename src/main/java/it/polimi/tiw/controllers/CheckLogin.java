package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.DAO.UserDAO;
import it.polimi.tiw.beans.User;

/**
 * Servlet implementation class CheckLogin
 */
@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CheckLogin() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void init() throws ServletException {
		ServletContext context = getServletContext();
		String driver = context.getInitParameter("dbDriver");
		String url = context.getInitParameter("dbUrl");
		String user = context.getInitParameter("dbUser");
		String password = context.getInitParameter("dbPassword");
		
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		}
		try {
			connection = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get db connection");
		}
		
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String loginPagePath = "/index.html";
		ServletContext servletContext = getServletContext();
		
		if(username == null || username.isEmpty() || password == null || password.isEmpty()) {
			final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
			webContext.setVariable("loginError", "Username or password were missing, please refill the form!");
			templateEngine.process(loginPagePath, webContext, response.getWriter());
			return;
		}
		UserDAO userDAO = new UserDAO(connection);
		User user = null;
		try {
			user = userDAO.checkLogin(username, password);
		}
		catch (SQLException e) {
			final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
			webContext.setVariable("loginError", "An error occurred with the database connection, please refill the form!");
			templateEngine.process(loginPagePath, webContext, response.getWriter());
			return;
		}
		if(user == null) {
			final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
			webContext.setVariable("loginError", "Username or password inserted aren't correct, please refill the form!");
			templateEngine.process(loginPagePath, webContext, response.getWriter());
			return;
		}
		request.getSession().setAttribute("username", username);
		response.sendRedirect(getServletContext().getContextPath() + "/GoToHome");
		
	}
	
	public void destroy() {
		if(connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
