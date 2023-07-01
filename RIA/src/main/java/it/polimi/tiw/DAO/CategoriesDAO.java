package it.polimi.tiw.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import it.polimi.tiw.beans.Category;
import it.polimi.tiw.exceptions.TooLongIDException;

public class CategoriesDAO {
	private Connection connection;
	
	private final static long MAX_ID = 100000000000000000L;
	private final static int MAX_ID_LENGTH = 18;
	
	public CategoriesDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Category> findAllCategories() throws SQLException {
		List<Category> categoriesList = new ArrayList<>();
		String query = "SELECT * FROM category";
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		ResultSet result = preparedStatement.executeQuery();
		if(!result.isBeforeFirst()) {
			return null;
		}
		else {
			while(result.next()) {
				Category category = new Category();
				category.setCategoryID(result.getLong("categoryID"));
				category.setName(result.getString("name"));
				category.setParentID(result.getLong("parentID"));  //gets a zero if null
				categoriesList.add(category);
			}
		}
		return orderCategoriesList(categoriesList, (long) 0);
															
	}

	
	public boolean createCategory(String name, long parentID) throws SQLException, TooLongIDException {
		String query = "INSERT INTO category VALUES (?, ?, ?)";
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		List<Category> categoriesList = findAllCategories();
		List<Long> categoriesIndexesList = new ArrayList<>();
		if (parentID >= MAX_ID) {
			throw new TooLongIDException();
		}
		if(categoriesList != null) {
			for(Category category : categoriesList) {
				categoriesIndexesList.add(category.getCategoryID());
			}
			long ninthChildren = Long.parseLong(Long.toString(parentID) + "9");
			if(parentID != 0 && (!categoriesIndexesList.contains(parentID) 
				|| categoriesIndexesList.contains(ninthChildren))) { //Check when parentID !=0
				return false;
			}
			if(parentID == 0 && categoriesIndexesList.contains(ninthChildren)) {
				return false;
			}
			long newCategoryID = findLastChildrenID(categoriesList, parentID) + 1;
			preparedStatement.setLong(1, newCategoryID);
			preparedStatement.setString(2, name);
			if(parentID == 0) {
				preparedStatement.setNull(3, java.sql.Types.BIGINT);
			}
			else {
				preparedStatement.setLong(3, parentID);
			}
			preparedStatement.executeUpdate();
			return true;
		}
		preparedStatement.setLong(1, 1);
		preparedStatement.setString(2, name);
		if(parentID == 0) {
			preparedStatement.setNull(3, java.sql.Types.BIGINT);
		}
		else {
			preparedStatement.setLong(3, parentID);
		}
		preparedStatement.executeUpdate();
		return true;
	}
	
	public long findLastChildrenID(List<Category> categoriesList, long parentID) {
		long maxIndex = 0;
		for(Category category : categoriesList) {
			if(category.getParentID() == parentID && category.getCategoryID() > maxIndex) {
				maxIndex = category.getCategoryID();
			}
		}
		return (maxIndex == 0) ? (parentID * 10) : maxIndex;
	}
	
	public void insertCopiedCategory(List<Category> toInsertList) throws SQLException { 
		
		connection.setAutoCommit(false); // disable autocommit
		try{
			for(Category category:toInsertList) {
				addCategoryInDatabase(category.getCategoryID(), category.getName(), category.getParentID());
			}
		}
		catch(SQLException e) {
				connection.rollback();
		}
		connection.setAutoCommit(true); // enable autocommit because the connection is shared
	}
	
	public void addCategoryInDatabase(long newID, String name, long newParentID) throws SQLException {
		String query = "INSERT INTO category VALUES (?, ?, ?)";
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		preparedStatement.setLong(1, newID);
		preparedStatement.setString(2, name);
		if(newParentID == 0) {
			preparedStatement.setNull(3, java.sql.Types.BIGINT);
		}
		else {
			preparedStatement.setLong(3, newParentID);
		}
		preparedStatement.executeUpdate();
	}
	
	public List<Category> orderCategoriesList(List<Category> unordered, Long parentID) {
		List<Category> result = new ArrayList<>();
		
		for(Category category : unordered) {
			if(category.getParentID() == parentID) {
				result.add(category);
				result.addAll(orderCategoriesList(unordered, category.getCategoryID()));
			}
		}
		return result;
	}
	
	public void changeCategoryName(long categoryID, String newName) throws SQLException {
		String queryString = "UPDATE category SET name = ? WHERE categoryID = ?";
		PreparedStatement preparedStatement = connection.prepareStatement(queryString);
		preparedStatement.setString(1,  newName);
		preparedStatement.setLong(2, categoryID);
		preparedStatement.executeUpdate();
	}
	

}
