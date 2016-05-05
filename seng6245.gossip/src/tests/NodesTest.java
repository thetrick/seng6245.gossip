package tests;

import adts.*;
import server.*;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Before;
import org.junit.Test;



/**
 * Test all public methods of Nodes using scenarios involving 
 * 1-to-n users
 */
public class NodesTest
{
	private Nodes nodes;
	private Channel channel1;
	private Channel channel2;
	private Channel channel3;
	private Channel channelOne;

	/**
	 * Sets up the test fixture.
	 * Needed for all test
	 */
	@Before
	public void initialize()
	{
		nodes = new Nodes(){};
		channel1 = new Channel("1");
		channel2 = new Channel("2");
		channel3 = new Channel("3");
		channelOne = new Channel("1");
	}

	// Determine if we can add a user properly
	@Test
	public void testAdd() throws IOException
	{
		nodes.add(channel1);
		Map<String, Channel> map = nodes.getNodesMap();
		assertEquals(map.size(), 1);
		assertEquals(map.get("1"), channel1);
		LinkedBlockingQueue<String> buffer = channel1.getBuffer();
		String output = buffer.poll();
		assertEquals(output, "1");
		output = buffer.poll();
		assertEquals(output, null);
	}

	// Determine if we can add multiple users
	@Test
	public void testAddN() throws IOException
	{
		String output;
		LinkedBlockingQueue<String> buffer;
		nodes.add(channel1);
		nodes.add(channel2);
		nodes.add(channel3);
		Map<String, Channel> map = nodes.getNodesMap();
		assertEquals(map.size(), 3);
		assertEquals(map.get("1"), channel1);
		assertEquals(map.get("2"), channel2);
		assertEquals(map.get("3"), channel3);

		buffer = channel1.getBuffer();
		output = buffer.poll();
		assertEquals(output, "1");
		output = buffer.poll();
		assertEquals(output, "1 2");
		output = buffer.poll();
		assertEquals(output, "1 2 3");
		output = buffer.poll();
		assertEquals(output, null);

		buffer = channel2.getBuffer();
		output = buffer.poll();
		assertEquals(output, "1 2");
		output = buffer.poll();
		assertEquals(output, "1 2 3");
		output = buffer.poll();
		assertEquals(output, null);

		buffer = channel3.getBuffer();
		output = buffer.poll();
		assertEquals(output, "1 2 3");
		output = buffer.poll();
		assertEquals(output, null);
	}

	// Check if multiple channels from same user fail
	@Test
	public void testFail()
	{
		try
		{
			nodes.add(channel1);
			nodes.add(channelOne);
			fail();
		}
		catch (IOException e)
		{
			assertEquals(e.getMessage(), "The user associated with this channel already exists.");
			assertFalse(nodes.getNodesMap().containsValue(channelOne));
			assertTrue(nodes.getNodesMap().containsValue(channel1));
		}
	}

	// Removes user from the list
	@Test
	public void testRemove() throws IOException
	{
		String output;
		LinkedBlockingQueue<String> buffer;
		nodes.add(channel1);
		nodes.add(channel2);
		nodes.add(channel3);
		Map<String, Channel> map = nodes.getNodesMap();
		for (Channel c : map.values())
			c.getBuffer().clear();

		nodes.remove(channel3);
		assertEquals(map.size(), 2);

		buffer = channel1.getBuffer();
		output = buffer.poll();
		assertEquals(output, "1 2");
		output = buffer.poll();
		assertEquals(output, null);

		buffer = channel2.getBuffer();
		output = buffer.poll();
		assertEquals(output, "1 2");
		output = buffer.poll();
		assertEquals(output, null);

		buffer = channel3.getBuffer();
		output = buffer.poll();
		assertEquals(output, null);
	}

	// Remove all clients
	@Test
	public void testRemoveAll() throws IOException
	{
		nodes.add(channel1);
		nodes.add(channel2);
		nodes.add(channel3);

		nodes.remove(channel3);
		nodes.remove(channel2);
		nodes.remove(channel1);
		Map<String, Channel> map = nodes.getNodesMap();
		assertEquals(map.size(), 0);
	}

	// Check getList method after adding channels
	@Test
	public void testGetList() throws IOException
	{
		nodes.add(channel1);
		nodes.add(channel2);
		nodes.add(channel3);

		String output = nodes.getList();
		assertEquals(output, "1 2 3");
	}

	// check for empty node list
	@Test
	public void testEmptyList() throws IOException
	{
		String output = nodes.getList();
		assertEquals(output, "");
	}

