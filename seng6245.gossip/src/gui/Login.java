package gui;

import client.*;
import javax.swing.*;

import java.awt.Font;
import java.beans.*;
import java.io.IOException;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * 
 * @author thetrick
 *
 */
public class Login extends JDialog implements PropertyChangeListener{
	private static final long serialVersionUID = 1L;
	private final JLabel _InitialJLabel;
    private final JTextField _userJTextField;
    private final JLabel _userJLabel;
    private final JTextField _ipJTextField;
    private final JLabel _ipJLabel;
    private final JTextField _portJTextField;
    private final JLabel _portJLabel;
    private final JLabel errJLabel;
    private final JOptionPane _loginJOptionPane;
    private String _submitText = "Submit";
    private Client _client = null;

    //Constructor. 
    // @param JFrame - container parent frame
    public Login (JFrame parent) {
        super(parent, true);
        Random rng = new Random();
        String userName = "user" + rng.nextInt(25);
        _InitialJLabel = new JLabel("Are you ready for some Gossip?");
        _userJTextField = new JTextField(userName, 20);
        _userJLabel = new JLabel("Username");
        _ipJTextField = new JTextField("127.0.0.1", 20);
        _ipJLabel = new JLabel("IP Address");
        _portJTextField = new JTextField("25252", 20);
        _portJLabel = new JLabel("Port Number");
        Font errorFont = new Font("SANS_SERIF", Font.BOLD, 12);
        errJLabel = new JLabel();
        errJLabel.setFont(errorFont);
        JPanel panel = new JPanel();

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(_InitialJLabel)
                .addGroup(layout.createParallelGroup()
                        .addComponent(_userJLabel)
                        .addComponent(_userJTextField, GroupLayout.PREFERRED_SIZE, 
                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup()
                        .addComponent(_ipJLabel)
                        .addComponent(_ipJTextField, GroupLayout.PREFERRED_SIZE, 
                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup()
                        .addComponent(_portJLabel)
                        .addComponent(_portJTextField, GroupLayout.PREFERRED_SIZE, 
                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addComponent(errJLabel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(_InitialJLabel)
                .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup()
                                .addComponent(_userJLabel)
                                .addComponent(_ipJLabel)
                                .addComponent(_portJLabel))
                        .addGroup(layout.createParallelGroup()
                                .addComponent(_userJTextField, GroupLayout.PREFERRED_SIZE, 
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(_ipJTextField, GroupLayout.PREFERRED_SIZE, 
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(_portJTextField, GroupLayout.PREFERRED_SIZE, 
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                 .addComponent(errJLabel, 0, 10, 400)
                );
        
        Object[] button = {_submitText};
        _loginJOptionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, button, button[0]);
        setContentPane(_loginJOptionPane);
        
        _loginJOptionPane.addPropertyChangeListener(this);
    }
    
    /**
     * Sets up the propertyChangeEvent on the login window so that it will make a client object
     * and then close. Or it will just close because the user closed it. 
     */
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        if (isVisible() && (e.getSource() == _loginJOptionPane) && (JOptionPane.VALUE_PROPERTY.equals(prop) 
                || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = _loginJOptionPane.getValue();
            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                return;
            }
            _loginJOptionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
            if (value == _submitText) {
                if ((_userJTextField.getText() == null) || _userJTextField.getText().length() < 1) {
                    errJLabel.setText("<html>Required: Username!</html>");
                }
                else if ((_ipJTextField.getText() == null) || _ipJTextField.getText().length() < 1) {
                    errJLabel.setText("<html>Required: IP Address!</html>");
                }
                else if ((_portJTextField.getText() == null) || _portJTextField.getText().length() < 1) {
                    errJLabel.setText("<html>Required: Port Number!</html>");
                }
                else {
                    if (isValidUsername(_userJTextField.getText())) {
                        try {
                            _client = new Client(_userJTextField.getText(), _ipJTextField.getText(), 
                                    Integer.parseInt(_portJTextField.getText()));
                            setVisible(false);
                        }
                        catch (IOException except) {
                            errJLabel.setText(except.getMessage());
                        }
                    }
                    else {
                        errJLabel.setText("<html>Invalid: Username cannot be greater than 20 characters<br> " +
                              "or contain any whitespace</html>");
                    }
                }
                pack();
            }
            else {
                setVisible(false);
            }
        }
    }   
    
    /**
     * performs validation on the username entered
     * @param Username - entered by user
     * @return boolean indicating if the user name entered is valid.
     */
    private boolean isValidUsername(String Username) {
        String regex = "\\p{Graph}+";
        if (Pattern.matches(regex, Username) && Username.length() < 20) {
            return true;
        }
        return false;
    }
    
    /**
     * Fires up the Login Window so that the user can login and connect to the server.
     * Main activity includes firing up the client object.
     * @return The client object to be used by the Main Window to talk with server
     */
    public Client getClient() {
        _client = null;
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
        return _client;
    }
}
