package engine;

import java.util.InputMismatchException;
import java.util.Scanner;

import gameObjects.AlphaBetaSearch;
public class SerbianConnectFour {
//	public static void main(String[] args) {
//		SerbianConnectFour scf = new SerbianConnectFour();
//		Scanner s = new Scanner(System.in);
//		scf.print();
//		int input = -1, check = 0, player = 1;
//		while (true) {
//			AlphaBetaSearch abs = new AlphaBetaSearch(scf);
////			System.out.println(input >= 0);
////			System.out.println(input <= 6);
//			while (!(input >= 0) || !(input <= 6)) {
//				try {
//					input = 
//							(player == 1) ? 
//							s.nextInt() 
//							: abs.alphaBetaSearch(scf.board, 4)
//							;
////					System.out.println(input >= 0);
////					System.out.println(input <= 6);
//				}
//				catch(InputMismatchException e) {
//					continue;
//				}
//			}
//			if (scf.add(scf.board, input, player)) {
//				input = -1;
//				check = scf.checkForWin(scf.board);
//				System.out.println();
//				scf.print();
//				player = (player == 1) ? 2 : 1;
//				if (check == 1 || check == 2)
//				{
//					System.out.println("Player " + check + " Wins!");
//					s.close();
//					break;
//				}
//			}
//			else System.out.println("Illegal Move."); //replace with something in game...
//			input = -1;
//		}
//		
//		
//
//	}
	private int[][] board = new int[7][6];
	private int[] xDir = {1, 0, 1, -1};
	private int[] yDir = {1, -1, 0, -1};
	public SerbianConnectFour() {
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 6; j++) {
				board[i][j] = 0;
			}
		}
	}
	
	public boolean add(int[][] board, int x, int player) {
		for (int i = 5; i > -1; i--) {
			if (board[x][i] == 0) {
				board[x][i] = player;
				return true;
			}
		}
		return false;
	}

	
	public int checkForWin(int[][] board) {
		int check, count = 1;
		for (int wx = 0; wx < 7; wx++) {
			for (int wy = 0; wy < 6; wy++) {
				if (board[wx][wy] != 0) {
					check = board[wx][wy];
					for (int q = 0; q < 4; q++) {
						int xMove = wx + xDir[q];
						int yMove = wy + yDir[q];
						while (xMove > -1 && xMove < 7 && yMove > -1 && yMove < 6) {
							if (board[xMove][yMove] == 0 || 
									board[xMove][yMove] == 
									((check == 1) ? 2 : 1)) break;
							if (board[xMove][yMove] == check) {
								count++;
							}
							xMove += xDir[q];
							yMove += yDir[q];
						}
						if (count >= 4) {
							return check;
						}
						count = 1;
					}
				}
			}
		}
		return 0;
	}
	
	public void print() {
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 6; j++) {
				System.out.print(board[i][j] + " ");
			}
			System.out.println();
		}
	}
	

}
