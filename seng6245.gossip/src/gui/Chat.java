package gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;

import javax.swing.*;

import client.*;

/**
 * A class representing the gui for a single chatroom. Messages are entered into a
 * text field and submitted to the server either by hitting the send button or by
 * pressing ENTER on the keyboard. The conversation is displayed in a text pane on
 * the left, and all users in the chatroom are displayed on the right. Earlier chat
 * history is displayed in the window upon the user rejoining the room, where the
 * history is defined as conversation held while the user is present in the room, and
 * rejoining is defined as entering a chatroom with the exact same name as a previously
 * entered chatroom.
 *
 */
public class Chat extends JPanel{
	private static final long serialVersionUID = 1L;
	private final JLabel chatName;
    private final JTextPane conversation;
    private final JList currentUsers;
    private final JTextField myMessage;
    private final JButton send;
    private Client client;
    private final String roomname;
    
    /**
     * 
     * The constructor for the ChatTab
     * 
     * @param chatname The name of the chatroom in the tab.
     * @param main The main where the ChatTab is displayed.
     */
    public Chat(String chatname, Main main) {
    	this.roomname = chatname;
        Font TitleFont = new Font("SANS_SERIF", Font.BOLD, 18);
        chatName = new JLabel(chatname);
        chatName.setFont(TitleFont);
        conversation = new JTextPane();
        conversation.setDocument(main.getCurrentRoom(chatname).getDoc());
        currentUsers = new JList(main.getCurrentRoom(chatname).getUserListModel());
        myMessage = new JTextField();
        send = new JButton("Submit");
        this.client = main.getClient();
        setName(chatname);
        
        conversation.setEditable(false);
        JScrollPane chatScroll = new JScrollPane (conversation);
        chatScroll.setPreferredSize(new Dimension(700, 550));
        JScrollPane userScroll = new JScrollPane (currentUsers);
        userScroll.setPreferredSize(new Dimension(250, 550));
        
        send.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent e) {
                sendMessage();
            }
        });

        myMessage.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER)
                    sendMessage();
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
                .addComponent(chatName)
                .addGroup(layout.createParallelGroup()
                        .addComponent(chatScroll)
                        .addComponent(userScroll))
                .addGroup(layout.createParallelGroup()
                        .addComponent(myMessage, GroupLayout.PREFERRED_SIZE, 
                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(send)));
        
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap(15, 22)
                        .addComponent(chatName))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(chatScroll)
                        .addComponent(userScroll))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(myMessage)
                        .addComponent(send)));
    }
    
    /**
     * Used to send a message to the client (which will pass it on to the server)
     */
    private void sendMessage() {
        String m = myMessage.getText();
        if (m != null && m.length() > 0) {
            client.send("message " + roomname + " " + m);
            myMessage.setText("");
        }
    }
}

