package com.ipvision.domain;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "Category")
public class Category {

	@Id
	@GeneratedValue
	@Column(name = "categoryId")
	private int categoryId;
	@Column(name = "categoryName")
	private String categoryName;
	@OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
	Set<LiveChannel> channels;
	@OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
	Set<VOD> vods;
	public int getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public Set<LiveChannel> getChannels() {
		return channels;
	}
	public void setChannels(Set<LiveChannel> channels) {
		this.channels = channels;
	}
	public Set<VOD> getVods() {
		return vods;
	}
	public void setVods(Set<VOD> vods) {
		this.vods = vods;
	}
	


}
