package engine;

import java.util.Random;

public class State {
	private int play, player, value;
	public int[][] board;
	public State[] children;
	
	public State(int p, int[][] board, int player) {
		this.play = p;
		this.board = board;
		children = new State[6];
		this.player = player;
	}
	
	public int getPlay() {
		return play;
	}
	public int getPlayer() {
		return player;
	}
	public void addChild(State child) {
		int pos = findPos();
		if (pos != -1) children[pos] = child;
	}
	public int findPos() {
		for (int i = 0; i < children.length; i++) {
			if (children[i] == null) return i;
		}
		return -1;
	}
	public int findChildPlay(int val) {
	
		if (children == null) {
			return 0;
		}
		
		for (int i = 0; i < 6; i++) {
			if (children[i].value == val) {
				return children[i].play;
			}
		}
		
		return 0;
	}
	public void setValue(int val) {
		this.value = val;
	}
	public int getValue() {
		return value;
	}
}
