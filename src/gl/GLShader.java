package gl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static org.lwjgl.opengl.GL20.*;

public class GLShader {
	
	private int shaderName;
	private boolean compiled = false;
	
	private String shaderSource;
	private int shaderType;
	
	public GLShader (String shaderSource, int shaderType) {
		
		//Compile shader
		this.shaderSource = shaderSource;
		this.shaderType = shaderType;
		
	}
	
	public void compile () {
		
		shaderName = glCreateShader (shaderType);
		glShaderSource (shaderName, shaderSource);
		glCompileShader (shaderName);
		
		//Print the error log if shader didn't compile
		int shaderOk = glGetShaderi (shaderName, GL_COMPILE_STATUS);
		if (shaderOk == 0) {
			System.out.println ("Error: shader failed to compile!");
			String errorMsg = glGetShaderInfoLog (shaderName);
			System.out.print (errorMsg);
			glDeleteShader (shaderName);
		} else {
			compiled = true;
		}
		
	}
	
	public int getShaderName () {
		return shaderName;
	}
	
	public boolean isCompiled () {
		return compiled;
	}
	
	public static GLShader shaderFromFile (String filepath, int shaderType) {
		
		try {
			File f = new File (filepath);
			Scanner s = new Scanner (f);
			StringBuilder sb = new StringBuilder ();
			while (s.hasNextLine ()) {
				sb.append (s.nextLine ());
				if (s.hasNextLine ()) {
					sb.append ("\n");
				}
			}
			s.close ();
			return new GLShader (sb.toString (), shaderType);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Return null if file was not found
		return null;
		
	}
	
}
