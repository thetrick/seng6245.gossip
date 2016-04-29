package client;

import gui.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Client {
    private final String username;
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;    

    /**
     * Constructor for a client.  Performs the handshake between
     * the server and the user to achieve a valid connection and
     * a valid client.
     * @param username
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
			throw new IOException("Could not resolve host");
		}
        System.err.println("Connected to server");

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream());
        System.err.println("IO Streams enabled");

        System.err.println("Waiting for Handshake");
        String prompt = in.readLine();

        System.err.println("Verifying Handshake");
        if (!prompt.equals("To connect type: \"connect [username]\""))
            throw new IOException("Server returned invalid handshake");
        System.err.println("Handshake Passed");

        System.err.println("Sending Username");
        out.println("connect " + this.username);
        out.flush();

        System.err.println("Verifying Username");
        prompt = in.readLine();
        if (!prompt.matches("Connected"))
            throw new IOException(prompt);

        System.err.println("Client connected");
    }

    /**
     * Reads the next line from the input buffer
     * @return The next line String from the input buffer
     * @throws IOException Disconnected from server
     */
    public String readBuffer() throws IOException {
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new IOException("Disconnected from Server");
        }
    }

    /**
     * Sends the output String
     * @param output String to be sent to the server
     */
    public void send(String output) {
        out.println(output);
        out.flush();
        System.err.println(output);
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
            for(String input = in.readLine(); input!=null; input = in.readLine()) {
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
            	in.close();
            	out.close();
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
                main.updateMainUserList(info);
                
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
                main.joinChat(chatname);

            } else if(command.equals("disconnectedRoom")) {
                String chatname = info[0];
                main.leaveChat(chatname);

            }  else {
                System.err.println("Derp we seem to have ended up in dead code");
            }
        }
    }

    // just a method to test this rig out; isn't used in the gui
    public static void main(String[] args) {
        try {
            Client c = new Client("user2", "127.0.0.1", 10000);

            while (true)
                System.out.println(c.readBuffer());
        } catch (IOException e) {
            System.err.println(e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
}
