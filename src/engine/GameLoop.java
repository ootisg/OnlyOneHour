package engine;

/**
 * The loop for rendering the game to the GameWindow. Runs on the event dispatching thread.
 * @author nathan
 *
 */
public class GameLoop implements Runnable {
	
	/**
	 * The maximum (target) simulation rate the game can run at
	 */
	public static final double maxStepRate = 30;
	/**
	 * The maximum (target) framerate the game can run at
	 */
	public static final double maxFramerate = 120;
	/**
	 * The time of the last update to the GameWindow, in nanoseconds.
	 */
	static private long lastUpdate;
	
	/**
	 * The system time when this frame's rendering began
	 */
	static private long frameTime;
	static private long frameTimeNs;
	
	/**
	 * The image of the input from the past GameLogic frame
	 */
	static private InputManager inputImage;
	
	public static GameWindow wind;

	public static void main (String[] args) {
		wind = new GameWindow (960, 540);
		GameLoop loop = new GameLoop ();
		Thread gameThread = new Thread (loop);
		gameThread.start ();
		while (true) {
			long startTime = System.currentTimeMillis ();
			int frames = 0;
			while (System.currentTimeMillis () - startTime < 1000) {
				long frameStart = System.nanoTime ();
				wind.refresh ();
				frames++;
				//For some reason, trying to cap the framerate results in terrible performance and weird texture issues
				/*try {
					Thread.sleep (1);
				} catch (InterruptedException e) {
					//do nothing, it's not that important
				}*/
			}
			System.out.println ("FPS: " + frames);
		}
	}
	
	@Override
	public void run () {
		GameCode.init ();
		//Sets the initial frame time
		frameTime = System.currentTimeMillis ();
		frameTimeNs = System.nanoTime ();
		//Initializes lastUpdate to the current time
		lastUpdate = System.nanoTime ();
		while (true) {
			//Get the target time in nanoseconds for this iteration; should be constant if the framerate doesn't change
			long targetNanoseconds = (long)(1000000000 / maxStepRate);
			//Get the time before refreshing the window
			long startTime = System.nanoTime ();
			frameTime = System.currentTimeMillis ();
			frameTimeNs = System.nanoTime ();
			//Render the window
			inputImage = GameLoop.wind.getInputImage ();
			GameCode.beforeGameLogic ();
			GameCode.gameLoopFunc();
			ObjectHandler.callAll ();
			GameLoop.wind.resetInputBuffers ();
			GameCode.afterGameLogic ();
			GameCode.beforeRender ();
			GameCode.renderFunc();
			ObjectHandler.renderAll ();
			GameCode.afterRender ();
			wind.render ();
			//Calculate elapsed time and time to sleep for
			lastUpdate = System.nanoTime ();
			long elapsedTime = lastUpdate - startTime;
			int sleepTime = (int)((targetNanoseconds - elapsedTime) / 1000000) - 1;
			
			if (sleepTime < 0) {
				sleepTime = 0;
			}
			//Sleep until ~1ms before it's time to redraw the frame (to account for inaccuracies in Thread.sleep)
			try {
				Thread.currentThread ().sleep (sleepTime);
			} catch (InterruptedException e) {
				//Do nothing; the while loop immediately after handles this case well
			}
			//Wait until the frame should be redrawn
			while (System.nanoTime () - startTime < targetNanoseconds) {
			
			}
		}
	}
	
	/**
	 * Gets the value returned by System.currentTimeMillis at the start of the frame being rendered.
	 * @return The start time of the current frame
	 */
	public static long frameStartTime () {
		return frameTime;
	}
	
	/**
	 * Gets the value returned by System.nanoTime() at the start of the frame being rendered.
	 * @return The start time of the current frame
	 */
	public static long frameStartTimeNs () {
		return frameTimeNs;
	}
	
	/**
	 * Gets the length of one (game logic) frame in nanoseconds
	 * @return The length of one frame
	 */
	public static long stepLength () {
		return (long)(1000000000L / maxStepRate); //10^9 ns in one s
	}
	
	/**
	 * Gets the length of one (rendered) frame in nanoseconds
	 * @return The length of one frame
	 */
	public static long frameLength () {
		return (long)(1000000000L / maxFramerate); //10^9 ns in one s
	}
	
	public static double deltaTime () {
		return (double)(System.nanoTime () - frameTimeNs) / stepLength ();
	}
	
	/**
	 * Gets the input image from the start of this game logic iteration.
	 * @return The input image from the start of this iteration
	 */
	public static InputManager getInputImage () {
		return inputImage;
	}
	
	/**
	 * Event callback for closing the game
	 */
	public static void end () {
		System.exit (0);
	}
	
}
