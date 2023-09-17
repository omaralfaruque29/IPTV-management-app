package com.ipvision.service;

import java.util.List;

import com.ipvision.domain.ChannelLog;

public interface ChannelLogService {

    public void saveChannelLog(ChannelLog channelLog) throws Exception;
	
	public List<ChannelLog> returnChannelLogByChannelId(int channelId);
}
