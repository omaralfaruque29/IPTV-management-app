package com.ipvision.domain;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Country")
public class Country {

	@Id
	@GeneratedValue
	@Column(name = "countryId")
	private int countryId;
	@Column(name = "countryName")
	private String countryName;
	@OneToMany(mappedBy = "country", fetch = FetchType.LAZY)
	@JsonIgnore
	Set<LiveChannel> channels;

	@OneToMany(mappedBy = "country", fetch = FetchType.LAZY)
	@JsonIgnore
	Set<VOD> vods;

	public int getCountryId() {
		return countryId;
	}

	public void setCountryId(int countryId) {
		this.countryId = countryId;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
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
