package com.ipvision.service;

import java.util.List;

import com.ipvision.domain.Server;

public interface ServerService {

	public Server returnServerById(String serverId);
	
	public Server returnServerByIp(String serverIp);
	
    public List<Server> returnAllServer(String pageNumber,int serverPerPage);
    
    public List<Server> returnAllTranscoderServerOrderByIdleCpu();
	
	public List<Server> returnAllStreamerServerOrderByTotalNumberOfStream();
	
	public int returnNumberOfServer();
	
	public int returnNumberOfTotalStream(int serverId);

	public void saveServer(Server server) throws Exception;
	
	public void updateServer(Server server,String serverId) throws Exception;
	
	public void updateServerMemoryCpu(int serverId,double cpuIdle,double memoryIdle) throws Exception;
	
	public void updateServerTotalStream(int serverId,int totalStream) throws Exception;
	
	public void deleteServer(Server server) throws Exception;
}
