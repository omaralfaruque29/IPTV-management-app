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

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "PrimaryTag")
public class PrimaryTag {

	@Id
	@GeneratedValue
	@Column(name = "primaryTagId")
	private int primaryTagId;

	@Column(name = "primaryTagName", unique = true)
	private String primaryTagName;

	@ManyToMany(mappedBy = "primaryTagSet", fetch = FetchType.LAZY)
	//@Fetch(value = FetchMode.SUBSELECT)
	private Set<LiveChannel> liveChannelSet;
	
	@ManyToMany(mappedBy = "vodPrimaryTagSet", fetch = FetchType.LAZY)
	//@Fetch(value = FetchMode.SUBSELECT)
	private Set<VOD> vodSet;

	public int getPrimaryTagId() {
		return primaryTagId;
	}

	public void setPrimaryTagId(int primaryTagId) {
		this.primaryTagId = primaryTagId;
	}

	public String getPrimaryTagName() {
		return primaryTagName;
	}

	public void setPrimaryTagName(String primaryTagName) {
		this.primaryTagName = primaryTagName;
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