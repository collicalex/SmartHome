package json;

import java.util.ArrayList;

public class JSONArray {

	private String _str;
	private ArrayList<Integer> _indexes;
	
	public JSONArray(String json) {
		_str = json;
		this.build();
	}
	
	private void build() {
		_indexes = new ArrayList<Integer>();
		_indexes.add(0);
		int level = 0;
		for (int i = 1; i < _str.length(); ++i) { //first char is [
			char c = _str.charAt(i);
			if (c == '{') {
				level++;
			} else if (c == '}') {
				level--;
			} else {
				if ((level == 0) && (c == ',')) {
					_indexes.add(i);
				}
			}
		}
		_indexes.add(_str.length()-1);
	}
	
	public int size() {
		return _indexes.size()-1;
	}

	public int length() {
		return size();
	}	
	
	public Boolean getBoolean(int index) {
		String str = this.getString(index);
		if (str == null) {
			return null;
		}
		return Boolean.parseBoolean(str);
	}	
	
	public Integer getInt(int index) {
		String str = this.getString(index);
		if (str == null) {
			return null;
		}
		return Integer.parseInt(str);
	}
	
	public String getString(int index) {
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException(index + " is out of [0;" + size() + "[");
		}
		int a = _indexes.get(index);
		int b = _indexes.get(index+1);
		return _str.substring(a+1, b);
	}
	
	public JSONObject getJSONObject(int index) {
		String str = this.getString(index);
		if (str != null) {
			return new JSONObject(str);
		} else {
			return null;
		}		
	}
	
	@Override
	public String toString() {
		return _str;
	}
}
