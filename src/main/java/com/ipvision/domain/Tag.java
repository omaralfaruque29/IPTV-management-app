package com.ipvision.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "Tag")
public class Tag {

	@Id
	@GeneratedValue
	@Column(name = "tagId")
	private int tagId;
	@Column(name = "tagName", unique = true)
	private String tagName;
	@Transient
	private String contentType;

	@ManyToMany(mappedBy = "tagSet", fetch = FetchType.LAZY)
	//@Fetch(value = FetchMode.SUBSELECT)
	private Set<LiveChannel> liveChannelSet;
	
	@ManyToMany(mappedBy = "vodTagSet", fetch = FetchType.LAZY)
	//@Fetch(value = FetchMode.SUBSELECT)
	private Set<VOD> vodSet;

	public int getTagId() {
		return tagId;
	}

	public void setTagId(int tagId) {
		this.tagId = tagId;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Set<LiveChannel> getLiveChannelSet() {
		return liveChannelSet;
	}

	public void setLiveChannelSet(Set<LiveChannel> liveChannelSet) {
		this.liveChannelSet = liveChannelSet;
	}

	public Set<VOD> getVodSet() {
		return vodSet;
	}

	public void setVodSet(Set<VOD> vodSet) {
		this.vodSet = vodSet;
	}

	
	
}