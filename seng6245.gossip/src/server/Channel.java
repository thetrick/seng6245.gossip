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
 * 
 * @author thetrick
 * Defines a particular connection made to a server. Orchestrates the communication
 * with a particular client.
 */
public class Channel implements Runnable
{
	private final String _userName;
	private final Socket _socket;
	private final Hive _hive; 
	private final ServerNodes _serverNodes; 
	private final HashMap<String, Quorum> _quorums = new HashMap<String, Quorum>();
	private final BufferedReader _bufferedReader;
	private final PrintWriter _printWriter;
	private final LinkedBlockingQueue<String> _buffer = new LinkedBlockingQueue<String>();
	private final Thread _thread;
	private boolean _isAlive = true;

	/*
	 * Constructor
	 * 
	 * @param Socket - Represents a connection to a client 
	 * @param Hive - Represents the master collection of Quorums
	 * @Param ServerNodes - reference to master list of all clients
	 * @throws IOException - Thrown if our socket is interrupted or closed
	 */
	public Channel(Socket socket, Hive hive, ServerNodes serverNodes) throws IOException
	{
		// fill in fields
		this._socket = socket;
		this._hive = hive;
		this._serverNodes = serverNodes;
		this._bufferedReader = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
		this._printWriter = new PrintWriter(_socket.getOutputStream(), true);

		// Need the User name
		_printWriter.println("To connect type: \"connect [username]\"");
		_printWriter.flush();

		String input = _bufferedReader.readLine();
		if (input == null)
			throw new IOException("input was null");
		
		Pattern pattern = Pattern.compile("connect \\p{Graph}+");
		Matcher matcher = pattern.matcher(input);
		
		// Check to see if user name entered is valid.
		if (!matcher.matches())
			throw new IOException("Invalid format 'connect [username]'");
		this._userName = input.substring(input.indexOf(' ') + 1);

		// Use thread to consume output send to client
		_thread = new Thread()
		{
			public void run()
			{
				System.out.println("Client: (" + _userName + ") " + "Started...");
				while (_isAlive)
					try
					{
						// push buffer content to the client
						parseOutput(_buffer.take());
					}
					catch (InterruptedException e)
					{
						System.out.println("Client: (" + _userName + ") " + "Stopping...");
						break;
					}
			}
		};
	}

	public Channel(String _userName)
	{
		this._userName = _userName;
		this._socket = null;
		this._hive = null;
		this._serverNodes = null;
		this._bufferedReader = null;
		this._printWriter = null;
		this._thread = null;
	}

	/*
	 * runs the main thread of the connection
	 */
	public void run()
	{
		// echo to client that you're connected
		_printWriter.println("Connected!");

		try
		{
			// start the thread that handles sending data back to the client
			_thread.start();

			System.out.println("Client: (" + _userName + ") " + "Starting...");

			// check the buffer for client responses
			for (String line = _bufferedReader.readLine(); (line != null && _isAlive); line = _bufferedReader.readLine())
			{
				String input = parseInput(line);
				// Send it back to the user
				updateBuffer(input);
				
				// Check if the client is still alive, if not kill
				if (!_isAlive)
				{
					System.out.println("Client: (" + _userName + ") " + "Stopping...");
					break;
				}
			}
			System.out.println("Client: (" + _userName + ") " + "Stopped");

		}
		catch (IOException e)
		{
			System.out.println("Client: (" + _userName + ") Connection Lost");
		}
		finally
		{
			// kill the thread
			_thread.interrupt();
			System.out.println("Client: (" + _userName + ") " + "Stopped");
			// remove client from all quorums
			removeUserConnections();
			// close the _socket
			try
			{
				_socket.close();
			}
			catch (IOException ignore)
			{
			}
			System.out.println("Client: (" + _userName + ") " + "Cleanup Complete");
		}
	}

