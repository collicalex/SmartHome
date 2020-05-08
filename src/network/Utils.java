package network;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	
	static private boolean checkAddress(String address, String pattern) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(address);
		return m.find();
	}
	
	static public void checkMacAddress(String macAddress) throws NetworkException {
		String macAddressPattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
		if (checkAddress(macAddress, macAddressPattern) == false) {
			throw new NetworkException("Wrong mac address format");
		}
	}
	
	static public void checkIPAddress(String ipAddress) throws NetworkException {
		String ipAddressPattern = 
				"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
		if (checkAddress(ipAddress, ipAddressPattern) == false) {
			throw new NetworkException("Wrong ip address format");
		}		
	}
	
	static public void wakeOnLan(String macAddress, int port) throws NetworkException {
		WakeOnLan.wol(macAddress, port);
	}
	
	static public boolean isServerListening(String host, int port, int timeout) {
		Socket s = null;
		try {
			s = new Socket();
			s.connect(new InetSocketAddress(host, port), timeout);
			return true;
	    } catch (Exception e) {
	        return false;
	    } finally {
	        if(s != null) {
	            try {
	            	s.close();
	            } catch(Exception e) {
	            }
	        }
	    }
	}	
	
	static public String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
}
