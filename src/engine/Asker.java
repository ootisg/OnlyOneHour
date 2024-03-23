package engine;

import java.util.ArrayList;

public class Asker {
	GameObject ask;
	ArrayList <Integer> heldKeys;
	
	public Asker (GameObject obj) {
		ask = obj;
		heldKeys = new ArrayList <Integer> ();
	}
	 
	public boolean isAsker(GameObject isHim) {
		return ask.equals(isHim);
	}
	
	public ArrayList <Integer> getKeys (){
		return heldKeys;
	}
	  
  }