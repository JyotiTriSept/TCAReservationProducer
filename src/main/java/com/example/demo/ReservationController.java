package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReservationController {
	
	
	@Autowired
	ReservationDataPublisherToEventHub publisher;
	
	@Autowired
	ReservationDeltaDataPublisherToEventHub deltaPublisher;
	
	@GetMapping("/tca/reservData/process")
	public String startProcess() throws Exception {
		String readingReservationDataTime = publisher.transferData();
		
		
		
		return readingReservationDataTime;
	}

	@GetMapping("/valaon/reservDelta/process")
	public String startavalonProcess() throws Exception {
		String readingReservationDataTime = deltaPublisher.transferData();
		
		
		
		return readingReservationDataTime;
	}
}
