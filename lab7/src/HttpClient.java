import javax.xml.crypto.URIReferenceException;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URISyntaxException;


public class HttpClient {
    private String ipAddress;
    private URLFormater urlFormater;
    private int nrOfRedirect = 0;
    private StringBuilder httpRequest;
    
    //constructor
    public HttpClient(URLFormater urlFormater, String ipAddress) {
        this.ipAddress = ipAddress;
        this.urlFormater = urlFormater;
    }

    //creare request
    private void buildHttpRequest(boolean isRobots) {
        httpRequest = new StringBuilder();
        httpRequest.append("GET ");
        if (isRobots)
            httpRequest.append("/robots.txt");
        else
            httpRequest.append(urlFormater.get_localPathStr());
        httpRequest.append(" HTTP/1.1\r\n");
        httpRequest.append("Host: ");
        httpRequest.append(urlFormater.get_domain());
        httpRequest.append("\r\n");
        httpRequest.append("User-Agent: CLIENT RIW\r\n");
        httpRequest.append("Connection: close\r\n");
        httpRequest.append("\r\n");
    }

    //verificare robots
    public boolean checkRobots() throws IOException {
        buildHttpRequest(true);
        Socket socket = new Socket(InetAddress.getByName(ipAddress), urlFormater.get_port());

        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.print(httpRequest);
        pw.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains("Disallow:"))
                if (line.contains(urlFormater.get_localPathStr()))
                    return false;
        }
        return true;
    }

    //trimitere request
    //verificare cod 301, apoi daca codul e diferit de 200
    //ultimul caz - 200 ok
    public boolean sendRequest() throws IOException {
        buildHttpRequest(false);
        InetAddress ipAddr = InetAddress.getByName(ipAddress);
        Socket socket = new Socket(ipAddr, urlFormater.get_port());
        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.print(httpRequest);
        pw.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String t = br.readLine();
        // verificare status coduri 
        try {
            if (t.contains("HTTP/1.1 301 Moved Permanently")) {
                
                t = br.readLine();
                if (t.contains("Location")) {
                    final String separator = "://";
                    int index = 0;
                    StringBuilder newLocation = new StringBuilder();
                    boolean flag = false;
                    for (Character c : t.toCharArray()) {
                        if (index > separator.length()) {
                            if (c.equals('/'))
                                break;
                            newLocation.append(c);
                        }

                        if (c == separator.charAt(index))
                            index++;
                        else
                            index = 0;
                    }

                    if (index < separator.length()) {
                        throw new CustomException("Invalid Location Header!");
                    }

                    urlFormater.set_domain(newLocation.toString());
                    return sendRequest();
                }
            } else if (!t.contains("HTTP/1.1 200 OK")) {
                throw new CustomException("Error request!");
            } else //200 ok
            {
                boolean flag = false;
                urlFormater.buildFolderPath();
                File output = new File(urlFormater.get_domain() + urlFormater.get_localPath() + "/" + urlFormater.get_page());
                if (!output.exists())
                    output.createNewFile();

                BufferedWriter writer = new BufferedWriter(new FileWriter(output));
                while ((t = br.readLine()) != null) {
                    if (t.trim().isEmpty())
                        flag = true;
                    if (flag)
                        writer.write(t + "\r\n");
                }
                writer.close();
            }
            br.close();

        } catch (CustomException e) {
            File output = new File("error.txt");
            if (!output.exists())
                output.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(output));
            writer.write(e.getMessage() + "\r\n\r\n");
            writer.write(httpRequest.toString());

            writer.write(t + "\r\n");
            while ((t = br.readLine()) != null) {
                if (t.trim().isEmpty())
                    break;
                writer.write(t + "\r\n");
            }
            writer.close();
            return false;
        }
        return true;
    }
    public static void main(String[] args) throws IOException, URISyntaxException 
    {
    	URLFormater url = new URLFormater("http://riweb.tibeica.com/crawl");
    	HttpClient httpClient = new HttpClient(url, "81.180.223.65");
    	if(httpClient.checkRobots())
    	{
    		httpClient.sendRequest();
    	}
    }
}
