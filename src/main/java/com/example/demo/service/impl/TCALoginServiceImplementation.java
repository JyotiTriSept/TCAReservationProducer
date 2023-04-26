package com.example.demo.service.impl;

import java.time.Duration;
import java.util.LinkedHashMap;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.demo.exception.AccessTokenNotAvailableException;
import com.example.demo.exception.InvalidTCAResponse;
import com.example.demo.exception.StoreLoginExceptionToADLS;
import com.example.demo.exception.TCALoginApiException;
import com.example.demo.model.LoginModel;

import reactor.util.retry.Retry;

@Component
public class TCALoginServiceImplementation {
	private static final String ACCESS_TOKEN = "access_token";
	public static String accessToken = null;

	public String getLoginToken() throws Exception {
		LoginModel loginReq = new LoginModel("AMR", "Y#QFgJKns%c3-yh-");

		WebClient webConsumerClient = WebClient.builder().baseUrl("https://cs-lab.amr.innsist.tca-ss.com/api").build();
		long before = System.currentTimeMillis();
		try {

			ResponseEntity<Object> responseEntity = webConsumerClient.post()
					                                .uri(uriBuilder -> uriBuilder.path("/LoginAPI").build())
					                                .contentType(MediaType.APPLICATION_JSON)
					                                .bodyValue(loginReq).accept().retrieve()
					                                .toEntity(Object.class)
					                                .retryWhen(Retry.backoff(3, Duration.ofSeconds(5)).doAfterRetry(retrySignal -> {
						                                System.out.println("Retried TCA Login API: " + retrySignal.totalRetries());
					                                  }))
					                                .block();
			if(responseEntity.getBody() != null) {
				
				LinkedHashMap<String, Object> responseBody = (LinkedHashMap<String, Object>) responseEntity.getBody();
				
				if (responseBody.get(ACCESS_TOKEN) != null) {
					
					accessToken = (String) responseBody.get(ACCESS_TOKEN);
					long after = System.currentTimeMillis();
					String totalTimeTaken = "Total time taken for successfully fetching access token from TCA: "
							+ (after - before) / 1000.0 + " seconds.\n";
					return totalTimeTaken;
				} else {
					storeExceptionIntoBlob("Accces Token not present in TCA login response.");
					throw new AccessTokenNotAvailableException("Accces Token not present in response");
				}
			} else {
				String issue  = "Empty response from TCA get Hotel Api";
				System.out.println(issue);
				storeExceptionIntoBlob(issue);
				throw new InvalidTCAResponse(issue);
			}

		} /*catch (AccessTokenNotAvailableException cause) {
			System.out.println(cause.getMessage());
			String errorMessage = "Error Message: " + cause.getMessage() + "Specific Cause: "
					+ cause.getCause();
			storeExceptionIntoBlob(errorMessage.strip());
			long after = System.currentTimeMillis();
			String totalTimeTaken = "Total time taken after error occured while fetching access token from TCA: "
					+ (after - before) / 1000.0 + " seconds.\n";
			
			return totalTimeTaken;
			
		}catch (InvalidTCAResponse cause) {
			System.out.println(cause.getMessage());
			String errorMessage = "Error Message: " + cause.getMessage() + "Specific Cause: "
					+ cause.getCause();
			storeExceptionIntoBlob(errorMessage.strip());
			long after = System.currentTimeMillis();
			String totalTimeTaken = "Total time taken after error occured while fetching access token from TCA: "
					+ (after - before) / 1000.0 + " seconds.\n";
			return totalTimeTaken;
		}*/catch (Exception e) {
			if (e.getCause() instanceof WebClientResponseException) {

				WebClientResponseException cause = (WebClientResponseException) e.getCause();
				System.out.println(cause.getResponseBodyAsString());
				System.out.println(cause.getMostSpecificCause());
				System.out.println(cause.getMessage());
				String errorMessage = "Error Message: " + cause.getMessage() + "Specific Cause: "
						+ cause.getMostSpecificCause() + "Response Body: " + cause.getResponseBodyAsString();
				storeExceptionIntoBlob(errorMessage.strip());
				throw new TCALoginApiException(cause);
			} else if (e.getCause() instanceof WebClientRequestException) {
				WebClientRequestException cause = (WebClientRequestException) e.getCause();
				System.out.println(cause.getRootCause());
				System.out.println(cause.getMostSpecificCause());
				System.out.println(cause.getMessage());
				String errorMessage = "Error Message: " + cause.getMessage() + "Specific Cause: "
						+ cause.getMostSpecificCause() + "Root Cause: " + cause.getRootCause();
				storeExceptionIntoBlob(errorMessage.strip());

				throw new TCALoginApiException(cause);

			} else {

				System.out.println(e.getLocalizedMessage());
				System.out.println(e.getMessage());
				System.out.println(e.getCause());
				String errorMessage = "Error Message: " + e.getMessage() + "Cause: " + e.getCause() + "Message: "
						+ e.getLocalizedMessage();
				storeExceptionIntoBlob(errorMessage.strip());
				throw new TCALoginApiException(e);
			}

		}

	}

	public String storeExceptionIntoBlob(String e) {
		StoreLoginExceptionToADLS.updateToLatestFolder(e.toString());
		StoreLoginExceptionToADLS.storingExceptionInArchiveLocation(e.toString(), "Login");
		return null;
	}

}
