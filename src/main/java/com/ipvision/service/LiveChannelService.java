package com.ipvision.service;

import java.util.List;

import com.ipvision.domain.LiveChannel;
import com.ipvision.domain.User;

public interface LiveChannelService {

    public LiveChannel returnLiveChannelById(String liveChannelId);
	
	public List<LiveChannel> returnAllLiveChannel(int startChannel,int selectedLiveChannelPerPage);
	
	public List<LiveChannel> returnAllLiveChannelByUserId(int startChannel,int liveChannelPerPage,int userId);
	
    public List<LiveChannel> returnUpChannelsByUserId(int userId);
	
	public List<LiveChannel> returnDownChannelsByUserId(int userId);
	
	public List<LiveChannel> getLatestFiveChannels();
	
	public int returnNumberOfLiveChannel();
	
	public int returnNumberOfLiveChannelByUserId(int userId);
	
	public void saveLiveChannel(LiveChannel liveChannel) throws Exception;
	
	public int saveLiveChannel(LiveChannel liveChannel, String logo,
			User user, String link720, String link480,String link360,
			String link180, String linkReturnToUser, String state) throws Exception;
	
	public void updateLiveChannel(LiveChannel liveChannel) throws Exception;
	
	public void deleteChannel(LiveChannel channel) throws Exception;
	
	/**
	 * for web service
	 */
	public List<LiveChannel> returnChannelsByCountryId(int countryId);
	
	public List<LiveChannel> getSearchedChannelList(String[] tagsArray);
	
	/**
	 * Generate playlist
	 */
	
	public void createPlayList(String fileName, String link720, String link480, String link360, String link180);
}
