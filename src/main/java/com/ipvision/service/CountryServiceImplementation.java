package com.ipvision.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipvision.dao.CountryDao;
import com.ipvision.domain.Country;

@Service
public class CountryServiceImplementation implements CountryService {

	@Autowired
	CountryDao countryDao;
	
	@Override
	public Country returnCountryById(String countryId) {
		return countryDao.returnCountryById(countryId);
	}

	@Override
	public List<Country> returnAllCountry(int startCountry,int selectedCountryPerPage) {
		return countryDao.returnAllCountry(startCountry, selectedCountryPerPage);
	}
	
	@Override
	public List<Country> returnAllCountry() {
		
		return countryDao.returnAllCountry();
	}

	@Override
	public int returnNumberOfCountry() {
		return countryDao.returnNumberOfCountry();
	}

	@Override
	public void saveCountry(Country country) throws Exception {
		countryDao.saveCountry(country);
	}

	@Override
	public void updateCountry(Country country, String countryId)
			throws Exception {
		countryDao.updateCountry(country, countryId);
		
	}
	
	@Override
	public void deleteCountry(Country country) throws Exception {
		countryDao.deleteCountry(country);
		
	}


}
