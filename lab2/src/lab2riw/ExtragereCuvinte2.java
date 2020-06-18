package lab2riw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import edu.smu.tspell.wordnet.*; 

public class ExtragereCuvinte2 
{
	public static boolean isProperNoun(String w)
	{
		boolean isProperNoun = false;
		
		if (Character.isUpperCase(w.charAt(0))) {
		    WordNetDatabase database = WordNetDatabase.getFileInstance();
		    Synset[] synsets = database.getSynsets(w, SynsetType.NOUN);
		    isProperNoun = synsets.length > 0;
		}
		return isProperNoun;
	}
	
	public static Queue<File> getAllFiles(File WD)
	{
		int n = 0;
		Queue<File> coadaProcesare = new LinkedList<>();//coada pt fisiere
		ArrayDeque<File> stack = new ArrayDeque<File>();//stiva cu directoare

	    stack.push(WD);
	    while(!stack.isEmpty()){
	        n++;
	        File file = stack.pop();
	        File[] files = file.listFiles();

	        for(File f: files){

	            if(f.isHidden()) continue;

	            if(f.isDirectory()){
	                stack.push(f);
	                continue;
	            }
	            coadaProcesare.add(f);
	            n++;
	        }

	    }
	    return coadaProcesare;
	}
	
	public static void printFilesName(Queue<File> q)
	{
		for(File s : q) { 
			  System.out.println(s); 
			}
	}
	
	public static ArrayList<String> getStopWords(Scanner sw)
	{
		ArrayList<String> stopWordsList = new ArrayList<String>();
		while(sw.hasNext()) {
			stopWordsList.add(sw.next());
		}
		return stopWordsList;
	}
	
	public static void writeToFileDict(PrintWriter writerD,ArrayList<String> dictionary)
	{
		Map<String, Integer> dict=new HashMap<String, Integer>();
		
		for(String word: dictionary)
		{
            if (dict.containsKey(word)) {
            	dict.put(word, dict.get(word) + 1);
            } else {
            	dict.put(word, 1);
            }
		}
		for (Map.Entry<String, Integer> entry : dict.entrySet())
		{			   
		    writerD.printf("%s %d\n\n", entry.getKey(), entry.getValue());
		    writerD.println("\n");
		}
	}
	
	public static void writeToFileExcept(PrintWriter writerE,ArrayList<String> exception)
	{
		Map<String, Integer> except=new HashMap<String, Integer>();
		
		for(String word: exception)
		{
            if (except.containsKey(word)) {
            	except.put(word, except.get(word) + 1);
            } else {
            	except.put(word, 1);
            }
		}
		for (Map.Entry<String, Integer> entry : except.entrySet())
		{			   
		    writerE.printf("%s %d\n\n", entry.getKey(), entry.getValue());
		    writerE.println("\n");
		}
	}
	public static void addExeptionsToFile(ArrayList<String> exception) throws FileNotFoundException, UnsupportedEncodingException
	{
		Map<String, Integer> except=new HashMap<String, Integer>();
		PrintWriter writerE = new PrintWriter("exception.txt", "UTF-8");
		for(String word: exception)
		{
            if (except.containsKey(word)) {
            	except.put(word, except.get(word) + 1);
            } else {
            	except.put(word, 1);
            }
		}
		for (Map.Entry<String, Integer> entry : except.entrySet())
		{			   
		    writerE.printf("%s %d\n\n", entry.getKey(), entry.getValue());
		    writerE.println("\n");
		}
		writerE.close();
		
	}
	
	public static void clasifyWords(Queue<File> coadaProcesare, ArrayList<String> stopWordsList) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter writerD = new PrintWriter("dictionary.txt", "UTF-8");
		PrintWriter writerE = new PrintWriter("exception.txt", "UTF-8");		
		ArrayList<String> exception = new ArrayList<String>();
		
		while(!coadaProcesare.isEmpty())
		{
			ArrayList<String> dict = new ArrayList<String>();
			Map<String, Integer> dictionar=new HashMap<String, Integer>();
			ArrayList<String> words = new ArrayList<String>();
			File f=coadaProcesare.poll();
			Scanner s = new Scanner(f);

			while (s.hasNext()){
			    words.add(s.next());
			}
			s.close();
			
			for(int i=0; i<words.size();i++)
			{
				String currentWord=words.get(i);
				String[] wordss = currentWord.split("-|\\.|\\,|\\?|\\!|\\ ");
				currentWord=wordss[0];
				if(isProperNoun(currentWord ) || (Character.isUpperCase(currentWord.charAt(0))))
				{
					//exceptie-stochez
					exception.add(currentWord);
					words.remove(i);
				}
				else if(stopWordsList.contains(words.get(i)))
				{
					//stopword-ignor
					words.remove(i);
				}
				else
				{
					//add to dict
					dict.add(currentWord);
				}
				
			}
			for(String word: dict)
			{
	            if (dictionar.containsKey(word)) {
	            	dictionar.put(word, dictionar.get(word) + 1);
	            } else {
	            	dictionar.put(word, 1);
	            }
			}
			for (Map.Entry<String, Integer> entry : dictionar.entrySet())
			{			   
			    writerD.printf("%s %d\n\n", entry.getKey(), entry.getValue());
			    writerD.println("\n");
			}
		}

		writeToFileExcept(writerE, exception);
		writerD.close();
		writerE.close();

	}
	
	public static void main(String[] args) throws IOException 
	{	
		File htmlFile = new File("E:\\FACULTATE\\An IV\\Sem II\\RIW\\RIW_LAB1\\index.html");
		Document document = Jsoup.parse(htmlFile, "UTF-8", "");
		File WD = new File("E:\\FACULTATE\\An IV\\Sem II\\RIW\\lab2riw\\RootDirectory");
		File stopWords = new File("E:\\FACULTATE\\An IV\\Sem II\\RIW\\lab2riw\\stopwords.txt");
		ArrayList<String> stopWordsList = new ArrayList<String>();
		Queue<File> coadaProcesare = new LinkedList<>();//coada pt fisiere
		Scanner sw = new Scanner(stopWords);
		System.setProperty("wordnet.database.dir", "D:\\wordnet\\WordNet.Net-3.1-master\\WordNet.Net-3.1-master\\WordNet-3.0\\dict\\");
		
		coadaProcesare=getAllFiles(WD);
		printFilesName(coadaProcesare);
		stopWordsList=getStopWords(sw);
		clasifyWords(coadaProcesare,stopWordsList);
	}

}
