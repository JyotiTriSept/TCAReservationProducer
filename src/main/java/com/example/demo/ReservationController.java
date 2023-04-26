package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReservationController {
	
	
	@Autowired
	ReservationDataPublisherToEventHub publisher;
	
	@GetMapping("/process")
	public String startProcess() throws Exception {
		String readingReservationDataTime = publisher.transferData();
		
		
		
		return readingReservationDataTime;
	}

}
