package lgwebos;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Builder;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JOptionPane;

import json.JSONArray;
import json.JSONObject;

import network.NetworkException;

public class LGWebOSCore implements Listener {
	
	private boolean _debug = false;
	
	private String _macAddress;
	private String _ipAddress;
	private WebSocket _webSocket;
	private WebSocket _webSocketMouse;
	//private WebSocket _webSocketKeyboard;
	private String _clientKey;
	
	public static int PAIRING_TYPE_PROMPT = 0;
	public static int PAIRING_TYPE_PIN = 1;
	public static int PAIRING_TYPE_COMBINED = 2;
	
	
/*
 	Not yet mapped ssap services:
 	-----------------------------
 
	// APIServiceListCommand lists the API services available on the TV.
	// NDLR: Not usefull
    APIServiceListCommand Command = "ssap://api/getServiceList"

    // MediaControlFastForwardCommand fast forwards the current media.
    MediaControlFastForwardCommand Command = "ssap://media.controls/fastForward"

    // MediaControlPauseCommand pauses the current media.
    MediaControlPauseCommand Command = "ssap://media.controls/pause"

    // MediaControlPlayCommand plays or resumes the current media.
    MediaControlPlayCommand Command = "ssap://media.controls/play"

    // MediaControlRewindCommand rewinds the current media.
    MediaControlRewindCommand Command = "ssap://media.controls/rewind"

    // MediaControlStopCommand stops the current media.
    MediaControlStopCommand Command = "ssap://media.controls/stop"

    // SystemLauncherCloseCommand closes a given application.
    SystemLauncherCloseCommand Command = "ssap://system.launcher/close"

    // SystemLauncherOpenCommand opens a previously launched application.
    SystemLauncherOpenCommand Command = "ssap://system.launcher/open"

    // SystemNotificationsCreateToastCommand creates a "toast" notification.
    SystemNotificationsCreateToastCommand Command = "ssap://system.notifications/createToast"  {message: 'Hello World!'}

    // TVChannelDownCommand changes the channel down.
    TVChannelDownCommand Command = "ssap://tv/channelDown"

    // TVChannelListCommand returns information about the available channels.
    TVChannelListCommand Command = "ssap://tv/getChannelList"

    // TVChannelUpCommand changes the channel up.
    TVChannelUpCommand Command = "ssap://tv/channelUp"

    // TVCurrentChannelCommand returns information about the current channel.
    TVCurrentChannelCommand Command = "ssap://tv/getCurrentChannel"

    // TVCurrentChannelProgramCommand returns information about the current program playing on
    // the current channel.
    TVCurrentChannelProgramCommand Command = "ssap://tv/getChannelProgramInfo"


    static final String CLOSE_MEDIA_URI = "ssap://media.viewer/close";
    static final String CLOSE_WEBAPP_URI = "ssap://webapp/closeWebApp";    	
    
    ssap://media.viewer/open
    ssap://tv/openChannel
    ssap://tv/getChannelCurrentProgramInfo
    ssap://com.webos.service.tv.display/set3DOn
    ssap://com.webos.service.tv.display/set3DOff
    ssap://com.webos.service.tv.display/get3DStatus
    
    ssap://webapp/launchWebApp
    ssap://webapp/connectToApp
    ssap://webapp/pinWebApp
    ssap://webapp/removePinnedWebApp
    ssap://webapp/isWebAppPinned
    
    ssap://system/getSystemInfo
    ssap://com.webos.service.secondscreen.gateway/test/secure
    ssap://tv/getACRAuthToken
    ssap://com.webos.applicationManager/listLaunchPoints
    
    ssap://com.webos.applicationManager/launch
    ssap://com.webos.service.update/getCurrentSWInformation
    	
    	
    
    ssap://com.webos.service.appstatus/getAppStatus
 */
	
	
	public LGWebOSCore(String macAddress, String ipAddress, String clientKey) throws NetworkException {
		this.init(macAddress, ipAddress, clientKey);
	}
	
	public LGWebOSCore(String macAddress, String ipAddress) throws NetworkException {
		this.init(macAddress, ipAddress, null);
	}
	
	private void init(String macAddress, String ipAddress, String clientKey) throws NetworkException {
		network.Utils.checkMacAddress(macAddress);
		network.Utils.checkIPAddress(ipAddress);
		_macAddress = macAddress;
		_ipAddress = ipAddress;
		_webSocket = null;
		_webSocketMouse = null;
		_clientKey = clientKey;
	}
	
