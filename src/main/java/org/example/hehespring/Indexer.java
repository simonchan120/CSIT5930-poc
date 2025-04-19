package org.example.hehespring;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import jdbm.helper.FastIterator;
import org.example.hehespring.IRUtilities.Porter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Indexer {
	private static Porter porter = new Porter();
	private static HashSet<String> stopWords = new HashSet<String>();
	private DatabaseManager recman_webpages;
	private DatabaseManager recman_invIdx_title;
	private DatabaseManager recman_invIdx_content;
	private HashMap<String, Integer> stems = new HashMap<String, Integer>();

	public Indexer(DatabaseManager webpageDbManager,
				   DatabaseManager invertedIndexTitleManager,
				   DatabaseManager invertedIndexContentManager) {
		this.recman_webpages = webpageDbManager;
		this.recman_invIdx_title = invertedIndexTitleManager;
		this.recman_invIdx_content = invertedIndexContentManager;

		try (BufferedReader reader = new BufferedReader(new FileReader("stopwords.txt"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				stopWords.add(line); // Store all stop words in the HashSet
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	protected Indexer(DatabaseManager recman1, DatabaseManager recman2, DatabaseManager recman3){
//		recman_webpages = recman1;
//		recman_invIdx_title = recman2;
//		recman_invIdx_content = recman3;
//
//		try {
//			BufferedReader reader = new BufferedReader(new FileReader("stopwords.txt")); //put at the root directory
//			String line;
//			while ((line = reader.readLine()) != null) {
//				stopWords.add(line);				//store all the stop word in the txt file into a HashSet
//			}
//			reader.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	protected void start() throws IOException{
		System.out.println("Start Indexing");
		stems.clear();
		FastIterator itr = recman_webpages.getIterator();
		URL key = (URL)itr.next();
		while (key != null) {					// loop through each webpage in Record Manager
			WebPage web = (WebPage) recman_webpages.getEntry(key);
			stopStem(web.getUrl(), web.getContent());	// remove stopword in Web Content & save result in inverted index of Web Content
			stopStem(web.getUrl(), web.getTitle());		// remove stopword in Web Title & save result in inverted index of Web Title
			key = (URL)itr.next();
		}
		System.out.println("Complete Indexing");
	}

	private void stopStem(URL url, ArrayList<String> contents) throws IOException {
		for (String str: contents) {			// loop through all the word in the Web Content
			if (stopWords.contains(str)) continue;
			String stem = porter.stripAffixes(str);	// use Porter Algorithm
			stems.computeIfAbsent(stem, garbage ->  new Integer(1) );
			stems.computeIfPresent(stem, (word, freq) -> new Integer(freq+1) );
		}
		createStemInfo(url, recman_invIdx_content);  //save result in inverted index of Web Content
	}
	
	private void stopStem(URL url, String title) throws IOException {
		for (String str: title.split(" ")) {			// loop through each word in the Web Title
			if (stopWords.contains(str)) continue;
			String stem = porter.stripAffixes(str); // use Porter Algorithm
			stems.computeIfAbsent(stem, garbage ->  new Integer(1) );
			stems.computeIfPresent(stem, (word, freq) -> new Integer(freq+1) );
		}
		createStemInfo(url, recman_invIdx_title); //save result in inverted index of Web Title
	}
	
	private void createStemInfo(URL url, DatabaseManager recman) throws IOException {
		ArrayList<StemInfo> result = new ArrayList<StemInfo>();
		stems.forEach((word, freq) -> result.add(new StemInfo(url, word, freq)));
		recman.updateEntry(url, result);
		stems.clear();
	}
	
}
