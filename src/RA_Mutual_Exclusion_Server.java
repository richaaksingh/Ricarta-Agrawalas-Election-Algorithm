import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.soap.Node;
/**
 * File: RA_Mutual_Exclusion_Server.java
 * 
 * This class has  uses logic of clients joining multicast group
 * 
 * @author Richa Singh
 *
 */

public class RA_Mutual_Exclusion_Server implements Runnable {

	// Initializing all the variables
	final static String INET_ADDR = "224.0.0.4";
	final static int PORT = 7777;
	private RA_Mutual_Exclusion_Client peer;
	private InetAddress ipAddress;
	DatagramSocket socket;
	private InetAddress group;
	DatagramPacket  receievePacket;

	/**
	 * Initializing all the variables
	 */
	public RA_Mutual_Exclusion_Server (RA_Mutual_Exclusion_Client peer){
		this.peer = peer;
		try {
			this.ipAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	/**
	 * retruns null and has run logic where thread starts
	 */
	public void run() {
		MulticastSocket clientSocket;
		try{
			group = InetAddress.getByName(INET_ADDR);
			clientSocket = new MulticastSocket(PORT);
			//DatagramSocket
			clientSocket.joinGroup(group);
			//System.out.println(" Listening thread joined ");
			byte[] buf = new byte[256];
			receievePacket = new DatagramPacket(buf, buf.length);
			socket = new DatagramSocket(4565);
			while (!Thread.interrupted()){
				try{
					System.out.println(this.ipAddress + " "+ "am ready to receive ");
					clientSocket.receive(receievePacket);
					System.out.println(this.ipAddress + " "+ " received ");
					//String receiveMsg = new String(buf,0,receievePacket.getLength());
					parse(receievePacket);
					//System.out.println("receive msg " + receiveMsg);
					//if(receiveMsg.equals("dummypacket")){
					//System.out.println(" i m here " + peer.setFlag);
					//	if(peer.setFlag ==true){
					//String msg = " ok ";
					//DatagramPacket sendPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, receievePacket.getAddress() , receievePacket.getPort());
					//socket.send(sendPacket);	
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * retruns null and reads the packet message
	 */
	public void parse(DatagramPacket receievePacket) throws IOException{
		String msg;
		byte[] buf = new byte[256];
		//socket = new DatagramSocket(9179);
		System.out.println(receievePacket.getLength());
		String receiveMsg = new String(receievePacket.getData(),0,receievePacket.getLength());
		System.out.println("receive msg " + receiveMsg);

		String msgrecieve[] = receiveMsg.split(" ");
		//String ipthread = msgrecieve[1];
		System.out.println("receive msg 1 " + msgrecieve[0]);

		// if the packet is initial packet
		if(msgrecieve[0].equals("dummypacket")){
			System.out.println(" i m here " + peer.setFlag);
			if(peer.setFlag == true){
				//System.out.println("count"+ peer.processCount);
				msg = "ok " + peer.processCount;
				DatagramPacket sendPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, receievePacket.getAddress() , receievePacket.getPort());
				socket.send(sendPacket);
				peer.processCount++;
				System.out.println("process count "+ peer.processCount);

			}
			else if(peer.setFlag == false){
				peer.processCount++;
				System.out.println("process count "+ peer.processCount);
			}
		}
		//if incoming message is in request for CS
		else if(msgrecieve[0].equals("Request")){
			System.out.println("In request : " + msgrecieve[1] );
			if(!(peer.ipAddress.toString().equals(msgrecieve[1]))){
				if(peer.status == "Held"){
					System.out.println("adding packet to my queue");
					//peer.myQueue.add(receievePacket);
					peer.addQ.add(receievePacket.getAddress());
					System.out.println("size of queue"+ " " + peer.addQ.size());
					//System.out.println("element is" + " "+ peer.addQ.get(0).getPort());
				}
				else if(peer.status == "Wanted"){
					if(Long.parseLong(msgrecieve[2].toString().trim()) > peer.timeNow){
						peer.addQ.add(receievePacket.getAddress());
						System.out.println("size of queue"+ " " + peer.addQ.size());
						//System.out.println("element is" + " "+ peer.addQ.get(0));
					}

					else{
						//System.out.println("sending reply because i m in wanted status ");
						msg = " ok ";
						DatagramPacket sendPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, receievePacket.getAddress() , receievePacket.getPort());
						socket.send(sendPacket);
					}



				}
				else if(peer.status == "Release"){
					System.out.println("sending reply because i m in release status ");
					msg = " ok ";
					DatagramPacket sendPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, receievePacket.getAddress() , receievePacket.getPort());
					socket.send(sendPacket);
					// peer.status = " Held ";

				}
			}

		}
	}
}




