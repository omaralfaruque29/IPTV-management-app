package com.ipvision.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ipvision.domain.ChannelLog;

@Repository
public class ChannelLogDaoImplementation implements ChannelLogDao {

	@Autowired
	SessionFactory sessionFactory;
	
	@Override
	public void saveChannelLog(ChannelLog channelLog)
			throws Exception {
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		Integer id=0;
		try {
			tx = dbsession.beginTransaction();
			id = (Integer) dbsession.save(channelLog);
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
	public List<ChannelLog> returnChannelLogByChannelId(int channelId) {
		
        List channelLogs = new ArrayList<ChannelLog>();
		
		Session ctDbSession = sessionFactory.openSession();
		Transaction cchannelLogtx = null;
		try {
			cchannelLogtx = ctDbSession.beginTransaction();
			channelLogs = ctDbSession.createQuery("FROM ChannelLog C WHERE C.channelId = "+channelId+" ORDER BY logId DESC").list();
			
			cchannelLogtx.commit();
		} catch (HibernateException e) {
			if (cchannelLogtx != null)
				cchannelLogtx.rollback();
			e.printStackTrace();
		} finally {
			ctDbSession.close();
		}
		
		return channelLogs;
	}

}
