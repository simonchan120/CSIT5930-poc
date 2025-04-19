package org.example.hehespring;

import java.io.Serializable;
import java.net.URL;

/* same stem in one document should
 * only create one StemInfo instance
 */
public class StemInfo implements Comparable<StemInfo>, Serializable {
	
	protected static final long serialVersionUID = 4321L; //requested by Serializable interface
	
	private URL source;
	private String stem;
	
	/* data members for vector space
	 * model, term weighting formula
	 * = tf*idf/max(tf)
	 */
	int freq = 0;
	
	
	public StemInfo(URL url, String stem, int freq) {
		this.source = url;
		this.stem = stem;
		this.freq = freq;
	}
	
	protected void setFreq(int freq) {
		this.freq = freq;
	}
	
	public int getFreq() {
		return this.freq;
	}
	
	public String getStem() {
		return this.stem;
	}
	public URL getSource() {
		return this.source;
	}
	
	@Override
	public int compareTo(StemInfo o) {
		return this.freq - o.getFreq();
	}
	
	@Override
	public String toString() {
		return this.source + "->" + this.stem + ": appeared " + this.freq + " times\n";
	}
}
