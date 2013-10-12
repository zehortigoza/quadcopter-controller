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
	public static final char CONFIG_READ = 'f';//controller ask for configs
	public static final char CONFIG_WRITE = 'w';//controller write configs
	public static final char ESC_CONFIG = 'e';//controller enable esc config mode
	public static final char ESC_CONFIG_DATA = 'h';//controller send esc configs data
	public static final char BLACKBOX = 'z';
	
	private Controller mController = null;
	private ThreadSocketServer mSocketListen;
	
	private static Sensors sSensorActivity = null;

	private List<ThreadSocketReader> mThreadReadList = null;
	
	private ScheduledExecutorService mScheduleTaskExecutor;
	
	private boolean mConnected = false;
	private boolean mFirstTimeoutPing = true;
	private int mWaitingPingResponse = 180;
	
	//last move, send it every 400miliseconds
	private char mAxis;
	private int mValue;
	ScheduledFuture<?> mRepeater = null;

	private static Quadcopter sQuadcopter = null;
	
	private Object mSyncPing = new Object();

	public static Quadcopter getInstance() {
		if (sQuadcopter == null)
			sQuadcopter = new Quadcopter();
		return sQuadcopter;
	}

	private Quadcopter() {
		mSocketListen = new ThreadSocketServer(this, PORT);
		mSocketListen.start();
		mThreadReadList = new ArrayList<ThreadSocketReader>();
		mScheduleTaskExecutor = Executors.newScheduledThreadPool(5);
		
		mScheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				synchronized (mSyncPing) {
					if (mWaitingPingResponse != 0) {
						if (mConnected == true) {
							if (mFirstTimeoutPing) {
								mFirstTimeoutPing = false;
							} else {
								if (Quadcopter.this.mController != null)
									Quadcopter.this.mController.onQuadDisconnected();
								Log.d("quad", "****disconnected waitingPingResponse="+mWaitingPingResponse);
								mConnected = false;
							}
						}
					}
					Log.d("quad2", "task each 5 seconds.");
					requestPing(179);
					mWaitingPingResponse = 180;
				}
			}
		}, 0, 2, TimeUnit.SECONDS);
	}
	
	public void onPingResponse(int num) {
		synchronized (mSyncPing) {
			if (mWaitingPingResponse == num) {
				mWaitingPingResponse = 0;
				mFirstTimeoutPing = true;
				Log.d("quad2", "ping handled");
				if (mConnected == false) {
					mConnected = true;
					if (mController != null)
						mController.onQuadConnected();
				}
			}
		}
		return;
	}
	
	public void setController(Controller controller) {
		this.mController = controller;
		if (mConnected == true)
			controller.onQuadConnected();
		else
			controller.onQuadDisconnected();
	}

	public Controller getController() {
		return mController;
	}

	public void exit() {
		mSocketListen.exit();
		mSocketListen = null;
		exitThreads();
		mController = null;
		mScheduleTaskExecutor.shutdown();
		mScheduleTaskExecutor = null;
	}

	private synchronized void exitThreads() {
		for (ThreadSocketReader reader : mThreadReadList) {
			reader.exit();
		}
	}

	private synchronized void appendThread(ThreadSocketReader listener) {
		mThreadReadList.add(listener);
	}

	private synchronized ThreadSocketReader getReader() {
		if (mThreadReadList.size() > 0)
			return mThreadReadList.get(0);
		else
			return null;
	}

	public synchronized void removeThread(ThreadSocketReader listener) {
		mThreadReadList.remove(listener);
		if (mSocketListen == null && mThreadReadList.size() == 0)
			mThreadReadList = null;
	}

	public boolean onSocketConnectionOpen(Socket socket) {
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
			this.mAxis = axis;
			this.mValue = value;
			
			if (mRepeater != null)
				mRepeater.cancel(false);
			mRepeater = null;
			
			if (value == 0)
				return;
			
			mRepeater = mScheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					new ThreadSocketWriter("^;m;1;"+Quadcopter.this.mAxis+";"+Quadcopter.this.mValue+";$", getReader()).start();
				}
			}, 400, 400, TimeUnit.MILLISECONDS);
		}
	}
	
	public void requestArm() {
		new ThreadSocketWriter("^;s;1;$", getReader()).start();
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

	public void setEscConfigurationMode(boolean enable) {
		new ThreadSocketWriter("^;"+ESC_CONFIG+";1;"+(enable ? 1 : 0)+";$", getReader()).start();
	}

	public void setEscValues(int fl, int fr, int bl, int br) {
		new ThreadSocketWriter("^;"+ESC_CONFIG_DATA+";1;"+fl+";"+fr+";"+bl+";"+br+";$", getReader()).start();
	}
}