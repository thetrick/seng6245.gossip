package client;

/**
 * 
 * @author thetrick
 * Model class used to represent a particular message
 */
public class Rumor {
    private final String _userName;
    private final String _message;
    
    /**
     * Constructor method
     * @param String - User Name
     * @param String - Message
     */
    public Rumor(String userName, String message) 
    {
    	this._userName = userName;
    	this._message = message;
    }

    /**
     * Getter method for the user
     * @return user String
     */
    public String getUserName() {
        return this._userName;
    }
    
    /**
     * Getter method for the message
     * @return message String
     */
    public String getRumor() {
        return this._message;
    }
}
