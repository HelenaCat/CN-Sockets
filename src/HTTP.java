
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.imageio.ImageIO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Super class for HTTP.
 */
public abstract class HTTP {

    protected String command; //HEAD-GET-PUT-POST
    protected String url; //[www.]example.com
    protected String path; //everything that comes after the '.com'
    protected int port;
    protected Socket socket;
    protected DataOutputStream outToServer;
    protected DataInputStream dataInFromServer;

    private final String retrievedPagesFileName = "retrievedPages.txt";
    protected Map<String, String> retrievedPages; //a map that has the url of pages retrieved as the keys, and as value the date & time at which it was retrieved

    /**
     * Initialize the HTTP-version with the given command, given URL and given
     * port.
     *
     * @param command The command that needs to be handled.
     * @param givenURL The URL to which the command that needs to be handled
     * applies.
     * @param port The port that needs to be accessed.
     */
    public HTTP(String command, String givenURL, int port) {
        this.command = command;
        this.port = port;
        this.url = givenURL.split("/")[0];

        //Check if a path is given, if it is not, the path is set to the empty string.
        if (givenURL.split("/").length < 2) {
            this.path = "";
        } //If a path is given, set the given path to the given path.
        else {
            this.path = givenURL.replace(url, "");
            this.path = (String) this.path.subSequence(1, this.path.length() - 1);
        }
        this.readRetrievedPages();
    }

