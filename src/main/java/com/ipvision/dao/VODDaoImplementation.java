package com.ipvision.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ipvision.domain.LiveChannel;
import com.ipvision.domain.PrimaryTag;
import com.ipvision.domain.Tag;
import com.ipvision.domain.VOD;

@Repository
public class VODDaoImplementation implements VODDao {
	
	@Autowired
	SessionFactory sessionFactory;

	@Override
	public VOD returnVODById(String vodId) {
		
		VOD vd = null;
		
		Session dbsession = sessionFactory.openSession();			
		Transaction tx = null;
        try {
            tx = dbsession.beginTransaction();
            vd = (VOD) dbsession.createCriteria(VOD.class).add( Restrictions.eq("videoId", Integer.parseInt(vodId)) ).uniqueResult();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }            
        } finally {
        	//dbsession.close();           
        }
		
		return vd;
	}

	@Override
	public List<VOD> returnAllVOD(String pageNumber, int vodPerPage) {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		List<VOD> vods = new ArrayList<VOD>();
		
	      try{
	    	  int pageNo = Integer.parseInt(pageNumber);
	         tx = dbsession.beginTransaction();
	         Query query = (Query) dbsession.createQuery("FROM VOD");
	         query.setFirstResult(pageNo);
	         query.setMaxResults(vodPerPage);
	         vods = query.list();	        
	         tx.commit();
	      }catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace(); 
	      }finally {
	    	  dbsession.close(); 
	      }
		
		return vods;
	}

	@Override
	public int returnNumberOfVOD() {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		Integer totalVod = 0;
	      try{
	         tx = dbsession.beginTransaction();
	         Long count = ((Long) (dbsession).createQuery(
						"select count(*) from VOD").uniqueResult());
	         totalVod = count.intValue();	        
	         tx.commit();
	      }catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace(); 
	      }finally {
	    	  dbsession.close(); 
	      }
		
		return totalVod;
	}

	@Override
	public void saveVOD(VOD vod) throws Exception {
		Session vodDbSsession = sessionFactory.openSession();
		Transaction vodTx = null;
		try {
			vodTx = vodDbSsession.beginTransaction();
			vodDbSsession.save(vod);
			vodTx.commit();

		} catch (Exception e) {
			e.printStackTrace();
			if (vodTx != null) {
				vodTx.rollback();
			}

			throw e;

		} finally {
			vodDbSsession.close();
		}

	}
	
	@Override
	public void deleteVOD(VOD vod) throws Exception {
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			dbsession.delete(vod);
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
	public void updateVOD(VOD vod, String vodId) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<VOD> getLatestFiveVods() {
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		List<VOD> vods = new ArrayList<VOD>();
		
	      try{
	         tx = dbsession.beginTransaction();
	         Query query = (Query) dbsession.createQuery("FROM VOD ORDER BY videoId DESC");
	         query.setMaxResults(5);
	         vods = query.list();	        
	         tx.commit();
	      }catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace(); 
	      }finally {
	    	  dbsession.close(); 
	      }
		return vods;
	}
	
	@Override
	public List<VOD> getSearchedVodList(String[] tagsArray) {
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		List<VOD> vodList = new ArrayList<VOD>();

		try {
			tx = dbsession.beginTransaction();
			for (int i = 0; i < tagsArray.length; i++) {
				String singleTag = "%" + tagsArray[i] + "%";
				Query primaryTagQuery = (Query) dbsession.createQuery("FROM PrimaryTag PT WHERE PT.primaryTagName like :primaryTagName");
				List<PrimaryTag> primaryTags = primaryTagQuery.setParameter("primaryTagName", singleTag).list();
				if (primaryTags.size() > 0) {
					for(PrimaryTag pTag : primaryTags){
						Set<VOD> primaryTagVodSet = pTag.getVodSet();
						for (VOD vod : primaryTagVodSet) {
							if (!vodList.contains(vod)) {
								vodList.add(vod);
							}
						}
					}
					
				} 
			
					 Query secondaryTagQuery = (Query) dbsession.createQuery("FROM Tag T WHERE T.tagName like :tagName");
					 List<Tag> tags = secondaryTagQuery.setParameter("tagName", singleTag).list();
					 if(tags.size() > 0){
						 for(Tag tag : tags){
							 Set<VOD> secondaryTagVodSet = tag.getVodSet();
								for (VOD vod : secondaryTagVodSet) {
									if (!vodList.contains(vod)) {
										vodList.add(vod);
									}
								}
						 }
						 
					 }

			}

			tx.commit();
		} catch (HibernateException ex) {
			if (tx != null) {
				tx.rollback();
			}
			ex.printStackTrace();
		} finally {
			dbsession.close();
		}
		return vodList;
	}

}