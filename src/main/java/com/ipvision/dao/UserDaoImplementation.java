package com.ipvision.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.ui.ModelMap;

import com.ipvision.domain.User;
import com.ipvision.domain.UserRole;

@Repository
public class UserDaoImplementation implements UserDao {

	@Autowired
	SessionFactory sessionFactory;
	
	@Override
	public boolean checkUserExist(User user) {
		
		boolean check = false;
		
		Session dbsession = sessionFactory.openSession();
		User userFromDb = null;
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			Query query = (Query) dbsession
					.createQuery("FROM User u where u.userName=:name and u.password=:pass");

			query.setParameter("name", user.getUserName());
			query.setParameter("pass", user.getPassword());
			List<User> list = query.list();
			if (list.size() > 0) {
				check = true;
			}
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}
		return check;
	}

	@Override
	public Set<UserRole> returnUserRoles(User user) {
        Set<UserRole> userRoles = new HashSet<UserRole>();
		
		Session dbsession = sessionFactory.openSession();
		User userFromDb = null;
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			Query query = (Query) dbsession
					.createQuery("FROM User u where u.userName=:name and u.password=:pass");

			query.setParameter("name", user.getUserName());
			query.setParameter("pass", user.getPassword());
			List<User> list = query.list();
			if (list.size() > 0) {
				userFromDb = list.get(0);
				userRoles = userFromDb.getUserRoles();
			}
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}
		return userRoles;
	}

	@Override
	public User returnUser(User user) {
		
        Session dbsession = sessionFactory.openSession();
		User userFromDb = null;
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			Query query = (Query) dbsession
					.createQuery("FROM User u where u.userName=:name and u.password=:pass");

			query.setParameter("name", user.getUserName());
			query.setParameter("pass", user.getPassword());
			List<User> list = query.list();
			if (list.size() > 0) {
				userFromDb = list.get(0);
			}
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}
		return userFromDb;
	}

	@Override
	public User returnUserById(String userId) {
		
		User usr = null;
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			usr = (User) dbsession.createCriteria(User.class)
					.add(Restrictions.eq("userId", Integer.parseInt(userId)))
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
		
		return usr;
	}

	@Override
	public List<User> returnAllUser(int startUser, int selectedUserPerPage) {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		List<User> users = new ArrayList<User>();
		try {

			tx = dbsession.beginTransaction();
			Query query = (Query) dbsession.createQuery("FROM User");
			query.setFirstResult(startUser);
			query.setMaxResults(selectedUserPerPage);
			users = query.list();
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}
		
		return users;
	}

	@Override
	public int returnNumberOfUser() {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		Integer totalUsers = 0;
		try {
			tx = dbsession.beginTransaction();
			Long count = ((Long) (dbsession).createQuery(
					"select count(*) from User").uniqueResult());
			totalUsers = count.intValue();
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}
		
		return totalUsers;
	}

	@Override
	public void saveUser(User user) throws Exception {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		Integer id = 0;
		try {
			tx = dbsession.beginTransaction();
			id = (Integer) dbsession.save(user);
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
	public void updateUser(User user, String userId) throws Exception {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			User editUser = (User) dbsession.get(User.class,
					Integer.parseInt(userId));
			editUser.setUserName(user.getUserName());
			editUser.setEmail(user.getEmail());
			editUser.setPassword(user.getPassword());
			editUser.setCountryName(user.getCountryName());
			dbsession.update(editUser);
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
	public void deleteUser(User user) throws Exception {
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			dbsession.delete(user);
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
