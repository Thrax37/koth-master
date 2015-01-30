import java.util.ArrayList;
import java.util.List;

public class Butter {

    int round;
    int phase;
    int playerID;
    int thisTownID;

    List<Town> towns;
    List<Town> myTowns;
    List<Town> otherTowns;

    Town thisTown;

    public Butter(String... args) {
        args = args[0].split(";");

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
    }

    public static void main(String[] args){
        if (args.length == 0) {
            System.out.println("12 12 12 12 13 13 13 13");
        } else {
            System.out.println(new Butter(args[0]).spread());
        }
    }

    private String spread() {

        if (phase == 2) {               // Steal
            //Command : S destinationId corsairs
            return steal();
        } else if (phase == 3) {        // Recruit
            //Command : R warlocks crusaders amazons corsairs bishops necromancers architects
            return recruit()    ;
        } else if (phase == 6) {        // Convert
            //Command : C destinationId warlocks crusaders amazons
            return convert();
        } else if (phase == 7) {        // Attack
            //Command : A destinationId warlocks crusaders amazons
            return attack();
        } else if (phase == 8) {        // Resurrect
            //Command : R corpses
            return resurrect();
        } else if (phase == 9) {        // Move
            //Command : M destinationId warlocks crusaders amazons corsairs bishops necromancers architects
            return move();
        } else if (phase == 11) {       // Build
            //Command : B building building building... (T: Temple, B: Barracks, E: Estate, P: Palace)
            return build();
        }
        throw new IllegalStateException(phase + "");
    }

    private String steal() {
        if (thisTown.getCorsairs() <= 0){
            return "W";
        }
        int mostGold = Integer.MIN_VALUE;
        Town richestTown = null;
        for (Town town : otherTowns){
            if (town.getGold() > mostGold){
                mostGold = town.getGold();
                richestTown = town;
            }
        }
        return "S " + richestTown.getId() + " " + thisTown.getCorsairs();
    }

    private String recruit() {
        int gold = thisTown.getFreeGold();

        int peons = thisTown.getPeons();
        int warlocks = thisTown.getWarlocks();
        int crusaders = thisTown.getCrusaders();
        int amazons = thisTown.getAmazons();
        int corsairs = thisTown.getCorsairs();
        int bishops = thisTown.getBishops();
        int necromancers = thisTown.getNecromancers();
        int architects = thisTown.getArchitects();

        int totalPeople = peons+warlocks+crusaders+amazons+corsairs+bishops+necromancers+architects;
        int averagePeople = totalPeople / 8;
        int extraPeons = peons - averagePeople;
        if (extraPeons <= 0 || gold <= 0){
            return "W";
        }

        int warlocksToAdd = warlocks < averagePeople ? 0 : warlocks - averagePeople;
        int crusadersToAdd = crusaders < averagePeople ? 0 : crusaders - averagePeople;
        int amazonsToAdd = amazons < averagePeople ? 0 : amazons - averagePeople;
        int corsairsToAdd = corsairs < averagePeople ? 0 : corsairs - averagePeople;
        int bishopsToAdd = bishops < averagePeople ? 0 : bishops - averagePeople;
        int necromancersToAdd = necromancers < averagePeople ? 0 : necromancers - averagePeople;
        int architectsToAdd = architects < averagePeople ? 0 : architects - averagePeople;

        warlocksToAdd = warlocksToAdd * 11 > gold ? gold / 11 : warlocksToAdd;
        gold -= warlocksToAdd * 11;
        crusadersToAdd = crusadersToAdd * 11 > gold ? gold / 11 : crusadersToAdd;
        gold -= crusadersToAdd * 11;
        amazonsToAdd = amazonsToAdd * 11 > gold ? gold / 11 : amazonsToAdd;
        gold -= amazonsToAdd * 11;
        corsairsToAdd = corsairsToAdd * 14 > gold ? gold / 14 : corsairsToAdd;
        gold -= corsairsToAdd * 14;
        bishopsToAdd = bishopsToAdd * 22 > gold ? gold / 22 : bishopsToAdd;
        gold -= bishopsToAdd * 22;
        necromancersToAdd = necromancersToAdd * 22 > gold ? gold / 22: necromancersToAdd;
        gold -= necromancersToAdd * 22;
        architectsToAdd = architectsToAdd * 17 > gold ? gold / 17 : architectsToAdd;
        gold -= architectsToAdd * 17;

        warlocksToAdd = warlocksToAdd > extraPeons ? extraPeons : warlocksToAdd;
        extraPeons -= warlocksToAdd;
        crusadersToAdd = crusadersToAdd > extraPeons ? extraPeons : crusadersToAdd;
        extraPeons -= crusadersToAdd;
        amazonsToAdd = amazonsToAdd > extraPeons ? extraPeons : amazonsToAdd;
        extraPeons -= amazonsToAdd;
        corsairsToAdd = corsairsToAdd > extraPeons ? extraPeons : corsairsToAdd;
        extraPeons -= corsairsToAdd;
        bishopsToAdd = bishopsToAdd > extraPeons ? extraPeons : bishopsToAdd;
        extraPeons -= bishopsToAdd;
        necromancersToAdd = necromancersToAdd > extraPeons ? extraPeons : necromancersToAdd;
        extraPeons -= necromancersToAdd;
        architectsToAdd = architectsToAdd > extraPeons ? extraPeons : architectsToAdd;

        return "R " + warlocksToAdd + " " + crusadersToAdd + " " + amazonsToAdd + " " + corsairsToAdd + " " +
                bishopsToAdd + " " + necromancersToAdd + " " + architectsToAdd;
    }

