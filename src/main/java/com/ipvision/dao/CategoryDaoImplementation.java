package com.ipvision.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ipvision.domain.Category;
import com.ipvision.domain.Language;

@Repository
public class CategoryDaoImplementation implements CategoryDao {

	@Autowired
	SessionFactory sessionFactory;
	
	@Override
	public Category returnCategoryById(String categoryId) {
		
		Category cat = null;
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			cat = (Category) dbsession
					.createCriteria(Category.class)
					.add(Restrictions.eq("categoryId", Integer.parseInt(categoryId)))
					.uniqueResult();
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			dbsession.close();
		}
		
		return cat;
	}
	
	@Override
	public List<Category> returnAllCategory(int startCategory,
			int selectedCategoryPerPage) {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		List<Category> categories = new ArrayList<Category>();
		try {
			tx = dbsession.beginTransaction();
			Query query = (Query) dbsession.createQuery("FROM Category");
			query.setFirstResult(startCategory);
			query.setMaxResults(selectedCategoryPerPage);
			categories = query.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}
		
		return categories;
	}
	
	@Override
	public List<Category> returnAllCategory() {
		
		List categories = new ArrayList<Category>();
		
		Session categoryDbSession = sessionFactory.openSession();
	      Transaction categorytx = null;
	      try{
	    	  categorytx = categoryDbSession.beginTransaction();
	         // categories = categoryDbSession.createQuery("SELECT categoryId,categoryName FROM Category").list();
	    	  Criteria cr = categoryDbSession.createCriteria(Category.class)
					    .setProjection(Projections.projectionList() 
					      .add(Projections.property("categoryId"), "categoryId") 
					      .add(Projections.property("categoryName"), "categoryName")) 
					    .setResultTransformer(Transformers.aliasToBean(Category.class));
	    	  categories = cr.list();
	          categorytx.commit();
	      }catch (HibernateException e) {
	         if (categorytx!=null) categorytx.rollback();
	         e.printStackTrace(); 
	      }finally {
	    	  categoryDbSession.close(); 
	      }
		
		return categories;
	}

	@Override
	public int returnNumberOfCategory() {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		Integer totalCategory = 0;
		try {
			tx = dbsession.beginTransaction();			
			Long count = ((Long) (dbsession).createQuery(
					"select count(*) from Category").uniqueResult());
			totalCategory = count.intValue();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}
		
		return totalCategory;
	}

	@Override
	public void saveCategory(Category category) throws Exception {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		Integer id = 0;
		try {
			tx = dbsession.beginTransaction();
			id = (Integer) dbsession.save(category);
			tx.commit();

		} catch (Exception e) {
			e.printStackTrace();
			if (tx != null) {
				tx.rollback();
			}
			throw e;

		} finally {
			dbsession.close();
		}
		
	}

	@Override
	public void updateCategory(Category category, String categoryId)
			throws Exception {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			Category editCategory = (Category) dbsession.get(
					Category.class, Integer.parseInt(categoryId));
			editCategory.setCategoryName(category.getCategoryName());
			dbsession.update(editCategory);
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
			throw e;

		} finally {
			dbsession.close();
		}
		
	}
	
	@Override
	public void deleteCategory(Category category) throws Exception {
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			dbsession.delete(category);
			tx.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			if (tx != null) {
				tx.rollback();
			}
			throw e;
			
		} finally {
			dbsession.close();
		}
	}



}
