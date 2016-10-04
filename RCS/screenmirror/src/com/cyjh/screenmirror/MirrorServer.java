package com.cyjh.screenmirror;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.graphics.Point;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import com.cyjh.screenmirror.utils.Utils;

public class MirrorServer extends Thread {

	private static final String LOGTAG = "MirrorDisplay";
	private static final int SERVER_PORT = 12580; // 10086; //63000;

	private ServerSocket m_serverSock;
	private static MirrorServer s_server;

	private MirrorCmdServer cmdServer;

	private static final int START_MIRROR_DISPLAY = 0x6d640001;
	private static final int STOP_MIRROR_DISPLAY = 0x6d640002;
	private static final int START_MIRROR_DISPLAY_RESPONSE = 0x6d64003;
	private static final int START_MIRROR_INPUT = 0x6d690001;
	private static final int STOP_MIRROR_INPUT = 0x6d690002;

	private OutputDevice mDisplay;

	public static class CONNECT_STATE {
		public static final int CLIENT_NOT_CONNECTED = 1;
		public static final int CLIENT_CONNECTING = 2;
		public static final int CLIENT_CONNECTED = 3;
	};

	private int clientConnected = CONNECT_STATE.CLIENT_NOT_CONNECTED;
	private Object connectLock = new Object();

	private boolean mQuitting = false;

	public void setConnectState(int state) {
		if (state != CONNECT_STATE.CLIENT_NOT_CONNECTED
				&& state != CONNECT_STATE.CLIENT_CONNECTING
				&& state != CONNECT_STATE.CLIENT_CONNECTED)
			return;

		if (state == clientConnected)
			return;

		synchronized (connectLock) {
			clientConnected = state;
		}
	}

	public int getConnectState(){
		return clientConnected;
	}
	private MirrorServer(String name) {
		this.setName(name);
		cmdServer = new MirrorCmdServer();
	}

	private boolean initServer() {
		try {
			m_serverSock = new ServerSocket(SERVER_PORT, 1);
		} catch (UnknownHostException e) {
			Log.e(LOGTAG, "Unkown host " + e);
			return false;
		} catch (IOException e) {
			Log.e(LOGTAG, "IOException " + e);
			return false;
		} catch (Exception e) {
			Log.e(LOGTAG, "Exception " + e);
		}

		if (m_serverSock == null) {
			Log.e(LOGTAG, "Mirror Server Create Socket failed");
			return false;
		}
		Log.i(LOGTAG, "MirrorServer " + "port: " + m_serverSock.getLocalPort()
				+ " host:  " + m_serverSock.getInetAddress().getHostAddress());
		return true;
	}

	public static MirrorServer getMirrorServer() {
		if (s_server == null) {
			s_server = new MirrorServer("Mirror");
			if (s_server.initServer())
				return s_server;
			else
				s_server = null;

		}
		return s_server;
	}

