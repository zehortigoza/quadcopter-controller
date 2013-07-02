package org.quadcopter.controller.view.util;

import java.io.PrintWriter;
import java.net.Socket;

import org.quadcopter.controller.controller.Quadcopter;

import android.util.Log;

public class ThreadSocketWriter extends Thread {
	private String msg = null;
	
	public ThreadSocketWriter(String msg) {
		this.msg = msg;
	}
	
	public void run() {
		Socket socket;
		PrintWriter out;
		try {
			socket = new Socket(Quadcopter.IP, Quadcopter.PORT);
			out = new PrintWriter(socket.getOutputStream());
			out.println(msg);
			out.flush();
			out.close();
			socket.close();
			Log.d("quad", "send: "+msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		msg = null;
	}
}
