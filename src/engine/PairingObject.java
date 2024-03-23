package engine;

import java.util.ArrayList;
import java.util.LinkedList;

import engine.GameObject;
import engine.ObjectHandler;
import map.Room;

public class PairingObject extends GameObject {
	ArrayList <Object> pairedObject;
	public PairingObject () {
		this.setHitboxAttributes (0,0,16,16);
		this.setGameLogicPriority(-2);
	}
	@Override 
	public void frameEvent () {
		if (pairedObject == null) {
			if (ObjectHandler.checkCollisionChildren("GameObject", this).collisionOccured()) {
				pairedObject = new ArrayList <Object> ();
				pairedObject.addAll(ObjectHandler.checkCollisionChildren("GameObject", this).getCollidingObjects());
				Room.isColliding(this);
			}
		}
	}
	public void addPairedObject (Object o) {
		pairedObject.add(o);
	}
	public ArrayList <Object> getPairedObjects (){
		return pairedObject;
	}
	public ArrayList <PairingObject> getPairedParingObjects() {
		ArrayList <GameObject> working = ObjectHandler.getObjectsByName("PairingObject");
		ArrayList <PairingObject> pairedObjects = new ArrayList <PairingObject>();
		for (int i = 0; i < working.size(); i++) {
			PairingObject workingPairing = (PairingObject) working.get(i);
			if (workingPairing.getVariantAttribute("Partner").equals(this.getVariantAttribute("Partner")) && !workingPairing.equals(this)) {
				pairedObjects.add(workingPairing);
				}
			}
		return pairedObjects;
		}
	public ArrayList <Object> getPairedPairedObjects (){
		ArrayList <Object> working = new ArrayList<Object> ();
		ArrayList <PairingObject> pairedObjects = this.getPairedParingObjects();
		for (int i =0; i < pairedObjects.size(); i++) {
			working.addAll(pairedObjects.get(i).getPairedObjects());
			}
		return working;
		}
	}