package adts;

//import static server.TestHelpers.pause;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import server.Channel;;

/**
 * this class is a generic class that constructs a list of channels
 * (clients) it defines methods to add, remove, and other list stuff as well as
 * the ability to message the entire list.
 */

public abstract class Nodes
{
	// Contains a Map of usernames to the appropriate Channel
	// For the server to use in general
	private Map<String, Channel> Nodes;
	// boolean for if this list is being tested for concurrency
	// this boolean will delay some methods from executing.

	/*
	 * constructor for a list of Nodes - just initializes the mapping of Nodes
	 */
	public Nodes()
	{
		Nodes = new HashMap<String, Channel>();
	}

	public Nodes(boolean testing)
	{
		Nodes = new HashMap<String, Channel>();
	}

	/*
	 * adds a user to this list (channel) if the user already exists,
	 * throw an exception if any change is made to the list, inform everyone on
	 * the list of the change.
	 * 
	 * @param channel - the client to be added
	 * 
	 * @throws IOException - if the user already exists in this list
	 */
	public void add(Channel channel) throws IOException
	{
		synchronized (Nodes)
		{
//			pause(1000, testing);
			if (this.contains(channel.getUserName()))
				throw new IOException("Username Already Exists");
			Nodes.put(channel.getUserName(), channel);
			informAll(getList());
			return;
		}
	}

	/*
	 * removes a channel from this list and informs everone
	 * 
	 * @param channel - user to remove
	 */
	public void remove(Channel channel)
	{
		synchronized (Nodes)
		{
			Nodes.remove(channel.getUserName());
			informAll(getList());
//			pause(1000, testing);
			return;
		}
	}

	/*
	 * returns true if the user is in this list
	 * 
	 * @param String - username in question
	 * 
	 * @return boolean - if the user is in this list
	 */
	public boolean contains(String userName)
	{
		return Nodes.containsKey(userName);
	}

	/*
	 * returns a string representation of everyone in this list
	 * 
	 * @return String - list of all Nodes in list
	 */
	protected String getList()
	{
		if (size() <= 0)
			return "";
		StringBuilder output = new StringBuilder("");
		ArrayList<String> copy = new ArrayList<String>();
		for (String NodesString : Nodes.keySet())
		{
			String s = new String(NodesString);
			copy.add(s);
		}
		Collections.sort(copy);
		for (String s : copy)
			output.append(s + " ");
		return output.substring(0, output.length() - 1);
	}

	/*
	 * accessor for how big the list is
	 * 
	 * @return int - size of list
	 */
	public int size()
	{
		return Nodes.size();
	}

	/*
	 * method to inform everyone on this list with a message creates a copy of
	 * the Nodes to prevent concurrency/locking
	 * 
	 * @param String - message to be sent to everyone
	 */
	public void informAll(String message)
	{
		Channel[] NodesCopy;
		// make a copy of the list to work with, this way it frees up the lock
		// sooner
		synchronized (Nodes)
		{
			NodesCopy = Nodes.values().toArray(new Channel[0]);
		}
		// send the message to every channel
		for (Channel node : NodesCopy)
			node.updateBuffer(message);
	}

	public Map<String, Channel> getMap()
	{
		return this.Nodes;
	}
}
