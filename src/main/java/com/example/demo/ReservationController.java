package com.example.demo;

import com.example.demo.model.Response;
import com.example.demo.service.impl.TCALoginServiceImplementation;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
public class ReservationController {
	
	
	@Autowired
	ReservationDataPublisherToEventHub publisher;
	
	@Autowired
	ReservationDeltaDataPublisherToEventHub deltaPublisher;
	
	@GetMapping("/tca/reservData/process")
	public String startProcess() throws Exception {
		Response res = new Response(true);
		
		String readingReservationDataTime = publisher.transferData(res);
		/*
	    if(res.isError()) {
	    	JSONObject jsonObject = new JSONObject();
	    	jsonObject.put("email", "jyotikumari.jyotikumari@harman.com");
	    	jsonObject.put("task", "Action Required for Failure");
	    	jsonObject.put("ErrorMesaage", res.getResponseMessage());
	    	
	    	WebClient webClient = WebClient.builder().baseUrl("https://prod-02.centralus.logic.azure.com:443/workflows").build();
	    	ResponseEntity<String> responseEntity = webClient.post()
                    .uri("/ede6d43b1fd44719a1e911c633b17e45/triggers/manual/paths/invoke?api-version=2016-10-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=RQzwofPWzed0N9r1MueA0TMScd-PHmNp2c7RdcL6U_I")
                    .accept(MediaType.APPLICATION_JSON, MediaType.ALL).contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(jsonObject)
                    .retrieve()
                    .toEntity(String.class)
                    .block();
	    }*/
		
		
		
		return readingReservationDataTime;
	}

	@GetMapping("/valaon/reservDelta/process")
	public String startavalonProcess() throws Exception {
		String readingReservationDataTime = deltaPublisher.transferData();
		
		
		
		return readingReservationDataTime;
	}
}
