package com.ipvision.dao;

import java.util.List;

import com.ipvision.domain.Language;

public interface LanguageDao {

    public Language returnLanguageById(String languageId);
	
	public List<Language> returnAllLanguage(int startLanguage,int selectedLanguagePerPage);
	
	public List<Language> returnAllLanguage();
	
	public int returnNumberOfLanguage();
	
	public void saveLanguage(Language language) throws Exception;
	
	public void updateLanguage(Language language,String languageId) throws Exception;
	
	public void deleteLanguage(Language language) throws Exception;
}
