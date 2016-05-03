package adts;

import java.io.IOException;

import server.Channel;

/*
 * Extends the base class Nodes
 */
public class ClientNodes extends Nodes
{

	// used to track the name of node list
	private final String name;

	/*
	 * constructor
	 * 
	 * @param String - name of the user list
	 */
	public ClientNodes(String name)
	{
		super();
		this.name = name;
	}

	/*
	 * appends "ClientNodes (" + this.name + "): "  to the underlying list of nodes
	 */
	@Override
	protected String getList()
	{
		String list = super.getList();
		return "ClientNodes (" + this.name + "): " + list;
	}
	
	/*
	 * adds a channel to the list of nodes
	 * handles the exception if the channel's user already exists in another channel
	 */
	@Override
	public void add(Channel channel) throws IOException
	{
		try
		{
			super.add(channel);
		}
		catch (IOException ioEx)
		{
			if(ioEx.getMessage().equals("The user associated with this channel already exists.")) {
				throw new IOException("The user already exist in the Channel.");
			} else {
				throw ioEx;
			}
		}
	}
}

