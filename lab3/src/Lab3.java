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

public class Lab3 
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
	
	public static void writeToFileIndexDirect(PrintWriter writerID, Map<String, Map<String,Integer>> index)
	{
		for (Map.Entry<String, Map<String,Integer>> entry : index.entrySet())
		{			   
		    writerID.printf("%s %s \n\n\n", entry.getKey(), entry.getValue());
		    writerID.println("\n");
		}
	}
	
	public static void writeToFileIndexIndirect(PrintWriter writerID, Map<String, ArrayList<Map<String, Integer>>> index)
	{
		for (Map.Entry<String, ArrayList<Map<String, Integer>>> entry : index.entrySet())
		{			   
		    writerID.printf("%s %s \n\n\n", entry.getKey(), entry.getValue());
		    writerID.println("\n");
		}
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
	//la fel si pt dictionar(ca mai sus)
	public static Map<String, Map<String,Integer>> clasifyWords(Queue<File> coadaProcesare, ArrayList<String> stopWordsList) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter writerD = new PrintWriter("dictionary.txt", "UTF-8");
		PrintWriter writerE = new PrintWriter("exception.txt", "UTF-8");
		Map<String, Map<String,Integer>> indexDirect=new HashMap<String, Map<String,Integer>>();		
		ArrayList<String> paths = new ArrayList<String>();
		ArrayList<String> exception = new ArrayList<String>();
		
		while(!coadaProcesare.isEmpty())
		{
			ArrayList<String> dict = new ArrayList<String>();
			Map<String, Integer> dictionar=new HashMap<String, Integer>();
			ArrayList<String> words = new ArrayList<String>();
			File f=coadaProcesare.poll();
			Scanner s = new Scanner(f);
			Map<String, Integer> hmm=new HashMap<String,Integer>();
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
					hmm.put(currentWord, 1);
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
			indexDirect.put(f.getPath(), dictionar);
		}

		writeToFileExcept(writerE, exception);
		writerD.close();
		writerE.close();
		
		return indexDirect;
	}
	public static Map<String, ArrayList<Map<String, Integer>>> indexIndirectFunc(Map<String, Map<String,Integer>> indexdirect)
	{
		Map<String, ArrayList<Map<String, Integer>>> indexIndirect = new HashMap<String, ArrayList<Map<String, Integer>>>();
		Map<String,Integer> words=new HashMap<String, Integer>();
		for (Map.Entry<String, Map<String,Integer>> entry : indexdirect.entrySet())
		{
			String path = entry.getKey();
			words = entry.getValue();
			for(Map.Entry<String,Integer> w : words.entrySet())
			{
				Map<String, Integer> pathAndNrAp = new HashMap<String, Integer>();
				ArrayList<Map<String, Integer>> list=new ArrayList<Map<String, Integer>>();
				if(indexIndirect.containsKey(w.getKey()))
				{
					list = indexIndirect.get(w.getKey());
					pathAndNrAp.put(path, w.getValue());
					list.add(pathAndNrAp);
					indexIndirect.put(w.getKey(),list );
				}
				else
				{
					pathAndNrAp.put(path, w.getValue());
					list.add(pathAndNrAp);
					indexIndirect.put(w.getKey(),list );
				}
				
			}
		}
		return indexIndirect;
		
	}
	
	public static void main(String[] args) throws IOException 
	{	
		PrintWriter writerID = new PrintWriter("indexDirect.txt", "UTF-8");
		PrintWriter writerIID = new PrintWriter("indexIndirect.txt", "UTF-8");
		File WD = new File("E:\\FACULTATE\\An IV\\Sem II\\RIW\\lab3riw\\RootDirectory");
		File stopWords = new File("E:\\FACULTATE\\An IV\\Sem II\\RIW\\lab3riw\\stopwords.txt");
		
		ArrayList<String> stopWordsList = new ArrayList<String>();
		
		Queue<File> coadaProcesare = new LinkedList<>();//coada pt fisiere
		Map<String, Map<String,Integer>> indexDirect=new HashMap<String, Map<String,Integer>>();
		Map<String, ArrayList<Map<String, Integer>>> indexIndirect=new HashMap<String, ArrayList<Map<String, Integer>>>();
		Scanner sw = new Scanner(stopWords);
		
		System.setProperty("wordnet.database.dir", "D:\\wordnet\\WordNet.Net-3.1-master\\WordNet.Net-3.1-master\\WordNet-3.0\\dict\\");
		
		coadaProcesare=getAllFiles(WD);
		printFilesName(coadaProcesare);
		stopWordsList=getStopWords(sw);
		
		indexDirect=clasifyWords(coadaProcesare,stopWordsList);
		writeToFileIndexDirect(writerID, indexDirect);
		
		indexIndirect = indexIndirectFunc(indexDirect);
		writeToFileIndexIndirect(writerIID, indexIndirect);
	
		writerIID.close();
		writerID.close();
	}

}