	@Override
	public void run() {
		while (!mQuitting) {
			try {
				// not allow more than more connection!!
				while (clientConnected != CONNECT_STATE.CLIENT_NOT_CONNECTED) {
					try {
						if (mQuitting)
							break;

						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}

				if (mQuitting)
					break;

				Socket local = m_serverSock.accept();

				setConnectState(CONNECT_STATE.CLIENT_CONNECTING);

				local.setTcpNoDelay(true);
				InputStream is = local.getInputStream();
				OutputStream os = local.getOutputStream();

				// byte buf[] = new byte[4];
				// is.read(buf, 0, 4);
				// int cmd = Utils.bytes2Int(buf, 0);
				int cmd = START_MIRROR_DISPLAY;

				switch (cmd) {
				case START_MIRROR_DISPLAY:
					Log.d(LOGTAG, "Received display connection request");
					handleDisplayConnect(is, os);
					break;
				case STOP_MIRROR_DISPLAY:
					Log.d(LOGTAG, "Received display disconnect request");
					handleDisplayDisconnect();
					break;
				case START_MIRROR_INPUT:
					Log.d(LOGTAG, "Received input connection request");
					handleInputConnect();
					break;
				case STOP_MIRROR_INPUT:
					Log.d(LOGTAG, "Received input disconnect request");
					handleInputDisconnect();
					break;
				}

			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		} // end while

		Log.i("MirrorDisplay", "video server exit~~");
		try {
			cmdServer.setExit();
			cmdServer.join();
		} catch (InterruptedException e) {
		} finally {
			Looper.getMainLooper().quit();
		}
	}

	private void handleInputConnect() {
		// TODO Auto-generated method stub

	}

	private void handleDisplayDisconnect() {
		// TODO Auto-generated method stub

	}

	private void handleInputDisconnect() {
		// TODO Auto-generated method stub
	}

	private void handleDisplayConnect(InputStream is, OutputStream os) {
		// try {
		// byte response[] = Utils.int2Bytes(START_MIRROR_DISPLAY_RESPONSE);
		// os.write(response, 0, 4);
		// }catch(IOException e) {
		// return ;
		// }

		if (Utils.supportsVDF()) {
			VirtualDisplayFactory vdf = new SurfaceControlVirtualDisplayFactory();

			Point size = SurfaceControlVirtualDisplayFactory
					.getCurrentDisplaySize();
			Log.d(LOGTAG, "displaySize " + size.x + "x" + size.y);

			// mDisplay = new OutputDevice(os, size.x, size.y);
			// mDisplay = new OutputDevice(os, size.x*6/20, size.y*6/20);
			mDisplay = new OutputDevice(os, 1280, 720);
			mDisplay.registerVirtualDisplay(null, vdf, 0);
		} else {
			Log.w(LOGTAG, "use screenshot for uyou");
			int quality = 0;
			/*
			 * 从/sdcard/.screenmirror.quality读取quality
			 */
			File file = Environment.getExternalStorageDirectory();
			file = new File(file, ".screenmirror.quality");
			InputStream in = null;
			try {
				in = new FileInputStream(file);
				byte bytes[] = new byte[1024];
				int length = in.read(bytes);
				if (length < 0) {
					quality = 100;
				} else {
					String str = new String(bytes, 0, length);
					quality = Integer.parseInt(str.trim());
				}
			} catch (Exception e) {
				quality = 100;
			} finally {
				try {
					in.close();
				} catch (Exception e) {
					Log.d(LOGTAG, "close input stream", e);
				}
			}
			Log.d(LOGTAG, "quality=" + quality);
			
			mDisplay = new OutputDevice(os, 1280, 720, quality);
			mDisplay.registerScreenshot();
		}

		setConnectState(CONNECT_STATE.CLIENT_CONNECTED);
	}

	private void stopMirror() {
		mQuitting = true;
		if (m_serverSock != null) {
			try {
				m_serverSock.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void requestStop() {
		try {
			String cmd = new String("ExitMirror");
			DatagramSocket sock = new DatagramSocket();
			InetAddress ia = InetAddress
					.getByAddress(new byte[] { 127, 0, 0, 1 });
			DatagramPacket packet = new DatagramPacket(cmd.getBytes(),
					cmd.length(), ia, 10001);
			sock.send(packet);
			sock.close();
		} catch (SocketException e) {
		} catch (UnknownHostException e) {
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
		}
	}

	// return true if server is running
	public static boolean checkMirrorServer() {
		try {
			DatagramSocket sk = new DatagramSocket();
			sk.setSoTimeout(1500);

			byte response[] = new byte[100];

			String cmd = "ping";
			DatagramPacket packet = new DatagramPacket(cmd.getBytes(),
					cmd.length(), InetAddress.getByAddress(new byte[] { 127, 0,
							0, 1 }), 10001);
			sk.send(packet);
			Log.e("MirrorDisplay", "ping server");
			packet = new DatagramPacket(response, response.length,
					InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }),
					10001);
			sk.receive(packet);
			sk.close();
			String result = new String(response, 0, packet.getLength());
			if (result.equals("pong"))
				return true;
		} catch (SocketTimeoutException e) {
			Log.e("MirrorDisplay", "ping ret 11");
		} catch (SocketException e) {
			Log.e("MirrorDisplay", "ping ret 12");
		} catch (IOException e) {
			Log.e("MirrorDisplay", "ping ret 13");
		}

		return false;
	}

	class MirrorCmdServer extends Thread {
		private MirrorCmdServer() {
			super("mirror_cmd");
			try {
				Log.e("MirrorDisplay", "start mirrorcmd server");
				sock = new DatagramSocket(10001);
				sock.setSoTimeout(2000);
				start();
			} catch (SocketException e) {
			}
		}

		@Override
		public void finalize() {
			if (sock != null) {
				sock.close();
				sock = null;
			}
		}

		public void run() {
			Log.e("MirrorDisplay", "enter mirror server cmd loop ");
			byte cmd[] = new byte[100];

			while (mRun) {
				try {
					DatagramPacket packet = new DatagramPacket(cmd, cmd.length);
					sock.receive(packet);
					String str = new String(cmd, 0, packet.getLength());
					Log.i("MirrorDisplay", "server cmd Receive " + str);
					if (str.equalsIgnoreCase("ExitMirror")) {
						Log.i("MirrorDisplay", "stopMirror called");
						stopMirror();
						break;
					}
					if (str.equals("ping")) {
						str = "pong";
						packet = new DatagramPacket(str.getBytes(),
								str.length(), packet.getAddress(),
								packet.getPort());
						sock.send(packet);
					}
					Thread.sleep(500);

				} catch (SocketTimeoutException e) {
				} catch (IOException e) {
				} catch (InterruptedException e) {
				} catch (Exception e) {
				}
			}

			sock.close();
		}

		public void setExit() {
			mRun = false;
		}

		private DatagramSocket sock;
		private boolean mRun = true;
	}

}
