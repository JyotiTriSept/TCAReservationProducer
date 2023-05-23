package com.example.demo;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.service.impl.TCALoginServiceImplementation;
import com.example.demo.service.impl.TCAReservationsDataService;
import com.example.demo.service.impl.avalon.AvalonReservationsDeltaDataService;

@Component
public class ReservationDeltaDataPublisherToEventHub {

	

	@Autowired
	AvalonReservationsDeltaDataService reservDeltaService;

	public String transferData() throws Exception {
		System.out.println("Fetch hotel data from Blob location");
		String hotelData = DownloadAvalonHotelDataFromADLS2.downloadHotelData();
		List<String> hotelsList = getHotels(hotelData);
		
		System.out.println("Get Reservation Data");
		String timeToGetReservationData = reservDeltaService.getReservationsData( hotelsList);
		return timeToGetReservationData;

	}

	private List<String> getHotels(String hotelData){
		ArrayList<String> list = new ArrayList<>();
		JSONObject jsonObject;
		try {
			
			jsonObject = (JSONObject) new JSONParser().parse(hotelData);
			JSONArray hotelList = (JSONArray) jsonObject.get("LSHotel");
			for(int i=0; i<hotelList.size();i++) {
				JSONObject parsedJSONObj = (JSONObject) hotelList.get(i);
				String hotel =(String) parsedJSONObj.get("Hotel");
				list.add(hotel);
			
			}
			return list;
		}catch (ParseException e) {
			// TODO: handle exception
			return null;
		}
	}
}
