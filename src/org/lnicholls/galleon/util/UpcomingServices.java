package org.lnicholls.galleon.util;

/*
 * Copyright (C) 2005 Leon Nicholls
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * 
 * See the file "COPYING" for more details.
 */

import java.util.ArrayList;
import java.util.List;

import com.socialistsoftware.upcoming.Country;
import com.socialistsoftware.upcoming.Event;
import com.socialistsoftware.upcoming.Metro;
import com.socialistsoftware.upcoming.State;
import com.socialistsoftware.upcoming.Upcoming;
import com.socialistsoftware.upcoming.Venue;

public class UpcomingServices {

	// This is the upcoming.org API key for this project. Do not use it for
	// another project.
	// Get your own API key from http://www.upcoming.org
	private final static String API_KEY = "3e22a47fac";

	public static List getCountries() {
		ArrayList countries = new ArrayList();
		try {
			Upcoming up = new Upcoming(null, null, API_KEY);

			ArrayList lists = up.getCountryList();
			for (int i = 0; i < lists.size(); i++) {
				Country country = (Country) lists.get(i);
				countries.add(new NameValue(country.getName(), country.getId()));
			}
		} catch (Exception ex) {
			Tools.logException(UpcomingServices.class, ex, "Could not get upcoming countries");
		}

		return countries;
	}

	public static List getStates(String countryId) {
		ArrayList states = new ArrayList();
		try {
			Upcoming up = new Upcoming(null, null, API_KEY);

			ArrayList lists = up.getStateList(countryId);
			for (int i = 0; i < lists.size(); i++) {
				State state = (State) lists.get(i);
				states.add(new NameValue(state.getName(), state.getId()));
			}
		} catch (Exception ex) {
			Tools.logException(UpcomingServices.class, ex, "Could not get upcoming states: " + countryId);
		}

		return states;
	}

	public static List getMetros(String stateId) {
		ArrayList metros = new ArrayList();
		try {
			Upcoming up = new Upcoming(null, null, API_KEY);

			ArrayList lists = up.getMetroList(stateId);
			for (int i = 0; i < lists.size(); i++) {
				Metro metro = (Metro) lists.get(i);
				metros.add(new NameValue(metro.getName(), metro.getId()));
			}
		} catch (Exception ex) {
			Tools.logException(UpcomingServices.class, ex, "Could not get upcoming metros: " + stateId);
		}

		return metros;
	}

	public static List getEvents(String countryId, String stateId, String metroId) {
		ArrayList events = new ArrayList();
		try {
			Upcoming up = new Upcoming(null, null, API_KEY);

			ArrayList lists = up.searchForEvent("", countryId, stateId, metroId, "", "", "", "", "100", "",
					"start-date-asc");

			for (int i = 0; i < lists.size(); i++) {
				Event event = (Event) lists.get(i);
				events.add(event);
			}
		} catch (Exception ex) {
			Tools.logException(UpcomingServices.class, ex, "Could not get upcoming events: " + metroId);
		}

		return events;
	}
	
	public static List searchEvents(String search, String countryId, String stateId, String metroId) {
		ArrayList events = new ArrayList();
		try {
			Upcoming up = new Upcoming(null, null, API_KEY);

			ArrayList lists = up.searchForEvent(search, countryId, stateId, metroId, "", "", "", "", "100", "",
					"start-date-asc");

			for (int i = 0; i < lists.size(); i++) {
				Event event = (Event) lists.get(i);
				events.add(event);
			}
		} catch (Exception ex) {
			Tools.logException(UpcomingServices.class, ex, "Could not get upcoming events: " + metroId);
		}

		return events;
	}

	public static List getVenues(String metroId) {
		ArrayList events = new ArrayList();
		try {
			Upcoming up = new Upcoming(null, null, API_KEY);

			ArrayList lists = up.searchForVenue("", "", "", metroId);

			for (int i = 0; i < lists.size(); i++) {
				Venue venue = (Venue) lists.get(i);
				events.add(venue);
			}
		} catch (Exception ex) {
			Tools.logException(UpcomingServices.class, ex, "Could not get upcoming venues: " + metroId);
		}

		return events;
	}
	
	public static List getVenueInfo(String venueId) {
		try {
			Upcoming up = new Upcoming(null, null, API_KEY);

			return up.getVenueInfo(venueId);
		} catch (Exception ex) {
			Tools.logException(UpcomingServices.class, ex, "Could not get upcoming venues info: " + venueId);
		}

		return null;
	}
	
	public static String getMetroId(String country, String state, String metro)
	{
		List countries = getCountries();
		for (int i = 0; i < countries.size(); i++) {
			NameValue countryId = (NameValue) countries.get(i);
			if (countryId.getName().equals(country))
			{
				List states = getStates(countryId.getValue());
				for (int j = 0; j < states.size(); j++) {
					NameValue stateId = (NameValue) states.get(j);
					if (stateId.getName().equals(state))
					{
						List metros = getMetros(stateId.getValue());
						for (int k = 0; k < metros.size(); k++) {
							NameValue metroId = (NameValue) metros.get(k);
							if (metroId.getName().equals(metro))
							{
								return metroId.getValue();
							}
						}
					}
				}
			}
		}
		return null;
	}
}