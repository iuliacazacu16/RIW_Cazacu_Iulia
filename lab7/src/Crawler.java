
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//import sun.rmi.runtime.Log;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

public class Crawler 
{
    public static void main(String[] args) throws IOException, URISyntaxException
    {
        Queue<URLFormater> urlQueue = new LinkedList<>();    
        int i = 0;
        String ipAddress;
        ipAddress = "67.207.88.228";
        urlQueue.add(new URLFormater("http://riweb.tibeica.com/crawl"));
        System.out.println("Se proceseaza...");
        while (!urlQueue.isEmpty() && i<100) 
        {
        	try
        	{
	        	//preluare url din coada
	            URLFormater processedURL = urlQueue.peek();
	            urlQueue.remove();
	
	            String domain = processedURL.get_domain();
	            String localPath = processedURL.get_localPath();
	            String page = processedURL.get_page();
	
	            // verific daca a mai fost procesat inainte
	            if (processedURL.wasProcessed())
	                continue;
	            
	            
	            // cautare /robots.txt
	            
	            HttpClient httpClient = new HttpClient(processedURL, ipAddress);
	       
	            if (!httpClient.checkRobots()) {
	                continue;
	            }
	            
	            if (!httpClient.sendRequest())
	                continue;
	
	            // initializare jsoup
	            File htmlFile = new File(domain + localPath + "/" + page);
	            Document document = Jsoup.parse(htmlFile, null, "http://" + domain + localPath + "/");
	
	            // extragere text
	            String text = document.body().text();
	
	            BufferedWriter writer = new BufferedWriter(new FileWriter(domain + localPath + "/"
	                    + page.replaceAll(".html", ".txt")));
	            writer.write(text);
	
	            writer.close();
	
	            // check for robots meta
//	            Elements metaRobots = document.select("meta[name=robots]");
//	            if (metaRobots != null) {
//	                String content = metaRobots.attr("content");
//	                if (content == null || content.contains("nofollow"))
//	                    continue;
//	            }
//				
	            // Extract link from currentPage
	            List<Element> links = document.select("a");
	            List<String> linksHref = new LinkedList<>();
	            for (Element e : links) {
	                String link = e.attr("abs:href");
	                if (!link.isEmpty()) {
	                    if (!link.contains("https://")) {
	                        URLFormater urlToAdd = new URLFormater(link);
	                        //daca url-ul nu a fost procesat, se adauga in coada
	                        if (!urlToAdd.wasProcessed())
	                            urlQueue.add(urlToAdd);
	                    }
	                }
	
	            }
	            htmlFile.delete();
	            i++;
	            
        	}
        	catch (URISyntaxException e)
        	{
        		e.printStackTrace();
        	}
    	}	
        System.out.println("Fisiere procesate: " + (i));
    }
}
