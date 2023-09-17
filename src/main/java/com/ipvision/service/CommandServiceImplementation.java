package com.ipvision.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.stereotype.Service;

@Service
public class CommandServiceImplementation implements CommandService {

	@Override
	public void execute(String inputCmd) {
		String[] cmd = {
				"/bin/sh",
				"-c",
				inputCmd
				};
		
        Process p;
        try {
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            p.destroy();
            System.out.println("Command : "+inputCmd);
            System.out.println("executed.......");
	}catch(Exception e){
		e.printStackTrace();
	}
	}

	@Override
	public String[] getCpuAndMemory(String inputCmd) {
		
		String[] cmd = {
				"/bin/sh",
				"-c",
				inputCmd
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
            System.out.println (str);
            p.destroy();
            System.out.println("..");
	}catch(Exception e){
		e.printStackTrace();
	}
        
        int start = str.toString().indexOf("CPU")+3;
        int end = str.toString().indexOf("Memory");
        String cpu = "";
        
        try{
        	cpu = str.toString().substring(start,end);
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        if(cpu.contains("id")){
        	cpu = ""+100;
        }
        
        start = str.toString().indexOf("Memory")+6;
        end = str.toString().length();
        String memory = "";
        
        try{
        	memory = str.toString().substring(start,end);
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        String [] cpuAndMemory = {cpu,memory};
        
        return cpuAndMemory;
	}

}
