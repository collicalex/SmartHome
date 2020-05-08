package lgwebos;

public class LGWebOSExternalDevice extends LGWebOSApp {

	private String _deviceId;
	
	public LGWebOSExternalDevice(String deviceId, String appId, String name) {
		super(appId, name);
		_deviceId = deviceId;
	}
	
	public String getDeviceId() {
		return _deviceId;
	}
	
	@Override
	public String toString() {
		return _name + " (" + _deviceId + " | " + _appId + ")";
	}
}
