package com.ipvision.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ipvision.domain.ChannelLink;

@Repository
public class ChannelLinkDaoImplementation implements ChannelLinkDao {

	@Autowired
	SessionFactory sessionFactory;

	@Override
	public ChannelLink returnChannelLinkByChannelId(int channelId) {
		
		ChannelLink cat = null;
		
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			cat = (ChannelLink) dbsession
					.createCriteria(ChannelLink.class)
					.add(Restrictions.eq("channelId", channelId))
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
		
		return cat;
	}
	
	@Override
	public void saveChannelLink(ChannelLink channelLink,int channelId,
			String liveStreamerLink720,String liveStreamerLink480,
			String liveStreamerLink360,String liveStreamerLink180,String sboxLink720,
			String sboxLink480,String sboxLink360,String sboxLink180) throws Exception {
		
		Session channelLinkDbSsession = sessionFactory.openSession();
		Transaction channelLinkTx = null;
		Integer channelLinkId=0;
		channelLink.setChannelId(channelId);
		channelLink.setLiveStreamerLink720(liveStreamerLink720);
		channelLink.setLiveStreamerLink480(liveStreamerLink480);
		channelLink.setLiveStreamerLink360(liveStreamerLink360);
		channelLink.setLiveStreamerLink180(liveStreamerLink180);
		channelLink.setSboxLink720(sboxLink720);
		channelLink.setSboxLink480(sboxLink480);
		channelLink.setSboxLink360(sboxLink360);
		channelLink.setSboxLink360(sboxLink180);
		try {
			channelLinkTx = channelLinkDbSsession.beginTransaction();
			channelLinkId = (Integer) channelLinkDbSsession.save(channelLink);
			channelLinkTx.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			if (channelLinkTx != null) {
				channelLinkTx.rollback();
			}
			throw e;
			
		} finally {
			channelLinkDbSsession.close();
		}
		
	}

	@Override
	public void updateChannelLink(int channelId,
			String liveStreamerLink720,String liveStreamerLink480,
			String liveStreamerLink360,String liveStreamerLink180,String sboxLink720,
			String sboxLink480,String sboxLink360,String sboxLink180)
			throws Exception {
		
		Session channelLinkDbSession = sessionFactory.openSession();
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
		
	}
	
	@Override
	public void deleteChannelLink(ChannelLink channelLink) throws Exception {
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			dbsession.delete(channelLink);
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