	// check for empty node list after removals
	@Test
	public void testEmptyListAfterRemovals() throws IOException
	{
		nodes.add(channel1);
		nodes.add(channel2);
		nodes.add(channel3);

		nodes.remove(channel3);
		nodes.remove(channel2);
		nodes.remove(channel1);

		String output = nodes.getList();
		assertEquals(output, "");
	}

	// tests the size method
	@Test
	public void testSize() throws IOException
	{
		nodes.add(channel1);
		nodes.add(channel2);
		nodes.add(channel3);

		Map<String, Channel> map = nodes.getNodesMap();
		assertEquals(map.size(), nodes.size());
	}

	// verify size method on an empty list
	@Test
	public void testSizeEmpty() throws IOException
	{
		Map<String, Channel> map = nodes.getNodesMap();
		assertEquals(map.size(), nodes.size());
	}

	// tests the size method on an empty list that was once filled
	@Test
	public void testSizeEmptyAfterFull() throws IOException
	{
		nodes.add(channel1);
		nodes.add(channel2);
		nodes.add(channel3);

		nodes.remove(channel3);
		nodes.remove(channel2);
		nodes.remove(channel1);

		Map<String, Channel> map = nodes.getNodesMap();
		assertEquals(map.size(), nodes.size());
	}

	// Notify Channel to verify if messages are sent to all channels
	@Test
	public void testNotifyChannels() throws IOException
	{
		nodes.add(channel1);
		nodes.add(channel2);
		nodes.add(channel3);
		Map<String, Channel> map = nodes.getNodesMap();
		for (Channel c : map.values())
			c.getBuffer().clear();

		nodes.notifyChannels("testing 123");
		for (Channel c : map.values())
		{
			assertEquals(c.getBuffer().poll(), "testing 123");
			assertEquals(c.getBuffer().poll(), null);
		}
	}

	// Notify Channel to verify if messages are sent to all channels
	// except to the one not added to nodes list
	@Test
	public void testNotifyChannelsNonUsers() throws IOException
	{

		nodes.add(channel1);
		nodes.add(channel2);
		Map<String, Channel> map = nodes.getNodesMap();
		for (Channel c : map.values())
			c.getBuffer().clear();

		nodes.notifyChannels("testing 123");
		for (Channel c : map.values())
		{
			assertEquals(c.getBuffer().poll(), "testing 123");
			assertEquals(c.getBuffer().poll(), null);
		}

		assertEquals(channel3.getBuffer().poll(), null);
	}

	// tests to see if inform all does not send messages to a newly disconnected
	// client
	@Test
	public void testInformAllNonUserAfterDisconnect() throws IOException
	{

		nodes.add(channel1);
		nodes.add(channel2);
		nodes.add(channel3);
		Map<String, Channel> map = nodes.getNodesMap();
		for (Channel c : map.values())
			c.getBuffer().clear();
		nodes.remove(channel3);

		nodes.notifyChannels("testing 123");
		for (Channel c : map.values())
		{
			c.getBuffer().poll();
			assertEquals(c.getBuffer().poll(), "testing 123");
			assertEquals(c.getBuffer().poll(), null);
		}

		assertEquals(channel3.getBuffer().poll(), null);
	}

	// Verify if add blocks removals
	@Test
	public void testConcurrencyAddRemove() throws IOException, InterruptedException
	{
		nodes = new Nodes(true)
		{
		};
		Thread t1 = new Thread()
		{
			public void run()
			{
				try
				{
					nodes.add(channel1);
				}
				catch (IOException e)
				{
					fail();
				}
			}
		};

		Thread t2 = new Thread()
		{
			public void run()
			{
				Utility.pause(500);
				nodes.remove(channel1);
			}
		};

		t1.start();
		t2.start();
		t1.join();
		t2.join();

		assertEquals(nodes.size(), 0);
	}

	// Verify if removal blocks adds
	@Test
	public void testConcurrencyRemoveAdd() throws IOException, InterruptedException
	{
		nodes = new Nodes(true){};
		nodes.add(channel1);
		Thread t1 = new Thread()
		{
			public void run()
			{
				nodes.remove(channel1);
			}
		};

		Thread t2 = new Thread()
		{
			public void run()
			{
			    Utility.pause(500);
				try
				{
					nodes.add(channel1);
				}
				catch (IOException e)
				{
					fail();
				}
			}
		};

		t1.start();
		t2.start();
		t1.join();
		t2.join();

		assertEquals(nodes.size(), 1);
	}
}
