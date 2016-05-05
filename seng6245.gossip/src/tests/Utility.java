package tests;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class Utility
{	
	
	public static Socket socket;

	public static void pause(long sleep)
	{
		try
		{
			Thread.sleep(sleep);
		}
		catch (InterruptedException ignore){}
	}
	
	public static void assignSocket(final ServerSocket server)
	{
		new Thread()
		{
			public void run()
			{
				try
				{
					socket = server.accept();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	public static Socket getServerSocket()
	{
		return socket;
	}
}
