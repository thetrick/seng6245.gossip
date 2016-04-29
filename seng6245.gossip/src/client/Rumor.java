package client;

public class Rumor {
    private final String _userName;
    private final String _message;
    
    /**
     * Constructor method
     * @param u user String
     * @param m msg String
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
