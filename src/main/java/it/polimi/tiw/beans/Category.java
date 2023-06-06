package it.polimi.tiw.beans;

public class Category {
	
	private int categoryID;
	private String name;
	private int parentID;
	
	public int getCategoryID() {
		return categoryID;
	}
	
	public void setCategoryID(int categoryID) {
		this.categoryID = categoryID;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getParentID() {
		return parentID;
	}
	
	public void setParentID(int parentID) {
		this.parentID = parentID;
	}

}
