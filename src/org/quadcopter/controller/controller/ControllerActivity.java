package org.quadcopter.controller.controller;

public interface ControllerActivity {	
	public void pingRequest(int num);//quad ask ping
	public void pingResponse(int num);//quad awnser ping
	public void batteryResponse(int percent);
	public void radioLevelResponse(int value);
	public void moveResponse();
	public void gyroResponse(int x, int y, int z);
	public void accelReponse(int x, int y, int z);
	public void calibrateReponse();
}
