package adts;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import server.Channel;

/**
 * The Quorum represents a gathering of channels or individual users who
 * come together to chat or communicate with each other. As long as one channel
 * is associated with the Quorum, then the Quorum will stay alive. Once, the last 
 * channel has left the Quorum, then the Quorum will interrupt its associated thread
 * and remove itself from the Hive.
 */
public class Quorum implements Runnable
{
	public final String Id;
	private final Hive _hive;
	private final ClientNodes _clientNodes;
	private LinkedBlockingQueue<String> _buffer = new LinkedBlockingQueue<String>();
	private Thread _thread;
	private boolean _alive = true;

	/*
	 * Main constructor for Quorum
	 * Performs the necessary activities needed to associated a thread with the Quorum
	 * 
	 * @param String - Name of the Quorum 
	 * @param Hive - Reference to Hive which orchestrates all Quorums
	 * @param Channel - The initial channel to add to the Quorum (Must have at least one)
	 * 
	 * @throws IOException - if Quorum already exists in the Hive or another problem exists
	 */
	public Quorum(String name, Hive hive, Channel channel) throws IOException
	{
		this.Id = name;
		this._hive = hive;
		
		// create a container to house all connected channels associated with this Quorum
		this._clientNodes = new ClientNodes(name);
		synchronized (this._clientNodes)
		{
			// add the quorum to the hive
			this._hive.addQuorum(this);
			
			// Add the channel to the quorum
			channel.updateBuffer("Connecting to Quorum: " + this.Id);
			this._clientNodes.add(channel);
						
			// construct a new thread
			this._thread = new Thread(this);
			
			System.out.println("Quorum: (" + name + ") " + "Created");
			this._thread.start();
		}
	}

	/*
	 * constructor - only used for testing
	 * @param String - name of room
	 */
	public Quorum(String name)
	{
		this.Id = name;
		this._hive = null;
		this._clientNodes = null;
		this._thread = null;
	}

	/*
	 * Run() does all the heavy lifting of orchestrating the Quorum.
	 * Runs in a loop checking for messages and notifying all the other channels.
	 * As long as one channel is associated with the Quorum, it will stay alive.
	 */
	public void run()
	{
		System.out.println("Quorum: " + this.Id + ": " + "Started...");
		
		// While the Quorum is alive, take messages from the message buffer
		// and notify users
		while (this._alive) {
			try
			{
				this._clientNodes.notifyChannels("Message (" + this.Id + "): " + this._buffer.take());
				System.out.println("Quorum: (" + this.Id + ") " + "Message Sent");
			}
			catch (InterruptedException ie)
			{
				System.out.println("Quorum: (" + this.Id + ") " + "Stopping...");
				// stop this loop
				break;
			}
		}
		
		System.out.println("Quorum: (" + this.Id + ") " + "Stopping...");
		// remove quorum from hive
		this.removeFromHive();
		System.out.println("Quorum: (" + this.Id + ") " + "Removed from hive");
	}

	/*
	 * adds a channel to the quorum
	 * @param Channel - channel to add
	 * @throws IOException - happens if the associated channel user has already connected or
	 * 						if the Quorum does not exist.
	 */
	public void addChannel(Channel channel) throws IOException
	{
		synchronized(_clientNodes)
		{
			// determine if the quorum still lives
			if (this._alive)
			{
				if (!this._clientNodes.contains(channel.getUserName()))
					channel.updateBuffer("Connecting to Quorum: " + this.Id);
				this._clientNodes.add(channel);
			} else {
				throw new IOException("The Quorum no longer exists");
			}
		}
	}

	/*
	 * removes a user from this Quorum
	 * @param Channel - channel with user to remove
	 */
	public void removeChannel(Channel channel)
	{
		synchronized (_clientNodes)
		{
			this._clientNodes.remove(channel);
			// kill the quorum if there are no more users
			if (this._clientNodes.size() <= 0)
			{
				this._alive = false;
				this._thread.interrupt();
			}
		}
	}

	/*
	 * remove quorum from the hive
	 */
	private void removeFromHive()
	{
		System.out.println("Quorum: " + Id + " - " + "removed from hive.");
		this._hive.removeQuorum(this);
	}

	/*
	 * writes a message into this quorum's buffer
	 */
	public void updateBuffer(String msg)
	{
		this._buffer.add(msg);
	}

	/*
	 * returns true if the Quorum is alive
	 * 
	 * @return boolean - determines if the Quorum is alive
	 */
	public boolean isAlive()
	{
		return this._thread.isAlive();
	}

	/*
	 * support testing
	 * 
	 * @return ClientNodes - return a list users in the Quorum 
	 */
	public ClientNodes getList()
	{
		return this._clientNodes;
	}
}
