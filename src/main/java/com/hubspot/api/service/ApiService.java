package com.hubspot.api.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hubspot.api.entity.Invitation;
import com.hubspot.api.entity.Partner;

public interface ApiService {
	
	String getURL = "https://candidate.hubteam.com/candidateTest/v1/partners?userKey=bede60f886f7068a632c7911919d";

	String postURL = "https://candidate.hubteam.com/candidateTest/v1/results?userKey=bede60f886f7068a632c7911919d";

	
	// function to get the list of partners from the api
	public List<Partner> retrievePartnersAvailability();
	
	// function to get the dates of the partner on which they can attend the conference
	public List<Date> feasibleDatesForPartner(Partner partner);
	
	// function to get the dates on which the conference can be kept on countries
	public Map<String, List<Partner>> buildCountryDates(List<Partner> partners);
	
	// function to get the final date, partner and country list
	public Map<String, Map<Date, List<Partner>>> getInvitationList(List<Partner> partnerList);
	
	// function to create the invitation list
	public List<Invitation> getInvites(List<Partner> partners);

	default String readUrl() throws Exception {
		BufferedReader reader = null;
		try {
			URL url = new URL(getURL);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer buffer = new StringBuffer();
			int read;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1)
				buffer.append(chars, 0, read); 

			return buffer.toString();
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	default String rrsHttpPost(String invitationList) {

		HttpPost post;
		HttpClient client;
		StringEntity entity;

		try {

			// create HttpPost and HttpClient object
			post = new HttpPost(postURL);
			client = HttpClientBuilder.create().build();

			// setup output message by copying JSON body into 
			// apache StringEntity object along with content type
			entity = new StringEntity(invitationList, HTTP.UTF_8);
			entity.setContentEncoding(HTTP.UTF_8);
			entity.setContentType("text/json");

			// add HTTP headers
			post.setHeader("Accept", "application/json");
			post.setHeader("Accept-Charset", "UTF-8");

			// set Authorization header based on the API key
			//post.setHeader("Authorization", ("Bearer "+"bede60f886f7068a632c7911919d"));
			post.setHeader("Authorization", ("userKey=bede60f886f7068a632c7911919d"));
			post.setEntity(entity);

			// Call REST API and retrieve response content
			HttpResponse authResponse = client.execute(post);
			System.out.println("Response: " + authResponse.getEntity() + " " + authResponse.getStatusLine() + " " + authResponse.getParams()); 
			System.out.println();

			return EntityUtils.toString(authResponse.getEntity());

		}
		catch (Exception e) {   
			System.out.println("Error occurred while calling the service!!");
			return e.toString();
		}
	}
	
	//utility function to convert the list of object to json in the required format
	default String stringToJSON(List<Invitation> inviteesList) {
		
		ObjectMapper objectMapper = new ObjectMapper();    	
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		String arrayToJson = null;
		try {
			arrayToJson = objectMapper.writeValueAsString(inviteesList);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		System.out.println(arrayToJson);
		
		StringBuilder s = new StringBuilder();
		s.append("{ " + "\"" + "countries" + "\"" + ": " + arrayToJson + "}");
		
		return s.toString();
	}
}
