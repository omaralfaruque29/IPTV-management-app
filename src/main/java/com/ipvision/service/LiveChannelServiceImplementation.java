package com.ipvision.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipvision.dao.LiveChannelDao;
import com.ipvision.domain.LiveChannel;
import com.ipvision.domain.User;

@Service
public class LiveChannelServiceImplementation implements LiveChannelService {

	@Autowired
	LiveChannelDao liveChannelDao;

	@Override
	public LiveChannel returnLiveChannelById(String liveChannelId) {
		
		return liveChannelDao.returnLiveChannelById(liveChannelId);
	}

	@Override
	public List<LiveChannel> returnAllLiveChannel(int startChannel,int selectedLiveChannelPerPage) {
		
		return liveChannelDao.returnAllLiveChannel(startChannel , selectedLiveChannelPerPage);
	}
	
	@Override
	public List<LiveChannel> returnAllLiveChannelByUserId(int startChannel,
			int liveChannelPerPage, int userId) {
		
		return liveChannelDao.returnAllLiveChannelByUserId(startChannel, liveChannelPerPage, userId);
	}
	
	@Override
	public List<LiveChannel> returnUpChannelsByUserId(int userId) {
		
		return liveChannelDao.returnUpChannelsByUserId(userId);
	}

	@Override
	public List<LiveChannel> returnDownChannelsByUserId(int userId) {
		
		return liveChannelDao.returnDownChannelsByUserId(userId);
	}
	
	@Override
	public List<LiveChannel> getLatestFiveChannels() {
		
		return liveChannelDao.getLatestFiveChannels();
	}

	@Override
	public int returnNumberOfLiveChannel() {
		
		return liveChannelDao.returnNumberOfLiveChannel();
	}
	
	@Override
	public int returnNumberOfLiveChannelByUserId(int userId) {
		
		return liveChannelDao.returnNumberOfLiveChannelByUserId(userId);
	}

	@Override
	public void saveLiveChannel(LiveChannel liveChannel) throws Exception {
		
		liveChannelDao.saveLiveChannel(liveChannel);
		
	}
	
	@Override
	public int saveLiveChannel(LiveChannel liveChannel, String logo,
			User user, String link720, String link480,String link360,
			String link180, String linkReturnToUser, String state)
			throws Exception {
		
		return liveChannelDao.saveLiveChannel(liveChannel, logo, user, link720, link480, link360, link180, linkReturnToUser, state);
		
	}

	@Override
	public void updateLiveChannel(LiveChannel liveChannel)
			throws Exception {
		
		liveChannelDao.updateLiveChannel(liveChannel );
		
	}
	
	@Override
	public void deleteChannel(LiveChannel channel) throws Exception {
		liveChannelDao.deleteChannel(channel);
		
	}

	/**
	 * 
	 * for webservice
	 */
	
	@Override
	public List<LiveChannel> returnChannelsByCountryId(int countryId) {
		
		return liveChannelDao.returnChannelsByCountryId(countryId);
	}
	
	@Override
	public List<LiveChannel> getSearchedChannelList(String[] tagsArray) {

		return liveChannelDao.getSearchedChannelList(tagsArray);
	}

	@Override
	public void createPlayList(String fileName, String link720, String link480,
			String link360, String link180) {
		 
		 String toWrite = "#EXTM3U\n"
		 				+ "#EXT-X-VERSION:3\n";
		 
		 String res="";
		 if(!link720.equals("")){
			 res = "#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=2000000,RESOLUTION=1280x720\n";
			 toWrite = toWrite+res+link720+"\n";
		 }
		 
		 if(!link480.equals("")){
			 res = "#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=800000,RESOLUTION=854x480\n";
			 toWrite = toWrite+res+link480+"\n";
		 }
		 
		 if(!link360.equals("")){
			 res = "#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=400000,RESOLUTION=640x360\n";
			 toWrite = toWrite+res+link360+"\n";
		 }
		 
		 if(!link180.equals("")){
			 res = "#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=200000,RESOLUTION=320x180\n";
			 toWrite = toWrite+res+link180+"\n";
		 }
		 
		 
		 try {
			 
			 String rootPath = System.getProperty("catalina.base")
						+ "/webapps/images";
			 String path = rootPath + "/"+ fileName + ".m3u8";
			
			File file = new File(path);
				
			FileWriter fileWriter = new FileWriter(file);
				
				
			/*PrintWriter out = new PrintWriter(path);
			out.println(toWrite);*/
			fileWriter.write(toWrite);
			fileWriter.flush();
			fileWriter.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	


}
