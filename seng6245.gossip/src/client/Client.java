package client;

import gui.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author thetrick
 * Orchestrates the activities necessary between the server and the user to obtain a connection
 */
public class Client {
    private final String username;
    private final Socket socket;
    private final PrintWriter _print;
    private final BufferedReader _buffer;    

    /**
     * Constructor.  
     * @param username - Identifies the user making the connection
     * @param IPAddress 
     * @param port
     * @throws IOException If the username is invalid or if logging
     *      in was unsuccessful
     */
    public Client(String username, String IPAddress, int port) throws IOException {
        this.username = username;
        try
		{
			this.socket = new Socket(IPAddress, port);
		}
		catch (Exception e)
		{
			throw new IOException("No host...");
		}
        
        System.out.println("Server has been found...");

        this._buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this._print = new PrintWriter(socket.getOutputStream());
        System.out.println("Stream processing has been enabled...");

        System.out.println("Starting the handshake...");
        String prompt = _buffer.readLine();

        System.out.println("Verification has started");
        if (!prompt.equals("To connect type: \"connect [username]\""))
            throw new IOException("Bad Handshake");
        System.out.println("Handshake Passed...");

        System.out.println("Sending Username");
        _print.println("connect " + this.username);
        _print.flush();

        System.out.println("Verifying Username...");
        //prompt = _buffer.readLine();
        //if (!prompt.matches("Connected"))
        //    throw new IOException(prompt);

        //System.err.println("Client connected");
    }

    /**
     * Reads the next line from the input buffer
     * @return The next line String from the input buffer
     * @throws IOException Disconnected from server
     */
    public String readBuffer() throws IOException {
        try {
            return _buffer.readLine();
        } catch (IOException e) {
            throw new IOException("Disconnected from Server");
        }
    }

    /**
     * Sends the output String
     * @param output String to be sent to the server
     */
    public void send(String output) {
        _print.println(output);
        _print.flush();
        System.out.println(output);
        return;
    }
    
    /**
     * @return The username that is using the client
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * The thread to begin the loop reading from the input and sends
     * it to the parser
     * @param main
     */
    public void start(Main main) {
        try {
            System.out.println("About to start loop");
            for(String input = _buffer.readLine(); input!=null; input = _buffer.readLine()) {
                System.out.println("Looping");
                if(input.equals("disconnectedServerSent"))
                	break;
                parseInput(input, main);
            }
        } catch(IOException e) {
            main.quit();
        } finally {
        	try
			{
            	_buffer.close();
            	_print.close();
				socket.close();
			}
			catch (IOException ignore)
			{
			}
        }
        System.err.println("client consumer terminated....");
    }
    
    /**
     * Parses the input and calls the correct command by using
     * the grammar specified
     * @param input The String input to be parsed
     * @param main The Main
     */
    private void parseInput(String input, Main main) {
        System.out.println(input);
        
        int firstSpaceIndex = input.indexOf(' ');
        String command;
        if (firstSpaceIndex ==  -1) {
            command = input;
            firstSpaceIndex = input.length()-1;
        }
        else
            command = input.substring(0, firstSpaceIndex);
        
        if(command.equals("message")) {
            int secondSpaceIndex = input.indexOf(' ', firstSpaceIndex+1);
            int thirdSpaceIndex = input.indexOf(' ', secondSpaceIndex+1);
            String chatRoomName = input.substring(firstSpaceIndex + 1, secondSpaceIndex);
            String userName = input.substring(secondSpaceIndex + 1, thirdSpaceIndex);
            String message = input.substring(thirdSpaceIndex + 1);
            main.updateConversation(chatRoomName, userName, message);
        } else if (command.equals("invalidRoom")) {
            int secondSpaceIndex = input.indexOf(' ', firstSpaceIndex+1);
            String errorMessage = input.substring(secondSpaceIndex + 1);
            main.displayErrorMessage(errorMessage);
            
        } else {
            String[] info = input.substring(firstSpaceIndex+1).split(" ");
            if(command.equals("serverUserList")) {
                main.updateUsers(info);
                
            } else if(command.equals("serverRoomList")) {
                main.updateMainChatList(info);

            } else if(command.equals("chatUserList")) {
                String chatname = info[0];
                ArrayList<String> newChatList = new ArrayList<String>();
                for (int i = 1; i < info.length; i++) {
                    System.err.println("adding: " + info[i]);
                    newChatList.add(info[i]);
                }
                main.updateChatUserList(chatname, newChatList);

            } else if(command.equals("clientRoomList")) {
                String username = info[0];
                String[] chats = Arrays.copyOfRange(info, 1, info.length);
                main.updateUserChatList(username, chats);
                
            } else if(command.equals("connectedRoom")) {
                String chatname = info[0];
                main.joinQuorum(chatname);

            } else if(command.equals("disconnectedRoom")) {
                String chatname = info[0];
                main.leaveQuorum(chatname);

            }  else {
                System.err.println("Derp we seem to have ended up in dead code");
            }
        }
    }

    // just a method to test this rig out; isn't used in the gui
    public static void main(String[] args) {
        try {
            Client client = new Client("user2", "127.0.0.1", 25252);
            while (true) {
                System.out.println(client.readBuffer());
            }
        } catch (IOException e) {
            System.err.println(e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
}
