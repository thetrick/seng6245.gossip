package adts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import server.Channel;

/**
 * This class implements a list that holds a number of chat _quorums this class
 * will define methods for adding and removing _quorums from the server
 */
public class Hive
{
	private ServerNodes _serverNodes;
	private final Map<String, Quorum> _quorums;

	/*
	 * constructor for this list just initializes the fields
	 * 
	 * @param ServerNodes - master list of all of the people on the server
	 * (used to inform everyone on the server)
	 */
	public Hive(ServerNodes _serverNodes)
	{
		this._serverNodes = _serverNodes;
		this._quorums = new HashMap<String, Quorum>();
	}

	/*
	 * Constructor for testing enables pauses that will be used for concurrency
	 * testing only
	 * 
	 * @param ServerNodes -master list of all the people on the server
	 * boolean - enable pauses
	 */
	public Hive(ServerNodes _serverNodes, boolean testing)
	{
		this._serverNodes = _serverNodes;
		this._quorums = new HashMap<String, Quorum>();
	}

	/*
	 * method to add a Quorum to this list inform everyone of the change
	 * 
	 * @param Quorum - quorum to add
	 * 
	 * @throws IOException - if the quorum already exists in this list
	 */
	public synchronized void add(Quorum quorum) throws IOException
	{
		// throw an ioException if the quorum exisits
		if (this.contains(quorum.name))
			throw new IOException("quorum already exists");
		// put the quorum in the list
		_quorums.put(quorum.name, quorum);
		// inform everyone of the new _quorums
		_serverNodes.informAll(getQuorums());
		return;
	}

	/*
	 * method to remove a Quorum from this list informs everyone of the change
	 * 
	 * @param Quorum - quorum to remove
	 */
	public synchronized void remove(Quorum quorum)
	{
		_quorums.remove(quorum.name);
		// inform everyone of change
		_serverNodes.informAll(getQuorums());
		return;
	}

	/*
	 * returns if a quorum name is in the list
	 * 
	 * @param String - name of quorum
	 * 
	 * @return boolean - if the quorum exists in this list
	 */
	public synchronized boolean contains(String name)
	{
		return _quorums.containsKey(name);
	}

	/*
	 * method for getting a Quorum object by name
	 * 
	 * @param String - name of quorum to get
	 * 
	 * @return Quorum - quorum that matches this name (null if no quorum)
	 */
	public Quorum getQuorumByName(String quorumName)
	{
		return _quorums.get(quorumName);
	}

	/*
	 * returns a string representation of all the _quorums in this list
	 * 
	 * @return String - string list of all the names of the _quorums in this list
	 */
	private String getQuorums()
	{
		StringBuilder Hive = new StringBuilder("serverHive ");
		ArrayList<String> copy = new ArrayList<String>();
		for (String _quorumsString : _quorums.keySet())
			copy.add(new String(_quorumsString));
		Collections.sort(copy);
		for (String s : copy)
			Hive.append(s + ' ');
		Hive.deleteCharAt(Hive.length() - 1);
		return Hive.toString();
	}

	/*
	 * method to update a specific user of all the _quorums in this list
	 * 
	 * @param Channel - user to be updated
	 */
	public synchronized void updateUser(Channel user)
	{
		user.updateBuffer(getQuorums());
	}

	/*
	 * method to return the map of all the _quorums only used for testing
	 * 
	 * @return Map<String, Quorum> - map that holds all of the _quorums
	 */
	public Map<String, Quorum> getMap()
	{
		return this._quorums;
	}
}
