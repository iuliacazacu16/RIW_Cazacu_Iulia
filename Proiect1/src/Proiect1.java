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

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import edu.smu.tspell.wordnet.*; 

public class Proiect1 
{
	//verific daca e subst propriu
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
	
	//parcurgere directoare
	public static Queue<File> getAllFiles(File WD)
	{
		int n = 0;
		Queue<File> coadaProcesare = new LinkedList<>();
		ArrayDeque<File> stack = new ArrayDeque<File>();

	    stack.push(WD);
	    while(!stack.isEmpty())
	    {
	        n++;
	        File file = stack.pop();
	        File[] files = file.listFiles();
	        for(File f: files)
	        {
	            if(f.isHidden()) continue;

	            if(f.isDirectory())
	            {
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
	/* scriere in fisiere */
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
	/* sf scriere fisiere */
	
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
	/* etapa de filtrare + returnare index direct */
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
					//forma canonica
					currentWord = getBaseForm(currentWord);
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
	/* index indirect */
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

	public static void getIntersection(String word1, String word2, Map<String, ArrayList<Map<String, Integer>>> indIndirect)
	{
		
		if(!indIndirect.containsKey(word1))
		{
			System.out.println("Cuvantul "+ word1 +" nu a fost gasit.");
			
		}
		else if(!indIndirect.containsKey(word2))
		{
			System.out.println("Cuvantul "+ word2 +" nu a fost gasit.");
		}
		else
		{
			ArrayList<Map<String, Integer>> fisWord1 = new ArrayList<Map<String, Integer>>(indIndirect.get(word1));
			ArrayList<Map<String, Integer>> fisWord2 = new ArrayList<Map<String, Integer>>(indIndirect.get(word2));
			ArrayList<Map<String, Integer>> intersectia = getArrayListIntersectie( fisWord1,fisWord2 );
			System.out.println("Cuvantul " + word1 + " -> " + fisWord1.toString());
			System.out.println("Cuvantul " + word2 + " -> " + fisWord2.toString());
			
			System.out.println("Intersectia: "+ intersectia);
		}
	}
	
	public static ArrayList<Map<String, Integer>> getArrayListIntersectie(ArrayList<Map<String, Integer>> fisiereSet1, ArrayList<Map<String, Integer>> fisiereSet2){
		ArrayList<Map<String, Integer>> intersectia = new ArrayList<Map<String, Integer>>();
		
		if(fisiereSet1.size() < fisiereSet2.size()) 
		{
			for(int i=0;i<fisiereSet1.size();i++)
			{
				Map<String, Integer> fis = fisiereSet1.get(i);
				if( fisiereSet2.contains(fis) ) 
				{
					intersectia.add(fis);
				}
			}
		}
		else 
		{
			for(int i=0;i<fisiereSet2.size();i++)
			{
				Map<String, Integer> fis = fisiereSet2.get(i);
				if( fisiereSet1.contains(fis) )
				{
					intersectia.add(fis);
				}
			}
		}
		return intersectia;
	}
	
	public static void getReunion(String word1, String word2, Map<String, ArrayList<Map<String, Integer>>> indexIndirect)
	{
		if(!indexIndirect.containsKey(word1))
		{
			System.out.println("Cuvantul "+ word1 +" nu a fost gasit.");
			
		}
		else if(!indexIndirect.containsKey(word2))
		{
			System.out.println("Cuvantul "+ word2 +" nu a fost gasit.");
		}
		else
		{
			ArrayList<Map<String, Integer>> fisWord1 = new ArrayList<Map<String, Integer>>(indexIndirect.get(word1));
			ArrayList<Map<String, Integer>> fisWord2 = new ArrayList<Map<String, Integer>>(indexIndirect.get(word2));
			ArrayList<Map<String, Integer>> reunion = getArrayListReunion( fisWord1,fisWord2 );
			System.out.println("Cuvantul " + word1 + " -> " + fisWord1.toString());
			System.out.println("Cuvantul " + word2 + " -> " + fisWord2.toString());
			
			System.out.println("Reuniunea "+ reunion);
			
		}
	}
	public static ArrayList<Map<String, Integer>> getArrayListReunion(ArrayList<Map<String, Integer>> fisiereSet1, ArrayList<Map<String, Integer>> fisiereSet2)
	{
		ArrayList<Map<String, Integer>> reunion = new ArrayList<Map<String, Integer>>();
		for(int i=0;i<fisiereSet1.size();i++)
		{
			if(!reunion.contains(fisiereSet1.get(i)))
			{
				reunion.add(fisiereSet1.get(i));	
			}
		}
		for(int i=0;i<fisiereSet2.size();i++)
		{
			
			if(!reunion.contains(fisiereSet2.get(i)))
			{
				reunion.add(fisiereSet2.get(i));
			}
		}
		
		return reunion;
	}
	
	//fisierele care contin word1, dar nu contin word2
	public static void getDiff(String word1, String word2,  Map<String, ArrayList<Map<String, Integer>>> indexIndirect)
	{
		if(!indexIndirect.containsKey(word1))
		{
			System.out.println("Cuvantul "+ word1 +" nu a fost gasit.");
			
		}
		else if(!indexIndirect.containsKey(word2))
		{
			System.out.println("Cuvantul "+ word2 +" nu a fost gasit.");
		}
		else
		{
			ArrayList<Map<String, Integer>> fisWord1 = new ArrayList<Map<String, Integer>>(indexIndirect.get(word1));
			ArrayList<Map<String, Integer>> fisWord2 = new ArrayList<Map<String, Integer>>(indexIndirect.get(word2));
			ArrayList<Map<String, Integer>> dif = getArrayListDiff( fisWord1,fisWord2 );
			System.out.println("Cuvantul " + word1 + " -> " + fisWord1.toString());
			System.out.println("Cuvantul " + word2 + " -> " + fisWord2.toString());
			
			System.out.println("Diferenta: "+ dif);
		}
	}
	public static ArrayList<Map<String, Integer>> getArrayListDiff(ArrayList<Map<String, Integer>> fisiereSet1, ArrayList<Map<String, Integer>> fisiereSet2)
	{
		ArrayList<Map<String, Integer>> dfr = new ArrayList<Map<String, Integer>>();
		
		if(fisiereSet1.size() <= fisiereSet2.size()) 
		{
			for(int i=0;i<fisiereSet1.size();i++)
			{
				Map<String, Integer> fis = fisiereSet1.get(i);
				if( !fisiereSet2.contains(fis) ) {
					dfr.add(fis);
				}
			}
		}
		else 
		{
			for(int i=0;i<fisiereSet2.size();i++)
			{
				Map<String, Integer> fis = fisiereSet2.get(i);
				if(!fisiereSet1.contains(fis) ) 
				{
					dfr.add(fis);
				}
			}
		}
		return dfr;
	}
	static void cautareBooleana(String words, Map<String, ArrayList<Map<String, Integer>>> indexIndirect)
	{
		String[] w = words.split("\\s+");
		String w1 = w[0];
		String w2 = w[1];
		if(w2.contains("+"))
		{
			String wo[] = w2.split("\\+");
			w2=wo[1];
			getIntersection(w1, w2, indexIndirect);
		}
		else if(w2.contains("-"))
		{
			String wo[] = w2.split("\\-");
			w2=wo[1];
			getDiff(w1, w2, indexIndirect);
		}
		else
		{
			getReunion(w1, w2, indexIndirect);
		}
	}
	static String getBaseForm(String word)
	{
		//utilizarea algoritmului Porter
		Stemmer s = new Stemmer();
		for(int i=0;i<word.length();i++)
		{
			 char ch = word.charAt(i);
			 if (Character.isLetter((char) ch))
			 {
				 s.add(ch);
			 }
		}
		s.stem();
		String u=s.toString();
		//System.out.println(u);
		return u;
	}
		
	public static void main(String[] args) throws IOException 
	{	
		PrintWriter writerID = new PrintWriter("indexDirect.txt", "UTF-8");
		PrintWriter writerIID = new PrintWriter("indexIndirect.txt", "UTF-8");
		System.out.println("Introdu numele directorului cu fisiere (RootDirectory): ");
		Scanner in = new Scanner(System.in); 
        String pathWD = in.nextLine(); 
		pathWD = "E:\\FACULTATE\\An IV\\Sem II\\RIW\\Proiect1_riw\\"+ pathWD;
		File WD = new File(pathWD);
		File stopWords = new File("E:\\FACULTATE\\An IV\\Sem II\\RIW\\RIW_LAB2\\stopwords.txt");
		ArrayList<String> stopWordsList = new ArrayList<String>();
		Queue<File> coadaProcesare = new LinkedList<>();//coada pt fisiere
		Map<String, Map<String,Integer>> indexDirect=new HashMap<String, Map<String,Integer>>();
		Map<String, ArrayList<Map<String, Integer>>> indexIndirect=new HashMap<String, ArrayList<Map<String, Integer>>>();
		Scanner sw = new Scanner(stopWords);
		
		System.setProperty("wordnet.database.dir", "D:\\wordnet\\WordNet.Net-3.1-master\\WordNet.Net-3.1-master\\WordNet-3.0\\dict\\");
		System.out.println(" ");
		coadaProcesare=getAllFiles(WD);
		System.out.println("\nAfisare cale fisiere existente: ");
		printFilesName(coadaProcesare);
		stopWordsList=getStopWords(sw);
		
		indexDirect=clasifyWords(coadaProcesare,stopWordsList);
		writeToFileIndexDirect(writerID, indexDirect);
		
		indexIndirect = indexIndirectFunc(indexDirect);
		writeToFileIndexIndirect(writerIID, indexIndirect);
	
		System.out.println("\n\nCautare booleana (ex: job +run sau job -run sau job run):");
		
		while(true)
		{
			Scanner sc = new Scanner(System.in); 
	        String words = sc.nextLine();
	        if(words.equals("no")) break;
	        cautareBooleana(words, indexIndirect);
	        System.out.println("\nAlta cautare?");
		}
		System.out.println("\n\nSfarsit cautare.");
		writerIID.close();
		writerID.close();
	}
}
