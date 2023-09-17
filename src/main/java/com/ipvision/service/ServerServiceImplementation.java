package com.ipvision.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipvision.dao.ServerDao;
import com.ipvision.domain.Server;

@Service
public class ServerServiceImplementation implements ServerService{

	@Autowired
	ServerDao serverDao;

	@Override
	public Server returnServerById(String serverId) {
		
		return serverDao.returnServerById(serverId);
	}
	
	@Override
	public Server returnServerByIp(String serverIp) {
		
		return serverDao.returnServerByIp(serverIp);
	}
	
	@Override
	public List<Server> returnAllServer(String pageNumber, int serverPerPage) {
		
		return serverDao.returnAllServer(pageNumber, serverPerPage);
	}
	
	@Override
	public List<Server> returnAllTranscoderServerOrderByIdleCpu() {
		
		return serverDao.returnAllTranscoderServerOrderByIdleCpu();
	}

	@Override
	public List<Server> returnAllStreamerServerOrderByTotalNumberOfStream() {
		
		return serverDao.returnAllStreamerServerOrderByTotalNumberOfStream();
	}

	@Override
	public int returnNumberOfServer() {
		
		return serverDao.returnNumberOfServer();
	}
	
	@Override
	public int returnNumberOfTotalStream(int serverId) {
		
		return serverDao.returnNumberOfTotalStream(serverId);
	}

	@Override
	public void saveServer(Server server) throws Exception {
		
		serverDao.saveServer(server);
		
	}

	@Override
	public void updateServer(Server server, String serverId) throws Exception {
		
		serverDao.updateServer(server, serverId);
		
	}

	@Override
	public void updateServerMemoryCpu(int serverId,
			double cpuIdle, double memoryIdle)
			throws Exception {
		
		serverDao.updateServerMemoryCpu(serverId, cpuIdle, memoryIdle);
		
	}

	@Override
	public void updateServerTotalStream(int serverId, int totalStream)
			throws Exception {
		
		serverDao.updateServerTotalStream(serverId, totalStream);
		
	}
	
	@Override
	public void deleteServer(Server server) throws Exception {
		serverDao.deleteServer(server);
		
	}

	
}
