package com.ipvision.dao;

import java.util.List;
import java.util.Map;

import com.ipvision.domain.LiveChannel;
import com.ipvision.domain.PrimaryTag;
import com.ipvision.domain.Tag;

public interface TagDao {
	
	public Tag returnTagById(String tagId);

	public List<Tag> returnAllTag(int startTag, int selectedTagPerPage);

	public int returnNumberOfTag();

	public void saveTag(Tag tag) throws Exception;

	public void savePrimaryTag(PrimaryTag primaryTag) throws Exception;

	public List<PrimaryTag> getAllPrimaryTags();

	public void updatePrimaryTag(String oldPrimaryTag, String newPrimaryTag);

	public void updateTag(Tag tag, String tagId) throws Exception;

	public Map<Integer, String> getTagMap();

	public List<String> getAllTags();

	public List<Tag> getSpecificTags(String[] tagsArray);
	
	public List<PrimaryTag> getSpecificPrimaryTags(String[] tagsArray);
	
	public String getFilteredTagsName(String[] tagsArray);
	
	public void deleteTag(Tag tag) throws Exception;
	
	public void deletePrimaryTag(PrimaryTag pTag) throws Exception;

	public void replaceTag(String oldTag, String newTag);

}