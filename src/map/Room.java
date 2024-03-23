 package map;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

import engine.GameAPI;
import engine.GameObject;
import engine.GameObject.HitboxInfo;
import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import json.JSONUtil;
import engine.ObjectHandler;
import engine.GameLoop;
import engine.Sprite;
import engine.SpriteParser;

public class Room {
	
	private static ArrayList <TileData> dataList;  // sorted by numeric ID
	private static HashMap <String,TileData> nameList; //indexed by tile name
	private static ArrayList <BufferedImage> tileIcons; //yeah? (sorted by numeric ID)

	private static HashMap <Long, ArrayList<GameObject>> mapObjects = new HashMap <Long, ArrayList<GameObject>>(); //objects that should be considered collisions with the map
	
	private static ArrayList<GameObject> mapObjectsUsed = new ArrayList<GameObject> ();
	
	private static ArrayList <Background> backgrounds;// all backgrounds sorted by layers
	
	private static ArrayList <TileEntitiy> tileEntitiys; //objects that contain code related to certain tiles
	private static HashMap <Long, TileEntitiy> positionToEntitiys;
	
	private static MapChungus [][] mapChungi; 
	
	private static int scrollX;
	private static int scrollY;
	
	private static int updateX = -1;
	private static int updateY = -1;
	
	private static byte [] inData;
	private static int readPos;
	
	private static int mapWidth;
	private static int mapHeight;
	private static int numLayers;
	
	public static int collisionLayer = 0;
	
	private static int chungusWidth = 20;
	private static int chungusHeight = 15;
	
	private static double gravity = .65625;
	
	private static boolean isLoaded = false;
	
	private static String roomName = "unloaded";
	
	private static int startMs;
	private static int bgSpeed;
	
	private static int tileWidth = 16;
	private static int tileHeight = 16;
	
	public static final int SPECIAL_TILE_ID = -1;
	public static final int ZREO = 0;
	
	public static final boolean SHOW_MAP_OBJS = false;
	
	//Stuff from (new) Room2
	private static ArrayList<MapLayer> layerData;

	private static ArrayList<MapObject> allMapObjs;
	private static HashMap<String, MapObject> mapObjNameMap;
	
	private Room () {
		//Non-electric boogaloo
	}
	
	private static boolean isBetween (double val, double bound1, double bound2) {
		if ((bound1 - val <= 0 && bound2 - val >= 0) || (bound1 - val >= 0 && bound2 - val <= 0)) {
			return true;
		}
		return false;
	}
	
	public static int getTile (int layer, int tileX, int tileY) {
		MapLayer ml = getMapLayers ().get (layer);
		if (ml instanceof TileLayer) { 
			return ((TileLayer)ml).tileData[tileY][tileX];
		} else {
			return 0;
		}
	}
	
	public static void setTile (int tid, int layer, int tileX, int tileY) {
		MapLayer ml = getMapLayers ().get (layer);
		if (ml instanceof TileLayer) { 
			((TileLayer)ml).tileData[tileY][tileX] = tid;
		}
	}
	
	private static boolean checkTileCollision (int tileX, int tileY, boolean flip) {
		Graphics2D g = (Graphics2D)GameLoop.wind.getBufferGraphics ();
		g.setColor(new Color(0x0000FF));
		if (flip) {
			//g.draw3DRect(tileY*16, tileX*16, 16, 16, true);
			return dataList.get(getTile (collisionLayer, tileY, tileX)).isSolid ();
		} else {
			//g.draw3DRect(tileX*16, tileY*16, 16, 16, true);
			return dataList.get(getTile (collisionLayer, tileX, tileY)).isSolid ();
		}
	}
	private static TileData checkTileData (int tileX, int tileY, boolean flip) {
		if (flip) {
			return dataList.get(getTile (collisionLayer, tileY, tileX));
		} else {
			return dataList.get(getTile (collisionLayer, tileX, tileY));
		}
	}
	/**
	 * does a collision using vectors
	 * @param x1 the x pos of the start of the line
	 * @param y1 the y pos of the start of the line
	 * @param x2 the x pos of the end of the line
	 * @param y2 the y pos of the end of the line
	 * @return true if the vector colides with a solid tile at any point
	 */
	public static boolean isColliding (double x1, double y1, double x2, double y2) {
	if (getCollisionInfo(x1,y1,x2,y2) == null) {
		return false;
		} else {
		return true;
		}
	}
	
	public static long toPackedLong (int x, int y) {
		long a = x;
		long b = y;
		a |= b << 32;
		return a;
	}
	
