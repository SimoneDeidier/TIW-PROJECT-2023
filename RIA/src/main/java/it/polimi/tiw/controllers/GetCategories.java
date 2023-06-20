package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import it.polimi.tiw.DAO.CategoriesDAO;
import it.polimi.tiw.beans.Category;

/**
 * Servlet implementation class GetCategories
 */
@WebServlet("/GetCategories")
@MultipartConfig
public class GetCategories extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetCategories() {
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
		CategoriesDAO categoriesDAO = new CategoriesDAO(connection);
		List<Category> categoriesList = null;
		try {
			categoriesList = categoriesDAO.findAllCategories();
		}
		catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("An error occurred with the database connection, please retry later!");
			return;
		}
		Gson gson = new Gson();
		String responseString = gson.toJson(categoriesList);
		System.out.println(responseString);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(responseString);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
