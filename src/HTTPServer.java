
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class HTTPServer {

    public static void main(String[] args) throws IOException {

        String[] request;
        String command;
        String url;
        int port;
        int httpVersion;

        Scanner scanner = new Scanner(System.in);

        while (args.length != 1) {
            System.out.println("Please enter exactly one argument, namely the port number: ");
            args = scanner.next().split(" ");
        }

        scanner.close();

        int portNumber = Integer.parseInt(args[0]);
        ServerSocket initialSocket = new ServerSocket(portNumber);

        while (true) {
            Socket socket = initialSocket.accept();
            if (socket != null) {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("Enter your command: ");
                request = inFromClient.readLine().split(" ");
                command = request[0];
                url = request[1];
                httpVersion = Integer.parseInt(request[2].substring(7, 8));
                ServerHTTP http = getHTTPversion(httpVersion, socket, command, url);
                Thread thread = new Thread(http);
                thread.start();
            }
        }
        //initialSocket.close();
    }

    private static ServerHTTP getHTTPversion(int number, Socket socket, String command, String url) throws IOException {
        if (number == 0) {
            return new ServerHTTP0(socket, command, url, number);
        } else if (number == 1) {
            return new ServerHTTP1(socket, command, url, number);
        } else {
            throw new IOException("HTTP version must be either 0 or 1");
        }
    }

}