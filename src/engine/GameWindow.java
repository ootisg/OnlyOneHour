package engine;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import engine.DrawCall.DrawData;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import gl.*;

/**
 * The window which the game is displayed in. Also handles keyboard and mouse events.
 * @author nathan
 *
 */
public class GameWindow {

	public String[] splashes = new String[] {
		"Left Larry for president!"
	};
	
	int width, height;
	int[] resolution = new int[2];
	
	/**
	 * The image used as a drawing buffer
	 */
	private BufferedImage buffer;
	/**
	 * another image that is not scalled by resolution
	 */
	private BufferedImage nonScallableBuffer;
	
	
	private ArrayList <BufferedImage> inGameBufferes = new ArrayList <BufferedImage>();
	/**
	 * The InputManager used to detect input for this window
	 */
	private InputManager inputManager;
	
	// The window handle
	private long window;
	
	//Draw calls
	private ArrayList<DrawCall> lastCalls = new ArrayList<DrawCall> ();
	private ArrayList<DrawCall> callBuffer = new ArrayList<DrawCall> ();
	
	//GL state things
	private GLProgram program;
	private ObjectVBOS vbos;
	
	public double fade = 0.0;
	
	//GPU rendering thread
	private ReentrantLock drawLock;
	
	/**
	 * Constructs a new GameWindow with the given width and height.
	 * @param width The initial width, in pixels, of the window content
	 * @param height The initial height, in pixels, of the window content
	 */
	public GameWindow (int width, int height) {
		
		this.width = width;
		this.height = height;
		this.resolution = new int[] {width, height};
		
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		window = glfwCreateWindow(width, height, splashes[(int)(Math.random () * splashes.length)], NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException ("Failed to create the GLFW window");
		
		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		inputManager = new InputManager ();
		glfwSetKeyCallback(window, inputManager);
		glfwSetWindowSizeCallback (window, new WinResizeCallback ());

		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
		
		GL.createCapabilities();
		
		//OpenGL init
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable (GL_BLEND);
		//glEnable (GL_DEPTH_TEST);
		
		setProgram (GLProgram.programFromDirectory ("resources/shaders/default/"));
		vbos = new ObjectVBOS ();
		
		drawLock = new ReentrantLock ();
		
	}

	public void setProgram (GLProgram program) {
		this.program = program;
	}
	
	public void closeWindow () {
		
		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
		
	}
	
	/**
	 * Renders the contents of the buffers onto the window.
	 */
	public void refresh () {
		
		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		if (glfwWindowShouldClose(window)) {
			this.closeWindow ();
			GameLoop.end ();
		} else {
			//Load all textures that need loaded
			Sprite.loadTextures ();
			
			// Set the clear color
			glClearColor(0.75f, 0.75f, 0.75f, 1.0f);
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
				
				//Draw to the screen
				program.use ();
				vbos.bindSpriteVBO ();
				glVertexAttribPointer (
						0,
						3,
						GL_DOUBLE,
						false,
						8 * 5,
						0);
				glEnableVertexAttribArray (0);
				glVertexAttribPointer (
						1,
						2,
						GL_DOUBLE,
						false,
						8 * 5,
						8 * 3);
				glEnableVertexAttribArray (1);
				
				//Get the uniforms
				int posALoc = glGetUniformLocation (program.getProgramName (), "pos_a");
				int sclALoc = glGetUniformLocation (program.getProgramName (), "scl_a");
				int rotALoc = glGetUniformLocation (program.getProgramName (), "rot_a");
				int posBLoc = glGetUniformLocation (program.getProgramName (), "pos_b");
				int sclBLoc = glGetUniformLocation (program.getProgramName (), "scl_b");
				int rotBLoc = glGetUniformLocation (program.getProgramName (), "rot_b");
				int dtLoc = glGetUniformLocation (program.getProgramName (), "delta_t");
				int[] uniformsA = new int[] {posALoc, sclALoc, rotALoc};
				int[] uniformsB = new int[] {posBLoc, sclBLoc, rotBLoc};
				
				int vpLoc = glGetUniformLocation (program.getProgramName (), "vp");
				
				int samplerLoc = glGetUniformLocation (program.getProgramName (), "texture");
				
				int fadeTimerGlobalLoc = glGetUniformLocation (program.getProgramName (), "fade_timer_global");
				int fadeTimerLocalLoc = glGetUniformLocation (program.getProgramName (), "fade_timer_local");
				
				//Configure textures
				GLTexture currentTexture = null;
				glUniform1i(samplerLoc, 0);
				
				//VP matrix
				float[] vpBuffer = new float[16];
				Matrix4f vp = new Matrix4f ().ortho (0f, (float)resolution[0], (float)resolution[1], 0, -5000, 5000);

				//Clone the draw calls
				ArrayList<DrawCall> currDrawCalls = new ArrayList<DrawCall> ();
				drawLock.lock ();
				for (int i = 0; i < lastCalls.size (); i++) {
					currDrawCalls.add (lastCalls.get (i));
				}
				drawLock.unlock ();
				
				//Render the draw calls
				for (int i = 0; i < currDrawCalls.size (); i++) {
					
					DrawCall working = currDrawCalls.get (i);
					working.drawData (1).setTextures (new int[] {0});
					
					Vector4f column = new Vector4f ();
					for (int wx = 0; wx < 4; wx++) {
						vp.getColumn (wx, column);
						vpBuffer[wx * 4] = column.x;
						vpBuffer[wx * 4 + 1] = column.y;
						vpBuffer[wx * 4 + 2] = column.z;
						vpBuffer[wx * 4 + 3] = column.w;
					}
					working.drawData (0).setUniforms (uniformsA);
					working.drawData (1).setUniforms (uniformsB);
					glUniform1f (dtLoc, (float)GameLoop.deltaTime ());
					glUniformMatrix4fv (vpLoc, true, vpBuffer);
					glDrawArrays (
							GL_TRIANGLE_STRIP,
							0,
							4);
				}
				
				glDisableVertexAttribArray (0);
				glDisableVertexAttribArray (1);

				glfwSwapBuffers(window); // swap the color buffers
		}		
		// Poll for window events. The key callback above will only be
		// invoked during this call.
		glfwPollEvents();
			
	}
	
	public void render () {
			
		//Reset the draw call buffer
		drawLock.lock ();
		lastCalls = callBuffer;
		callBuffer = new ArrayList<DrawCall> ();
		drawLock.unlock ();
		
	}
	
	/**
	 * Gets the dimensions of the buffer, e.g. the resolution of the output.
	 * @return The dimensions of this GameWindow's buffer as an int array, in the format [width, height]
	 */
	public int[] getResolution () {
		return new int[] {resolution[0], resolution[1]};
	}
	
	/**
	 * Sets the resolution of the buffer to the given width and height; erases its contents.
	 * @param width The width to use, in pixels
	 * @param height The height to use, in pixels
	 */
	public void setResolution (int width, int height) {
		resolution[0] = width;
		resolution[1] = height;
	}
	
	public InputManager getInputImage () {
		return inputManager.createImage ();
	}

	public void resetInputBuffers () {
		inputManager.resetKeyBuffers ();
		inputManager.resetMouseBuffers ();
	}
	
	public void drawSprite (Transform t, GLTexture tex, GameObject obj) {
		drawLock.lock ();
		DrawCall.DefaultDrawData drawData = new DrawCall.DefaultDrawData (t, tex);
		callBuffer.add (new DrawCall (new DrawCall.DrawData[] {drawData, drawData}, obj));
		drawLock.unlock ();
	}
	
	public void drawSprite (Transform t, GLTexture tex) {
		drawLock.lock ();
		DrawCall.DefaultDrawData drawData = new DrawCall.DefaultDrawData (t, tex);
		callBuffer.add (new DrawCall (new DrawCall.DrawData[] {drawData, drawData}, null));
		drawLock.unlock ();
	}
	
	public void drawSprite (Transform a, Transform b, GLTexture tex, GameObject obj) {
		drawLock.lock ();
		DrawCall.DefaultDrawData drawDataA = new DrawCall.DefaultDrawData (a, tex);
		DrawCall.DefaultDrawData drawDataB = new DrawCall.DefaultDrawData (b, tex);
		callBuffer.add (new DrawCall (new DrawCall.DrawData[] {drawDataA, drawDataB}, obj));
		drawLock.unlock ();
	}
	
	public void drawSprite (Transform a, Transform b, GLTexture tex) {
		drawLock.lock ();
		DrawCall.DefaultDrawData drawDataA = new DrawCall.DefaultDrawData (a, tex);
		DrawCall.DefaultDrawData drawDataB = new DrawCall.DefaultDrawData (b, tex);
		callBuffer.add (new DrawCall (new DrawCall.DrawData[] {drawDataA, drawDataB}, null));
		drawLock.unlock ();
	}
	
	public class WinResizeCallback implements GLFWWindowSizeCallbackI {

		@Override
		public void invoke (long window, int wwidth, int wheight) {
			width = wwidth;
			height = wheight;
			glViewport (0, 0, width, height);
		}
		
	}
	
}