package it.polimi.tiw.beans;

public class Category {
	
	private long categoryID;
	private String name;
	private long parentID;
	
	public long getCategoryID() {
		return categoryID;
	}
	
	public void setCategoryID(long categoryID) {
		this.categoryID = categoryID;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public long getParentID() {
		return parentID;
	}
	
	public void setParentID(long parentID) {
		this.parentID = parentID;
	}

}
