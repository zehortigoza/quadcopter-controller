package org.quadcopter.controller.controller;

import java.net.ServerSocket;


public class SocketListener extends Thread {
	private int port;
	private NotifyNewSocketInterface notify;

	private Boolean run = false;

	public SocketListener(NotifyNewSocketInterface notify, int port) {
		this.notify = notify;
		this.port = port;
	}

	public void exit() {
		run = false;
		notify = null;
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
			try {
				notify.socketConnected(serverSocket.accept());
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
