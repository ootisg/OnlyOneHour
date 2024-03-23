package engine;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import gl.GLTexture;

import static org.lwjgl.opengl.GL42.*;

import java.awt.Color;



/**
 * Represents a drawable image
 * @author nathan
 *
 */
public class Sprite {
	/**
	 * The global image cache
	 */
	private static HashMap<String, CacheNode> cache = new HashMap<String, CacheNode> ();
	/**
	 * The image data this sprite contains
	 */
	private BufferedImage[] images;
	/**
	 * Something like that
	 */
	private GLTexture[] glTextures;
	/**
	 * The filepath to this sprite, if applicable
	 */
	private String imagePath;
	/**
	 * The filepath to the parsing parameters, if applicable
	 */
	private String parsePath;
	/**
	 * Whether the sprite is animated or not
	 */
	private boolean isAnimated;
	/**
	 * does this get scalled by the changes of resolution
	 */
	protected boolean doesScale = true;
	/**
	 * GL texture queue
	 */
	private static ArrayList<GLTexture> loadQueue = new ArrayList<GLTexture> (); 
	private static ReentrantLock textureLoadLock = new ReentrantLock ();
	
	private float opacity = 1;
	
	/**
	 * 
	 * Constructs a sprite with the given image filepath and parsing parameter filepath.
	 * @param imagepath The filepath to the image to use
	 * @param parsepath The filepath to the parsing parameters to use
	 */
	public Sprite (String imagepath, String parsepath) {
		this.imagePath = imagepath;
		this.parsePath = parsepath;
		String key = imagepath + ":" + parsepath;
		CacheNode data = cache.get (key);
		if (data == null) {
			SpriteParser parser = new SpriteParser (parsepath);
			File imageFile = new File (imagepath);
			BufferedImage img = null;
			try {
				img = ImageIO.read (imageFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			images = parser.parse (img);
			genGLTextures ();
			cache.put (key, new CacheNode (key, images, glTextures));
		} else {
			images = data.getData ();
			glTextures = data.getTextures ();
		}
		if (images.length > 1) {
			isAnimated = true;
		} else {
			isAnimated = false;
		}
	}
	
	/**
	 * Constructs a sprite with the given image and parsing parameter filepath. Does not support caching.
	 * @param image The image to use
	 * @param parsepath The filepath to the parsing parameters to use
	 */
	public Sprite (BufferedImage image, String parsepath) {
		this.parsePath = parsepath;
		SpriteParser parser = new SpriteParser (parsepath);
		images = parser.parse (image);
		if (images.length > 1) {
			isAnimated = true;
		} else {
			isAnimated = false;
		}
		genGLTextures ();
	}
	
	/**
	 * Constructs a sprite with the given image and parser. Does not support caching.
	 * @param image The image to use
	 * @param parser The parser to use
	 */
	public Sprite (BufferedImage image, SpriteParser parser) {
		images = parser.parse (image);
		if (images.length > 1) {
			isAnimated = true;
		} else {
			isAnimated = false;
		}
		genGLTextures ();
	}
	/**
	 * Constructs a sprite with the given image path and parser. Does not support caching.
	 * @param imagepath The image to use
	 * @param parser The parser to use
	 */
	public Sprite (String imagepath, SpriteParser parser) {
		this.imagePath = imagepath;
		File imageFile = new File (imagepath);
		BufferedImage img = null;
		try {
			img = ImageIO.read (imageFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		images = parser.parse (img);
		if (images.length > 1) {
			isAnimated = true;
		} else {
			isAnimated = false;
		}
		genGLTextures ();
	}
	
	/**
	 * Constructs a sprite with the given image filepath--or uses only the filepath of the parser if given a .txt filepath.
	 * @param imagepath The filepath to use for the image
	 */
	public Sprite (String imagepath) {
		String[] splitPath = imagepath.split ("\\.");
		if (splitPath.length != 0 && splitPath [splitPath.length - 1].equals ("txt")) {
		
			parsePath = imagepath;
			images = new SpriteParser (imagepath).parse ();
			genGLTextures ();
			if (images.length > 1) {
				isAnimated = true;
			} else {
				isAnimated = false;
			}
		} else {
			this.imagePath = imagepath;
			CacheNode data = cache.get (imagepath);
			if (data == null) {
				File imageFile = new File (imagepath);
				BufferedImage img = null;
				try {
					img = ImageIO.read (imageFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				images = new BufferedImage[] {img};
				genGLTextures ();
				cache.put (imagepath, new CacheNode (imagepath, images, glTextures));
			} else {
				images = data.getData ();
				glTextures = data.getTextures ();
			}
			isAnimated = false;
		}
	}
	
	/**
	 * Copy constructor for ease of inheretence
	 * @param sprite The Sprite object to copy
	 */
	public Sprite (Sprite sprite) {
		
		this.images = new BufferedImage[sprite.images.length];
		
		for (int i = 0; i < sprite.images.length; i++) {
			BufferedImage bimage = new BufferedImage(sprite.images[i].getWidth(null), sprite.images[i].getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics2D bGr = bimage.createGraphics();
		    bGr.drawImage(sprite.images[i], 0, 0, null);
		    bGr.dispose();
		    
		    this.images[i] = bimage;
		}
		
		this.isAnimated = sprite.isAnimated;
		this.imagePath = sprite.imagePath;
		this.parsePath = sprite.parsePath;
		genGLTextures ();
	}
	
	public void genGLTextures () {
		textureLoadLock.lock ();
		glTextures = new GLTexture[images.length];
		for (int i = 0; i < images.length; i++) {
			glTextures [i] = new GLTexture (GL_TEXTURE_2D, images[i]);
			loadQueue.add (glTextures[i]);
		}
		textureLoadLock.unlock ();
	}
	
	public static void loadTextures () {
		textureLoadLock.lock ();
		for (int i = 0; i < loadQueue.size (); i++) {
			GLTexture curr = loadQueue.get (i);
			curr.load ();
		}
		loadQueue = new ArrayList<GLTexture> ();
		textureLoadLock.unlock ();
	}
	
	/**
	 * Gets the frame count of this sprite.
	 * @return The number of frames in this sprite
	 */
	public int getFrameCount () {
		return images.length;
	}
	/**
	 * returns the width of the sprite
	 * @return the width of the sprite
	 */
	public int getWidth() {
		return images[0].getWidth();
	}
	/**
	 * returns the height of the sprite
	 * @return the height of the sprite
	 */
	public int getHeight() {
		return images[0].getHeight();
	}
	/**
	 * Constructs a sprite with the given image. Does not support caching.
	 * @param image The image to use
	 */
	public Sprite (BufferedImage image) {
		images = new BufferedImage[] {image};
		genGLTextures ();
	}
	/**
	 * Constructs a sprite with the given images. Does not support caching.
	 * @param image The image to use
	 * why did it take so long to make this?
	 */
	public Sprite (BufferedImage [] frames) {
		images = frames;
		genGLTextures ();
	}
	
	/**
	 * Draws the first frame of this sprite at the given x and y coordinates.
	 * @param usedX The x coordinate to draw this sprite at
	 * @param usedY The y coordinate to draw this sprite at
	 */
	public void draw (int usedX, int usedY) {
		draw (usedX, usedY, 0);
	}
	
	public void draw (Transform t, int frame, GameObject obj) {
		GameLoop.wind.drawSprite (t, glTextures[frame], obj);
	}
	
	public void draw (Transform t, int frame) {
		GameLoop.wind.drawSprite (t, glTextures[frame]);
	}
	
	public void draw (Transform a, Transform b, int frame, GameObject obj) {
		GameLoop.wind.drawSprite (a, b, glTextures[frame], obj);
	}
	
	public void draw (Transform a, Transform b, int frame) {
		GameLoop.wind.drawSprite (a, b, glTextures[frame]);
	}
	
	public void drawScaled (int x, int y, double scaleX, double scaleY, int frame) {
		Matrix4f transform = new Matrix4f (
				(float)scaleX, 0, 0, 0,
				0, (float)scaleY, 0, 0,
				0, 0, 1, 0,
				x, y, 0, 1
				);
		draw (transform, frame);
	}
	
	/**
	 * Draws the given frame of this sprite at the given x and y coordinates.
	 * @param usedX The x coordinate to draw this sprite at
	 * @param usedY The y coordinate to draw this sprite at
	 * @param frame The frame of this sprite to draw
	 */
	public void draw (int usedX, int usedY, int frame) {
		Matrix4f mat = new Matrix4f (
				1, 0, 0, (float)usedX,
				0, 1, 0, (float)usedY,
				0, 0, 1, 0,
				0, 0, 0, 1
				);
		GameLoop.wind.drawSprite (mat, glTextures[frame]);
	}
	
	/**
	 * Gets the BufferedImage representing the given frame of the sprite.
	 * @param frame The frame to get
	 * @return the given frame
	 */
	public BufferedImage getFrame (int frame) {
		return images [frame];
	}
	/**
	 * sets any frame of the animation to anything you want
	 * @param frame the frame to change
	 * @param newImage the new image
	 */
	public void setFrame (int frame, BufferedImage newImage) {
		images[frame] = newImage;
	}
	/**
	 * Gets the filepath of the image used to create this sprite.
	 * @return the filepath of this sprite's image; returns null if not applicable
	 */
	public String getImagePath () {
		return imagePath;
	}
	
	/**
	 * Gets the filepath of the file used to parse this sprite.
	 * @return the filepath used to parse this sprite; returns null if not applicable
	 */
	public String getParsePath () {
		return parsePath;
	}
	
	/**
	 * Gets the BufferedImage associated with the given filepath.
	 * @param path the filepath to use
	 * @return the resulting image; null if no image is found
	 */
	public static BufferedImage getImage (String path) {
		CacheNode data = null; //Don't cache standalone BufferedImages
		if (data == null) {
			File imageFile = new File (path);
			BufferedImage img = null;
			try {
				img = ImageIO.read (imageFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedImage[] currentImg = new BufferedImage[] {img};
			//cache.put (path, new CacheNode (path, currentImg));
			return currentImg [0];
		} else {
			return data.getData () [0];
		}
	}
	
	private static class CacheNode {
		
		/**
		 * The number of times the data in this node has been accessed
		 */
		private int accessCount;
		/**
		 * The images stored in this node
		 */
		private BufferedImage[] data;
		/**
		 * The textures stored in this node
		 */
		private GLTexture[] textures;
		/**
		 * The key associated with this node
		 */
		private String key;
		
		/**
		 * Construct a new CacheNode indexed by the given key, holding the given data.
		 * @param key The key to index this node
		 * @param data The data to store in this node
		 */
		public CacheNode (String key, BufferedImage[] data, GLTexture[] textures) {
			accessCount = 0;
			this.key = key;
			this.data = data;
			this.textures = textures;
			cache.put (key, this);
		}
		
		/**
		 * Gets the key value associated with this node.
		 * @return This node's key
		 */
		public String getKey () {
			return key;
		}
		
		/**
		 * Gets the data stored by this node.
		 * @return This node's data
		 */
		public BufferedImage[] getData () {
			accessCount ++;
			return data;
		}
		
		/**
		 * Gets the textures stored by this node.
		 * @return This node's data
		 */
		public GLTexture[] getTextures () {
			accessCount ++;
			return textures;
		}
		
		/**
		 * Gets the number of times getData has been called on this node.
		 * @return The number of calls to getData
		 */
		public int getAccessCount () {
			return accessCount;
		}
		
		/**
		 * Removes this element from the cache.
		 * @return True if the element was removed; false oftherwise
		 */
		public boolean remove () {
			return cache.remove (key, this);
		}
	}
}
