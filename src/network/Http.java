package network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Http {

	public static String get(String url) throws IOException {
		HttpURLConnection con = null;
		StringBuffer content = new StringBuffer();
		try {
			URL urll = new URL(url);
			con = (HttpURLConnection) urll.openConnection();
			con.setConnectTimeout(250);
			con.setReadTimeout(250);
			con.setInstanceFollowRedirects(false);
			con.setRequestMethod("GET");
			int status = con.getResponseCode();
			if (status == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
				String inputLine;
				
				while ((inputLine = in.readLine()) != null) {
					content.append(inputLine);
				}
				in.close();
			}
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
		return content.toString();
	}
	
	public static String post(String url, String urlParameters) throws IOException {
		HttpURLConnection con = null;
		StringBuffer content = new StringBuffer();
		//String urlParameters = "name=Jack&occupation=programmer";
		byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        try {
        	URL urll = new URL(url);
            con = (HttpURLConnection) urll.openConnection();
			con.setConnectTimeout(250);
			con.setReadTimeout(250); 
			con.setInstanceFollowRedirects(false);			
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            //con.setRequestProperty("User-Agent", "Java client");
            //con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(postData);
            }

            int status = con.getResponseCode();
			if (status == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				
				while ((inputLine = in.readLine()) != null) {
					content.append(inputLine);
				}
				in.close();
			}
        } finally {
			if (con != null) {
				con.disconnect();
			}
        }
        return content.toString();
	}
	
}
