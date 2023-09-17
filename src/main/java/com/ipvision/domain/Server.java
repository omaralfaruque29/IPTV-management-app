package com.ipvision.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Server")
public class Server {

	@Id
	@GeneratedValue
	@Column(name = "serverId")
	private int serverId;
	@Column(name = "serverName" , unique = true)
	private String serverName;
	@Column(name = "privateIp" , unique = true)
	private String privateIp;
	@Column(name = "publicIp")
	private String publicIp;
	@Column(name = "serverType")
	private String serverType;
	@Column(name = "cpuUsage")
	private double cpuUsage;
	@Column(name = "ramUsage")
	private double ramUsage;
	@Column(name = "bandwidth")
	private double bandwidth;
	@Column(name = "edge")
	private String edge;
	@Column(name = "totalNumberOfStream")
	private int totalNumberOfStream;

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getPrivateIp() {
		return privateIp;
	}

	public void setPrivateIp(String privateIp) {
		this.privateIp = privateIp;
	}

	public String getPublicIp() {
		return publicIp;
	}

	public void setPublicIp(String publicIp) {
		this.publicIp = publicIp;
	}

	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	public double getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(double cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	public double getRamUsage() {
		return ramUsage;
	}

	public void setRamUsage(double ramUsage) {
		this.ramUsage = ramUsage;
	}

	public double getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(double bandwidth) {
		this.bandwidth = bandwidth;
	}

	public String getEdge() {
		return edge;
	}

	public void setEdge(String edge) {
		this.edge = edge;
	}

	public int getTotalNumberOfStream() {
		return totalNumberOfStream;
	}

	public void setTotalNumberOfStream(int totalNumberOfStream) {
		this.totalNumberOfStream = totalNumberOfStream;
	}

}
