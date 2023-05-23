package com.example.demo.service.impl.avalon;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Future;

import org.json.simple.JSONObject;
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
import com.example.demo.service.impl.DownloadReservationDataFromBlob;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
public class AvalonGetReservationsDeltaServiceImplementation {
	
	private static final String HOTEL ="hotel";
	private static final String RESERVATION_DELTA_DATA ="RservationDeltaInfo";
	


	
	@Async("publisherExecutor")
	public Future<EventData> getReservationsData( String hotel) throws WebClientResponseException, Exception {
		 WebClient webClient = WebClient.builder().baseUrl("https://cs-lab.amr.innsist.tca-ss.com/api").build();
		
		System.out.println("Invoking an asynchronous method. " 
				+ Thread.currentThread().getName());
		try {
			
			
			ResponseEntity<String> responseEntity = webClient.post()
					                                .uri("/ISL/ListPendingReservations")
					                                .header("Authorization", "Bearer ")
					                                .accept(MediaType.APPLICATION_JSON, MediaType.ALL).contentType(MediaType.APPLICATION_JSON)
					                                .retrieve()
					                                //.onStatus(status -> (status.value() == 204 || status.value() == 500 || status.value() == 400),clientResponse -> Mono.empty())
					                                .onStatus(status -> (status.value() == 204 ),clientResponse -> Mono.empty())
					                                //.onStatus(status -> (status.value() != 204 || status.value() != 200), clientResponse-> clientResponse.createException())
					                                .toEntity(String.class)
					                                .retryWhen(Retry.backoff(3, Duration.ofSeconds(5)).doAfterRetry(retrySignal -> {
						                                System.out.println("Retried Avalon Reservation Delta API: " + retrySignal.totalRetries());
					                                  }))
					                                .block();
			
			
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(HOTEL, hotel);
			
			if(responseEntity.getBody() == null || responseEntity.getStatusCodeValue() == 500) {
				//jsonObj.put(RESERVATIONDATA, " ");
				jsonObj=readReservationDeltaDataFromLocalFile(jsonObj);
			}else {
				
				jsonObj.put(RESERVATION_DELTA_DATA, responseEntity.getBody());
			}
           
			
			return new AsyncResult<EventData>(new EventData(jsonObj.toString()));

		} catch (Exception e) {
			if (e.getCause() instanceof WebClientResponseException) {

				WebClientResponseException cause = (WebClientResponseException) e.getCause();
				System.out.println(cause.getResponseBodyAsString());
				System.out.println(cause.getMostSpecificCause());
				System.out.println(cause.getMessage());
				String storedMessage = "Error Message: " + cause.getMessage() + "Specific Cause: "
						+ cause.getMostSpecificCause() + "Response Body: " + cause.getResponseBodyAsString();
				StoreReservExceptionToBlob.updateToLatestFolder(storedMessage, "Reservations",hotel);
				StoreReservExceptionToBlob.storingExceptionInArchiveLocation(storedMessage,"Reservations", hotel);
				return new AsyncResult<EventData>(null);
				
			} else if (e.getCause() instanceof WebClientRequestException) {
				WebClientRequestException cause = (WebClientRequestException) e.getCause();
				System.out.println(cause.getRootCause());
				System.out.println(cause.getMostSpecificCause());
				System.out.println(cause.getMessage());
				
				String storedMessage = "Error Message: " + cause.getMessage() + "Specific Cause: "
						+ cause.getMostSpecificCause() + "Root Cause: " + cause.getRootCause();
				StoreReservExceptionToBlob.updateToLatestFolder(storedMessage, "Reservations",hotel);
				StoreReservExceptionToBlob.storingExceptionInArchiveLocation(storedMessage,"Reservations", hotel);
				return new AsyncResult<EventData>(null);

			} else {

				System.out.println(e.getLocalizedMessage());
				System.out.println(e.getMessage());
				System.out.println(e.getCause());
				String storedMessage = "Error Message: " + e.getMessage() + "Cause: " + e.getCause() + "Message: "
						+ e.getLocalizedMessage();
				StoreReservExceptionToBlob.updateToLatestFolder(storedMessage, "Reservations",hotel);
				StoreReservExceptionToBlob.storingExceptionInArchiveLocation(storedMessage,"Reservations", hotel);
				return new AsyncResult<EventData>(null);
			}

		}
		
	}

	
	private static JSONObject readReservationDeltaDataFromLocalFile(JSONObject jsonObj) throws IOException {

		String downloadReservationBlobData = DownloadReservationDataFromBlob.downloadReservationBlobData();
		jsonObj.put(RESERVATION_DELTA_DATA, downloadReservationBlobData.trim());
		return jsonObj;

	}
}
