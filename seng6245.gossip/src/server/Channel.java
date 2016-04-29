package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adts.*;

/**
 * This class represents a connection to the server and handles communicating
 * with a single client
 * 
 */
public class Channel implements Runnable
{
	private final String _userName;
	private final Socket _socket;
	private final Hive _hive; // List of _hive in server
	private final ServerNodes serverNodes; // List of serverNodes in server
	private final HashMap<String, Quorum> _quorums = new HashMap<String, Quorum>();
	private final BufferedReader _bufferedReader;
	private final PrintWriter _printWriter;
	private final LinkedBlockingQueue<String> _buffer = new LinkedBlockingQueue<String>();
	private final Thread _thread;
	private boolean _isAlive = true;

	/*
	 * Constructor for connection
	 * 
	 * @param _socket - connection to client Hive - reference to master list
	 * of all _hive ServerNodes - reference to master list of all clients
	 * 
	 * @throws IOException - if the _socket is somehow closed during this process
	 */
	public Channel(Socket _socket, Hive hive, ServerNodes serverNodes) throws IOException
	{
		// fill in fields
		this._socket = _socket;
		this._hive = hive;
		this.serverNodes = serverNodes;
		this._bufferedReader = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
		this._printWriter = new PrintWriter(_socket.getOutputStream(), true);
		// prompt for _userName
		_printWriter.println("To connect type: \"connect [_userName]\"");
		_printWriter.flush();

		// parse _userName
		String input = _bufferedReader.readLine();
		if (input == null)
			throw new IOException("input was null");
		Pattern p = Pattern.compile("connect \\p{Graph}+");
		Matcher m = p.matcher(input);
		// if the _userName is invalid, disconnect the client
		if (!m.matches())
			throw new IOException("Client input not in the format 'connect [_userName]'");
		this._userName = input.substring(input.indexOf(' ') + 1);

		// thread object that will monitor and consume the output buffer and
		// send it to the client
		_thread = new Thread()
		{
			public void run()
			{
				System.out.println("Client: " + _userName + " - " + "Started Output Thread");
				// keep looping this thread until the connection dies
				while (_isAlive)
					try
					{
						// send data to client
						parseOutput(_buffer.take());
					}
					catch (InterruptedException e)
					{
						System.out.println("Client: " + _userName + " - " + "Stopping Output Thread");
						break;
					}
			}
		};
	}

	/*
	 * Constructor to test other stuff with Creates a completely empty
	 * Connection handler to test other classes with
	 * 
	 * @param String - _userName for this Channel
	 */
	public Channel(String _userName)
	{
		this._userName = _userName;
		this._socket = null;
		this._hive = null;
		this.serverNodes = null;
		this._bufferedReader = null;
		this._printWriter = null;
		this._thread = null;
	}

	/*
	 * Starts the main thread for this connection
	 */
	public void run()
	{
		// echo to client that you're connected
		_printWriter.println("Connected");

		try
		{
			// start the output consumer thread to relay data back to user
			_thread.start();

			System.out.println("Client: " + _userName + " - " + "Starting Input Thread");

			// main loop for parsing responses from client
			for (String line = _bufferedReader.readLine(); (line != null && _isAlive); line = _bufferedReader.readLine())
			{
				// _printWriter.println();
				// parse the input
				String parsedInput = parseInput(line);
				// echo the parsed output to the client
				updateBuffer(parsedInput);
				// if the client is not _isAlive anymore, shutdown
				if (!_isAlive)
				{
					System.out.println("Client: " + _userName + " - " + "Stopping Input Thread");
					break;
				}
			}
			System.out.println("Client: " + _userName + " - " + "Input Thread Stopped");

		}
		catch (IOException e)
		{
			System.out.println("Client: " + _userName + " - Connection Lost");
		}
		finally
		{
			// stop the output thread
			_thread.interrupt();
			System.out.println("Client: " + _userName + " - " + "Output Thread Stopped");
			// remove client from all chat _hive and room listings
			removeAllConnections();
			// close the _socket
			try
			{
				_socket.close();
			}
			catch (IOException ignore)
			{
			}
			System.out.println("Client: " + _userName + " - " + "Cleanup Complete");
		}
	}

