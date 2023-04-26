package com.example.demo.exception;

public class ReservationDataConsumerApplicationException extends Exception{
	public ReservationDataConsumerApplicationException(Throwable ex) {
		super(ex);
		//System.out.println("Error code is: "+status.name()+" "+status.value());
	}

}