    private String convert() {
        return "W";
    }

    private String attack() {
        for (Town town : otherTowns){
            if (town.getSoldiers() * 4 < thisTown.getSoldiers()){
                return "A " + town.getId() + " " + thisTown.getWarlocks() / 2 + " " + thisTown.getCrusaders() / 2 + " "
                        + thisTown.getAmazons() / 2;
            }
        }
        return "W";
    }

    private String move() {
        int totalWarlocks = 0;
        int totalCrusaders = 0;
        int totalAmazons = 0;
        int totalCorsairs = 0;
        int totalBishops = 0;
        int totalNecromancers = 0;
        int totalArchitects = 0;
        for (Town town : myTowns){
            totalWarlocks += town.getWarlocks();
            totalCrusaders += town.getCrusaders();
            totalAmazons += town.getAmazons();
            totalCorsairs += town.getCorsairs();
            totalBishops += town.getBishops();
            totalNecromancers += town.getNecromancers();
            totalArchitects += town.getArchitects();
        }
        int averageWarlocks = totalWarlocks / myTowns.size();
        int averageCrusaders = totalCrusaders / myTowns.size();
        int averageAmazons = totalAmazons / myTowns.size();
        int averageCorsairs = totalCorsairs / myTowns.size();
        int averageBishops = totalBishops / myTowns.size();
        int averageNecromancers = totalNecromancers / myTowns.size();
        int averageArchitects = totalArchitects / myTowns.size();

        Town worstTown = null;
        int biggestDifference = Integer.MIN_VALUE;
        for (Town town : myTowns){
            int difference = 0;
            difference += town.getWarlocks() < averageWarlocks ? averageWarlocks - town.getWarlocks() : 0;
            difference += town.getCrusaders() < averageCrusaders ? averageCrusaders - town.getCrusaders() : 0;
            difference += town.getAmazons() < averageAmazons ? averageAmazons - town.getAmazons() : 0;
            difference += town.getCorsairs() < averageCorsairs ? averageCorsairs - town.getCorsairs() : 0;
            difference += town.getBishops() < averageBishops ? averageBishops - town.getBishops() : 0;
            difference += town.getNecromancers() < averageNecromancers ? averageNecromancers - town.getNecromancers() :
                    0;
            difference += town.getArchitects() < averageArchitects ? averageArchitects - town.getArchitects() : 0;
            if (difference > biggestDifference){
                worstTown = town;
                biggestDifference = difference;
            }
        }
        int neededWarlocks = worstTown.getWarlocks() < averageWarlocks ? averageWarlocks - worstTown.getWarlocks() : 0;
        int neededCrusaders = worstTown.getCrusaders() < averageCrusaders ? averageCrusaders - worstTown.getCrusaders()
                : 0;
        int neededAmazons = worstTown.getAmazons() < averageAmazons ? averageAmazons - worstTown.getAmazons() : 0;
        int neededCorsairs = worstTown.getCorsairs() < averageCorsairs ? averageCorsairs - worstTown.getCorsairs() : 0;
        int neededBishops = worstTown.getBishops() < averageBishops ? averageBishops - worstTown.getBishops() : 0;
        int neededNecromancers = worstTown.getNecromancers() < averageNecromancers ? averageNecromancers - worstTown.
                getNecromancers() : 0;
        int neededArchitects = worstTown.getArchitects() < averageArchitects ? averageArchitects - worstTown.
                getArchitects() : 0;
        return "M " + worstTown.getId() + " " + neededWarlocks + " " + neededCrusaders + " " + neededAmazons + " " +
                neededCorsairs + " " + neededBishops + " " + neededNecromancers + " " + neededArchitects;
    }

    private String resurrect() {
        return "R " + thisTown.getCorpses();
    }

    private String build() {
        if (thisTown.getGold() > 500){
            return "B P";
        }
        if (thisTown.getGold() > 200){
            return "B T";
        }
        return "W";
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
        public int neededGold(){
            return 2 * getUnits() - getSoldiers();
        }
        public int getFreeGold(){
            return gold - neededGold();
        }

    }

}