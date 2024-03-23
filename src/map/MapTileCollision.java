package map;

public class MapTileCollision {
	public MapTile tile;
	public double collisionX;
	public double collisionY;
	public MapTileCollision (MapTile tile, double collisionX, double collisionY) {
		this.tile = tile;
		this.collisionX = collisionX;
		this.collisionY = collisionY;
	}
}
