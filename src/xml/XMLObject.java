package xml;

public class XMLObject {

	private String _str;
	
	public XMLObject(String xml) {
		_str = xml;
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
	
	public String getString(String id) {
		int beginNodeIndex = _str.indexOf("<" + id + ">");
		if (beginNodeIndex == -1) {
			return null;
		}
		int endNodeIndex = _str.lastIndexOf("</" + id + ">");
		if (endNodeIndex == -1) {
			return null;
		}
		if (endNodeIndex <= beginNodeIndex) {
			return null;
		}
		return _str.substring(beginNodeIndex + id.length() + 2, endNodeIndex);
	}
	
	public XMLObject getXMLObject(String id) {
		String str = this.getString(id);
		if (str != null) {
			return new XMLObject(str);
		} else {
			return null;
		}
	}	
}
