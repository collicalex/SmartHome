package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

public class Utils {

	public static Map<String, String> queryToMap(HttpExchange httpExchange) {
		return queryToMap(httpExchange.getRequestURI().getQuery());
	}
	
	private static Map<String, String> queryToMap(String query) {
	    Map<String, String> result = new HashMap<>();
	    if (query != null) {
		    for (String param : query.split("&")) {
		        String[] entry = param.split("=");
		        if (entry.length > 1) {
		            result.put(entry[0], entry[1]);
		        }else{
		            result.put(entry[0], "");
		        }
		    }
	    }
	    return result;
	}
	
	public static String getSourceIP(HttpExchange httpExchange) {
		String fwdAddr = httpExchange.getRequestHeaders().getFirst("X-Forwarded-For");
		if ((fwdAddr != null) && (fwdAddr.length() > 0)) {
			return fwdAddr;
		}
		InetAddress addr = httpExchange.getRemoteAddress().getAddress();
		return addr == null ? httpExchange.getRemoteAddress().getHostName() : addr.getHostAddress();
	}
	
	
	/*
	public static String getPage(String url) {
		try {
			HttpURLConnection huc = (HttpURLConnection) new URL(url).openConnection();
			HttpURLConnection.setFollowRedirects(false);
			huc.setConnectTimeout(500); //all pages will be in local network, it must answer in less that 0.5s
			huc.setRequestMethod("GET");
			huc.connect();
			
			if (huc.getResponseCode() == HttpURLConnection.HTTP_OK) {
				String encoding = huc.getContentEncoding();
				if (encoding == null) {
					encoding = "UTF-8";
				}				
				BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream(), encoding));
				String inputLine;
				String response = "";
				while ((inputLine = in.readLine()) != null) {
					response += inputLine + "\n";
				}
				in.close();
				return response;
			}
			return null;
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	*/
	
	/*
	public static boolean isAvailable(String url) {
		try {
			HttpURLConnection huc = (HttpURLConnection) new URL(url).openConnection();
			HttpURLConnection.setFollowRedirects(false);
			huc.setConnectTimeout(500);
			huc.setRequestMethod("GET");
			huc.connect();
			
			if (huc.getResponseCode() == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream()));
				String inputLine;
				String response = "";
				while ((inputLine = in.readLine()) != null) {
					response += inputLine + "\n";
				}
				in.close();
				return true;
			}
			return false;
		} catch(IOException e) {
			return false;
		}
	}
	*/
	
}
