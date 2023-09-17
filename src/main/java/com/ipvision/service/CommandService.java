package com.ipvision.service;

public interface CommandService {

	public void execute(String inputCmd);
	
	public String [] getCpuAndMemory(String inputCmd);
}
