package org.example.hehespring;

import java.io.IOException;
import java.net.URL;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

public class DatabaseManager {
	private RecordManager recman;
	private HTree db;
	
	private DatabaseFiles file;
	
	public DatabaseManager(DatabaseFiles file) throws IOException{
		this.file = file;
		recman = RecordManagerFactory.createRecordManager(file.toString());
		db = HTree.createInstance(recman);
	}
	
	synchronized protected void updateEntry(URL key, Object val) throws IOException{
		if (file.valTypeMatch(val)) {
			db.put(key, val);
			saveChanges();
		}
		else throw new IllegalArgumentException("value type not match");
	}
	
	protected Object getEntry(URL key) throws IOException{
		try {
			return db.get(key);
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	synchronized protected void removeEntry(URL key) throws IOException{
		db.remove(key);
		saveChanges();
	}
	
	protected FastIterator getIterator() throws IOException{
		return db.keys();
	}
	
	synchronized private void saveChanges() throws IOException {
		recman.commit();
	} 
	
	@Override
	public String toString(){
        StringBuilder result = new StringBuilder();
		try {
			FastIterator iter = db.keys();
	        URL key;
	        int count = 0;
	        while( (key=(URL)iter.next()) != null ) {
	            result.append((count++) + ". " + key.toExternalForm() + "\n" + db.get(key) + "\n");
	        	// result.append((count++) + ". " + key.toExternalForm() + "\n");
	        }
		} catch (IOException e) {
			return "Error occured when retrieving keys";
		}
        return result.toString();
	}
	
}