package com.ipvision.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerHandler {

	public static String pid = "";
	public static String idleCpu = "";
	public static String idleMemory = "";
	
	public void acceptResult(int port) throws IOException {
		
		ServerSocket serverSocket = null;
		Socket socket2 = null;
		int indicator = 0;
		try{
			serverSocket = new ServerSocket(port);
			socket2 = serverSocket.accept();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					socket2.getInputStream()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = reader.readLine()) != null) {
				result.append(line + "\n");
			}
			System.out.println(" " + result.toString());

			String[] splitResult = result.toString().split(",");
			pid = splitResult[0];
			idleCpu = splitResult[1];
			idleMemory = splitResult[2];
		}catch(Exception e){
			e.printStackTrace();
			try{
				serverSocket.close();
				socket2.close();
			}catch(Exception e1){
				e1.printStackTrace();
			}
			indicator = 1;
		}
		
		if(indicator == 0){
			try{
				serverSocket.close();
				socket2.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		
	}

}
