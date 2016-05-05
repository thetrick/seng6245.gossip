package tests;

import static org.junit.Assert.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import adts.*;
import server.*;

/*
 * Test all activities associated with a Channel
 * Including that the command behave as expected
 */
public class ChannelTest
{
	private ServerSocket server;
	private Socket serverSide;
	private Socket clientSide;
	private BufferedReader bufferIn;
	private PrintWriter writerOut;
	private BufferedReader handlerIn;
	private PrintWriter handlerOut;
	private ServerNodes serverNodes;
	private Hive hive;
	private Channel channel;

	/**
	 * Initialize the Test
	 * Called before every test case method.
	 * Build out the Server Socker, input and output buffers
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	@Before
	public void initialize() throws IOException 
	{
		server = new ServerSocket(5000);
		Utility.pause(100);
		clientSide = new Socket("localhost", 5000);
		serverSide = server.accept();
		bufferIn = new BufferedReader(new InputStreamReader(clientSide.getInputStream()));
		writerOut = new PrintWriter(clientSide.getOutputStream());
		handlerIn = new BufferedReader(new InputStreamReader(serverSide.getInputStream()));
		handlerOut = new PrintWriter(clientSide.getOutputStream());
		serverNodes = new ServerNodes();
		hive = new Hive(serverNodes);
	}
	/**
	 * Cleanup the Test by closing the server
	 */
	@After
	public void Cleanup() throws IOException
	{
		server.close();
		Utility.pause(100);
	}
	
	//Verify that our required pieces are running
	@Test
	public void testSetup()
	{
		assertFalse(serverSide == null);
		assertFalse(writerOut == null);
		assertFalse(bufferIn == null);
		assertFalse(handlerOut == null);
		assertFalse(handlerIn == null);
	}
	
