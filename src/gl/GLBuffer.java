package gl;

import static org.lwjgl.opengl.GL15.*;

public class GLBuffer {
	
	private int bufferName;
	private long size; //size in bytes
	private int bufferType;
	private int drawType;
	
	public GLBuffer (int bufferType, int drawType, long size) {
		
		this.bufferType = bufferType;
		this.drawType = drawType;
		bufferName = glGenBuffers ();
		glBufferData (bufferType, size, drawType);
	
	}
	
	public GLBuffer (int bufferType, int drawType, double[] data) {
		
		this.bufferType = bufferType;
		this.drawType = drawType;
		bufferName = glGenBuffers ();
		glBindBuffer (bufferType, bufferName);
		glBufferData (bufferType, data, drawType);
		
	}
	
	public GLBuffer (int bufferType, int drawType, float[] data) {
		
		this.bufferType = bufferType;
		this.drawType = drawType;
		bufferName = glGenBuffers ();
		glBindBuffer (bufferType, bufferName);
		glBufferData (bufferType, data, drawType);
		
	}
	
	public GLBuffer (int bufferType, int drawType, long[] data) {
		
		this.bufferType = bufferType;
		this.drawType = drawType;
		bufferName = glGenBuffers ();
		glBindBuffer (bufferType, bufferName);
		glBufferData (bufferType, data, drawType);
		
	}
	
	public GLBuffer (int bufferType, int drawType, int[] data) {
		
		this.bufferType = bufferType;
		this.drawType = drawType;
		bufferName = glGenBuffers ();
		glBindBuffer (bufferType, bufferName);
		glBufferData (bufferType, data, drawType);
		
	}
	
	public void setData (double[] data) {
		glBindBuffer (bufferType, bufferName);
		glBufferData (bufferType, data, drawType);
	}
	
	public void setData (long offset, double[] data) {
		glBindBuffer (bufferType, bufferName);
		glBufferSubData (bufferType, offset, data);
	}
	
	public void setData (float[] data) {
		glBindBuffer (bufferType, bufferName);
		glBufferData (bufferType, data, drawType);
	}
	
	public void setData (long offset, float[] data) {
		glBindBuffer (bufferType, bufferName);
		glBufferSubData (bufferType, offset, data);
	}
	
	public void setData (long[] data) {
		glBindBuffer (bufferType, bufferName);
		glBufferData (bufferType, data, drawType);
	}
	
	public void setData (long[] data, long offset) {
		glBindBuffer (bufferType, bufferName);
		glBufferSubData (bufferType, offset, data);
	}
	
	public void setData (int[] data) {
		glBindBuffer (bufferType, bufferName);
		glBufferData (bufferType, data, drawType);
	}
	
	public void setData (int[] data, long offset) {
		glBindBuffer (bufferType, bufferName);
		glBufferSubData (bufferType, offset, data);
	}
	
	public void bindBuffer () {
		glBindBuffer (bufferType, bufferName);
	}
	
	public void draw () {
		glDrawBuffer (bufferName);
	}
	
	public int getBufferName () {
		return bufferName;
	}
	
	public long getBufferSize () {
		return size;
	}
	
	public int getBufferType () {
		return bufferType;
	}
	
	public int getDrawType () {
		return drawType;
	}
	
}
