package engine;

import java.util.Comparator;

public class RenderComparator implements Comparator<GameObject> {
	
	@Override
	public int compare(GameObject o1, GameObject o2) {
		return (int)(o1.getRenderPriority () - o2.getRenderPriority ());
	}
}
