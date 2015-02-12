import java.util.ArrayList;
import java.util.List;

public class Commander {

	int round;
	int phase;
	int playerID;
	int thisTownID;
	
	List<Town> towns;
    List<Town> myTowns;
    List<Town> otherTowns;

    Town thisTown;
	
    public static void main(String[] args){
        if (args.length == 0) {
        	System.out.println("15 10 12 10 7 5 1 40");
        } else {
        	new Commander().conquer(args[0].split(";"));
        }
    }
    
    private void conquer(String[] args) {
    	
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
        
		if (phase == 2) { 				// Steal
			//Command : S destinationId corsairs
			steal();
		} else if (phase == 3) {		// Recruit
			//Command : R warlocks crusaders amazons corsairs bishops necromancers architects
			recruit()	;
		} else if (phase == 6) {		// Convert
			//Command : C destinationId warlocks crusaders amazons 
			convert();
		} else if (phase == 7) {		// Attack
			//Command : A destinationId warlocks crusaders amazons
			attack();	
		} else if (phase == 8) {		// Resurrect
			//Command : R corpses
			resurrect();	
		} else if (phase == 9) {		// Move
			//Command : M destinationId warlocks crusaders amazons corsairs bishops necromancers architects
			move();
		} else if (phase == 11) {		// Build
			//Command : B building building building... (T: Temple, B: Barracks, E: Estate, P: Palace)
			build();
		}
    }
    
    private void steal() {
    	Town richestTown = otherTowns.stream().max((a,b) -> a.gold - b.gold).get();
    	System.out.println("S " + richestTown.getId() + " " + thisTown.getCorsairs());
    }
    
    private void recruit() {
    	
    	int maxUnitsToRecruits = Math.floorDiv(thisTown.getPeons() - thisTown.getUnits(), 2);
    	int goldAvailable = thisTown.getGold() - (thisTown.getUnits() * 5);
    	int unitsRecruited = 0;
    	int cost = 10;
    	int[] recruits = new int[3];
    	int i = 0;
    	int necromancers = Math.max(0, 5 - thisTown.getNecromancers());
    	while (goldAvailable >= 0 && unitsRecruited <= maxUnitsToRecruits) {
    		 i = (i >= recruits.length - 1 ? 0 : i+1);
    		 recruits[i]++;
    		 unitsRecruited++;
    		 goldAvailable-=cost;
    	}
    	if (unitsRecruited > 0) {
    		System.out.println("R " + recruits[0] + " " + recruits[1] + " " + recruits[2] + " 0 0 " +  necromancers + " 0");
    	} else {
    		System.out.println("W");
    	}
    }

    private void convert() {
    	
     	Town biggestTown = otherTowns.stream().max((a,b) -> a.getCitizens() - b.getCitizens()).get();
       	int goldAvailable = thisTown.getGold() - (thisTown.getUnits() * 5);
        int bishopsAvailable = thisTown.getBishops();
       	int unitsConverted = 0;
    	int cost = 50;
    	int[] converts = new int[3];
    	int i = 0;
    	while (goldAvailable >= 0 && unitsConverted <= bishopsAvailable) {
    		 i = (i >= converts.length - 1 ? 0 : i+1);
    		 converts[i]++;
    		 goldAvailable-=cost;
    	}
      	System.out.println("C " + biggestTown.getId() + " " + converts[0] + " " + converts[1] + " " + converts[2]);  
    }

    private void attack() {
    	
    	Town lessDefendedTown = otherTowns.stream().max((a,b) -> a.getSoldiers() - b.getSoldiers()).get();
    	int neededWarlocks =  thisTown.getWarlocks() - lessDefendedTown.getWarlocks();
    	int neededCrusaders = thisTown.getCrusaders() - lessDefendedTown.getCrusaders();
    	int neededAmazons = thisTown.getAmazons() - lessDefendedTown.getAmazons() ;
    	
    	if (neededWarlocks > 0 && neededCrusaders > 0 && neededAmazons > 0) {
    		System.out.println("A " + lessDefendedTown.getId() + " " + (lessDefendedTown.getWarlocks() + 1) + " " + (lessDefendedTown.getCrusaders() + 1) + " " + (lessDefendedTown.getAmazons() + 1));  
    	} else {
    		System.out.println("W");
    	}
    	
    }

    private void move() {
    	System.out.println("W");
    }
    
    private void resurrect() {
      	int goldAvailable = thisTown.getGold() - (thisTown.getUnits() * 5);
      	int corpsesAvailable = thisTown.getCorpses();
      	int availableNecromancers = thisTown.getNecromancers();
      	int raiseCapacity = availableNecromancers * 5;
      	int raisedCorpses = 0;
      	while (corpsesAvailable >= 0 && raiseCapacity >= 0 && goldAvailable >= 0) {
      		raisedCorpses++;
      		corpsesAvailable--;
      		goldAvailable -= 20;
      		raiseCapacity--;
      	}
      	if (raisedCorpses > 0) {
      		System.out.println("R " + raisedCorpses);
      	} else {
      		System.out.println("W");
      	}
    }

    private void build() {
      	int goldAvailable = thisTown.getGold() - (thisTown.getUnits() * 5);
      	if (goldAvailable >= 400) {
      		System.out.println("B B");
      	} else {
      		System.out.println("W");
      	}
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