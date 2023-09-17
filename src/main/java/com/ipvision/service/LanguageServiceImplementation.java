package com.ipvision.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipvision.dao.LanguageDao;
import com.ipvision.domain.Language;

@Service
public class LanguageServiceImplementation implements LanguageService {

	@Autowired
	LanguageDao languageDao;
	
	@Override
	public Language returnLanguageById(String languageId) {
		return languageDao.returnLanguageById(languageId);
	}

	@Override
	public List<Language> returnAllLanguage(int startLanguage,int selectedLanguagePerPage) {
		return languageDao.returnAllLanguage(startLanguage , selectedLanguagePerPage);
	}
	
	@Override
	public List<Language> returnAllLanguage() {
		
		return languageDao.returnAllLanguage();
	}

	@Override
	public int returnNumberOfLanguage() {
		return languageDao.returnNumberOfLanguage();
	}

	@Override
	public void saveLanguage(Language language) throws Exception {
		languageDao.saveLanguage(language);
	}

	@Override
	public void updateLanguage(Language language, String languageId)
			throws Exception {
		
		languageDao.updateLanguage(language, languageId);
	}
	
	@Override
	public void deleteLanguage(Language language) throws Exception {
		languageDao.deleteLanguage(language);
		
	}

	

}
