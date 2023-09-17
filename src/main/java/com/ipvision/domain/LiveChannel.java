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
@Table(name = "LiveChannel")
public class LiveChannel {

	@Id
	@GeneratedValue
	@Column(name = "channelId")
	private int channelId;
	@Column(name = "channelName", unique = true)
	private String channelName;
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
	@Column(name = "link")
	private String link;
	@Column(name = "logo")
	private String logo;
	@Transient
	@JsonIgnore
	private MultipartFile file;
	@Column(name = "tag")
	private String tag;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId")
	@JsonIgnore
	private User user;
	@Column(name = "pid")
	@JsonIgnore
	private String pid;
	@Column(name = "link720")
	@JsonIgnore
	private String link720;
	@Column(name = "link480")
	@JsonIgnore
	private String link480;
	@Column(name = "link360")
	@JsonIgnore
	private String link360;
	@Column(name = "link180")
	@JsonIgnore
	private String link180;
	@Column(name = "linkReturnToUser")
	private String linkReturnToUser;
	@Column(name = "state")
	private String state;
	@Column(name = "streamerIp")
	@JsonIgnore
	private String streamerIp;
	@Column(name = "transcoderIp")
	@JsonIgnore
	private String transcoderIp;
	@Column(name = "numberOfStream")
	private int numberOfStream;
	@Column(name = "command", length = 1000)
	@JsonIgnore
	private String command;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "LiveChannel_Tag", joinColumns = @JoinColumn(name = "channelId"), inverseJoinColumns = @JoinColumn(name = "tagId"))
	//@Fetch(value = FetchMode.SUBSELECT)
	public Set<Tag> tagSet;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "LiveChannelPrimaryTag", joinColumns = @JoinColumn(name = "channelId"), inverseJoinColumns = @JoinColumn(name = "primarytTagId"))
	//@Fetch(value = FetchMode.SUBSELECT)
	public Set<PrimaryTag> primaryTagSet;

	@Column(name = "publishName")
	@JsonIgnore
	private String publishName;

	public int getChannelId() {
		return channelId;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
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

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getLink720() {
		return link720;
	}

	public void setLink720(String link720) {
		this.link720 = link720;
	}

	public String getLink480() {
		return link480;
	}

	public void setLink480(String link480) {
		this.link480 = link480;
	}

	public String getLink360() {
		return link360;
	}

	public void setLink360(String link360) {
		this.link360 = link360;
	}

	public String getLink180() {
		return link180;
	}

	public void setLink180(String link180) {
		this.link180 = link180;
	}

	public String getLinkReturnToUser() {
		return linkReturnToUser;
	}

	public void setLinkReturnToUser(String linkReturnToUser) {
		this.linkReturnToUser = linkReturnToUser;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getStreamerIp() {
		return streamerIp;
	}

	public void setStreamerIp(String streamerIp) {
		this.streamerIp = streamerIp;
	}

	public String getTranscoderIp() {
		return transcoderIp;
	}

	public void setTranscoderIp(String transcoderIp) {
		this.transcoderIp = transcoderIp;
	}

	public int getNumberOfStream() {
		return numberOfStream;
	}

	public void setNumberOfStream(int numberOfStream) {
		this.numberOfStream = numberOfStream;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public Set<Tag> getTagSet() {
		return tagSet;
	}

	public void setTagSet(Set<Tag> tagSet) {
		this.tagSet = tagSet;
	}

	public Set<PrimaryTag> getPrimaryTagSet() {
		return primaryTagSet;
	}

	public void setPrimaryTagSet(Set<PrimaryTag> primaryTagSet) {
		this.primaryTagSet = primaryTagSet;
	}

	public String getPublishName() {
		return publishName;
	}

	public void setPublishName(String publishName) {
		this.publishName = publishName;
	}

	

}
