//package org.example.hehespring;
//
//import java.io.FileWriter;
//import java.net.URL;
//import java.util.ArrayList;
//
//
//public class Test {
//	public static void main(String[] args) {
//		Crawler crawler;
//		DatabaseManager recman_webpages, recman_inverted_title, recman_inverted_content;
//		Indexer indexer;
//		try {
//			recman_webpages = new DatabaseManager(DatabaseFiles.WEBPAGE_DB);
//			/*crawler = new Crawler(recman_webpages, new URL("https://www.cse.ust.hk/"),50);
//			crawler.start();*/
//
//			crawler = new Crawler(recman_webpages, new URL("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm"),300);
//			crawler.start();
//
//			System.out.println("recman_webpages: " + recman_webpages);
//
//			// inverted index for webpage title
//			recman_inverted_title = new DatabaseManager(DatabaseFiles.INVERTED_INDEX_TITLE);
//			// inverted index for webpage Content
//			recman_inverted_content = new DatabaseManager(DatabaseFiles.INVERTED_INDEX_CONTENT);
//
//			indexer = new Indexer(recman_webpages, recman_inverted_title, recman_inverted_content);
//			indexer.start();
//
////			System.out.println("indexer: " + indexer);
//
//			Retriever ret = new Retriever("HKUST", recman_webpages, recman_inverted_title, recman_inverted_content);
//			ArrayList<WebPage> resultList = ret.start();
//
////			System.out.println("resultList: " + resultList);
//
//			FileWriter result = new FileWriter("result.txt", true);
//			result.append(resultList.toString());
//
//			result.close();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//}
