package client;

import javax.swing.*;

import gui.*;

/**
 * 
 * @author thetrick
 * Main organizer as Gossip marries all the login window, main window and 
 * client together into a cohesive gui.
 */
public class Gossip {

    private final Login _login;
    private final Main _main;
    private Client _client = null;
    private Thread _thread;
    
    /**
     * Constructor
     */
    public Gossip() {
        this._main = new Main();
        this._login = new Login(this._main);
    }
    
    /**
     * When opening the Main Window, the Login Window is opened
     * which grabs a client.  Please note that the Login Window will not 
     * return until a client has been found...
     * Once, the user has logged in, then the Main Window will orchestrate
     * all activities between client/server. 
     */
    public void start() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                _main.pack();
                _main.setLocationRelativeTo(null);
                _main.setVisible(true);
                _client = _login.getClient();
                if (_client == null) {
                    System.out.println("Login window has been closed...");
                    _main.dispose();
                }
                else {
                    _main.setClient(_client);
                    _thread = new Thread()
                    {
                    	public void run()
                    	{
                    		_client.start(_main);
                    	}
                    };
                    _thread.start();
                }
            }
        });
    }
    
    public static void main(final String[] args) {
        Gossip gossip = new Gossip();
        gossip.start();
    }
}
