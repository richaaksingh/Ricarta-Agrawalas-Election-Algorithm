import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

import javax.swing.text.html.HTMLDocument.Iterator;
import javax.xml.soap.Node;

/**
 * File: RA_Mutual_Exclusion_Client.java
 * 
 * This class has  uses muticast group id to join different clients and creates 
 * thread for them
 * 
 * @author Richa Singh
 *
 */

public class RA_Mutual_Exclusion_Client {

	// Initializing all the variables
	final static String INET_ADDR = "224.0.0.4";
	final static int PORT = 7777;
	private InetAddress group;
	InetAddress ipAddress;
	public int processCount = 0;
	private MulticastSocket clientSocket;
	public String status = "Release";
	long timeNow;
	DatagramSocket socket = null;
	//List<DatagramPacket> myQueue = new LinkedList<DatagramPacket>();
	List<InetAddress> addQ = new LinkedList<InetAddress>();
	public String msg;

	boolean setFlag = false;
	//final static String fileName = "C:\\desktop\\input.txt";
	Boolean criticalSection = false;

	/**
	 * Initializing all the variables
	 */
	public RA_Mutual_Exclusion_Client() throws IOException{
		// Get the address that we are going to connect to.
		group = InetAddress.getByName(INET_ADDR);
		// Create a buffer of bytes, which will be used to store
		// the incoming bytes containing the information from the server.
		// Since the message is small here, 256 bytes should be enough.
		ipAddress = InetAddress.getLocalHost();
		byte[] buf = new byte[256];
		// Create a new Multicast socket (that will allow other sockets/programs
		// to join it as well.
		try (MulticastSocket clientSocket = new MulticastSocket(PORT)){
			//Joint the Multicast group.
			clientSocket.joinGroup(group);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.out.println("I m joining");
		this.join();
		Thread process = new Thread(new RA_Mutual_Exclusion_Server(this));
		process.start();


	}
	/**
	 * retruns null and has option as user input for CS
	 */
	public void console() throws IOException{
		//System.out.println(" entering critical region");
		int ch;
		Scanner sc = new Scanner(System.in);
		while(true){
			try{
				System.out.println(" 1: requestingCriticalSection ");
				ch  = sc.nextInt();
				switch(ch){
				case 1:
					//System.out.println(" lalalala ");
					this.requestingCriticalSection();
					break;
				}
			}
			catch(Exception e){

			}
		}

	}

	/**
	 * retruns boolean and has logic for clients joining multicast group
	 */
	public boolean join() throws IOException{
		try{
			clientSocket = new MulticastSocket(PORT);
			socket = new DatagramSocket(4040);
			msg = "dummypacket";
			DatagramPacket sendPacket = new DatagramPacket(msg.getBytes(),msg.getBytes().length, group , PORT);
			socket.send(sendPacket);
			socket.setSoTimeout(5000);
			byte[] buf = new byte[256];
			DatagramPacket  receievePacket = new DatagramPacket(buf, buf.length);
			socket.receive(receievePacket);
			String receiveMsg = new String(buf,0,buf.length);
			System.out.println(" received and i m rest " + receiveMsg);
			setFlag = false;
			String msgrecieve[] = receiveMsg.split(" ");
			int count = Integer.parseInt(msgrecieve[1].trim());
			//System.out.println("num" + count);
			processCount = count + 1;
			System.out.println(" processNum "+ " " + processCount);
			//			return setFlag;
		}
		catch(SocketTimeoutException e){
			if(socket != null)
				socket.close();
			setFlag = true ;
			//System.out.println(" flag "+ " " + setFlag);
			processCount = processCount + 1;
			System.out.println(" processNum "+ " " + processCount);
			System.out.println(" i m first ");	
		}
		//this.console();
		socket.close();
		return setFlag;

	}
	/**
	 * retruns null and has logic for entering CS
	 */
	public void criticalSection() throws IOException{
		this.status = "Held";
		System.out.println(" Entered in critical section ");

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*String line = null;
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		while((line = bufferedReader.readLine()) != null) {
			System.out.println(line);
		}
		bufferedReader.close();
		 */
		this.leaveCriticalSection();


	}

	/**
	 * retruns null and has logic for requesting CS
	 */
	public void requestingCriticalSection() throws IOException{
		this.status = "Wanted";
		System.out.println("my status" +" "+ this.status );
		timeNow = System.currentTimeMillis();
		socket = new DatagramSocket(4040);
		//InetAddress ipAddress = InetAddress.getLocalHost();
		msg = "Request" + " " + ipAddress + " " + timeNow ;
		System.out.println(" before sending request "+ msg);
		DatagramPacket sendPacket = new DatagramPacket(msg.getBytes(),msg.getBytes().length, group , PORT);
		System.out.println(" packet sent ");
		socket.send(sendPacket);
		byte[] buf = new byte[256];
		for(int i = 0; i < processCount-1; i++){
			DatagramPacket  receievePacket = new DatagramPacket(buf, buf.length);
			socket.receive(receievePacket);
			System.out.println("packet received");

		}

		System.out.println("recevied all the reply");
		this.criticalSection();
		System.out.println("DONE WITH EVERYTHING");
	}
	/**
	 * returns null and has logic for  leaving CS
	 */
	public void leaveCriticalSection(){
		if(!(this.addQ.isEmpty())){	
			for(int i = 0; i < this.addQ.size(); i++){
				System.out.println("leaving critical section and sending reply ");
				//System.out.println("lalalalla");
				//System.out.println("Really important");
				System.out.println("Address: " + addQ.get(0).toString());
				msg = " ok ";
				DatagramPacket sendPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, addQ.get(i), 4040);
				try {
					socket.send(sendPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			socket.close();
		}

		System.out.println(" leaving critical region ");
		this.status = "Release";
		this.addQ.clear();
		System.out.println(" My status is " + this.status);

	}


	/**
	 * This is the main method for program
	 */
	public static void main(String[] args) throws IOException {
		RA_Mutual_Exclusion_Client ob = new RA_Mutual_Exclusion_Client();
		ob.console();


	}
}





