package com.ipvision.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ipvision.domain.Role;


@Repository
public class RoleDaoImplementation implements RoleDao {

	@Autowired
	SessionFactory sessionFactory;
	
	@Override
	public Role returnRoleById(String roleId) {
		
		Role rl = null;
		
		Session dbSession = sessionFactory.openSession();			
		Transaction tx = null;
        try {
            tx = dbSession.beginTransaction();
            rl = (Role) dbSession.createCriteria(Role.class)
                    .add( Restrictions.eq("roleId", Integer.parseInt(roleId)) ).uniqueResult();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }            
        } finally {
        	dbSession.close();           
        }
		
		return rl;
	}

	@Override
	public List<Role> returnAllRole(String pageNumber, int rolePerPage) {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		List<Role> roles = new ArrayList<Role>();
		int pageNo = Integer.parseInt(pageNumber);
		try {

			tx = dbsession.beginTransaction();
			Query query = (Query) dbsession.createQuery("FROM Role");
			query.setFirstResult((pageNo - 1) * 10);
			query.setMaxResults(rolePerPage);
			roles = query.list();
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}
		
		return roles;
	}
	
	@Override
	public List<Role> returnAllRole() {
		
		List roles = new ArrayList<Role>();
		
		Session rlDbSession = sessionFactory.openSession();
		Transaction roletx = null;
		try {
			roletx = rlDbSession.beginTransaction();
			roles = rlDbSession.createQuery("FROM Role").list();
			
			roletx.commit();
		} catch (HibernateException e) {
			if (roletx != null)
				roletx.rollback();
			e.printStackTrace();
		} finally {
			rlDbSession.close();
		}
		
		return roles;
	}

	@Override
	public int returnNumberOfRole() {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		Integer totalRole = 0;
		try {
			tx = dbsession.beginTransaction();
			Long count = ((Long) (dbsession).createQuery(
					"select count(*) from Role").uniqueResult());
			totalRole = count.intValue();
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}
		
		return totalRole;
	}

	@Override
	public void saveRole(Role role) throws Exception {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		Integer id=0;
		try {
			tx = dbsession.beginTransaction();
			id = (Integer) dbsession.save(role);
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
	public void updateRole(Role role, String roleId) throws Exception {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			Role editRole = (Role) dbsession.get(Role.class,
					Integer.parseInt(roleId));
			editRole.setRoleName(role.getRoleName());
			dbsession.update(editRole);
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
	public void deleteRole(Role role) throws Exception {
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			dbsession.delete(role);
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
