/**************************************************
 * Breeana Nikaido
 * May 1, 2016
 **************************************************/
import java.io.*;
import java.net.Socket;

public class ConnectionToClient extends Thread {
		private Socket clientSocket;
		private DataOutputStream out;
		private DataInputStream in;
		private String message;
		private boolean encrypted;
		
	public ConnectionToClient (Socket clientSoc) throws IOException {
		this.clientSocket = clientSoc;
		this.out = new DataOutputStream(clientSocket.getOutputStream());
		this.in = new DataInputStream(clientSocket.getInputStream()); 
	}
	
	public void println(String s, boolean encrypted) {
		try {
			if(encrypted) {
				out.writeByte('E');
			} else {
				out.writeByte('S');
			}
			out.writeUTF(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isEncrypted() {
		return encrypted;
	}
	
	public synchronized void setMessage(String s, boolean isEncrypted) {
		if (message != null) {
			try {
				wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		message = s;
		encrypted = isEncrypted;
	}
	
	public synchronized String getMessage() {
		if (message == null) {
			return null;
		}
		String ret = message;
		message = null;
		notifyAll();
		return ret;
	}
	
	public void run () {
		try {		
			for(int code; (code = in.read()) != -1;) {
				switch(code) {
					case 'S':
						setMessage(in.readUTF(), false);
						break;
					case 'E':
						setMessage(in.readUTF(), true);
						break;
					default:
						break;
				}
			}
			out.close(); 
			in.close(); 
			clientSocket.close(); 
		} catch (IOException e) {
				System.err.println("Problem with Communication Server. Connection Lost!");
				System.exit(1); 
		}
	}
}