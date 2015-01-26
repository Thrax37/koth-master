package controller;

public class Building {
	private BuildingType type;
	private int completion;
	
	
	public Building(BuildingType type) {
		super();
		this.type = type;
		this.completion = 0;
	}
	
	public BuildingType getType() {
		return type;
	}

	public int getCompletion() {
		return completion;
	}


	public void setCompletion(int completion) {
		this.completion = completion;
	}
}
