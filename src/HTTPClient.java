
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HTTPClient {

    /**
     * Main method to set up HTTPClient.
     *
     * @param args[0] = command The HTTP-command that needs to be executed.
     * @param args[1] = URI The URI to get data from.
     * @param args[2] = Port The port to connect to.
     * @param args[3] = HTTPVersion The version of HTTP that is used.
     */
    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        try {
            //Check to see if there are 4 arguments (command, URI, the port to connect to and the HTTP-version) 
            while (args.length != 4) {
                System.out.println("Give 4 arguments please!");
                args = br.readLine().split(" ");
            }
            br.close();
        } //If the given input is invalid, throw an IOException.
        catch (IOException e) {
            //TODO hoe catchen?
            System.out.println("The given input was invalid. Closing.");
            e.printStackTrace();
            System.exit(0);
        }

        //Initialize the command, URL and port to the input received from the console.
        String command = args[0];
        String url = args[1];
        int port = Integer.parseInt(args[2]);
        int httpVersion = Integer.parseInt(args[3].substring(7, 8));

        //Set the HTTP-version according to the version that was entered in the console.
        HTTP http = null;
        if (httpVersion == 0) {
            http = new HTTP0(command, url, port);
        } else if (httpVersion == 1) {
            http = new HTTP1(command, url, port);
        }

        http.sendRequest();
        try {
            http.handleResponse();
        } catch (IOException e) {
            //TODO deftige catch
            e.printStackTrace();
        }
    }

}