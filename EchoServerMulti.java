/**************************************************
 * Breeana Nikaido
 * May 1, 2016
 **************************************************/
import java.io.*;
import java.net.Socket; 
import java.net.ServerSocket; 
import java.util.Vector;

public class EchoServerMulti extends Thread {
	protected Vector<ConnectionToClient> connections;
	
	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null; 
		Vector<ConnectionToClient> connections = new Vector<ConnectionToClient>();
		EchoServerMulti echoServer = new EchoServerMulti(connections);
		
		try { 
			serverSocket = new ServerSocket(4444); 
			System.out.println ("Connection Socket Established");
			echoServer.start();
			try { 
				while (true)
				{
					System.out.println ("Waiting for Connection...");
					Socket clientSocket = serverSocket.accept();
					connections.add(new ConnectionToClient(clientSocket));
					connections.lastElement().start();
					System.out.println ("New Communication Thread Established");
				}
			} catch (IOException e) {
				System.err.println("Accept failed!"); 
				System.exit(1); 
			} 
		} catch (IOException e) {
				System.err.println("Could not listen on port: 4444."); 
				System.exit(1); 
		} finally {
			try {
				serverSocket.close(); 
			}
			catch (IOException e) { 
				System.err.println("Could not close port: 4444."); 
				System.exit(1); 
			} 
		}
	}
	
	public EchoServerMulti(Vector<ConnectionToClient> con) {
		this.connections = con;
	}
	
	public void run() {
		while(true) {
			for (int i = 0; i < connections.size(); i++) {
				ConnectionToClient current = connections.get(i);
				if (!current.isAlive()) {
					connections.remove(i);
				}
				boolean encrypted = current.isEncrypted();
				String message = current.getMessage();
				if (message != null) {
					System.out.println ("Server BroadCast: " + message);
					for (ConnectionToClient j: connections) {
						j.println(message, encrypted);
					}
				}
			}
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
