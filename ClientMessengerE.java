/**************************************************
 * Breeana Nikaido
 * May 1, 2016
 **************************************************/
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Random;

public class ClientMessengerE extends JFrame implements ActionListener {
	/* Client Identification */
	private String screenName;
	private boolean encryptMode;
	private int secretKey;
	/* Server Connection */
	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;
	/* GUI Items */
	private static final int TEXT_COL = 42;
	private static final int TEXT_ROW = 10;
	private JTextArea textBox = new JTextArea(TEXT_ROW, TEXT_COL);
	private JTextField inputBox = new JTextField(TEXT_COL);
	private JToggleButton encryptButton = new JToggleButton("Encrypt: Off", encryptMode);
	
	public static void main(String[] args) throws IOException {
		ClientMessengerE client = new ClientMessengerE(args[0], args[1]);
		client.listen();
	}
	
	public ClientMessengerE(String screenName, String hostName) {
		this.screenName = screenName;
		this.encryptMode = false;
		this.secretKey = -1;

		try {
			socket = new Socket(hostName, 4444);
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
            System.err.println("No I/O for connection to:" + hostName);
            System.exit(1);
        } catch (Exception e) { 
			e.printStackTrace();
			System.exit(1);
		}
		addWindowListener (
			new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					try {
						out.close();
					} catch (IOException exception) {
						//System.err.println("Cannot readLine.");
					}
				}
			}
		);	

		/* Initialize GUI */
		textBox.setEditable(false);
		textBox.setEditable(false);
		inputBox.addActionListener(this);
		encryptButton.addActionListener(this);
		/* Configure GUI Layout */
		Container content = getContentPane();
		content.add(encryptButton, BorderLayout.NORTH);
		content.add(new JScrollPane(textBox), BorderLayout.CENTER);
		content.add(inputBox, BorderLayout.SOUTH);
		/* Display */
		setTitle("Encryption Messenger [" + screenName + "]");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		inputBox.requestFocusInWindow();
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent event) {
		try {
			if(event.getSource() == inputBox) {
				String output = inputBox.getText();
				if(encryptMode) {
					output = encrypt("[" + screenName + "]:" + output);
					out.writeByte('E');
					out.writeUTF(output);
				} else {
					out.writeByte('S');
					out.writeUTF("[" + screenName + "]:" + output);
				}
				inputBox.setText("");
				inputBox.requestFocusInWindow();
			} else if (event.getSource() == encryptButton) {
				encryptMode = encryptButton.isSelected();
				if(encryptMode) {
					encryptButton.setText("Encrypt: On");
					while(secretKey < 0) {
						String choice = JOptionPane.showInputDialog(this, "Input any number.", "Select Key", JOptionPane.QUESTION_MESSAGE);
						try { 
							secretKey = Integer.parseInt(choice);

						} catch(NumberFormatException e) { 
						  JOptionPane.showMessageDialog(null, "Value must be an integer!", "Parse Error!", JOptionPane.WARNING_MESSAGE);
						}
					}
				}
				else {
					encryptButton.setText("Encrypt: Off");
					secretKey = -1;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void listen() {
		try {
			String newMessage = "Welcome!";
			textBox.insert(newMessage + "\n", textBox.getText().length());
			textBox.setCaretPosition(textBox.getText().length());
			for(int code; (code = in.read()) != -1;) {
				switch(code) {
					/* E for Encrypted */
					case 'E':
						if (encryptMode) {
							newMessage = decrypt(in.readUTF());
						} else {
							newMessage = in.readUTF();
						}
						break;
					/* S for String */
					case 'S':
						newMessage = in.readUTF();
						break;
					default:
						break;
				}
				textBox.insert(newMessage + "\n", textBox.getText().length());
				textBox.setCaretPosition(textBox.getText().length());
			}
			out.close();
			in.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println("Closed client socket.");
	}
	
	private String encrypt(String msg){
		Random rand = new Random((long)secretKey);
		String msgCipher = "";
		int key = secretKey;
		for(int i = 0; i < msg.length(); i++){
			key = rand.nextInt(26);
			int cipher = (msg.charAt(i) + key) % 255;
			msgCipher += (char)cipher;
			//System.out.println("Debug Key: " + key);
		}
		return msgCipher;
	}
	
	private String decrypt(String cipher) {
		Random rand = new Random((long)secretKey);
		String msgPlain = "";
		for(int i = 0; i < cipher.length(); i++){
			secretKey = rand.nextInt(26);
			int decipher = (cipher.charAt(i) - secretKey) % 255;
			msgPlain += (char)decipher;
			//System.out.println("Debug Key: " + secretKey);
		}
		return msgPlain;
	}
}