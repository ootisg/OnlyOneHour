package engine;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class Transform {

	private Vector3f pos;
	private Vector2f scl;
	private float rot;
	
	public Transform () {
		this (new Vector3f (0, 0, 0), new Vector2f (1, 1), 0);
	}
	
	public Transform (Vector3f pos) {
		this (pos, new Vector2f (1, 1), 0);
	}
	
	public Transform (Vector3f pos, Vector2f scl, float rot) {
		this.pos = pos;
		this.scl = scl;
		this.rot = rot;
	}
	
	public Transform (float x, float y, float z, float scaleX, float scaleY, float rot) {
		this (new Vector3f (x, y, z), new Vector2f (scaleX, scaleY), rot);
	}
	
	public void translate (Vector3f offs) {
		pos.x += offs.x;
		pos.y += offs.y;
	}
	
	public void scale (Vector2f scl) {
		this.scl.x *= scl.x;
		this.scl.y *= scl.y;
	}
	
	public void rotate (float theta) {
		rot += theta;
	}
	
	public Vector3f getPos () {
		return pos;
	}
	
	public Vector2f getScl () {
		return scl;
	}
	
	public float getRot () {
		return rot;
	}
	
	@Override
	public String toString () {
		return "[" + pos.x + "," + pos.y + "," + pos.z + "]; [" + scl.x + "," + scl.y + "]; " + rot; 
	}
	
	@Override
	public boolean equals (Object other) {
		Transform t = (Transform)other;
		if (pos.x == t.pos.x &&
			pos.y == t.pos.y &&
			pos.z == t.pos.z &&
			scl.x == t.scl.x &&
			scl.y == t.scl.y &&
			rot == t.rot) {
			return true;
		}
		return false;
	}
	
}