	/*
	 * parses the input string and performs the appropriate action such as
	 * joining a quorum or saying a message
	 * @param String - the string to be parsed
	 */
	private String parseInput(String input)
	{
		// sets up regex
		String regex = "(((disconnect)|(make)|(join)|(exit)) " + "\\p{Graph}+)|" + "(message \\p{Graph}+ \\p{Print}+)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);

		// if there is no match for the input string
		if (!matcher.matches())
			return "Unrecognized Command: " + input; 

		// find the first space in the string
		int idx = input.indexOf(' ');
		String command = input.substring(0, idx);

		// if the string contains "disconnect", set _isAlive to false and kill
		// connection
		if (command.trim().equals("disconnect"))
		{
			// removeAllConnections();
			this._isAlive = false;
			return "disconnectedFromServer";
		}
		// if the command is to make, join or exit (it is a quorum command)
		else if (command.equals("make") || command.equals("join") || command.equals("exit"))
		{

			// find the next word in string and parse it as the quorum name
			String quorumName = input.substring(idx + 1);

			// if making a new quorum
			if (command.equals("make"))
				try
				{
					// make a new quorum
					Quorum newQuorum = new Quorum(quorumName, _hive, this);
					// Constructor above automatically adds the Quorum to the
					// list of chat _hive of the server
					_quorums.put(newQuorum.Id, newQuorum);
					String note = notifyQuorums();
					return note;
				}
				catch (IOException e)
				{
					return "badQuorum " + quorumName + " " + e.getMessage();
				}

			// if joining a new quorum
			else if (command.equals("join"))
			{
				// if there exists the quorum
				if (_hive.contains(quorumName))
					try
					{
						// try to join the quorum - what could happen is the quorum
						// could disappear at this stage, expect an IOException
						Quorum quorumToJoin = _hive.getQuorumById(quorumName);
						quorumToJoin.addChannel(this);
						this._quorums.put(quorumToJoin.Id, quorumToJoin);
						return "";
					}
					catch (IOException e)
					{
						return "badQuorum " + quorumName + " " + e.getMessage();
					}
				else
				{
					return "badQuorum " + quorumName + " quorum name does not exist";
				}
			}
			else if (command.equals("exit"))
			{
				// remove the quorum from personal listings
				Quorum quorumToExit = _quorums.remove(quorumName);
				if (quorumToExit != null)
				{
					// remove the user from the quorum
					quorumToExit.removeChannel(this);
					return "disconnectedquorum " + quorumName;
				}
				return "badQuorum " + quorumName + " user not connected to quorum";
			}

			// stuff for messaging a specific quorum
		}
		else if (command.equals("message"))
		{
			// splice out the target Quorum and message
			int idx2 = input.indexOf(' ', idx + 1);
			String Quorum = input.substring(idx + 1, idx2);
			String message = input.substring(idx2 + 1);

			// update the queue of the Quorum
			Quorum quorumToMessage = _quorums.get(Quorum);
			if (quorumToMessage != null)
			{
				quorumToMessage.updateBuffer(_userName + " " + message);
				return "";
			}
			return "";
		}

		return "Unrecongnized Command: " + input;
	}

	/**
	 * Builds a list of all connected quorums the user is currently associated with
	 * @return - string description of all quorums as user is associated with
	 */
	private String notifyQuorums()
	{
		StringBuilder stringBuilder = new StringBuilder("List of connected Quorums: ");
		for (String quorum : _quorums.keySet())
			stringBuilder.append(quorum + " ");
		return (stringBuilder.substring(0, stringBuilder.length() - 1));
	}

	/*
	 * decorates input and posts it to the client
	 * @param String - input to be sent to the client
	 */
	private void parseOutput(String input)
	{
		if (input.equals(""))
			return;
		
		System.out.println("Client: (" + _userName + ") sending... " + input);
		_printWriter.println(input);
		_printWriter.flush();
		return;
	}

	/*
	 * Performs a cleanup to make sure that this channel associated with the 
	 * user is removed from all quorums as well as the ServerNodes
	 * when the user disconnects.
	 */
	private void removeUserConnections()
	{
		System.out.println("Client: (" + _userName + ") " + "Removing from all connected Quorums");

		// removes the user from all connected Quorums
		for (Quorum quorum : _quorums.values())
			quorum.removeChannel(this);

		// removes the user from the server
		System.out.println("Client: (" + _userName + ") " + "Removing from server");
		_serverNodes.remove(this);
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

