package gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;

import javax.swing.*;

import client.*;

/**
 * 
 * @author thetrick
 * The gui representation of a chat room.  As such, it handles the activities
 * associated with user entering rumors to send to the group of users in a particular 
 * quorum.  The ongoing conversation is displayed in a text pan on the left while the list 
 * of users are displayed on the right.  Any history associated with the user is continously
 * displayed as long as the user is connected to the Quorum
 */
public class Chat extends JPanel{
	private static final long serialVersionUID = 1L;
	private final JLabel _quorumJLabel;
    private final JTextPane _ongoingJTextPane;
    private final JList _quorumUsersJList;
    private final JTextField _newMessageJTextField;
    private final JButton _submitJButton;
    private Client _client;
    private final String _quorumId;
    
    /**
     * 
     * constructor
     * 
     * @param quorumId The id of the Quorum in the tab.
     * @param main The main window where Chat is displayed.
     */
    public Chat(String quorumId, Main main) {
    	this._quorumId = quorumId;
        Font TitleFont = new Font("SANS_SERIF", Font.BOLD, 18);
        _quorumJLabel = new JLabel(quorumId);
        _quorumJLabel.setFont(TitleFont);
        _ongoingJTextPane = new JTextPane();
        _ongoingJTextPane.setDocument(main.getCurrentQuorum(quorumId).getDefaultStyledDocument());
        _quorumUsersJList = new JList(main.getCurrentQuorum(quorumId).getUsers());
        _newMessageJTextField = new JTextField();
        _submitJButton = new JButton("Submit");
        this._client = main.getClient();
        setName(quorumId);
        
        _ongoingJTextPane.setEditable(false);
        JScrollPane chatScroll = new JScrollPane (_ongoingJTextPane);
        chatScroll.setPreferredSize(new Dimension(700, 550));
        JScrollPane userScroll = new JScrollPane (_quorumUsersJList);
        userScroll.setPreferredSize(new Dimension(250, 550));
        
        // Call sendMessage when the Submit Button is clicked
        _submitJButton.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent e) {
                sendMessage();
            }
        });

        // Call sendMessage when the Enter button is pressed
        _newMessageJTextField.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER)
                    sendMessage();
            }
        });
        
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(_quorumJLabel)
                .addGroup(layout.createParallelGroup()
                        .addComponent(chatScroll)
                        .addComponent(userScroll))
                .addGroup(layout.createParallelGroup()
                        .addComponent(_newMessageJTextField, GroupLayout.PREFERRED_SIZE, 
                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(_submitJButton)));
        
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap(15, 22)
                        .addComponent(_quorumJLabel))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(chatScroll)
                        .addComponent(userScroll))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(_newMessageJTextField)
                        .addComponent(_submitJButton)));
    }
    
    /**
     * Push the message to the client which orchestrates
     * the activities to send to the server.
     */
    private void sendMessage() {
        String message = _newMessageJTextField.getText();
        if (message != null && message.length() > 0) {
            _client.send("message " + _quorumId + " " + message);
            _newMessageJTextField.setText("");
        }
    }
}

