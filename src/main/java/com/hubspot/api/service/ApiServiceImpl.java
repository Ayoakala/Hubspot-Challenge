package com.hubspot.api.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hubspot.api.entity.Invitation;
import com.hubspot.api.entity.Partner;

@Service
public class ApiServiceImpl implements ApiService {

	@Override
	public List<Partner> retrievePartnersAvailability() {

		try {

			List<Partner> partnerList = new ArrayList<Partner>();

			String jsonData = readUrl();			
			System.out.println(jsonData);

			JSONParser parser = new JSONParser();	    	
			Object obj = parser.parse(jsonData);	 

			JSONObject json = (JSONObject) obj;

			JSONArray result = (JSONArray) json.get("partners");
			System.out.println("Result : " + json.get("partners"));

			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

			for (int i = 0; i < result.size(); i++) {

				JSONObject jsonPartner = (JSONObject) result.get(i);

				Partner p = gson.fromJson(jsonPartner.toJSONString(), Partner.class);

				partnerList.add(p);

			}

			return partnerList;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Date> feasibleDatesForPartner(Partner partner)
	{

		Collections.sort(partner.getAvailableDates());

		Map<Date, Integer> startDates = new TreeMap<Date, Integer>();

		List<Date> sortedDates = partner.getAvailableDates();

		startDates.put(sortedDates.get(0), 0);

		for (int i = 1; i < sortedDates.size() ; i++)
		{			
			Date prevDate = sortedDates.get(i-1);
			Date curDate = sortedDates.get(i);

			long diff = Math.abs(curDate.getTime() - prevDate.getTime());
			long diffDays = diff / (24 * 60 * 60 * 1000);

			if(diffDays == 1) {					
				int count  = startDates.get(prevDate);
				startDates.put(prevDate, count+1);
				startDates.put(curDate, 1);
			}
			startDates.put(curDate, 0);
		}

		List<Date> feasibleDates = new ArrayList<Date>();

		for(Map.Entry<Date, Integer> dateEntry : startDates.entrySet()) {			
			if(dateEntry.getValue()  > 0) {
				feasibleDates.add(dateEntry.getKey());
			}
		}

		return feasibleDates;
	}

	public Map<String, List<Partner>> buildCountryDates(List<Partner> partners) 
	{
		//Map<Date, Integer> startDates = new HashMap<Date, Integer>();

		Map<String, List<Partner>> countryDates = new HashMap<String, List<Partner>>();

		for(Partner p : partners) {

			List<Partner> partnerList = new ArrayList<Partner>();

			if(countryDates.containsKey(p.getCountry())) {
				partnerList = countryDates.get(p.getCountry());				
				partnerList.add(p);
			}
			else {
				partnerList.add(p);
				countryDates.put(p.getCountry(), partnerList);
			}
		}

		return countryDates;
	}

	public Map<String, Map<Date, List<Partner>>> getInvitationList(List<Partner> partnerList) {

		Map<String, List<Partner>> countryMap = buildCountryDates(partnerList);
		
		Map<String, Map<Date, List<Partner>>> invitatonMap = new HashMap<String, Map<Date, List<Partner>>>();

		for(Map.Entry<String, List<Partner>> countryEntry : countryMap.entrySet()) {

			List<Partner> partners = countryEntry.getValue();

			Set<Date> setDate = new TreeSet<Date>();
			
			for(Partner p : partners) {
				setDate.addAll(feasibleDatesForPartner(p));			
			}		
			
			//countryDate.put(countryEntry.getKey(), setDate);
			Map<Date, List<Partner>> partnerMap = new TreeMap<Date, List<Partner>>();
			
			for(Partner p : partners) {
				List<Date> partnerDate = feasibleDatesForPartner(p);
				
				for(Date d : partnerDate) {
					if(setDate.contains(d)) {
						List<Partner> par = new ArrayList<Partner>();
						if(partnerMap.containsKey(d)) {
							par = partnerMap.get(d);							
						}					
						par.add(p);
						partnerMap.put(d, par);
					}
				}
			}
			
			invitatonMap.put(countryEntry.getKey(), partnerMap);
			
		}
		
		return invitatonMap;
	}
	
	public List<Invitation> getInvites(List<Partner> partners) {
		
		List<Invitation> invites = new ArrayList<Invitation>();
		
		Map<String, Map<Date, List<Partner>>> invitationMap = getInvitationList(partners);
		
		for(Entry<String, Map<Date, List<Partner>>> invitationEntry : invitationMap.entrySet()) {
			
			Map<Date, List<Partner>> dateMap = invitationEntry.getValue();
			
			int max = -1;
			Date date  = null;
			List<Partner> availablePartnerList = null;

			for (Map.Entry<Date, List<Partner>> entry : dateMap.entrySet()) {

				List<Partner> partnerList = entry.getValue();

				if(partnerList.size() > max) {
					max = partnerList.size();
					date = entry.getKey();
					availablePartnerList = partnerList;
				}
			}	
			
			Invitation i = new Invitation();
			
			i.setAttendeeCount(availablePartnerList.size());

			List<String> emailList  = new ArrayList<String>();
			for(Partner partner : availablePartnerList)
				emailList.add(partner.getEmail());
			i.setAttendees(emailList);

			i.setName(invitationEntry.getKey());
			i.setStartDate(date);
			invites.add(i);
		}
		
		return invites;
	}
	
}
