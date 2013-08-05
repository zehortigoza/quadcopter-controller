package org.quadcopter.controller.view.util;

import java.io.PrintWriter;
import java.net.Socket;

import org.quadcopter.controller.controller.Quadcopter;
import org.quadcopter.controller.view.Main;

import android.util.Log;

public class ThreadSocketWriter extends Thread {
	private String msg = null;
	private ThreadSocketReader reader;
	
	public ThreadSocketWriter(String msg, ThreadSocketReader reader) {
		this.msg = msg;
		this.reader = reader;
	}
	
	public ThreadSocketWriter(String msg) {
		this.msg = msg;
	}
	
	public void run() {
		Socket socket = null;
		PrintWriter out;
		try {
			if (reader != null) {
				out = reader.getPrintWriter();
			}
			else {
				socket = new Socket(Quadcopter.IP, Quadcopter.PORT);
				out = new PrintWriter(socket.getOutputStream());
			}
			out.println(msg);
			out.flush();
			if (socket != null) {
				out.close();
				socket.close();
			}
			Log.d(Main.TAG, "send: "+msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		msg = null;
	}
}
