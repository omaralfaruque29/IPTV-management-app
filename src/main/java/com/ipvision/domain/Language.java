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
@Table(name = "Language")
public class Language {

	@Id
	@GeneratedValue
	@Column(name = "languageId")
	private int languageId;
	@Column(name = "languageName")
	private String languageName;
	@OneToMany(mappedBy = "language", fetch = FetchType.LAZY)
	Set<LiveChannel> channels;
	@OneToMany(mappedBy = "language", fetch = FetchType.LAZY)
	Set<VOD> vods;
	public int getLanguageId() {
		return languageId;
	}
	public void setLanguageId(int languageId) {
		this.languageId = languageId;
	}
	public String getLanguageName() {
		return languageName;
	}
	public void setLanguageName(String languageName) {
		this.languageName = languageName;
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
