package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
import it.polimi.tiw.exceptions.TooLongIDException;

/**
 * Servlet implementation class InsertCopiedCategory
 */
@WebServlet("/InsertCopiedCategory")
@MultipartConfig
public class InsertCopiedCategory extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
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
    	
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    Gson gson = new Gson();
	    Category[] categoryArray = gson.fromJson(request.getParameter("jsonData"), Category[].class);
	    ArrayList<Category> categoriesToBeInsertedList = new ArrayList<>(Arrays.asList(categoryArray));
	    
	    if(categoriesToBeInsertedList.size()==0 ) {
	    	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Error during the process, retry doing the operation!");
			return;
	    }
	    for(Category category:categoriesToBeInsertedList) {
	    	if(category.getCategoryID() <= 0 || category.getName().length()==0 || category.getParentID()<0) {
	    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Error during the process, retry doing the operation!");
				return;
	    	}
	    }
	    
	    //make sure it's ordered by categoryID
	    categoriesToBeInsertedList.sort(Comparator.comparing(Category::getCategoryID));
	  
		CategoriesDAO categoriesDAO = new CategoriesDAO(connection);
		List<Category> allCategoryList = new ArrayList<>();
		try {
			allCategoryList = categoriesDAO.findAllCategories();
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("An error occurred with the database connection, please retry doing the operation!");
			return;
		}
		for(Category category:categoriesToBeInsertedList) {
			for(Category cat : allCategoryList) {
				if(category.getCategoryID() == cat.getCategoryID()) { //the category ID we're trying to insert it's already in the list
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println("Error during the process, retry doing the operation!");
					return;
				}
			}
		}
		//checking first element to be added
		if(Long.toString(categoriesToBeInsertedList.get(0).getCategoryID()).length() != 1) {
			boolean check= false;
			long IDToCheck = categoriesToBeInsertedList.get(0).getCategoryID() / 10;
			for(Category category : allCategoryList) {
				if(category.getCategoryID() == IDToCheck)
					check=true;
			}
			if(!check) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Error during the process, retry doing the operation!");
				return;
			}
		}
		//Checking all other elements
		for(Category category:categoriesToBeInsertedList) {
			if(!Long.toString(category.getCategoryID()).contains(Long.toString(categoriesToBeInsertedList.get(0).getCategoryID()))) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Error during the process, retry doing the operation!");
				return;
			}
		}
		
	    try {
			categoriesDAO.insertCopiedCategory(categoriesToBeInsertedList);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("An error occurred with the database connection, please retry doing the operation!");
			return;
		}
	    response.setStatus(HttpServletResponse.SC_OK);
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
