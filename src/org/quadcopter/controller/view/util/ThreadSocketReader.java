package org.quadcopter.controller.view.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.quadcopter.controller.controller.Quadcopter;

import android.util.Log;

public class ThreadSocketReader extends Thread {
	private BufferedReader in;
	private boolean run = true;
	private Socket socket;
	private Quadcopter quad;
	
	public ThreadSocketReader(Socket socket, Quadcopter quad) {
		this.socket = socket;
		this.quad = quad;
		try {
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void exit() {
		run = false;
	}
	
	private void socketClose() {
		if (run == false)
			return;
		try {
			in.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}			
		run = false;
		Log.d("quad", "ThreadListener - socket close");
	}
	
	public void run() {
		while (run) {
			try {
				String msg = in.readLine();
				if (msg == null)
						socketClose();
				Log.d("quad", "msg received: "+msg);
				if (msg.charAt(0) == '^' && msg.endsWith("$"))
					msgHandle(msg);
			} catch (Exception e) {
				socketClose();
				e.printStackTrace();
			}
		}
		quad.removeThread(this);
	}
	
	private void msgHandle(String msg) {
		String tokens[] = msg.split(";");
		
		if (tokens[1].charAt(0) == Quadcopter.PING) {
			if (tokens[2].charAt(0) == '1') {
				quad.controllerGet().requestPing(Integer.parseInt(tokens[3]));					
			} else {
				int num = Integer.parseInt(tokens[3]);
				if (quad.handlePing(num) == false)
						quad.controllerGet().responsePing(num);					
			}
		} else if (tokens[1].charAt(0) == Quadcopter.BATTERY) {
			if (tokens[2].charAt(0) == '0') {
				quad.controllerGet().responseBattery(Integer.parseInt(tokens[3]));
			}
		} else if (tokens[1].charAt(0) == Quadcopter.RADIO_LEVEL) {
			if (tokens[2].charAt(0) == '0') {
				quad.controllerGet().responseRadioLevel(Integer.parseInt(tokens[3]));
			}
		} else if (tokens[1].charAt(0) == Quadcopter.MOVE) {
			if (tokens[2].charAt(0) == '0') {
				quad.controllerGet().responseMove();
			}
		} else if (tokens[1].charAt(0) == Quadcopter.GYRO) {
			if (tokens[2].charAt(0) == '0') {
				float x, y, z;
				x = Float.parseFloat(tokens[3]);
				y = Float.parseFloat(tokens[4]);
				z = Float.parseFloat(tokens[5]);
				quad.controllerGet().responseGyro(x, y, z);
			}
		} else if (tokens[1].charAt(0) == Quadcopter.ACCELEROMETER) {
			if (tokens[2].charAt(0) == '0') {
				float x, y, z;
				x = Float.parseFloat(tokens[3]);
				y = Float.parseFloat(tokens[4]);
				z = Float.parseFloat(tokens[5]);
				quad.controllerGet().responseAccel(x, y, z);
			}
		} else if (tokens[1].charAt(0) == Quadcopter.CALIBRATE) {
			if (tokens[2].charAt(0) == '0') {
				quad.controllerGet().reponseCalibrate();
			}
		}
	}
}