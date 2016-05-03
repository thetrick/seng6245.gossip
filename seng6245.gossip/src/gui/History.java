package gui;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.*;
import javax.swing.event.*;

import client.QuorumClient;

/**
 * 
 * @author thetrick
 * History contains past chats completed by the user. Provides Gui
 * representation of all Quorums the user has joined.  When selecting
 * a Quorum, the history is updated. As long as the user is connected
 * to a Quorum, the history is updated to log all activity.  Any time
 * a user reconnects to chatroom, the history should be maintained.
 */
public class History extends JPanel{
	private static final long serialVersionUID = 1L;
	private final JLabel _historyJLabel;
    private final JTextPane _historyJTextPane;
    private final JList _historyJList;
    
    /**
     * Constructor
     * @param history - represents the history of any conversations matching the QuorumClient.
     */
    public History(DefaultListModel history) {
        Font TitleFont = new Font("SANS_SERIF", Font.BOLD, 18);
        _historyJLabel = new JLabel("History");
        _historyJLabel.setFont(TitleFont);
        _historyJTextPane = new JTextPane();
        _historyJList = new JList(history);
        setName("History");
        
        
        _historyJTextPane.setEditable(false);
        JScrollPane convoScroll = new JScrollPane (_historyJTextPane);
        convoScroll.setPreferredSize(new Dimension(700, 550));
        JScrollPane chatScroll = new JScrollPane (_historyJList);
        chatScroll.setPreferredSize(new Dimension(250, 550));
        _historyJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        _historyJList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && _historyJList.getSelectedValue() != null) {
                    QuorumClient chatroom = (QuorumClient) _historyJList.getSelectedValue();
                    _historyJTextPane.setStyledDocument(chatroom.getDefaultStyledDocument());
                }
            }
        });
        
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(_historyJLabel)
                .addGroup(layout.createParallelGroup()
                        .addComponent(convoScroll)
                        .addComponent(chatScroll)));
        
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap(15, 22)
                        .addComponent(_historyJLabel))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(convoScroll)
                        .addComponent(chatScroll)));
    }
}
