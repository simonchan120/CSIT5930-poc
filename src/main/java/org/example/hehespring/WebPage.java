package org.example.hehespring;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class WebPage implements Serializable{
	
	static final long serialVersionUID = 4321L; //requested by Serializable imterface
			
	private URL url;
	private LocalDateTime date;
	private int pageSize;
	private ArrayList<URL> childLinks = new ArrayList<URL>();
	private ArrayList<URL> parentLinks = new ArrayList<URL>();
	private String title;
	private ArrayList<String> content = new ArrayList<String>();
	private String formattedDate;

	public WebPage(URL url) {
		this.url = url;
	}
	
	@Override
	public String toString(){
		StringBuilder result = new StringBuilder();
		result.append("Page Title: " + title + "\n");
		result.append("URL: " + url + "\n");
		result.append("Last Modified Date: " + date.format(DateTimeFormatter.ISO_LOCAL_DATE) + "\n");
		
		result.append("Size of Page: ");
		if (pageSize>=0)
			result.append(pageSize);
		else
			result.append("IS NOT FOUND");
		result.append("\n\n");
		
		result.append("Words in " + url + ":" + "\n");
		for(int i = 0; i < content.size(); i++)
			result.append(content.get(i) + " ");
		result.append("\n\n");
		
		int link_count = 1;
		
		if(!parentLinks.isEmpty()){
			for (URL Link : parentLinks){
				result.append("Parent link " + link_count + ": " + Link.toString() + "\n");
				link_count+=1;
			}
		}
		else
			result.append("No Parent Link of this link");
		result.append("\n");
		link_count = 1;
		
		for (URL Link : childLinks){
			result.append("Child link " + link_count + ": " + Link.toString() + "\n");
			link_count+=1;
		}
		
//		result.append("Number of Child Links: " + childLinks.size() + "\n");
		result.append("\n");
		
		return result.toString();
	}

	public String getFormattedDate() {
		return formattedDate;
	}

	public void setFormattedDate(String formattedDate) {
		this.formattedDate = formattedDate;
	}

	public URL getUrl() {
		return url;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public int getPageSize() {
		return pageSize;
	}

	public ArrayList<URL> getChildLinks() {
		return childLinks;
	}
	
	public ArrayList<URL> getParentLinks() {
		return parentLinks;
	}

	public String getTitle() {
		return title;
	}

	public ArrayList<String> getContent() {
		return content;
	}

	protected void setDate(String date) {
		this.date = LocalDateTime.parse(date);
	}
	
	protected void setDate(LocalDateTime date) {
		this.date = date;
	}

	protected void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	protected void setChildLinks(ArrayList<URL> childLinks) {
		// add elements to al, including duplicates
		Set<URL> hs = new HashSet<>();
		hs.addAll(childLinks);
		childLinks.clear();
		childLinks.addAll(hs);
		childLinks.remove(this.url);

		this.childLinks = childLinks;
	}

	protected void addParentLinks(URL parentLink){
		
		if (!parentLinks.contains(parentLink))
			parentLinks.add(parentLink);
	}
	
	protected void setTitle(String title) {
		this.title = title;
	}

	protected void setContent(ArrayList<String> content) {
		this.content = content;
	}
	
	protected void setContent(String contents) {
		StringTokenizer st = new StringTokenizer(contents);
		while (st.hasMoreTokens()) {
		    this.content.add(st.nextToken());
		}
	}
}
