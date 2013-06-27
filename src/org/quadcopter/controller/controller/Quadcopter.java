package org.quadcopter.controller.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


import android.util.Log;

public class Quadcopter implements NotifyNewSocketInterface {
	private static final int PORT = 8020;
	private static final String IP = "192.168.0.2";
	
	private static char PING = 'p';
	private static char BATTERY = 'b';
	private static char RADIO_LEVEL = 'r';
	private static char MOVE = 'm';
	private static char GYRO = 'g';
	private static char ACCELEROMETER = 'a';
	private static char CALIBRATE = 'c';
	private static char AXIS_X = 'x';
	private static char AXIS_Y = 'y';
	private static char AXIS_Z = 'z';
	private static char AXIS_ROTATE = 'r';
	
	private ControllerActivity activity;
	private SocketListener socketListern;
	
	private Socket socketIn = null;	
	private ThreadListener listener = null;
	
	private Socket socketOut = null;
	private PrintWriter out;
	
	public Quadcopter(ControllerActivity activity) {
		this.activity = activity;
		
		socketListern = new SocketListener(this, PORT);
		socketListern.start();
	}
	
	public void ControllerActivitySet(ControllerActivity activity) {
		this.activity = activity;
	}
	
	public void halt() {
		socketListern.exit();
		socketListern = null;
		if (listener != null)
			listener.exit();
		if (socketIn != null)
			socketDisconected();
		activity = null;
	}
	
	private void socketDisconected() {
		try {
			socketIn.close();
			socketIn = null;
			listener = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean socketConnected(Socket socket) {
		if (this.socketIn != null)
			return false;
		
		this.socketIn = socket;
		try {			
			listener = new ThreadListener(socket.getInputStream());
			listener.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	private boolean setupOut() {
		if (socketOut != null)
			return true;
		
		try {
			socketOut = new Socket(IP, PORT);
			out = new PrintWriter(socketOut.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private void closeOut() {
		out.flush();
		out.close();
		try {
			socketOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		socketOut = null;
		out = null;
	}
	
	public void pingRequest(int num) {
		setupOut();
		out.println("^;p;1;"+num+";$");
		closeOut();
	}
	
	public void pingResponse(int num) {
		setupOut();
		out.println("^;p;0;"+num+";$");
		closeOut();
	}
	
	public void batteryRequest() {
		setupOut();
		out.println("^;b;1;$");
		closeOut();
	}
	
	public void radioLevelRequest() {
		setupOut();
		out.println("^;r;1;$");
		closeOut();
	}
	
	public void moveRequest(char axis, int value) {
		if (axis != AXIS_ROTATE && axis != AXIS_X &&
		    axis != AXIS_Y && axis != AXIS_Z)
			return;
		setupOut();
		out.println("^;m;1;"+axis+";"+value+";$");
		closeOut();
	}
	
	public void gyroRequest() {
		setupOut();
		out.println("^;g;1;$");
		closeOut();
	}
	
	public void accelRequest() {
		setupOut();
		out.println("^;a;1;$");
		closeOut();
	}
	
	public void calibrateRequest() {
		setupOut();
		out.println("^;c;1;$");
		closeOut();
	}
	
	private class ThreadListener extends Thread {
		private BufferedReader in;
		private boolean run;		
		
		public ThreadListener (InputStream in) {
			this.in = new BufferedReader(new InputStreamReader(in));
		}
		
		public void exit() {
			run = false;
		}
		
		public void run() {
			while (run) {
				try {
					String msg = in.readLine();
					if (msg.charAt(0) == '^' && msg.endsWith("%"))
						msgHandle(msg);
				} catch (Exception e) {
					if (socketIn.isConnected() == false) {
						run = false;
						try {
							in.close();
						} catch (Exception e2) {
							e2.printStackTrace();
						}
						socketDisconected();
					}					
					e.printStackTrace();
				}
			}
		}
		
		private void msgHandle(String msg) {
			String tokens[] = msg.split(";");			
			Log.d("Quad", "Msg received: "+msg);
			
			if (tokens[0].charAt(0) != '^')
				return;
			if (tokens[tokens.length-1].charAt(0) != '$')
				return;
			
			if (tokens[1].charAt(0) == PING) {
				if (tokens[2].charAt(0) == '1') {
					activity.pingRequest(Integer.parseInt(tokens[3]));					
				} else {
					activity.pingResponse(Integer.parseInt(tokens[3]));					
				}
			} else if (tokens[1].charAt(0) == BATTERY) {
				if (tokens[2].charAt(0) == '0') {
					activity.batteryResponse(Integer.parseInt(tokens[3]));
				}
			} else if (tokens[1].charAt(0) == RADIO_LEVEL) {
				if (tokens[2].charAt(0) == '0') {
					activity.radioLevelResponse(Integer.parseInt(tokens[3]));
				}
			} else if (tokens[1].charAt(0) == MOVE) {
				if (tokens[2].charAt(0) == '0') {
					activity.moveResponse();
				}
			} else if (tokens[1].charAt(0) == GYRO) {
				if (tokens[2].charAt(0) == '0') {
					int x, y, z;
					x = Integer.parseInt(tokens[3]);
					y = Integer.parseInt(tokens[4]);
					z = Integer.parseInt(tokens[5]);
					activity.gyroResponse(x, y, z);
				}
			} else if (tokens[1].charAt(0) == ACCELEROMETER) {
				if (tokens[2].charAt(0) == '0') {
					int x, y, z;
					x = Integer.parseInt(tokens[3]);
					y = Integer.parseInt(tokens[4]);
					z = Integer.parseInt(tokens[5]);
					activity.accelReponse(x, y, z);
				}
			} else if (tokens[1].charAt(0) == CALIBRATE) {
				if (tokens[2].charAt(0) == '0') {
					activity.calibrateReponse();
				}
			}
		}
	}
}
