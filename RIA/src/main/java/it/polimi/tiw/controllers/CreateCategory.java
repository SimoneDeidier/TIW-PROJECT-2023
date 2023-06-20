package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.DAO.CategoriesDAO;
import it.polimi.tiw.exceptions.TooLongIDException;

/**
 * Servlet implementation class CreateCategory
 */
@WebServlet("/CreateCategory")
@MultipartConfig
public class CreateCategory extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
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
		
		if(parentIDString == null || parentIDString.isEmpty() || nameString == null || nameString.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Parent ID or name were missing, please refill the form!");
			return;
		}
		long parentID = 0;
		if(!Objects.equals(parentIDString, "root")) {
			try {
				parentID = Long.parseLong(parentIDString);
			}
			catch (NumberFormatException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Parent ID's format is not acceptable, please refill the form!");
				return;
			}
			if(parentID <= 0) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Parent ID's format is not acceptable, please refill the form!");
				return;
			}
		}
		CategoriesDAO categoriesDAO = new CategoriesDAO(connection);
		boolean creationOk;
		try {
			creationOk = categoriesDAO.createCategory(nameString, parentID);
		}
		catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("An error occurred with the database connection, please refill the form!");
			return;
		}
		catch (TooLongIDException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("The chosen parent can't have childrens, this branch has reached its maximum length!");
			return;
		}
		if(!creationOk) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("An error occurred with the creation of the new category, please remember that a category may have a maximum of nine childrens!");
			return;
		}
		response.setStatus(HttpServletResponse.SC_OK);
	}

}
