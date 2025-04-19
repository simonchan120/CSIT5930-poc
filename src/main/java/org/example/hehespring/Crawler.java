package org.example.hehespring;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.HashMap;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.beans.StringBean;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Crawler {

	private URL baseURL;
	private int totalPages;
	public ArrayList<URL> childLinks = new ArrayList<URL>();
	public ArrayList<URL> parentLinks = new ArrayList<URL>();
	public Map<URL, WebPage> webPages = new HashMap<URL, WebPage>();
	private final DatabaseManager recman;

	@Autowired
	public Crawler(DatabaseManager webpageDbManager) {
		this.recman = webpageDbManager;
	}

//	public Crawler (DatabaseManager recman, URL url, int numPages) throws MalformedURLException {
//		this(recman, url.toExternalForm(), numPages);
//	}

	public void start(URL baseURL, int numPages) throws IOException, ParserException{
		this.baseURL = baseURL;
		this.totalPages = numPages;  //total extract 300 pages
		System.out.println("Start crawling...");
		childLinks.add(baseURL);

		ListIterator<URL> childLinksIt = childLinks.listIterator(0);

		int processedCount = 0;
		// while (childLinksIt.hasNext()) {
		while (processedCount < childLinks.size() && processedCount < totalPages) {
			// URL url = childLinksIt.next();
			URL url = childLinks.get(processedCount);
//			System.out.println("Current page: " + url.toString());
			if (!shouldRetrieve(url)){
				processedCount++;
				continue;
			} 
			
			/*HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			String contentType = conn.getContentType();
			if (!contentType.contains("text/html")) continue;*/

			int page_size;
			LocalDateTime page_date;
			ArrayList<URL> childLinks;
			//ArrayList<URL> parentLinks;
			String title;
			String content;

			try {

				page_size = getPageSize(url);
				page_date = getPageDate(url);
				childLinks = getChildLinks(url);
				//parentLinks = getParentLinks(url);
				title = getTitle(url);
				content = getContent(url);

			} catch (FileNotFoundException e) {
				continue;
			} catch (ParserException e) {
				continue;
			}

			WebPage newPage = new WebPage(url);		//create the <Value> using the above extracted INFO
			newPage.setPageSize(page_size);
			newPage.setDate(page_date);
			newPage.setChildLinks(childLinks);
			//newPage.setParentLinks(parentLinks);
			newPage.setTitle(title);
			newPage.setContent(content);
			//  DEBUG: System.out.println(newPage);
			webPages.put(url, newPage);				// set it to be a key value object. i.e. Map<URL, WebPage>
			//recman.updateEntry(url, newPage);
			//WebPage getPage = (WebPage)recman.getEntry(url);
			//System.out.println(getPage);

			ListIterator<URL> temp = childLinksIt;
//			System.out.println("childLinks: " + childLinks.toString()); // ust_cse.htm   news.htm,  /books.htm,  Movie.htm

			for (URL childURL: childLinks) {			// check all the child link of current URL
				if (this.childLinks.size() >= totalPages) {
					System.out.println("this.childLinks.size() >= totalPages");
					break;
				}
//				if (childURL.toExternalForm().contains(baseURL.toExternalForm())) {
//					// Process this URL (it matches our domain)
//				} else {
//					System.out.println("Skipping external URL: " + childURL);
//					continue;
//				}
				if (!this.childLinks.contains(childURL)){
					// temp.add(childURL);		// BFS::  and add this child link behind the childLinksIt
					// childLinksIt.previous();
					this.childLinks.add(childURL);  // Add to end of queue
//					System.out.println("Queued: " + childURL);
				}
			}
			processedCount++;

			
			/*if(childLinksIt.hasPrevious())
				System.out.println("Has previous.");*/



		}
		System.out.println("End crawling...");
		setParentLinks();

	}

	private static int getPageSize(URL url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection)url.openConnection(); //connect the URL
		conn.setDoOutput(false); // dont need to input/output any data to the URL
		conn.setRequestMethod("HEAD");   // request to get the header

		int size = -1;

		//if the content length is not null, then we can get the length from the field "Content-Length"
		Map<String, List<String>> fields = conn.getHeaderFields();
		if (fields.get("Content-Length") != null && !fields.get("Content-Length").isEmpty()) {
			String page_size = (String) fields.get("Content-Length").get(0);
			if (page_size != null){
				size = Integer.parseInt(page_size);
			}
		}

		// if header dont contain the size info, then we count on our own
		if (size <= 0) {   //lab2 code
			StringBean stringBean = new StringBean();
			stringBean.setURL(url.getPath());
			stringBean.setLinks(false);
			String contents = stringBean.getStrings();

			size = contents.length();
		}

		conn.disconnect();

		return size;
	}

	private static LocalDateTime getPageDate(URL url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();//connect the URL
		conn.setDoOutput(false);// dont need to input/output any data to the URL
		conn.setRequestMethod("HEAD");// request to get the header

		LocalDateTime date = null;

		Map<String, List<String>> fields = conn.getHeaderFields();
		if (fields.get("Last-Modified") != null && !fields.get("Last-Modified").isEmpty()){ // check if the field use "Last-Modified", then extract it
			String last_date = (String) fields.get("Last-Modified").get(0);
			if (last_date != null) date = LocalDateTime.parse(last_date, DateTimeFormatter.RFC_1123_DATE_TIME);
		}
		else if (fields.get("Date") != null && !fields.get("Date").isEmpty()){		// check if the field use "Date", then extract it
			String last_date = (String) fields.get("Date").get(0);
			if (last_date != null) date = LocalDateTime.parse(last_date, DateTimeFormatter.RFC_1123_DATE_TIME);
		}
		else { 		// if header dont have "date", then simply use current time as the value
			date = LocalDateTime.now();
		}
		conn.disconnect();
		return date;
	}

	private static ArrayList<URL> getChildLinks(URL url) throws IOException {	//lab2 code
		LinkBean linkBean = new LinkBean();
		linkBean.setURL(url.toString());
		URL[] urls = linkBean.getLinks();
		return new ArrayList<URL>(Arrays.asList(urls));
	}

	private void setParentLinks() throws IOException {


		for (URL url : webPages.keySet()){
			for (URL childURL : webPages.get(url).getChildLinks()){  // loop through all the child links of current URL
				try {
					if (this.childLinks.contains(childURL))     // the current URL's Child array contain this child URL
						webPages.get(childURL).addParentLinks(url); // set the parent of this child link to "Current URL"
				}catch(NullPointerException e) { continue;}
			}


			recman.updateEntry(url, webPages.get(url));
		}

	}

	private static String getTitle(URL url) throws IOException, ParserException{
		Parser parser = new Parser();
		parser.setResource(url.toString());
		TagNode tnode = new TagNode();
		tnode.setChildren(parser.extractAllNodesThatMatch(new AndFilter()));
		for(Node node: tnode.getChildren().toNodeArray()) {
			if (node instanceof TitleTag) {
				return ((TitleTag)node).getTitle();
			}
		}
		return "(title not found)";
	}

	private static String getContent(URL url) throws IOException { //lab2 code
		StringBean stringBean = new StringBean();
		stringBean.setURL(url.toString());
		stringBean.setLinks(false);
		String contents = stringBean.getStrings();
		return contents;
	}

	private boolean shouldRetrieve(URL url) throws IOException{
		// check whether the URL already in inverted index, also check if last date is later or not
		try {
			WebPage webpage = (WebPage)recman.getEntry(url);
			LocalDateTime retrieveDate = webpage.getDate();
			return retrieveDate.isBefore(getPageDate(url)); // if retrieve date is before, thats mean we found a more latest version and should retrieve
		} catch (NullPointerException e) {
			return true;    // cause exception because "webpage" is null, and hence should retrieve
		}

	}
}
