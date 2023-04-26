package com.example.demo.service;

import java.util.concurrent.Future;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;

public interface TCAGetReservationsDataService {

	@Retryable(include = {WebClientResponseException.class , Exception.class}, maxAttempts = 3, backoff = @Backoff(200))
	public Future<EventData> getReservationsData(String accessToken, String hotelData) throws WebClientResponseException, Exception;

	
	@Recover
	public Future<EventData> recover(WebClientResponseException exc, String accessToken,String hotelData) throws org.json.simple.parser.ParseException;
}
