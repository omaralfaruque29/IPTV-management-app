package com.ipvision.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipvision.dao.TagDao;
import com.ipvision.domain.LiveChannel;
import com.ipvision.domain.PrimaryTag;
import com.ipvision.domain.Tag;

@Service
public class TagServiceImplementation implements TagService {

	@Autowired
	TagDao tagDao;

	@Override
	public Tag returnTagById(String tagId) {

		return tagDao.returnTagById(tagId);
	}

	@Override
	public List<Tag> returnAllTag(int startTag, int selectedTagPerPage) {

		return tagDao.returnAllTag(startTag, selectedTagPerPage);
	}

	@Override
	public int returnNumberOfTag() {

		return tagDao.returnNumberOfTag();
	}

	@Override
	public void saveTag(Tag tag) throws Exception {

		tagDao.saveTag(tag);

	}

	@Override
	public void savePrimaryTag(PrimaryTag primaryTag) throws Exception {

		tagDao.savePrimaryTag(primaryTag);

	}

	@Override
	public List<PrimaryTag> getAllPrimaryTags() throws Exception {

		return tagDao.getAllPrimaryTags();

	}

	@Override
	public void updatePrimaryTag(String oldPrimaryTag, String newPrimaryTag)
			throws Exception {

		tagDao.updatePrimaryTag(oldPrimaryTag, newPrimaryTag);

	}
	
	@Override
	public void replaceTag(String oldTag, String newTag)
			throws Exception {

		tagDao.replaceTag(oldTag, newTag);

	}

	@Override
	public void updateTag(Tag tag, String tagId) throws Exception {

		tagDao.updateTag(tag, tagId);

	}

	@Override
	public Map<Integer, String> getTagMap() {

		return tagDao.getTagMap();
	}

	@Override
	public List<String> getAllTags() {

		return tagDao.getAllTags();
	}

	@Override
	public List<Tag> getSpecificTags(String[] tagsArray) {

		return tagDao.getSpecificTags(tagsArray);
	}

	@Override
	public List<PrimaryTag> getSpecificPrimaryTags(String[] tagsArray) {

		return tagDao.getSpecificPrimaryTags(tagsArray);
	}

	@Override
	public String getFilteredTagsName(String[] tagsArray) {

		return tagDao.getFilteredTagsName(tagsArray);
	}
	
	@Override
	public void deleteTag(Tag tag) throws Exception {
		tagDao.deleteTag(tag);
	}
	
	@Override
	public void deletePrimaryTag(PrimaryTag pTag) throws Exception {
		tagDao.deletePrimaryTag(pTag);
		
	}

}