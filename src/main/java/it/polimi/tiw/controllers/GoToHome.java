package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
import it.polimi.tiw.beans.Category;

/**
 * Servlet implementation class GoToHome
 */
@WebServlet("/GoToHome")
public class GoToHome extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToHome() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init() throws UnavailableException {
    	try {
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);

		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get db connection");
		}
    	
    	ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
    	
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String homePath = "Home.html";
		ServletContext servletContext = getServletContext();
		
		CategoriesDAO categoriesDAO = new CategoriesDAO(connection);
    	List<Category> list = null;
		try {
			list = categoriesDAO.findAllCategories();
		}
		catch (SQLException e) {
			final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
			webContext.setVariable("categoriesError", "An errorr ocurred whith the database connection!");
			templateEngine.process(homePath, webContext, response.getWriter());
			return;
		}
		
		List<String> idList = new ArrayList<>();
		idList.add("root");
		if(list != null) {
			for(Category category : list) {
				idList.add(Integer.toString(category.getCategoryID()));
			}
		}
		
		String userName = (String) request.getSession().getAttribute("username");
		final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
		String errorMessage = (String) request.getAttribute("errorMessage");
		if(errorMessage != null && !errorMessage.isEmpty()) {
			webContext.setVariable("categoriesError", errorMessage);
		}
		webContext.setVariable("categoryList", list);
		webContext.setVariable("idList", idList);
		webContext.setVariable("user", userName);
		webContext.setVariable("copyLink", true);
		templateEngine.process(homePath, webContext, response.getWriter());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
