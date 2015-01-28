import java.util.ArrayList;
import java.util.List;

public class Sleeper {

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
        	new Sleeper().sleep(args[0].split(";"));
        }
    }
    
    private void sleep(String[] args) {
    	
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
			System.out.println("W");
		} else if (phase == 3) {		// Recruit
			//Command : R warlocks crusaders amazons corsairs bishops necromancers architects
			System.out.println("W");	
		} else if (phase == 6) {		// Convert
			//Command : C destinationId warlocks crusaders amazons 
			System.out.println("W");	
		} else if (phase == 7) {		// Attack
			//Command : A destinationId warlocks crusaders amazons
			System.out.println("W");	
		} else if (phase == 8) {		// Resurrect
			//Command : R necromancers
			System.out.println("W");	
		} else if (phase == 9) {		// Move
			//Command : M destinationId warlocks crusaders amazons corsairs bishops necromancers architects
			System.out.println("W");	
		} else if (phase == 11) {		// Build
			//Command : B building building building... (T: Temple, B: Barracks, E: Estate, P: Palace)
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