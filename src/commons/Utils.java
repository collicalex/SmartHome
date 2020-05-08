package commons;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

public class Utils {

	public static String getDateTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return "[" + sdf.format(new Date()) +  "]";
	}
	
	public static void log(String msg) {
		System.out.println(getDateTime() + " " + msg);
	}
	
	public static void ler(String msg) {
		System.err.println(getDateTime() + " " + msg);
	}
	
	public static String firstWord(String text) {
		text = text.trim();
		int index = text.indexOf(' ');
	    if (index > -1) { // Check if there is more than one word.
	    	return text.substring(0, index); // Extract first word.
	    } else {
	    	return text; // Text is the first word itself.
	    }
	}
	
	public static String otherWords(String text) {
		text = text.trim();
		int index = text.indexOf(' ');
	    if (index > -1) { // Check if there is more than one word.
	    	return text.substring(index).trim();
	    } else {
	    	return null; // Text is the first word itself, so there is no other word
	    }		
	}
	
	public static String pack(String name) {
		return name.replaceAll("\\s+","").replaceAll("-", "").replaceAll("\\+", "plus").replaceAll("'", "").replaceAll("\\.", "").toLowerCase();
	}
	
	public static String padLeft(String str, char pad, int size) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < (size - str.length()); ++i) {
			sb.append(pad);
		}
		sb.append(str);
		return sb.toString();
	}
	
	//Retrieve the element in list which name is the most similar to the key
	public static Nameable smartSearch(Collection<Nameable> list, String key) {
		return smartSearch(list, key, 0);
	}
	
	public static Nameable smartSearch(Collection<Nameable> list, String key, double minLimit) {
		double simalarity = minLimit;
		Nameable found = null;
		
		key = commons.Utils.pack(key);
		
		for (Nameable n : list) {
			String name = commons.Utils.pack(n.getName());
			double s = new string.NormalizedLevenshtein().similarity(name, key);
			if (s > simalarity) {
				simalarity = s;
				found = n;
			}
		}
		
		return found;
	}
}
