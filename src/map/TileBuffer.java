package map;

import engine.Sprite;

public class TileBuffer {
	public double collisionX;
	public double collisionY;
	public Sprite spriteUsed;
	public MapTile mapTile;
	public boolean enabled;
	public TileBuffer () {
		collisionX = -1;
		collisionY = -1;
		spriteUsed = null;
		mapTile = new MapTile (null, -1, -1);
		enabled = false;
	}
}