package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.util.regex.Pattern;

import javax.swing.*;

/**
 * 
 * @author thetrick
 * Represents the main activity tab for Gossip. Presents the user a list of all users and 
 * all Quorums.  The user has the ability to create his own quorum.
 */
public class MainTab extends JPanel{
	
	private static final long serialVersionUID = 1L;
	private final JLabel mainJLabel;
	private final JLabel initialJLabel;
    private final JButton newQuorumButton;
    private final JLabel quorumJLabel;
    private final JList quorums;
    private final JLabel userLabel;
    private final JList userList;
    private final Main main;
    
    public MainTab(Main main) {
        Font TitleFont = new Font("SANS_SERIF", Font.BOLD, 24);
        Font LabelFont = new Font("SANS_SERIF", Font.BOLD, 16);
        mainJLabel = new JLabel("Welcome to Gossip!");
        mainJLabel.setFont(TitleFont);
        newQuorumButton = new JButton("New Quorum");
        quorumJLabel = new JLabel("List of Quorums");
        quorumJLabel.setFont(LabelFont);
        quorums = new JList(new DefaultListModel());
        userLabel = new JLabel("List of Users");
        userLabel.setFont(LabelFont);
        userList = new JList(new DefaultListModel());
        setName("Home");
        this.main = main;
        main.setModelLists(userList, quorums);
        initialJLabel = new JLabel();
        
        quorums.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane chatScroll = new JScrollPane (quorums);
        chatScroll.setPreferredSize(new Dimension(400, 400));
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane userScroll = new JScrollPane (userList);
        userScroll.setPreferredSize(new Dimension(150, 400));
        
        newQuorumButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String newChat = JOptionPane.showInputDialog(MainTab.this, "Name for your new quorum:", 
                        "Create New Quorum", JOptionPane.PLAIN_MESSAGE);
                if (newChat.length() > 0) {
                    if (validQuorum(newChat)) {
                        MainTab.this.main.getClient().send("make " + newChat);
                    }
                    else {
                        JOptionPane.showMessageDialog(MainTab.this, "Error: Quorum cannot be longer than 40 characters " +
                                "or contain any whitespace", "Error", JOptionPane.WARNING_MESSAGE);
                    }
                }
                else {
                    newQuorumButton.doClick();
                }
            }
        });
        
        quorums.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && quorums.getSelectedValue() != null) {
                    String chatName = (String)quorums.getSelectedValue();
                    MainTab.this.main.getClient().send("join " + chatName);
                }
            }
        });
        
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(mainJLabel)
                .addComponent(initialJLabel)
                .addComponent(newQuorumButton)
                .addGroup(layout.createParallelGroup()
                        .addComponent(userLabel)
                        .addComponent(quorumJLabel))
                .addGroup(layout.createParallelGroup()
                        .addComponent(userScroll)
                        .addComponent(chatScroll))
                );
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(mainJLabel)
                .addComponent(initialJLabel)
                .addComponent(newQuorumButton)
                .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup()
                                .addComponent(quorumJLabel)
                                .addComponent(chatScroll))
                        .addGroup(layout.createParallelGroup()
                                .addComponent(userLabel)
                                .addComponent(userScroll)
                                )
                        )
                );       
    }
    
    //Checks to make sure a chatname is valid
    private boolean validQuorum(String quorum) {
        String regex = "\\p{Graph}+";
        if (Pattern.matches(regex, quorum) && quorum.length() < 40) {
            return true;
        }
        return false;
    }
    
    public void setUsername(String username) {
        Font welcomeFont = new Font("MONOSPACED", Font.PLAIN, 13);
        initialJLabel.setText("Now gossiping... " + username);
        initialJLabel.setFont(welcomeFont);
        initialJLabel.setForeground(Color.MAGENTA);
    }
}

