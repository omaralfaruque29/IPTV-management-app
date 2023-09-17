package com.ipvision.monitorModule.threads;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ipvision.dao.ChannelLinkDao;
import com.ipvision.dao.ChannelLinkDaoImplementation;
import com.ipvision.dao.LiveChannelDao;
import com.ipvision.dao.LiveChannelDaoImplementation;
import com.ipvision.dao.ServerDao;
import com.ipvision.dao.ServerDaoImplementation;
import com.ipvision.domain.LiveChannel;
import com.ipvision.domain.Server;
import com.ipvision.monitorModule.DBAccess;
import com.ipvision.monitorModule.FFMPEGCommands;
import com.ipvision.service.CommandService;
import com.ipvision.service.CommandServiceImplementation;
import com.ipvision.service.LiveChannelService;
import com.ipvision.service.LiveChannelServiceImplementation;
import com.ipvision.threads.ServerHandler;

public class MonitoringThread implements Runnable {

	DBAccess dbAccess = new DBAccess();
	FFMPEGCommands ffmpegCommand = new FFMPEGCommands();
	CommandService commandService = new CommandServiceImplementation();
	LiveChannelService liveChannelService2 = new LiveChannelServiceImplementation();
	
	@Override
	public void run() {
		
		while(true){
			System.out.println("Monitor Thread Running....");
			
			
			/**
			 * All Servers Cpu usage update
			 * 
			 */
			List<Server> transcoderServers = new ArrayList<Server>();
			
			transcoderServers = dbAccess.returnAllTranscoderServer();
			
			if (transcoderServers.size() > 0) {
				for (Server server : transcoderServers) {
					String command = "ssh -tt support@" + server.getPrivateIp()
							+ " TERM=vt100 ./getinfo";
					String[] cpuAndMemory = commandService.getCpuAndMemory(command);
					System.out.println(server.getPrivateIp()+" servers cpu memory"+cpuAndMemory);
					double idleCpu = 0;
					double idleMemory = 0;

					try {
						if (cpuAndMemory.length == 2) {
							idleCpu = Double.parseDouble(cpuAndMemory[0]);
							idleMemory = Double.parseDouble(cpuAndMemory[1]);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					try {
						dbAccess.updateServerMemoryCpu(server.getServerId(), idleCpu,idleMemory);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			
			List<LiveChannel> channelsList = new ArrayList<LiveChannel>();
			
			try{
				channelsList = dbAccess.listDownChannels();
			}catch(Exception e){
				e.printStackTrace();
			}
			
			if(channelsList != null){
				if(channelsList.size() > 0){
					for (Iterator iterator = channelsList.iterator(); iterator.hasNext();){
	                    LiveChannel channel = (LiveChannel) iterator.next();
	                    	System.out.println("down channel : "+channel.getChannelName());
	                    	//String command = "/home/shams/bin/ffprobe -i "+channel.getLink();
	                    	String command = "/home/support/bin/ffprobe -timeout 10000k -i "+channel.getLink();
	                    	boolean isSourceOk = ffmpegCommand.checkIfSourceOk(command);
	                    	if(isSourceOk){
	                    		System.out.println(channel.getChannelName()+" Source ok..");
	                    		
	                    		/**
	                    		 * send command for kill previous proccess
	                    		 */
	                    		String name = channel.getPublishName();
	        					
	        					String command1 = "ssh support@"+channel.getTranscoderIp()+" \"ps -ef | grep "+name+"  | grep -v grep | awk '{print $2}' | xargs kill -INT\"";
	        					
							String[] cmd = { "/bin/sh", "-c", command1 };

							Process p;
							try {
								p = Runtime.getRuntime().exec(cmd);
								p.waitFor();

								p.destroy();
								System.out.println("Command : " + command1);
								System.out.println("executed.......");
							} catch (Exception e) {
								e.printStackTrace();
							}
	        					
	        					System.out.println("first sent : "+command1);
	        					
	        					try{
	        						Thread.sleep(10000);
	        					}catch(Exception e){
	        						e.printStackTrace();
	        					}
	        					
	        					/**
	        					 * command for grabbing idle cpu and memory
	        					 */
	        					String command2 = "ssh -tt support@" + channel.getTranscoderIp()
	        							+ " TERM=vt100 ./getinfo";
	        					System.out.println("comman for cpu : " + command2);
	        					String[] cpuAndMemory = commandService.getCpuAndMemory(command2);
	        					
	        					double idleCpu = 0;
	        					double idleMemory = 0;

	        					try {
	        						if (cpuAndMemory.length == 2) {
	        							idleCpu = Double.parseDouble(cpuAndMemory[0]);
	        							idleMemory = Double.parseDouble(cpuAndMemory[1]);
	        						}
	        					} catch (Exception e) {
	        						e.printStackTrace();
	        					}
	        					
	        					if (idleCpu > 0) {
	        						try {
										dbAccess.updateServerMemoryCpu(dbAccess.returnServerByIp(channel.getTranscoderIp()).getServerId(),
												idleCpu, idleMemory);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
	        					}
	        					
	        					int totalStream = dbAccess.returnNumberOfTotalStream(dbAccess.returnServerByIp(channel.getStreamerIp()).getServerId());
	        					if(totalStream - channel.getNumberOfStream()>0){
	        						try {
										dbAccess.updateServerTotalStream(
												dbAccess.returnServerByIp(channel.getStreamerIp()).getServerId(),
												totalStream - channel.getNumberOfStream());
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
	        					}
	        					
	        					/**
	        					 * 
	        					 * Here is the code for getting least cpu usage transcoder and
	        					 * streamer server IP
	        					 */

	        					List<Server> transcoderServerList = new ArrayList<Server>();
	        					List<Server> streamerServerList = new ArrayList<Server>();

	        					transcoderServerList = dbAccess
	        							.returnAllTranscoderServerOrderByIdleCpu();
	        					streamerServerList = dbAccess
	        							.returnAllStreamerServerOrderByTotalNumberOfStream();
	        					Server transcoderServer = null;
	        					Server streamerServer = null;

	        					if (transcoderServerList.size() > 0) {
	        						for (Server server : transcoderServerList) {
	        							if (server.getCpuUsage() > 20) {
	        								transcoderServer = server;
	        								break;
	        							}
	        						}
	        					}
	        					if (streamerServerList.size() > 0) {
	        						 streamerServer = streamerServerList.get(0);
	        					}

	        					if (transcoderServer == null || streamerServer == null) {
	        						System.out.println("Transcoder or streamer is null..");
	        						break;
	        					}

	        					String transcoderIp = "";
	        					String streamerIp = "";
	        					String streamingServersEdgeApplication = "";
	        					totalStream = 0;

	        					if (transcoderServer != null) {
	        						transcoderIp = transcoderServer.getPrivateIp();
	        					}
	        					if (streamerServer != null) {
	        						streamerIp = streamerServer.getPrivateIp();
	        						streamingServersEdgeApplication = streamerServer.getEdge();
	        						totalStream = streamerServer.getTotalNumberOfStream();
	        					}
	        					
	        					int videoHeight = 0;
	        					
							try {
								videoHeight = Integer
										.parseInt(ffmpegCommand
												.getSourceHeight("/home/support/bin/ffprobe -v error -show_streams "
														+ channel.getLink()
														+ " | grep height"));
							} catch (Exception e) {
								e.printStackTrace();
							}
	        					
	        					/**
	        					 * send command for channel re publish
	        					 */
	        					String cmd1 = "";
	        					int numberOfStream = 4;
	        					String publishName = channel.getPublishName();
	        					if (videoHeight >= 500) {
	        						cmd1 = "ssh support@"
	        								+ transcoderIp
	        								+ " /home/support/bin/ffmpeg -i "
	        								+ channel.getLink()
	        								+ " -c:v copy -c:a libfdk_aac -f flv rtmp://"
	        								+ streamerIp
	        								+ ":1935/"
	        								+ streamerServer.getServerName()
	        								+ "/"
	        								+ publishName
	        								+ "_720"
	        								+ " -vf scale=640:480 -minrate 800k -maxrate 800k -bufsize 800k -c:v h264 -ab 64k -ar 44100 -c:a libfdk_aac -f flv rtmp://"
	        								+ streamerIp
	        								+ ":1935/"
	        								+ streamerServer.getServerName()
	        								+ "/"
	        								+ publishName
	        								+ "_480 -vf scale=480:360 -minrate 400k -maxrate 400k -bufsize 400k -c:v h264 -ab 32k -ar 22050 -c:a libfdk_aac -f flv rtmp://"
	        								+ streamerIp + ":1935/"
	        								+ streamerServer.getServerName() + "/" + publishName + "_360 -vf scale=240:180 -minrate 200k -maxrate 200k -bufsize 200k -c:v h264 -ab 16k -ar 11025 -c:a libfdk_aac -f flv rtmp://"
	        								+ streamerIp + ":1935/"
	        								+ streamerServer.getServerName() + "/" + publishName + "_180 &";
	        						numberOfStream = 4;
	        					} else if (videoHeight < 500 && videoHeight >= 420) {
	        						cmd1 = "ssh support@"
	        								+ transcoderIp
	        								+ " /home/support/bin/ffmpeg -i "
	        								+ channel.getLink()
	        								+ " -c:v copy -c:a libfdk_aac -f flv rtmp://"
	        								+ streamerIp
	        								+ ":1935/"
	        								+ streamerServer.getServerName()
	        								+ "/"
	        								+ publishName
	        								+ "_480"
	        								+ " -vf scale=480:360 -minrate 400k -maxrate 400k -bufsize 400k -c:v h264 -ab 32k -ar 22050 -c:a libfdk_aac -f flv rtmp://"
	        								+ streamerIp + ":1935/"
	        								+ streamerServer.getServerName() + "/" + publishName+ "_360 -vf scale=240:180 -minrate 200k -maxrate 200k -bufsize 200k -c:v h264 -ab 16k -ar 11025 -c:a libfdk_aac -f flv rtmp://"
	        								+ streamerIp + ":1935/"
	        								+ streamerServer.getServerName() + "/" + publishName + "_180 &";
	        						numberOfStream = 3;
	        					} else if (videoHeight < 420 && videoHeight >= 270) {
	        						cmd1 = "ssh support@" + transcoderIp
	        								+ " /home/support/bin/ffmpeg -i "
	        								+ channel.getLink()
	        								+ " -c:v copy -c:a libfdk_aac -f flv rtmp://"
	        								+ streamerIp + ":1935/"
	        								+ streamerServer.getServerName() + "/" + publishName + "_360 -vf scale=240:180 -minrate 200k -maxrate 200k -bufsize 200k -c:v h264 -ab 16k -ar 11025 -c:a libfdk_aac -f flv rtmp://"
	        								+ streamerIp + ":1935/"
	        								+ streamerServer.getServerName() + "/" + publishName + "_180 &";
	        						numberOfStream = 2;
	        					} else if (videoHeight < 270 && videoHeight > 0) {
	        						cmd1 = "ssh support@" + transcoderIp
	        								+ " /home/support/bin/ffmpeg -i "
	        								+ channel.getLink()
	        								+ " -c:v copy -c:a libfdk_aac -f flv rtmp://"
	        								+ streamerIp + ":1935/"
	        								+ streamerServer.getServerName() + "/" + publishName + "_180 &";
	        						numberOfStream = 1;
	        					}else {
	        						System.out.println("Video height not ok ...");
	        						continue;
	        					}
	        					System.out.println(cmd1);
	        					
	        					commandService.execute(cmd1);
	        					
	        					System.out.println("command executed..");

	        					try {
	        						LiveChannelDao liveChannelService = new LiveChannelDaoImplementation();
	        						Thread.sleep(10000);
	        					} catch (InterruptedException e1) {
	        						e1.printStackTrace();
	        					}

	        					/**
	        					 * command for grabbing idle cpu and memory
	        					 */

	        					String command3 = "ssh -tt support@" + transcoderIp
	        							+ " TERM=vt100 ./getinfo";
	        					String[] cpuAndMemory2 = commandService
	        							.getCpuAndMemory(command3);
	        					
	        					System.out.println("Streamer :.................." + streamerIp);
	        					System.out.println("transcoder :.................."
	        							+ transcoderIp);
	        					System.out.println("command : ------    " + cmd1);

	        					double idleCpu2 = 0;
	        					double idleMemory2 = 0;

	        					try {
	        						if (cpuAndMemory2.length == 2) {
	        							idleCpu2 = Double.parseDouble(cpuAndMemory2[0]);
	        							idleMemory2 = Double.parseDouble(cpuAndMemory2[1]);
	        						}
	        					} catch (Exception e) {
	        						e.printStackTrace();
	        					}
	        					
	        					LiveChannel editChannel = dbAccess.returnLiveChannelById(""+channel.getChannelId());
	        					
	        					editChannel.setCommand(cmd1);
	        					editChannel.setState("up");
	        					
	        					if (numberOfStream == 4) {

	        						try {
	        							if (idleCpu2 > 0) {
	        								dbAccess.updateServerMemoryCpu(
	        										transcoderServer.getServerId(),
	        										idleCpu2, idleMemory2);
	        							}
	        							dbAccess.updateServerTotalStream(
	        									streamerServer.getServerId(),
	        									totalStream + 4);
	        						} catch (Exception e) {
	        							// TODO Auto-generated catch block
	        							e.printStackTrace();
	        						}
	        						
	        						editChannel.setNumberOfStream(4);
	        						editChannel.setLink720("http://" + streamerIp + ":1935/"
	        								+ streamerServer.getServerName() + "/"
	        								+ publishName
	        								+ "_720/playlist.m3u8");
	        						editChannel.setLink480("http://" + streamerIp + ":1935/"
	        								+ streamerServer.getServerName() + "/"
	        								+ publishName
	        								+ "_480/playlist.m3u8");
	        						editChannel.setLink360("http://" + streamerIp
	        								+ ":1935/" + streamerServer.getServerName()
	        								+ "/" + publishName
	        								+ "_360/playlist.m3u8");
	        						editChannel.setLink180("http://" + streamerIp
	        								+ ":1935/" + streamerServer.getServerName()
	        								+ "/" + publishName
	        								+ "_180/playlist.m3u8");
	        					} else if (numberOfStream == 3) {

	        						try {
	        							if (idleCpu2 > 0) {
	        								dbAccess.updateServerMemoryCpu(
	        										transcoderServer.getServerId(),
	        										idleCpu2, idleMemory2);
	        							}
	        							dbAccess.updateServerTotalStream(
	        									streamerServer.getServerId(),
	        									totalStream + 3);
	        						} catch (Exception e) {
	        							// TODO Auto-generated catch block
	        							e.printStackTrace();
	        						}

	        						editChannel.setNumberOfStream(3);
	        						editChannel.setLink720("");
	        						editChannel.setLink480("http://" + streamerIp + ":1935/"
	        								+ streamerServer.getServerName() + "/"
	        								+ publishName
	        								+ "_480/playlist.m3u8");
	        						editChannel.setLink360("http://" + streamerIp
	        								+ ":1935/" + streamerServer.getServerName()
	        								+ "/" + publishName
	        								+ "_360/playlist.m3u8");
	        						editChannel.setLink180("http://" + streamerIp
	        								+ ":1935/" + streamerServer.getServerName()
	        								+ "/" + publishName
	        								+ "_180/playlist.m3u8");
	        					} else if (numberOfStream == 2) {

	        						try {
	        							if (idleCpu2 > 0) {
	        								dbAccess.updateServerMemoryCpu(
	        										transcoderServer.getServerId(),
	        										idleCpu2, idleMemory2);
	        							}
	        							dbAccess.updateServerTotalStream(
	        									streamerServer.getServerId(),
	        									totalStream + 2);
	        						} catch (Exception e) {
	        							// TODO Auto-generated catch block
	        							e.printStackTrace();
	        						}

	        						editChannel.setNumberOfStream(2);
	        						editChannel.setLink720("");
	        						editChannel.setLink480("");
	        						editChannel.setLink360("http://" + streamerIp
	        								+ ":1935/" + streamerServer.getServerName()
	        								+ "/" + publishName
	        								+ "_360/playlist.m3u8");
	        						editChannel.setLink180("http://" + streamerIp
	        								+ ":1935/" + streamerServer.getServerName()
	        								+ "/" + publishName
	        								+ "_180/playlist.m3u8");
	        					}else if (numberOfStream == 1) {

	        						try {
	        							if (idleCpu2 > 0) {
	        								dbAccess.updateServerMemoryCpu(
	        										transcoderServer.getServerId(),
	        										idleCpu2, idleMemory2);
	        							}
	        							dbAccess.updateServerTotalStream(
	        									streamerServer.getServerId(),
	        									totalStream + 1);
	        						} catch (Exception e) {
	        							// TODO Auto-generated catch block
	        							e.printStackTrace();
	        						}

	        						editChannel.setNumberOfStream(1);
	        						editChannel.setLink720("");
	        						editChannel.setLink480("");
	        						editChannel.setLink360("");
	        						editChannel.setLink180("http://" + streamerIp
	        								+ ":1935/" + streamerServer.getServerName()
	        								+ "/" + publishName
	        								+ "_180/playlist.m3u8");
	        					}
	        					
	        					editChannel.setTranscoderIp(transcoderIp);
        						editChannel.setStreamerIp(streamerIp);
	        					
	        					
	        					try {
									dbAccess.updateLiveChannel(editChannel);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
	        					
	        					if (numberOfStream == 4) {
	        						try {
	        							dbAccess.updateChannelLink(channel.getChannelId(), "http://"
	        											+ streamingServersEdgeApplication
	        											+ ".livestreamer.com:1935/"
	        											+ streamingServersEdgeApplication + "/"
	        											+ publishName
	        											+ "_720/playlist.m3u8", "http://"
	        											+ streamingServersEdgeApplication
	        											+ ".livestreamer.com:1935/"
	        											+ streamingServersEdgeApplication + "/"
	        											+ publishName
	        											+ "_480/playlist.m3u8", "http://"
	        											+ streamingServersEdgeApplication
	        											+ ".livestreamer.com:1935/"
	        											+ streamingServersEdgeApplication + "/"
	        											+ publishName
	        											+ "_360/playlist.m3u8","http://"
	        													+ streamingServersEdgeApplication
	        													+ ".livestreamer.com:1935/"
	        													+ streamingServersEdgeApplication + "/"
	        													+ publishName
	        													+ "_180/playlist.m3u8", "http://"
	        											+ streamingServersEdgeApplication
	        											+ ".livestreamer.com:1935/"
	        											+ streamingServersEdgeApplication + "/"
	        											+ publishName
	        											+ "_720/playlist.m3u8", "http://"
	        											+ streamingServersEdgeApplication
	        											+ ".livestreamer.com:1935/"
	        											+ streamingServersEdgeApplication + "/"
	        											+ publishName
	        											+ "_480/playlist.m3u8", "http://"
	        											+ streamingServersEdgeApplication
	        											+ ".livestreamer.com:1935/"
	        											+ streamingServersEdgeApplication + "/"
	        											+ publishName
	        											+ "_360/playlist.m3u8","http://"
	        													+ streamingServersEdgeApplication
	        													+ ".livestreamer.com:1935/"
	        													+ streamingServersEdgeApplication + "/"
	        													+ publishName
	        													+ "_180/playlist.m3u8");
	        						} catch (Exception e) {
	        							e.printStackTrace();
	        						}
	        						
	        						liveChannelService2.createPlayList(channel.getChannelId()
	        								+ "_" + channel.getChannelName(), "http://"
	        								+ streamingServersEdgeApplication
	        								+ ".livestreamer.com:1935/"
	        								+ streamingServersEdgeApplication + "/"
	        								+ publishName + "_720/chunklist.m3u8",
	        								"http://" + streamingServersEdgeApplication
	        										+ ".livestreamer.com:1935/"
	        										+ streamingServersEdgeApplication + "/"
	        										+ publishName
	        										+ "_480/chunklist.m3u8", "http://"
	        										+ streamingServersEdgeApplication
	        										+ ".livestreamer.com:1935/"
	        										+ streamingServersEdgeApplication + "/"
	        										+ publishName
	        										+ "_360/chunklist.m3u8", "http://"
	        										+ streamingServersEdgeApplication
	        										+ ".livestreamer.com:1935/"
	        										+ streamingServersEdgeApplication + "/"
	        										+ publishName
	        										+ "_180/chunklist.m3u8");
	        						
	        					} else if (numberOfStream == 3) {
	        						try {
	        							dbAccess.updateChannelLink(channel.getChannelId(), "", "http://"
	        											+ streamingServersEdgeApplication
	        											+ ".livestreamer.com:1935/"
	        											+ streamingServersEdgeApplication + "/"
	        											+ publishName
	        											+ "_480/playlist.m3u8", "http://"
	        											+ streamingServersEdgeApplication
	        											+ ".livestreamer.com:1935/"
	        											+ streamingServersEdgeApplication + "/"
	        											+ publishName
	        											+ "_360/playlist.m3u8","http://"
	        													+ streamingServersEdgeApplication
	        													+ ".livestreamer.com:1935/"
	        													+ streamingServersEdgeApplication + "/"
	        													+ publishName
	        													+ "_180/playlist.m3u8", "",
	        									"http://" + streamingServersEdgeApplication
	        											+ ".livestreamer.com:1935/"
	        											+ streamingServersEdgeApplication + "/"
	        											+ publishName
	        											+ "_480/playlist.m3u8", "http://"
	        											+ streamingServersEdgeApplication
	        											+ ".livestreamer.com:1935/"
	        											+ streamingServersEdgeApplication + "/"
	        											+ publishName
	        											+ "_360/playlist.m3u8","http://"
	        													+ streamingServersEdgeApplication
	        													+ ".livestreamer.com:1935/"
	        													+ streamingServersEdgeApplication + "/"
	        													+ publishName
	        													+ "_180/playlist.m3u8");
	        						} catch (Exception e) {
	        							e.printStackTrace();
	        						}
	        						
	        						liveChannelService2.createPlayList(channel.getChannelId()
	        								+ "_" + channel.getChannelName(), "",
	        								"http://" + streamingServersEdgeApplication
	        										+ ".livestreamer.com:1935/"
	        										+ streamingServersEdgeApplication + "/"
	        										+ publishName
	        										+ "_480/chunklist.m3u8", "http://"
	        										+ streamingServersEdgeApplication
	        										+ ".livestreamer.com:1935/"
	        										+ streamingServersEdgeApplication + "/"
	        										+ publishName
	        										+ "_360/chunklist.m3u8", "http://"
	        										+ streamingServersEdgeApplication
	        										+ ".livestreamer.com:1935/"
	        										+ streamingServersEdgeApplication + "/"
	        										+ publishName
	        										+ "_180/chunklist.m3u8");
	        						
	        						
	        					} else if (numberOfStream == 2) {
	        						try {
	        							dbAccess.updateChannelLink(channel.getChannelId(), "", "", "http://"
	        											+ streamingServersEdgeApplication
	        											+ ".livestreamer.com:1935/"
	        											+ streamingServersEdgeApplication + "/"
	        											+ publishName
	        											+ "_360/playlist.m3u8","http://"
	        													+ streamingServersEdgeApplication
	        													+ ".livestreamer.com:1935/"
	        													+ streamingServersEdgeApplication + "/"
	        													+ publishName
	        													+ "_180/playlist.m3u8", "", "",
	        									"http://" + streamingServersEdgeApplication
	        											+ ".livestreamer.com:1935/"
	        											+ streamingServersEdgeApplication + "/"
	        											+ publishName
	        											+ "_360/playlist.m3u8","http://" + streamingServersEdgeApplication
	        											+ ".livestreamer.com:1935/"
	        											+ streamingServersEdgeApplication + "/"
	        											+ publishName
	        											+ "_180/playlist.m3u8");
	        						} catch (Exception e) {
	        							e.printStackTrace();
	        						}
	        						
	        						liveChannelService2.createPlayList(channel.getChannelId()
	        								+ "_" + channel.getChannelName(), "",
	        								"", "http://"
	        										+ streamingServersEdgeApplication
	        										+ ".livestreamer.com:1935/"
	        										+ streamingServersEdgeApplication + "/"
	        										+ publishName
	        										+ "_360/chunklist.m3u8", "http://"
	        										+ streamingServersEdgeApplication
	        										+ ".livestreamer.com:1935/"
	        										+ streamingServersEdgeApplication + "/"
	        										+ publishName
	        										+ "_180/chunklist.m3u8");
	        						
	        					}else if (numberOfStream == 1) {
	        						try {
	        							dbAccess.updateChannelLink(channel.getChannelId(), "", "","", "http://"
	        											+ streamingServersEdgeApplication
	        											+ ".livestreamer.com:1935/"
	        											+ streamingServersEdgeApplication + "/"
	        											+ publishName
	        											+ "_180/playlist.m3u8", "", "","",
	        									"http://" + streamingServersEdgeApplication
	        											+ ".livestreamer.com:1935/"
	        											+ streamingServersEdgeApplication + "/"
	        											+ publishName
	        											+ "_180/playlist.m3u8");
	        						} catch (Exception e) {
	        							e.printStackTrace();
	        						}
	        						
	        						liveChannelService2.createPlayList(channel.getChannelId()
	        								+ "_" + channel.getChannelName(), "",
	        								"", "", "http://"
	        										+ streamingServersEdgeApplication
	        										+ ".livestreamer.com:1935/"
	        										+ streamingServersEdgeApplication + "/"
	        										+ publishName
	        										+ "_180/chunklist.m3u8");
	        						
	        					}
	        					
	        					
	                    	}
	                }
				}
			}
			
			
			/**
			 * 
			 * Delay for some period..
			 */
			try {
				Thread.sleep(40000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}
