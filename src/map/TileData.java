package map;


import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Scanner;

import engine.GameObject;




public class TileData {
	//Container class for tile metadata
	private String name;
	private String fname;
	private boolean isSolid;
	private boolean isSpecial;
	private GameObject observer = null;
	private TileEntitiy partner;
	
	private HashMap <String,String> attributes;// info about specific tile 
	private static HashMap <String, String> defaultMap = null; //defualt tile infos

	public static final String PATH = "resources/tileInfo/";
	public static final String DEFAULT_NAME = "default.txt"; //path to the defult info string
	
	public TileData (String name) {
		//Name is in the format [tileset name].[position in tileset]
		this.name = name;
		this.fname = name.split ("\\.")[0];
		
		this.isSolid = true;
		//sets info for the defualt map
		if (defaultMap == null) {
			defaultMap = getFile(DEFAULT_NAME);
		}
		
		attributes = getFile (name + ".txt"); // sets info specific to this tile
		isSolid = Boolean.valueOf(getAttribute("solid"));
		if (getAttribute("tileEntity") != null) {
			isSpecial= true;
		} else {
			isSpecial = false;
		}
	}
	public void setSolid (boolean newSolidity) {
		isSolid = newSolidity;
	}
/**
 * returns the tileEntitiy associated witht this type of tile (null if no tile Entitiy is associated)
 * @return the thing up there
 */
	public TileEntitiy makeEntity () {
		if (isSpecial) {
			Class<?> c;
			try {
				c =Class.forName("tileEntities." +this.getAttribute("tileEntity"));
				TileEntitiy newEntitiy = (TileEntitiy) c.getConstructor().newInstance();
				return newEntitiy;
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		} else {
			return null;
		}
	}
	/**
	 * gets the attribute assigned to this specific tile
	 * @param name the name of the attribute
	 * @return the attribute
	 */
	public String getAttribute (String name) {
		if (attributes.get(name) != null) {
			return attributes.get(name);
		} else {
			return defaultMap.get(name);
		}
	}
	
	/**
	 * gets the info about the tile from the file (in tileInfo)
	 * @param path path to the file
	 * @return hasmap with the parsed info
	 */
	private static HashMap<String,String> getFile (String name) {
		if (name.equals("_NULL.txt")) {
			return getFile ("null.txt");
		}
		String [] ded = name.split(".");
		File file = new File (PATH + name);
		if (!file.exists()){
			try {
			file = new File (PATH + ded[0] +"."+ ded[2]);
			}catch (ArrayIndexOutOfBoundsException e) {
				return defaultMap;
			}
		} 
		HashMap <String,String> working= new HashMap <String,String>();
		try {
			Scanner s = new Scanner (file);
			while(s.hasNextLine()) {
				String line = s.nextLine();
				String [] args = line.split(":");
				working.put(args[0], args[1]);
			}
			return working;
		} catch (FileNotFoundException e) {
			return defaultMap;
		}
		
	}
	public String getName () {
		return name;
	}
	public String getFName () {
		return fname;
	}
	public boolean isSpecial () {
		return isSpecial;
	}
	public boolean isSolid () {
		return isSolid;
	}
}