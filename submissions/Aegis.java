import java.util.ArrayList;
import java.util.List;

public class Aegis {

	int round;
	int phase;
	int playerID;
	int thisTownID;
	
	final int PEONS_PER_BISHOPS = 50;
	final int GOLD_STOLEN_PER_CORSAIR = 10;
	final int AVERAGE_COST_PER_UNIT = 2;
	final int GOLD_SAFETY = 100;
	final int PEONS_SAFETY = 20;
	final int COST_CORSAIR = 12;
	final int COST_SOLDIER = 10;
	final int COST_ESTATE = 200;
	final int MAX_CORSAIRS = 30;
	
	List<Town> towns;
    List<Town> myTowns;
    List<Town> otherTowns;

    Town thisTown;
	
    public static void main(String[] args){
        if (args.length == 0) {
        	System.out.println("10 10 10 10 1 1 3 55");
        } else {
        	new Aegis().protect(args[0].split(";"));
        }
    }
    
    private void protect(String[] args) {
    	
    	round = Integer.parseInt(args[0]);
		phase = Integer.parseInt(args[1]);
		playerID = Integer.parseInt(args[2]);
		thisTownID = Integer.parseInt(args[3]);
		
		towns = new ArrayList<>();
        myTowns = new ArrayList<>();
        otherTowns = new ArrayList<>();
		
        for (int i = 4; i < args.length; i++){
            towns.add(new Town(args[i]));
        }

        for (Town town : towns){
            if (town.isMine()){
                myTowns.add(town);
                if (town.isThisTown()){
                    thisTown = town;
                }
            } else {
                otherTowns.add(town);
            }
        }
        
		switch (phase) {
		case 2: steal(); break;
		case 3: recruit(); break;
		case 6: convert(); break;
		case 7: attack(); break;
		case 8: move(); break;
		case 9: resurrect(); break;
		case 11: build(); break;
		}
    }
    
    private void steal() {
       	Town richestTown = otherTowns.stream().max((a,b) -> a.gold - b.gold).get();
    	System.out.println("S " + richestTown.getId() + " " + thisTown.getCorsairs());
    }
    
    private void recruit() {
    	Town richestTown = otherTowns.stream().max((a,b) -> a.gold - b.gold).get();
    	Town soldierTown = otherTowns.stream().max((a,b) -> (a.amazons + a.warlocks + a.crusaders) - (b.amazons + b.warlocks + b.crusaders)).get();
    	int requiredBishops = Math.max(0, Math.floorDiv(thisTown.getPeons(), PEONS_PER_BISHOPS) - thisTown.getBishops());
    	int requiredNecromancers = Math.max(0, 1 - thisTown.getNecromancers());
    	int requiredArchitects = Math.max(0, 5 - thisTown.getArchitects());
    	int requiredCorsairs = Math.max(0, Math.min(MAX_CORSAIRS - thisTown.getCorsairs(), Math.floorDiv(richestTown.getGold(), GOLD_STOLEN_PER_CORSAIR)));
    	int goldAvailable = thisTown.getGold() - (AVERAGE_COST_PER_UNIT * thisTown.getUnits()) - GOLD_SAFETY;
    	int peonsAvailable = Math.max(0, thisTown.getPeons() - PEONS_SAFETY);
    	int recruitedCorsairs = 0;
    	int[] recruits = new int[3];
    	int i = 0;
    	while (peonsAvailable >= 5 && goldAvailable >= 50 && recruitedCorsairs < requiredCorsairs) {
    		recruitedCorsairs++;
    		peonsAvailable--;
    		goldAvailable-=COST_CORSAIR;
    	}
    	while (peonsAvailable >= 5 && goldAvailable >= 50) {
	    	if (soldierTown.getSoldiers() > thisTown.getSoldiers()) {
	    		i = (i >= recruits.length - 1 ? 0 : i+1);
	    		recruits[i]++;
	    		peonsAvailable--;
	   		 	goldAvailable-=COST_SOLDIER;
	    	} else {
	    		break;
	    	}
    	}
    	if (recruits[0] + recruits[1] + recruits[2] + recruitedCorsairs + requiredBishops + requiredNecromancers + requiredArchitects > 0) {
    		System.out.println("R " + recruits[0] + " " + recruits[1] + " " + recruits[2] + " " + recruitedCorsairs + " " + requiredBishops + " " +  requiredNecromancers + " " + requiredArchitects);
    	} else {
    		System.out.println("W");
    	}
    }

