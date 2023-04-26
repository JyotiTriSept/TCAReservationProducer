package com.example.demo.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.example.demo.model.ReservationModel;
import com.example.demo.producer.Producer;
import com.example.demo.exception.StoreReservExceptionToBlob;
import com.example.demo.service.TCAGetReservationsDataService;

import reactor.core.publisher.Mono;

@Service
public class TCAGetReservationsDataServiceImpl implements TCAGetReservationsDataService{
	private static final String GROUPCODE ="group_code";
	private static final String BRANDCODE ="brand_code";
	private static final String HOTELCODE ="hotel_code";
	private static final String RESERVATIONDATA ="ReservationData";
	
	@Autowired
	Producer producer;

	static WebClient webClient = WebClient.builder().baseUrl("https://cs-lab.amr.innsist.tca-ss.com/api").build();

	
	@Override
	@Async("publisherExecutor")
	public Future<EventData> getReservationsData(String accessToken, String hotelData) throws WebClientResponseException, Exception {
		
		System.out.println("Invoking an asynchronous method. " 
				+ Thread.currentThread().getName());
		JSONParser jparser = new JSONParser();
		try {
			JSONObject parsedJsonObject = (JSONObject) jparser.parse(hotelData);

			ReservationModel req = new ReservationModel("AMR", (String) parsedJsonObject.get(BRANDCODE),
					(String) parsedJsonObject.get(HOTELCODE));
			ResponseEntity<String> responseEntity = webClient.post()
					                                .uri("/ISL/ListPendingReservations")
					                                .header("Authorization", "Bearer " + accessToken)
					                                .accept(MediaType.APPLICATION_JSON, MediaType.ALL).contentType(MediaType.APPLICATION_JSON)
					                                .bodyValue(req)
					                                .retrieve()
					                                .onStatus(status -> (status.value() == 204 || status.value() == 400 ),clientResponse -> Mono.empty())
					                                //.onStatus(status -> (status.value() == 204 ),clientResponse -> Mono.empty())
					                                //.onStatus(status -> (status.value() != 204 || status.value() != 200), clientResponse-> clientResponse.createException())
					                                .toEntity(String.class).block();
			
			
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(GROUPCODE, "AMR");
			jsonObj.put(BRANDCODE, (String) parsedJsonObject.get(BRANDCODE));
			jsonObj.put(HOTELCODE, (String) parsedJsonObject.get(HOTELCODE));
			
			if(responseEntity.getBody() == null || responseEntity.getStatusCodeValue() == 400) {
				//jsonObj=readReservationDataFromLocalFile(jsonObj);
				jsonObj=readReservationDataFromLocalFile(jsonObj);
			}else {
				
				jsonObj.put(RESERVATIONDATA, responseEntity.getBody());
			}
           
			
			return new AsyncResult<EventData>(new EventData(jsonObj.toString()));

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new AsyncResult<EventData>(null);
		}	
		
	}

	@Override
	public Future<EventData> recover(WebClientResponseException e,String accessToken, String hotelData) throws ParseException {
		

		System.out.println("Response Body: "+e.getResponseBodyAsString());
		System.out.println("Specific Cause: "+e.getMostSpecificCause());
		String error = "Error Message: "+e.getMessage()+"Specific Cause: "+e.getMostSpecificCause()+"Response Body: "+e.getResponseBodyAsString();
		//String storedMessage = error.stripIndent().strip();
		String storedMessage = error.strip();
	
		JSONParser jparser = new JSONParser();
		JSONObject parsedJsonObject =(JSONObject) jparser.parse(hotelData);
		String folderName = "AMR"+"_"+(String) parsedJsonObject.get(BRANDCODE)+"_"+(String) parsedJsonObject.get(HOTELCODE);
		
		StoreReservExceptionToBlob.updateToLatestFolder(storedMessage, "Reservations",folderName);
		StoreReservExceptionToBlob.storingExceptionInArchiveLocation(storedMessage,"Reservations", folderName);
		return new AsyncResult<EventData>(null);
	}

	
	private static JSONObject readReservationDataFromLocalFile(JSONObject jsonObj) throws IOException {

		String downloadReservationBlobData = DownloadReservationDataFromBlob.downloadReservationBlobData();
		jsonObj.put("ReservationData", downloadReservationBlobData.trim());
		return jsonObj;

	}
}
