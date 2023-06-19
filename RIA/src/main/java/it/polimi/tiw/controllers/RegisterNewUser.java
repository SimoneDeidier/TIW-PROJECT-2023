package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.DAO.UserDAO;

/**
 * Servlet implementation class RegisterNewUser
 */
@WebServlet("/RegisterNewUser")
@MultipartConfig
public class RegisterNewUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterNewUser() {
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
			this.connection = DriverManager.getConnection(url, user, password);
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
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		if(username == null || username.isEmpty() || password == null || password.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Username or password were missing, please refill the form!");
			return;
			
		}
		UserDAO userDAO = new UserDAO(this.connection);
		boolean registration = false;
		try {
			registration = userDAO.registerNewUser(username, password);
		}
		catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("An error occurred with the database connection, please refill the form!");
			return;
		}
		if(!registration) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Username is already taken, please provide a different one!");
			return;
		}
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("Registration succesful! You can log in with the form above!");
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