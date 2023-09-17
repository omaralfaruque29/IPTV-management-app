package com.ipvision.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "VOD")
public class VOD {

	@Id
	@GeneratedValue
	@Column(name = "videoId")
	private int videoId;
	@Column(name = "videoName")
	private String videoName;
	@Column(name = "about")
	private String about;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "categoryId")
	@JsonIgnore
	private Category category;
	@Transient
	private int categoryId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "countryId")
	@JsonIgnore
	private Country country;
	@Transient
	private int countryId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "languageId")
	@JsonIgnore
	private Language language;
	@Transient
	private int languageId;
	@Column(name = "poster")
	private String poster;
	@Column(name = "link")
	private String link;
	@Transient
	private MultipartFile file;
	@Column(name = "tag")
	private String tag;
	
	@ManyToMany(fetch = FetchType.LAZY)
	//@Fetch(value = FetchMode.SUBSELECT)
	@JoinTable(name = "VodTag", joinColumns = @JoinColumn(name = "videoId"), inverseJoinColumns = @JoinColumn(name = "tagId"))
	private Set<Tag> vodTagSet;

	@ManyToMany(fetch = FetchType.LAZY)
	//@Fetch(value = FetchMode.SUBSELECT)
	@JoinTable(name = "VodPrimaryTag", joinColumns = @JoinColumn(name = "videoId"), inverseJoinColumns = @JoinColumn(name = "primaryTagId"))
	private Set<PrimaryTag> vodPrimaryTagSet;

	public int getVideoId() {
		return videoId;
	}

	public void setVideoId(int videoId) {
		this.videoId = videoId;
	}

	public String getVideoName() {
		return videoName;
	}

	public void setVideoName(String videoName) {
		this.videoName = videoName;
	}

	public String getAbout() {
		return about;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public int getCountryId() {
		return countryId;
	}

	public void setCountryId(int countryId) {
		this.countryId = countryId;
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public int getLanguageId() {
		return languageId;
	}

	public void setLanguageId(int languageId) {
		this.languageId = languageId;
	}

	public String getPoster() {
		return poster;
	}

	public void setPoster(String poster) {
		this.poster = poster;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Set<Tag> getVodTagSet() {
		return vodTagSet;
	}

	public void setVodTagSet(Set<Tag> vodTagSet) {
		this.vodTagSet = vodTagSet;
	}

	public Set<PrimaryTag> getVodPrimaryTagSet() {
		return vodPrimaryTagSet;
	}

	public void setVodPrimaryTagSet(Set<PrimaryTag> vodPrimaryTagSet) {
		this.vodPrimaryTagSet = vodPrimaryTagSet;
	}

}
