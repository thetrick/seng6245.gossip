package adts;

/*
 * this class extends the abstract class UserList and redefines the getList method 
 */

public class ServerNodes extends Nodes
{
	/*
	 * overrites the get list method to append "serverUserList" to the begining
	 * of the string
	 */
	protected String getList()
	{
		String list = super.getList();
		return "serverUserList " + list;
	}
}