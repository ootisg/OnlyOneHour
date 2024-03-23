package engine;

import java.util.ArrayList;

import javafx.scene.media.AudioClip;
import gameObjects.CharlieConnect;
import gameObjects.CheatingCharlie;
import gameObjects.Connect;
import gameObjects.LarryConnect;
import gameObjects.ConnectFourGame;
import gameObjects.ConnectMap;
import gameObjects.DarkCoolean;
import gameObjects.DarkCooleanConnect;
import gameObjects.DeliriousDerek;
import gameObjects.DerekConnect;
import gameObjects.HenryConnect;
import gameObjects.HorizontalHenry;
import gameObjects.Imagamer;
import gameObjects.ImagamerConnect;
import gameObjects.JeffWeiner;
import gameObjects.Jerry;
import gameObjects.JerryConnect;
import gameObjects.LeftLarry;
import gameObjects.MerylConnect;
import gameObjects.MirroredMeryl;
import gameObjects.RandomRandy;
import gameObjects.RandyConnect;
import map.Room;
import titleSequence.TitleScreen;

import java.awt.event.KeyEvent;



public class GameCode {
	
	static int veiwX;
	static int veiwY;
	

	public static void testBitch () {
		
		
	}
	
	public static void beforeGameLogic () {
		
	}

	public static void afterGameLogic () {
		
	}

	public static void init () {

	}
	
	public static void gameLoopFunc () {
		
	}
	
	  public static void removeAsker(GameObject asker) {
		  Asker toAsk = getAsker(asker);
		  askers.remove(toAsk);
	  }
	  
	  public static boolean keyCheck(int keyCode, GameObject whosAsking) {
			boolean returnValue = GameLoop.getInputImage().keyDown(keyCode);
		    
			Asker asking = getAsker(whosAsking);
			
			if (returnValue) {
				
				asking.getKeys().add(keyCode);
			}
			
			
			return returnValue;
		  }
		
		public static Asker getAsker (GameObject whosAsking) {
		
			Asker asking = null;
			
			boolean foundAsker = false;
			
			for (int i = 0; i < askers.size(); i++) {
				if (askers.get(i).isAsker(whosAsking)) {
					asking = askers.get(i);
					foundAsker = true;
					break;
				}
			}
			
			if (!foundAsker) {
				askers.add(new Asker(whosAsking));
				asking = askers.get(askers.size() -1);
			}
			
			return asking;
		}
		  
		  public static boolean keyPressed(int keyCode, GameObject whosAsking) {
			boolean returnValue = GameLoop.getInputImage().keyPressed(keyCode);
			
			Asker asking = getAsker(whosAsking);
			
			if (returnValue && !asking.getKeys().contains(keyCode)) {
				asking.getKeys().add(keyCode);
				return returnValue;
			} else {
				return false;
			}
			
			
		  }
		  
		  public static boolean keyReleased(int keyCode) {
		    return GameLoop.getInputImage().keyReleased(keyCode);
		  }
	
	
	public static void renderFunc () {
		Room.render();
	}
	
	public static void beforeRender() {
		
	}
	
	public static void afterRender()
	{
		
	}
		
	public static int getResolutionX() {
		return GameLoop.wind.getResolution()[0];
	}
	public static int getResolutionY() {
		return GameLoop.wind.getResolution()[1];
	}
	
	public static int getViewX() {
		return veiwX;
	}



	public static void setViewX(int newVeiwX) {
		veiwX = newVeiwX;
	}



	public static int getViewY() {
		return veiwY;
	}



	public static void setViewY(int newVeiwY) {
		veiwY = newVeiwY;
	}
	


	
}
