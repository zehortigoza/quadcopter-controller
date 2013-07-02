package org.quadcopter.controller.view.util;

import java.net.ServerSocket;
import java.net.Socket;

import org.quadcopter.controller.controller.Quadcopter;

import android.util.Log;


public class ThreadSocketServer extends Thread {
	private int port;
	private Quadcopter quad;

	private Boolean run = false;

	public ThreadSocketServer(Quadcopter quad, int port) {
		this.quad = quad;
		this.port = port;
	}

	public void exit() {
		run = false;
		quad = null;
	}

	public void run() {
		ServerSocket serverSocket = null;
		
		try {
			serverSocket = new ServerSocket(port);
			run = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (run) {
			Socket socket;
			try {
				socket = serverSocket.accept();
				Log.d("quad", "socket accept");
				if (socket != null)
					if (quad.connectedSocket(socket) == false)
						socket.close();					
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			if (serverSocket != null)
				serverSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
