package lgwebos;

import commons.Nameable;

public class LGWebOSApp implements Nameable {

	protected String _appId;
	protected String _name;
	
	public LGWebOSApp(String appId, String name) {
		_appId = appId;
		_name = name;
	}

	public String getAppId() {
		return _appId;
	}	
	
	public String getName() {
		return _name;
	}
	
	@Override
	public String toString() {
		return _name + " (" + _appId + ")";
	}
}
