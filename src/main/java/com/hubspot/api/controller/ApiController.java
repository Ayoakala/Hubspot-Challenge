package com.hubspot.api.controller;

import java.util.List;

import org.eclipse.persistence.sessions.serializers.JSONSerializer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.hubspot.api.entity.Invitation;
import com.hubspot.api.entity.Partner;
import com.hubspot.api.service.ApiService;

@RestController
public class ApiController {
		
	final static String getURL = "https://candidate.hubteam.com/candidateTest/v1/partners?userKey=bede60f886f7068a632c7911919d";

	final static String postURL = "https://candidate.hubteam.com/candidateTest/v1/results?userKey=bede60f886f7068a632c7911919d";

	@Autowired
	public ApiService service;

	private List<Partner> partnerList;
	
	private List<Invitation> inviteesList;

	@RequestMapping(method=RequestMethod.GET, value="/partners")
	public String retrievePartnersAvailability() {
		
		partnerList = service.retrievePartnersAvailability(getURL);
		
		if(partnerList == null) {
			System.out.println("There was error processing the partner list!!");
		}

		inviteesList = service.getInvites(partnerList);
		
		if(partnerList == null) {
			System.out.println("There was error processing the invitation list!!");
		}

		String invitationList = service.stringToJSON(inviteesList);
		
		return service.rrsHttpPost(invitationList, postURL);
	}
}
