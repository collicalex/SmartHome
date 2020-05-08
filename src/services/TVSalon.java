package services;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import bosesoundtouch.BoseSoundTouch;
import commons.Nameable;
import lgwebos.LGWebOS;
import lgwebos.LGWebOSApp;
import network.NetworkException;
import orangebox.Channel;
import orangebox.OrangeBox;
import orangebox.Program;
import server.Service;
import server.Utils;

/*
 * IFTTT : "Allume la télé"
 * URL   : http://xxx.xxx.xxx.xxx:xxx/tvsalon?action=power&value=on
 * 
 * IFTTT: "Éteins la télé"
 * URL   : http://xxx.xxx.xxx.xxx:xxx/tvsalon?action=power&value=off
 * 
 * IFTTT: Q: "Sur la télé $"
 *        A: "Ok je fais $ sur la télé"
 *        
 *        $ = "lance <appName>" 		--> run a TV App
 *        $ = "zappe sur <channelName>"	--> switch to channel name on the box
 *        $ = "trouve <programName>"  	--> find the program and switch to the appropriate channel on the box
 *        $ = "augmente le volume"		--> volume up
 *        $ = "baisse le volume"		--> volume down
 *        
 * URL   : http://xxx.xxx.xxx.xxx:xxx/tvsalon?action=launch&value= {{TextField}}
 *
 */

public class TVSalon extends Service {
	private LGWebOS _lgWebOS;
	private BoseSoundTouch _bose;
	private OrangeBox _orangeBox;
	
	private boolean _simulation = false;
	
	@Override
	public String getName() {
		return "TVSalon";
	}	

