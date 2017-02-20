package com.hubspot.api.entity;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Invitation
{
	private int attendeeCount;

	private List<String> attendees;

	private String name;

	private Date startDate;

	public Invitation() {		
		attendees = new ArrayList<String>();
	}

	public int getAttendeeCount() {
		return attendeeCount;
	}

	public void setAttendeeCount(int attendeeCount) {
		this.attendeeCount = attendeeCount;
	}

	public List<String> getAttendees() {
		return attendees;
	}

	public void setAttendees(List<String> attendees) {
		this.attendees = attendees;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
}