	//-- Test -------------------------------------------------------------------------------------
	
	public void test(String str) {
		sendCommand("idTest", "ssap://com.webos.service.ime/insertText", "text", str);
		sendCommand("idTestTest", "ssap://com.webos.service.ime/sendEnterKey");
	}
	
	//-- ON / OFF commands ------------------------------------------------------------------------
	
	public void switchOn() throws NetworkException {
		network.Utils.wakeOnLan(_macAddress, 2034); //send magic packet on port 2034
	}
	
	public void connect() {
		if (_webSocket == null) {
			debug("Connecting to the TV...");
	        String wsURL = "ws://"+_ipAddress+":3000";
	        HttpClient httpClient = HttpClient.newBuilder().build();
	        Builder webSocketBuilder = httpClient.newWebSocketBuilder();
	        _webSocket = webSocketBuilder.buildAsync(URI.create(wsURL), this).join();
		}
	}
	
	public boolean register(int pairingType) {
		//1st : Register and get back a client key
        if (retrieveClientKey(pairingType) == false) {
        	return false;
        }
        //2nd : Check the client key, send the same message but with the client key, it must return the same client key
        if (checkClientKey(pairingType) == false) {
        	return false;
        }
        return true;
	}
	
	public void connectANDregister(int pairingType) {
		if (_webSocket == null) {
			connect();
			register(pairingType);
		}
	}
	
	public void switchOff() {
		sendCommand("idTurnOff", "ssap://system/turnOff");
	}
	
