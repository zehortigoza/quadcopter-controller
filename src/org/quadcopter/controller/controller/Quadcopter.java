package org.quadcopter.controller.controller;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.quadcopter.controller.view.util.ThreadSocketReader;
import org.quadcopter.controller.view.util.ThreadSocketServer;
import org.quadcopter.controller.view.util.ThreadSocketWriter;
import org.quadcopter.model.SettingsData;

import android.util.Log;

public class Quadcopter {
	public static final int PORT = 9750;
	public static final String IP = "192.168.43.7";
	public static final char PING = 'p';
	public static final char BATTERY = 'b';
	public static final char RADIO_LEVEL = 'r';
	public static final char MOVE = 'm';
	public static final char GYRO = 'g';
	public static final char ACCELEROMETER = 'a';
	public static final char ORIENTATION = 'o';
	public static final char CALIBRATE = 'c';
	public static final char AXIS_X = 'x';
	public static final char AXIS_Y = 'y';
	public static final char AXIS_Z = 'z';
	public static final char AXIS_ROTATE = 'r';
	public static final char DEBUG_MSG = 'd';
	public static final char CONFIG_READ = 'f';
	public static final char CONFIG_WRITE = 'w';
	
	private Controller controller = null;
	private ThreadSocketServer socketListen;
	
	private static Sensors sSensorActivity = null;

	private List<ThreadSocketReader> threadReadList = null;
	
	private ScheduledExecutorService scheduleTaskExecutor;
	
	private boolean connected = false;
	private int waitingPingResponse = 180;
	
	//last move, send it every 400miliseconds
	private char axis;
	private int value;
	ScheduledFuture<?> repeater = null;

	private static Quadcopter sQuadcopter = null;
	
	public static Quadcopter getInstance() {
		if (sQuadcopter == null)
			sQuadcopter = new Quadcopter();
		return sQuadcopter;
	}

	private Quadcopter() {
		socketListen = new ThreadSocketServer(this, PORT);
		socketListen.start();
		threadReadList = new ArrayList<ThreadSocketReader>();
		scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
		
		scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (waitingPingResponse != 0) {
					if (connected == true) {
						if (Quadcopter.this.controller != null)
							Quadcopter.this.controller.disconnectedQuad();
						Log.d("quad2", "disconnected waitingPingResponse="+waitingPingResponse);
						connected = false;
					}
				}
				Log.d("quad2", "task each 5 seconds.");
				requestPing(179);
				waitingPingResponse = 180;
			}
		}, 0, 5, TimeUnit.SECONDS);
	}
	
	public void handlePing(int num) {
		if (waitingPingResponse == num) {
			waitingPingResponse = 0;
			Log.d("quad2", "ping handled");
			if (connected == false) {
				connected = true;
				if (controller != null)
					controller.connectedQuad();
			}
		}
		return;
	}
	
	public void controllerSet(Controller controller) {
		this.controller = controller;
		if (connected == true)
			controller.connectedQuad();
		else
			controller.disconnectedQuad();
	}

	public Controller controllerGet() {
		return controller;
	}

	public void exit() {
		socketListen.exit();
		socketListen = null;
		exitThreads();
		controller = null;
		scheduleTaskExecutor.shutdown();
		scheduleTaskExecutor = null;
	}

	private synchronized void exitThreads() {
		for (ThreadSocketReader reader : threadReadList) {
			reader.exit();
		}
	}

	private synchronized void appendThread(ThreadSocketReader listener) {
		threadReadList.add(listener);
	}

	private synchronized ThreadSocketReader getReader() {
		if (threadReadList.size() > 0)
			return threadReadList.get(0);
		else
			return null;
	}

	public synchronized void removeThread(ThreadSocketReader listener) {
		threadReadList.remove(listener);
		if (socketListen == null && threadReadList.size() == 0)
			threadReadList = null;
	}

	public boolean connectedSocket(Socket socket) {
		ThreadSocketReader listener = null;
		try {			
			listener = new ThreadSocketReader(socket, this);
			listener.start();
			appendThread(listener);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public void requestOrientation(final boolean enable) {
		int resp = enable ? 1 : 0;
		new ThreadSocketWriter("^;o;1;"+resp+";$", getReader()).start();
	}
	
	public void requestPing(final int num) {
		Log.d("quad2", "ping="+num);
		new ThreadSocketWriter("^;p;1;"+num+";$", getReader()).start();
	}
	
	public void responsePing(int num) {
		new ThreadSocketWriter("^;p;0;"+num+";$", getReader()).start();
	}
	
	public void requestBattery() {
		new ThreadSocketWriter("^;b;1;$", getReader()).start();
	}
	
	public void requestRadioLevel() {
		new ThreadSocketWriter("^;r;1;$", getReader()).start();
	}
	
	public void requestMove(char axis, int value) {
		if (axis != AXIS_ROTATE && axis != AXIS_X &&
		    axis != AXIS_Y && axis != AXIS_Z)
			return;
		new ThreadSocketWriter("^;m;1;"+axis+";"+value+";$", getReader()).start();
		
		if (axis != AXIS_Z) {
			this.axis = axis;
			this.value = value;
			
			if (repeater != null)
				repeater.cancel(false);
			repeater = null;
			
			if (value == 0)
				return;
			
			repeater = scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					new ThreadSocketWriter("^;m;1;"+Quadcopter.this.axis+";"+Quadcopter.this.value+";$", getReader()).start();
				}
			}, 400, 400, TimeUnit.MILLISECONDS);
		}
	}
	
	public void requestGyro() {
		new ThreadSocketWriter("^;g;1;$", getReader()).start();
	}
	
	public void requestAccel() {
		new ThreadSocketWriter("^;a;1;$", getReader()).start();
	}
	
	public void requestConfigs() {
		new ThreadSocketWriter("^;f;1;$", getReader()).start();
	}
	
	public void writeConfigs(SettingsData data) {
		new ThreadSocketWriter("^;w;1;"+data.getPidPValue()+";"+data.getPidIValue()+";$", getReader()).start();
	}
	
	public void requestCalibrate() {
		new ThreadSocketWriter("^;c;1;$", getReader()).start();
	}

	public static void setSensorActivity(Sensors activity) {
		sSensorActivity = activity;
	}

	public static Sensors getSensorActivity() {
		return sSensorActivity;
	}
}