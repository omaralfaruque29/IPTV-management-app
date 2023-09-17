package com.ipvision.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipvision.dao.VODDao;
import com.ipvision.domain.LiveChannel;
import com.ipvision.domain.VOD;

@Service
public class VODServiceImplementation implements VODService {

	@Autowired
	VODDao vodDao;

	@Override
	public VOD returnVODById(String vodId) {
		
		return vodDao.returnVODById(vodId);
	}

	@Override
	public List<VOD> returnAllVOD(String pageNumber, int vodPerPage) {
		
		return vodDao.returnAllVOD(pageNumber, vodPerPage);
	}

	@Override
	public int returnNumberOfVOD() {
		
		return vodDao.returnNumberOfVOD();
	}

	@Override
	public void saveVOD(VOD vod) throws Exception {
		
		vodDao.saveVOD(vod);
		
	}

	@Override
	public void deleteVOD(VOD vod) throws Exception {
		
		vodDao.deleteVOD(vod);
		
	}
	
	@Override
	public void updateVOD(VOD vod, String vodId) throws Exception {
		
		vodDao.updateVOD(vod, vodId);
		
	}
	
	@Override
	public List<VOD> getLatestFiveVods() {
		
		return vodDao.getLatestFiveVods();
	}
	
	@Override
	public List<VOD> getSearchedVodList(String[] tagsArray) {
		
		return vodDao.getSearchedVodList(tagsArray);
	}
}