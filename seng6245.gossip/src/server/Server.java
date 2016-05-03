package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import adts.*;

/**
 * @author thetrick
 * Gossip server listens and accept channels from clients over a socket connection.
 */
public class Server
{
	private final ServerSocket _serverSocket;
	private final Hive _hive;
	private final ServerNodes _serverNodes;

	/*
	 * Constructor
	 * Binds a server to a port on the local address
	 * 
	 * @param int - port 0 <= int <= 65535
	 * @throws IOException - if socket cannot be bound to port
	 */
	public Server(int port) throws IOException
	{
		this._serverSocket = new ServerSocket(port);
		this._serverNodes = new ServerNodes();
		this._hive = new Hive(this._serverNodes);
	}

	/*
	 * Orchestrates the server listening activities which includes spawning
	 * new threads when valid channel requests are initiated
	 */
	public void serve()
	{
		while (true)
			try
			{
				System.out.println("Server waiting for clients...");
				// accepts a new socket channel
				final Socket socket = this._serverSocket.accept();
				// create a new thread to create a channel (so the server is
				// free to accept another channel)
				new Thread()
				{
					public void run()
					{
						try
						{
							// Create a new channel
							System.out.println("Creating Channel...");
							Channel channel = new Channel(socket, _hive, _serverNodes);
							
							// add the channel to the hive
							System.out.println("Adding Channel for " + channel.getUserName());
							_serverNodes.add(channel);
							_hive.updateChannel(channel);
							
							// start the channel thread
							System.out.println("Starting Channel for " + channel.getUserName());
							new Thread(channel).start();
						}
						catch (Exception ex)
						{
							try
							{
								// inform the client of the issue
								new PrintWriter(socket.getOutputStream(), true).println(ex.getMessage());
								System.out.println("Error: could not run channel ~ " + ex.getMessage());
								// close the socket
								socket.close();
							}
							catch (IOException iox)
							{
								System.out.println("Something bad happened!?");
							}
						}
					}
				}.start(); // start the thread to create channels

			}
			catch (IOException iox)
			{
				System.out.println("Something really bad happened!?");
				break;
			}
	}

	/*
	 * returns the serverSocket instance - for teting only
	 * 
	 * @return SeverSocket - the socket that everytihng is connected to
	 */
	public ServerSocket getServerSocket()
	{
		return this._serverSocket;
	}
	
	/*
	 * returns the list of all _serverNodes - for testing only
	 * 
	 * @return ServerNodes - list of all the _serverNodes
	 */
	public ServerNodes getServerNodes()
	{
		return this._serverNodes;
	}
	
	/*
	 * returns the ist o fall the _hive - for testing only
	 * 
	 * @return
	 * _hive - list of all the _hive
	 */
	public Hive getHive()
	{
		return this._hive;
	}
	
	/*
	 * determines if a string is an integer
	 */
	private static boolean isInteger(String s) 
	{
	    try 
	    { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) 
	    { 
	        return false; 
	    }
	    return true;
	}

	/**
	 * Start a chat server.
	 */
	public static void main(String[] args) throws IOException
	{
		System.out.println();
		int port = -1;
		if(args.length == 0)
			port = 25252;
		else if (args.length == 2 &&  args[0].equals("-p") && isInteger(args[1]))
			port = Integer.parseInt(args[1]);
		else
			System.out.println("USAGE: [-p PORT]");
		
		if(port >= 0 && port <= 65535)
		{
			System.out.println("Starting Server on port " + port);
			Server server = new Server(port);
			server.serve();
		}
		else
			System.out.println("   PORT should be betweeen [0, 65535]");
		return;
	}
}
