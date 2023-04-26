package com.example.demo.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.example.demo.producer.Producer;
import com.example.demo.service.TCAGetReservationsDataService;

import reactor.core.publisher.Mono;

@Component
public class TCAReservationsDataService {

	@Autowired
	TCAGetReservationsDataServiceImplementation tcaSer;
	
	@Autowired
	Producer producer;
	
	@Autowired
	ReservationConsumerEHServiceImpl reservationConsumer;
	
	static List<EventData> eventDataList = new ArrayList<>();
	
    
	public String getReservationsData( String hotelData) throws Exception {
		
		long before = System.currentTimeMillis();
		
		Collection<Future<EventData>> futures = new ArrayList<Future<EventData>>();
		List<String> listOfHotelData = hotelData.lines().collect(Collectors.toList());

		for (String data : listOfHotelData) {
			futures.add(tcaSer.getReservationsData( data));
		}
		
		for (Future<EventData> future : futures) {
	        EventData eventData = future.get();
	        if(eventData != null) {
	        	eventDataList.add(eventData);
	        }
	    }
		
		String publishEvents = producer.publishEvents(eventDataList);
		System.out.println(publishEvents);
		long after = System.currentTimeMillis(); 
		String reservationTime = "Time it took for reservation data of all Hotel Data to be published to event hub: " + (after - before) / 1000.0 + " seconds.\n";
	    System.out.println(reservationTime);
	    
	  /*  ResponseEntity<String> response = webConsumerClient.get()
		                                   .uri(uriBuilder -> uriBuilder.path("/consumer")
		                                                      .queryParam("totalEvent", eventDataList.size())
		                                                      .queryParam("loginToken", accessToken)
		                                                      .build())
		                                                      .accept()
		                                                      .retrieve()
		                                                      .toEntity(String.class).block();*/
	    String response = reservationConsumer.consumeHotelData(TCALoginServiceImplementation.accessToken);
	    
	    
	    return reservationTime+response;
	
	}

	

	


	




}
