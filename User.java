
//package broadcast;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class User extends Thread {

	// The user socket
	private static Socket userSocket = null;
	// The output stream
	private static PrintStream output_stream = null;
	// The input stream
	private static BufferedReader input_stream = null;

	private static BufferedReader inputLine = null;
	private static boolean closed = false;


	public static void main(String[] args) {

		// The default port.
		int portNumber = 8000;
		// The default host.
		String host = "localhost";

		if (args.length < 2) {
			System.out
			.println("Usage: java User <host> <portNumber>\n"
					+ "Now using host=" + host + ", portNumber=" + portNumber);
		} else {
			host = args[0];
			portNumber = Integer.valueOf(args[1]).intValue();
		}

		/*
		 * Open a socket on a given host and port. Open input and output streams.
		 */
		try {
			userSocket = new Socket(host, portNumber);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			output_stream = new PrintStream(userSocket.getOutputStream());
			input_stream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + host);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Couldn't get I/O for the connection to the host "
					+ host);
		}

		/*
		 * If everything has been initialized then we want to write some data to the
		 * socket we have opened a connection to on port portNumber.
		 */
		if (userSocket != null && output_stream != null && input_stream != null) {
			try {                
				/* Create a thread to read from the server. */
				new Thread(new User()).start();

				// Get user name and join the social net
				
				System.out.println("To join enter a username: ");
				String userInput = inputLine.readLine().trim();
				String join = "#join " + userInput;  //adding #join for the first input
				output_stream.println(join);
				
				
				while (!closed) { //this will catch all the @ commands and see other inputs as statuses
					String userMessage = new String();
					userInput = inputLine.readLine().trim();
					String info[] = new String[2]; 
					
					if(userInput.equals("Exit")) {
						output_stream.println("#Bye");
						break;
					}
					else if(userInput.startsWith("@connect")) {
						info = userInput.split("\\s", 2); //splicing the input between each white spaces 
						output_stream.println("#friendme " + info[1] );
					}
					else if(userInput.startsWith("@friend")) {
						info = userInput.split("\\s", 2);
						output_stream.println("#friends " + info[1]);

					}
					else if(userInput.startsWith("@deny")) {
						info = userInput.split("\\s", 2);

						output_stream.println("#DenyFriendRequest " + info[1]);

					}
					else if(userInput.startsWith("@disconnect")) {
						info = userInput.split("\\s", 2);
						output_stream.println("#unfriend "+ info[1]);
					}
					else {
						String userStatus = "#status " + userInput;
						output_stream.println(userStatus);
					}
					
					
				
					
					// Read user input and send protocol message to server

				}
				/*
				 * Close the output stream, close the input stream, close the socket.
				 */
			} catch (IOException e) {
				System.err.println("IOException:  " + e);
			}
		}
	}

	/*
	 * Create a thread to read from the server.
	 */
	public void run() {
		/*
		 * Keep on reading from the socket till we receive a Bye from the
		 * server. Once we received that then we want to break.
		 */
		String responseLine;
		String info[] = new String[2];
		
		try {
			while ((responseLine = input_stream.readLine()) != null) { //catches all the protocals sent by the server

				// Display on console based on what protocol message we get from server.
				if(responseLine.startsWith("#welcome")) {
					info = responseLine.split("\\s", 2);
					System.out.println("Welcome to Bootleg Facebook ");
					System.out.println("To leave type Exit!");
				}
				if(responseLine.startsWith("#busy")) {
					System.out.println("Currently too many users! Try again later!");
				}
				if(responseLine.startsWith("#statusPosted")) {
					System.out.println("Your status has been posted successfully!");
				}
				if(responseLine.startsWith("#newuser")) {
					info = responseLine.split("\\s", 2);
					System.out.println("A new user has joined: " + info[1]);
				}
				if(responseLine.startsWith("#newStatus")) {
					info = responseLine.split("\\s", 3);
					System.out.println("New Status from: " + info[1] +" " + info[2]);
					
				}
				if(responseLine.startsWith("#Leave")) {
					info = responseLine.split("\\s", 2);
					System.out.println(info[1] + " is leaving!");
				}
				if(responseLine.startsWith("#friendme")) {
					info = responseLine.split("\\s", 2);
					System.out.println("Type @friend " + info[1] + " to accept or @deny " + info[1]);
				}
				if(responseLine.startsWith("#OKfriends")) {
					info = responseLine.split("\\s", 3);
					System.out.println("Now friends " + info[1] + " " + info[2]);
				}
				if(responseLine.startsWith("#NotFriends")) {
					info = responseLine.split("\\s", 3);
					System.out.println(info[1] + " " + info[2] + " are no longer friends");
					
				}
				if(responseLine.startsWith("#FriendRequestDenied")) {
					info = responseLine.split("\\s", 2);
					System.out.println(info[1] + " has denied your request");
					
				}
				if(responseLine.startsWith("#Bye")) {
					break;
				}


			}
			closed = true;
			output_stream.close();
			input_stream.close();
			userSocket.close();
		} catch (IOException e) {
			System.err.println("IOException:  " + e);
		}
	}
}



