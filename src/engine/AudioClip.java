package engine;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

//Minimal substitute for JavaFX's AudioClip class
//Mostly copied from Jeffrey's SoundPlayer class for 16BL (sorry)

public class AudioClip implements LineListener {
	
	private boolean playing;
	private boolean loaded;
	private boolean loops;
	
	private AudioInputStream stream;
	private AudioFormat format;
	private DataLine.Info info;
	private Clip clip;
	
	public AudioClip (String uri) {
		URI uriObj;
		try {
			uriObj = new URI (uri);
			if (uriObj.getScheme ().equals ("file")) {
				String filepath = uriObj.getSchemeSpecificPart ();
				File f = new File (filepath);
				stream = AudioSystem.getAudioInputStream (f);
				format = stream.getFormat ();
				info = new DataLine.Info (Clip.class, format);
				clip = (Clip)AudioSystem.getLine (info);
				loaded = true;
				clip.addLineListener (this);
				clip.open (stream);
			} else {
				System.out.println ("Error: only file loading is supported");
				loaded = false;
			}
		} catch (URISyntaxException | UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			System.out.println ("Error: audio file " + uri + " failed to load.");
			loaded = false;
		}
	}
	
	public void loop () {
		loops = true;
		clip.loop (Clip.LOOP_CONTINUOUSLY);
	}
	
	public void play () {
		clip.start ();
	}
	
	public void stop () {
		clip.stop ();
	}
	
	public boolean isPlaying () {
		return playing;
	}
	
	public boolean isLooping () {
		return isPlaying () && loops;
	}
	
	public boolean isLoaded () {
		return loaded;
	}
	
	@Override
	public void update (LineEvent event) {
		if (event.getType() == LineEvent.Type.START) {
			playing = true;
		} else if (event.getType() == LineEvent.Type.STOP){
			clip.stop ();
			clip.flush ();
			clip.setFramePosition(0);
			playing = false;
		}
	}

}
