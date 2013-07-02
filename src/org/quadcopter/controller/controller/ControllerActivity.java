package org.quadcopter.controller.controller;

public interface ControllerActivity {	
	public void requestPing(int num);//quad ask ping
	public void responsePing(int num);//quad awnser ping
	public void responseBattery(int percent);
	public void responseRadioLevel(int value);
	public void responseMove();
	public void responseGyro(int x, int y, int z);
	public void responseAccel(int x, int y, int z);
	public void reponseCalibrate();

	public void connectedQuad();
	public void disconnectedQuad();
}
