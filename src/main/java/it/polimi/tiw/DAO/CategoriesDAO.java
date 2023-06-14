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
	
	
	/*  public List<Category> findSubCategories(int categoryID) throws SQLException { //also contains the element we're looking for the sub categories for
		List<Category> categoryList=findAllCategories();
		int lastChildrenID= findLastChildrenID(categoryList,categoryID);
		int previousLastChildrenID=lastChildrenID;
		while(lastChildrenID!=0) {
			previousLastChildrenID=lastChildrenID;
			lastChildrenID= findLastChildrenID(categoryList,lastChildrenID);
		}
		List<Category> subCategoriesList=new ArrayList<>();
		boolean check=false;
		for(Category category:categoryList) {
			if(category.getCategoryID()==categoryID)
				check=true;
			if(check)
				subCategoriesList.add(category);
			if(category.getCategoryID()==previousLastChildrenID || previousLastChildrenID==0)
				check=false;
		}
		return subCategoriesList; //returns only the category corresponding to categoryID if it doesn't have any subCategories
		
	}*/
	
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
		if(!categoriesIndexesList.contains(categoryID) || !categoriesIndexesList.contains(parentID))
			throw new InvalidParameterException();
		int lastChildrenOfParentRightNow=findLastChildrenID(categoriesList,parentID);
		if(lastChildrenOfParentRightNow%10==9)
			throw new AlreadyTooManyChildrenException();
		
		List<Category> subCategories=findSubCategories(categoryID);
		int newIDForCategoryID;
		if(lastChildrenOfParentRightNow!=0)
			newIDForCategoryID=lastChildrenOfParentRightNow+1;
		else
			newIDForCategoryID= (parentID*10) +1;
		
		connection.setAutoCommit(false); // disable autocommit
		try{
			for(Category category:subCategories) {
			if(category.getCategoryID()==categoryID)//it's the first to be added 
				addCategoryInDatabase(newIDForCategoryID,category.getName(),parentID);
			int newParentID;
			if(category.getParentID()==categoryID)
				newParentID=newIDForCategoryID;
			else {
				newParentID=calculateNewID(newIDForCategoryID,category.getParentID());
			}
			addCategoryInDatabase(calculateNewID(newIDForCategoryID,category.getCategoryID()),category.getName(),newParentID);
			}
		}catch(SQLException e) {
				connection.rollback();
			}
		connection.setAutoCommit(true); // enable autocommit because the connection is shared
	}
		
	
	public int calculateNewID(int newIDForCategoryID,int previousID) {
		String previousIDString = Integer.toString(previousID);
		String newIDForCategoryIDString= Integer.toString(newIDForCategoryID);
		previousIDString.substring(newIDForCategoryIDString.length());
		return Integer.parseInt(newIDForCategoryIDString.concat(previousIDString));
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
