package orangebox;

import commons.Nameable;

public class Channel implements Nameable {
	private String _name;
	private int _epgid;
	
	public Channel(String name, int epgid) {
		_name = name;
		_epgid = epgid;
	}
	
	public String getName() {
		return _name;
	}
	
	public int getEPGID() {
		return _epgid;
	}
	
	@Override
	public String toString() {
		return _name + " (" + _epgid + ")";
	}
}
