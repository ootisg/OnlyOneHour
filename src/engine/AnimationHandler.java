package engine;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import org.joml.Matrix4f;

import map.Room;

public class AnimationHandler {
	
	/**
	 * The image for this AnimationHandler to draw
	 */
	private Sprite image;
	
	/**
	 * The frame which corresponds to the starting time
	 */
	private int startFrame;
	
	/**
	 * The time the animation started
	 */
	private long startTime;
	
	/**
	 * The time of each frame, in milliseconds
	 */
	private double frameTime;
	
	/**
	 * Whether or not this animation repeats
	 */
	private boolean repeat;
	/**
	 * make true to make the sprite alternate from going forward to back to backward to forward
	 */
	private boolean alternate = false;
	/**
	 * Whether or not to apply horizontal flip
	 */
	private boolean visible = true;
	private boolean flipHorizontal;
	
	/**
	 * Whether or not to apply vertical flip
	 */
	private boolean flipVertical;
	
	private int playToo = 420;
	private double previosFrameTime = 0;
	
	/**
	 * deal with the reversal
	 */
	private boolean reverse = false;
	private boolean hasReversed = false;
	private double currentRotation = 0;
	/**
	 * the height to draw too
	 */
	private int height;
	
	/**
	 * the width to draw too
	 */
	
	private int width;
	/**
	 * tells the handler wherether or not to keep the sprite to scale
	 */
	private boolean keepScale = false;
	/**
	 * what the fuck ever (nice comment?)
	 */
	private boolean rotationsEnabled = false;
	/**
	 * Constructs a new AnimationHandler with the given image, defaulting to a static image.
	 * @param image The image to use
	 */
	public AnimationHandler (Sprite image) {
		this (image, 0);
	}
	
	/**
	 * Constructs a new AnimationHandler with the given image, with each frame lasting for frameTime milliseconds.
	 * @param image The image to use
	 * @param frameTime The numer of milliseconds to show each frame for; displays a static image if set to 0
	 */
	public AnimationHandler (Sprite image, double frameTime) {
		this.image = image;
		startTime = GameLoop.frameStartTime ();
		startFrame = 0;
		this.frameTime = frameTime;
		repeat = true;
		playToo = -1;
		try {
			width = image.getFrame(0).getWidth();
			height = image.getFrame(0).getHeight();
		} catch (NullPointerException e) {
			
		}
	}
	
	/**
	 * Draws the sprite's current animation frame at the given x and y coordinates.
	 * @param transform the transform to draw with
	 */
	public void draw (Transform a, Transform b) {
		if (visible) {
		if (image != null) {
			if (frameTime == 0) {
				startTime = GameLoop.frameStartTime ();
				image.draw (a, b, startFrame);
			} else {
				if (startTime == 0) {
					startTime = GameLoop.frameStartTime();
				}
				long elapsedTime = GameLoop.frameStartTime () - startTime;
				int elapsedFrames = ((int)(((double)elapsedTime) / ((double)frameTime)) + startFrame);
				if (!repeat && elapsedFrames >= image.getFrameCount ()) {
					image.draw (a, b, image.getFrameCount () - 1);
				} else {
					int frame = elapsedFrames % image.getFrameCount ();
//					if (alternate) {
//						System.out.println(frame);
//						System.out.println(reverse);
//					}
					if (frame == 0 && alternate && image.getFrameCount() != 1 && elapsedFrames > 1) {
						
						if (!hasReversed) {
						
							reverse = !reverse;
							
							hasReversed = true;
						}
					} else {
						hasReversed = false;
					}
					if (reverse) {
						frame = (image.getFrameCount() - 1) - frame;
					}
					if (this.getHeight() == 32) {
						
					}
					if (frame == playToo) {
						this.setFrameTime(0);
						startFrame = frame - 1;
					}
					
					image.draw (a, b, frame);
				}
			}
		}
		}
	}
	
	public void draw (Transform t) {
		draw (t, t);
	}


