import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;


public class HTTP1 extends HTTP{
	
	/**
	 * Initialize the HTTP-version to HTTP version 1.1 with the given command, given url and given port.
	 * 
	 * @param command
	 * 		The command that needs to be handled.
	 * @param url
	 * 		The url on which the given command needs to be executed.
	 * @param port
	 * 		The port that needs to be accessed.
	 */
	public HTTP1(String command, String url, int port){
		super(command, url, port);	
		this.socket = null;
	}

	/**
	 * Is called everytime a connection to a server is needed.
	 * In the case of HTTP1.0 there should be a new socket everytime this happens.
	 */
	@Override
	public void setSocket() throws UnknownHostException, IOException{
		//Initialize a new socket and all communication attributes.
		if(this.socket == null){
			this.socket = new Socket(this.url, this.port);
			this.outToServer = new DataOutputStream(this.socket.getOutputStream());
	        //this.inFromServer = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
	        this.dataInFromServer = new DataInputStream(socket.getInputStream());
		}
	}
	
	/**
	 * Send the given sentence (command, URI and HTTP-version to the server. 
	 */
	public void sendSentence(String sentence) throws IOException{
		this.outToServer.writeBytes(sentence + "\n");
		//outToServer.writeBytes("Connection: Keep-Alive");//TODO werkt?
		outToServer.writeBytes("Host: " + this.url + "\n");
	}
	
	
	/**
	 * Return the HTTP version that is used.
	 * 
	 * @return The HTTP version that is used.
	 */
	@Override
	protected int getHttpVersion() {
		return 1;
	}

}