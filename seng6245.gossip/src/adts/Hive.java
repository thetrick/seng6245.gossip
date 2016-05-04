package adts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import server.Channel;

/**
 * The Hive is container class for the list of Quorums that may exist.
 * As such, the class contains the means by which to orchestrate and administer
 * the activities associated the Hive
 */
public class Hive
{
	private ServerNodes _serverNodes;
	private final Map<String, Quorum> _quorums;
	private boolean isTest = false;

	/*
	 * constructor
	 * @param ServerNodes - details all the Nodes that exist on the server
	 */
	public Hive(ServerNodes serverNodes)
	{
		this(serverNodes, false);
	}

	public Hive(ServerNodes serverNodes, boolean isTest)
	{
		this._serverNodes = serverNodes;
		this._quorums = new HashMap<String, Quorum>();		
	}
	
	/*
	 * Add Quorum to the Hive's master list and notify everyone of the change
	 * @param Quorum - quorum to add
	 * @throws IOException - thrown if the Quorum already exists in the master list
	 */
	public synchronized void addQuorum(Quorum quorum) throws IOException
	{
		if (this.contains(quorum.Id))
			throw new IOException("The Quorum already exists in the Hive.");
		
		// store the Quorum
		this._quorums.put(quorum.Id, quorum);
		
		// notify all the nodes
		this._serverNodes.notifyChannels(getQuorums());
		return;
	}

	/*
	 * Removes the Quorum from the Master Hive list
	 * @param Quorum - quorum to remove
	 */
	public synchronized void removeQuorum(Quorum quorum)
	{
		this._quorums.remove(quorum.Id);
		// notify all the nodes
		this._serverNodes.notifyChannels(getQuorums());
		return;
	}

	/*
	 * True if the Quorum exists in the Hive
	 * @param String - name of quorum
	 * @return boolean - indicator if the quorum exists in the hive
	 */
	public synchronized boolean contains(String id)
	{
		return this._quorums.containsKey(id);
	}

	/*
	 * returns the Quorum by id
	 * @param String - id of Quorum
	 * @return Quorum - quorum that matches the id, return null if quorum does not exist
	 */
	public Quorum getQuorumById(String id)
	{
		return _quorums.get(id);
	}

	/*
	 * Returns all quorum names
	 * @return String - The names of the Quorums in the Hive
	 */
	private String getQuorums()
	{
		StringBuilder stringBuilder = new StringBuilder("Hive ");
		ArrayList<String> quorums = new ArrayList<String>();
		for (String quorum : _quorums.keySet())
			quorums.add(quorum);
		Collections.sort(quorums);
		for (String quorum : quorums)
			stringBuilder.append(quorum + " ");
		stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		return stringBuilder.toString();
	}

	/*
	 * Update a specific channel of all the Quorums in the Hive
	 * @param Channel - user to be updated
	 */
	public synchronized void updateChannel(Channel channel)
	{
		channel.updateBuffer(getQuorums());
	}

	/*
	 * Return the underlying Map containing Quorums in the Hive
	 * @return the Map between Quorum Id and Quorum
	 */
	public Map<String, Quorum> getQuorumsMap()
	{
		return this._quorums;
	}
}
