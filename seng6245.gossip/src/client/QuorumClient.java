package client;
import java.awt.Color;
import java.util.*;

import javax.swing.DefaultListModel;
import javax.swing.text.*;


/**
 * This is a QuorumClient that manages a chat room from the
 * client side.  It has the chat room name, the Rumor history,
 * the displayed Rumor document, and the list of users, and 
 * the username of the user using the chatroom client.
 *
 */
public class QuorumClient {
    private final String chatRoomName;
    private ArrayList<Rumor> RumorHistory;
    private DefaultStyledDocument displayedRumors;
    private DefaultListModel userModel;
    private final String myUsername;
    
    /**
     * Constructs a QuorumClient with the given chatroom name
     * and the username
     * @param nameOfChatRoom
     * @param username
     */
    public QuorumClient(String nameOfChatRoom, String username) {
        chatRoomName = nameOfChatRoom;
        RumorHistory = new ArrayList<Rumor>();
        displayedRumors = new DefaultStyledDocument();
        userModel = new DefaultListModel();
        myUsername = username;
    }
    
    /**
     * Adds a Rumor to the Rumor history and appends the Rumor
     * to the styled document to be displayed in the chat tab text
     * area
     * @param Rumor Rumor object to be added
     * @throws BadLocationException
     */
    public synchronized void addRumor(Rumor Rumor) throws BadLocationException {
        RumorHistory.add(Rumor);
        SimpleAttributeSet userStyle = new SimpleAttributeSet();
        StyleConstants.setBold(userStyle, true);
        if (Rumor.getUserName().equals(myUsername)) {
            StyleConstants.setForeground(userStyle, Color.blue);
        }
        displayedRumors.insertString(displayedRumors.getLength(), Rumor.getUserName() + ": ", userStyle);
        displayedRumors.insertString(displayedRumors.getLength(), Rumor.getRumor() + "\n", null);

    }
    
    /**
     * Updates the users in the user list model from an array list
     * of a new set of users by clearing the array list and repopulating
     * it with the new list.
     * @param newUsers
     */
    public synchronized void updateUsers(ArrayList<String> newUsers) {
        //connectedUsers = newUsers;
        userModel.clear();
        for (int i = 0; i < newUsers.size(); i++) {
        	System.out.println("putting   " + newUsers.get(i));
            userModel.addElement(newUsers.get(i));
        }
        
    }
    
    
    @Override
    public String toString() {
        return chatRoomName;
    }

    /**
     * @return The DefaultStyledDocument of the chatroom Rumors
     */
    public DefaultStyledDocument getDoc() {
        return displayedRumors;
    }
    
    /**
     * @return The name of the chatroom
     */
    public String getChatRoomName() {
        return chatRoomName;
    }

    /**
     * @return The DefaultListModel containing the list of users
     */
    public DefaultListModel getUserListModel() {
        return userModel;
    }

}

