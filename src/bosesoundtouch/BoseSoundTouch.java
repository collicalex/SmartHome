package bosesoundtouch;

import java.io.IOException;

import network.NetworkException;
import xml.XMLObject;

public class BoseSoundTouch {

	private String _ipAddress;
	
	public BoseSoundTouch(String ipAddress) throws NetworkException {
		network.Utils.checkIPAddress(ipAddress);
		_ipAddress = ipAddress;
	}
	
	public Integer getVolume() throws IOException {
		String result = network.Http.get("http://" + _ipAddress + ":8080/volume");
		
		
		if (result != null) {
			XMLObject xml = new XMLObject(result);
			return xml.getInt("actualvolume");
		}
		return null;
	}
	
	public void setVolume(int volume) throws IOException {
		if (volume < 0) {
			volume = 0;
		} else if (volume > 100) {
			volume = 100;
		}
		network.Http.post("http://" + _ipAddress + ":8090/volume", "<?xml version=\"1.0\" ?>\r\n<volume>"+volume+"</volume>");
	}
	
}
