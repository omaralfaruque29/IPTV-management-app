package com.ipvision.monitorModule;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class FFMPEGCommands {

	public boolean checkIfSourceOk(String command){
		boolean isOk = false;
		String s;
        StringBuilder str = new StringBuilder();
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getErrorStream()));
            while ((s = br.readLine()) != null){
                str.append(s);
            }
            p.waitFor();
            //System.out.println (str);
            if(str.toString().contains("h264")){
            	System.out.println("It Works");
            	isOk = true;
            }else{
            	isOk = false;
            }
            //System.out.println ("exit: " + p.exitValue());
            p.destroy();
        } catch (Exception e) {
        	isOk = false;
        	e.printStackTrace();
        }
        
        return isOk;
	}
	
	public static String getSourceHeight(String command){
		String[] cmd = {
				"/bin/sh",
				"-c",
				command
				};
		String s = "";
        StringBuilder str = new StringBuilder();
        Process p;
        try {
            p = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream()));
            while ((s = br.readLine()) != null){
                str.append(s);
            }
            p.waitFor();
            p.destroy();
	}catch(Exception e){
		e.printStackTrace();
	}
        
        String height = str.substring(str.lastIndexOf("height")+7,str.length());
        
        return height;
	}
}
