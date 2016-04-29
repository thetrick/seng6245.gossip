package gui;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.*;
import javax.swing.event.*;

import client.QuorumClient;

/**
 * Class representing the history of a user's chat. Contains a list with all chatrooms
 * the user has joined. Joining another chatroom after opening the history tab causes
 * the history tab to update with that new chatroom included. 
 * 
 * Selecting a chatroom on the list causes the text pane to display the history
 * of that chatroom up until the user closed the chatroom. If the user is still in the
 * chatroom, the history will also update itself to remain consistent with the open 
 * chatroom. Rejoining the chatroom will once again start updating the history with
 * messages. Creating a new chatroom with the exact same name as an old, closed 
 * chatroom counts as rejoining that conversation, and history continues as appropriate.
 *
 */
public class History extends JPanel{
	private static final long serialVersionUID = 1L;
	private final JLabel history;
    private final JTextPane convoHistory;
    private final JList pastChats;
    
    /**
     * Constructor for the History Tab.
     * @param connectedRoomsHistory A hashmap of chatroom names to the matching QuorumClient.
     * Should contain all chatrooms connected to ever during this user session.
     */
    public History(DefaultListModel pastChatModel) {
        Font TitleFont = new Font("SANS_SERIF", Font.BOLD, 18);
        history = new JLabel("History");
        history.setFont(TitleFont);
        convoHistory = new JTextPane();
        pastChats = new JList(pastChatModel);
        setName("History");
        
        
        convoHistory.setEditable(false);
        JScrollPane convoScroll = new JScrollPane (convoHistory);
        convoScroll.setPreferredSize(new Dimension(700, 550));
        JScrollPane chatScroll = new JScrollPane (pastChats);
        chatScroll.setPreferredSize(new Dimension(250, 550));
        pastChats.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        pastChats.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && pastChats.getSelectedValue() != null) {
                    QuorumClient chatroom = (QuorumClient) pastChats.getSelectedValue();
                    convoHistory.setStyledDocument(chatroom.getDoc());
                }
            }
        });
        
        //defining the layout
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        
        //setting some margins around our components
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        //organizing components
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(history)
                .addGroup(layout.createParallelGroup()
                        .addComponent(convoScroll)
                        .addComponent(chatScroll)));
        
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap(15, 22)
                        .addComponent(history))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(convoScroll)
                        .addComponent(chatScroll)));
    }
}
