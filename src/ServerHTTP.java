
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;

public abstract class ServerHTTP implements Runnable {

    final String ROOT = "localhost";
    final String NAME = "putpostfile";
    protected Socket socket;
    protected String command;
    protected String url;
    protected Path filePath;
    String domain = ROOT;
    StatusCodes statusCode;
    DataOutputStream outToClient;
    BufferedReader inFromClient;
    BufferedReader reader;
    protected File ppfile;

    String[] request;
    int httpVersion;

    public ServerHTTP(Socket socket, String command, String url, int httpVersion) throws IOException {
        this.command = command;
        this.url = url;
        this.httpVersion = httpVersion;
        this.socket = socket;

        //this.filePath = FileSystems.getDefault().getPath(domain, this.NAME); //TODO klopt dit?
        this.filePath = FileSystems.getDefault().getPath("/home/kommusoft/Projects/javaServer/javaserver/src/data.html");
        this.outToClient = new DataOutputStream(this.socket.getOutputStream());
        initializePPFile(this.ROOT, this.NAME);
    }

    private void initializePPFile(String location, String fileName) {
        ppfile = new File(location, fileName + ".txt"); //TODO klopt dit?
    }

    public abstract void run();

    public void handleRequest() throws IOException {
        String response = "";
        switch (this.command) {
            case "GET":
                if (Files.exists(this.filePath)) {
                    this.statusCode = new StatusCodes(200, "path exists");
                    outToClient.writeUTF("HTTP/1.0 200 OK\r\n\r\n");
                    byte[] data = processGet(this.filePath);
                    this.outToClient.write(data);
                    this.outToClient.writeBytes("\n\n");
                    this.outToClient.flush();
                    break;
                } else {
                    this.statusCode = new StatusCodes(404, "invalid path name");
                }
                break;
            case "HEAD":
                if (Files.exists(this.filePath)) {
                    this.statusCode = new StatusCodes(200, "path exists");
                    response += getHead(this.filePath);
                    this.outToClient.writeBytes(response);
                    break;
                } else {
                    this.statusCode = new StatusCodes(404, "invalid path name");
                }
                break;
            case "PUT":
                ArrayList<String> body = retrieveBody(this.inFromClient);
                if (body == null) {
                    this.statusCode = new StatusCodes(400, "invalid input");
                } else {
                    writeToPPLog("PUT", this.ppfile, body);
                    this.statusCode = new StatusCodes(200, "PUT-command written to log");
                }
                break;
            case "POST":
                ArrayList<String> msg = retrieveBody(this.inFromClient);
                if (msg == null) {
                    this.statusCode = new StatusCodes(400, "invalid input");
                } else {
                    writeToPPLog("POST", this.ppfile, msg);
                    this.statusCode = new StatusCodes(200, "POST-command written to log");
                }
                break;
            default:
                this.statusCode = new StatusCodes(404, "invalid command");
        }
    }

    private void writeToPPLog(String command, File file, ArrayList<String> msg) throws IOException {
        PrintWriter printer = new PrintWriter(new BufferedWriter(new FileWriter(file.getAbsolutePath())));
        //String bodyStr = body.toString();
        String toWrite = command + msg;//bodyStr; //TODO mag dit zomaar? moet je niet loopen over de elementen van de AL?
        printer.write(toWrite);
        printer.close();
    }

    private ArrayList<String> retrieveBody(BufferedReader input) throws IOException {
        String nextLine = input.readLine();
        ArrayList<String> body = new ArrayList<String>(); //TODO welke grootte?
        while (nextLine != null) {
            body.add(nextLine);
        }
        return body;
    }

    @SuppressWarnings("deprecation")
    private String getHead(Path filePath2) throws IOException {
        Date date = new Date();
        String response = "Date: " + date.toString().substring(0, 3) + " " + date.toGMTString() + "\n"; //TODO nakijken!
        response += "Content-Type: " + Files.probeContentType(filePath2) + "\n";
        response += "Content-Length: " + Files.size(filePath2);
        //TODO kijken of last request?
        return response;
    }

    private byte[] processGet(Path filePath) throws IOException {
        return Files.readAllBytes(filePath);
    }

}