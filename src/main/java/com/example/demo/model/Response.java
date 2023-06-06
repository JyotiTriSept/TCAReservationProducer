package com.example.demo.model;

public class Response {
	private boolean error;
	private String responseMessage;
	private boolean repeat;

	public Response(boolean repeat) {
		super();
		this.setRepeat(repeat);
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public boolean isRepeat() {
		return repeat;
	}

	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}

}
