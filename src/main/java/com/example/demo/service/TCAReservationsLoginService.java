package com.example.demo.service;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

public interface TCAReservationsLoginService {

	@Retryable(include =  { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 100))
	public String getLoginToken() throws Exception;

	
	@Recover
	public void storeExceptionIntoBlob(Exception e);
}