	/*
	 * parses the input string and performs the appropriate action such as
	 * joining a room or saying a message
	 * 
	 * @param String - the string to be parsed
	 */
	private String parseInput(String input)
	{
		// sets up regex
		String regex = "(((disconnect)|(make)|(join)|(exit)) " + "\\p{Graph}+)|" + "(message \\p{Graph}+ \\p{Print}+)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(input);

		// if there is no match for the input string
		if (!m.matches())
			return "Unrecognized Command " + input; // Should not occur assuming
													// client input is correct

		// find the first space in the string
		int spaceIndex = input.indexOf(' ');
		String command = input.substring(0, spaceIndex);

		// if the string contains "disconnect", set _isAlive to false and kill
		// connection
		if (command.trim().equals("disconnect"))
		{
			// removeAllConnections();
			this._isAlive = false;
			return "disconnectedServerSent";
		}
		// if the command is to make, join or exit (it is a room command)
		else if (command.equals("make") || command.equals("join") || command.equals("exit"))
		{

			// find the next word in string and parse it as the room name
			String quorumName = input.substring(spaceIndex + 1);

			// if making a new room
			if (command.equals("make"))
				try
				{
					// make a new room
					Quorum newQuorum = new Quorum(quorumName, _hive, this);
					// Constructor above automatically adds the Quorum to the
					// list of chat _hive of the server
					_quorums.put(newQuorum.name, newQuorum);
					inform_quorums();
					return "";
				}
				catch (IOException e)
				{
					// if we cant make a room there will be an error message
					return "invalidRoom " + quorumName + " " + e.getMessage();
				}

			// if joining a new room
			else if (command.equals("join"))
			{
				// if there exists the room
				if (_hive.contains(quorumName))
					try
					{
						// try to join the room - what could happen is the room
						// could dissapear at this stage, expect an IOException
						Quorum roomToJoin = _hive.getQuorumByName(quorumName);
						roomToJoin.addUser(this);
						this._quorums.put(roomToJoin.name, roomToJoin);
						return "";
						// if something bad happened when joining a room
					}
					catch (IOException e)
					{
						return "invalidRoom " + quorumName + " " + e.getMessage();
					}
				else
					return "invalidRoom " + quorumName + " Room name does not exist";

				// stuff for exiting a room
			}
			else if (command.equals("exit"))
			{
				// remove the room from personal listings
				Quorum roomToExit = _quorums.remove(quorumName);
				if (roomToExit != null)
				{
					// remove the user from the room
					roomToExit.removeUser(this);
					return "disconnectedRoom " + quorumName;
				}
				return "invalidRoom " + quorumName + " user not connected to room";
			}

			// stuff for messaging a specific room
		}
		else if (command.equals("message"))
		{
			// splice out the target Quorum and message
			int secondSpaceIndex = input.indexOf(' ', spaceIndex + 1);
			String Quorum = input.substring(spaceIndex + 1, secondSpaceIndex);
			String message = input.substring(secondSpaceIndex + 1);

			// update the queue of the Quorum
			Quorum roomToMessage = _quorums.get(Quorum);
			if (roomToMessage != null)
			{
				roomToMessage.updateBuffer(_userName + " " + message);
				return "";
			}
			return "";
		}

		return "Unrecongnized Command " + input;
	}

	// method to build a string containing all of the connected _hive of the
	// user
	private String inform_quorums()
	{
		StringBuilder output = new StringBuilder("_quorums");
		for (String room : _quorums.keySet())
			output.append(room + " ");
		return (output.substring(0, output.length() - 1));
	}

	/*
	 * method to post process string before sending to the client removes any
	 * emtpy strings
	 * 
	 * @param String - the raw output string
	 */
	private void parseOutput(String input)
	{
		if (input.equals(""))
			return;
		System.out.println("Client: " + _userName + " - seinding - " + input);
		_printWriter.println(input);
		_printWriter.flush();
		return;
	}

	/*
	 * method to remove this connection from everything called when the user
	 * leaves the server
	 */
	private void removeAllConnections()
	{
		System.out.println("Client: " + _userName + " - " + "Removing from all connected _hive");

		// removes the user from all connected Quorums
		for (Quorum c : _quorums.values())
			c.removeUser(this);

		// removes the user from the server
		System.out.println("Client: " + _userName + " - " + "Removing from server listing");
		serverNodes.remove(this);
		return;
	}

	/*
	 * method for other things to send messages to this client (like Quorums)
	 * adds the string to a buffer to be consumed when ready. this frees the
	 * sender to do other things and not wait for a slow connection.
	 * 
	 * @param String - message to be sent to the client
	 */
	public void updateBuffer(String msg)
	{
		_buffer.add(msg);
	}

	/*
	 * accessor method to get the output buffer Should not be used for anything
	 * other than testing this class and classes that use this class.
	 * 
	 * @return LinkedBlockingQueue<String> - the queue object of this class.
	 */
	public LinkedBlockingQueue<String> getBuffer()
	{
		return this._buffer;
	}

	/*
	 * accessor method to get the list of connected _hive Should not be used for anything
	 * other than testing this class and classes that use this class.
	 * 
	 * @return Map<String, Quorum> - the map of connected _hive
	 */
	public HashMap<String, Quorum> getQuorums()
	{
		return this._quorums;
	}

	/*
	 * accessor method to get the thread of the consumer Should not be used for anything
	 * other than testing this class and classes that use this class.
	 * 
	 * @return Thread - the queue object of this class.
	 */
	public Thread getThread()
	{
		return this._thread;
	}
	
	public String getUserName()
	{
		return this._userName;
	}
}

