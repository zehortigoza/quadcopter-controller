package org.quadcopter.controller.controller;

public interface Controller {	
	public void requestPing(int num);//quad ask ping
	public void responsePing(int num);//quad awnser ping
	public void responseBattery(int percent);
	public void responseRadioLevel(int value);
	public void responseMove();
	public void responseGyro(float x, float y, float z);
	public void responseAccel(float x, float y, float z);
	public void reponseCalibrate();

	public void connectedQuad();
	public void disconnectedQuad();
}
