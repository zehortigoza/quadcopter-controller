package org.quadcopter.controller.controller;

import org.quadcopter.model.SettingsData;

public interface Controller {
	public void requestPing(int num);//quad ask ping
	public void responsePing(int num);//quad awnser ping
	public void responseBattery(int percent);
	public void responseRadioLevel(int value);
	public void responseMove();
	public void responseGyro(float x, float y, float z);
	public void responseAccel(float x, float y, float z);
	public void reponseCalibrate();
	public void onReponseConfig(SettingsData settings);
	public void reponseWriteConfig();

	public void onQuadConnected();
	public void onQuadDisconnected();
}
