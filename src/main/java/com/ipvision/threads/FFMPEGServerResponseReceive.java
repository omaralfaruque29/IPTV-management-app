package com.ipvision.threads;

import java.io.IOException;

public class FFMPEGServerResponseReceive implements Runnable {

	ServerHandler serverHandler = new ServerHandler();
	
	@Override
	public void run() {
		while(true){
			try {
				serverHandler.acceptResult(6000);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
