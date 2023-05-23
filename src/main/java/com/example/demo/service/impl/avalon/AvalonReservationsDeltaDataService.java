package com.example.demo.service.impl.avalon;

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
import com.example.demo.producer.ReservationDeltaEventHubProducer;
import com.example.demo.service.TCAGetReservationsDataService;
import com.example.demo.service.impl.ReservationDeltaConsumerEHServiceImpl;

import reactor.core.publisher.Mono;

@Component
public class AvalonReservationsDeltaDataService {

	@Autowired
	AvalonGetReservationsDeltaServiceImplementation avalonSer;
	
	@Autowired
	ReservationDeltaEventHubProducer producer;
	
	@Autowired
	ReservationDeltaConsumerEHServiceImpl reservationConsumer;
	
	static List<EventData> eventDataList = new ArrayList<>();
	
    
	public String getReservationsData( List<String> hotelList) throws Exception {
		
		long before = System.currentTimeMillis();
		
		Collection<Future<EventData>> futures = new ArrayList<Future<EventData>>();

		for (String data : hotelList) {
			futures.add(avalonSer.getReservationsData( data));
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
	    
	    String response = reservationConsumer.consumeHotelData();
	    
	    
	    return reservationTime+response;
	
	}

	

	


	




}
