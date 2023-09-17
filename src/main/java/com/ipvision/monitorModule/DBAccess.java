package com.ipvision.monitorModule;

import java.util.ArrayList;
import java.util.List; 
import java.util.Date;
import java.util.Iterator; 

import org.hibernate.HibernateException; 
import org.hibernate.Query;
import org.hibernate.Session; 
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;

import com.ipvision.domain.ChannelLink;
import com.ipvision.domain.ChannelLog;
import com.ipvision.domain.LiveChannel;
import com.ipvision.domain.Server;

public class DBAccess {

	
	
	/**
	 * This method return down channels from db
	 * 
	 * @return List of down channel
	 */
	public List<LiveChannel> listDownChannels(){
		SessionFactory factory = null;
		try{
	         factory = new Configuration().configure().buildSessionFactory();
	      }catch (Exception e) { 
	          e.printStackTrace();
	      }
		
		List channels = new ArrayList<LiveChannel>();
		
		if(factory != null){
			Session session = null;
		      Transaction tx = null;
		      try{
		    	 session = factory.openSession(); 
		         tx = session.beginTransaction();
		         channels = session.createQuery("FROM LiveChannel L WHERE L.state='down'").list(); 
		         tx.commit();
		      }catch (HibernateException e) {
		         if (tx!=null) tx.rollback();
		         e.printStackTrace(); 
		      }finally {
		         session.close(); 
		      }
		      
		      factory.close();
		}
	      
	      return channels;
	}
	
	/**
	 * This method update channels pid and state
	 */
	   public void updateChannelsState(Integer channelId,String state){
		   SessionFactory factory = null;   
		try {
			factory = new Configuration().configure().buildSessionFactory();
		} catch (Exception e) {
			e.printStackTrace();
		}
	      if(factory != null){
	    	  Session session = null;
		      Transaction tx = null;
		      try{
		    	 session = factory.openSession(); 
		         tx = session.beginTransaction();
		         LiveChannel editChannel = 
		                    (LiveChannel)session.get(LiveChannel.class, channelId);
		         editChannel.setState(state);
				 session.update(editChannel); 
		         tx.commit();
		      }catch (HibernateException e) {
		         if (tx!=null) tx.rollback();
		         e.printStackTrace(); 
		      }finally {
		         session.close(); 
		      }
		      
		      factory.close();
	      }
	   }
	   
	   /**
	    * Log for channel log table that channel gone up
	    */
	   public void saveChannelLog(int channelId){
		   
		   SessionFactory factory = null;
		   
		   try {
				factory = new Configuration().configure().buildSessionFactory();
			} catch (Exception e) {
				e.printStackTrace();
			}
		   
		   ChannelLog channelLog = new ChannelLog();
		   channelLog.setChannelId(channelId);
		   channelLog.setDate(new Date());
		   channelLog.setLog("Channel state gone up..");
		   
		   if(factory != null){
			   Session session = factory.openSession();
			      Transaction tx = null;
			      Integer channelLogID = null;
			      try{
			         tx = session.beginTransaction();
			         channelLogID = (Integer) session.save(channelLog); 
			         tx.commit();
			      }catch (HibernateException e) {
			         if (tx!=null) tx.rollback();
			         e.printStackTrace(); 
			      }finally {
			         session.close(); 
			      }
			      
			      factory.close();
		   }
	   }
	   
	   /**
	    * For server's db access
	    * 
	    */
	   
