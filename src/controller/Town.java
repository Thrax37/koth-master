package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Town {
	private final int id;
	private Player owner;
	private int gold;
	private int corpses;
	private int warlocks;
	private int crusaders;
	private int amazons;
	private int corsairs;
	private int bishops;
	private int necromancers;
	private int architects;
	private int peons;
	private List<Building> buildings;

	public Town(int id, Player owner, int gold, int corpses, int warlocks, int crusaders, int amazons, int corsairs, int bishops, int necromancers, int architects, int peons) {
		this.id = id;
		this.gold = gold;
		this.corpses = corpses;
		this.owner = owner;
		this.warlocks = warlocks;
		this.crusaders = crusaders;
		this.amazons = amazons;
		this.corsairs = corsairs;
		this.bishops = bishops;
		this.necromancers = necromancers;
		this.architects = architects;
		this.peons = peons;
		this.buildings = new ArrayList<Building>();
	}
	
	public Town(int id) {
		this.id = id;
	}

	public void setOwner(Player newOwner) {
		owner = newOwner;
	}
	
	public Player getOwner() {
		return owner;
	}
	
	public int getId() {
		return id;
	}
	
	public int getGold() {
		return gold;
	}

	public int getCorpses() {
		return corpses;
	}

	public int getWarlocks() {
		return warlocks;
	}

	public int getCrusaders() {
		return crusaders;
	}

	public int getAmazons() {
		return amazons;
	}

	public int getCorsairs() {
		return corsairs;
	}

	public int getBishops() {
		return bishops;
	}

	public int getNecromancers() {
		return necromancers;
	}

	public int getArchitects() {
		return architects;
	}
	
	public int getPeons() {
		return peons;
	}
	
	public int getTemples() {
		int count = 0;
		for (Building building : this.buildings) {
			if (building.getCompletion() == Game.COMPLETION_NEEDED) {
				switch (building.getType()) {
					case TEMPLE : count++; break;
					default: break;
				}
			}
		}
		return count;
	}
	
	public int getBarracks() {
		int count = 0;
		for (Building building : this.buildings) {
			if (building.getCompletion() == Game.COMPLETION_NEEDED) {
				switch (building.getType()) {
					case BARRACKS : count++; break;
					default: break;
				}
			}
		}
		return count;
	}
	
	public int getEstates() {
		int count = 0;
		for (Building building : this.buildings) {
			if (building.getCompletion() == Game.COMPLETION_NEEDED) {
				switch (building.getType()) {
					case ESTATE : count++; break;
					default: break;
				}
			}
		}
		return count;
	}
	
	public int getPalaces() {
		int count = 0;
		for (Building building : this.buildings) {
			if (building.getCompletion() == Game.COMPLETION_NEEDED) {
				switch (building.getType()) {
					case PALACE : count++; break;
					default: break;
				}
			}
		}
		return count;
	}
	
	public int getPopulation() {
		return getWarlocks() + getCrusaders() + getAmazons() + getCorsairs() + getBishops() + getNecromancers() + getArchitects() + getPeons();
	}
	
	public List<Building> getBuildings() {
		return buildings;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}

	public void setCorpses(int corpses) {
		this.corpses = corpses;
	}

	public void setWarlocks(int warlocks) {
		this.warlocks = warlocks;
	}

	public void setCrusaders(int crusaders) {
		this.crusaders = crusaders;
	}

	public void setAmazons(int amazons) {
		this.amazons = amazons;
	}

	public void setCorsairs(int corsairs) {
		this.corsairs = corsairs;
	}

	public void setBishops(int bishops) {
		this.bishops = bishops;
	}

	public void setNecromancers(int necromancers) {
		this.necromancers = necromancers;
	}

	public void setArchitects(int architects) {
		this.architects = architects;
	}

	public void setPeons(int peons) {
		this.peons = peons;
	}

	public void setBuildings(List<Building> buildings) {
		this.buildings = buildings;
	}

	public String getCommand(String args) throws Exception {
		//neutral player
		if ("".equals(owner.getCmd())) {
			return "W";
		}
		Process proc = null;
		Scanner stdin = null;
		try {
			proc = Runtime.getRuntime().exec(owner.getCmd() + " " + args);
			stdin = new Scanner(proc.getInputStream());
			StringBuilder response = new StringBuilder();
			while (stdin.hasNext()) {
				response.append(stdin.next()).append(' ');
			}
			return response.toString();	
		} finally {
			if (stdin != null) stdin.close();
			if (proc != null) proc.destroy();
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other != null && other instanceof Town) {
			return getId() == ((Town) other).getId();
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "Id: " + id + " Owner: " + owner.getDisplayName();
	}
	
}
