package org.example.hehespring;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequestMapping("/api")
public class JDBMsample
{
	@GetMapping("/run-main")
	public String runMain() {
		StringBuilder result = new StringBuilder();
		try {
			RecordManager recman;
			HTree hashtable;
			recman = RecordManagerFactory.createRecordManager("testRM");
			long recid = recman.getNamedObject("ht1");
			if (recid != 0) {
				hashtable = HTree.load(recman, recid);
			} else {
				hashtable = HTree.createInstance(recman);
				recman.setNamedObject("ht1", hashtable.getRecid());
			}

			hashtable.put("key1", "context 1");
			hashtable.put("key2", "context 2");
			hashtable.put("key3", "context 3");
			hashtable.put("key4", "context 4");

			result.append("Value for key3: ").append(hashtable.get("key3")).append("<br>");

			hashtable.remove("key2");

			FastIterator iter = hashtable.keys();
			String key;
			while ((key = (String) iter.next()) != null) {
				result.append(key).append(" : ").append(hashtable.get(key)).append("<br>");
			}

			recman.commit();
			recman.close();
		} catch (java.io.IOException ex) {
			result.append("Error: ").append(ex.toString());
		}
		return result.toString();
	}
}