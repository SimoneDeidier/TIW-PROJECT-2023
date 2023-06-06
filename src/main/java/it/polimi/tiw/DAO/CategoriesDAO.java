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

	
	public boolean createCategory(String name, int parentID) throws SQLException {
		String query = "INSERT INTO Category VALUES (?, ?, ?)";
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		
		List<Category> categoriesList = findAllCategories();
		List<Integer> categoriesIndexesList = new ArrayList<>();
		for(Category category : categoriesList) {
			categoriesIndexesList.add(category.getCategoryID());
		}
		int ninthChildren = Integer.parseInt(Integer.toString(parentID) + "9");
		if(!categoriesIndexesList.contains(parentID) || categoriesIndexesList.contains(ninthChildren)) {
			return false;
		}
		int newCategoryID = findLastChildrenID(categoriesList, parentID) + 1;
		preparedStatement.setInt(1, newCategoryID);
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

}
