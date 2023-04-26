package com.example.demo.model;

public class ReservationModel {

	private String group_code;
	private String brand_code;
	private String hotel_code;

	public ReservationModel(String group_code, String brand_code, String hotel_code) {
		super();
		this.group_code = group_code;
		this.brand_code = brand_code;
		this.hotel_code = hotel_code;
	}

	public String getGroup_code() {
		return group_code;
	}

	public void setGroup_code(String group_code) {
		this.group_code = group_code;
	}

	public String getBrand_code() {
		return brand_code;
	}

	public void setBrand_code(String brand_code) {
		this.brand_code = brand_code;
	}

	public String getHotel_code() {
		return hotel_code;
	}

	public void setHotel_code(String hotel_code) {
		this.hotel_code = hotel_code;
	}

}
