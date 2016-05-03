package client;
import java.awt.Color;
import java.util.*;

import javax.swing.DefaultListModel;
import javax.swing.text.*;


/**
 * @author thetrick
 * Manages the Quorum from the client. Stores the following information:
 * Name, History, Display Style Information and list of users
 */
public class QuorumClient {
    private final String _quorumId;
    private ArrayList<Rumor> _rumorHistory;
    private DefaultStyledDocument _defaultStyledDocument;
    private DefaultListModel _users;
    private final String _currentUserName;
    
    /**
     * Constructs a QuorumClient with the given chatroom name
     * and the username
     * @param quorumId
     * @param username
     */
    public QuorumClient(String quorumId, String username) {
        this._quorumId = quorumId;
        this._rumorHistory = new ArrayList<Rumor>();
        this._defaultStyledDocument = new DefaultStyledDocument();
        this._users = new DefaultListModel();
        this._currentUserName = username;
    }
    
    /**
     * Adds a Rumor to the Rumor history and appends the Rumor
     * to the styled document to be displayed in the chat tab text
     * area
     * @param Rumor Rumor object to be added
     * @throws BadLocationException
     */
    public synchronized void addRumor(Rumor Rumor) throws BadLocationException {
    	this._rumorHistory.add(Rumor);
        SimpleAttributeSet userStyle = new SimpleAttributeSet();
        StyleConstants.setBold(userStyle, true);
        if (Rumor.getUserName().equals(_currentUserName)) {
            StyleConstants.setForeground(userStyle, Color.blue);
        }
        this._defaultStyledDocument.insertString(_defaultStyledDocument.getLength(), Rumor.getUserName() + ": ", userStyle);
        this._defaultStyledDocument.insertString(_defaultStyledDocument.getLength(), Rumor.getRumor() + "\n", null);
    }
    
    /**
     * refreshes the users using a list passed in... clears the old list
     * and adds the new users back in.
     * @param newUsers
     */
    public synchronized void updateUsers(ArrayList<String> newUsers) {
        this._users.clear();
        for (int i = 0; i < newUsers.size(); i++) {
        	System.out.println("putting   " + newUsers.get(i));
            this._users.addElement(newUsers.get(i));
        }
        
    }
    
    
    @Override
    public String toString() {
        return this._quorumId;
    }

    /**
     * @return The DefaultStyledDocument of the Quorum's Rumors
     */
    public DefaultStyledDocument getDefaultStyledDocument() {
        return this._defaultStyledDocument;
    }
    
    /**
     * @return The name of the Quorum
     */
    public String getQuorumId() {
        return this._quorumId;
    }

    /**
     * @return The DefaultListModel containing the list of users
     */
    public DefaultListModel getUsers() {
        return this._users;
    }

}

