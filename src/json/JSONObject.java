package json;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class JSONObject {
	
	HashMap<String, Object> values;
	
	/**
	 * Constructs a JSONObject from the given String
	 * @param value the JSON String representing the JSONObject
	 * @throws JSONException if the given JSON String is invalid
	 */
	public JSONObject (String value) throws JSONException {
		//Parse the input text to remove whitespace
		values = new HashMap<String, Object> ();
		StringBuilder noWhitespaceBuilder = new StringBuilder ();
		boolean remove = true;
		long ct = System.nanoTime ();
		for (int i = 0; i < value.length(); i ++) {
			if (value.charAt(i) == '"') {
				remove = !remove;
			}
			if (!remove || !Character.isWhitespace(value.charAt(i))) {
				noWhitespaceBuilder.append (value.charAt(i));
			}
		}
		String noWhitespace = noWhitespaceBuilder.toString ();
		if (noWhitespace.charAt (0) == '{' && noWhitespace.charAt (noWhitespace.length () - 1) == '}') {
			noWhitespace = noWhitespace.substring (1, noWhitespace.length () - 1);
		} else {
			throw new JSONException ("Input String is not a valid JSON Object");
		}
		
		//Main parsing loop
		int i = 0;
		while (i < noWhitespace.length ()) {
			String key;
			StringBuilder keyBuilder = new StringBuilder ();
			StringBuilder working;
			
			//Parse out key
			while (noWhitespace.charAt (i) != ':') {
				keyBuilder.append (noWhitespace.charAt (i++));
			}
			key = keyBuilder.toString ();
			if (key.charAt (0) == '\"' && key.charAt (key.length () - 1) == '\"') {
				key = key.substring (1, key.length () - 1);
			} else {
				throw new JSONException ("Invalid JSON formatting");
			}
			i++;
			
			//Parse out value
			if (noWhitespace.charAt (i) == '{') {
				//Value is a JSONObject
				working = new StringBuilder ();
				working.append ("{");
				i++;
				int bracketCount = 1;
				while (true) {
					if (noWhitespace.charAt (i) == '{') {
						bracketCount++;
					} else if (noWhitespace.charAt (i) == '}') {
						if (bracketCount == 1) {
							//End of bracket
							working.append ('}');
							break;
						} else {
							bracketCount--;
						}
					}
					working.append (noWhitespace.charAt (i));
					i++;
					if (i >= noWhitespace.length ()) {
						throw new JSONException ("Missing } when parsing token " + value);
					}
				}
				values.put (key, new JSONObject (working.toString ()));
			} else if (noWhitespace.charAt (i) == '[') {
				//Value is a JSONArray
				working = new StringBuilder ();
				working.append ("[");
				i++;
				int bracketCount = 1;
				while (true) {
					if (noWhitespace.charAt (i) == '[') {
						bracketCount++;
					} else if (noWhitespace.charAt (i) == ']') {
						if (bracketCount == 1) {
							//End of bracket
							working.append ("]");
							break;
						} else {
							bracketCount--;
						}
					}
					working.append (noWhitespace.charAt (i));
					i++;
					if (i >= noWhitespace.length ()) {
						throw new JSONException ("Missing ] when parsing token " + value);
					}
				}
				values.put (key, new JSONArray (working.toString ()));
			} else {
				//Value is a JSON literal
				working = new StringBuilder ();
				while (i < noWhitespace.length () && noWhitespace.charAt (i) != ',') {
					working.append (noWhitespace.charAt (i++));
				}
				values.put (key, JSONUtil.getValueOfJSONLiteral (working.toString ()));
			}
			i++;
			if (i < noWhitespace.length () && noWhitespace.charAt (i) == ',') {
				i++;
			}
		}
	}
	
	/**
	 * Returns the Object associated with the given key
	 * @param key the key to use
	 * @return the associated Object
	 */
	public Object get (String key) {
		return values.get (key);
	}
	
	/**
	 * Returns the JSONObject associated with the given key
	 * @param key the key to use
	 * @return the associated JSONObject
	 */
	public JSONObject getJSONObject (String key) {
		return (JSONObject)values.get (key);
	}
	
	/**
	 * Returns the JSONObject associated with the given key
	 * @param key the key to use
	 * @return the associated JSONObject
	 */
	public JSONArray getJSONArray (String key) {
		return (JSONArray)values.get (key);
	}
	
	/**
	 * Returns the String associated with the given key
	 * @param key the key to use
	 * @return the associated String
	 */
	public String getString (String key) {
		return (String)values.get (key);
	}
	
	/**
	 * Returns the integer associated with the given key
	 * @param key the key to use
	 * @return the associated int
	 */
	public int getInt (String key) {
		try {
			return Integer.parseInt((String) values.get (key));
		} catch (ClassCastException e) {
			return (Integer) values.get (key);
		}
	}
	
	/**
	 * Returns the double associated with the given key
	 * @param key the key to use
	 * @return the associated double
	 */
	public double getDouble (String key) {
		try {
			return Double.parseDouble(values.get (key).toString());
		} catch (ClassCastException e) {
			return (Double) values.get (key);
		}
	}
	
	@Override
	public String toString () {
		String working = "{";
		Set<Entry<String, Object>> entries = values.entrySet ();
		Iterator<Entry<String, Object>> iter = entries.iterator ();
		while (iter.hasNext ()) {
			Entry<String, Object> entry = iter.next ();
			working += entry.getKey () + ":";
			Object o = entry.getValue ();
			if (o instanceof String) {
				working += '\"' + (String)o + '\"';
			} else {
				working += o.toString ();
			}
			if (iter.hasNext ()) {
				working += ",";
			}
		}
		working += "}";
		return working;
	}
	
}