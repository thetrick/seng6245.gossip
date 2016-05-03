package gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.swing.*;
import javax.swing.text.BadLocationException;

import client.*;

/**
 * 
 * @author thetrick
 * The containing frame for the application's gui components.  Leverages 
 * Tabs for easy access to quorums, history, users, etc.  Offers
 * a menu to access chat history and logout features.
 */
public class Main extends JFrame {
	private static final long serialVersionUID = 1L;
	private final JTabbedPane _tabs;
    private final JMenuBar _menuBar;
    private final JMenu file;
    private final JMenuItem getHistory;
    private final JMenuItem logout;
    private final MainTab mainTab;
    private Client client = null;
    
    private final DefaultListModel _users;
    private final HashMap<String, QuorumClient> _quorumClientHistory;
    private final HashMap<String, QuorumClient> _quorumClients;
    private final DefaultListModel _quorums;
    private final DefaultListModel _historyList;
    
    public Main() {
        _menuBar = new JMenuBar();
        file = new JMenu("File");
        getHistory = new JMenuItem("History");
        logout = new JMenuItem("Logout");
        _users = new DefaultListModel();
        _quorums = new DefaultListModel();
        _quorumClientHistory = new HashMap<String,QuorumClient>();
        _quorumClients = new HashMap<String, QuorumClient>();
        _historyList = new DefaultListModel();
        this.setTitle("Gossip Chat");
        
        _menuBar.add(file);
        file.add(getHistory);
        file.add(logout);
        setJMenuBar(_menuBar);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        _tabs = new JTabbedPane();
        mainTab = new MainTab(this);
        _tabs.addTab("Home", mainTab);
        add(_tabs);
        
        getHistory.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                History history = new History(_historyList);
                addTab("History", history);
            }    
        });
        
        logout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.send("Disconnecting... " + client.getUsername());
                quit();
            }
        });
    }
    /**
     * Dynamically adds a tab which can be closed if needed
     * 
     * @param String - Identifies the tab to be added
     * @param JPanel - Tab to be added
     */
    public void addTab(String tabId, JPanel tab) {
        _tabs.addTab(tabId, tab);
        int i = _tabs.indexOfComponent(tab);
        if (i != -1) {
            QuorumClient quorumClient = _quorumClients.get(tabId);
            if (quorumClient == null) {
                if (tabId == "History") {
                    _tabs.setTabComponentAt(i, new CustomTab(null));
                }
                else {
                    System.err.println("Concurrency Problems, I see!");
                }
            }
            _tabs.setTabComponentAt(i, new CustomTab(quorumClient));
        }
    }
    
    /**
     * Represents a custom tab component that can be closed to be added as needed
     */
    private class CustomTab extends JPanel {

		private static final long serialVersionUID = 1L;
		private final JLabel name;
        private final QuorumClient _quorumClient;
        private final String tabName;
        
        private CustomTab(QuorumClient quorumClient) {
            this._quorumClient = quorumClient;
            if (quorumClient == null) {
                tabName = "History";
            }
            else {
                this.tabName = quorumClient.getQuorumId();
            }
            if (_tabs == null) {
                throw new NullPointerException("Tabbed Pane is null");   
            }
            setOpaque(false);
            
            name = new JLabel() {
				private static final long serialVersionUID = 1L;

				public String getText() {
                    return CustomTab.this.tabName;
                }
            };
            name.setPreferredSize(new Dimension(60, 15));
            
            add(name);
            
            //making the button
            ImageIcon close = makeIcon("crossoff.png");
            ImageIcon rolloverClose = makeIcon("crosson.png");
            
            System.out.println(close);
            JButton exit = new JButton(close);
            exit.setContentAreaFilled(false);
            exit.setPreferredSize(new Dimension(12, 12));
            exit.setFocusable(false);
            exit.setBorder(BorderFactory.createEtchedBorder());
            exit.setBorderPainted(false);
            exit.setRolloverIcon(rolloverClose);
            exit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    closeTab(CustomTab.this, CustomTab.this._quorumClient);
                }
            });
            add(exit);
        }
    }
    
    //Makes the icon for the button
    private ImageIcon makeIcon(String path) {
        URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("path couldn't be found: " + path);
            return null;
        }
    }
    
    //Sets the window's client
    //Also gets username from client to set a label in maintab
    public void setClient(Client c) {
        client = c;
        mainTab.setUsername(c.getUsername());
    }
    
    /**
     * Returns the particular tab given the string for the
     * chat name
     * @param chatName
     * @return The JPanel Tab with the given chatroom name
     */
    private JPanel findTab(String chatName) {
        for (int i = 0; i<_tabs.getTabCount(); i++) {
            String tabName = _tabs.getComponentAt(i).getName();
            if (tabName == chatName) {
                return (JPanel) _tabs.getComponentAt(i);
            }
        }
        return null;
    }
    

    /**
     * Adds a Message object to the appropriate QuorumClient, which will result in that
     * chatroom object's conversation being updated appropriately.
     * @param chatRoomName name of the chatroom the message is to be sent to
     * @param userName the user sending the message
     * @param message the actual message itself
     */
    public void updateConversation(String chatRoomName, String userName, String message) {
        final String c = chatRoomName;
        final String u = userName;
        final String m = message;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(_quorumClients.containsKey(c)){
                    QuorumClient roomCurrent = _quorumClients.get(c);
                    try {
                        roomCurrent.addRumor(new Rumor(u, m));
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }
    
    /**
     * Causes a JDialog to popup
     * @param errorMessage the error message to be displayed inside of the popup
     */
    public void displayErrorMessage(String errorMessage) {
        JOptionPane.showMessageDialog(this, errorMessage.toString(), "Error", JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Performs an update which adds a new list of users to underlying list of users on the server.
     * @param String[] - user that need to be added user list
     */
    public void updateUsers(String[] users) {
        final String[] list = users;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                _users.clear();
                for(int i = 0; i < list.length; i++) {
                    _users.addElement(list[i]);
                }
            }
        });
    }
    
    /**
     * Updates the DefaultListModel for the list of all chatrooms on the server
     * @param chats Array of chats to update the model wiht
     */
    public void updateMainChatList(String[] chats) {
        final String[] list = chats;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                System.out.println("Updating serverRoomList");
                _quorums.clear();
                for(int i = 0; i < list.length; i++) {
                    System.out.println("Adding room: " + list[i]);
                    _quorums.addElement(list[i]);
                }
            }
        });
    }
    
    /**
     * Updates the list of users inside of a chatroom
     * @param chatname Name of the chatroom to update
     * @param users ArrayList of users to update the chatroom with
     */
    public void updateChatUserList(String chatname, ArrayList<String> users) {
        final String c = chatname;
        final ArrayList<String> list = users;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (_quorumClients.containsKey(c)) {
                    QuorumClient roomCurrent = _quorumClients.get(c);
                    roomCurrent.updateUsers(list);
                }
            }
        });
    }
    /**
     * Makes sure that the local list of chats the user is a part of is the same as
     * the list of chats on the server. If the server returns a chatroom that the user
     * is not a part of, the client will send a message telling the server to disconnect
     * the user from that chatroom. If the server does not return a chatroom that the user
     * says they are connected to, a message will be sent to the server telling them to 
     * connect the user.
     * 
     * TODO: compare both ways, not just check if a room is in c
     * @param username The user who this list of chats corresponds to
     * @param chats The list of all chats the server thinks the user is connected to
     */
    public void updateUserChatList(String username, String[] chats) {
        final String u = username;
        final String[] list = chats;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (u.equals(client.getUsername())) {
                    System.out.println("Checking that chatlist is up to date");
                    for(int i = 0; i < list.length; i++) {
                        Set<String> localRooms= _quorumClients.keySet();
                        localRooms.toArray(new String[0]);
                        if(!_quorumClients.containsKey(list[i])) {
                            client.send("disconnect " + u);
                        }
                    }
                }
            }
        });
    }
    
    /**
     * Joins the appropriate chat by opening up a tab for it and adding a QuorumClient
     * object to _quorumClients. If the room has been joined before, that previous
     * QuorumClient object is added. Otherwise, a new QuorumClient is created.
     * @param chatname The name of the chatroom to be joined
     */
    public void joinQuorum(String chatname) {
        final String c = chatname;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(_quorumClientHistory.containsKey(c)) {
                    if(_quorumClients.containsKey(c)) {
                    } else {
                        QuorumClient chat = _quorumClientHistory.get(c);
                        _quorumClients.put(c, chat);
                        addTab(c, new Chat(c, Main.this));
                    }
                } else {
                        QuorumClient chat = new QuorumClient(c, client.getUsername());
                        _quorumClients.put(c, chat);
                        _quorumClientHistory.put(c, chat);
                        _historyList.addElement(chat);
                        addTab(c, new Chat(c, Main.this));
                }
            }
        });
    }
    
    /**
     * Removes the Kicks the user out of a chatroom. If the user isn't in the room, nothing
     * happens.
     * @param quorumId The name of the chatroom
     */
    public void leaveQuorum(String quorumId) {
        final String id = quorumId;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (_quorumClients.containsKey(id)) {
                    _quorumClients.remove(id);
                    JPanel removedQuorum = findTab(id);
                    if (removedQuorum != null) {
                        int tabIndex = _tabs.indexOfComponent(removedQuorum);
                        _tabs.remove(tabIndex);
                    }
                } 
            }
        });
    }
    
    /**
     * Kill the application.
     */
    public void quit() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dispose();
            }
        });
    }
    
    /**
     * Used to set the User and Quorums on the Main Window
     * @param JList - Users List
     * @param JList - Quorums List
     */
    public void setModelLists (JList users, JList quorums) {
        users.setModel(_users);
        quorums.setModel(_quorums);
    }
    
    /**
     * Used to close a tab. Removes the tab represented as a component
     * and disposes the associated QuorumClient associated with the tab
     * if it exists.  The only time a tab does not have an associated
     * QuorumClient is when the tab is the History Tab.
     * 
     * @param Component - The component associated with that tab
     * @param QuorumClient - The QuorumClient object associated with that tab.
     */
    private void closeTab(Component component, QuorumClient quorumClient) {
        int idx = _tabs.indexOfTabComponent(component);
        if (idx != -1) {
            if (quorumClient != null) {
                client.send("Exiting... " + quorumClient.getQuorumId());
                _quorumClients.remove(quorumClient.getQuorumId());
                quorumClient.getUsers().clear();
            }
            _tabs.remove(idx);
        }
    }

    public DefaultListModel getQuorums() {
        return this._quorums;
    }

    public DefaultListModel getUsersModel() {
        return this._users;
    }
    
    public QuorumClient getCurrentQuorum(String name) {
        return this._quorumClients.get(name);
    }
    
    public QuorumClient getHistory(String name) {
        return this._quorumClientHistory.get(name);
    }
    
    public Client getClient() {
        return client;
    }
}
