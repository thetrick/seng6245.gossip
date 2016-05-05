package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.Before;
import org.junit.Test;
import adts.*;
import server.*;

public class ClientNodesTest
{
	private ClientNodes clientNodes;
	private Channel channel1;
	private Channel channel2;
	private Channel channel3;
	private Channel channelOne;

	/**
	 * Sets up the test fixture.
	 * Called before every test case method.
	 * creates a list and three client stubs to test with.
	 */
	@Before
	public void initialize()
	{
		clientNodes = new ClientNodes("users");
		channel1 = new Channel("1");
		channel2 = new Channel("2");
		channel3 = new Channel("3");
		channelOne = new Channel("1");
	}

	// tests the getList method after adding clients
	@Test
	public void testGetList() throws IOException
	{
		clientNodes.add(channel1);
		clientNodes.add(channel2);
		clientNodes.add(channel3);

		String output = clientNodes.getList();
		assertEquals(output, "ClientNodes (users): 1 2 3");
	}

	// tests the getList method on an empty list
	@Test
	public void testEmptyList() throws IOException
	{
		String output = clientNodes.getList();
		assertEquals(output, "ClientNodes (users): ");
	}

	// tests the getList method on an empty list that was once filled
	@Test
	public void testEmptyListAfterRemovals() throws IOException
	{
		clientNodes.add(channel1);
		clientNodes.add(channel2);
		clientNodes.add(channel3);

		clientNodes.remove(channel3);
		clientNodes.remove(channel2);
		clientNodes.remove(channel1);

		String output = clientNodes.getList();
		assertEquals(output, "ClientNodes (users): ");
	}

	// test to see if we can add a user properly
	// tests both local map as well as messages passed to the client
	@Test
	public void testAdd() throws IOException
	{
		clientNodes.add(channel1);
		Map<String, Channel> map = clientNodes.getNodesMap();
		assertEquals(map.size(), 1);
		assertEquals(map.get("1"), channel1);
		LinkedBlockingQueue<String> buffer = channel1.getBuffer();
		String output = buffer.poll();
		assertEquals(output, "ClientNodes (users): 1");
		output = buffer.poll();
		assertEquals(output, null);
	}

	// similar to above, tests with multiple user additions
	@Test
	public void testAddN() throws IOException
	{
		String output;
		LinkedBlockingQueue<String> buffer;
		clientNodes.add(channel1);
		clientNodes.add(channel2);
		clientNodes.add(channel3);
		Map<String, Channel> map = clientNodes.getNodesMap();
		assertEquals(map.size(), 3);
		assertEquals(map.get("1"), channel1);
		assertEquals(map.get("2"), channel2);
		assertEquals(map.get("3"), channel3);

		buffer = channel1.getBuffer();
		output = buffer.poll();
		assertEquals(output, "ClientNodes (users): 1");
		output = buffer.poll();
		assertEquals(output, "ClientNodes (users): 1 2");
		output = buffer.poll();
		assertEquals(output, "ClientNodes (users): 1 2 3");
		output = buffer.poll();
		assertEquals(output, null);

		buffer = channel2.getBuffer();
		output = buffer.poll();
		assertEquals(output, "ClientNodes (users): 1 2");
		output = buffer.poll();
		assertEquals(output, "ClientNodes (users): 1 2 3");
		output = buffer.poll();
		assertEquals(output, null);

		buffer = channel3.getBuffer();
		output = buffer.poll();
		assertEquals(output, "ClientNodes (users): 1 2 3");
		output = buffer.poll();
		assertEquals(output, null);
	}

	// tests to see if multiple additions of same user fails
	@Test
	public void addFailure()
	{
		try
		{
			clientNodes.add(channel1);
			clientNodes.add(channelOne);
			fail();
		}
		catch (IOException ioEx)
		{
			assertEquals(ioEx.getMessage(), "The user already exist in the Channel.");
			assertFalse(clientNodes.getNodesMap().containsValue(channelOne));
			assertTrue(clientNodes.getNodesMap().containsValue(channel1));
		}
	}
}
