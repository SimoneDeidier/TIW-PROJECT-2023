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
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.DAO.CategoriesDAO;
import it.polimi.tiw.exceptions.AlreadyTooManyChildrenException;
import it.polimi.tiw.exceptions.InvalidParameterException;

/**
 * Servlet implementation class InsertCopiedCategory
 */
@WebServlet("/InsertCopiedCategory")
public class InsertCopiedCategory extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InsertCopiedCategory() {
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
		String parentIDString = request.getParameter("parentID");
		ServletContext servletContext = getServletContext();
		
		if(categoryIDString == null || categoryIDString.isEmpty() || parentIDString == null || parentIDString.isEmpty()) {
			request.setAttribute("errorMessage", "An empty parameter was inserted, please redo the copying operation!");
			servletContext.getRequestDispatcher("/GoToHome").forward(request, response);
			return;
		}
		long categoryID = 0, parentID = 0;
		try {
			categoryID = Long.parseLong(categoryIDString);
			if(!Objects.equals(parentIDString, "root")) {
					parentID = Long.parseLong(parentIDString);
			}
		}
		catch (NumberFormatException e) {
			request.setAttribute("errorMessage", "A category ID's format is not acceptable, please redo the copying operation!");
			servletContext.getRequestDispatcher("/GoToHome").forward(request, response);
			return;
		}
		CategoriesDAO categoriesDAO = new CategoriesDAO(connection);
		try {
			categoriesDAO.insertCopiedCategory(categoryID, parentID);
		} catch (AlreadyTooManyChildrenException e) {
			request.setAttribute("errorMessage", "The parent category you have chosen already has the maximum number of children, please redo the copy operation!");
			servletContext.getRequestDispatcher("/GoToHome").forward(request, response);
			return;	
		} catch (InvalidParameterException e) {
			request.setAttribute("errorMessage", "Wrong category in the query string, please redo the copying operation!");
			servletContext.getRequestDispatcher("/GoToHome").forward(request, response);
			return;	
		} catch (SQLException e) {
			request.setAttribute("errorMessage", "An error occurred with the database connection!");
			servletContext.getRequestDispatcher("/GoToHome").forward(request, response);
			return;
		}
		response.sendRedirect(getServletContext().getContextPath() + "/GoToHome");
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
