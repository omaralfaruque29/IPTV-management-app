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

import com.ipvision.domain.Country;

@Repository
public class CountryDaoImplementation implements CountryDao {

	@Autowired
	SessionFactory sessionFactory;
	
	@Override
	public Country returnCountryById(String countryId) {
		
		Country ct = null;
		
		Session dbSession = sessionFactory.openSession();			
		Transaction tx = null;
        try {
            tx = dbSession.beginTransaction();
            ct = (Country) dbSession.createCriteria(Country.class)
                    .add( Restrictions.eq("countryId", Integer.parseInt(countryId)) ).uniqueResult();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }            
        } finally {
        	dbSession.close();           
        }
		
		return ct;
	}

	@Override
	public List<Country> returnAllCountry(int startCountry,int selectedCountryPerPage) {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		List<Country> countries = new ArrayList<Country>();
		try {

			tx = dbsession.beginTransaction();
			Query query = (Query) dbsession.createQuery("FROM Country");
			query.setFirstResult(startCountry);
			query.setMaxResults(selectedCountryPerPage);
			countries = query.list();
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}
		
		return countries;
	}
	
	@Override
	public List<Country> returnAllCountry() {
		
		List countries = new ArrayList<Country>();
		
		Session ctDbSession = sessionFactory.openSession();
		Transaction countrytx = null;
		try {
			countrytx = ctDbSession.beginTransaction();
			Criteria cr = ctDbSession.createCriteria(Country.class)
				    .setProjection(Projections.projectionList() 
				      .add(Projections.property("countryId"), "countryId") 
				      .add(Projections.property("countryName"), "countryName")) 
				    .setResultTransformer(Transformers.aliasToBean(Country.class));
			countries = cr.list();
			
			countrytx.commit();
		} catch (HibernateException e) {
			if (countrytx != null)
				countrytx.rollback();
			e.printStackTrace();
		} finally {
			ctDbSession.close();
		}
		
		return countries;
	}

	@Override
	public int returnNumberOfCountry() {
		
		int totalCountry = 0;
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		
		try {

			tx = dbsession.beginTransaction();
			Long count = ((Long) (dbsession).createQuery(
					"select count(*) from Country").uniqueResult());
			totalCountry = count.intValue();
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}
		
		return totalCountry;
	}

	@Override
	public void saveCountry(Country country) throws Exception {
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		Integer id=0;
		try {
			tx = dbsession.beginTransaction();
			id = (Integer) dbsession.save(country);
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
	public void updateCountry(Country country, String countryId) throws Exception {
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			Country editCountry = (Country) dbsession.get(Country.class,
					Integer.parseInt(countryId));
			editCountry.setCountryName(country.getCountryName());
			dbsession.update(editCountry);
			tx.commit();			
			
		} catch (HibernateException e) {
			if (tx != null){
				tx.rollback();
			}
			throw e;
			
		} finally {
			dbsession.close();
		}		
	}
	
	@Override
	public void deleteCountry(Country country) throws Exception {
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			dbsession.delete(country);
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