	   public void updateServerMemoryCpu(int serverId,
				double cpuIdle, double memoryIdle)
				throws Exception {
		   
           SessionFactory factory = null;
		   
		   try {
				factory = new Configuration().configure().buildSessionFactory();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Session dbsession = factory.openSession();
			Transaction tx = null;
			try {
				tx = dbsession.beginTransaction();
				Server editServer = (Server) dbsession.get(
						Server.class, serverId);
				editServer.setCpuUsage(cpuIdle);
				editServer.setRamUsage(memoryIdle);
				dbsession.update(editServer);
				tx.commit();

			} catch (HibernateException e) {
				if (tx != null)
					tx.rollback();
				e.printStackTrace();
				throw e;

			} finally {
				dbsession.close();
			}
			
			factory.close();
			
		}
	   
	   public Server returnServerByIp(String serverIp) {
	        Server server = null;
	        
	        SessionFactory factory = null;
			   
			   try {
					factory = new Configuration().configure().buildSessionFactory();
				} catch (Exception e) {
					e.printStackTrace();
				}
			
			Session dbsession = factory.openSession();
			Transaction tx = null;
			try {
				tx = dbsession.beginTransaction();
				server = (Server) dbsession
						.createCriteria(Server.class)
						.add(Restrictions.eq("privateIp", serverIp))
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
			
			factory.close();
			
			return server;
		}
	   
	   public int returnNumberOfTotalStream(int serverId) {
			
		   SessionFactory factory = null;
		   
		   try {
				factory = new Configuration().configure().buildSessionFactory();
			} catch (Exception e) {
				e.printStackTrace();
			}
		   
	        Server server = null;
			int totalNumberOfStream = 0;
			
			Session dbsession = factory.openSession();
			Transaction tx = null;
			try {
				tx = dbsession.beginTransaction();
				server = (Server) dbsession
						.createCriteria(Server.class)
						.add(Restrictions.eq("serverId", serverId))
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
			
			if(server != null){
				totalNumberOfStream = server.getTotalNumberOfStream();
			}
			
			factory.close();
			
			return totalNumberOfStream;
		}
	   
	   public void updateServerTotalStream(int serverId, int totalStream)
				throws Exception {
		   
           SessionFactory factory = null;
		   
		   try {
				factory = new Configuration().configure().buildSessionFactory();
			} catch (Exception e) {
				e.printStackTrace();
			}
		   
			Session dbsession = factory.openSession();
			Transaction tx = null;
			try {
				tx = dbsession.beginTransaction();
				Server editServer = (Server) dbsession.get(
						Server.class, serverId);
				editServer.setTotalNumberOfStream(totalStream);
				dbsession.update(editServer);
				tx.commit();

			} catch (HibernateException e) {
				if (tx != null)
					tx.rollback();
				e.printStackTrace();
				throw e;

			} finally {
				dbsession.close();
			}
			
			factory.close();
			
		}
	   
	   public List<Server> returnAllTranscoderServerOrderByIdleCpu() {
			
           SessionFactory factory = null;
		   
		   try {
				factory = new Configuration().configure().buildSessionFactory();
			} catch (Exception e) {
				e.printStackTrace();
			}
		   
			Session dbsession = factory.openSession();
			Transaction tx = null;
			List<Server> servers = new ArrayList<Server>();
			try {
				tx = dbsession.beginTransaction();
				Query query = (Query) dbsession.createQuery("FROM Server WHERE serverType='Transcoder' ORDER BY cpuUsage DESC");
				servers = query.list();
				tx.commit();
			} catch (HibernateException e) {
				if (tx != null)
					tx.rollback();
				e.printStackTrace();
			} finally {
				dbsession.close();
			}
			
			factory.close();
			
			return servers;
		}
	   
	   public List<Server> returnAllTranscoderServer() {
			
           SessionFactory factory = null;
		   
		   try {
				factory = new Configuration().configure().buildSessionFactory();
			} catch (Exception e) {
				e.printStackTrace();
			}
		   
			Session dbsession = factory.openSession();
			Transaction tx = null;
			List<Server> servers = new ArrayList<Server>();
			try {
				tx = dbsession.beginTransaction();
				Query query = (Query) dbsession.createQuery("FROM Server WHERE serverType='Transcoder'");
				servers = query.list();
				tx.commit();
			} catch (HibernateException e) {
				if (tx != null)
					tx.rollback();
				e.printStackTrace();
			} finally {
				dbsession.close();
			}
			
			factory.close();
			
			return servers;
		}
	   
	   public List<Server> returnAllStreamerServerOrderByTotalNumberOfStream() {
			
           SessionFactory factory = null;
		   
		   try {
				factory = new Configuration().configure().buildSessionFactory();
			} catch (Exception e) {
				e.printStackTrace();
			}
		   
			Session dbsession = factory.openSession();
			Transaction tx = null;
			List<Server> servers = new ArrayList<Server>();
			try {
				tx = dbsession.beginTransaction();
				Query query = (Query) dbsession.createQuery("FROM Server WHERE serverType='Streamer' ORDER BY totalNumberOfStream ASC");
				servers = query.list();
				tx.commit();
			} catch (HibernateException e) {
				if (tx != null)
					tx.rollback();
				e.printStackTrace();
			} finally {
				dbsession.close();
			}
			
			factory.close();
			
			return servers;
		}
	   
	   /**
	    * LiveChannel db access
	    * 
	    */
	   
	   public LiveChannel returnLiveChannelById(String liveChannelId) {
			
           SessionFactory factory = null;
		   
		   try {
				factory = new Configuration().configure().buildSessionFactory();
			} catch (Exception e) {
				e.printStackTrace();
			}
		   
			LiveChannel ch = null;
			
			Session dbsession = factory.openSession();			
			Transaction tx = null;
	        try {
	            tx = dbsession.beginTransaction();
	            ch = (LiveChannel) dbsession.createCriteria(LiveChannel.class)
	                    .add( Restrictions.eq("channelId", Integer.parseInt(liveChannelId)) ).uniqueResult();
	            tx.commit();
	        } catch (Exception e) {
	            e.printStackTrace();
	            if (tx != null) {
	                tx.rollback();
	            }            
	        } finally {
	        	dbsession.close();           
	        }
	        
	        factory.close();
			
			return ch;
		}
	   
	   public void updateLiveChannel(LiveChannel liveChannel)
				throws Exception {
		   
           SessionFactory factory = null;
		   
		   try {
				factory = new Configuration().configure().buildSessionFactory();
			} catch (Exception e) {
				e.printStackTrace();
			}
		   
			Session dbsession = factory.openSession();
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
			
			factory.close();
			
		}
	   
	   /**
	    * ChannelLink db access
	    * 
	    */
	   
	   public void updateChannelLink(int channelId,
				String liveStreamerLink720,String liveStreamerLink480,
				String liveStreamerLink360,String liveStreamerLink180,String sboxLink720,
				String sboxLink480,String sboxLink360,String sboxLink180)
				throws Exception {
		   
           SessionFactory factory = null;
		   
		   try {
				factory = new Configuration().configure().buildSessionFactory();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Session channelLinkDbSession = factory.openSession();
			Transaction tx = null;
			try {
				tx = channelLinkDbSession.beginTransaction();
				ChannelLink editChannelLink = (ChannelLink) channelLinkDbSession.get(ChannelLink.class,channelId);
				editChannelLink.setLiveStreamerLink720(liveStreamerLink720);
				editChannelLink.setLiveStreamerLink480(liveStreamerLink480);
				editChannelLink.setLiveStreamerLink360(liveStreamerLink360);
				editChannelLink.setLiveStreamerLink180(liveStreamerLink180);
				editChannelLink.setSboxLink720(sboxLink720);
				editChannelLink.setSboxLink480(sboxLink480);
				editChannelLink.setSboxLink360(sboxLink360);
				editChannelLink.setSboxLink180(sboxLink180);
				channelLinkDbSession.update(editChannelLink);
				tx.commit();

			} catch (HibernateException e) {
				if (tx != null)
					tx.rollback();
				e.printStackTrace();
				throw e;

			} finally {
				channelLinkDbSession.close();
			}
			
			factory.close();
			
		}
}
