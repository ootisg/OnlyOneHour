package map;

public class VectorCollisionInfo {

	TileData colidedTile;
	double collisionX;
	double collisionY;
	int tileX;
	int tileY;
	/**
	 * generated when a vector collsion occurs
	 * @param data the tile data of the collided tile
	 * @param x the x position where the collision occured
	 * @param y the y posistion where the collision occured
	 */
	public VectorCollisionInfo (TileData data, double x, double y,int tileX,int tileY) {
		colidedTile = data;
		collisionX = x;
		collisionY = y;
		this.tileX = tileX;
		this.tileY = tileY;
	}
	/**
	 * returns the x coordinate where the collision ocuurd
	 * @return the x coordinate where the collision occured
	 */
	public double getCollisionX() {
		return collisionX;
	}
	/**
	 * returns the y coordinate where the collision ocuurd
	 * @return the y coordinate where the collision occured
	 */
	public double getCollisionY() {
		return collisionY;
	}
	/**
	 * returns the tile data of the tile that was collided with it
	 * @return the tile data of the tile that was collided with it
	 */
	public TileData getData () {
		return colidedTile;
	}
	/**
	 * gets the x pos of the tile that was collided with
	 * @return the x pos of the tile that was collided with
	 */
	public double getTileX() {
		return	tileX;
	}
	/**
	 * gets the y pos of the tile that was collided with
	 * @return the y pos of the tile that was collided with
	 */
	public double getTileY() {
		return	tileY;
	}
		
}
