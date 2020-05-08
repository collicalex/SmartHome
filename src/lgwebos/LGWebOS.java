package lgwebos;

import java.util.List;

import network.NetworkException;


public class LGWebOS {
	
	private LGWebOSCore _lgTV;
	
	public LGWebOS(String macAddress, String ipAddress, String clientKey) throws NetworkException {
		_lgTV = new LGWebOSCore(macAddress, ipAddress, clientKey);
	}
	
	public LGWebOS(String macAddress, String ipAddress) throws NetworkException {
		_lgTV = new LGWebOSCore(macAddress, ipAddress);
	}
	
	//-- ON / OFF -------------------------------------------------------------
	
	public void switchOn() throws NetworkException {
		_lgTV.switchOn();
		_lgTV.connect();
		_lgTV.register(LGWebOSCore.PAIRING_TYPE_PROMPT);
	}
	
	public boolean isOn() {
		return _lgTV.isOn();
	}
	
	public boolean isOff() {
		return _lgTV.isOff();
	}
	
	public void switchOff() {
		_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
		_lgTV.switchOff();
		_lgTV.disconnect();
	}
	
	//-- Volume ---------------------------------------------------------------
	
	public Integer getVolume() {
		_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
		return _lgTV.getVolume();
	}

	public Boolean isMuted() {
		_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
		return _lgTV.isMuted();
	}	
	
	public void volumeUp() {
		_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
		_lgTV.volumeUp();
	}
	
	public void volumeDown() {
		_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
		_lgTV.volumeDown();
	}
	
	public void setMute(boolean muted) {
		_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
		_lgTV.setMute(muted);
	}
	
	public void setVolume(int volume) {
		_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
		_lgTV.setVolume(volume);
	}
	
	//-- Mouse ----------------------------------------------------------------
	
    public void mouseClick() {
    	_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
    	_lgTV.mouseClick();
    }
    
    public void mouseMove(int dx, int dy) {
    	_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
    	_lgTV.mouseMove(dx, dy);
    }

    public void mouseMove(int dx, int dy, boolean drag) {
    	_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
    	_lgTV.mouseMove(dx, dy, drag);
    }

    public void mouseScroll(int dx, int dy) {
    	_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
   		_lgTV.mouseScroll(dx, dy);
    }    
	
    public void mouseButtonUp() {
    	_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
    	_lgTV.mouseButtonUp();
    }

    public void mouseButtonDown() {
    	_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
    	_lgTV.mouseButtonDown();
    }

    public void mouseButtonLeft() {
    	_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
    	_lgTV.mouseButtonLeft();
    }

    public void mouseButtonRight() {
    	_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
    	_lgTV.mouseButtonRight();
    }

    public void mouseButtonHome() {
    	_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
    	_lgTV.mouseButtonHome();
    }
    
    public void mouseButtonBack() {
    	_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
    	_lgTV.mouseButtonBack();
    }
    
    public void mouseButtonNone() {
    	_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
    	_lgTV.mouseButtonNone();
    }
    
    //-- Text -----------------------------------------------------------------
    
    public void inputText(String text) {
    	_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
    	_lgTV.inputText(text);
    }
    
    public void inputEnter() {
    	_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
    	_lgTV.inputEnter();
    }
    
    public void inputBackspace(int count) {
    	_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
    	_lgTV.inputBackspace(count);
    }
	
	//-- Applications ---------------------------------------------------------
	
    public List<LGWebOSApp> listAppAndExternalDevices() {
		_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
		return _lgTV.listAppAndExternalDevices();
    }
    
	public List<LGWebOSApp> listApp() {
		_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
		return _lgTV.listApp();
	}
	
	public String launchApp(LGWebOSApp app) {
		_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
		return _lgTV.launchApp(app.getAppId());
	}
	
	public String launchAppById(String id) {
		_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
		return _lgTV.launchApp(id);
	}
	
	public String launchAppByName(String name) {
		_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
		List<LGWebOSApp> apps = _lgTV.listApp();
		if (apps != null) {
			name = name.toLowerCase();
			for (LGWebOSApp app : apps) {
				if (app.getName().toLowerCase().compareTo(name) == 0) {
					return this.launchApp(app);
				}
			}
		}
		return null;
	}
	
	public Boolean isAppRunning(String sessionId) {
		_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
		return _lgTV.isAppRunning(sessionId);
	}
	
	public Boolean isAppVisible(String sessionId) {
		_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
		return _lgTV.isAppVisible(sessionId);
	}
	
	public String getForegroundAppId() {
		_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
		return _lgTV.getForegroundAppId();
	}
	
	//-- Input ----------------------------------------------------------------
	
	public List<LGWebOSExternalDevice> listExternalDevices() {
		_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
		return _lgTV.listExternalDevices();		
	}
	
	public void switchToExternalDevice(String deviceId) {
		_lgTV.connectANDregister(LGWebOSCore.PAIRING_TYPE_PROMPT);
		_lgTV.switchToExternalDevice(deviceId);
	}
	
	//-- Test -----------------------------------------------------------------
	
	public void test(String str) {
		_lgTV.test(str);
	}
	
}
