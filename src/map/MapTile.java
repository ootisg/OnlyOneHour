package map;

public class MapTile {
	//Container class for map tiles
	public TileData properties;
	public double x;
	public double y;
	public TileEntitiy partner;
	public MapTile (TileData tileId, double x, double y) {
		//tileId is in the format [tile name]
		this.properties = tileId;
		this.x = x;
		this.y = y;
	}
	public MapTile (TileData tileId, double x, double y, TileEntitiy partner) {
		//tileId is in the format [tile name]
		this.properties = tileId;
		this.x = x;
		this.y = y;
		this.partner = partner;
	}
}