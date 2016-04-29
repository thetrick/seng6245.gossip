package client;

import javax.swing.*;

import gui.*;

/**
 * Complete chat organizes the _loginWindow, the _mainWindow,
 * and the Client all together.  The complete chat begins
 * the entire client workable from the GUI.
 *
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
        _main = new Main();
        _login = new Login(this._main);
    }
    
    /**
     * The start method starts the _main window which opens
     * the _login window dialog which does not return until
     * a valid client is found.  Attempts to make a client
     * will continue until one is found.  Then the _main
     * window will begin and the input/output stream between
     * client and server will continue properly.
     */
    public void start() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                _main.pack();
                _main.setLocationRelativeTo(null);
                _main.setVisible(true);
                _client = _login.getClient();
                if (_client == null) {
                    System.out.println("closed _login window");
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
