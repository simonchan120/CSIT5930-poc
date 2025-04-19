package org.example.hehespring;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jdbm.helper.FastIterator;
import org.example.hehespring.IRUtilities.Porter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class Retriever {
	private static Porter porter = new Porter();
	private static HashSet<String> stopWords = new HashSet<String>();
	private DatabaseManager recman_webpages;
	private DatabaseManager recman_invIdx_title;
	private DatabaseManager recman_invIdx_content;
	private int numPages = 0;
	private HashMap<String, Integer> docStems_title = new HashMap<String, Integer>();
	private HashMap<String, Integer> docStems_content = new HashMap<String, Integer>();
	private HashMap<String, Integer> maxTf_content = new HashMap<String, Integer>();
	private HashMap<String, Integer> maxTf_title = new HashMap<String, Integer>();
	private HashMap<String, Integer> docStems = new HashMap<String, Integer>();
	private HashMap<String, Integer> queryStems = new HashMap<String, Integer>();
	public HashMap<URL, Double> scores = new HashMap<URL, Double>();

	private ArrayList<String> query;

	@Autowired
	public Retriever(DatabaseManager webpageDbManager, DatabaseManager invertedIndexTitleManager, DatabaseManager invertedIndexContentManager) {
		this.recman_webpages = webpageDbManager;
		this.recman_invIdx_title = invertedIndexTitleManager;
		this.recman_invIdx_content = invertedIndexContentManager;

		try (BufferedReader reader = new BufferedReader(new FileReader("stopwords.txt"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				stopWords.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
//	protected Retriever(String query, DatabaseManager recman1, DatabaseManager recman2, DatabaseManager recman3){
//		recman_webpages = recman1;
//		recman_invIdx_title = recman2;
//		recman_invIdx_content = recman3;
//		this.query = new ArrayList<String>(Arrays.asList(query.split(" ")));
//
//		try {
//			BufferedReader reader = new BufferedReader(new FileReader("stopwords.txt"));
//			String line;
//			while ((line = reader.readLine()) != null) {
//				stopWords.add(line);
//			}
//			reader.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	@GetMapping("/process")
	public String processQuery(@RequestParam("txtname") String query, Model model) {
		try {
			ArrayList<WebPage> results = start(query);
			String[] words = query.split(" "); // Split the input string into words
			// Format the date for each WebPage
			for (WebPage webpage : results) {
				if (webpage.getDate() != null) {
					webpage.setFormattedDate(webpage.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
				}
			}
			model.addAttribute("txtname", query);
			model.addAttribute("words", words); // Pass the split words to the model
			model.addAttribute("results", results);
		} catch (IOException e) {
			model.addAttribute("error", "Error processing query: " + e.getMessage());
		}
		return "lab4"; // Thymeleaf template name
	}

	protected ArrayList<WebPage> start(String query) throws IOException{
		ArrayList<String> queryList = new ArrayList<>(Arrays.asList(query.split(" ")));
		HashMap<String, Integer> queryStems = new HashMap<>();
		HashMap<URL, Double> scores = new HashMap<>();
		int numPages = 0;

		queryStopStem(queryList, queryStems);
		FastIterator itr = recman_webpages.getIterator();
		URL key = (URL)itr.next();
		while (key != null) {
			ArrayList<StemInfo> stems = (ArrayList<StemInfo>) recman_invIdx_content.getEntry(key);
			for(StemInfo stem: stems) {
				if (!docStems_content.containsKey(stem.getStem())) {
					docStems_content.put(stem.getStem(), 1);
					if (!maxTf_content.containsKey(stem.getStem())) {
						maxTf_content.put(stem.getStem(), 1);
					}
					else maxTf_content.put(stem.getStem(), maxTf_content.get(stem.getStem())+1);
				}
				else {
					docStems_content.put(stem.getStem(), docStems_content.get(stem.getStem())+1);
					if (!maxTf_content.containsKey(stem.getStem())) {
						maxTf_content.put(stem.getStem(), 1);
					}
					else maxTf_content.put(stem.getStem(), maxTf_content.get(stem.getStem())+1);
				}
			}
			stems = (ArrayList<StemInfo>) recman_invIdx_title.getEntry(key);
			for(StemInfo stem: stems) {
				if (!docStems_title.containsKey(stem.getStem())) {
					docStems_title.put(stem.getStem(), 1);
					if (!maxTf_title.containsKey(stem.getStem())) {
						maxTf_title.put(stem.getStem(), 1);
					}
					else maxTf_title.put(stem.getStem(), maxTf_title.get(stem.getStem())+1);
				}
				else {
					docStems_title.put(stem.getStem(), docStems_title.get(stem.getStem())+1);
					if (!maxTf_title.containsKey(stem.getStem())) {
						maxTf_title.put(stem.getStem(), 1);
					}
					else maxTf_title.put(stem.getStem(), maxTf_title.get(stem.getStem())+1);
				}
			}
			numPages++;
			key = (URL)itr.next();
		}
		/* term weighting formula
		 * = tf*idf/max(tf)
		 */
		itr = recman_webpages.getIterator();
		key = (URL)itr.next();
		while (key != null) {
			WebPage web = (WebPage) recman_webpages.getEntry(key);
			double score = 0;
			ArrayList<StemInfo> stems = (ArrayList<StemInfo>) recman_invIdx_content.getEntry(key);
			for(StemInfo stem: stems) {
				if (queryStems.containsKey(stem.getStem())) {
					double idf = Math.log10(numPages/stem.getFreq());
					double term_weight = stem.getFreq()*idf/maxTf_content.get(stem.getStem());
					score += queryStems.get(stem.getStem())*term_weight;
				}
			}
			stems = (ArrayList<StemInfo>) recman_invIdx_title.getEntry(key);
			for(StemInfo stem: stems) {
				if (queryStems.containsKey(stem.getStem())) {
					double idf = Math.log10(numPages/stem.getFreq());
					double term_weight = stem.getFreq()*idf/maxTf_title.get(stem.getStem());
					score += 5*queryStems.get(stem.getStem())*term_weight;
				}
			}
			scores.put(key, score);
			key = (URL)itr.next();
		}
		scores = (HashMap)MapUtil.sortByValue(scores);
		
		ArrayList<WebPage> result = new  ArrayList<WebPage>();
		for(URL url: scores.keySet()) {
			result.add((WebPage)recman_webpages.getEntry(url));
		}
		
		return result;
	}

	private void queryStopStem(ArrayList<String> query, HashMap<String, Integer> queryStems) {
		for (String str : query) {
			if (stopWords.contains(str)) continue;
			String stem = porter.stripAffixes(str);
			queryStems.computeIfAbsent(stem, k -> 1);
			queryStems.computeIfPresent(stem, (k, v) -> v + 1);
		}
	}

//	private void queryStopStem() throws IOException {
//		for (String str: query) {
//			if (stopWords.contains(str)) continue;
//			String stem = porter.stripAffixes(str);
//			queryStems.computeIfAbsent(stem, garbage ->  new Integer(1) );
//			queryStems.computeIfPresent(stem, (word, freq) -> new Integer(freq+1) );
//		}
//	}
	
	private void docStopStem(URL url, ArrayList<String> contents) throws IOException {
		for (String str: contents) {
			if (stopWords.contains(str)) continue;
			String stem = porter.stripAffixes(str);
			docStems_content.computeIfAbsent(stem, garbage ->  new Integer(1) );
			docStems_content.computeIfPresent(stem, (word, freq) -> new Integer(freq+1) );
		}
	}
	
	private void docStopStem(URL url, String title) throws IOException {
		for (String str: title.split(" ")) {
			if (stopWords.contains(str)) continue;
			String stem = porter.stripAffixes(str);
			docStems_title.computeIfAbsent(stem, garbage ->  new Integer(1) );
			docStems_title.computeIfPresent(stem, (word, freq) -> new Integer(freq+1) );
		}
	}
	

}