	/**
	 * Sets the image used by this AnimationHandler to the given sprite.
	 * @param image The image to use
	 */
	public void setImage (Sprite image) {
		this.image = image;
		reverse = false;
		playToo = -1;
		if ((width == 0 && height == 0) || !keepScale) {
		width = image.getFrame(0).getWidth();
		height = image.getFrame(0).getHeight();
		} else {
			if (width != image.getFrame(0).getWidth() || height != image.getFrame(0).getHeight()) {
				this.scale(width, height);
			}
		}
	}
	public double getRotation () {
		return currentRotation;
	}
	//lots of code copy pasted from stack overflow here (obsouleted use the method in sprite instead)
//	public BufferedImage rotate (double rotation, BufferedImage startImg) {
//		double workingRotation = rotation - currentRotation;
//		currentRotation = currentRotation + workingRotation;
//		double rads = Math.toRadians(workingRotation);
//		double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
//		int w = startImg.getWidth();
//		int h = startImg.getHeight();
//		int newWidth = (int) Math.floor(w * cos + h * sin);
//        int newHeight = (int) Math.floor(h * cos + w * sin);
//		BufferedImage newImg = new BufferedImage (newWidth,newHeight,startImg.getType());
//		Graphics2D graphic = newImg.createGraphics();
//		AffineTransform at = new AffineTransform();
//		at.translate((newWidth - w) / 2, (newHeight - h) / 2);
//        int x = w / 2;
//        int y = h / 2;
//        at.rotate(rads, x, y);
//        graphic.setTransform(at);
//	    graphic.drawImage(startImg, 0, 0, null);
//		graphic.dispose();
//		return newImg;
//	}
	/**
	 * Sets the image used by this AnimationHandler to the given sprite, and restarts the animation from the beginning.
	 * @param image The image to use
	 */
	public void resetImage (Sprite image) {
		setImage (image);
		startTime = GameLoop.frameStartTime ();
		startFrame = 0;
	}
	public void playToo (int where) {
		if (where < image.getFrameCount() ) {
			playToo = where;
		} else {
			playToo = image.getFrameCount() -1;
		}
		this.setFrameTime(previosFrameTime);
	}
	public void playFrom (int where, int too) {
		this.setAnimationFrame(where);
		if (too < image.getFrameCount() ) {
			playToo = too;
		} else {
			playToo = image.getFrameCount() -1;
		}
		this.setFrameTime(previosFrameTime);
		this.startFrame = where;
		this.startTime = GameLoop.frameStartTime();
	}
	public void playInfintely () {
		playToo = -1;
		startFrame = 0;
		this.setFrameTime(previosFrameTime);
	}
	/**
	 * makes the animation handler keep the sprite to scale
	 * ie will NOT allow changes to width and height
	 */
	public void keepScale () {
		keepScale = true;
	}
	/**
	 * makes the animation handler not keep the sprite to scale
	 * ie will allow changes to width and height
	 */
	public void dontKeepScale () {
		keepScale = false;
	}
	/**
	 * Sets the current frame of the animation to the given frame.
	 * @param frame The frame to use
	 */
	public void setAnimationFrame (int frame) {
		startFrame = frame;
		startTime = GameLoop.frameStartTime ();
	}
	
	/**
	 * Sets the time between frames to the given time.
	 * @param frameTime The time to use, in milliseconds
	 */
	public void setFrameTime (double frameTime) {
		if (frameTime != 0) {
			previosFrameTime = frameTime;
		} 
		this.frameTime = frameTime;
	}
	
	/**
	 * Sets whether this animation repeats or not after it is complete.
	 * @param repeats Whether the animation repeats
	 */
	public void setRepeat (boolean repeats) {
		repeat = repeats;
	}
	/**
	 * makes the sprite alternate
	 */
	public void enableAlternate () {
		alternate = true;
	}
	/**
	 * makes the sprite not alternate
	 */
	public void disableAlternate () {
		alternate = false;
		reverse = false;
		this.setAnimationFrame(0);
	}
	/**
	 * changes how far out it draws the image
	 */
	public void setHeight(int newHeight) {
		height = newHeight;
	}
	/**
	 * changes how far to the right it draws the image
	 */
	public void setWidth(int newWidht) {
		width = newWidht;
	}
	/**
	 * Gets the image used by this AnimationHandler.
	 * @return The image field for this AnimationHandler
	 */
	public Sprite getImage () {
		return image;
	}
	/**
	 * scales the image to a specific size
	 *@param width the width you want
	 *@param height the height you want
	 */
	public void scale (int width, int height) {
		for (int i = 0; i < image.getFrameCount(); i++) {
			Image img =image.getFrame(i).getScaledInstance(width, height, Image.SCALE_FAST);
			BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics2D bGr = bimage.createGraphics();
		    bGr.drawImage(img, 0, 0, null);
		    bGr.dispose();
			image.setFrame(i,bimage);
		}
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Gets the frame which would be drawn at the time this method is called. Not guarenteed to be the frame that will be drawn next.
	 * @return The current frame for this AnimationHandler
	 */
	public int getFrame () {
		if (image != null) {
			long elapsedTime = GameLoop.frameStartTime () - startTime;
			int frame = ((int)(((double)elapsedTime) / ((double)frameTime)) + startFrame) % image.getFrameCount ();
			if (reverse) {
				if ((image.getFrameCount() -1) - frame == 0 && !alternate){

					reverse = false;
				}
			return (image.getFrameCount() -1) - frame;
			} else {
			return frame;
			}
		} else {
			return -1;
		}
	}
	public int getWidth () {
		return width;
	}
	public int getHeight () {
		return height;
	}
	public boolean isAnimationDone() { return getFrame() == image.getFrameCount() - 1; }
	/**
	 * Gets the time each frame should be displayed.
	 * @return The length of time, in milliseconds, to display each frame
	 */
	public double getFrameTime () {
		return frameTime;
	}
	
	/**
	 * Returns true if this animation is set to repeat; false otherwise.
	 * @return Whether this animation repeats or not
	 */
	public boolean repeats () {
		return repeat;
	}
	
	/**
	 * Returns whether or not horizontal flipping is applied.
	 * @return the horizontal flip
	 */
	public boolean flipHorizontal () {
		return flipHorizontal;
	}
	
	/**
	 * Returns whether or not vertical flipping is applied.
	 * @return the vertical flip
	 */
	public boolean flipVertical () {
		return flipVertical;
	}
	
	/**
	 * Sets whether or not to flip the image horizontally to the given value.
	 * @param flip whether or not to apply horizontal flip
	 */
	public void setFlipHorizontal (boolean flip) {
		flipHorizontal = flip;
	}
	
	/**
	 * Sets whether or not to flip the image vertically to the given value.
	 * @param flip whether or not to apply vertical flip
	 */
	public void setFlipVertical (boolean flip) {
		flipVertical = flip;
	}
	public void hide () {
		this.visible = false;
	}
	public void show () {
		this.visible = true;
	}
}
