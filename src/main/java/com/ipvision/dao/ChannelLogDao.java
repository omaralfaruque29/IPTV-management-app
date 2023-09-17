package com.ipvision.dao;

import java.util.List;

import com.ipvision.domain.ChannelLog;


public interface ChannelLogDao {

	public void saveChannelLog(ChannelLog channelLog) throws Exception;
	
	public List<ChannelLog> returnChannelLogByChannelId(int channelId);
}
