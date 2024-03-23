package map;

import json.JSONArray;
import json.JSONObject;

public abstract class MapLayer {
	
	public MapLayer () {
		//No-arg constructor for factory
	}
	
	public MapLayer (JSONObject layer) {
		//TODO read misc. layer properties
	}
	
	public void draw () {
		//Override to implement drawing
	}
	
	public abstract String getTypeId ();
	
}
