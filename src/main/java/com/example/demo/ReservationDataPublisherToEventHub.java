package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.model.Response;
import com.example.demo.service.impl.TCALoginServiceImplementation;
import com.example.demo.service.impl.TCAReservationsDataService;

@Component
public class ReservationDataPublisherToEventHub {

	@Autowired
	TCALoginServiceImplementation tcaReservLoginService;

	@Autowired
	TCAReservationsDataService tcaService;

	public String transferData(Response res) throws Exception {
		System.out.println("Fetch hotel data from Blob location");
		String hotelData = DownloadHotelDataFromADLS.downloadHotelData();
		//String hotelData = DownloadHotelDataFromBlob.downloadBlobData();
		System.out.println("No of Hotels: " + hotelData.lines().count());
		System.out.println("Fetch bearer token from login");
		String bearerToken = tcaReservLoginService.getLoginToken();
		System.out.println("Bearer token fetched: " + bearerToken);
		System.out.println("Get Reservation Data");
		String timeToGetReservationData = tcaService.getReservationsData( hotelData, res);
		return timeToGetReservationData;

	}

}
