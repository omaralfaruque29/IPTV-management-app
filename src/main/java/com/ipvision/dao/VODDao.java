package com.ipvision.dao;

import java.util.List;

import com.ipvision.domain.VOD;

public interface VODDao {

    public VOD returnVODById(String vodId);
	
	public List<VOD> returnAllVOD(String pageNumber,int vodPerPage);
	
	public int returnNumberOfVOD();
	
	public void saveVOD(VOD vod) throws Exception;
	
	public void deleteVOD(VOD vod) throws Exception;
	
	public void updateVOD(VOD vod,String vodId) throws Exception;

	public List<VOD> getLatestFiveVods();

	public List<VOD> getSearchedVodList(String[] tagsArray);
	
}
