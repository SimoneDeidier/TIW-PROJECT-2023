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
import it.polimi.tiw.exceptions.AlreadyTooManyChildrenException;
import it.polimi.tiw.exceptions.InvalidParameterException;

public class CategoriesDAO {
	private Connection connection;
	
	public CategoriesDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Category> findAllCategories() throws SQLException {
		List<Category> categoriesList = new ArrayList<>();
		String query = "SELECT * FROM Category";
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		ResultSet result = preparedStatement.executeQuery();
		if(!result.isBeforeFirst()) {
			return null;
		}
		else {
			while(result.next()) {
				Category category = new Category();
				category.setCategoryID(result.getInt("categoryID"));
				category.setName(result.getString("name"));
				category.setParentID(result.getInt("parentID"));
				categoriesList.add(category);
			}
		}
		return orderCategoriesList(categoriesList, 0);
															
	}

	
	public boolean createCategory(String name, int parentID) throws SQLException {
		String query = "INSERT INTO Category VALUES (?, ?, ?)";
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		List<Category> categoriesList = findAllCategories();
		List<Integer> categoriesIndexesList = new ArrayList<>();
		if(categoriesList != null) {
			for(Category category : categoriesList) {
				categoriesIndexesList.add(category.getCategoryID());
			}
			int ninthChildren = Integer.parseInt(Integer.toString(parentID) + "9");
			if(parentID !=0 && (!categoriesIndexesList.contains(parentID) 
				|| categoriesIndexesList.contains(ninthChildren))) { //Check when parentID !=0
				return false;
			}
			if(parentID==0 && categoriesIndexesList.contains(ninthChildren))
				return false;
			int newCategoryID = findLastChildrenID(categoriesList, parentID) + 1;
			preparedStatement.setInt(1, newCategoryID);
			preparedStatement.setString(2, name);
			preparedStatement.setInt(3, parentID);
			preparedStatement.executeUpdate();
			return true;
		}
		preparedStatement.setInt(1, 1);
		preparedStatement.setString(2, name);
		preparedStatement.setInt(3, parentID);
		preparedStatement.executeUpdate();
		return true;
	}
	
	public int findLastChildrenID(List<Category> categoriesList, int parentID) {
		int maxIndex = 0;
		for(Category category : categoriesList) {
			if(category.getParentID() == parentID && category.getCategoryID() > maxIndex) {
				maxIndex = category.getCategoryID();
			}
		}
		return (maxIndex == 0) ? (parentID * 10) : maxIndex;
	}
	
	
	public boolean checkExistingCategoryFromID(int categoryID) throws SQLException {
		List<Category> categories = findAllCategories();
		for(Category category : categories) {
			if(category.getCategoryID() == categoryID) {
				return true;
			}
		}
		return false;
	}
	
	public List<Category> findSubCategories(int categoryID) throws SQLException {
		List<Category> resultCategories = new ArrayList<>();
		for(Category category : findAllCategories()) {
			if(category.getParentID() == categoryID) {
				resultCategories.add(category);
				resultCategories.addAll(findSubCategories(category.getCategoryID()));
			}
		}
		return resultCategories;
	}
	
	public List<Category> toCopyList(int categoryID) throws SQLException {
		List<Category> resCategories = new ArrayList<>();
		for(Category category : findAllCategories()) {
			if(category.getCategoryID() == categoryID) {
				resCategories.add(category);
				break;
			}
		}
		resCategories.addAll(findSubCategories(categoryID));
		return resCategories;
	}
	
	public void insertCopiedCategory(int categoryID,int parentID) throws AlreadyTooManyChildrenException, InvalidParameterException, SQLException { 
		List<Category> categoriesList = findAllCategories();
		List<Integer> categoriesIndexesList = new ArrayList<>();
		
		for(Category category : categoriesList) {
			categoriesIndexesList.add(category.getCategoryID());
		}
		if(!categoriesIndexesList.contains(categoryID) || (parentID != 0 && !categoriesIndexesList.contains(parentID))) {
			throw new InvalidParameterException();
		}
		
		int lastChildrenOfRoot = findLastChildrenID(categoriesList, parentID);
		if(lastChildrenOfRoot % 10 == 9) {
			throw new AlreadyTooManyChildrenException();
		}
		
		List<Category> subCategories = toCopyList(categoryID);
		int newRootID;
		if(lastChildrenOfRoot != 0) {
			newRootID = lastChildrenOfRoot + 1;
		}
		else {
			newRootID = (parentID*10) + 1;
		}
		
		connection.setAutoCommit(false); // disable autocommit
		try{
			for(Category category : subCategories) {
				String oldIDString = Integer.toString(category.getCategoryID());
				String oldRootIDString = Integer.toString(categoryID);
				String newRootIDString = Integer.toString(newRootID);
				String newIDString = oldIDString.replace(oldRootIDString, newRootIDString);
				int newID = Integer.parseInt(newIDString);
				int newParentID = newID / 10;
				
				addCategoryInDatabase(newID, category.getName(), newParentID);
			}
		}
		catch(SQLException e) {
				connection.rollback();
		}
		connection.setAutoCommit(true); // enable autocommit because the connection is shared
	}
	
	public void addCategoryInDatabase(int categoryID,String name,int parentID) throws SQLException {
		String query = "INSERT INTO Category VALUES (?, ?, ?)";
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		preparedStatement.setInt(1, categoryID);
		preparedStatement.setString(2, name);
		preparedStatement.setInt(3, parentID);
		preparedStatement.executeUpdate();
	}
	
	public List<Category> orderCategoriesList(List<Category> unordered, Integer parentID) {
		List<Category> result = new ArrayList<>();
		
		for(Category category : unordered) {
			if(category.getParentID() == parentID) {
				result.add(category);
				result.addAll(orderCategoriesList(unordered, category.getCategoryID()));
			}
		}
		return result;
	}
	
	
	
	

}
