package com.example.demo.service.impl;

import java.time.Duration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.demo.exception.ReservationDataConsumerApplicationException;
import com.example.demo.exception.StoreExceptionToBlob;

import reactor.util.retry.Retry;

@Component
public class ReservationConsumerEHServiceImpl {
	private static final String TOTALEVENT="totalEvent";
	private static final String LOGINTOKEN="loginToken";
	
	public String consumeHotelData(String accessToken) throws Exception{
		long before = System.currentTimeMillis();
		 WebClient webConsumerClient = WebClient.builder().baseUrl("https://ehub-sb-poc-ehreservationconsumer.azuremicroservices.io").build();
		 //WebClient webConsumerClient = WebClient.builder().baseUrl("http://localhost:9090").build();
		 try {
			 
			 ResponseEntity<String> response = webConsumerClient.get()
					 .uri(uriBuilder -> uriBuilder.path("/consumer")
							 .queryParam(TOTALEVENT, TCAReservationsDataService.eventDataList.size())
							 .queryParam(LOGINTOKEN, accessToken)
							 .build())
					 //.bodyValue(KeyVaultProcessor.getKeyVaultProperties())
					 .accept()
					 .retrieve()
					 .toEntity(String.class)
					 //.timeout(Duration.ofSeconds(5))
					 .retryWhen(Retry.backoff(3, Duration.ofSeconds(5))
							 .doAfterRetry(retrySignal->{ System.out.println("Retried for connecting to reservation data consumer applicationendpoint" +
									 retrySignal.totalRetries()); }))
					 .block();
			 String res = response.getBody();
			 long after = System.currentTimeMillis(); 
			 String totalTimeTaken = "Total time taken at consumer application: " + (after - before) / 1000.0 + " seconds.\n";
			 return res+totalTimeTaken;
			 
		 } catch(Exception e) {
				if(e.getCause() instanceof WebClientResponseException) {
					
					WebClientResponseException cause =(WebClientResponseException) e.getCause();
					System.out.println(cause.getResponseBodyAsString());
					System.out.println(cause.getMostSpecificCause());
					System.out.println(cause.getMessage());
					String errorMessage = "Error Message: " + cause.getMessage() + "Specific Cause: "
							+ cause.getMostSpecificCause() + "Response Body: " + cause.getResponseBodyAsString();
					storeExceptionIntoBlob(errorMessage.strip());
					long after = System.currentTimeMillis(); 
					String totalTimeTaken = "Total time taken at consumer application: " + (after - before) / 1000.0 + " seconds.\n";
					throw new ReservationDataConsumerApplicationException(cause);
					//return totalTimeTaken;
				} else if(e.getCause() instanceof WebClientRequestException) {
					WebClientRequestException cause =(WebClientRequestException) e.getCause();
					System.out.println(cause.getRootCause());
					System.out.println(cause.getMostSpecificCause());
					System.out.println(cause.getMessage());
					String errorMessage = "Error Message: " + cause.getMessage() + "Specific Cause: "
							+ cause.getMostSpecificCause() + "Root Cause: " + cause.getRootCause();
					storeExceptionIntoBlob(errorMessage.strip());
					long after = System.currentTimeMillis(); 
					String totalTimeTaken = "Total time taken at consumer application: " + (after - before) / 1000.0 + " seconds.\n";
					throw new ReservationDataConsumerApplicationException(cause);
					//return totalTimeTaken;
					
				} else {
					
					System.out.println(e.getLocalizedMessage());
					System.out.println(e.getMessage());
					System.out.println(e.getCause());
					String errorMessage = "Error Message: " + e.getMessage() + "Cause: " + e.getCause() + "Message: "
							+ e.getLocalizedMessage();
					storeExceptionIntoBlob(errorMessage.strip());
					long after = System.currentTimeMillis(); 
					String totalTimeTaken = "Total time taken at consumer application: " + (after - before) / 1000.0 + " seconds.\n";
					throw new ReservationDataConsumerApplicationException(e);
					//return totalTimeTaken;
				}
				
			}
		 
	}

	
	public String storeExceptionIntoBlob(String e) {
		StoreExceptionToBlob.updateToLatestFolder(e.toString());
		StoreExceptionToBlob.storingExceptionInArchiveLocation(e.toString(), "Reservations");
		return null;
	}
}
