package controller;

public class Score implements Comparable<Score> {
	private Player player;
	private int towns;
	private int knights;
	private int lowborns;

	public Score(Player player, int towns, int knights, int lowborns) {
		super();
		this.player = player;
		this.towns = towns;
		this.knights = knights;
		this.lowborns = lowborns;
	}
	
	public String print() {
		return player.getDisplayName() + "(" + towns + ", " + knights + ", " + lowborns + ")";
	}
	
	@Override
	public int compareTo(Score other) {
		if (towns > other.towns) {
			return 1;
		} else if (towns < other.towns){
			return -1;
		}
		
		if (knights > other.knights) {
			return 1;
		} else if (knights < other.knights){
			return -1;
		}
		
		if (lowborns > other.lowborns) {
			return 1;
		} else if (lowborns < other.lowborns){
			return -1;
		}
		
		return 0;
	}	
}
