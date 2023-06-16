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
		String categoryIDString = request.getParameter("categoryID");
		ServletContext servletContext = getServletContext();
		String homePath = "Home.html";
		String userName = (String) request.getSession().getAttribute("username");
		
		if(categoryIDString == null || categoryIDString.isEmpty()) {
			request.setAttribute("errorMessage", "An empty parameter was inserted, please select another category!");
			servletContext.getRequestDispatcher("/GoToHome").forward(request, response);
			return;
		}
		long categoryID = 0;
		try {
			categoryID = Long.parseLong(categoryIDString);
		}
		catch (NumberFormatException e) {
			request.setAttribute("errorMessage", "Category ID's format is not acceptable, please select another category!");
			servletContext.getRequestDispatcher("/GoToHome").forward(request, response);
			return;
		}
		CategoriesDAO categoriesDAO = new CategoriesDAO(connection);
		boolean parameterOK;
		List<Category> categoriesList = null;
		List<Category> toCopyList = null;
		try {
			parameterOK = categoriesDAO.checkExistingCategoryFromID(categoryID);
			if(!parameterOK) {
				request.setAttribute("errorMessage", "Wrong category ID in the query string, please select another category!");
				servletContext.getRequestDispatcher("/GoToHome").forward(request, response);
				return;	
			}
			categoriesList = categoriesDAO.findAllCategories();
			toCopyList = categoriesDAO.toCopyList(categoryID);
		}
		catch (SQLException e) {
			request.setAttribute("errorMessage", "An errorr ocurred whith the database connection!");
			servletContext.getRequestDispatcher("/GoToHome").forward(request, response);
			return;
		}
		Map<Long, Boolean> isToCopyMap = new HashMap<>();
		for(Category category : categoriesList) {
			boolean toAdd = true;
			long id = category.getCategoryID();
			for(Category toCopyCategory : toCopyList) {
				if(id == toCopyCategory.getCategoryID()) {
					isToCopyMap.put(id, true);
					toAdd = false;
					break;
				}
			}
			if(toAdd) {
				isToCopyMap.put(id, false);
			}
		}
		final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
		webContext.setVariable("user", userName);
		webContext.setVariable("categoryList", categoriesList);
		webContext.setVariable("isToCopyMap", isToCopyMap);
		webContext.setVariable("copyLink", false);
		webContext.setVariable("categoryID", categoryID);
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
