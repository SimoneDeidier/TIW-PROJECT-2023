package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.beans.User;

public class UserDAO {
	
	private Connection connection;
	
	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	public User checkLogin(String username, String password) throws SQLException {
		String query = "SELECT username, password FROM User WHERE username = ? AND password = ?";
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, username);
		preparedStatement.setString(2, password);
		ResultSet result = preparedStatement.executeQuery();
		if(!result.isBeforeFirst()) {
			return null;
		}
		else {
			result.next();
			User user = new User();
			user.setUsername(result.getString("username"));
			user.setPassword(result.getString("password"));
			return user;
		}
	}

}
