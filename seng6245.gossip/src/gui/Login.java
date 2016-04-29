package gui;

import client.*;
import javax.swing.*;

import java.awt.Font;
import java.beans.*;
import java.io.IOException;
import java.util.regex.Pattern;

public class Login extends JDialog implements PropertyChangeListener{
	private static final long serialVersionUID = 1L;
	private final JLabel welcome;
    private final JTextField username;
    private final JLabel usernameLabel;
    private final JTextField ipAddress;
    private final JLabel ipLabel;
    private final JTextField port;
    private final JLabel portLabel;
    private final JLabel errorMessage;
    private final JOptionPane loginPane;
    private String btnString = "Submit";
    private Client client = null;

    //Constructor for the login window. Takes in the parent frame that the popup opesn over
    public Login (JFrame parent) {
        super(parent, true); //turns on window modality
        welcome = new JLabel("Welcome to Complete Chat!");
        username = new JTextField(20);
        usernameLabel = new JLabel("Username");
        ipAddress = new JTextField(20);
        ipLabel = new JLabel("IP Address");
        port = new JTextField("10000", 20);
        portLabel = new JLabel("Port Number");
        Font errorFont = new Font("SANS_SERIF", Font.BOLD, 12);
        errorMessage = new JLabel();
        errorMessage.setFont(errorFont);
        JPanel panel = new JPanel();

        // defining the layout
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        //setting some margins around our components
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        //organizing components in this view
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(welcome)
                .addGroup(layout.createParallelGroup()
                        .addComponent(usernameLabel)
                        .addComponent(username, GroupLayout.PREFERRED_SIZE, 
                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup()
                        .addComponent(ipLabel)
                        .addComponent(ipAddress, GroupLayout.PREFERRED_SIZE, 
                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup()
                        .addComponent(portLabel)
                        .addComponent(port, GroupLayout.PREFERRED_SIZE, 
                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addComponent(errorMessage, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(welcome)
                .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup()
                                .addComponent(usernameLabel)
                                .addComponent(ipLabel)
                                .addComponent(portLabel))
                        .addGroup(layout.createParallelGroup()
                                .addComponent(username, GroupLayout.PREFERRED_SIZE, 
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(ipAddress, GroupLayout.PREFERRED_SIZE, 
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(port, GroupLayout.PREFERRED_SIZE, 
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                 .addComponent(errorMessage, 0, 10, 400)
                );
        
        Object[] button = {btnString};
        loginPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, button, button[0]);
        setContentPane(loginPane);
        
        loginPane.addPropertyChangeListener(this);
    }
    
    /**
     * Sets up the propertyChangeEvent on the login window so that it will make a client object
     * and then close. Or it will just close because the user closed it. 
     */
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        if (isVisible() && (e.getSource() == loginPane) && (JOptionPane.VALUE_PROPERTY.equals(prop) 
                || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = loginPane.getValue();
            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                return;
            }
            loginPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
            if (value == btnString) {
                if ((username.getText() == null) || username.getText().length() < 1) {
                    errorMessage.setText("<html>Error: Valid username required to login</html>");
                }
                else if ((ipAddress.getText() == null) || ipAddress.getText().length() < 1) {
                    errorMessage.setText("<html>Error: IP Address required to connect to server</html>");
                }
                else if ((port.getText() == null) || port.getText().length() < 1) {
                    errorMessage.setText("<html>Error: Port number required to connect</html>");
                }
                else {
                    if (isValidUsername(username.getText())) {
                        try {
                            client = new Client(username.getText(), ipAddress.getText(), 
                                    Integer.parseInt(port.getText()));
                            setVisible(false);
                        }
                        catch (IOException except) {
                            errorMessage.setText(except.getMessage());
                        }
                    }
                    else {
                        errorMessage.setText("<html>Error: Username can't exceed 20 characters<br> " +
                              "or contain any whitespace</html>");
                    }
                }
                pack();
            }
            else {//User closed the dialog
                setVisible(false);
            }
        }
    }   
    
    //Makes sure that the username is valid in terms of syntax
    private boolean isValidUsername(String Username) {
        String regex = "\\p{Graph}+";
        if (Pattern.matches(regex, Username) && Username.length() < 20) {
            return true;
        }
        return false;
    }
    
    /**
     * Launches the login window and then returns the client object created by the
     * window's input. If the user closes the window, null will be returned, prompting
     * the application to shutdown.
     * @return Client object to be used by the gui to talk to the server
     */
    public Client getClient() {
        client = null; //reset client to make sure it starts as null
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
        return client;
    }
}
