package engine;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL20;

import gl.GLTexture;

public class DrawCall {
	
	private GameObject obj;
	private DrawData[] drawData;
	
	public DrawCall (DrawData data, GameObject obj) {
		this.drawData = new DrawData[] {data};
		this.obj = obj;
	}
	
	public DrawCall (DrawData[] data, GameObject obj) {
		this.drawData = data;
		this.obj = obj;
	}
	
	public DrawCall (DrawCall call) {
		this.drawData = call.drawData;
		this.obj = call.obj;
	}
	
	public GameObject getCallingObject () {
		return obj;
	}
	
	public int size () {
		return drawData.length;
	}
	
	public DrawData drawData (int index) {
		return drawData[index];
	}
	
	public interface Translatable {
		
		void translate (float x, float y, float z);
		
	}
	
	public interface Scaleable {
		
		void scale (float x, float y);
		
	}
	
	public interface Rotatable {
		
		void rotate (float degrees);
		
	}
	
	public interface DrawData {
		
		void setUniforms (int[] locations);
		void setTextures (int[] locations);
		Matrix4f getTransform ();
		Transform getT ();
		
	}
	
	public static class DefaultDrawData implements DrawData, Scaleable, Rotatable, Translatable {
		
		private Transform t;
		private GLTexture tex;
		
		public DefaultDrawData (float x, float y, float z, float scaleX, float scaleY, float theta, GLTexture tex) {
			t = new Transform (new Vector3f (x, y, z), new Vector2f (scaleX, scaleY), theta);
			this.tex = tex;
		}
		
		public DefaultDrawData (Transform t, GLTexture tex) {
			this.t = t;
			this.tex = tex;
		}
		
		public void setUniforms (int[] locations) {
			GL20.glUniform3f (locations[0], t.getPos ().x, t.getPos ().y, t.getPos ().z);
			GL20.glUniform2f (locations[1], t.getScl ().x, t.getScl ().y);
			GL20.glUniform1f (locations[2], t.getRot ());
//			GL20.glUniform3f (locations[0], 0, 0, 0);
//			GL20.glUniform2f (locations[1], 128, 128);
//			GL20.glUniform1f (locations[2], 0);
		}
		
		public void setTextures (int[] locations) {
			tex.bindTexture ();
		}
		
		public void setTexture (GLTexture tex) {
			this.tex = tex;
		}

		public Transform getT () {
			return t;
		}
		
		@Override
		public Matrix4f getTransform () {
			return new Matrix4f (); //TODO
		}

		@Override
		public void rotate (float degrees) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void scale (float x, float y) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void translate (float x, float y, float z) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
