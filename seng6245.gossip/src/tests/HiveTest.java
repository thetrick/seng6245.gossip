package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Before;
import org.junit.Test;

import server.*;
import adts.*;

/**
 * Test all activities associated with a Hive
 */
public class HiveTest
{
	private Hive hive;
	private Quorum quorum1;
	private Quorum quorum2;
	private Quorum quorum3;
	private Channel channel;
	private ServerNodes serverNodes;

	/**
	 * Sets up the test fixture.
	 * Called before every test case method.
	 */
	@Before
	public void initialize()
	{
		channel = new Channel("client");
		serverNodes = new ServerNodes();
		try
		{
			serverNodes.add(channel);
		}
		catch (IOException ioEx){}
		channel.getBuffer().clear();
		hive = new Hive(serverNodes);
		quorum1 = new Quorum("quorum1");
		quorum2 = new Quorum("quorum2");
		quorum3 = new Quorum("quorum3");
	}

	// Test adding a single quorum to the Hive
	@Test
	public void testAdd() throws IOException
	{
		hive.addQuorum(quorum1);
		Map<String, Quorum> map = hive.getQuorumsMap();
		assertEquals(map.size(), 1);
		assertEquals(map.get("quorum1"), quorum1);
		LinkedBlockingQueue<String> buffer = channel.getBuffer();
		String output = buffer.poll();
		assertEquals(output, "Hive quorum1");
		output = buffer.poll();
		assertEquals(output, null);
	}

	// Test adding multiple quorums to the Hive
	@Test
	public void testAddN() throws IOException
	{
		String output;
		LinkedBlockingQueue<String> buffer;
		hive.addQuorum(quorum1);
		hive.addQuorum(quorum2);
		hive.addQuorum(quorum3);
		Map<String, Quorum> map = hive.getQuorumsMap();
		assertEquals(map.size(), 3);
		assertEquals(map.get("quorum1"), quorum1);
		assertEquals(map.get("quorum2"), quorum2);
		assertEquals(map.get("quorum3"), quorum3);

		buffer = channel.getBuffer();
		output = buffer.poll();
		assertEquals(output, "Hive quorum1");
		output = buffer.poll();
		assertEquals(output, "Hive quorum1 quorum2");
		output = buffer.poll();
		assertEquals(output, "Hive quorum1 quorum2 quorum3");
		output = buffer.poll();
		assertEquals(output, null);
	}

	// Force fail by adding two quorums to same hive
	@Test(expected = IOException.class)
	public void testFailure_DuplicateQuorum() throws IOException
	{
		hive.addQuorum(quorum1);
		hive.addQuorum(quorum1);
		fail();
	}

	// Remove a single quorum from the Hive
	@Test
	public void testQuorumRemoval() throws IOException
	{
		String output;
		LinkedBlockingQueue<String> buffer;
		hive.addQuorum(quorum1);
		hive.addQuorum(quorum2);
		hive.addQuorum(quorum3);
		Map<String, Quorum> map = hive.getQuorumsMap();
		channel.getBuffer().clear();

		hive.removeQuorum(quorum3);
		assertEquals(map.size(), 2);

		buffer = channel.getBuffer();
		output = buffer.poll();
		assertEquals(output, "Hive quorum1 quorum2");
		output = buffer.poll();
		assertEquals(output, null);
	}

	// Remove all Quorums from the Hive
	@Test
	public void testRemoveAllQuorums() throws IOException
	{
		hive.addQuorum(quorum1);
		hive.addQuorum(quorum2);
		hive.addQuorum(quorum3);

		hive.removeQuorum(quorum3);
		hive.removeQuorum(quorum2);
		hive.removeQuorum(quorum1);
		Map<String, Quorum> map = hive.getQuorumsMap();

		assertEquals(map.size(), 0);
	}

	// Verify if add blocks removals
	@Test
	public void testConcurrencyAddRemove() throws IOException, InterruptedException
	{
		hive = new Hive(serverNodes, true);
		Thread t1 = new Thread()
		{
			public void run()
			{
				try
				{
					hive.addQuorum(quorum1);
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
				hive.removeQuorum(quorum1);
			}
		};

		t1.start();
		t2.start();
		t1.join();
		t2.join();

		assertEquals(hive.getQuorumsMap().size(), 0);
	}

	// Verify if removal blocks adds
	@Test
	public void testConcurrencyRemoveAdd() throws IOException, InterruptedException
	{
		hive = new Hive(serverNodes, true);
		hive.addQuorum(quorum1);
		Thread t1 = new Thread()
		{
			public void run()
			{
				hive.removeQuorum(quorum1);
			}
		};

		Thread t2 = new Thread()
		{
			public void run()
			{
				Utility.pause(500);
				try
				{
					hive.addQuorum(quorum1);
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

		assertEquals(hive.getQuorumsMap().size(), 1);
	}
}
