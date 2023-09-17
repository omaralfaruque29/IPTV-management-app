package com.ipvision.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.ipvision.domain.Category;
import com.ipvision.domain.LiveChannel;
import com.ipvision.domain.PrimaryTag;
import com.ipvision.domain.Tag;

@Repository
public class TagDaoImplementation implements TagDao {

	@Autowired
	SessionFactory sessionFactory;

	@Override
	public Tag returnTagById(String tagId) {

		Tag tg = null;

		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			tg = (Tag) dbsession.createCriteria(Tag.class)
					.add(Restrictions.eq("tagId", Integer.parseInt(tagId)))
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

		return tg;
	}

	@Override
	public List<Tag> returnAllTag(int startTag, int selectedTagPerPage) {

		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		List<Tag> tags = new ArrayList<Tag>();
		try {
			tx = dbsession.beginTransaction();
			Query query = (Query) dbsession.createQuery("FROM Tag");
			query.setFirstResult(startTag);
			query.setMaxResults(selectedTagPerPage);
			tags = query.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}

		return tags;
	}

	@Override
	public int returnNumberOfTag() {

		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		Integer totalTag = 0;
		try {
			tx = dbsession.beginTransaction();
			Long count = ((Long) (dbsession).createQuery(
					"select count(*) from Tag").uniqueResult());
			totalTag = count.intValue();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}

		return totalTag;
	}

