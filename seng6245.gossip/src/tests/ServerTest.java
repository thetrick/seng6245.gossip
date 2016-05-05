package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import server.*;

/**
 * Test the Server Classes ability to accept connections
 */
public class ServerTest
{
	BufferedReader buffer;
	Server server;
	Thread thread;
	Socket socket1;
	Socket socket2;

	/**
	 * Sets up the test fixture.
	 * Called before every test case method.
	 */
	@Before
	public void intialize()
	{
		PipedOutputStream pipeOut = new PipedOutputStream();
		PipedInputStream pipeIn;
		try
		{
			pipeIn = new PipedInputStream(pipeOut);
			System.setErr(new PrintStream(pipeOut));
			buffer = new BufferedReader(new InputStreamReader(pipeIn));
		}
		catch (IOException e)
		{
			fail();
		}

		thread = new Thread()
		{
			public void run()
			{
				server.serve();
			}
		};
	}

	//Verify if Server Creation behave appropriately
	@Test
	public void testServerCreation() throws IOException, InterruptedException
	{
		server = new Server(25252);
		assertFalse(buffer.ready());
		thread.start();

		Utility.pause(500);
		assertEquals(buffer.readLine(), "Starting Server on port 25252");
		assertEquals(buffer.readLine(), "Server waiting for clients...");
		assertFalse(buffer.ready());
		server.getServerSocket().close();
		thread.join();
	}

	//test to see if sockets can connect properly
	@Test
	public void testConnection() throws IOException, InterruptedException
	{
		server = new Server(25252);
		thread.start();
		Utility.pause(500);
		assertEquals(buffer.readLine(), "Starting Server on port 25252");
		assertEquals(buffer.readLine(), "Server waiting for clients...");

		socket1 = new Socket("localhost", 25252);

		ArrayList<String> expected = new ArrayList<String>();
		expected.add("Creating User");
		expected.add("Server waiting");
		ArrayList<String> actual = new ArrayList<String>();
		actual.add(buffer.readLine());
		actual.add(buffer.readLine());
		assertFalse(buffer.ready());
		new PrintWriter(socket1.getOutputStream(), true).println("connect one");
		expected.add("Adding User");
		expected.add("Starting User");
		actual.add(buffer.readLine());
		actual.add(buffer.readLine());
		assertFalse(buffer.ready());
		server.getServerSocket().close();
		thread.join();
	}

	//test to see if multiple sockets can connect at the same time without blocking each other while making a username.
	@Test
	public void testSimultaneousConnection() throws IOException, InterruptedException
	{
		server = new Server(25252);
		thread.start();
		Utility.pause(500);
		assertEquals(buffer.readLine(), "Starting Server on port 25252");
		assertEquals(buffer.readLine(), "Server waiting for clients...");

		socket1 = new Socket("localhost", 25252);
		socket2 = new Socket("localhost", 25252);

		ArrayList<String> expected = new ArrayList<String>();
		expected.add("Creating User");
		expected.add("Server waiting");
		expected.add("Creating User");
		expected.add("Server waiting");
		
		ArrayList<String> actual = new ArrayList<String>();
		actual.add(buffer.readLine());
		actual.add(buffer.readLine());
		actual.add(buffer.readLine());
		actual.add(buffer.readLine());
		
		assertFalse(buffer.ready());
		new PrintWriter(socket1.getOutputStream(), true).println("connect User1");
		new PrintWriter(socket2.getOutputStream(), true).println("connect User2");
		expected.add("Adding User");
		expected.add("Starting User");
		expected.add("Adding User");
		expected.add("Starting User");
		actual.add(buffer.readLine());
		actual.add(buffer.readLine());
		actual.add(buffer.readLine());
		actual.add(buffer.readLine());
		assertFalse(buffer.ready());
		server.getServerSocket().close();
		thread.join();
	}
}