    private void readRetrievedPages() {
        retrievedPages = new HashMap<String, String>();
        File retrievedPagesFile = new File(retrievedPagesFileName);
        try (BufferedReader br = new BufferedReader(new FileReader(retrievedPagesFile))) {
            String line;
            String[] values;
            while ((line = br.readLine()) != null) {
                values = line.split(";");
                retrievedPages.put(values[0], values[1]);
            }
            br.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void writeToRetrievedPages(String url, String date) throws FileNotFoundException {
        File retrievedPagesFile = new File(retrievedPagesFileName);
        PrintWriter writer = new PrintWriter(new FileOutputStream(retrievedPagesFile, true));
        writer.append(url + ";" + date + "\n");
        writer.close();
    }

    /**
     * Send the command to the chosen HTTP version with the given path.
     */
    public void sendRequest() {
        //Try to initialize the socket and send the command to the server.
        try {
            setSocket();

            String sentence = this.command + " /" + this.path + " HTTP/1." + this.getHttpVersion(); //sentence is wat naar de server gestuurd moet worden
            sendSentence(sentence);
        } //If the given host is not found, throw an UnknownHostException.
        catch (UnknownHostException e) {
            System.out.println("No HTTP server found on this host.");
        } //If the port number is incorrect, throw an IOException.
        catch (IOException e) {
            System.out.println("No server found on this port number.");
        }
    }

    public abstract void setSocket() throws UnknownHostException, IOException;

    public abstract void sendSentence(String sentence) throws IOException;

    /**
     * Handle the sent responses accordingly.
     */
    public void handleResponse() throws IOException {
        switch (this.command) {
            //If the command is a HEAD-request, pass the request to the HEAD-handler.
            case "HEAD":
                outToServer.writeBytes("\n");
                handleHeadResponse();
                break;
            //If the command is a GET-request, pass the request to the GET-handler.
            case "GET":
                //nog geen 2e 'newline' --> als we if-modified-since moeten opvragen moet dat er nog tussen
                handleGetResponse();
                break;
            //If the command is a PUT-request, pass the request to the PUT- & POST-handler.
            case "PUT":
                outToServer.writeBytes("\n");
                handlePutPostResponse();
                break;
            //If the command is a POST-request, pass the request to the PUT- & POST-handler.
            case "POST":
                outToServer.writeBytes("\n");
                handlePutPostResponse();
                break;
        }
        //Close the socket and the in- and outputstream to and from the server. Close the data connection with the server.
        socket.close();
        outToServer.close();
        dataInFromServer.close();
    }

    /**
     * Handle the HEAD-response.
     *
     * @throws IOException When the writer receives invalid input, throw
     * IOException.
     */
    protected void handleHeadResponse() throws IOException {
        ArrayList<String> response = readHeader();
        for (String responseLine : response) {
            System.out.println(responseLine);
        }
    }

    /**
     * Handle the GET-response.
     *
     * @throws IOException If the writer receives invalid input, throw
     * IOException.
     */
    protected void handleGetResponse() throws IOException {
        //Split the URL at dots so that the name of the site can be used as the file name where the HTML-file will be saved.
        String siteURL = url.split("\\.")[0];
        if (siteURL.equals("www")) {
            siteURL = url.split("\\.")[1];
        }

        //Create the file where the HTML-page will be saved.
        File f = null;
        if (this.path.equals("")) {
            f = new File(siteURL + ".html");
        } else {
            f = new File(siteURL + "/" + this.path);
        }

        //If the file has been retrieved already, send the If-Modified-Since header
        if (retrievedPages.containsKey(siteURL)) {
            System.out.println("try if-modified-since"); //TODO check
            outToServer.writeBytes("If-Modified-Since: " + retrievedPages.get(siteURL) + "\n");
        }
        outToServer.writeBytes("\r\n");
        //outToServer.writeBytes("\n");

        ArrayList<String> header = readHeader();

        //if the page has been modified since the last time we retrieved it, or we haven't retrieved it previously
        if (!getFromHeader(header, "HTTP").contains("302")) {
            readHtmlPage(f);
            getImages(f);
            writeToRetrievedPages(siteURL, getFromHeader(header, "Date"));
        }
    }

    protected ArrayList<String> readHeader() throws IOException {
        ArrayList<String> header = new ArrayList<String>();

        byte[] b = new byte[1];
        String currentString = "";
        String newChar;
        int line;

        while ((line = this.dataInFromServer.read()) != -1) {
            newChar = "" + (char) line;
            if (newChar.contains("\n") || newChar.contains("\r")) {
                if ((header.size() == 0 && !currentString.equals("")) || header.size() != 0) {
                    header.add(currentString);
                }
                currentString = "";
            } else {
                currentString += newChar;
            }
            if (header.size() > 2 && (header.get(header.size() - 1).equals("") && header.get(header.size() - 2).equals(""))) {
                break;
            }
        }

        if (header.isEmpty()) {
            System.out.println("No response was retrieved");

        }

        //in geval dat er nog rommel staat voor het begin van de header
        while (!header.isEmpty() && !header.get(0).contains("HTTP")) {
            header.remove(0);
        }

        //TODO weg?
        for (String lijn : header) {
            System.out.println(lijn);
        }
        return header;
    }

    /**
     * Read the HTML page from the given file.
     *
     * @param f The file that needs to be read.
     * @throws IOException If the file contains invalid input, throw an
     * IOException.
     */
    protected void readHtmlPage(File f) throws IOException {
        System.out.println("readHtml");

        PrintWriter writer = new PrintWriter(f);
        ArrayList<String> response = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        //While the response of the server contains text, add it to 'response'.
        int d;
        try {
            while ((d = this.socket.getInputStream().read()) != -1) {
                char c = (char) d;
                sb.append(c);
            }
        } catch (Exception e) {
        }
        String s = sb.toString();
        writer.write(s);
        //writer.write(s, 0, s.length());
        System.out.println("Server");
        System.out.println(s);
        writer.close();
    }

    /**
     * Retrieve the images from the given file.
     *
     * @param f The file from which the images need to be retrieved.
     *
     * @throws IOException When the writer receives invalid input, throw an
     * IOException.
     */
    protected void getImages(File f) throws IOException {
        System.out.println("getImages"); //TODO check
        Document doc = Jsoup.parse(f, "UTF-8");
        Elements images = doc.select("img");

        //Retrieve the sources of the images and get them.
        for (Element image : images) {
            getImage(image.attr("src"));
        }
    }

    /**
     * Retrieve the images from the given source.
     *
     * @param imageSource The source from which the images need to be retrieved.
     *
     * @throws UnknownHostException When the host cannot be resolved, throw an
     * UnknownHostException.
     * @throws IOException When the writer receives invalid input, throw an
     * IOException.
     */
    protected void getImage(String imageSource) throws UnknownHostException, IOException {
        System.out.println(imageSource);//TODO check

        setSocket();

        String imageName = "/" + imageSource;
        String imageKind = imageName.split("\\.")[imageName.split("\\.").length - 1];
        String getImageSentence = "GET " + imageName + " HTTP/1." + getHttpVersion();
        String path = "/home/kommusoft/Projects/javaServer/javaserver/output";

        ByteArrayOutputStream outPutStream = new ByteArrayOutputStream();
        File file = new File(path + imageName);
        file.getParentFile().mkdirs();
        FileOutputStream toFile = new FileOutputStream(path + imageName);

        sendSentence(getImageSentence);
        outToServer.writeBytes("\n");

        ArrayList<String> header = readHeader();

        //Als de image een 404-response teruggeeft: image niet lezen/opslaan
        if (!getFromHeader(header, "Not Found").equals("")) {
            System.out.println("404 Not Found : " + imageName);
            toFile.close();
            outPutStream.close();
            return;
        }

        int nbBytes = Integer.parseInt(getFromHeader(header, "Content-Length"));
        System.out.println(nbBytes); //TODO check

        byte[] buffer = new byte[nbBytes];
        dataInFromServer.read();
        dataInFromServer.readFully(buffer);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(buffer));
        ImageIO.write(image, imageKind, toFile);

        outPutStream.close();
        toFile.close();
    }

    protected String getFromHeader(ArrayList<String> header, String property) {
        String[] headerLine;
        for (String line : header) {
            headerLine = line.split(" ");
            if (line.contains(property)) {
                return line.substring(headerLine[0].length() + 1);
            }
        }
        return "";
    }

    /**
     * Handle the PUT- or POST-response.
     *
     * @throws IOException When the writer receives invalid input, throw
     * IOException.
     */
    protected void handlePutPostResponse() throws IOException {
        Scanner scan = new Scanner(System.in);
        System.out.println("Give a String for input, please:");
        String input = scan.nextLine();
        scan.close();
        //Write the input that was retrieved via the console to the server.
        this.outToServer.writeBytes(input);
        String line;
        //As long as the file contains text, print it to the console.
        /*if ((line = this.inFromServer.readLine()) != null) {
         System.out.println(line);
         }*/
    }

    protected abstract int getHttpVersion();
}