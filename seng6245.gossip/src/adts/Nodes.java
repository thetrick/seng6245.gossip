package adts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import server.Channel;
import tests.Utility;;

/**
 * base class used to construct a list of channels
 * handles the plumbing of orchestrating activities associated with the channels
 */

public abstract class Nodes
{
	// Maps the users to channels
	private Map<String, Channel> Nodes;
	private boolean isTest = false;
	
	/*
	 * Constructor
	 */
	public Nodes()
	{
		this(false);
	}

	public Nodes(boolean isTest)
	{
		// Initializes the mapping of Nodes
		this.Nodes = new HashMap<String, Channel>();
		this.isTest = isTest;
	}
	
	/*
	 * adds a channel to the list of nodes
	 * throw an exception if the channel's user already exists within another channel
	 * notifies all channels of the action
	 * 
	 * @param channel - the channel to be added
	 * 
	 * @throws IOException - if the user associated with the channel already exists throw error
	 */
	public void add(Channel channel) throws IOException
	{
		synchronized (Nodes)
		{
			if(this.isTest)
				Utility.pause(1000);
			if (this.contains(channel.getUserName()))
				throw new IOException("The user associated with this channel already exists.");
			Nodes.put(channel.getUserName(), channel);
			notifyChannels(getList());
			return;
		}
	}

	/*
	 * removes a channel and inform the group
	 * 
	 * @param channel - The channel that needs to be removed
	 */
	public void remove(Channel channel)
	{
		synchronized (Nodes)
		{
			Nodes.remove(channel.getUserName());
			notifyChannels(getList());
			if(this.isTest)
				Utility.pause(1000);
			return;
		}
	}

	/*
	 * returns true if the user is already registered with another channel
	 * 
	 * @param String - userName to check
	 * 
	 * @return boolean - true if userName already exists
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
	public String getList()
	{
		if (this.size() <= 0)
			return "";
		
		StringBuilder stringBuilder = new StringBuilder("");
		ArrayList<String> users = new ArrayList<String>();
		for (String NodesString : Nodes.keySet())
		{
			String s = new String(NodesString);
			users.add(s);
		}
		Collections.sort(users);
		for (String user : users)
			stringBuilder.append(user + " ");
		return stringBuilder.substring(0, stringBuilder.length() - 1);
	}

	/*
	 * Identifies the size of the channel list
	 * 
	 * @return int - size of the channel list
	 */
	public int size()
	{
		return Nodes.size();
	}

	/*
	 * provides a mechanism to send all channels a message
	 *  
	 * @param String - the message we want to send to everyone
	 */
	public void notifyChannels(String message)
	{
		// need a temporary array of channels to store a copy
		Channel[] channelsCopy;
		// populate the temporary array with a copy of the channels
		synchronized (Nodes)
		{
			channelsCopy = Nodes.values().toArray(new Channel[0]);
		}
		
		// notify all the channels by sending it a message
		for (Channel node : channelsCopy)
			node.updateBuffer(message);
	}

	/*
	 * Return the underlying Map containing users to channels
	 * @return the Map between users and channels
	 */
	public Map<String, Channel> getNodesMap()
	{
		return this.Nodes;
	}
}
