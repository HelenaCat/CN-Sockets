
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerHTTP0 extends ServerHTTP {
    
    public ServerHTTP0(Socket socket, String command, String url, int httpVersion) throws IOException {
        super(socket, command, url, httpVersion);
    }
    
    @Override
    public void run() {
        try {
            handleRequest();
            this.socket.close();
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
}