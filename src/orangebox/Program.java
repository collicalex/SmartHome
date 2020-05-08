package orangebox;

import commons.Nameable;

public class Program implements Nameable {

	private String _name;
	private int _epgid;
	private long _startTime;
	private long _duration;
	
	public Program(String name, int epgid, long startTime, long duration) {
		_name = name;
		_epgid = epgid;
		if (startTime < 10000000000L) { //in second
			_startTime = startTime * 1000;
			_duration = duration * 1000;
		} else { //in millisecond
			_startTime = startTime;
			_duration = duration;
		}
	}
	
	public String getName() {
		return _name;
	}
	
	public int getEPGID() {
		return _epgid;
	}
	
	private long endTime() {
		return _startTime + _duration;
	}
	
	public boolean isLive() {
		long currentTime = System.currentTimeMillis();
		return ((_startTime < currentTime) && (endTime() > currentTime));
	}
	
	public boolean isFuture() {
		long currentTime = System.currentTimeMillis();
		return (_startTime > currentTime);
	}
	
	public boolean isPast() {
		long currentTime = System.currentTimeMillis();
		return (endTime() < currentTime);
	}
	
	@Override
	public String toString() {
		return _name + " (" + _epgid + ")";
	}
	
}
