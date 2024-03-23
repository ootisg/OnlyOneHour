package engine;

import java.util.Comparator;

public class GameLogicComparator implements Comparator<GameObject> {
	
	@Override
	public int compare(GameObject o1, GameObject o2) {
		return (int)(o1.getGameLogicPriority () - o2.getGameLogicPriority ());
	}

}
