package com.ipvision.service;

import java.util.List;

import com.ipvision.domain.ChannelLink;

public interface ChannelLinkService {

	public ChannelLink returnChannelLinkByChannelId(int channelId);
	
	public void saveChannelLink(ChannelLink channelLink,int channelId,
			String liveStreamerLink720,String liveStreamerLink480,
			String liveStreamerLink360,String liveStreamerLink180,String sboxLink720,
			String sboxLink480,String sboxLink360,String sboxLink180) throws Exception;
	
	public void updateChannelLink(int channelId,
			String liveStreamerLink720,String liveStreamerLink480,
			String liveStreamerLink360,String liveStreamerLink180,String sboxLink720,
			String sboxLink480,String sboxLink360,String sboxLink180) throws Exception;
	
	public void deleteChannelLink(ChannelLink channelLink) throws Exception;
}
