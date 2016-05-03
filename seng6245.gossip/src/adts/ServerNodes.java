package adts;

/*
 * Extends the base class nodes 
 */

public class ServerNodes extends Nodes
{
	/*
	 * appends "ServerNodes: "  to the underlying list of nodes
	 */
	@Override
	protected String getList()
	{
		String list = super.getList();
		return "ServerNodes: " + list;
	}
}