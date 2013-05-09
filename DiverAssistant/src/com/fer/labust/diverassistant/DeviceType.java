package com.fer.labust.diverassistant;

public enum DeviceType {
	COMMUNICATION(0), GPS(1), DEPTH_SENSOR(2);
	private int value;
	
	private DeviceType(int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
}
