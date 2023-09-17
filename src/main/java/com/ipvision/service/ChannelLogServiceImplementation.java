package com.ipvision.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipvision.dao.ChannelLogDao;
import com.ipvision.domain.ChannelLog;

@Service
public class ChannelLogServiceImplementation implements ChannelLogService {

	@Autowired
	ChannelLogDao channelLogDao;

	@Override
	public void saveChannelLog(ChannelLog channelLog) throws Exception {
		
		channelLogDao.saveChannelLog(channelLog);
		
	}

	@Override
	public List<ChannelLog> returnChannelLogByChannelId(int channelId) {
		
		return channelLogDao.returnChannelLogByChannelId(channelId);
	}
	
}
