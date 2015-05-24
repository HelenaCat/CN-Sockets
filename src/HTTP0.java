
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class HTTP0 extends HTTP {

    /**
     * Initialize the HTTP-version to HTTP version 1.0 with the given command,
     * given url and given port.
     *
     * @param command The command that needs to be handled.
     * @param url The url on which the given command needs to be executed.
     * @param port The port that needs to be accessed.
     */
    public HTTP0(String command, String url, int port) {
        super(command, url, port);
    }

    /**
     * Is called every time a connection to a server is needed. In the case of
     * HTTP1.0 there should be a new socket every time this happens.
     */
    @Override
    public void setSocket() throws UnknownHostException, IOException {
        //If a socket is already running, close it before creating a new one.
        if (socket != null) {
            outToServer.close();
            dataInFromServer.close();
            socket.close();
        }
        //Create a new socket and all the needed attributes.
        socket = new Socket(url, port);
        outToServer = new DataOutputStream(socket.getOutputStream());
        dataInFromServer = new DataInputStream(socket.getInputStream());
    }

    /**
     * Send the given sentence (command, URI and HTTP-version to the server.
     */
    public void sendSentence(String sentence) throws IOException {
        outToServer.writeBytes(sentence);
        outToServer.writeBytes("\n");
    }

    /**
     * Return the HTTP-version that is used.
     *
     * @return The HTTP-version that is used.
     */
    @Override
    protected int getHttpVersion() {
        return 0;
    }
}