	//Verify that the Channel Creation behave appropriately
	@Test
	public void testConstructor() throws IOException, InterruptedException
	{
		writerOut.println("connect User1");
		writerOut.flush();
		channel = new Channel(serverSide, hive, serverNodes);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "To connect type: \"connect [username]\"");
		assertEquals(channel.getUserName(), "User1");
	}
	
	//Verify that Channel Creation works correctly attached to thread
	@Test
	public void testConstructerThreaded() throws IOException, InterruptedException
	{
		Thread thread = new Thread()
		{
			public void run()
			{
				try
				{
					channel = new Channel(serverSide, hive, serverNodes);
				}
				catch (IOException e)
				{
					fail();
				}
			}
		};
		thread.start();
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "To connect type: \"connect [username]\"");
		writerOut.println("connect User1");
		writerOut.flush();
		Utility.pause(100);
		thread.join();
		assertEquals(channel.getUserName(), "User1");
	}
	
	//Verify that the thread is running as expected
	@Test
	public void testThreadInit() throws IOException
	{
		writerOut.println("connect User1");
		writerOut.flush();
		channel = new Channel(serverSide, hive, serverNodes);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "To connect type: \"connect [username]\"");
		assertEquals(channel.getUserName(), "User1");
		
		Thread thread = new Thread(channel);
		thread.start();

		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "Connected");
	}
	
	//Force fail a Bad User Name command
	@Test (expected = IOException.class)
	public void testFail_BadUserName() throws IOException
	{
		writerOut.println("thisiswrong User1");
		writerOut.flush();
		channel = new Channel(serverSide, hive, serverNodes);
		fail();
	}
	
	//Force Fail a channel disconnecting at login
	@Test
	public void testFail_DisconnectAtLogin() throws IOException, InterruptedException
	{
		Thread connector = new Thread()
		{
			public void run()
			{
				try
				{
					channel = new Channel(serverSide, hive, serverNodes);
				}
				catch (IOException e)
				{
					channel = null;
				}
			}
		};
		connector.start();
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "To connect type: \"connect [username]\"");
		clientSide.close();
		connector.join();
		assertEquals(channel, null);
	}
	
	//Verify that our Thread starts correctly
	@Test
	public void testThreading() throws IOException
	{
		writerOut.println("connect User1");
		writerOut.flush();
		channel = new Channel(serverSide, hive, serverNodes);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "To connect type: \"connect [username]\"");
		assertEquals(channel.getUserName(), "User1");
		
		Thread thread = new Thread(channel);
		thread.start();

		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "Connected");
		
		channel.updateBuffer("test-test-test");

		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "test-test-test");
		assertFalse(handlerIn.ready());
	}
		
	//Perform Channel Overload
	@Test
	public void testThreadOverload() throws IOException, InterruptedException
	{
		writerOut.println("connect User1");
		writerOut.flush();
		channel = new Channel(serverSide, hive, serverNodes);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "To connect type: \"connect [username]\"");
		assertEquals(channel.getUserName(), "User1");
		
		Thread thread = new Thread(channel);
		thread.start();

		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "Connected");
		
		Thread[] threads = new Thread[500];
		for(int i = 0; i < 500; i++)
		{
			threads[i] = new Thread()
			{
				public void run()
				{
					for(int x = 0; x < 10; x++)
						channel.updateBuffer("killit");
				}
			};
		}
		
		for(Thread t : threads)
			t.start();
		for(Thread t : threads)
			t.join();
		
		Utility.pause(500);
		for(int i = 0; i<500; i++)
		{
			assertTrue(bufferIn.ready());
			assertEquals(bufferIn.readLine(), "killit");
		}
		assertFalse(bufferIn.ready());
	}
	
	//Test for Bad Command
	@Test
	public void testBadCommand() throws IOException
	{
		writerOut.println("connect User1");
		writerOut.flush();
		channel = new Channel(serverSide, hive, serverNodes);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "To connect type: \"connect [username]\"");
		assertEquals(channel.getUserName(), "User1");
		
		Thread thread = new Thread(channel);
		thread.start();

		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "Connected");
		assertFalse(bufferIn.ready());
		
		writerOut.println("does not exist");
		writerOut.flush();
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "Unrecognized Command: does not exist");
		assertFalse(bufferIn.ready());
	}
	
	//Test Command disconnect
	@Test
	public void testCommand_disconnect() throws IOException
	{
		writerOut.println("connect User1");
		writerOut.flush();
		channel = new Channel(serverSide, hive, serverNodes);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "To connect type: \"connect [username]\"");
		assertEquals(channel.getUserName(), "User1");
		serverNodes.add(channel);
		
		Thread thread = new Thread(channel);
		thread.start();

		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "Connected");
		assertEquals(bufferIn.readLine(), "ServerNodes: User1");
		assertFalse(bufferIn.ready());
		
		//send test data and check response
		writerOut.println("disconnect User1");
		writerOut.flush();
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "disconnectedFromServer");
		assertFalse(bufferIn.ready());
		
		//check state
		assertFalse(thread.isAlive());
		assertFalse(serverNodes.contains("User1"));
		assertTrue(serverSide.isClosed());
		assertFalse(channel.getThread().isAlive());
		channel.updateBuffer("unchecked");
		assertFalse(bufferIn.ready());
	}
	
	//tests if the parser makes a quorum properly
	@Test
	public void testCommand_make() throws IOException
	{
		writerOut.println("connect User1");
		writerOut.flush();
		channel = new Channel(serverSide, hive, serverNodes);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "To connect type: \"connect [username]\"");
		assertEquals(channel.getUserName(), "User1");
		serverNodes.add(channel);
		
		Thread thread = new Thread(channel);
		thread.start();

		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "Connected");
		assertEquals(bufferIn.readLine(), "ServerNodes: User1");
		assertFalse(bufferIn.ready());
		
		//send test data and check response
		writerOut.println("make quorum1");
		writerOut.flush();
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "List of connected Quorums: quorum1");
		assertEquals(bufferIn.readLine(), "connectToQuorum quorum1");
		assertEquals(bufferIn.readLine(), "quorumUsers quorum1 User1");
		assertFalse(bufferIn.ready());
		
		//check state
		assertTrue(thread.isAlive());
		assertTrue(serverNodes.contains("User1"));
		assertFalse(serverSide.isClosed());
		assertTrue(channel.getThread().isAlive());
		channel.updateBuffer("checked");
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "checked");
		assertFalse(bufferIn.ready());
		assertTrue(channel.getQuorums().containsKey("quorum1"));
		
		assertTrue(hive.contains("quorum1"));
	}
	
	//Make a new Quorum when other Quorums are running 
	@Test
	public void testCommand_makeWithOtherQuorums() throws IOException
	{
		hive.addQuorum(new Quorum("quorum2"));
		writerOut.println("connect User1");
		writerOut.flush();
		channel = new Channel(serverSide, hive, serverNodes);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "To connect type: \"connect [username]\"");
		assertEquals(channel.getUserName(), "User1");
		serverNodes.add(channel);
		
		Thread thread = new Thread(channel);
		thread.start();

		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "Connected");
		assertEquals(bufferIn.readLine(), "ServerNodes: User1");
		assertFalse(bufferIn.ready());
		
		//send test data and check response
		writerOut.println("make quorum1");
		writerOut.flush();
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "List of connected Quorums: quorum2 quorum1");
		assertEquals(bufferIn.readLine(), "connectToQuorum quorum1");
		assertEquals(bufferIn.readLine(), "quorumUsers quorum1 User1");
		assertFalse(bufferIn.ready());
		
		//check state
		assertTrue(thread.isAlive());
		assertTrue(serverNodes.contains("User1"));
		assertFalse(serverSide.isClosed());
		assertTrue(channel.getThread().isAlive());
		channel.updateBuffer("checked");
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "checked");
		assertFalse(bufferIn.ready());
		assertTrue(channel.getQuorums().containsKey("quorum1"));
		
		assertTrue(hive.contains("quorum2") && hive.contains("quorum1"));
	}
	
	//Try to make two Quorum with the same name
	@Test
	public void testCommand_makeDuplicateQuorum() throws IOException
	{
		hive.addQuorum(new Quorum("quorum1"));
		writerOut.println("connect User1");
		writerOut.flush();
		channel = new Channel(serverSide, hive, serverNodes);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "To connect type: \"connect [username]\"");
		assertEquals(channel.getUserName(), "User1");
		serverNodes.add(channel);
		
		Thread thread = new Thread(channel);
		thread.start();

		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "Connected");
		assertEquals(bufferIn.readLine(), "ServerNodes: User1");
		assertFalse(bufferIn.ready());
		
		//send test data and check response
		writerOut.println("make quorum1");
		writerOut.flush();
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "badQuorum quorum1 quorum already exists");
		assertFalse(bufferIn.ready());
		
		//check state
		assertTrue(thread.isAlive());
		assertTrue(serverNodes.contains("User1"));
		assertFalse(serverSide.isClosed());
		assertTrue(channel.getThread().isAlive());
		channel.updateBuffer("checked");
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "checked");
		assertFalse(bufferIn.ready());
		assertFalse(channel.getQuorums().containsKey("quorum1"));
		
		assertTrue(hive.contains("quorum1"));
	}
	
	//Test Joining a quorum
	@Test
	public void testCommand_join() throws IOException
	{
		new Quorum("quorum1", hive, new Channel("channel1"));
		writerOut.println("connect User1");
		writerOut.flush();
		channel = new Channel(serverSide, hive, serverNodes);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "To connect type: \"connect [username]\"");
		assertEquals(channel.getUserName(), "User1");
		serverNodes.add(channel);
		
		Thread thread = new Thread(channel);
		thread.start();

		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "Connected");
		assertEquals(bufferIn.readLine(), "ServerNodes: User1");
		assertFalse(bufferIn.ready());
		
		//send test data and check response
		writerOut.println("join quorum1");
		writerOut.flush();
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "connectToQuorum quorum1");
		assertEquals(bufferIn.readLine(), "quorumUsers quorum1 channel1 User1");
		assertFalse(bufferIn.ready());
		
		//check state
		assertTrue(thread.isAlive());
		assertTrue(serverNodes.contains("User1"));
		assertFalse(serverSide.isClosed());
		assertTrue(channel.getThread().isAlive());
		channel.updateBuffer("checked");
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "checked");
		assertFalse(bufferIn.ready());
		assertTrue(channel.getQuorums().containsKey("quorum1"));
		
		assertTrue(hive.contains("quorum1"));
	}
	
	//Test Joining a quorum user already is in
	@Test
	public void testCommand_joinDuplicate() throws IOException
	{
		writerOut.println("connect User1");
		writerOut.flush();
		channel = new Channel(serverSide, hive, serverNodes);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "To connect type: \"connect [username]\"");
		assertEquals(channel.getUserName(), "User1");
		serverNodes.add(channel);
		
		Thread thread = new Thread(channel);
		thread.start();

		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "Connected");
		assertEquals(bufferIn.readLine(), "ServerNodes: User1");
		assertFalse(bufferIn.ready());
		new Quorum("quorum1", hive, channel);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "List of connected Quorums: quorum1");
		assertEquals(bufferIn.readLine(), "connectToQuorum quorum1");
		assertEquals(bufferIn.readLine(), "quorumUsers quorum1 User1");
		
		//send test data and check response
		writerOut.println("join quorum1");
		writerOut.flush();
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "badQuorum quorum1 User already in Quorum");
		assertFalse(bufferIn.ready());
		
		//check state
		assertTrue(thread.isAlive());
		assertTrue(serverNodes.contains("User1"));
		assertFalse(serverSide.isClosed());
		assertTrue(channel.getThread().isAlive());
		channel.updateBuffer("checked");
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "checked");
		assertFalse(bufferIn.ready());
		
		assertTrue(hive.contains("quorum1"));
	}
	 
	//Test Join to Quorum that does not exist
	@Test
	public void testCommand_joinNonExistent() throws IOException
	{
		writerOut.println("connect User1");
		writerOut.flush();
		channel = new Channel(serverSide, hive, serverNodes);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "To connect type: \"connect [username]\"");
		assertEquals(channel.getUserName(), "User1");
		serverNodes.add(channel);
		
		Thread thread = new Thread(channel);
		thread.start();

		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "Connected");
		assertEquals(bufferIn.readLine(), "ServerNodes: User1");
		assertFalse(bufferIn.ready());
		new Quorum("quorum1", hive, channel);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "List of connected Quorums: quorum1");
		assertEquals(bufferIn.readLine(), "connectToQuorum quorum1");
		assertEquals(bufferIn.readLine(), "quorumUsers quorum1 User1");
		
		//send test data and check response
		writerOut.println("join quorum2");
		writerOut.flush();
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "badQuorum quorum2 quorum name does not exist");
		assertFalse(bufferIn.ready());
		
		//check state
		assertTrue(thread.isAlive());
		assertTrue(serverNodes.contains("User1"));
		assertFalse(serverSide.isClosed());
		assertTrue(channel.getThread().isAlive());
		channel.updateBuffer("checked");
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "checked");
		assertFalse(bufferIn.ready());
		
		assertTrue(hive.contains("quorum1"));
	}
	
	//Test Exit Command
	@Test
	public void testCommand_exit() throws IOException
	{
		writerOut.println("connect User1");
		writerOut.flush();
		channel = new Channel(serverSide, hive, serverNodes);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "To connect type: \"connect [username]\"");
		assertEquals(channel.getUserName(), "User1");
		serverNodes.add(channel);
		
		Thread thread = new Thread(channel);
		thread.start();

		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "Connected");
		assertEquals(bufferIn.readLine(), "ServerNodes: User1");
		assertFalse(bufferIn.ready());
		Quorum quorum1 = new Quorum("quorum1", hive, new Channel("channel1"));
		quorum1.addChannel(channel);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "List of connected Quorums: quorum1");
		assertEquals(bufferIn.readLine(), "connectToQuorum quorum1");
		assertEquals(bufferIn.readLine(), "quorumUsers quorum1 channel1 User1");
		channel.getQuorums().put("quorum1", quorum1);
		
		//send test data and check response
		writerOut.println("exit quorum1");
		writerOut.flush();
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "disconnectedquorum quorum1");
		assertFalse(bufferIn.ready());
		
		//check state
		assertTrue(thread.isAlive());
		assertTrue(serverNodes.contains("User1"));
		assertFalse(serverSide.isClosed());
		assertTrue(channel.getThread().isAlive());
		channel.updateBuffer("checked");
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "checked");
		assertFalse(bufferIn.ready());
		assertFalse(channel.getQuorums().containsKey("quorum1"));
		
		assertTrue(hive.contains("quorum1"));
	}
	
	//Test Exit Command on Quorum that user not in
	@Test
	public void testCommand_exitNotInQuorum() throws IOException
	{
		writerOut.println("connect User1");
		writerOut.flush();
		channel = new Channel(serverSide, hive, serverNodes);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "To connect type: \"connect [username]\"");
		assertEquals(channel.getUserName(), "User1");
		serverNodes.add(channel);
		
		Thread thread = new Thread(channel);
		thread.start();

		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "Connected");
		assertEquals(bufferIn.readLine(), "ServerNodes: User1");
		assertFalse(bufferIn.ready());
		new Quorum("quorum1", hive, new Channel("channel1"));
		//quorum1.addChannel(c);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "List of connected Quorums: quorum1");
		//assertEquals(clientIn.readLine(), "connectToQuorum quorum1");
		//assertEquals(clientIn.readLine(), "quorumUsers quorum1 User1 channel1");
		//c.getQuorums().put("quorum1", quorum1);
		
		//send test data and check response
		writerOut.println("exit quorum1");
		writerOut.flush();
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "badQuorum quorum1 user not connected to quorum");
		assertFalse(bufferIn.ready());
		
		//check state
		assertTrue(thread.isAlive());
		assertTrue(serverNodes.contains("User1"));
		assertFalse(serverSide.isClosed());
		assertTrue(channel.getThread().isAlive());
		channel.updateBuffer("checked");
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "checked");
		assertFalse(bufferIn.ready());
		assertFalse(channel.getQuorums().containsKey("quorum1"));
		
		assertTrue(hive.contains("quorum1"));
	}
	
	//test if parser sends messages properly to quorums
	@Test
	public void testCommand_message() throws IOException
	{
		writerOut.println("connect User1");
		writerOut.flush();
		channel = new Channel(serverSide, hive, serverNodes);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "To connect type: \"connect [username]\"");
		assertEquals(channel.getUserName(), "User1");
		serverNodes.add(channel);
		
		Thread thread = new Thread(channel);
		thread.start();

		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "Connected");
		assertEquals(bufferIn.readLine(), "ServerNodes: User1");
		assertFalse(bufferIn.ready());
		Channel channel1 = new Channel("channel1");
		Quorum quorum1 = new Quorum("quorum1", hive, channel1);
		quorum1.addChannel(channel);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "List of connected Quorums: quorum1");
		assertEquals(bufferIn.readLine(), "connectToQuorum quorum1");
		assertEquals(bufferIn.readLine(), "quorumUsers quorum1 channel1 User1");
		channel.getQuorums().put("quorum1", quorum1);
		channel1.getBuffer().clear();
		
		//send test data and check response
		writerOut.println("message quorum1 test-test-test");
		writerOut.flush();
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "message quorum1 User1 test-test-test");
		assertFalse(bufferIn.ready());
		
		//check state
		assertTrue(thread.isAlive());
		assertTrue(serverNodes.contains("User1"));
		assertFalse(serverSide.isClosed());
		assertTrue(channel.getThread().isAlive());
		channel.updateBuffer("checked");
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "checked");
		assertFalse(bufferIn.ready());
		assertTrue(channel.getQuorums().containsKey("quorum1"));
		
		assertTrue(hive.contains("quorum1"));
		assertEquals(channel1.getBuffer().poll(), "message quorum1 User1 test-test-test");
		assertEquals(channel1.getBuffer().poll(), null);
	}
	
	//Test for cleanup resources once disconnected
	@Test
	public void testCommand_disconnectCleanup() throws IOException
	{
		writerOut.println("connect User1");
		writerOut.flush();
		channel = new Channel(serverSide, hive, serverNodes);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "To connect type: \"connect [username]\"");
		assertEquals(channel.getUserName(), "User1");
		serverNodes.add(channel);
		
		Thread thread1 = new Thread(channel);
		thread1.start();

		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "Connected");
		assertEquals(bufferIn.readLine(), "ServerNodes: User1");
		assertFalse(bufferIn.ready());
		Channel channel1 = new Channel("channel1");
		Quorum quorum1 = new Quorum("quorum1", hive, channel1);
		quorum1.addChannel(channel);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "List of connected Quorums: quorum1");
		assertEquals(bufferIn.readLine(), "connectToQuorum quorum1");
		assertEquals(bufferIn.readLine(), "quorumUsers quorum1 channel1 User1");
		channel.getQuorums().put("quorum1", quorum1);
		channel1.getBuffer().clear();
		
		//send test data and check response
		writerOut.println("disconnect User1");
		writerOut.flush();
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "disconnectedFromServer");
		assertFalse(bufferIn.ready());
		
		//check state
		assertFalse(thread1.isAlive());
		assertFalse(serverNodes.contains("User1"));
		assertTrue(serverSide.isClosed());
		assertFalse(channel.getThread().isAlive());
		channel.updateBuffer("unchecked");
		Utility.pause(100);
		assertFalse(bufferIn.ready());
		//assertEquals(clientIn.readLine(), "checked");
		//assertFalse(clientIn.ready());
		assertTrue(channel.getQuorums().containsKey("quorum1"));
		
		assertFalse(quorum1.getList().contains("User1"));
		assertTrue(hive.contains("quorum1"));
		assertEquals(channel1.getBuffer().poll(), "quorumUsers quorum1 channel1");
		assertEquals(channel1.getBuffer().poll(), null);
	}
	
	//Test for bad disconnection
	@Test
	public void testCommand_disconnectForce() throws IOException
	{
		writerOut.println("connect User1");
		writerOut.flush();
		channel = new Channel(serverSide, hive, serverNodes);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "To connect type: \"connect [username]\"");
		assertEquals(channel.getUserName(), "User1");
		serverNodes.add(channel);
		
		Thread thread = new Thread(channel);
		thread.start();

		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "Connected");
		assertEquals(bufferIn.readLine(), "ServerNodes: User1");
		assertFalse(bufferIn.ready());
		Channel channel1 = new Channel("channel1");
		Quorum quorum1 = new Quorum("quorum1", hive, channel1);
		quorum1.addChannel(channel);
		Utility.pause(100);
		assertTrue(bufferIn.ready());
		assertEquals(bufferIn.readLine(), "List of connected Quorums: quorum1");
		assertEquals(bufferIn.readLine(), "connectToQuorum quorum1");
		assertEquals(bufferIn.readLine(), "quorumUsers quorum1 channel1 User1");
		channel.getQuorums().put("quorum1", quorum1);
		channel1.getBuffer().clear();
		
		//send test data and check response
		clientSide.close();
		Utility.pause(100);
		assertFalse(bufferIn.ready());
		
		//check state
		assertFalse(thread.isAlive());
		assertFalse(serverNodes.contains("User1"));
		assertTrue(serverSide.isClosed());
		assertFalse(channel.getThread().isAlive());
		channel.updateBuffer("unchecked");
		Utility.pause(100);
		assertFalse(bufferIn.ready());
		//assertEquals(clientIn.readLine(), "checked");
		//assertFalse(clientIn.ready());
		assertTrue(channel.getQuorums().containsKey("quorum1"));
		
		assertFalse(quorum1.getList().contains("User1"));
		assertTrue(hive.contains("quorum1"));
		assertEquals(channel1.getBuffer().poll(), "quorumUsers quorum1 channel1");
		assertEquals(channel1.getBuffer().poll(), null);
	}
	
}
