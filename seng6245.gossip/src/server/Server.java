package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import adts.*;

/**
 * This class starts a server to listen and accept channels from clients over
 * a socket channel.
 */
public class Server
{
	private final ServerSocket _serverSocket;
	private final Hive _hive;
	private final ServerNodes _serverNodes;

	/*
	 * Constructor for server binds a server to a port on the local address and
	 * initializes lists
	 * 
	 * @param int - port -- port to bind to (0 <= int <= 65535)
	 * 
	 * @throws IOException - if socket cannot be bound to port
	 */
	public Server(int port) throws IOException
	{
		this._serverSocket = new ServerSocket(port);
		this._serverNodes = new ServerNodes();
		this._hive = new Hive(this._serverNodes);
	}

	/*
	 * Main loop for server listens for a channel then creates a new thread
	 * to construct a handler for that channel as well as updates all the
	 * lists
	 */
	public void serve()
	{
		System.out.println("SERVER INIT");
		// start main loop
		while (true)
			try
			{
				System.out.println("Server waiting");
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
							System.out.println("Creating User");
							// create a new channel handler
							Channel channel = new Channel(socket, _hive, _serverNodes);
							System.out.println("Adding User");
							// attempt to add the channel (client) to the
							// list of connected clients
							_serverNodes.add(channel);
							// update the client with all the available _hive
							_hive.updateUser(channel);
							System.out.println("Starting User");
							// start the channel thread to start IO
							new Thread(channel).start();
							// If there is an error (like the user already
							// exists, or user disconnects at startup sequence)
						}
						catch (Exception e)
						{
							try
							{
								// try to tell the client what happened
								new PrintWriter(socket.getOutputStream(), true).println(e.getMessage());
								System.err.println("Error: could not run user ~ " + e.getMessage());
								// close the channel
								socket.close();
							}
							catch (IOException wtf)
							{
								System.err.println("I dont even know right now...");
							}
						}
					}
				}.start(); // start the thread to create channels

			}
			catch (IOException kill)
			{
				// really... this shouldnt ever be ran... really... unless
				// something FUBAR happened.... like the world dying... or the serverSocket being closed
				System.err.println("Oh noez the server was closed spontaneously!");
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
			port = 10000;
		else if (args.length == 2 &&  args[0].equals("-p") && isInteger(args[1]))
			port = Integer.parseInt(args[1]);
		else
			System.out.println("USAGE: [-p PORT]");
		
		if(port >= 0 && port <= 65535)
		{
			System.out.println("Starting Server on port " + port);
			Server server = new Server(10000);
			server.serve();
		}
		else
			System.out.println("   PORT should be betweeen [0, 65535]");
		return;
	}
}
