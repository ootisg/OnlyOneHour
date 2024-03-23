package engine;

import static org.lwjgl.opengl.GL40.*;

import gl.GLBuffer;

public class ObjectVBOS {
	
	private GLBuffer spriteVBO;
	private double[] verts;
	
	public ObjectVBOS () {
		
		verts = new double[] {
				-0.5, -0.5, 0.0,   0.0,  0.0,
				 0.5, -0.5, 0.0,   1.0,  0.0,
				-0.5,  0.5, 0.0,   0.0,  1.0,
				 0.5,  0.5, 0.0,   1.0,  1.0
		};
		spriteVBO = new GLBuffer (GL_ARRAY_BUFFER, GL_STATIC_DRAW, verts);
		
	}
	
	public GLBuffer getSpriteVBO () {
		return spriteVBO;
	}
	
	public void bindSpriteVBO () {
		spriteVBO.bindBuffer ();
	}
	
}
