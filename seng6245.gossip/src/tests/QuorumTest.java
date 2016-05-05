package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import server.*;
import adts.*;

/**
 * Test all activities associated with a Quorum
 */
public class QuorumTest
{
	public Quorum quorum1;
	public Quorum quorumOne;
	public Hive hive;
	public Channel channel1;
	public Channel channel2;
	public Channel channel3;
	public ServerNodes serverNodes;

	/**
	 * Initialize the Test
	 * Called before every test case method.
	 * Stubs out some test channels, add to the Server Node
	 * and initializes the Hive
	 */
	@Before
	public void initialize()
	{
		channel1 = new Channel("1");
		channel2 = new Channel("2");
		channel3 = new Channel("3");
		serverNodes = new ServerNodes();
		try
		{
			serverNodes.add(channel1);
			serverNodes.add(channel2);
			serverNodes.add(channel3);
			hive = new Hive(serverNodes);
			channel1.getBuffer().clear();
			channel2.getBuffer().clear();
			channel3.getBuffer().clear();
		}
		catch (IOException e)
		{
			fail();
		}
	}

	//Verify that the Quorum Creation behave appropriately
	@Test
	public void testQuorumCreation() throws IOException
	{
		quorum1 = new Quorum("q1test", hive, channel1);
		assertEquals(quorum1.Id, "q1test");
		assertTrue(quorum1.getList().getNodesMap().containsValue(channel1));
		assertTrue(hive.contains("q1test"));
		assertTrue(quorum1.isAlive());
	}

	//Verify that Duplicate Quourms added to the Hive Fails
	@Test
	public void testDuplicateQuorumFailure()
	{
		try
		{
			quorum1 = new Quorum("q1test", hive, channel1);
			quorumOne = new Quorum("q1test", hive, channel1);
			fail();
		}
		catch (IOException e)
		{
		}
		assertTrue(quorum1.getList().getNodesMap().containsValue(channel1));
		assertTrue(hive.contains("q1test"));
		assertTrue(hive.getQuorumsMap().containsValue(quorum1));
		assertFalse(hive.getQuorumsMap().containsValue(quorumOne));
		assertTrue(quorum1.isAlive());
	}

	//Add Channel to Quorum
	@Test
	public void testAdd1() throws IOException
	{
		quorum1 = new Quorum("q1test", hive, channel1);
		quorum1.addChannel(channel2);
		assertTrue(quorum1.getList().getNodesMap().containsValue(channel2));
	}

	//Add Multiple Channels to a Quorum
	@Test
	public void testAddN() throws IOException
	{
		quorum1 = new Quorum("q1test", hive, channel1);
		quorum1.addChannel(channel2);
		assertTrue(quorum1.getList().getNodesMap().containsValue(channel2));
		quorum1.addChannel(channel3);
		assertTrue(quorum1.getList().getNodesMap().containsValue(channel3));
	}

	//Verify that Duplicate Channel result in a failure
	@Test(expected = IOException.class)
	public void testDuplicateChannelFailure() throws IOException
	{
		quorum1 = new Quorum("q1test", hive, channel1);
		quorum1.addChannel(channel2);
		assertTrue(quorum1.getList().getNodesMap().containsValue(channel2));
		quorum1.addChannel(channel2);
		fail();
	}

	//Fail adding a channel to Non-Existent Room
	@Test(expected = IOException.class)
	public void testSendMessageToNonExistentRoom() throws IOException
	{
		quorum1 = new Quorum("q1test", hive, channel1);
		quorum1.removeChannel(channel1);
		Utility.pause(500);
		quorum1.addChannel(channel2);
		fail();
	}

	//Remove Channel from Quorum
	@Test
	public void testChannelRemoval() throws IOException
	{
		quorum1 = new Quorum("q1test", hive, channel1);
		quorum1.addChannel(channel2);
		assertTrue(quorum1.getList().getNodesMap().containsValue(channel2));
		quorum1.addChannel(channel3);
		assertTrue(quorum1.getList().getNodesMap().containsValue(channel3));

		quorum1.removeChannel(channel3);
		assertTrue(quorum1.getList().getNodesMap().containsValue(channel2));
		assertTrue(quorum1.getList().getNodesMap().containsValue(channel1));
		assertFalse(quorum1.getList().getNodesMap().containsValue(channel3));
	}

	//Remove all channels from Quorum and verify that the Quorum is removed
	@Test
	public void testRemoveAllChannels() throws IOException
	{
		quorum1 = new Quorum("q1test", hive, channel1);
		quorum1.addChannel(channel2);
		assertTrue(quorum1.getList().getNodesMap().containsValue(channel2));
		quorum1.addChannel(channel3);
		assertTrue(quorum1.getList().getNodesMap().containsValue(channel3));

		quorum1.removeChannel(channel1);
		quorum1.removeChannel(channel2);
		quorum1.removeChannel(channel3);
		Utility.pause(500);

		assertFalse(quorum1.getList().getNodesMap().containsValue(channel2));
		assertFalse(quorum1.getList().getNodesMap().containsValue(channel1));
		assertFalse(quorum1.getList().getNodesMap().containsValue(channel3));

		assertFalse(hive.contains("q1test"));
		assertFalse(hive.getQuorumsMap().containsValue(quorum1));
		assertFalse(quorum1.isAlive());
	}

	//Send message to the entire room
	@Test
	public void testSendMessageToQuorum() throws IOException
	{
		quorum1 = new Quorum("q1test", hive, channel1);
		quorum1.addChannel(channel2);
		assertTrue(quorum1.getList().getNodesMap().containsValue(channel2));
		quorum1.addChannel(channel3);
		assertTrue(quorum1.getList().getNodesMap().containsValue(channel3));

		channel1.getBuffer().clear();
		channel2.getBuffer().clear();
		channel3.getBuffer().clear();

		quorum1.updateBuffer("test-test-test");
		Utility.pause(500);

		String s1 = channel1.getBuffer().poll();
		assertEquals(s1, "Message (q1test): test-test-test");
		assertEquals(channel1.getBuffer().poll(), null);
		assertEquals(channel2.getBuffer().poll(), "Message (q1test): test-test-test");
		assertEquals(channel2.getBuffer().poll(), null);
		assertEquals(channel3.getBuffer().poll(), "Message (q1test): test-test-test");
		assertEquals(channel3.getBuffer().poll(), null);
	}
}
