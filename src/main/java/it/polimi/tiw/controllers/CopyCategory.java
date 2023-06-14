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
		String userName = (String) request.getSession().getAttribute("username");
		
		if(parentIDString == null || parentIDString.isEmpty()) {
			request.setAttribute("errorMessage", "An empty parameter was inserted, please select another category!");
			servletContext.getRequestDispatcher("/GoToHome").forward(request, response);
			return;
		}
		int parentID = 0;
		try {
			parentID = Integer.parseInt(parentIDString);
		}
		catch (NumberFormatException e) {
			request.setAttribute("errorMessage", "Parent ID's format is not acceptable, please select another category!");
			servletContext.getRequestDispatcher("/GoToHome").forward(request, response);
			return;
		}
		CategoriesDAO categoriesDAO = new CategoriesDAO(connection);
		boolean parameterOK;
		List<Category> categoriesList = null;
		List<Category> toCopyList = null;
		try {
			parameterOK = categoriesDAO.checkExistingCategoryFromID(parentID);
			if(!parameterOK) {
				request.setAttribute("errorMessage", "Wrong parent ID in the query string, please select another category!");
				servletContext.getRequestDispatcher("/GoToHome").forward(request, response);
				return;	
			}
			categoriesList = categoriesDAO.findAllCategories();
			toCopyList = categoriesDAO.toCopyList(parentID);
		}
		catch (SQLException e) {
			request.setAttribute("errorMessage", "An errorr ocurred whith the database connection!");
			servletContext.getRequestDispatcher("/GoToHome").forward(request, response);
			return;
		}
		Map<Integer, Boolean> isToCopyMap = new HashMap<>();
		for(Category category : categoriesList) {
			boolean toAdd = true;
			int categoryID = category.getCategoryID();
			for(Category toCopyCategory : toCopyList) {
				if(categoryID == toCopyCategory.getCategoryID()) {
					isToCopyMap.put(categoryID, true);
					toAdd = false;
					break;
				}
			}
			if(toAdd) {
				isToCopyMap.put(categoryID, false);
			}
		}
		final WebContext webContext = new WebContext(request, response, servletContext, request.getLocale());
		webContext.setVariable("user", userName);
		webContext.setVariable("categoryList", categoriesList);
		webContext.setVariable("isToCopyMap", isToCopyMap);
		webContext.setVariable("copyLink", false);
		webContext.setVariable("parentID", parentID);
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
