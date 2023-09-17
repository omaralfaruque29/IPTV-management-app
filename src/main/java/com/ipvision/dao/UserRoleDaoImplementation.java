package com.ipvision.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ipvision.domain.UserRole;

@Repository
public class UserRoleDaoImplementation implements UserRoleDao {

	@Autowired
	SessionFactory sessionFactory;

	@Override
	public List<UserRole> returnUserRolesListByUserId(int userId) {
		
		List rolesList = new ArrayList<UserRole>();
		
		Session session2 = sessionFactory.openSession();
	      Transaction tx2 = null;
	      try{
	         tx2 = session2.beginTransaction();
	         rolesList = session2.createQuery("FROM UserRole WHERE userId="+userId).list();
	         tx2.commit();
	      }catch (HibernateException e) {
	         if (tx2!=null) tx2.rollback();
	         e.printStackTrace(); 
	      }finally {
	         session2.close(); 
	      }
		
		return rolesList;
	}

	@Override
	public void saveUserRole(UserRole userRole) throws Exception {
		
		Session dbsession1 = sessionFactory.openSession();
		Transaction tx1 = null;
		try {
			tx1 = dbsession1.beginTransaction();
			Integer id = (Integer) dbsession1.save(userRole);
			tx1.commit();
		} catch (Exception e) {
			e.printStackTrace();
			if (tx1 != null) {
				tx1.rollback();
			}
			throw e;
		} finally {
			dbsession1.close();
		}
		
	}

	@Override
	public void deleteUserRoleByUserRoleId(int userRoleId) {
		
		Session dbsession = sessionFactory.openSession();
	      Transaction tx = null;
	      try{
	         tx = dbsession.beginTransaction();
	         UserRole role = 
	                   (UserRole)dbsession.get(UserRole.class, userRoleId); 
	         dbsession.delete(role); 
	         tx.commit();
	      }catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace();;
	      }finally {
	    	  dbsession.close(); 
	      }
		
	}
}
