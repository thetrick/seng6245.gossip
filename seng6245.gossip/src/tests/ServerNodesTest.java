package tests;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import adts.*;
import server.*;

public class ServerNodesTest
{
	private ServerNodes serverNodes;
	private Channel channel1;
	private Channel channel2;
	private Channel channel3;

	/**
	 * Sets up the test fixture.
	 * Needed for all test
	 */
	@Before
	public void initialize()
	{
		serverNodes = new ServerNodes();
		channel1 = new Channel("1");
		channel2 = new Channel("2");
		channel3 = new Channel("3");
	}

	// tests the getList method after adding clients
	@Test
	public void testGetList() throws IOException
	{
		serverNodes.add(channel1);
		serverNodes.add(channel2);
		serverNodes.add(channel3);

		String output = serverNodes.getList();
		assertEquals(output, "ServerNodes: 1 2 3");
	}

	// tests the getList method on an empty list
	@Test
	public void testEmptyList() throws IOException
	{
		String output = serverNodes.getList();
		assertEquals(output, "ServerNodes: ");
	}

	// tests the getList method on an empty list that was once filled
	@Test
	public void testEmptyListAfterRemovals() throws IOException
	{
		serverNodes.add(channel1);
		serverNodes.add(channel2);
		serverNodes.add(channel3);

		serverNodes.remove(channel3);
		serverNodes.remove(channel2);
		serverNodes.remove(channel1);

		String output = serverNodes.getList();
		assertEquals(output, "ServerNodes: ");
	}
}
