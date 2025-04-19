package org.example.hehespring;

import java.util.ArrayList;

public enum DatabaseFiles {
	INVERTED_INDEX_TITLE("invertedIndexTitle") { 
		@Override protected boolean valTypeMatch(Object obj){ return obj instanceof ArrayList;  }
	},
	INVERTED_INDEX_CONTENT("invertedIndexContent") { 
		@Override protected boolean valTypeMatch(Object obj){ return obj instanceof ArrayList;  }
	},
	WEBPAGE_DB("webpages"){ 
		@Override protected boolean valTypeMatch(Object obj){ return obj instanceof WebPage; }
	};
	
	protected String filename;
	
	private DatabaseFiles(String file) {
		this.filename = file;
	}
	
	protected abstract boolean valTypeMatch(Object obj);
	
	@Override
	public String toString() {return filename; }
}