	@Override
	public void saveTag(Tag tag) throws Exception {

		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		Integer id = 0;
		try {
			tx = dbsession.beginTransaction();
			id = (Integer) dbsession.save(tag);
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
	public List<PrimaryTag> getAllPrimaryTags() {

		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		List<PrimaryTag> primaryTags = new ArrayList<PrimaryTag>();
		try {
			tx = dbsession.beginTransaction();
			primaryTags = dbsession.createQuery("FROM PrimaryTag").list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}
		return primaryTags;
	}

	@Override
	public void updatePrimaryTag(String oldPrimaryTag, String newPrimaryTag) {

		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		List<PrimaryTag> primaryTags;
		try {
			tx = dbsession.beginTransaction();
			Query query = (Query) dbsession
					.createQuery("FROM PrimaryTag AS PT WHERE PT.primaryTagName = :primaryTagName");
			query.setString("primaryTagName", oldPrimaryTag);
			primaryTags = query.list();
			for (PrimaryTag pt : primaryTags) {
				if (pt.getPrimaryTagName().equalsIgnoreCase(oldPrimaryTag)) {
					pt.setPrimaryTagName(newPrimaryTag);
					dbsession.update(pt);
				}
			}

			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}
	}
	
	@Override
	public void replaceTag(String oldTag, String newTag) {

		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			Query query = (Query) dbsession.createQuery("FROM Tag AS T WHERE T.tagName = :tagName");
			query.setString("tagName", oldTag);
			List<Tag> tags = query.list();
			for (Tag tg : tags) {
				if (tg.getTagName().equalsIgnoreCase(oldTag)) {
					tg.setTagName(newTag);
					dbsession.update(tg);
				}
			}

			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			dbsession.close();
		}
	}

	@Override
	public void savePrimaryTag(PrimaryTag primaryTag) throws Exception {

		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		Integer id = 0;
		try {
			tx = dbsession.beginTransaction();
			id = (Integer) dbsession.save(primaryTag);
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
	public void updateTag(Tag tag, String tagId) throws Exception {

		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			Tag editTag = (Tag) dbsession.get(Tag.class,
					Integer.parseInt(tagId));
			editTag.setTagName(tag.getTagName());
			dbsession.update(editTag);
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
	public Map<Integer, String> getTagMap() {
		Map<Integer, String> tagMap = new HashMap<Integer, String>();
		List tags = new ArrayList<Tag>();
		Session tagSession = sessionFactory.openSession();
		Transaction tagtx = null;
		try {
			tagtx = tagSession.beginTransaction();
			//List tags = tagSession.createQuery("SELECT tagId,tagName FROM Tag").list();
			Criteria cr = tagSession.createCriteria(Tag.class)
				    .setProjection(Projections.projectionList() 
				      .add(Projections.property("tagId"), "tagId") 
				      .add(Projections.property("tagName"), "tagName")) 
				    .setResultTransformer(Transformers.aliasToBean(Tag.class));
    	  tags = cr.list();
			for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
				Tag temp = (Tag) iterator.next();
				tagMap.put(temp.getTagId(), temp.getTagName());
			}
			tagtx.commit();

		} catch (HibernateException ex) {
			if (tagtx != null) {
				tagtx.rollback();
			}
			ex.printStackTrace();
		} finally {
			tagSession.close();

		}
		return tagMap;
	}

	@Override
	public List<String> getAllTags() {
		List<String> tagList = new ArrayList<String>();
		Session tagSession = sessionFactory.openSession();
		Transaction tagtx = null;
		try {
			tagtx = tagSession.beginTransaction();
			List tags = tagSession.createQuery("FROM Tag").list();

			for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
				Tag temp = (Tag) iterator.next();
				tagList.add(temp.getTagName());
			}
			tagtx.commit();

		} catch (HibernateException ex) {
			if (tagtx != null) {
				tagtx.rollback();
			}
			ex.printStackTrace();
		} finally {
			tagSession.close();

		}
		return tagList;
	}

	public List<Tag> getSpecificTags(String[] tagsArray) {
		List<Tag> tagList = new ArrayList<Tag>();
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			for (String splitTagName : tagsArray) {
				Query secondaryTagQuery = (Query) dbsession.createQuery(
						"FROM Tag T WHERE T.tagName = :tagName").setString(
						"tagName", splitTagName);
				List<Tag> tags = secondaryTagQuery.list();
				if (tags.size() > 0) {
					tagList.add(tags.get(0));
				}
			}
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw e;

		} finally {
			dbsession.close();
		}
		return tagList;
	}

	public List<PrimaryTag> getSpecificPrimaryTags(String[] tagsArray) {
		List<PrimaryTag> primarytagList = new ArrayList<PrimaryTag>();
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			for (String splitTagName : tagsArray) {
				Query primaryTagQuery = (Query) dbsession
						.createQuery(
								"FROM PrimaryTag PT WHERE PT.primaryTagName = :primaryTagName")
						.setString("primaryTagName", splitTagName);
				List<PrimaryTag> tags = primaryTagQuery.list();
				if (tags.size() > 0) {
					primarytagList.add(tags.get(0));
				}
			}
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw e;

		} finally {
			dbsession.close();
		}
		return primarytagList;
	}

	public String getFilteredTagsName(String[] tagsArray) {
		String filteredTagsName = "";
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			for (String splitTagName : tagsArray) {
				Query primaryTagQuery = (Query) dbsession.createQuery("FROM PrimaryTag PT WHERE PT.primaryTagName = :primaryTagName").setString("primaryTagName", splitTagName);
				List<PrimaryTag> primaryTags = primaryTagQuery.list();
				if (primaryTags.size() > 0) {
					continue;
				} else {
					Query secondaryTagQuery = (Query) dbsession.createQuery("FROM Tag T WHERE T.tagName = :tagName").setString("tagName", splitTagName);
					List<Tag> tags = secondaryTagQuery.list();
					if (tags.size() > 0) {
						continue;
					} else {
						filteredTagsName = filteredTagsName + splitTagName + ",";
					}
				}
			}
			tx.commit();

		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw e;

		} finally {
			dbsession.close();
		}
		return filteredTagsName;
	}
	
	@Override
	public void deleteTag(Tag tag) throws Exception {
		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
			dbsession.delete(tag);
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
	public void deletePrimaryTag(PrimaryTag pTag) throws Exception {

		Session dbsession = sessionFactory.openSession();
		Transaction tx = null;
		try {
			tx = dbsession.beginTransaction();
		    dbsession.delete(pTag);
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