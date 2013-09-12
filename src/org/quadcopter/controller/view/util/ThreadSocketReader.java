package org.quadcopter.controller.view.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.quadcopter.controller.controller.Quadcopter;
import org.quadcopter.controller.view.Main;

import android.util.Log;

public class ThreadSocketReader extends Thread {
	private BufferedReader in;
	private PrintWriter out;
	private boolean run = true;
	private Socket socket;
	private Quadcopter quad;
	
	public ThreadSocketReader(Socket socket, Quadcopter quad) {
		this.socket = socket;
		this.quad = quad;
		try {
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.out = new PrintWriter(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void exit() {
		run = false;
	}
	
	public PrintWriter getPrintWriter() {
		return out;
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
		Log.d(Main.TAG, "ThreadListener - socket close");
	}
	
	public void run() {
		while (run) {
			try {
				String msg = in.readLine();
				if (msg == null)
						socketClose();
				Log.d(Main.TAG, "msg received: "+msg);
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
		
		switch(tokens[1].charAt(0)) {
		case Quadcopter.PING:
			if (tokens[2].charAt(0) == '1') {
				quad.controllerGet().requestPing(Integer.parseInt(tokens[3]));					
			} else {
				int num = Integer.parseInt(tokens[3]);
				quad.handlePing(num);				
			}
			break;
		case Quadcopter.BATTERY:
			if (tokens[2].charAt(0) == '0') {
				quad.controllerGet().responseBattery(Integer.parseInt(tokens[3]));
			}
			break;
		case Quadcopter.RADIO_LEVEL:
			if (tokens[2].charAt(0) == '0') {
				quad.controllerGet().responseRadioLevel(Integer.parseInt(tokens[3]));
			}
			break;
		case Quadcopter.MOVE:
			if (tokens[2].charAt(0) == '0') {
				quad.controllerGet().responseMove();
			}
			break;
		case Quadcopter.GYRO:
			if (tokens[2].charAt(0) == '0') {
				float x, y, z;
				x = Float.parseFloat(tokens[3]);
				y = Float.parseFloat(tokens[4]);
				z = Float.parseFloat(tokens[5]);
				quad.controllerGet().responseGyro(x, y, z);
			}
			break;
		case Quadcopter.ACCELEROMETER:
			if (tokens[2].charAt(0) == '0') {
				float x, y, z;
				x = Float.parseFloat(tokens[3]);
				y = Float.parseFloat(tokens[4]);
				z = Float.parseFloat(tokens[5]);
				quad.controllerGet().responseAccel(x, y, z);
			}
			break;
		case Quadcopter.CALIBRATE:
			if (tokens[2].charAt(0) == '0') {
				quad.controllerGet().reponseCalibrate();
			}
			break;
		case Quadcopter.DEBUG_MSG:
			Log.d(Main.TAG, "debug: "+tokens[3]);
			break;
		case Quadcopter.ORIENTATION:
			if (tokens[2].charAt(0) == '0') {
				float roll, pitch, yaw;
				roll = Float.parseFloat(tokens[3]);
				pitch = Float.parseFloat(tokens[4]);
				yaw = Float.parseFloat(tokens[5]);
				if (Quadcopter.getSensorActivity() != null)
					Quadcopter.getSensorActivity().updateOrientation(roll, pitch, yaw);
			}
			break;
		default:
			Log.d(Main.TAG, "Invalid message type: "+tokens[1].charAt(0));
		}
	}
}