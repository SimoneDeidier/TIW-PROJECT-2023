package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Servlet implementation class CopyCategory
 */
@WebServlet("/CopyCategory")
public class CopyCategory extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CopyCategory() {
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
		String parentIDString = request.getParameter("parentID");
		ServletContext servletContext = getServletContext();
		String homePath = "Home.html";
		
		if(parentIDString == null || parentIDString.isEmpty()) {
			// TODO error
		}
		int parentID = 0;
		try {
			parentID = Integer.parseInt(parentIDString);
		}
		catch (NumberFormatException e) {
			// TODO: handle exception
		}
		CategoriesDAO categoriesDAO = new CategoriesDAO(connection);
		List<Category> categoriesList = null;
		try {
			categoriesList = categoriesDAO.findAllCategories();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
		}
		List<Category> toCopyList = null;
		try {
			toCopyList = categoriesDAO.toCopyList(parentID);
		}
		catch (SQLException e) {
			// TODO: handle exception
		}
		/*for(Category category : copyHereLinkMap.keySet()) {
			System.out.println(category.getCategoryID() + " - " + copyHereLinkMap.get(category));
		}*/
		final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
		//webContext.setVariable("categoriesError", "An error occurred with the database connection!");
		webContext.setVariable("categoryList", categoriesList);
		webContext.setVariable("toCopyList", toCopyList);
		webContext.setVariable("copyLink", false);
		templateEngine.process(homePath, webContext, response.getWriter());
		return;
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
