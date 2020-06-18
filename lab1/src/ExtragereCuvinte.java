import java.awt.List;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.nodes.Document;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
public class ExtragereCuvinte {
	
	public static void main(String[] args) throws IOException {
		
		File htmlFile = new File("E:\\FACULTATE\\An IV\\Sem II\\RIW\\RIW_LAB1\\index.html");
		Document document = Jsoup.parse(htmlFile, "UTF-8", "");
		PrintWriter writer = new PrintWriter("outputFile.txt", "UTF-8");
		
		String title = document.title();
		System.out.println("The title is: "+title);
		Elements metaTags = ((Element) document).getElementsByTag("meta");
		for (Element metaTag : metaTags) {
			 String content = metaTag.attr("name");
	         if(content.equals("keyword")||content.equals("robots")||content.equals("description")){
	        	 System.out.println(metaTag.attr("content"));
	         }  
		}
		for(Element meta : document.select("meta")) {
			System.out.println("Meta: Name: " + meta.attr("name") + " - Content: " + meta.attr("content"));
		}
		Element link=document.select("a").first();
		String relHref=link.attr("href");
		//String absHref=link.attr("abs:href");
		
		System.out.println("\nContinutul elementelor <a>:");
		System.out.println(link.text());
		System.out.println("Tag-ul href:");
		
		System.out.println(relHref);
		
		String text = ((org.jsoup.nodes.Document) document).body().text();
		System.out.println("\nBody text:");
		System.out.println(text);
		System.out.println("");
		
		Map<String, Integer> hmap=new HashMap<String, Integer>();
		String[] words = text.split("-|\\.|\\,|\\?|\\!|\\ ");
		
		for(String word: words)
		{
            if (hmap.containsKey(word)) {
                hmap.put(word, hmap.get(word) + 1);
            } else {
                hmap.put(word, 1);
            }
		}
		
		for (Map.Entry<String, Integer> entry : hmap.entrySet()) {
		    System.out.printf("%s %d\n", entry.getKey(), entry.getValue());
		    writer.printf("%s %d\n\n", entry.getKey(), entry.getValue());
		    writer.println("\n");
		}
		writer.close();
	}
}
