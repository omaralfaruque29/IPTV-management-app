package com.ipvision.dao;

import java.util.List;

import com.ipvision.domain.Country;

public interface CountryDao {

	public Country returnCountryById(String countryId);
	
	public List<Country> returnAllCountry(int startCountry,int selectedCountryPerPage);
	
	public List<Country> returnAllCountry();
	
	public int returnNumberOfCountry();
	
	public void saveCountry(Country country) throws Exception;
	
	public void updateCountry(Country country,String countryId) throws Exception;
	
	public void deleteCountry(Country country) throws Exception;
}
