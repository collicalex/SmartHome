package orangebox;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;

import json.JSONArray;
import json.JSONObject;
import network.NetworkException;

public class OrangeBox {

	private String _ipAddress;
	
	public OrangeBox(String ipAddress) throws NetworkException {
		network.Utils.checkIPAddress(ipAddress);
		_ipAddress = ipAddress;
	}
	
	//-- ON/OFF --------------------------------------------------------
	
	public void switchONOFF() throws IOException {
		network.Http.get("http://"+_ipAddress+ ":8080/remoteControl/cmd?operation=01&key=116&mode=0");
	}
	
	public boolean isON() throws IOException {
		return !isOFF();
	}
	
	public boolean isOFF() throws IOException {
		String context = getContext();
		if (context == null) {
			return true;
		} else {
			if ("MAIN_PROCESS".compareTo(context) == 0) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	//-- Context --------------------------------------------------------
	
	private String getContext() throws IOException {
		try {
			String url = "http://" + _ipAddress + ":8080/remoteControl/cmd?operation=10";
			String result = network.Http.get(url);
			if (result != null) {
				JSONObject json = new JSONObject(result);
				JSONObject jresult = json.getJSONObject("result");
				if (jresult != null) {
					JSONObject jdata = jresult.getJSONObject("data");
					if (jdata != null) {
						return jdata.getString("osdContext");
					}
				}
			}
		} catch (SocketTimeoutException e) {
			return null;
		}
		return null;
	}
	
	
	//-- Channels --------------------------------------------------------
	
	public void switchTo(int epgid) throws IOException {
		String normalizedEPGID = commons.Utils.padLeft(""+epgid, '*', 10); 
		String encodedNormalizedEPGID = network.Utils.encodeValue(normalizedEPGID);
		String url = "http://" + _ipAddress + ":8080/remoteControl/cmd?operation=09&epg_id=" + encodedNormalizedEPGID + "&uui=1";
		network.Http.get(url);
	}
	
	public void switchTo(String name) throws IOException {
		List<Channel> channels = getChannels();
		for (Channel channel : channels) {
			if (channel.getName().compareTo(name) == 0) {
				switchTo(channel.getEPGID());
				return ;
			}
		}
	}
	
	//available urls for channel list (depend on BOX) are listed here:
	//https://rp-live-pc.woopic.com/live-webapp/v3/applications/
	public List<Channel> getChannels() throws IOException {
		List<Channel> channels = new LinkedList<Channel>();
		String url = "http://rp-live.orange.fr/live-webapp/v3/applications/PCK/channels";
		String result = network.Http.get(url);
		if (result != null) {
			JSONArray jarray = new JSONArray(result);
			for (int i = 0; i < jarray.length(); ++i) {
				JSONObject jobject = jarray.getJSONObject(i);
				channels.add(new Channel(jobject.getString("name"), jobject.getInt("id")));	
			}
		}
		return channels;
	}
	
	
	public Channel getChannel(int epgid) throws IOException {
		List<Channel> channels = getChannels();
		for (Channel channel : channels) {
			if (channel.getEPGID() == epgid) {
				return channel;
			}
		}
		return null;
	}
	
	//-- Programs --------------------------------------------------------
	
	public List<Program> getPrograms() throws IOException {
		List<Program> programs = new LinkedList<Program>();
		String url = "https://rp-live-pc.woopic.com/live-webapp/v3/applications/STB4PC/programs";
		String result = network.Http.get(url);
		if (result != null) {
			JSONArray jarray = new JSONArray(result);
			for (int i = 0; i < jarray.length(); ++i) {
				JSONObject jobject = jarray.getJSONObject(i);
				
				int epgid = jobject.getInt("channelId");
				long startTime = jobject.getLong("diffusionDate");
				long duration = jobject.getLong("duration");
				String type = jobject.getString("programType");
				String name = jobject.getString("title");

				if ("EPISODE".compareTo(type) == 0) {
					JSONObject jseason = jobject.getJSONObject("season");
					if (jseason != null) {
						JSONObject jserie = jseason.getJSONObject("serie");
						if (jserie != null) {
							name = jserie.getString("title");
						}
					}
				}
				
				Program program = new Program(name, epgid, startTime, duration);
				if (program.isLive()) {
					programs.add(program);
				}
			}
		}
		return programs;
	}
	
}
