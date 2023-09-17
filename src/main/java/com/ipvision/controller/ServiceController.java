package com.ipvision.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ipvision.domain.ChannelLink;
import com.ipvision.domain.Country;
import com.ipvision.domain.LiveChannel;
import com.ipvision.service.ChannelLinkService;
import com.ipvision.service.CountryService;
import com.ipvision.service.LiveChannelService;

@RestController
@RequestMapping("/api")
public class ServiceController {

	@Autowired
	CountryService countryService;
	
	@Autowired
	LiveChannelService liveChannelService;
	
	@Autowired
	ChannelLinkService channelLinkService;
	
	@RequestMapping(value="/allCountry", method=RequestMethod.GET)
	public ResponseEntity<Map> listAllCountries(){
		List<Country> countries = new ArrayList<Country>();
		
		countries = countryService.returnAllCountry();
		
		Map dataMap = new HashMap();
		
		if(countries.isEmpty()){
            return new ResponseEntity<Map>(HttpStatus.NOT_FOUND);//You many decide to return HttpStatus.NOT_FOUND
        }else{
        	dataMap.put("success", true);
        	dataMap.put("datarows", countries);
        	
        	return new ResponseEntity<Map>(dataMap, HttpStatus.OK);
        }    	
	}
	
	@RequestMapping(value="/liveChannel/{countryId}", method=RequestMethod.GET)
	public ResponseEntity<Map> listCountryChannels(@PathVariable String countryId){
		int id = 0;
		try{
			id = Integer.parseInt(countryId);
		}catch(Exception e){
			e.printStackTrace();
		}
		List<LiveChannel> liveChannels = new ArrayList<LiveChannel>();
		
		liveChannels = liveChannelService.returnChannelsByCountryId(id);
		
		Map dataMap = new HashMap();
		
		if(liveChannels.isEmpty()){
            return new ResponseEntity<Map>(HttpStatus.NOT_FOUND);//You many decide to return HttpStatus.NOT_FOUND
        }else{
        	dataMap.put("success", true);
        	dataMap.put("datarows", liveChannels);
        	
        	return new ResponseEntity<Map>(dataMap, HttpStatus.OK);
        }	
	}
	
	@RequestMapping(value="/channelLink/{channelId}", method=RequestMethod.GET)
	public ResponseEntity<Map> listChannelsLink(@PathVariable String channelId){
		int id = 0;
		try{
			id = Integer.parseInt(channelId);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		ChannelLink channelLink = channelLinkService.returnChannelLinkByChannelId(id);
		List channelLinkList = new ArrayList<ChannelLink>();
		channelLinkList.add(channelLink);
		
		Map dataMap = new HashMap();
		
		if(channelLink == null){
            return new ResponseEntity<Map>(HttpStatus.NOT_FOUND);//You many decide to return HttpStatus.NOT_FOUND
        }else{
        	dataMap.put("success", true);
        	dataMap.put("datarows", channelLinkList);
        	
        	return new ResponseEntity<Map>(dataMap, HttpStatus.OK);
        }	
	}
}
