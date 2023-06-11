package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

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

import it.polimi.tiw.DAO.CategoriesDAO;

/**
 * Servlet implementation class CreateCategory
 */
@WebServlet("/CreateCategory")
public class CreateCategory extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateCategory() {
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
		String parentIDString = request.getParameter("parentID");
		String nameString = request.getParameter("name");
		String homePath = "/WEB-INF/Home.html";
		ServletContext servletContext = getServletContext();
		
		if(parentIDString == null || parentIDString.isEmpty() || nameString == null || nameString.isEmpty()) {
			final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
			webContext.setVariable("categoriesError", "PARAMETRO VUOTO");
			templateEngine.process(homePath, webContext, response.getWriter());
			return;
		}
		int parentID = 0;
		if(!Objects.equals(parentIDString, "root")) {
			try {
				parentID = Integer.parseInt(parentIDString);
			}
			catch (NumberFormatException e) {
				final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
				webContext.setVariable("categoriesError", "NON UN INT!");
				templateEngine.process(homePath, webContext, response.getWriter());
				return;
			}
		}
		CategoriesDAO categoriesDAO = new CategoriesDAO(connection);
		boolean creationOk;
		try {
			creationOk = categoriesDAO.createCategory(nameString, parentID);
		}
		catch (SQLException e) {
			final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
			webContext.setVariable("categoriesError", "ECCEZINE SQL");
			templateEngine.process(homePath, webContext, response.getWriter());
			return;
		}
		if(!creationOk) {
			final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
			webContext.setVariable("categoriesError", "ERRORE NELLA CREAZIONE");
			templateEngine.process(homePath, webContext, response.getWriter());
			return;
		}
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
