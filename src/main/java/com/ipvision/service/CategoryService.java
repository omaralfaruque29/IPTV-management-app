package com.ipvision.service;

import java.util.List;

import com.ipvision.domain.Category;

public interface CategoryService {
	
    public Category returnCategoryById(String categoryId);
	
	public List<Category> returnAllCategory(int startCategory,int selectedCategoryPerPage);
	
	public List<Category> returnAllCategory();
	
	public int returnNumberOfCategory();
	
	public void saveCategory(Category category) throws Exception;
	
	public void updateCategory(Category category,String categoryId) throws Exception;
	
	public void deleteCategory(Category category) throws Exception;

}
