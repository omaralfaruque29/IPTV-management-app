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
import com.ipvision.domain.Language;

@Repository
public class LanguageDaoImplementation implements LanguageDao {

	@Autowired
	SessionFactory sessionFactory;
	
	@Override
	public Language returnLanguageById(String languageId) {
		
		Language ln = null;
		
		Session dbsession = sessionFactory.openSession();			
		Transaction tx = null;
        try {
            tx = dbsession.beginTransaction();
            ln = (Language) dbsession.createCriteria(Language.class)
                    .add( Restrictions.eq("languageId", Integer.parseInt(languageId)) ).uniqueResult();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }            
        } finally {
        	dbsession.close();           
        }
		
		return ln;
	}

	@Override
	public List<Language> returnAllLanguage(int startLanguage,int selectedLanguagePerPage) {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		List<Language> languages = new ArrayList<Language>();
		try {
			tx = dbsession.beginTransaction();
			Query query = (Query) dbsession.createQuery("FROM Language");
			query.setFirstResult(startLanguage);
			query.setMaxResults(selectedLanguagePerPage);
			languages = query.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}
		
		return languages;
	}
	
	@Override
	public List<Language> returnAllLanguage() {
		
		List languages = new ArrayList<Language>();
		
		Session languageDbSession = sessionFactory.openSession();
	      Transaction languagetx = null;
	      try{
	    	  languagetx = languageDbSession.beginTransaction();
	    	  Criteria cr = languageDbSession.createCriteria(Language.class)
					    .setProjection(Projections.projectionList() 
					      .add(Projections.property("languageId"), "languageId") 
					      .add(Projections.property("languageName"), "languageName")) 
					    .setResultTransformer(Transformers.aliasToBean(Language.class));
	    	  languages = cr.list();
	          languagetx.commit();
	      }catch (HibernateException e) {
	         if (languagetx!=null) languagetx.rollback();
	         e.printStackTrace(); 
	      }finally {
	    	  languageDbSession.close(); 
	      }
		
		return languages;
	}

	@Override
	public int returnNumberOfLanguage() {
		
		int totalLanguage = 0;
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			Long count = ((Long) (dbsession).createQuery(
					"select count(*) from Language").uniqueResult());
			totalLanguage = count.intValue();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}
		
		return totalLanguage;
	}

	@Override
	public void saveLanguage(Language language) throws Exception {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		Integer id=0;
		try {
			tx = dbsession.beginTransaction();
			id = (Integer) dbsession.save(language);
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
	public void updateLanguage(Language language, String languageId)
			throws Exception {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			Language editLanguage = (Language) dbsession.get(Language.class,
					Integer.parseInt(languageId));
			editLanguage.setLanguageName(language.getLanguageName());
			dbsession.update(editLanguage);
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
	public void deleteLanguage(Language language) throws Exception {
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			dbsession.delete(language);
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
