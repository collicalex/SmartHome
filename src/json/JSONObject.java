package json;

import java.util.ArrayDeque;
import java.util.Deque;

public class JSONObject {

	private String _str;
	
	public JSONObject(String json) {
		_str = json;
	}
	
	public Boolean getBoolean(String id) {
		String str = this.getString(id);
		if (str == null) {
			return null;
		}
		return Boolean.parseBoolean(str);
	}	
	
	public Integer getInt(String id) {
		String str = this.getString(id);
		if (str == null) {
			return null;
		}
		return Integer.parseInt(str);
	}
	
	public Long getLong(String id) {
		String str = this.getString(id);
		if (str == null) {
			return null;
		}
		return Long.parseLong(str);
	}
	
	public String getString(String id) {
		Deque<Character> stack = new ArrayDeque<Character>();
		int beginIndex = -1;
		int colonIndex = -1;
		int commaIndex = -1;
		
		for (int i = 0; i < _str.length(); ++i) {
			char c = _str.charAt(i);
			
			if ((stack.size() > 0) && (c == stack.peek())) {
				if (stack.size() == 1) {
					commaIndex = i;
					if (isId(beginIndex, colonIndex, id)) {
						return getValue(colonIndex, commaIndex);
					}
					beginIndex = colonIndex = commaIndex = -1;
				}
				stack.pop();			
			} else if (c == '{') {
				stack.push('}');
			} else if (c == '[') {
				stack.push(']');
			} else if (c == '"') {
				if (stack.size() == 1) {
					if (beginIndex == -1) {
						beginIndex = i;
					}
				}
				stack.push('"');
			} else if (stack.size() == 1) { //parse only level 1, else must recurse!
				if ((beginIndex == -1) && (Character.isWhitespace(c))) {
					; //just skip whitespace
				} else if (beginIndex == -1) {
					beginIndex = i;
				} else if (c == ':') {
					colonIndex = i;	
				} else if (c == ',') {
					commaIndex = i;
					if (isId(beginIndex, colonIndex, id)) {
						return getValue(colonIndex, commaIndex);
					}
					beginIndex = colonIndex = commaIndex = -1;
				}
			}
		}
		return null;
	}
	
	public JSONObject getJSONObject(String id) {
		String str = this.getString(id);
		if (str != null) {
			return new JSONObject(str);
		} else {
			return null;
		}
	}
	
	public JSONArray getJSONArray(String id) {
		String str = this.getString(id);
		if (str != null) {
			return new JSONArray(str);
		} else {
			return null;
		}
	}
	
	private boolean isId(int beginIndex, int colonIndex, String id) {
		char a = _str.charAt(beginIndex);
		char b = _str.charAt(colonIndex-1);
		
		if ((a == b) && ((a == '"') || (a == '\''))) {
			if (id.length() != (colonIndex-beginIndex-2)) {
				return false;
			}
			for (int i = 0; i < id.length(); ++i) {
				if (id.charAt(i) != _str.charAt(i+beginIndex+1)) {
					return false;
				}
			}
		} else {
			if (id.length() != (colonIndex-beginIndex)) {
				return false;
			}
			for (int i = 0; i < id.length(); ++i) {
				if (id.charAt(i) != _str.charAt(i+beginIndex)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private String getValue(int colonIndex, int commaIndex) {
		//skip space if any
		while ((colonIndex < commaIndex) && Character.isWhitespace(_str.charAt(colonIndex+1))) {
			colonIndex++;
		}
		
		//retrieve value
		char a = _str.charAt(colonIndex+1);
		char b = _str.charAt(commaIndex-1);
		
		if ((a == b) && ((a == '"') || (a == '\''))) {
			return _str.substring(colonIndex+2, commaIndex-1);
		} else {
			return _str.substring(colonIndex+1, commaIndex);
		}
	}
	
	@Override
	public String toString() {
		return _str;
	}
}
