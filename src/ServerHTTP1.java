
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ServerHTTP1 extends ServerHTTP {

    String[] request;
    String[] connectionCloseHeader = new String[2];

    public ServerHTTP1(Socket socket, String command, String url, int httpVersion) throws IOException {
        super(socket, command, url, httpVersion);
        connectionCloseHeader[0] = "Connection: ";
        connectionCloseHeader[1] = "close";
    }

    @Override
    public void run() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                BufferedReader inFromClient;
                try {
                    inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    //TODO hierin opnieuw vragen naar requests
                    while (true) {
                        request = inFromClient.readLine().split(" ");
                        if (request != null && (request[0] != connectionCloseHeader[0] && request[1] != connectionCloseHeader[1])) { //TODO testen!
                            command = request[0];
                            url = request[1];
                            //port = Integer.parseInt(request[2]);
                            httpVersion = Integer.parseInt(request[3].substring(7, 8));
                            if (httpVersion == 0) {
                                throw new IOException("You are currently working under HTTP version 1.1. Version 1.0 is not allowed in this session!");
                            }
                            handleRequest();
                        } else if (request == null) {
                            statusCode = new StatusCodes(400, "no header detected");
                        } else {
                            socket.close();
                        }
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }, 2 * 60 * 1000); //run the timer during 2 minutes
        try {
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
	//TODO host teruggeven!

}