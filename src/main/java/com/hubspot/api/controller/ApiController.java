package com.hubspot.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hubspot.api.entity.Invitation;
import com.hubspot.api.entity.Partner;
import com.hubspot.api.service.ApiService;

@RestController
public class ApiController {
	
	@Autowired
	public ApiService service;

	private List<Partner> partnerList;
	
	private List<Invitation> inviteesList;

	// run the server and call "localhost:8080/hubspot-api/partners"
	
	// calling the rest api GET request for partners list
	// then processing that data through the algorithm to retrieve
	// the best date for the country to host the conference
	// and the make a post request based on that URL
	@RequestMapping(method=RequestMethod.GET, value="/partners")
	public String sendInvitation() {
		
		partnerList = service.retrievePartnersAvailability();
		
		if(partnerList == null) {
			System.out.println("There was error processing the partner list!!");
		}

		inviteesList = service.getInvites(partnerList);
		
		if(partnerList == null) {
			System.out.println("There was error processing the invitation list!!");
		}

		String invitationList = service.stringToJSON(inviteesList);
		
		return service.rrsHttpPost(invitationList);
	}
}