	public static VectorCollisionInfo getCollisionInfo (double x1, double y1, double x2, double y2) {
		
		//DIVIDE
		//TODO evaluate the usefulness of this method when TILE_WIDTH != TILE_HEIGHT
		y1 /= tileWidth;
		y2 /= tileWidth;
		x1 /= tileWidth;
		x2 /= tileWidth;
		
		//The coordinates of the tile that we are currently checking for collision
		int tileX = (int) x1;
		int tileY = (int) y1;
		
		//Easy peasy
		if (checkTileCollision (tileX, tileY, false)) {
			return new VectorCollisionInfo(checkTileData(tileX,tileY,false),x1*tileWidth,y1*tileHeight,tileX,tileY);
		}
		
		//Flipped mode
		boolean flipped = false;
		if (Math.abs(y2 - y1) > Math.abs(x2 - x1)) {
			flipped = true;
			double temp = y2;
			y2 = x2;
			x2 = temp;
			temp = y1;
			y1 = x1;
			x1 = temp;
			int tempp = tileY;
			tileY = tileX;
			tileX = tempp;
		}
		int dir = 1;
		if (x2< x1 ) {
			dir = -1;
		}
		//Set the slope
		double m = (y2 - y1) / (x2 - x1); //THIS IS THE SLOPE JEFFREY
		double b = y1 - m * x1; 
		//Snap to next tile
		//stepX and stepY are the current point on the line that we are checking
		double stepX = x1;
		double stepY = y1;
		int steps = 0;
		try {
			while (true) {
				if ((m >= 0 && dir == 1) || (m<= 0 && dir == -1)) {
					if (steps != 0 && !(isBetween (stepY, y1, y2) && isBetween (stepX, x1, x2))) {
						return null;
					}
					if (stepX % 1 == 0) {
						stepX += dir;
					} else {
						if (dir == 1) {
						stepX = Math.ceil (stepX);
						} else {
						stepX = Math.floor (stepX);
						}
					}
					int tempY = (int)stepY;
					stepY = m * stepX + b;
					if ((int)stepY > tempY) {
						tileY++;
						if (checkTileCollision (tileX, tileY, flipped)) {
							stepY = tileY;
							stepX = (stepY - b) / m;
							if (isBetween (stepY, y1, y2) && isBetween (stepX, x1, x2)) {
								//TODO will crash with tileEnitiys
								double usedXCol = stepX*tileWidth;
								double usedYCol = stepY*tileWidth;
								int usedTileX = tileX;
								int usedTileY = tileY;
								if (flipped) {
									double temp = usedXCol;
									int tempp = tileX;
									usedXCol = usedYCol;
									usedYCol = temp;
									usedTileX = usedTileY;
									usedTileY = tempp;
								}
								return new VectorCollisionInfo(checkTileData(tileX,tileY,false),usedXCol,usedYCol,usedTileX,usedTileY);
							}
							return null;
						}
					}
					tileX= tileX + dir;
					if (checkTileCollision (tileX, tileY, flipped)) {
						stepX = tileX;
						if (dir == -1) {
							stepX++;
						}
						stepY = m * stepX + b;
						if (isBetween (stepY, y1, y2) && isBetween (stepX, x1, x2)) {
							double usedXCol = stepX*tileWidth;
							double usedYCol = stepY*tileWidth;
							int usedTileX = tileX;
							int usedTileY = tileY;
							if (flipped) {
								double temp = usedXCol;
								int tempp = tileX;
								usedXCol = usedYCol;
								usedYCol = temp;
								usedTileX = usedTileY;
								usedTileY = tempp;
							}
							return new VectorCollisionInfo(checkTileData(tileX,tileY,false),usedXCol,usedYCol,usedTileX,usedTileY);
						}
						return null;
					}
				} else {
					if (steps != 0 && !(isBetween (stepY, y1, y2) && isBetween (stepX, x1, x2))) {
						return null;
					}
					if (stepX % 1 == 0) {
						stepX += dir;
					} else {
						if (dir == 1) {
							stepX = Math.ceil (stepX);
							} else {
							stepX = Math.floor (stepX);
							}
					}
					int tempY = (int)stepY;
					stepY = m * stepX + b;
					if ((int)stepY < tempY) {
						tileY--;
						if (checkTileCollision (tileX, tileY, flipped)) {
							stepY = tileY + 1;
							stepX = ((stepY) - b) / m;
							if (isBetween (stepY, y1, y2) && isBetween (stepX, x1, x2)) {
								double usedXCol = stepX*tileWidth;
								double usedYCol = stepY*tileWidth;
								int usedTileX = tileX;
								int usedTileY = tileY;
								if (flipped) {
									double temp = usedXCol;
									int tempp = tileX;
									usedXCol = usedYCol;
									usedYCol = temp;
									usedTileX = usedTileY;
									usedTileY = tempp;
								}
								return new VectorCollisionInfo(checkTileData(tileX,tileY,false),usedXCol,usedYCol,usedTileX,usedTileY);
							}
							return null;
						}
					}
					tileX = tileX + dir;
					if (checkTileCollision (tileX, tileY, flipped)) {
						stepX = tileX;
						if (dir == -1) {
							stepX++;
						}
						stepY = m * stepX + b;
						if (isBetween (stepY, y1, y2) && isBetween (stepX, x1, x2)) {
							double usedXCol = stepX*tileWidth;
							double usedYCol = stepY*tileWidth;
							int usedTileX = tileX;
							int usedTileY = tileY;
							if (flipped) {
								double temp = usedXCol;
								int tempp = tileX;
								usedXCol = usedYCol;
								usedYCol = temp;
								usedTileX = usedTileY;
								usedTileY = tempp;
							}
							return new VectorCollisionInfo(checkTileData(tileX,tileY,false),usedXCol,usedYCol,usedTileX,usedTileY);
						}
						return null;
					}
				}
				steps++;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
	public static ArrayList<GameObject> getMapObjectsUsed() {
		return mapObjectsUsed;
	}

	public static void setMapObjectsUsed(ArrayList<GameObject> mapObjectsUsed) {
		Room.mapObjectsUsed = mapObjectsUsed;
	}

	public static boolean isCollidingWithTile (Rectangle obj, int tileX, int tileY) {
		
		//Get the tile icon
		BufferedImage sampleImg = tileIcons.get (getTile (collisionLayer, tileX, tileY));
		WritableRaster imgRaster = sampleImg.getAlphaRaster ();
		
		//Calculate the intersection
		Rectangle tileRect = new Rectangle (tileX * tileWidth, tileY * tileHeight, tileWidth, tileHeight);
		Rectangle isectRect = obj.intersection (tileRect);
		int spriteX = isectRect.x % tileWidth;
		int spriteY = isectRect.y % tileHeight;
		//System.out.println ("TO CHECK: " + spriteX + ", " + spriteY);
		
		//Randomly check 1% of the pixels in the tile
		int numPixelsToCheck = (isectRect.width * isectRect.height) / 100;
		for (int i = 0; i < numPixelsToCheck; i++) {
			int randX = (int)(Math.random () * isectRect.width);
			int randY = (int)(Math.random () * isectRect.height);
			int currX = randX + spriteX;
			int currY = randY + spriteY;
			//Sample here
			double[] sample = new double[1];
			imgRaster.getPixel (currX, currY, sample);
			if (sample[0] > 0) {
				return true;
			}
		}
			
		//Check every pixel in the intersection
		for (int wx = 0; wx < isectRect.width; wx++) {
			for (int wy = 0; wy < isectRect.height; wy++) {
				int currX = wx + spriteX;
				int currY = wy + spriteY;
				//Sample here
				double[] sample = new double[1];
				imgRaster.getPixel (currX, currY, sample);
				if (sample[0] > 0) {
					return true;
				}
			}
		}
		
		//No pixels were in the intersection
		return false;
		
	}
	
	/**
	 * checks to see if the given object is colliding with a solid tile
	 * @param obj the object to check
	 * @return wheather or not its touching a solid tile
	 */
	public static boolean isColliding (GameObject obj) {
		mapObjectsUsed = new ArrayList<GameObject> ();
		boolean foundCollision = false;
		for (int i = 0; i < obj.hitboxes().length; i++) {
			Rectangle hitbox = obj.hitboxes()[i];
			int startX = Math.max(hitbox.x/tileWidth,0);
			int startY = Math.max(hitbox.y/tileHeight,0);
			int endX = Math.min((hitbox.x + hitbox.width)/tileWidth,mapWidth-1);
			int endY = Math.min((hitbox.y + hitbox.height)/tileHeight,mapHeight-1);
			for (int wx = startX; wx <= endX; wx++) {
				for (int wy = startY; wy <= endY; wy++) {
					int index = getTile (collisionLayer, wx, wy);
					if (mapObjects.get(toPackedLong(wx,wy)) == null || index == SPECIAL_TILE_ID) {
						if (index == SPECIAL_TILE_ID) {
							long pos = toPackedLong (wx,wy);
							positionToEntitiys.get(pos).onCollisionIntermidete(obj);
							if (!foundCollision) {
							foundCollision = positionToEntitiys.get(pos).doesColide(obj);
							}
						} else if (dataList.get(index).isSolid() && isCollidingWithTile (obj.hitbox (), wx, wy)) {
							foundCollision = true;
						}
				} else {
					try {
					for (int b = 0; b < mapObjects.get(toPackedLong(wx,wy)).size(); b++ ) {
						
						
						if (mapObjects.get(toPackedLong(wx,wy)).get(b).isColliding(obj) && !obj.equals(mapObjects.get(toPackedLong(wx,wy)).get(b))) {
							mapObjectsUsed.add(mapObjects.get(toPackedLong(wx,wy)).get(b));
							return true;
						}
					}
					//System.out.println ("OUTSIDE: " + index);
					if (dataList.get(index).isSolid() && isCollidingWithTile (obj.hitbox (), wx, wy)) {
						foundCollision = true;
					}
					} catch (NullPointerException e) {
						return false;
					}
				}
			}
		}
	}
	return foundCollision;
}

	public static boolean isColliding (GameObject obj, String tileId) {
		mapObjectsUsed = new ArrayList<GameObject> ();
		boolean foundCollision = false;
		for (int i = 0; i < obj.hitboxes().length; i++) {
			Rectangle hitbox = obj.hitboxes()[i];
			int startX = Math.max(hitbox.x/tileWidth,0);
			int startY = Math.max(hitbox.y/tileHeight,0);
			int endX = Math.min((hitbox.x + hitbox.width)/tileWidth,mapWidth-1);
			int endY = Math.min((hitbox.y + hitbox.height)/tileHeight,mapHeight-1);
			for (int wx = startX; wx <= endX; wx++ ){
				for (int wy = startY; wy <= endY; wy++) {
					int index = getTile (collisionLayer, wx, wy);
					if (index == SPECIAL_TILE_ID) {
						long pos = toPackedLong (wx,wy);
						if (positionToEntitiys.get(pos).getData().getName().equals(tileId)) {
							if (!foundCollision) {
								foundCollision = positionToEntitiys.get(pos).doesColide(obj);
								}
							if (foundCollision) {
								positionToEntitiys.get(pos).onCollisionIntermidete(obj);
							}
						}
					} else if (dataList.get(index).getName().equals(tileId)) {
						foundCollision= true;
					}
				}
			}
		}
		return foundCollision;
}
	public static MapTile[] getCollidingTiles (GameObject obj) {
		mapObjectsUsed = new ArrayList<GameObject> ();
		ArrayList<MapTile> working =new ArrayList<MapTile>();
		for (int i = 0; i < obj.hitboxes().length; i++) {
			Rectangle hitbox = obj.hitboxes()[i];
			int startX = Math.max(hitbox.x/tileWidth,0);
			int startY = Math.max(hitbox.y/tileHeight,0);
			int endX = Math.min((hitbox.x + hitbox.width)/tileWidth,mapWidth-1);
			int endY = Math.min((hitbox.y + hitbox.height)/tileHeight,mapHeight-1);
			for (int wx = startX; wx <= endX; wx++ ){
				for (int wy = startY; wy <= endY; wy++) {
					int index = getTile (collisionLayer, wx, wy);
					if (mapObjects.get(toPackedLong(wx,wy)) == null) {
					if (index == SPECIAL_TILE_ID) {
						long pos = toPackedLong (wx,wy);
						if (positionToEntitiys.get(pos).doesColide(obj)) {
							working.add(new MapTile (positionToEntitiys.get(pos).getData(),wx*tileWidth,wy*tileHeight,positionToEntitiys.get(pos)));	
						}
					} else if (dataList.get(index).isSolid()) {
						working.add(new MapTile (dataList.get(index),wx*tileWidth,wy*tileHeight));
					}
					} else{
						try {
						for (int b = 0; b < mapObjects.get(toPackedLong(wx,wy)).size(); b++ ) {
							if (mapObjects.get(toPackedLong(wx,wy)).get(b).isColliding(obj) && !obj.equals(mapObjects.get(toPackedLong(wx,wy)).get(b))) {
								mapObjectsUsed.add(mapObjects.get(toPackedLong(wx,wy)).get(b));
								ArrayList <HitboxInfo> collidingHitboxes = mapObjects.get(toPackedLong(wx,wy)).get(b).getCollidingHitboxes(obj);
								for (int c = 0; c < collidingHitboxes.size(); c++) {
									if (index != SPECIAL_TILE_ID) {
										working.add(new MapTile (dataList.get(index),mapObjects.get(toPackedLong(wx,wy)).get(b).getX() + collidingHitboxes.get(c).getXOffset(),mapObjects.get(toPackedLong(wx,wy)).get(b).getY() + collidingHitboxes.get(c).getYOffset()));
									} else {
										long pos = toPackedLong (wx,wy);
										working.add(new MapTile (positionToEntitiys.get(pos).getData(),mapObjects.get(toPackedLong(wx,wy)).get(b).getX() + collidingHitboxes.get(c).getXOffset(),mapObjects.get(toPackedLong(wx,wy)).get(b).getY() + collidingHitboxes.get(c).getYOffset()));
									}
								}
							}
						}
						if (index != SPECIAL_TILE_ID) {
							if (dataList.get(index).isSolid()  && isCollidingWithTile (obj.hitbox (), wx, wy)) {
								working.add(new MapTile (dataList.get(index),wx*tileWidth,wy*tileHeight));
							}
						} else {
							long pos = toPackedLong (wx,wy);
							if (positionToEntitiys.get(pos).doesColide(obj)) {
								working.add(new MapTile (positionToEntitiys.get(pos).getData(),wx*tileWidth,wy*tileHeight,positionToEntitiys.get(pos)));	
							}
						}
						} catch (NullPointerException e) {
						}
					}
				}
			}
		}
		return working.toArray(new MapTile[0]);
	}
	public static MapTile[] getCollidingTiles (GameObject obj, String tileName) {
		mapObjectsUsed = new ArrayList<GameObject> ();
		ArrayList<MapTile> working =new ArrayList<MapTile>();
		for (int i = 0; i < obj.hitboxes().length; i++) {
			Rectangle hitbox = obj.hitboxes()[i];
			int startX = Math.max(hitbox.x/tileWidth,0);
			int startY = Math.max(hitbox.y/tileHeight,0);
			int endX = Math.min((hitbox.x + hitbox.width)/tileWidth,mapWidth-1);
			int endY = Math.min((hitbox.y + hitbox.height)/tileHeight,mapHeight-1);
			for (int wx = startX; wx <= endX; wx++ ){
				for (int wy = startY; wy <= endY; wy++) {
					int index = getTile (collisionLayer, wx, wy); 			
					if (index == SPECIAL_TILE_ID) {
						long pos = toPackedLong (wx,wy);
						if (positionToEntitiys.get(pos).getData().getName().equals(tileName)) {
							working.add(new MapTile (positionToEntitiys.get(pos).getData(),wx*tileWidth,wy*tileHeight));	
						}
					} else if (dataList.get(index).getName().equals(tileName)) {
						working.add(new MapTile (dataList.get(index),wx*tileWidth,wy*tileHeight));
					}
				}
			}
		}
		return working.toArray(new MapTile[0]);
	}
	/**
	 * returns all tiles colliding with this solid or not
	 * @param obj the object to check against
	 * @return all tiles inside that object
	 */
	public static MapTile[] getAllCollidingTiles (GameObject obj) {
		ArrayList<MapTile> working =new ArrayList<MapTile>();
		for (int i = 0; i < obj.hitboxes().length; i++) {
			Rectangle hitbox = obj.hitboxes()[i];
			int startX = Math.max(hitbox.x/tileWidth,0);
			int startY = Math.max(hitbox.y/tileHeight,0);
			int endX = Math.min((hitbox.x + hitbox.width)/tileWidth,mapWidth-1);
			int endY = Math.min((hitbox.y + hitbox.height)/tileHeight,mapHeight-1);
			for (int wx = startX; wx <= endX; wx++ ){
				for (int wy = startY; wy <= endY; wy++) {
					int index = getTile (collisionLayer, wx, wy);
					if (index == SPECIAL_TILE_ID) {
						long pos = toPackedLong (wx,wy);
							working.add(new MapTile (positionToEntitiys.get(pos).getData(),wx*tileWidth,wy*tileHeight));	
					} else {
						working.add(new MapTile (dataList.get(index),wx*tileWidth,wy*tileHeight));
					}
				}
			}
		}
		return working.toArray(new MapTile[0]);
	}
	/**
	 * returns true if the specified tile is a solid tile
	 * @param layer the layer the tile is on
	 * @param x the x coordinate of the tile
	 * @param y the y coordinate of the tile
	 * @return true if its solid like a rock or my mixtape
	 */
	public static boolean isSolid (int layer,int x, int y) {
		return dataList.get(getTile (collisionLayer, x, y)).isSolid();
	}
	public static ArrayList<TileEntitiy> getEntitiys(){
		return tileEntitiys;
	}
	/**
	 * gets the chungus that contains the tile at this position
	 * @param x the x position of the tile in question (in tiles)
	 * @param y the y position of the tile in question (in tiles)
	 * @return the map chungus asociated
	 */
	public static MapChungus getChungus (int x, int y) {	
		return mapChungi[y/chungusHeight][x/chungusWidth];
	}
	/**
	 * returns true if the specified tile is a solid tile (false if the tile is not loaded)
	 * @param name the name of the specified tile
	 * @return true if its solid like a rock or my mixtape
	 */
	public static boolean isSolid (String name) {
		try {
		return nameList.get(name).isSolid();
		} catch (NullPointerException e) {
		return false;
		}
	}
	/**
	 * renders the map and associated chungi
	 */
	public static void render () {
		
		if (updateX != -1) {
			if (updateX > scrollX) {
				if (updateX + GameLoop.wind.getResolution()[0] < mapWidth * Room.tileWidth) {
					scrollX = updateX;	
				} else {
					scrollX = (mapWidth * tileWidth) - GameLoop.wind.getResolution()[0];
					if (scrollX < 0) {
						scrollX = 0;
					}
				}
			} else {
				if (updateX > 0) {
					scrollX = updateX;	
				} else {
					scrollX =0;
				}
			}
			updateX = -1;
		}
		if (updateY != -1) {
			if (updateY > scrollY) {
				if (updateY + GameLoop.wind.getResolution()[1] < mapHeight * Room.tileHeight) {
					scrollY = updateY;	
				} else {
					scrollY = (mapHeight * tileHeight) - GameLoop.wind.getResolution()[1];
					if (scrollY < 0) {
						scrollY = 0;
					}
				}
			} else {
				if (updateY > 0) {
				
					scrollY = updateY;	
				} else {
					scrollY =0;
				}
			}
		
			updateY = -1;
		}
		Rectangle viewport = new Rectangle (scrollX - 20,scrollY - 20,GameLoop.wind.getResolution()[0] + 40,GameLoop.wind.getResolution()[1] + 40);
		if (isLoaded()) {
			//runs code on tileEntitiys
			for (int i = 0; i < tileEntitiys.size();i++) {
				tileEntitiys.get(i).frameEvent();
			}
		for (int wy = 0; wy < mapChungi.length; wy++) {
			for (int wx = 0; wx < mapChungi[0].length; wx++) {
				MapChungus currentChungus = mapChungi[wy][wx];
				
				Rectangle chungtangle = new Rectangle (currentChungus.getX()*tileWidth,currentChungus.getY()*tileHeight,chungusWidth*tileWidth,chungusHeight*tileHeight);
				if (!viewport.intersects(chungtangle)) {
					
					if (!currentChungus.isFree()) {
						currentChungus.freeImage();
						currentChungus.invalidate();
					}
				} else {
					currentChungus.draw();
				}
			}
		}
		}
	}
	
	/**
	 * loads the map file at the given filepath
	 * @param path the filepath to the map file
	 * @return wheater or not the loading was succesful
	 */
	public static boolean loadRoom (String path) {
		//START OF HEADER
		//Bytes 0-3: RMF# (# is version number)
		//Bytes 4-7: Map width, in tiles
		//Bytes 8-11: Map height, in tiles
		//Bytes 12-15: Number of layers
		//Bytes 16-19: Number of objects (placed)
		//END OF HEADER
		//Tileset list (background layers are excluded)
		//Object import list
		//Background list
		//Tiles
		//Object list (x, y, id, variant)
		
		//initalizes fields relating to the map
		isLoaded = false;
		dataList = new ArrayList<TileData>();
		nameList = new HashMap<String,TileData>();
		tileIcons = new ArrayList<BufferedImage> ();
		backgrounds = new ArrayList<Background>();
		tileEntitiys = new ArrayList<TileEntitiy>();
		positionToEntitiys = new HashMap<Long,TileEntitiy>();
		layerData = new ArrayList<MapLayer> ();
		allMapObjs = new ArrayList<MapObject> ();
		mapObjNameMap = new HashMap<String, MapObject> ();
		
		//purges the gameObjects
		ArrayList<ArrayList<GameObject>> objList = ObjectHandler.getChildrenByName("GameObject");
		for (int i = 0; i < objList.size (); i ++) { 
			if (objList.get (i) != null) {
				int listSize = objList.get (i).size ();
				int deletThis = 0;
				for (int j = 0; j < listSize; j ++) {
					if (objList.get (i).get (0) != null) {
						if (!objList.get (i).get (deletThis).isPersistent ()) {
							objList.get (i).get (deletThis).forget ();
						} else {
							if (roomName.equals(path)) {
								deletThis++;
							} else {
								objList.get (i).get (deletThis).forget ();
							}
						}
					}
				}
			}
		}
		
		//Import all layers
		JSONObject mapData;
		try {
			mapData = JSONUtil.loadJSONFile (path);
			loadGlobalProperties (mapData);
			loadMapTilesets (mapData);
			loadMapLayers (mapData);
			spawnAllObjs ();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		//Create tile entities
		for (int wl = 0; wl < numLayers; wl++) {
			if (layerData.get (wl) instanceof TileLayer) {
				for (int wy = 0; wy < mapHeight; wy++) {
					for (int wx = 0; wx < mapWidth; wx++) {
						if (dataList.get(getTile (wl, wx, wy)).isSpecial()) {
							TileEntitiy enity = dataList.get(getTile (wl, wx, wy)).makeEntity();
							enity.setX(wx);
							enity.setY(wy);
							enity.setLayer(wl);
							enity.setTileData(dataList.get(getTile (wl, wx, wy)));
							enity.setTexture(tileIcons.get(getTile (wl, wx, wy)));
							tileEntitiys.add(enity);
							positionToEntitiys.put(toPackedLong(wx,wy),enity);
							setTile (SPECIAL_TILE_ID, wl, wx, wy);
						} 
					}
				}
			}
		}
		
		//convert the map to a big number of map chungi
		int gridWidth = (int)Math.ceil((((double)mapWidth)/chungusWidth));
		int gridHidth = (int)Math.ceil((((double)mapHeight)/chungusHeight)); // short for width2
		mapChungi = new MapChungus [gridHidth][gridWidth];
		for (int wx = 0; wx < mapChungi[0].length; wx++) {
			for (int wy = 0; wy<mapChungi.length; wy++) {
				MapChungus workingChungus =  new MapChungus(wx*chungusWidth,wy*chungusHeight);
				int workingX = Math.min(workingChungus.getX() + chungusWidth,mapWidth);
				int workingY = Math.min(workingChungus.getY() + chungusHeight,mapHeight);
				for (int wwwwX = workingChungus.getX(); wwwwX <workingX; wwwwX ++) {
					for (int wwwwY = workingChungus.getY(); wwwwY <workingY; wwwwY ++) {
						for (int wwwwL = 0; wwwwL <numLayers; wwwwL ++) {
							if (getTile (wwwwL, wwwwX, wwwwY) == SPECIAL_TILE_ID) {
								workingChungus.addTileEntity(positionToEntitiys.get(toPackedLong(wwwwX,wwwwY)));
							}
						}
					}
				}
				mapChungi[wy][wx] = workingChungus;
			}
		}
		isLoaded = true;
		roomName = path;
		return false;
		
	}
	
	/**
	 * adds all of the tiles to the map from the given tileset
	 * @param path the path to the tileset
	 */
	public static void importTileset(JSONObject tileset) {
		
		if (tileset != null) {
			
			//Load the sprite and get its BufferedImage
			File f = new File (tileset.getString ("image"));
			String tileId = f.getName ().split ("\\.")[0];
			Sprite tilesImg = new Sprite ("resources/mapdata/" + f.getName ());
			BufferedImage rawImg = tilesImg.getFrame (0);
			int tileCount = tileset.getInt ("tilecount");
			
			//Populate the tile array from the source image
			int numColumns = tileset.getInt ("columns");
			int tIndex = 0;
			int wy = 0;
			while (tIndex < tileCount) {
				for (int wx = 0; wx < numColumns && tIndex < tileCount; wx++) {
					//Parse out the tile here
					BufferedImage tileImg = rawImg.getSubimage (tileWidth * wx, tileHeight * wy, tileWidth, tileHeight);
					TileData current = new TileData (tileId);
					dataList.add(current);
					nameList.put(tileId, current);
					BufferedImage tile = tileImg;
					tileIcons.add(tile);
					tIndex++;
				}
				wy++;
			}
			
		} else {
			
			TileData current = new TileData ("_NULL");
			dataList.add(current);
			nameList.put("_NULL", current);
			Sprite newSprite = new Sprite ("resources/mapdata/transparent.png");
			BufferedImage tile = newSprite.getFrame(0);
			tileIcons.add(tile);
			
		}

	}
	
	private static String getString (int length) {
		byte[] usedData = new byte[length];
		int endPos = readPos + length;
		int i = 0;
		while (readPos < endPos) {
			usedData [i] = inData [readPos];
			readPos ++;
			i ++;
		}
		return new String (usedData);
	}
	private static int getInteger (int bytes) {
		int total = 0;
		for (int i = 0; i < bytes; i ++) {
			int toRead = inData [readPos + i];
			if (toRead < 0) {
				toRead += 256;
			}
			total += (toRead << ((bytes - 1 - i) * 8));
		}
		readPos += bytes;
		return total;
	}
	private static String getString (char endChar) {
		int len = 0;
		int i = readPos;
		while (inData [i] != endChar) {
			len ++;
			i ++;
		}
		String str = getString (len);
		readPos ++;
		return str;
	}
	private static int getByteCount (int value) {
		int i = 0;
		while (value != 0) {
			value /= 256;
			i ++;
		}
		return i;
	}
	public static String getRoomName () {
		return roomName;
	}
	/**
	 * set the points visable on the screen to whatever
	 * @param x the x coordinate to set the veiw two 2
	 * @param y the y coordinate to set the veiw too
	 */
	public static void setView (int x, int y) {	
		updateX = x;
		updateY = y;
	}
	
	/**
	 * returns what point of the map the leftmost edge of the screen is on
	 * @return the x coordinate of the viewport
	 */
	public static int getViewX () {
		return scrollX;
	}
	/**
	 * returns what point of the map the top edge of the screen is on
	 * @return the y coordinate of the viewport
	 */
	 public static int getViewY () {
		return scrollY;
	}
	 /**
	 * returns what point of the map the leftmost edge of the screen is going to be updated to
	 */
		public static int getViewXAcurate () {
			if (updateX  != -1) {
				return updateX;
			} else {
				return scrollX;
			}
		}
		/**
		 * returns what point of the map the top edge of the screen is going to be updated to
		 */
		 public static int getViewYAcurate () {
			 if (updateX  != -1) {
					return updateY;
				} else {
					return scrollY;
				}
		}
		  
	/**
	 * returns the width of the map
	 * @return how THICC the map is
	 */
	public static int getWidth () {
		return mapWidth;
	}
	/**
	 * returns the height of the map
	 * @return how high the map is (420!)
	 */
	public static int getHeight () {
		return mapHeight;
	}
	
	/**
	 * returns the info about a specific type of tile
	 * @param tileName the name of the tile you want info about
	 * @return info about the tile if the tile is currently loaded null otherwise
	 */
	public static TileData getTileProperties(String tileName) {
		return nameList.get(tileName);
	}
	/**
	 * returns the info about a specific type of tile
	 * @param layer the layer the tile is on
	 * @param x the x coordinate of the tile
	 * @param y the y coordinate of the tile
	 * @return info about the tile specified
	 */
	public static TileData getTileProperties(int layer, int x, int y) {
		return dataList.get(getTile (layer, x, y));
	}
	/**
	 * returns the name of a speicfic tile
	 * @param layer the layer the tile is on
	 * @param x the x coordinate of the tile
	 * @param y the y coordinate of the tile
	 * @return name of the tile specified
	 */
	public static String getTileName (int layer, int x, int y) {
		try {
		return dataList.get(getTile (layer, x, y)).getName();
		} catch (IndexOutOfBoundsException e) {
		long working = toPackedLong (x,y);
		return positionToEntitiys.get(working).getType();
		}
	}
	/**
	 * returns a list of the backgrounds
	 * @return duh
	 */
	public static ArrayList<Background> getBackgroundList () {
		return backgrounds;
	}
	/**
	 * gets the gravity (in pixels/frame/frame)
	 * @return how fast things go down dude
	 */
	public static double getGravity () {
		return gravity;
	}
	
	/**
	 * sets the gravity (whoa why havent scientists done this yet)
	 * @param grav the gravity to set it too
	 */
	public static void setGravity (double grav) {
		gravity = grav;
	}
	/**
	 * returns true if the tile is within the confines of the map
	 * @param x the xpos of the tile
	 * @param y the y position of the tile
	 * @return true if the tile is in the map: false otherwise
	 */
	public static boolean tileInBounds (int x, int y) {
		return !(x> mapWidth|| y > mapHeight);
	}
	/**
	 * returns true if the map is makin bank
	 * @return true if the map is rich 
	 */
	public static boolean isLoaded () {
		return isLoaded;
	}
	public static HashMap <Long, ArrayList<GameObject>> getMapObjects() {
		return mapObjects;
	}

	public static void setMapObjects(HashMap <Long, ArrayList<GameObject>> mapObjects) {
		Room.mapObjects = mapObjects;
	}
	/**
	 * Returns the MapObjects collided with in the most recent room collision call
	 * @return YEET
	 */
	public static ArrayList<GameObject> getObjectCollisionInfo () {
		return mapObjectsUsed;
	}
	
	public static void loadGlobalProperties (JSONObject mapData) {
		
		//Map width and height
		mapWidth = mapData.getInt ("width");
		mapHeight = mapData.getInt ("height");
		
		//Tile width and height
		tileWidth = mapData.getInt ("tilewidth");
		tileHeight = mapData.getInt ("tileheight");
		
		//Other global properties of the map are unimportant/reserved for use by Tiled
		
	}
	
	public static void loadMapTilesets (JSONObject mapData) {
		importTileset (null);
		JSONArray mapSets = mapData.getJSONArray ("tilesets");
		for (int i = 0; i < mapSets.getContents ().size (); i++) {
			String fname = ((JSONObject)mapSets.get (i)).getString ("source");
			String path = "resources/mapdata/" + fname;
			try {
				JSONObject tsJSON = JSONUtil.loadJSONFile (path);
				importTileset (tsJSON);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void loadMapLayers (JSONObject mapData) {
		JSONArray mapLayers = mapData.getJSONArray ("layers");
		numLayers = mapLayers.getContents ().size ();
		for (int i = 0; i < mapLayers.getContents ().size (); i++) {
			MapLayer working = new MapLayerFactory ((JSONObject)mapLayers.get (mapLayers.getContents ().size () - i - 1)).newMapLayer ();
			loadLayer (working);
			if (working instanceof TileLayer && ((TileLayer)working).isCollisionLayer ()) {
				collisionLayer = i;
			}
		}
	}
	
	public static ArrayList<MapLayer> getMapLayers () {
		
		//Create the loaded tile list if it doesn't exist
		if (layerData == null) {
			layerData = new ArrayList<MapLayer> ();
		}
		
		//Return the tile list
		return layerData;
		
	}
	
	public static ArrayList<MapObject> getMapObjectsList () {
		
		//Create the map objects list if it doesn't exist
		if (allMapObjs == null) {
			allMapObjs = new ArrayList<MapObject> ();
		}
		
		//Return the object list
		return allMapObjs;
		
	}
	
	public static HashMap<String, MapObject> getMapObjectsMap () {
		
		//Create the map objects list if it doesn't exist
		if (mapObjNameMap == null) {
			mapObjNameMap = new HashMap<String, MapObject> ();
		}
		
		//Return the object list
		return mapObjNameMap;
		
	}
	
	public static void loadLayer (MapLayer layer) {
		getMapLayers ().add (layer);
	}
	
	public static void spawnAllObjs () {
		
		for (int i = 0; i < getMapObjectsList ().size (); i++) {
			MapObject curr = getMapObjectsList ().get (i);
			if (curr.getType ().equals ("tile")) {
				int tileTypeID = curr.getTile ();
				String objTypename = dataList.get (i).getFName ();
				GameObject newObj = ObjectHandler.getInstance (objTypename);
				if (newObj != null) {
					newObj.declare (curr.pos.x, curr.pos.y);
					if (curr.getRawProperties () != null) {
						//Set all the variant attributes as needed
						JSONArray propertyArr = curr.getRawProperties ();
						for (int j = 0; j < propertyArr.getContents ().size (); j++) {
							JSONObject working = (JSONObject)propertyArr.get (j);
							newObj.setVariantAttribute (working.getString ("name"), working.getString ("value")); //TODO use the type parameter? (probably not since variants are all Strings)
						}
					}
				} else {
					System.out.println ("Error: failed to instantiate object of type " + objTypename + " for MapObject " + curr.getId ());
				}
			}
		}
		
	}
	
	public static class MapLayerFactory extends MapLayer {

		private JSONObject layer;
		
		public MapLayerFactory (JSONObject layer) {
			super ();
			this.layer = layer;
		}
		
		public MapLayer newMapLayer () {
			
			String layerType = layer.getString ("type");
			
			switch (layerType) {
				case "tilelayer":
					return new TileLayer (layer);
				case "objectgroup":
					return new ObjectLayer (layer);
				case "imagelayer":
					return new ImageLayer (layer);
				default:
					System.out.println ("ERR: layer type not supported");
					return null;
			}
			
		}

		@Override
		public String getTypeId () {
			return null;
		}
		
	}
	
	public static class TileLayer extends MapLayer {
		
		private String layerType;
		
		private int[][] tileData;
		private boolean isCollisionLayer = false;
		
		public TileLayer (JSONObject layer) {
			
			super (layer);
			
			//Load custom attribute (IsCollisionLayer)
			JSONArray properties = (JSONArray)layer.get ("properties");
			if (properties != null) {
				for (int i = 0; i < properties.getContents ().size (); i++) {
					JSONObject curr = (JSONObject)properties.get (i);
					if (curr.getString ("name").equals ("IsCollisionLayer") && curr.get ("value").equals (Boolean.TRUE)) {
						isCollisionLayer = true;
					}
				}
			}
			
			//Processing for tile layer
			tileData = new int[mapHeight][mapWidth];
			JSONArray layerData = layer.getJSONArray ("data");
			for (int wy = 0; wy < mapHeight; wy++) {
				for (int wx = 0; wx < mapWidth; wx++) {
					tileData[wy][wx] = (int)layerData.get (wy * mapWidth + wx);
				}
			}
			
		}
		
		public boolean isCollisionLayer () {
			return isCollisionLayer;
		}
		
		@Override
		public String getTypeId () {
			return "tilelayer";
		}
		
	}
	
	public static class ObjectLayer extends MapLayer {
		
		//TODO store objects in layer instead of map?
		
		public ObjectLayer (JSONObject layer) {
			
			super (layer);
			
			JSONArray objs = layer.getJSONArray ("objects");
			for (int i = 0; i < objs.getContents ().size (); i++) {
				JSONObject curr = (JSONObject)objs.get (i);
				MapObject newObj = new MapObject (curr);
				getMapObjectsList ().add (newObj);
				if (!newObj.getName ().equals ("")) {
					getMapObjectsMap ().put (newObj.getName (), newObj);
				}
			}
			
		}

		@Override
		public String getTypeId () {
			return "objectgroup";
		}
		
	}
	
	public static class ImageLayer extends MapLayer {
		
		private String layerType;
		
		public ImageLayer (JSONObject layer) {
			
			super (layer);
			
			//TODO
			
		}
		
		@Override
		public String getTypeId () {
			return "imagelayer";
		}
		
	}
	
	//its a map chunk ... a big map chunk
	/**
	 * a portion of the map
	 * @author GOD 
	 *(god made map chungus)
	 */
	public static class MapChungus {
		ArrayList <BufferedImage> renderedImages; // the compressed images sorted by layer (and each layer contains all layers that wont cause problems)
		// if you don't understand this anymore your stupid
		 private ArrayList <Boolean> valid; // states wheather each of the images is valid
		
		int x;
		int y;
		
		ArrayList <TileEntitiy> tilelist;
		ArrayList<Integer> layerClassfications; // gives the index for the layers bufferedimage
		
		
		
		boolean incompleate = false;
		int width = chungusWidth;
		int height = chungusHeight;
		/**
		 * makes the map chungus
		 * @param x the x coordinate of the map chungus (in tiles)
		 * @param y the y coordinate of the map chungus (in tiles)
		 */
		public MapChungus (int x, int y) { 
			layerClassfications = new ArrayList<Integer>();
			tilelist = new ArrayList<TileEntitiy> ();
			renderedImages = new ArrayList<BufferedImage>();
			valid = new ArrayList<Boolean>();
			this.x = x;
			this.y = y;
			if (x + chungusWidth > mapWidth || y + chungusHeight > mapHeight) {
				incompleate =true;
				if (x + chungusWidth > mapWidth) {
					width = mapWidth - x ;
				}
				if (y + chungusHeight > mapHeight) {
					height = mapHeight - y;
				}
			}
			if (numLayers== 1) {
				layerClassfications.add(0);
				valid.add(false);
				renderedImages.add(null);
			} else {
			int mappedLayerIndex = 0;
			
			//really confusing code I tried to comment it out, but my guess is Imma have to figure this one out again someday (but Ill at least be able to do it with an RGB gamer keyboard)
			for (int i = 0; i < numLayers; i++) {
				if (isSpecialLayer(i)) {
					boolean extraImage =false;
					if (i != 0) {
						if (!isSpecialLayer (i-1)) {
							mappedLayerIndex = mappedLayerIndex + 1;
							extraImage = true;
						}
					}
					// set the preious layer classification
					layerClassfications.add(mappedLayerIndex);
					
					// makes an image for this previous layer
					valid.add(false);
					renderedImages.add(null);
					mappedLayerIndex = mappedLayerIndex +1;
					
					//makes an image for this layer if nessasary
					if (extraImage && i != numLayers - 1) {
						valid.add(false);
						renderedImages.add(null);
					}
				} else {
					//use the same layer for the previous one and the one before that
					layerClassfications.add(mappedLayerIndex);
				}
			}
			
			//makes an image for the final layer
			valid.add(false);
			renderedImages.add(null);
			
			}
			
		}
		/**
		 * returns whearer or not this layer can be rendered with all the rest
		 * @return whearer or not this layer can be rendered with all the rest
		 */
		public boolean isSpecialLayer (int layerNum) {
			if (layerData.get (layerNum) instanceof ImageLayer) {
				//TODO fix this pls
				return false;
				//if (backgrounds.get(layerNum).getScrollRateHorizontal() != 1 || backgrounds.get(layerNum).getScrollRateVertical() != 1 || backgrounds.get(layerNum).getFrames ().size () > 1){
				//	return true;
				//}
			} else {
				for (int wx = 0; wx < width; wx++) {
					for (int wy = 0; wy < height; wy++) {
						if (! ((wx + x > width) || wy + y > height )) {
							if (getTile (layerNum, wx + x, wy + y) == SPECIAL_TILE_ID) {
								return true;
							}	
						} else {
							if (wx + x > width) {
								if (wy + y > height) {
									if (getTile (layerNum, width, height) == SPECIAL_TILE_ID) {
										return true;
									}	
								} else {
									if (getTile (layerNum, width, wy + y) == SPECIAL_TILE_ID) {
										return true;
									}	
								}
							} else {
								if (getTile (layerNum, wx + x, height) == SPECIAL_TILE_ID) {
									return true;
								}
							}
						}
						
						
					}
				}
			}
			return false;
		}
		
		/**
		 * destroyes the pre rendered image when called
		 */
		public void freeImage() {
			for (int i = 0; i < renderedImages.size(); i++) {
				renderedImages.set(i, null);
			}
		}
		
		/**
		 * returns true if the image is not pre rendered
		 * @return true if the image is not pre rendered
		 */
		public boolean isFree () {
			if (renderedImages.get(0) == null) {
				return true;
			} else {
				return false;
			}
		}
		
		/**
		 * adds a special tile to the tiles rendered by this map chungus
		 * @param entity the entity to add
		 */
		public void addTileEntity (TileEntitiy entity) {
			tilelist.add(entity);
		
		}
		
		/**
		 * tells the computer that this render of the map chungus is no longer correct
		 */
		public void invalidate (int layer) {
			valid.set(layerClassfications.get(layer), false);
		}
		public void invalidate () {
			for (int i = 0; i<valid.size(); i++) {
				valid.set(i, false);
			}
		}

		/**
		 * returns wheather or not the map chungus is still valid
		 * @return wheather or not the map chungus is still valid
		 */
		public boolean isValid (int layer){
			return valid.get(layerClassfications.get(layer));	
		}
		
		/**
		 * gets the x value of the map chungi (in tiles)
		 * @return that 
		 */
		public int getX() {
			return x;
		}
		
		/**
		 * gets the y value of the map chungi (in tiles)
		 * @return that 
		 */
		public int getY() {
			return y;
		}
		
		/**
		 * draws the map chungus (and any special entitys that are associated with it)
		 */
		public void draw () {
			int currentLayer = layerClassfications.size() -1;
//			for (int l = 0; l < backgrounds.size(); l++) {
//				if (backgrounds.get(l) != null) {
//					if (backgrounds.get(l).isNewFrame()) {
//						this.invalidate(l);
//					}
//				}
//			}
				for (int l = renderedImages.size()-1; l >=0; l--) {
					if (!this.isValid(l)) {
						renderedImages.set(l,new BufferedImage (chungusWidth*tileHeight,chungusWidth*tileHeight,BufferedImage.TYPE_4BYTE_ABGR));
						Graphics g = renderedImages.get(l).getGraphics();
						while (layerClassfications.get(currentLayer)>=l) {
							if (layerData.get (currentLayer) instanceof TileLayer && !((TileLayer)layerData.get (currentLayer)).isCollisionLayer ()) {
							for (int wx = 0; wx < width; wx++) {
								for (int wy = 0; wy < height; wy++) {
									if (getTile (currentLayer, wx + x, wy + y) == SPECIAL_TILE_ID){
										g.drawImage(positionToEntitiys.get(toPackedLong(wx + x,wy + y)).getTexture(),wx*tileWidth,wy*tileHeight,null);
									
									} else {
										g.drawImage(tileIcons.get(getTile (currentLayer, wx + x, wy + y)),wx*tileHeight,wy*tileWidth,null);
									}
								}
							}
							} else {
								//TODO get this to work with animated backgrounds
								//ALSO fix this pls
								/*int rasterWidth = Math.min (x*tileWidth + chungusWidth*tileWidth,mapWidth*tileWidth);
								int rasterHeight = Math.min (y*tileHeight + chungusHeight*tileHeight,mapHeight*tileHeight);
								rasterWidth = Math.min(rasterWidth, backgrounds.get(currentLayer).getWidth()) - x*tileWidth;
								rasterHeight = Math.min(rasterHeight, backgrounds.get(currentLayer).getHeight()) - y*tileHeight;
								if (rasterWidth > 0 && rasterHeight > 0){
									int backgroundFrame = backgrounds.get(currentLayer).getCurrentFrame();
									BufferedImage working = backgrounds.get(currentLayer).getFrames ().get(backgroundFrame).getSubimage(x*tileWidth, y*tileHeight, rasterWidth, rasterHeight);
									g.drawImage(working,0,0,null);
								}*/
							}
							valid.set(layerClassfications.get(l), true);
							currentLayer = currentLayer - 1;
							if (currentLayer <0) {
								break;
							}
						}
					}
				}
				
				for (int l = renderedImages.size()-1; l >=0; l--) {
					Graphics g = GameLoop.wind.getBufferGraphics();
					g.drawImage(renderedImages.get(l),x*tileWidth - scrollX, y*tileHeight - scrollY,null);
			}
		}	
	}
	
	public static class MapObject extends GameObject {
		
		private int id;
		
		private double width;
		private double height;
		private Point pos;
		private ArrayList<Point> vertices;
		private String type;
		private int tileId;
		private String text;
		private JSONArray properties;
		private String name;
		
		public MapObject (JSONObject params) {
			assignType (params);
			loadProperties (params);
			if (SHOW_MAP_OBJS) {
				declare ();
				this.setRenderPriority (-1000000);
			}
		}
		
		public void assignType (JSONObject params) {
			
			if (params.get ("polygon") != null) {
				type = "polygon";
			} else if (params.get ("polyline") != null) {
				type = "polyline";
			} else if (params.get ("text") != null) {
				type = "text";
			} else if (params.get ("gid") != null) {
				type = "tile";
			} else if (params.get ("ellipse") != null && params.get ("ellipse").equals (Boolean.TRUE)) {
				type = "ellipse";
			} else if (params.get ("point") != null && params.get ("point").equals (Boolean.TRUE)) {
				type = "point";
			} else {
				//Rectangle by default
				type = "rectangle";
			}
			
		}
		
		public void loadProperties (JSONObject params) {
			id = params.getInt ("id");
			pos = new Point (params.getDouble ("x"), params.getDouble ("y"));
			width = params.getDouble ("width");
			height = params.getDouble ("height");
			properties = (JSONArray)params.get ("properties");
			name = (String)params.get ("name");
			JSONArray pts;
			switch (type) {
				case "polygon":
					vertices = new ArrayList<Point> ();
					pts = params.getJSONArray ("polygon");
					for (int i = 0; i < pts.getContents ().size (); i++) {
						JSONObject pt = (JSONObject)pts.get (i);
						vertices.add (new Point (pt.getDouble ("x"), pt.getDouble ("y")));
					}
					break;
				case "polyline":
					vertices = new ArrayList<Point> ();
					pts = params.getJSONArray ("polyline");
					for (int i = 0; i < pts.getContents ().size (); i++) {
						JSONObject pt = (JSONObject)pts.get (i);
						vertices.add (new Point (pt.getDouble ("x"), pt.getDouble ("y")));
					}
					break;
				case "text":
					text = params.getJSONObject ("text").getString ("text");
					break;
				case "tile":
					tileId = params.getInt ("gid");
					pos.y -= height; //For some reason, tiles SPECIFICALLY and ONLY TILES use their bottom-left corner for their position
					break;
				case "ellipse":
					//Nothing to do here
					break;
				case "point":
					//Also nothing to do here
					break;
				case "rectangle":
					//Still nothing to do here
					break;
				default:
					break;
			}
		}
		
		public boolean hasName () {
			return name == null;
		}
		
		public String getName () {
			return name;
		}
		
		public int getId () {
			return id;
		}
		
		public String getType () {
			return type;
		}
		
		public Point getPos () {
			return pos;
		}
		
		public int getTile () {
			return tileId;
		}
		
		public ArrayList<Point> getVertices () {
			return vertices;
		}
		
		public JSONArray getRawProperties () {
			return properties;
		}
		
		@Override
		public void draw () {
			Graphics2D g = (Graphics2D)GameLoop.wind.getBufferGraphics ();
			g.setColor (Color.BLACK);
			switch (type) {
			case "polygon":
				for (int i = 0; i < vertices.size (); i++) {
					Point fromPt = vertices.get (i);
					Point toPt = vertices.get ((i + 1) % vertices.size ());
					g.drawLine ((int)(pos.x + fromPt.x), (int)(pos.y + fromPt.y), (int)(pos.x + toPt.x), (int)(pos.y + toPt.y));
				}
				break;
			case "polyline":
				for (int i = 0; i < vertices.size () - 1; i++) {
					Point fromPt = vertices.get (i);
					Point toPt = vertices.get ((i + 1) % vertices.size ());
					g.drawLine ((int)(pos.x + fromPt.x), (int)(pos.y + fromPt.y), (int)(pos.x + toPt.x), (int)(pos.y + toPt.y));
				}
				break;
			case "text":
				g.drawString (text, (int)pos.x, (int)pos.y);
				break;
			case "tile":
				//Don't draw tile objects
				//Sprite spr = tiles.get (tileId).getSprite ();
				//g.drawImage (spr.getFrame (0), (int)pos.x, (int)pos.y, (int)(pos.x + width), (int)(pos.y + height), 0, 0, spr.getWidth (), spr.getHeight (), null);
				break;
			case "ellipse":
				g.drawOval ((int)pos.x, (int)pos.y, (int)width, (int)height);
				break;
			case "point":
				g.fillRect ((int)pos.x - 2, (int)pos.y - 2, 4, 4);
				break;
			case "rectangle":
				g.drawRect ((int)pos.x, (int)pos.y, (int)width, (int)height);
				break;
			default:
				break;
			}
		}

	}
	
	private static class Point {
		
		public double x;
		public double y;
		
		public Point (double x, double y) {
			this.x = x;
			this.y = y;
		}
		
	}
}