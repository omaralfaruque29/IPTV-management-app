package com.ipvision.dao;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ipvision.domain.LiveChannel;
import com.ipvision.domain.PrimaryTag;
import com.ipvision.domain.Tag;
import com.ipvision.domain.User;

@Repository
public class LiveChannelDaoImplementation implements LiveChannelDao {

	@Autowired
	SessionFactory sessionFactory;

	@Override
	public LiveChannel returnLiveChannelById(String liveChannelId) {
		
		LiveChannel ch = null;
		
		Session dbsession = sessionFactory.openSession();			
		Transaction tx = null;
		//Session dbsession = sessionFactory.getCurrentSession();
		//dbsession.beginTransaction();
        try {
            tx = dbsession.beginTransaction();
            ch = (LiveChannel) dbsession.createCriteria(LiveChannel.class).add( Restrictions.eq("channelId", Integer.parseInt(liveChannelId)) ).uniqueResult();         
            
//            Criteria cr = dbsession.createCriteria(LiveChannel.class).add(Restrictions.eq("channelId",Integer.parseInt(liveChannelId)))
//            		.setProjection(Projections.projectionList()
//            		.add(Projections.property("channelId"), "channelId")
//            		.add(Projections.property("channelName"), "channelName")
//            		.add(Projections.property("tag"), "tag"))
//            		.setResultTransformer(Transformers.aliasToBean(LiveChannel.class));
//            		List<LiveChannel> list = cr.list();
//            		System.out.println("list.size():" +list.size());
//            		ch = list.get(0);
//            		for(LiveChannel chnl : list){
//            			if(chnl.getChannelId() == Integer.parseInt(liveChannelId)){
//            				ch = chnl;
//            				break;
//            			}
//            		}
            tx.commit();
   //         dbsession.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
            	tx.rollback();
            }            
        } finally {
        	//dbsession.clear();           
        }
		return ch;
	}

	@Override
	public List<LiveChannel> returnAllLiveChannel(int startChannel,int selectedLiveChannelPerPage) {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		List<LiveChannel> channels = new ArrayList<LiveChannel>();
		
	      try{
	         tx = dbsession.beginTransaction();
	         Query query = (Query) dbsession.createQuery("FROM LiveChannel");
	         query.setFirstResult(startChannel);
	         query.setMaxResults(selectedLiveChannelPerPage);
	         channels = query.list();	        
	         tx.commit();
	      }catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace(); 
	      }finally {
	    	  dbsession.close(); 
	      }
		
		return channels;
	}
	
	@Override
	public List<LiveChannel> returnAllLiveChannelByUserId(int startChannel,
			int liveChannelPerPage, int userId) {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		List<LiveChannel> channels = new ArrayList<LiveChannel>();
	      try{
	         tx = dbsession.beginTransaction();
	         Query query = (Query) dbsession.createQuery("FROM LiveChannel WHERE userId="+userId);
	         query.setFirstResult(startChannel);
	         query.setMaxResults(liveChannelPerPage);
	         channels = query.list();	        
	         tx.commit();
	      }catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace(); 
	      }finally {
	    	  dbsession.close(); 
	      }
		
		return channels;
	}
	
	@Override
	public List<LiveChannel> returnUpChannelsByUserId(int userId) {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		List<LiveChannel> channels = new ArrayList<LiveChannel>();
		
	      try{
	         tx = dbsession.beginTransaction();
	         Query query = (Query) dbsession.createQuery("FROM LiveChannel L WHERE L.user.userId="+userId+" AND L.state='up'");
	         channels = query.list();	        
	         tx.commit();
	      }catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace(); 
	      }finally {
	    	  dbsession.close(); 
	      }
		
		return channels;
	}

	@Override
	public List<LiveChannel> returnDownChannelsByUserId(int userId) {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		List<LiveChannel> channels = new ArrayList<LiveChannel>();
		
	      try{
	         tx = dbsession.beginTransaction();
	         Query query = (Query) dbsession.createQuery("FROM LiveChannel L WHERE L.user.userId="+userId+" AND L.state='down'");
	         channels = query.list();	        
	         tx.commit();
	      }catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace(); 
	      }finally {
	    	  dbsession.close(); 
	      }
		
		return channels;
	}

	@Override
	public List<LiveChannel> getLatestFiveChannels() {
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		List<LiveChannel> channels = new ArrayList<LiveChannel>();
		
	      try{
	         tx = dbsession.beginTransaction();
	         Query query = (Query) dbsession.createQuery("FROM LiveChannel ORDER BY channelId DESC");
	         query.setMaxResults(5);
	         channels = query.list();	        
	         tx.commit();
	      }catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace(); 
	      }finally {
	    	  dbsession.close(); 
	      }
		
		return channels;
	}
	
	@Override
	public int returnNumberOfLiveChannel() {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		Integer totalChannel = 0;
	      try{
	    	  tx = dbsession.beginTransaction();
	          Long count = ((Long) (dbsession).createQuery(
						"select count(*) from LiveChannel").uniqueResult());
	         totalChannel = count.intValue();	        
	         tx.commit();
	      }catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace(); 
	      }finally {
	    	  dbsession.close(); 
	      }
		
		return totalChannel;
	}
	
	@Override
	public int returnNumberOfLiveChannelByUserId(int userId) {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		Integer totalChannel = 0;
	      try{
	         tx = dbsession.beginTransaction();
	         Long count = ((Long) (dbsession).createQuery(
						"select count(*) from LiveChannel WHERE userId="+userId).uniqueResult());
	         totalChannel = count.intValue();	        
	         tx.commit();
	      }catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace(); 
	      }finally {
	    	  dbsession.close(); 
	      }
		
		return totalChannel;
	}

	@Override
	public void saveLiveChannel(LiveChannel liveChannel) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public int saveLiveChannel(LiveChannel liveChannel, String logo,
			User user, String link720, String link480,String link360,
			String link180, String linkReturnToUser, String state)
			throws Exception {
		
		Session channelDbSsession = sessionFactory.openSession();
		Transaction channelTx = null;
		Integer id=0;
		try {
			channelTx = channelDbSsession.beginTransaction();
			liveChannel.setLogo(logo);
			liveChannel.setUser(user);
			liveChannel.setLink720(link720);
			liveChannel.setLink480(link480);
			liveChannel.setLink360(link360);
			liveChannel.setLink180(link180);
			liveChannel.setLinkReturnToUser(linkReturnToUser);
			liveChannel.setState(state);
			id = (Integer) channelDbSsession.save(liveChannel);
			channelTx.commit();
			
			

		} catch (Exception e) {
			e.printStackTrace();
			if (channelTx != null) {
				channelTx.rollback();
			}
			
			throw e;
			
		} finally {
			channelDbSsession.close();
		}
		
		return id;
		
	}

	@Override
	public void updateLiveChannel(LiveChannel liveChannel)
			throws Exception {
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			dbsession.update(liveChannel);
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
	public void deleteChannel(LiveChannel channel) throws Exception {
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			dbsession.delete(channel);
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
	
	/**
	 * 
	 * for web service
	 */

	@Override
	public List<LiveChannel> returnChannelsByCountryId(int countryId) {
		List liveChannels = new ArrayList<LiveChannel>();
		Session dbSession = sessionFactory.openSession();
		Transaction tx = null;
		
		try{
			tx = dbSession.beginTransaction();
			liveChannels= dbSession.createQuery("From LiveChannel WHERE countryId ="+countryId).list();
			tx.commit();
		}catch(Exception e){
			e.printStackTrace();
			if(tx!=null){
				tx.rollback();
			}
		}finally{
			dbSession.close();
		}
		
		return liveChannels;
	}

	@Override
	public List<LiveChannel> getSearchedChannelList(String[] tagsArray) {
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		List<LiveChannel> liveChannelList = new ArrayList<LiveChannel>();

		try {
			tx = dbsession.beginTransaction();
			for (int i = 0; i < tagsArray.length; i++) {
				String singleTag = "%" + tagsArray[i] + "%";
				Query primaryTagQuery = (Query) dbsession.createQuery("FROM PrimaryTag PT WHERE PT.primaryTagName like :primaryTagName");
				List<PrimaryTag> primaryTags = primaryTagQuery.setParameter("primaryTagName", singleTag).list();
				if (primaryTags.size() > 0) {
					for(PrimaryTag pTag : primaryTags){
						Set<LiveChannel> primaryTagChannelSet = pTag.getLiveChannelSet();
						for (LiveChannel ch : primaryTagChannelSet) {
							if (!liveChannelList.contains(ch)) {
								liveChannelList.add(ch);
							}
						}
					}
					
				} 
			
					 Query secondaryTagQuery = (Query) dbsession.createQuery("FROM Tag T WHERE T.tagName like :tagName");
					 List<Tag> tags = secondaryTagQuery.setParameter("tagName", singleTag).list();
					 if(tags.size() > 0){
						 for(Tag tag : tags){
							 Set<LiveChannel> secondaryTagChannelSet = tag.getLiveChannelSet();
								for (LiveChannel ch : secondaryTagChannelSet) {
									if (!liveChannelList.contains(ch)) {
										liveChannelList.add(ch);
									}
								}
						 }
					 }

			}

			Collections.sort(liveChannelList, new channelComparator(tagsArray));
			tx.commit();
		} catch (HibernateException ex) {
			if (tx != null) {
				tx.rollback();
			}
			ex.printStackTrace();
		} finally {
			dbsession.close();
		}
		return liveChannelList;
	}

	class channelComparator implements Comparator<LiveChannel> {
		String[] tagsArray;

		public channelComparator(String[] tagsArray) {
			this.tagsArray = tagsArray;
		}

		@Override
		public int compare(LiveChannel ch1, LiveChannel ch2) {
			int matchedTagCh1 = 0;
			int matchedTagCh2 = 0;
			List<String> ch1Tags = new ArrayList<String>();
			List<String> ch2Tags = new ArrayList<String>();

			for (Tag tag : ch1.getTagSet()) {
				ch1Tags.add(tag.getTagName());
			}

			for (Tag tag : ch2.getTagSet()) {
				ch2Tags.add(tag.getTagName());
			}

			for (String tagCh1 : tagsArray) {
				if (ch1Tags.contains(tagCh1)) {
					matchedTagCh1++;
				}
			}
			for (String tagCh2 : tagsArray) {
				if (ch2Tags.contains(tagCh2)) {
					matchedTagCh2++;
				}
			}
			if (matchedTagCh1 < matchedTagCh2) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	

}
