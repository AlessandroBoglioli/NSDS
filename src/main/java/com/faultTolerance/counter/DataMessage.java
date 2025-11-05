package com.faultTolerance.counter;

public class DataMessage {

	private int code;	// Used in the actor to perform different actions based on this
	
	public int getCode() {
		return code;
	}

	public DataMessage(int code) {
		this.code = code;
	}
	
}
