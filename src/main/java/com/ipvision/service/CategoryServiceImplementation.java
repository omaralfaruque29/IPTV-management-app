package com.ipvision.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipvision.dao.CategoryDao;
import com.ipvision.domain.Category;

@Service
public class CategoryServiceImplementation implements CategoryService {

	@Autowired
	CategoryDao categoryDao;
	
	@Override
	public Category returnCategoryById(String categoryId) {
		
		return categoryDao.returnCategoryById(categoryId);
	}
	
	@Override
	public List<Category> returnAllCategory(int startCategory,
			int selectedCategoryPerPage) {
		
		return categoryDao.returnAllCategory(startCategory, selectedCategoryPerPage);
	}
	
	@Override
	public List<Category> returnAllCategory() {
		
		return categoryDao.returnAllCategory();
	}

	@Override
	public int returnNumberOfCategory() {
		
		return categoryDao.returnNumberOfCategory();
	}

	@Override
	public void saveCategory(Category category) throws Exception {
		
		categoryDao.saveCategory(category);
		
	}

	@Override
	public void updateCategory(Category category, String categoryId)
			throws Exception {
		
		categoryDao.updateCategory(category, categoryId);
		
	}
	
	@Override
	public void deleteCategory(Category category) throws Exception {
		categoryDao.deleteCategory(category);
		
	}


}
