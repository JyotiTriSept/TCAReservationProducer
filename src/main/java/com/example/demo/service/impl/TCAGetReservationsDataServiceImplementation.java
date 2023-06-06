package com.example.demo.service.impl;

import java.io.IOException;
import java.time.Duration;
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
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.azure.messaging.eventhubs.EventData;
import com.example.demo.exception.StoreReservExceptionToBlob;
import com.example.demo.model.ReservationModel;
import com.example.demo.model.Response;
import com.example.demo.producer.Producer;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
public class TCAGetReservationsDataServiceImplementation {
	
	private static final String GROUPCODE ="group_code";
	private static final String BRANDCODE ="brand_code";
	private static final String HOTELCODE ="hotel_code";
	private static final String RESERVATIONDATA ="ReservationData";
	private static String fileName;
	
	@Autowired
	Producer producer;

	@Autowired
	ReservationConsumerEHServiceImpl reservationConsumer;
	
	@Async("publisherExecutor")
	public Future<String> getReservationsData( String hotelData, Response res) throws WebClientResponseException, Exception {
		long before = System.currentTimeMillis(); 
		
		WebClient webClient = WebClient.builder().baseUrl("https://cs-lab.amr.innsist.tca-ss.com/api").build();
		
		System.out.println("Invoking an asynchronous method. " 
				+ Thread.currentThread().getName());
		JSONParser jparser = new JSONParser();
		try {
			JSONObject parsedJsonObject = (JSONObject) jparser.parse(hotelData);
			fileName = "AMR"+"_"+(String) parsedJsonObject.get(BRANDCODE)+"_"+(String) parsedJsonObject.get(HOTELCODE);
			ReservationModel req = new ReservationModel("AMR", (String) parsedJsonObject.get(BRANDCODE),
					(String) parsedJsonObject.get(HOTELCODE));
			ResponseEntity<String> responseEntity = webClient.post()
					                                .uri("/ISL/ListPendingReservations")
					                                .header("Authorization", "Bearer " + TCALoginServiceImplementation.accessToken)
					                                .accept(MediaType.APPLICATION_JSON, MediaType.ALL).contentType(MediaType.APPLICATION_JSON)
					                                .bodyValue(req)
					                                .retrieve()
					                                //.onStatus(status -> (status.value() == 204 || status.value() == 500 || status.value() == 400),clientResponse -> Mono.empty())
					                                .onStatus(status -> (status.value() == 204 ),clientResponse -> Mono.empty())
					                                //.onStatus(status -> (status.value() != 204 || status.value() != 200), clientResponse-> clientResponse.createException())
					                                .toEntity(String.class)
					                                .retryWhen(Retry.backoff(3, Duration.ofSeconds(5)).doAfterRetry(retrySignal -> {
						                                System.out.println("Retried TCA get list of Reservation API: " + retrySignal.totalRetries());
					                                  }))
					                                .block();
			
			
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(GROUPCODE, "AMR");
			jsonObj.put(BRANDCODE, (String) parsedJsonObject.get(BRANDCODE));
			jsonObj.put(HOTELCODE, (String) parsedJsonObject.get(HOTELCODE));
			
			if(responseEntity.getBody() != null ) {
				
				jsonObj.put(RESERVATIONDATA, responseEntity.getBody());
			}
			List<EventData> eventDataList = new ArrayList<>();
			eventDataList.add(new EventData(jsonObj.toString()));
			
			String publishEvents = producer.publishEvents(eventDataList);
			System.out.println(publishEvents);
			long after = System.currentTimeMillis(); 
			String reservationTime = "Time it took for reservation data of hotel code: "+(String) parsedJsonObject.get(HOTELCODE) +" to be published to event hub: " + (after - before) / 1000.0 + " seconds.\n";
			
			return new AsyncResult<String>(reservationTime);

		} catch (ParseException e) {
     
			System.out.println(e.getLocalizedMessage());
			System.out.println(e.getMessage());
			System.out.println(e.getCause());
			String storedMessage = "Error Message: " + e.getMessage() + "Cause: " + e.getCause() + "Message: "
					+ e.getLocalizedMessage();
			StoreReservExceptionToBlob.updateToLatestFolder(storedMessage, "Reservations",TCAGetReservationsDataServiceImplementation.fileName);
			StoreReservExceptionToBlob.storingExceptionInArchiveLocation(storedMessage,"Reservations", TCAGetReservationsDataServiceImplementation.fileName);
			return new AsyncResult<String>(null);
		}catch (Exception e) {
			if (e.getCause() instanceof WebClientResponseException) {
                res.setError(true);
				WebClientResponseException cause = (WebClientResponseException) e.getCause();
				res.setResponseMessage(cause.getResponseBodyAsString());
				System.out.println(cause.getResponseBodyAsString());
				System.out.println(cause.getMostSpecificCause());
				System.out.println(cause.getMessage());
				String storedMessage = "Error Message: " + cause.getMessage() + "Specific Cause: "
						+ cause.getMostSpecificCause() + "Response Body: " + cause.getResponseBodyAsString();
				StoreReservExceptionToBlob.updateToLatestFolder(storedMessage, "Reservations",TCAGetReservationsDataServiceImplementation.fileName);
				StoreReservExceptionToBlob.storingExceptionInArchiveLocation(storedMessage,"Reservations", TCAGetReservationsDataServiceImplementation.fileName);
				return new AsyncResult<String>(null);
				
			} else if (e.getCause() instanceof WebClientRequestException) {
				res.setError(true);
				WebClientRequestException cause = (WebClientRequestException) e.getCause();
				res.setResponseMessage(cause.getMessage());
				System.out.println(cause.getRootCause());
				System.out.println(cause.getMostSpecificCause());
				System.out.println(cause.getMessage());
				
				String storedMessage = "Error Message: " + cause.getMessage() + "Specific Cause: "
						+ cause.getMostSpecificCause() + "Root Cause: " + cause.getRootCause();
				StoreReservExceptionToBlob.updateToLatestFolder(storedMessage, "Reservations",TCAGetReservationsDataServiceImplementation.fileName);
				StoreReservExceptionToBlob.storingExceptionInArchiveLocation(storedMessage,"Reservations", TCAGetReservationsDataServiceImplementation.fileName);
				return new AsyncResult<String>(null);

			} else {
				res.setError(true);

				System.out.println(e.getLocalizedMessage());
				System.out.println(e.getMessage());
				System.out.println(e.getCause());
				String storedMessage = "Error Message: " + e.getMessage() + "Cause: " + e.getCause() + "Message: "
						+ e.getLocalizedMessage();
				StoreReservExceptionToBlob.updateToLatestFolder(storedMessage, "Reservations",TCAGetReservationsDataServiceImplementation.fileName);
				StoreReservExceptionToBlob.storingExceptionInArchiveLocation(storedMessage,"Reservations", TCAGetReservationsDataServiceImplementation.fileName);
				return new AsyncResult<String>(null);
			}

		}
		
	}

	
	private static JSONObject readReservationDataFromLocalFile(JSONObject jsonObj) throws IOException {

		String downloadReservationBlobData = DownloadReservationDataFromBlob.downloadReservationBlobData();
		jsonObj.put("ReservationData", downloadReservationBlobData.trim());
		return jsonObj;

	}
}
