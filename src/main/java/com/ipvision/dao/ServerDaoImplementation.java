package com.ipvision.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ipvision.domain.Server;

@Repository
public class ServerDaoImplementation implements ServerDao{

	@Autowired
	SessionFactory sessionFactory;

	@Override
	public Server returnServerById(String serverId) {
        Server server = null;
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			server = (Server) dbsession
					.createCriteria(Server.class)
					.add(Restrictions.eq("serverId", Integer.parseInt(serverId)))
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
		
		return server;
	}
	
	@Override
	public Server returnServerByIp(String serverIp) {
        Server server = null;
		
		Session dbsession = sessionFactory.openSession();
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
		
		return server;
	}
	
	@Override
	public List<Server> returnAllServer(String pageNumber, int serverPerPage) {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		List<Server> servers = new ArrayList<Server>();
		int pageNo = Integer.parseInt(pageNumber);
		try {
			tx = dbsession.beginTransaction();
			Query query = (Query) dbsession.createQuery("FROM Server");
			query.setFirstResult((pageNo - 1) * 10);
			query.setMaxResults(serverPerPage);
			servers = query.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}
		
		return servers;
	}
	
	@Override
	public List<Server> returnAllTranscoderServerOrderByIdleCpu() {
		
		Session dbsession = sessionFactory.openSession();
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
		
		return servers;
	}
	
	@Override
	public List<Server> returnAllStreamerServerOrderByTotalNumberOfStream() {
		
		Session dbsession = sessionFactory.openSession();
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
		
		return servers;
	}
	
	@Override
	public int returnNumberOfServer() {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		Integer totalServer = 0;
		try {
			tx = dbsession.beginTransaction();			
			Long count = ((Long) (dbsession).createQuery(
					"select count(*) from Server").uniqueResult());
			totalServer = count.intValue();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}
		
		return totalServer;
	}
	
	@Override
	public int returnNumberOfTotalStream(int serverId) {
		
        Server server = null;
		int totalNumberOfStream = 0;
		
		Session dbsession = sessionFactory.openSession();
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
		
		return totalNumberOfStream;
	}

	@Override
	public void saveServer(Server server) throws Exception {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		Integer id = 0;
		try {
			tx = dbsession.beginTransaction();
			id = (Integer) dbsession.save(server);
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
	public void updateServer(Server server, String serverId) throws Exception {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			Server editServer = (Server) dbsession.get(
					Server.class, Integer.parseInt(serverId));
			editServer.setServerName(server.getServerName());
			editServer.setPrivateIp(server.getPrivateIp());
			editServer.setPublicIp(server.getPublicIp());
			editServer.setServerType(server.getServerType());
			editServer.setCpuUsage(server.getCpuUsage());
			editServer.setRamUsage(server.getRamUsage());
			editServer.setBandwidth(server.getBandwidth());
			editServer.setEdge(server.getEdge());
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
		
	}

	@Override
	public void updateServerMemoryCpu(int serverId,
			double cpuIdle, double memoryIdle)
			throws Exception {
		
		Session dbsession = sessionFactory.openSession();
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
		
	}

	@Override
	public void updateServerTotalStream(int serverId, int totalStream)
			throws Exception {
		Session dbsession = sessionFactory.openSession();
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
		
	}
	
	@Override
	public void deleteServer(Server server) throws Exception {
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			dbsession.delete(server);
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
