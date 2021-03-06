package org.quadcopter.controller.view.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;
import org.quadcopter.controller.controller.Quadcopter;
import org.quadcopter.controller.view.Main;
import org.quadcopter.model.SettingsData;

import android.os.Environment;
import android.util.Log;

public class ThreadSocketReader extends Thread {
	private BufferedReader in;
	private PrintWriter out;
	private boolean run = true;
	private Socket socket;
	private Quadcopter quad;

	private static FileOutputStream sJsonFile = null;

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
				quad.getController().requestPing(Integer.parseInt(tokens[3]));					
			} else {
				int num = Integer.parseInt(tokens[3]);
				quad.onPingResponse(num);				
			}
			break;
		case Quadcopter.BATTERY:
			if (tokens[2].charAt(0) == '0') {
				quad.getController().responseBattery(Integer.parseInt(tokens[3]));
			}
			break;
		case Quadcopter.RADIO_LEVEL:
			if (tokens[2].charAt(0) == '0') {
				quad.getController().responseRadioLevel(Integer.parseInt(tokens[3]));
			}
			break;
		case Quadcopter.MOVE:
			if (tokens[2].charAt(0) == '0') {
				quad.getController().responseMove();
			}
			break;
		case Quadcopter.GYRO:
			if (tokens[2].charAt(0) == '0') {
				float x, y, z;
				x = Float.parseFloat(tokens[3]);
				y = Float.parseFloat(tokens[4]);
				z = Float.parseFloat(tokens[5]);
				quad.getController().responseGyro(x, y, z);
			}
			break;
		case Quadcopter.ACCELEROMETER:
			if (tokens[2].charAt(0) == '0') {
				float x, y, z;
				x = Float.parseFloat(tokens[3]);
				y = Float.parseFloat(tokens[4]);
				z = Float.parseFloat(tokens[5]);
				quad.getController().responseAccel(x, y, z);
			}
			break;
		case Quadcopter.CALIBRATE:
			if (tokens[2].charAt(0) == '0') {
				quad.getController().reponseCalibrate();
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
		case Quadcopter.CONFIG_READ:
			if (tokens[2].charAt(0) == '0') {
				SettingsData data = new SettingsData();
				float p, i;
				
				p = Float.parseFloat(tokens[3]);
				i = Float.parseFloat(tokens[4]);
				
				data.setPidPValue(p);
				data.setPidIValue(i);
				quad.getController().onReponseConfig(data);
			}
			break;
		case Quadcopter.CONFIG_WRITE:
			if (tokens[2].charAt(0) == '0') {
				quad.getController().reponseWriteConfig();
			}
		break;
		case Quadcopter.ESC_CONFIG:
			if (tokens[2].charAt(0) == '0') {
				quad.getController().reponseEscConfigMode();
			}
			break;
		case Quadcopter.ESC_CONFIG_DATA:
			if (tokens[2].charAt(0) == '0') {
				quad.getController().reponseEscConfigData();
			}
			break;
		case Quadcopter.BLACKBOX:
			if (tokens[2].charAt(0) == '1') {
				String json = "{ "+tokens[3]+" }\n";
				try {
					JSONObject reader = new JSONObject(json);
					if (reader.opt("gains") != null) {
						openFileJson();
					}

					if (sJsonFile == null)
						openFileJson();
					sJsonFile.write(json.getBytes());
					sJsonFile.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		default:
			Log.d(Main.TAG, "Invalid message type: "+tokens[1].charAt(0));
		}
	}

	private void openFileJson() {
		try {
			File sdCard = Environment.getExternalStorageDirectory();
			File dir = new File (sdCard.getAbsolutePath() + "/quad/");
			dir.mkdirs();

			String filename = new SimpleDateFormat("yyyy-MM-dd_HH:mm").format(new Date());
			filename = filename+"_json.txt";
			File file = new File(dir, filename);

			if (sJsonFile != null)
				sJsonFile.close();
			sJsonFile = new FileOutputStream(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}