	public void disconnect() {
		if (_webSocket != null) {
			_webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "disconnect");
			_webSocket = null;
		}
		disconnectMouse();
		//disconnectKeyboard();
	}
	
	public boolean isOn() {
		return network.Utils.isServerListening(_ipAddress, 3000, 250); //test that port 3000 is opened, timeout 250ms
	}
	
	public boolean isOff() {
		return !isOn();
	}
	
	//-- Volume -----------------------------------------------------------------------------------
	
	public Integer getVolume() {
		JSONObject json = sendCommand("idGetVolume", "ssap://audio/getVolume"); //return about the same as command ssap://audio/getStatus
		return parseResponseValueInteger(json, "volume");
	}
	
	public Boolean isMuted() {
		JSONObject json = sendCommand("idIsMuted", "ssap://audio/getMute");
		return parseResponseValueBoolean(json, "mute");
	}
	
	public void volumeUp() {
		sendCommand("idVolumeUp", "ssap://audio/volumeUp");
	}
	
	public void volumeDown() {
		sendCommand("idVolumeDown", "ssap://audio/volumeDown");
	}
	
	public void setMute(boolean muted) {
		sendCommand("idSetMute", "ssap://audio/setMute" , "mute", muted ? true : false);
	}
	
	public void setVolume(int volume) {
		if (volume < 0) {
			volume = 0;
		} else if (volume > 100) {
			volume = 100;
		}
		sendCommand("idSetVolume", "ssap://audio/setVolume", "volume", volume);
	}
	
	//-- Mouse (remote controller) ----------------------------------------------------------------
	
	private void connectMouse() {
		if (_webSocketMouse == null) {
			debug("Connecting to the Mouse...");
			JSONObject json = sendCommand("idGetPointerInputSocket", "ssap://com.webos.service.networkinput/getPointerInputSocket");
			String socketPath = parseResponseValueString(json, "socketPath");
			socketPath = socketPath.replace("wss:", "ws:").replace(":3001/", ":3000/"); // downgrade to plaintext
	        HttpClient httpClient = HttpClient.newBuilder().build();
	        Builder webSocketBuilder = httpClient.newWebSocketBuilder();
	        _webSocketMouse = webSocketBuilder.buildAsync(URI.create(socketPath), new Listener() {}).join(); //no need to have a dedicated listener, the TV never send any response message
		}
	}
	
	private void disconnectMouse() {
		if (_webSocketMouse != null) {
			_webSocketMouse.sendClose(WebSocket.NORMAL_CLOSURE, "disconnect");
			_webSocketMouse = null;
		}
	}
	
    public void mouseClick() {
   		sendMouseText("type:click\n\n");
    }
    
    public void mouseMove(int dx, int dy) {
   		sendMouseText("type:move\n" + "dx:" + dx + "\n" + "dy:" + dy + "\n" + "down:0\n\n");
    }

    public void mouseMove(int dx, int dy, boolean drag) {
   		sendMouseText("type:move\n" + "dx:" + dx + "\n" + "dy:" + dy + "\n" + "down:" + (drag ? 1 : 0) + "\n\n");
    }

    public void mouseScroll(int dx, int dy) {
   		sendMouseText("type:scroll\n" + "dx:" + dx + "\n" + "dy:" + dy + "\n\n");
    }    
	
    public void mouseButtonUp() {
        sendSpecialKey("UP");
    }

    public void mouseButtonDown() {
        sendSpecialKey("DOWN");
    }

    public void mouseButtonLeft() {
        sendSpecialKey("LEFT");
    }

    public void mouseButtonRight() {
        sendSpecialKey("RIGHT");
    }

    public void mouseButtonHome() {
        sendSpecialKey("HOME");
    }
    
    public void mouseButtonBack() {
        sendSpecialKey("BACK");
    }
    
    public void mouseButtonNone() {
    	sendSpecialKey("NONE");
    }
    
    private void sendSpecialKey(String key) {
   		sendMouseText("type:button\n" + "name:" + key + "\n\n");
	}
    
    private long _lastSentMouseTextTime = 0;
    
    private void sendMouseText(String text) {
    	connectMouse();
    	if (_webSocketMouse != null) {
    		debug("Sending mouse: " + text);
    		long currentTime = System.currentTimeMillis();
    		long diffTime = currentTime - _lastSentMouseTextTime;
    		if (diffTime < 20) {
    			try {
					Thread.sleep(20 - diffTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    		_webSocketMouse.sendText(text, true);
    		_lastSentMouseTextTime = currentTime;
    	}
    }
    
    //-- Keyboard ---------------------------------------------------------------------------------
    
    /*
	private void connectKeyboard() {
		if (_webSocketKeyboard == null) {
			debug("Connecting to the Keyboard...");
			JSONObject json = sendCommand("idRegisterKeyboard", "ssap://com.webos.service.ime/registerRemoteKeyboard");
			String socketPath = parseResponseValueString(json, "socketPath");
			socketPath = socketPath.replace("wss:", "ws:").replace(":3001/", ":3000/"); // downgrade to plaintext
	        HttpClient httpClient = HttpClient.newBuilder().build();
	        Builder webSocketBuilder = httpClient.newWebSocketBuilder();
	        _webSocketKeyboard = webSocketBuilder.buildAsync(URI.create(socketPath), this).join();
		}
	}
	
	private void disconnectKeyboard() {
		if (_webSocketKeyboard != null) {
			_webSocketKeyboard.sendClose(WebSocket.NORMAL_CLOSURE, "disconnect");
			_webSocketKeyboard = null;
		}
	}
	*/
    
    public void inputText(String text) {
    	sendCommand("idInputText", "ssap://com.webos.service.ime/insertText", "text", text);
    }
    
    public void inputEnter() {
    	sendCommand("idTestTest", "ssap://com.webos.service.ime/sendEnterKey");
    }
    
    public void inputBackspace(int count) {
    	if (count > 0) {
    		sendCommand("idTestTest", "ssap://com.webos.service.ime/deleteCharacters", "count", count);
    	}
    }
    
    
	//-- Applications -----------------------------------------------------------------------------
	
    public List<LGWebOSApp> listAppAndExternalDevices() {
    	List<LGWebOSApp> result = listApp();
    	result.addAll(listExternalDevices());
    	return result;
    }
    
	public List<LGWebOSApp> listApp() {
		List<LGWebOSApp> result = new LinkedList<LGWebOSApp>();
		JSONObject json = sendCommand("idListApp", "ssap://com.webos.applicationManager/listApps");
		if (json != null) {
			if ("response".compareTo(json.getString("type")) == 0) {
				JSONObject payload = json.getJSONObject("payload");
				if (payload != null) {
					JSONArray jarray = payload.getJSONArray("apps");
					if (jarray != null) {
						for (int i = 0; i < jarray.length(); ++i) {
							JSONObject app = jarray.getJSONObject(i);
							result.add(new LGWebOSApp(app.getString("id"), app.getString("title")));
						}
					}
				}
			}
		}
		return result;
	}
	
	//return a sessionId
	public String launchApp(String appId) {
		JSONObject json = sendCommand("idLaunchApp", "ssap://system.launcher/launch", "id", appId);
		return parseResponseValueString(json, "sessionId");
	}
	
	public Boolean isAppRunning(String sessionId) {
		JSONObject json = sendCommand("idIsAppRunning", "ssap://system.launcher/getAppState", "sessionId", sessionId);
		return parseResponseValueBoolean(json, "running");
	}
	
	public Boolean isAppVisible(String sessionId) {
		JSONObject json = sendCommand("idIsAppVisible", "ssap://system.launcher/getAppState", "sessionId", sessionId);
		return parseResponseValueBoolean(json, "visible");
	}
	
	public String getForegroundAppId() {
		JSONObject json = sendCommand("idGetForegroundApp", "ssap://com.webos.applicationManager/getForegroundAppInfo");
		return parseResponseValueString(json, "appId");
	}
	
	//-- Connected External Devices ---------------------------------------------------------------
	
	public List<LGWebOSExternalDevice> listExternalDevices() {
		List<LGWebOSExternalDevice> result = new LinkedList<LGWebOSExternalDevice>();
		JSONObject json = sendCommand("idGetExternalInputList", "ssap://tv/getExternalInputList");
		System.out.println(json);
		if (json != null) {
			if ("response".compareTo(json.getString("type")) == 0) {
				JSONObject payload = json.getJSONObject("payload");
				if (payload != null) {
					JSONArray jarray = payload.getJSONArray("devices");
					if (jarray != null) {
						for (int i = 0; i < jarray.length(); ++i) {
							JSONObject device = jarray.getJSONObject(i);
							if (device.getBoolean("connected") == Boolean.TRUE) {
								result.add(new LGWebOSExternalDevice(device.getString("id"), device.getString("appId"), device.getString("label")));
							}
						}
					}
				}
			}
		}
		return result;		
	}	
	
	public void switchToExternalDevice(String deviceId) {
		JSONObject json = sendCommand("idSwitchInput", "ssap://tv/switchInput", "inputId", deviceId);
		System.out.println(json);
		//return parseResponseValueString(json, "sessionId");
	}
	
	//-- Send command -----------------------------------------------------------------------------
	
	private JSONObject sendCommand(String id, String uri) {
        String json = "{\"type\":\"request\", \"id\":\""+id+"\", \"uri\":\""+uri+"\"}";
        this.sendText(json);
        return this.waitResponse(id);
	}
	
	private JSONObject sendCommand(String id, String uri, String payloadId, String payloadValue) {
        String json = "{\"type\":\"request\", \"id\":\""+id+"\", \"uri\":\""+uri+"\", \"payload\":{\""+payloadId+"\":\""+payloadValue+"\"}}";
        this.sendText(json);
        return this.waitResponse(id);	
	}
	
	private JSONObject sendCommand(String id, String uri, String payloadId, int payloadValue) {
        String json = "{\"type\":\"request\", \"id\":\""+id+"\", \"uri\":\""+uri+"\", \"payload\":{\""+payloadId+"\":"+payloadValue+"}}";
        this.sendText(json);
        return this.waitResponse(id);	
	}	
	
	private JSONObject sendCommand(String id, String uri, String payloadId, boolean payloadValue) {
		String value = payloadValue ? "true" : "false";
		String json = "{\"type\":\"request\", \"id\":\""+id+"\", \"uri\":\""+uri+"\", \"payload\":{\""+payloadId+"\":"+value+"}}";
        this.sendText(json);        
        return this.waitResponse(id);	
	}	
	
	private void sendText(String str) {
		debug("Sending text: " + str);
		if (_webSocket != null) {
			_webSocket.sendText(str, true);
		}
	}
	
	//-- Receive response -------------------------------------------------------------------------
	
	private void receiveResponse(String answer) {
		debug("Rceiving json : " + answer);
        JSONObject json = new JSONObject(answer);
        String id = json.getString("id");
        _responses.put(id, json);
	}	
	
	//-- Wait response ----------------------------------------------------------------------------
	
	private ConcurrentHashMap<String, JSONObject> _responses = new ConcurrentHashMap<String, JSONObject>();
	
	private JSONObject waitResponse(String id) {
		return waitResponse(id, 100); //10s max
	}
	
	private JSONObject waitResponse(String id, int maxWait) {
		JSONObject response = null;
        for (int i = 0; i < maxWait; ++i) {
        	response = _responses.remove(id);
        	if (response != null) {
        		return response;
        	}
        	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        return response;
	}	
	
	//-- Parse response ---------------------------------------------------------------------------
	
	private String parseResponseValueString(JSONObject response, String id) {
		if (response != null) {
			if ("response".compareTo(response.getString("type")) == 0) {
				return getPayloadString(response, id);
			}
		}
		return null;
	}
	
	private Integer parseResponseValueInteger(JSONObject response, String id) {
		if (response != null) {
			if ("response".compareTo(response.getString("type")) == 0) {
				return getPayloadInteger(response, id);
			}
		}
		return null;
	}
	
	private Boolean parseResponseValueBoolean(JSONObject response, String id) {
		if (response != null) {
			if ("response".compareTo(response.getString("type")) == 0) {
				return getPayloadBoolean(response, id);
			}
		}
		return null;
	}
	
	//-- Parse payload ----------------------------------------------------------------------------
	
	private String getPayloadString(JSONObject json, String key) {
		debug("getPayLaodString : " + json);
		JSONObject payload = json.getJSONObject("payload");
		if (payload != null) {
			return payload.getString(key);
		}
		return null;
	}
	
	private Integer getPayloadInteger(JSONObject json, String key) {
		debug("getPayLoadInteger : " + json);
		JSONObject payload = json.getJSONObject("payload");
		if (payload != null) {
			return payload.getInt(key);
		}
		return null;
	}
	
	private Boolean getPayloadBoolean(JSONObject json, String key) {
		debug("getPayLoadBoolean : " + json);
		JSONObject payload = json.getJSONObject("payload");
		if (payload != null) {
			return payload.getBoolean(key);
		}
		return null;
	}	
	
	
	//-- LG Registration part ---------------------------------------------------------------------
	
	// Here the app have to retrieve a client key.
	// 1) Send a register command to the TV, user must interact with the TV to click on "Allow" or get the PIN code display on the tv to input it in this app
	//    Once allowed, the TV send a clientKey.
	// 2) In a second part, the APP have to say hello to the TV by sending again the register request, but with the client Key received just before (step 1) or in a previous session
	//    If use the client key retrieved in a previous session, user does not need to interact again (part 1 will be skipped)
	
	private String getPairing(int pairingType, String id, String clientKey) {
		String pairingTypeStr = pairingType == PAIRING_TYPE_PROMPT ? "PROMPT" : pairingType == PAIRING_TYPE_PIN ? "PIN" : "COMBINED";
			
		String pairing = "";
		pairing += "{\n";
		pairing += "	\"type\":\"register\",\n";
		pairing += "	\"id\":\""+id+"\",\n";
		pairing += "	\"payload\":{\n";
		pairing += "		\"forcePairing\":false,\n";
		pairing += "		\"pairingType\":\""+pairingTypeStr+"\",\n";
		if (clientKey != null) {
		pairing += "		\"client-key\": \""+clientKey+"\",\n";	
		}		
		pairing += "		\"manifest\":{\n";
		pairing += "			\"manifestVersion\":1,\n";
		pairing += "			\"permissions\":[\n";
		pairing += "				\"APP_TO_APP\",\n";
		pairing += "				\"CLOSE\",\n";
		pairing += "				\"CONTROL_AUDIO\",\n";
		pairing += "				\"CONTROL_DISPLAY\",\n";
		pairing += "				\"CONTROL_INPUT_JOYSTICK\",\n";
		pairing += "				\"CONTROL_INPUT_MEDIA_PLAYBACK\",\n";
		pairing += "				\"CONTROL_INPUT_MEDIA_RECORDING\",\n";
		pairing += "				\"CONTROL_INPUT_TEXT\",\n";
		pairing += "				\"CONTROL_INPUT_TV\",\n";		
		pairing += "				\"CONTROL_MOUSE_AND_KEYBOARD\",\n";
		pairing += "				\"CONTROL_POWER\",\n";		
		pairing += "				\"LAUNCH\",\n";
		pairing += "				\"LAUNCH_WEBAPP\",\n";		
		pairing += "				\"READ_APP_STATUS\",\n";	
		pairing += "				\"READ_COUNTRY_INFO\",\n";		
		pairing += "				\"READ_CURRENT_CHANNEL\",\n";
		pairing += "				\"READ_INPUT_DEVICE_LIST\",\n";		
		pairing += "				\"READ_INSTALLED_APPS\",\n";
		pairing += "				\"READ_LGE_SDX\",\n";
		pairing += "				\"READ_LGE_TV_INPUT_EVENTS\",\n";
		pairing += "				\"READ_NETWORK_STATE\",\n";		
		pairing += "				\"READ_NOTIFICATIONS\",\n";
		pairing += "				\"READ_POWER_STATE\",\n";		
		pairing += "				\"READ_RUNNING_APPS\",\n";
		pairing += "				\"READ_TV_CHANNEL_LIST\",\n";		
		pairing += "				\"READ_TV_CURRENT_TIME\",\n";		
		pairing += "				\"READ_UPDATE_INFO\",\n";
		pairing += "				\"SEARCH\",\n";		
		pairing += "				\"TEST_OPEN\",\n";
		pairing += "				\"TEST_PROTECTED\",\n";
		pairing += "				\"TEST_SECURE\",\n";
		pairing += "				\"UPDATE_FROM_REMOTE_APP\",\n";		
		pairing += "				\"WRITE_NOTIFICATION_ALERT\",\n";
		pairing += "				\"WRITE_NOTIFICATION_TOAST\",\n";		
		pairing += "				\"WRITE_SETTINGS\"\n";
		pairing += "			]\n";
		pairing += "		}\n";
		pairing += "	}\n";
		pairing += "}\n";		

		return pairing;
	}
	
	
	//Part 1 of registration
	private boolean retrieveClientKey(int pairingType) {
		if (_clientKey == null) { //if already have a client key, no need to register!
			JSONObject json = new JSONObject(getPairing(pairingType, "idRegistration1", null));
			this.sendText(json.toString());
		        
			if (pairingType == PAIRING_TYPE_PIN) {
				String pin = JOptionPane.showInputDialog("Please input pin code: ");
				this.sendCommand("idSendPin", "ssap://pairing/setPin", "pin", pin);
			} else {

				while (_clientKey == null) {
					JSONObject response = this.waitResponse("idRegistration1");
					if (response != null) {
				
						if ("registered".compareTo(response.getString("type")) == 0) {
							_clientKey = getPayloadString(response, "client-key");
							if (_clientKey != null) {
								debug("Registered with key " + _clientKey);
							} else {
								debug("Registration faile (unable to retrieve client key)");
								return false;
							}
						} else if ("error".compareTo(response.getString("type")) == 0) {
							debug("Not registered error is : '"+response.getString("error")+"'");
							return false;
						} else if ("response".compareTo(response.getString("type")) == 0) {
							debug("Get a waiting answer...");
						}
					}
				}
			}
		}
		return true;
	}
	
	//Part 2 of registration
	private boolean checkClientKey(int pairingType) {
		String pairing = getPairing(pairingType, "idRegistration2", _clientKey);
        this.sendText(pairing);
        JSONObject response2 = this.waitResponse("idRegistration2");
        
        if (response2 != null) {
            if ("registered".compareTo(response2.getString("type")) == 0) {
            	String clientKey = getPayloadString(response2, "client-key");
            	if (_clientKey.compareTo(clientKey) == 0) {
            		debug("Registration successfull!");
            	} else {
            		debug("Registration failed!");
            		return false;
            	}
            } else if ("error".compareTo(response2.getString("type")) == 0) {
            	debug("Not registered error is : '"+response2.getString("error")+"'");
            	return false;
            }
            return true;
        }
        return false;
	}	
	
	
	//-- Websocket listner ------------------------------------------------------------------------
	
	private String webSocketName(WebSocket webSocket) {
		if (webSocket == _webSocket) {
			return "main";
		} else if (webSocket == _webSocketMouse) {
			return "mouse";
		//} else if (webSocket == _webSocketKeyboard) {
		//	return "keyboard";
		} else {
			return "???";
		}
	}
	
	@Override
	public void onOpen(WebSocket webSocket) {
		debug("Websocket("+webSocketName(webSocket)+").onOpen : connected");
        Listener.super.onOpen(webSocket);      
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
    	debug("Websocket("+webSocketName(webSocket)+").onError : " + error.getMessage());
        Listener.super.onError(webSocket, error);
    }

    private StringBuilder _receivedText = new StringBuilder();
    
    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
    	//debug("Websocket("+webSocketName(webSocket)+").onText ("+last+") : " + data);
    	
    	if (webSocket == _webSocket) {    	
	    	_receivedText.append(data);
	    	if (last) {
	    		receiveResponse(_receivedText.toString());
	    		_receivedText = new StringBuilder();
	    	}
    	}
    	
        return Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
    	debug("Websocket("+webSocketName(webSocket)+").onPong: " + new String(message.array()));
        return Listener.super.onPong(webSocket, message);
    }
    
    //-- Debug ------------------------------------------------------------------------------------
    
    private void debug(String message) {
    	if (_debug) {
    		String msg = message.replaceAll("\n", "").replaceAll("\t", " ");
    		System.out.println(msg);
    	}
    }
	
}