	@Override
	protected void init() {
		try {
			log("Add TV LG WEB OS sub service");
			_lgWebOS = new LGWebOS("XX:XX:XX:XX:XX:XX", "XXX.XXX.XXX.XXX", "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			log("Add BOSE SOUND TOUCH sub service");
			_bose = new BoseSoundTouch("XXX.XXX.XXX.XXX");
			log("Add ORANGE TV BOX sub service");
			_orangeBox = new OrangeBox("XXX.XXX.XXX.XXX");
		} catch (NetworkException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		//-- Retrieve HTTP parameters
		
		Map<String, String> query = Utils.queryToMap(httpExchange);
		String action = query.get("action");
		String value = query.get("value");
		log("Get action '" + action + "' with value '" + value + "' from " + server.Utils.getSourceIP(httpExchange));
		
		//-- Do action

		boolean result = doAction(action, value);
		
		//-- Send http response
		String okko = result ? "[OK]" : "[KO]"; 
		String response = commons.Utils.getDateTime() + okko + " Do action '" + action + "' with value '" + value + "'";
		log(okko + " Do '" + action + "'");
		
		httpExchange.getResponseHeaders().add("Content-Type", "text/plain");
		httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
	}
	
	private boolean doAction(String action, String value) {
		try {
			if (action != null) {
				return doAction_(action, value);
			} else {
				ler("No action provided");
				return false;
			}
		} catch (NetworkException | IOException e) {
			ler(e.getMessage());
			return false;
		}
	}
	
	private boolean doAction_(String action, String value) throws NetworkException, IOException {
		if ("power".compareTo(action) == 0) {
			return power(value);
		} else if ("launch".compareTo(action) == 0) {
			return launch(value);
		} else {
			ler("Unknown action '" + action + "'");
			return false;
		}
	}
	
	private boolean power(String value) throws NetworkException, IOException {
		log("Action to do : power LG tv " + value);
		if (value == null) {
			ler("No value provider for power action");
			return false;
		} else {
			if ("on".compareTo(value) == 0) {
				if (_simulation == false) {
					switchAllOn();
				}
				return true;
			} else if ("off".compareTo(value) == 0) {
				if (_simulation == false) {
					switchAllOff();
				}
				return true;				
			} else {
				ler("Unknown value '" + value + "' for power action");
				return false;
			}
		}
	}
	
	private void switchAllOn() throws NetworkException, IOException {
		if (_lgWebOS.isOff()) {
			_lgWebOS.switchOn();
		}
		if (_orangeBox.isOFF()) {
			_orangeBox.switchONOFF();
		}
	}
	
	private void switchAllOff() throws NetworkException, IOException {
		if (_lgWebOS.isOn()) {
			_lgWebOS.switchOff();
		}
		if (_orangeBox.isON()) {
			_orangeBox.switchONOFF();
		}
	}
	
	
	private boolean launch(String something) throws IOException {
		something = something.trim();
		if (something.startsWith("et ")) {
			something = something.substring(3);
		}
		log("Action to do : " + something);
		String firstWord = commons.Utils.firstWord(something);
		if (firstWord.compareTo("lance") == 0) {
			return launchApp(commons.Utils.otherWords(something));
		} else if (firstWord.compareTo("zappe") == 0) {
			String channelName = commons.Utils.otherWords(commons.Utils.otherWords(something));
			return displayChannel(channelName);
		} else if (firstWord.compareTo("trouve") == 0) {
			return displayProgram(commons.Utils.otherWords(something));	
		} else if ((firstWord.compareTo("baisse") == 0) && (something.endsWith("volume"))) {
			return volume(-1);
		} else if ((firstWord.compareTo("augmente") == 0) && (something.endsWith("volume"))) {
			return volume(+1);
		} else {
			ler("Unknown action '" + firstWord + "' for a launch action");
			return false;
		}
	}
	
	private boolean launchApp(String appName) {
		log("Action to do : launch app on the TV : '" + appName + "'");
		List<LGWebOSApp> tvApps = _lgWebOS.listApp();
		LGWebOSApp tvApp = (LGWebOSApp) commons.Utils.smartSearch((List<Nameable>)(List<?>)tvApps, appName, 0.75);
		if (tvApp != null) {
			log("launchApp Ask   '" + appName + "'");
			log("launchApp Found '" + tvApp.getName() + "'");
			if (_simulation == false) {
				_lgWebOS.launchApp(tvApp);
			}
			return true;			
		} else {
			return false;
		}
	}
	
	
	private boolean displayChannel(String channelName) throws IOException {
		log("Action to do : swith to channel on the box '" + channelName + "'");
		List<Channel> channels = _orangeBox.getChannels();
		Channel channel = (Channel) commons.Utils.smartSearch((List<Nameable>)(List<?>)channels, channelName, 0.75);
		if (channel != null) {
			log("displayChannel Ask   '" + channelName + "'");
			log("displayChannel Found '" + channel.getName() + "' on epgid " + channel.getEPGID());
			if (_simulation == false) {
				_orangeBox.switchTo(channel.getEPGID());
			}
			return true;
		} else {
			return false;
		}
	}
	
	private boolean displayProgram(String programName) throws IOException {
		log("Action to do : swith to program on the box '" + programName + "'");
		List<Program> programs = _orangeBox.getPrograms();
		Program program = (Program) commons.Utils.smartSearch((List<Nameable>)(List<?>)programs, programName, 0.75);
		if (program != null) {
			Channel channel = _orangeBox.getChannel(program.getEPGID());
			String channelStr = channel == null ? "???" : channel.getName();
			log("displayProgram Ask   '" + programName + "'");
			log("displayProgram Found '" + program.getName() + "' on channel '" + channelStr + "'");
			if (_simulation == false) {
				_orangeBox.switchTo(program.getEPGID());
			}
			return true;
		} else {
			return false;
		}
	}
	
	private boolean volume(int way) {
		if (way > 0) {
			_lgWebOS.volumeUp();
		} else  if (way < 0) {
			_lgWebOS.volumeDown();
		} else if (way == 0) {
			_lgWebOS.setMute(true);
		}
		return true;
	}
}
