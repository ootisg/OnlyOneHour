package map;


import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import engine.AnimationHandler;
import engine.GameCode;
import engine.GameLoop;
import engine.Sprite;
import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import json.JSONUtil;

public class Background {
	private AnimationHandler animationHandler;
	private double scrollRateHorizontal;
	private double scrollRateVertical;
	
	private long creationNs;
	private long frameNs = 300000000;
	
	int oldFrame = 0;
	long oldFrameNum = 0;
	boolean diffrentFrame = false;
	
	private ArrayList<BufferedImage> frames;
	
	public Background (String filepath) {
		setImg (filepath);
		resetAnimation ();
		this.scrollRateHorizontal = 1.0;
		this.scrollRateVertical = 1.0;
	}
	
	public void setImg (String filepath) {
		
		//Get the JSON path
		String noExtPath = filepath.split ("\\.")[0];
		String[] splitPath = noExtPath.split("/|\\\\");
		splitPath [splitPath.length - 1] = "config/" + splitPath [splitPath.length - 1] + ".json";
		String jsonPath = String.join("/", splitPath);
		
		try {
			
			//Get the sprite and JSON data
			BufferedImage srcImg = Sprite.getImage(filepath);
			JSONObject cfg = JSONUtil.loadJSONFile (jsonPath);
			
			//Set the default width and height accordingly
			int default_width = -1;
			int default_height = -1;
			if (cfg.get ("default_width") != null && cfg.get("default_height") != null) {
				default_width = cfg.getInt("default_width");
				default_height = cfg.getInt("default_height");
			}
			
			//Add the frames
			this.frames = new ArrayList<BufferedImage> ();
			if (cfg.get("frames") != null) {
				JSONArray frames = cfg.getJSONArray("frames");
				for (int i = 0; i < frames.getContents ().size (); i++) {
					JSONObject curr = (JSONObject)frames.getContents ().get(i);
					if (curr.get("endX") != null && curr.get("endY") != null) {
						int sx = curr.getInt("x");
						int sy = curr.getInt("y");
						int width = curr.getInt("endX") - sx;
						int height = curr.getInt("endY") - sy;
						this.frames.add(srcImg.getSubimage(sx, sy, width, height));
					} else {
						int sx = curr.getInt("x");
						int sy = curr.getInt("y");
						int width = curr.get("width") == null ? default_width : curr.getInt("width");
						int height = curr.get("height") == null ? default_height : curr.getInt("height");
						this.frames.add(srcImg.getSubimage(sx, sy, width, height));
					}
				}
			} else {
				throw new JSONException ("Haha lol theres no frames here");
			}
		} catch (JSONException e) {
			//The file probably does not exist TODO lolz
			this.frames = new ArrayList<BufferedImage> ();
			frames.add(Sprite.getImage(filepath));
		}
	}
	
	public void draw (double viewX, double viewY) {
		int width = GameLoop.wind.getResolution () [0];
		int height = GameLoop.wind.getResolution () [1];
		int imgWidth = animationHandler.getImage ().getFrame (animationHandler.getFrame ()).getWidth ();
		int imgHeight = animationHandler.getImage ().getFrame (animationHandler.getFrame ()).getHeight ();
		for (int i = -((int)viewX % imgWidth); i < width; i += imgWidth) {
			for (int j = -((int)viewY % imgHeight); j < height; j += imgHeight) {
				animationHandler.draw (i, j);
			}
		}
		animationHandler.draw ((int)(-viewX / scrollRateHorizontal), (int)(-viewY / scrollRateVertical));
	}
	public void setImage (Sprite image) {
		animationHandler.setImage (image);
	}
	public void setScrollRateHorizontal (double scrollRate) {
		this.scrollRateHorizontal = scrollRate;
	}
	public void setScrollRateVertiacal (double scrollRate) {
		this.scrollRateVertical = scrollRate;
	}
	public int getCurrentFrame () {
		return ((int)((System.nanoTime () - creationNs) / frameNs)) % frames.size ();
	}
//	public boolean isNewFrame () {
//		if (GameCode.getFrameNum() != oldFrameNum) {
//			
//			if (getCurrentFrame() != oldFrame) {
//				oldFrame = getCurrentFrame();
//				diffrentFrame = true;
//				oldFrameNum = GameCode.getFrameNum();
//				return true;
//			}
//			diffrentFrame = false;
//			oldFrameNum = GameCode.getFrameNum();
//			return false;
//		} else {
//			return diffrentFrame;
//		}
//		
//	}
	public void resetAnimation () {
		creationNs = System.nanoTime ();
	}
	public BufferedImage getImage (int frame) {
		return frames.get(frame);
	}
	public ArrayList<BufferedImage> getFrames () {
		return frames;
	}
	public double getScrollRateHorizontal () {
		return scrollRateHorizontal;
	}
	public double getScrollRateVertical () {
		return scrollRateVertical;
	}
	public int getWidth () {
		return getImage (getCurrentFrame ()).getWidth ();
	}
	public int getHeight () {
		return getImage (getCurrentFrame ()).getHeight ();
	}
}