    private void convert() { 
    	System.out.print("W");
    }

    private void attack() {
    	if (this.countOtherPlayers() <= 1) {
    	  	Town richestTown = otherTowns.stream().max((a,b) -> a.getGold() - b.getGold()).get();
    	  	int neededWarlocks =  thisTown.getWarlocks() - richestTown.getWarlocks();
        	int neededCrusaders = thisTown.getCrusaders() - richestTown.getCrusaders();
        	int neededAmazons = thisTown.getAmazons() - richestTown.getAmazons() ;
        	
        	if (neededWarlocks > 0 && neededCrusaders > 0 && neededAmazons > 0) {
        		System.out.println("A " + richestTown.getId() + " " + (richestTown.getWarlocks() + 1) + " " + (richestTown.getCrusaders() + 1) + " " + (richestTown.getAmazons() + 1));  
        	} else {
        		System.out.println("W");
        	}
    	} else {
    		System.out.println("W");
    	}
    }

    private void move() {
       	for (Town town : myTowns) {
       		int goldBalance = town.getGold() - (AVERAGE_COST_PER_UNIT * thisTown.getUnits()) + GOLD_SAFETY;
       		if (goldBalance <= 0) {
       			System.out.println("T " + town.getId() + " " + (-goldBalance + GOLD_SAFETY));
       			break;
       		}
       	}
       	System.out.println("W");
    }
    
    private void resurrect() {
    	if (thisTown.getCorpses() > 0) {
    		int corpses = Math.min(5, thisTown.getCorpses());
    		System.out.print("R " + corpses);
    	} else {
    		System.out.print("W");
    	}
    }

    private void build() {
      	int goldAvailable = thisTown.getGold() - (AVERAGE_COST_PER_UNIT * thisTown.getUnits()) - GOLD_SAFETY;
      	if (goldAvailable >= (COST_ESTATE + 50)) {
      		System.out.println("B E");
      	} else {
      		System.out.println("W");
        }
    }
    
    public int countOtherPlayers() {
    	List<Integer>players = new ArrayList<>();
    	for (Town town : otherTowns) {
    		if (!players.contains(town.getOwnerId()) && town.getOwnerId() >= 0) players.add(town.getOwnerId());
    	}
    	return players.size();
    }
    
    private class Town {
		 
        private final int ownerId;
        private final int id;
        private final int gold;
        private final int corpses;
        private final int warlocks;
        private final int crusaders;
        private final int amazons;
        private final int corsairs;
        private final int bishops;
        private final int necromancers;
        private final int architects;
        private final int peons;
        private final int temples;
        private final int barracks;
        private final int estates;
        private final int palaces;

        public Town(String string){
            String[] args = string.split("_");
            ownerId = Integer.parseInt(args[0]);
            id = Integer.parseInt(args[1]);
            gold = Integer.parseInt(args[2]);
            corpses = Integer.parseInt(args[3]);
            warlocks = Integer.parseInt(args[4]);
            crusaders = Integer.parseInt(args[5]);
            amazons = Integer.parseInt(args[6]);
            corsairs = Integer.parseInt(args[7]);
            bishops = Integer.parseInt(args[8]);
            necromancers = Integer.parseInt(args[9]);
            architects = Integer.parseInt(args[10]);
            peons = Integer.parseInt(args[11]);
            temples = Integer.parseInt(args[12]);
            barracks = Integer.parseInt(args[13]);
            estates = Integer.parseInt(args[14]);
            palaces = Integer.parseInt(args[15]);
        }
		public int getOwnerId() {
			return ownerId;
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
			return temples;
		}
		public int getBarracks() {
			return barracks;
		}
		public int getEstates() {
			return estates;
		}
		public int getPalaces() {
			return palaces;
		}
		public int getBuildings() {
			return getTemples() + getBarracks() + getEstates() + getPalaces();
		}
		public int getSoldiers() {
			return getWarlocks() + getCrusaders() + getAmazons();
		}
		public int getUnits() {
			 return getSoldiers() + getCorsairs() + getBishops() + getNecromancers() + getArchitects();
		}
		public int getCitizens() {
			return getUnits() + getPeons();
		}
		public boolean isMine(){
            return getOwnerId() == playerID;
        }
		public boolean isThisTown(){
            return id == thisTownID;
        }
    }

}