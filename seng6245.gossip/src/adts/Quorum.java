package adts;


/**
 * This class implement a fixture representing a 'chat room' that allows channel handlers(clients) to communicate with each other
 * This room will remain active as long as there exists a user in the room. upon the last user leaving, the room will deregister it_thread from the listings and 
 * disable it_thread. 
 * 
 */

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import server.Channel;

public class Quorum implements Runnable
{
	public final String name;
	private final Hive _hive;
	private final ClientNodes _clientNodes;
	// input buffer into this chat room for concurrency
	private LinkedBlockingQueue<String> _buffer = new LinkedBlockingQueue<String>();
	// a consumer thread for the above buffer
	private Thread _thread;
	private boolean alive = true;

	/*
	 * constructor for Chat room - will attempt to add _thread to the room listing,
	 * and will throw an exception if it cant(like if the room exists)
	 * 
	 * @param String - name of the room Hive - pointer to the master list of
	 * all the _hive Channel - user creating this room in order to
	 * have at least one user.
	 * 
	 * @throws IOException - if the room cannot be created (or added to the
	 * master list)
	 */
	public Quorum(String name, Hive hive, Channel channel) throws IOException
	{
		this.name = name;
		this._hive = hive;
		// create a new list to hold all of the connected users to this room
		this._clientNodes = new ClientNodes(name);
		synchronized (_clientNodes)
		{
			// add this room to the listing of the _hive
			_hive.add(this);
			// connect the creator to the room
			channel.updateBuffer("connectedRoom " + name);
			_clientNodes.add(channel);
			// construct a new thread based on it_thread
			_thread = new Thread(this);
			System.out.println("  Room: " + name + " - " + "Created");
			// start this Quorum!!!!
			_thread.start();
		}
	}

	/*
	 * Constructor for Quorum stub - only used for testing sets all fields
	 * except name to null
	 * 
	 * @param String - name of room
	 */
	public Quorum(String name)
	{
		this.name = name;
		this._hive = null;
		this._clientNodes = null;
		this._thread = null;
	}

	/*
	 * Method that controls the main loop for the Quorum will keep looping
	 * while this Quorum is 'alive' - at least one person is connected the
	 * loop will block until it consumes an element from the buffer and relay
	 * the message to all the connected buffers
	 */
	public void run()
	{
		System.out.println("  Room: " + name + " - " + "Input Thread Started");
		// main loop
		while (alive)
			// read an element and inform all of the connected users using the
			// Hive method
			try
			{
				_clientNodes.informAll("message " + name + " " + _buffer.take());
				System.out.println("  Room: " + name + " - " + "Message Sent");
				// if the thread is interrupted (on shutdown)
			}
			catch (InterruptedException e)
			{
				System.out.println("  Room: " + name + " - " + "Stopping Input Thread");
				// stop this loop
				break;
			}

		System.out.println("  Room: " + name + " - " + "Stopped Input Thread");
		// remove this room from all listings.
		cleanup();
		System.out.println("  Room: " + name + " - " + "Cleanup complete");
	}

	/*
	 * Adds a user to this chat room
	 * 
	 * @param Channel - client attempting connect
	 * 
	 * @throws IOException - if the client cannot be added this could happen if
	 * the client already exists or if the room is dead
	 */
	public void addUser(Channel channel) throws IOException
	{
		synchronized(_clientNodes)
		{
			if (alive)
			// try to add the channel
			{
				if (!this._clientNodes.contains(channel.getUserName()))
					channel.updateBuffer("connectedRoom " + name);
				this._clientNodes.add(channel);
			}
			else
				// throw an IOException if the room is dead
				throw new IOException("Room no longer exists");
		}
	}

	/*
	 * removes a user from this Quorum
	 * 
	 * @param Channel - client to remove
	 */
	public void removeUser(Channel channel)
	{
		synchronized (_clientNodes)
		{
			this._clientNodes.remove(channel);
			// if there are no more channels to this room - stop the room
			if (_clientNodes.size() <= 0)
			{
				alive = false;
				this._thread.interrupt();
			}
		}
	}

	/*
	 * cleans up this room from all the listings
	 */
	private void cleanup()
	{
		System.out.println("  Room: " + name + " - " + "Removing from server listing");
		// remove this room from the master list
		this._hive.remove(this);
	}

	/*
	 * pushes a message into this room's message buffer
	 */
	public void updateBuffer(String info)
	{
		this._buffer.add(info);
	}

	/*
	 * returns if this room is alive or not
	 * 
	 * @return boolean - if the room is alive (active)
	 */
	public boolean isAlive()
	{
		return this._thread.isAlive();
	}

	/*
	 * gets the userList - for testing only
	 * 
	 * @return ClientNodes - list of all people on this room
	 */
	public ClientNodes getList()
	{
		return this._clientNodes;
	}
}
