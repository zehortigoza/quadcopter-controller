package org.quadcopter.model;

import java.io.Serializable;

public class SettingsData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private float pidPValue;
	private float pidIValue;
	
	public float getPidPValue() {
		return pidPValue;
	}
	
	public void setPidPValue(float pidPValue) {
		this.pidPValue = pidPValue;
	}
	
	public float getPidIValue() {
		return pidIValue;
	}
	
	public void setPidIValue(float pidIValue) {
		this.pidIValue = pidIValue;
	}
	
	public String toString() {
		return "{ P="+pidPValue+" I="+pidIValue+" }";
	}
}
