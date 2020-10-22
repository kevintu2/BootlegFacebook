
//package broadcast;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;



/*
* A server that delivers status messages to other users.
*/
public class Server {

	// Create a socket for the server 
	private static ServerSocket serverSocket = null;
	// Create a socket for the server 
	private static Socket userSocket = null;
	// Maximum number of users 
	private static int maxUsersCount = 5;
	// An array of threads for users
	private static userThread[] threads = null;


	public static void main(String args[]) {

		// The default port number.
		int portNumber = 58000;
		if (args.length < 2) {
			System.out.println("Usage: java Server <portNumber>\n"
					+ "Now using port number=" + portNumber + "\n" +
					"Maximum user count=" + maxUsersCount);
		} else {
			portNumber = Integer.valueOf(args[0]).intValue();
			maxUsersCount = Integer.valueOf(args[1]).intValue();
		}

		System.out.println("Server now using port number=" + portNumber + "\n" + "Maximum user count=" + maxUsersCount);
		
		
		userThread[] threads = new userThread[maxUsersCount];


		/*
		 * Open a server socket on the portNumber (default 8000). 
		 */
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println(e);
		}

		/*
		 * Create a user socket for each connection and pass it to a new user
		 * thread.
		 */
		while (true) {
			try {
				userSocket = serverSocket.accept();
				int i = 0;
				for (i = 0; i < maxUsersCount; i++) {
					if (threads[i] == null) {
						threads[i] = new userThread(userSocket, threads);
						threads[i].start();
						break;
					}
				}
				if (i == maxUsersCount) {
					PrintStream output_stream = new PrintStream(userSocket.getOutputStream());
					output_stream.println("#busy");
					output_stream.close();
					userSocket.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}

/*
* Threads
*/
class userThread extends Thread {

	private String userName = null;
	private BufferedReader input_stream = null;
	private PrintStream output_stream = null;
	private Socket userSocket = null;
	private final userThread[] threads;
	private int maxUsersCount;
	private ArrayList<userThread> friendslist = new ArrayList<userThread>(); //the friends list for each user
	private ArrayList<String> requests = new ArrayList<String>(); //the request list for each user

	public userThread(Socket userSocket, userThread[] threads) {
		this.userSocket = userSocket;
		this.threads = threads;
		maxUsersCount = threads.length;
	}

	public void run() {
		int maxUsersCount = this.maxUsersCount;
		userThread[] threads = this.threads;

		try {
			/*
			 * Create input and output streams for this client.
			 * Read user name.
			 */
			input_stream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
			output_stream = new PrintStream(userSocket.getOutputStream());
			String line = input_stream.readLine();
			String[] name = line.split("\\s", 2); //splicing the string into two strings between the white space
			userName = name[1];
			if(line.startsWith("#join")) {
				output_stream.println("#welcome " + userName);
			
				synchronized (userThread.class) {// synchronized to make the threads execute fully first
					for(int i = 0; i<maxUsersCount; i++) {
						if(threads[i]!= this && threads[i] != null) {

							threads[i].output_stream.println("#newuser " + "<" + userName + ">");

				}
			}
			}
			}
		


			/* Welcome the new user. */


			/* Start the conversation. */
			while (true) {
				
				input_stream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
				output_stream = new PrintStream(userSocket.getOutputStream());
				line = input_stream.readLine().trim();
				
				if(line.startsWith("#Bye")) { //reading the exit protocal from the user
					output_stream.println("#Bye");
					synchronized (userThread.class) {
					for(int i = 0; i<maxUsersCount; i++) {
						if(threads[i]!= this && threads[i] != null) {

							threads[i].output_stream.println("#Leave " + "<" + userName + "> ");


						}
				}
					}
					break; //break out of the loop
				}
				
				if(line.startsWith("#friendme")) { //reading the @connect protocal
					String[]message = line.split("\\s", 2);
					String reciever = message[1]; //the username of the reciever
					synchronized (userThread.class){
						for(int i = 0; i<maxUsersCount; i++) {
							if(threads[i]!= null && threads[i].userName.equals(reciever)) { //iterate until you find the reciever thread
								threads[i].requests.add(this.userName); //adding the friend request to the users request list
								threads[i].output_stream.println("#friendme " + "<" + userName + ">"); //sending the next protocal to the user
						}
					}
					}
					
				}
				if(line.startsWith("#friends")) { //reading the @friend protocal
					String[]message = line.split("\\s", 2);
					String requester = message[1];
					
						synchronized (userThread.class){
						for(int i = 0; i<maxUsersCount; i++) {
							if(threads[i]!= null && threads[i].userName.equals(requester) && this.requests.contains(requester)) {//check to see if the user has a pending request from the requester 
								this.friendslist.add(threads[i]); //adding to each others friends list
								threads[i].friendslist.add(this);
								this.requests.remove(requester); //removing the request because it has been handled
								this.output_stream.println("#OKfriends " + requester + " " +userName); //sending next protocals to the users
								threads[i].output_stream.println("#OKfriends " + userName + " " + requester);
					   }
						}
							} 	   
					
								}
				if(line.startsWith("#DenyFriendRequest")) { //reading @deny protocal
					String[]message = line.split("\\s", 2);
					String requester = message[1];
					synchronized (userThread.class){
						   for(int i = 0; i<maxUsersCount; i++) {
							   if(threads[i]!= null && threads[i].userName.equals(requester) && this.requests.contains(requester)) { //check to see if the user has a request
								   this.requests.remove(requester); //remove because user denied it
								   threads[i].output_stream.println("#FriendRequestDenied " + this.userName ); //send next protocal to user
							   }
					
					
				}

			}
				}
				if(line.startsWith("#unfriend")) {//reading @disconnect protocal
					String[]message = line.split("\\s", 2);
					String reciever = message[1];
					
					synchronized (userThread.class){
						for(int i = 0; i<maxUsersCount; i++) {
							if(threads[i]!= null && threads[i].userName.equals(reciever) && this.friendslist.contains(threads[i])) {//check if the friendslist have the user
								friendslist.remove(threads[i]); //remove friend from both friendslist
								threads[i].friendslist.remove(this);
								threads[i].output_stream.println("#NotFriends " + userName + " " + reciever); //send next protocal to user
								this.output_stream.println("#NotFriends " + userName + " " + reciever);
					   }
				   }
					}
					
				}
				if(line.startsWith("#status")) {
					output_stream.println("#statusPosted");
					String[]message = line.split("\\s", 2);
					synchronized (userThread.class){
						for(int i = 0; i<maxUsersCount; i++) {
							if(threads[i]!= null && this.friendslist.contains(threads[i])) {
								threads[i].output_stream.println("#newStatus " + "<" + userName + "> " + message[1]);


								}
							}
						}
				}
			}

			// conversation ended.

			/*
			 * Clean up. Set the current thread variable to null so that a new user
			 * could be accepted by the server.
			 */
			synchronized (userThread.class) {
				for (int i = 0; i < maxUsersCount; i++) {
					if (threads[i] == this) {
						threads[i] = null;
					}
				}
			}
			/*
			 * Close the output stream, close the input stream, close the socket.
			 */
			input_stream.close();
			output_stream.close();
			userSocket.close();
		}
		catch (IOException e) {
		}
	}
}




