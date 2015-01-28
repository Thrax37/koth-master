import java.util.ArrayList;
import java.util.List;

public class TheLannisters {
    private static final int MIN_PEONS = 20;
    private static final int COST_PER_SOLDIER = 11;
    private static final int COST_PER_CORSAIR = 14;
    private static final int COST_PER_BISHOP = 22;
    private static final int BUILDING_COST = 200;
    private static final int COST_PER_CONVERTION = 50;
    private final int phase;
    private final int myId;
    private final int round;
    private final List<Town> myTowns = new ArrayList<>();
    private final List<Town> enemyTowns = new ArrayList<>();
    private final List<Town> enemyPlayerTowns = new ArrayList<>();
    private final List<Town> outlawTowns = new ArrayList<>();
    private Town thisTown = null;

    /*
     * Warlocks : Fighter (magic) 
     * Crusaders : Fighter (melee) 
     * Amazons : Fighter (range) 
     * Corsairs : Utility (steal, guard, transport) 
     * Bishops : Utility (convert, exorcize) 
     * Necromancers : Utility (resurrect) 
     * Architects : Utility (build) 
     * Peons : Resource (income, recruits)
     */

    public static void main(String[] args){
        if (args.length == 0) {
            System.out.println("12 12 12 13 3 5 5 38");
        } else {
            new TheLannisters(args[0].split(";")).command();
        }
    }

    public TheLannisters(String[] args) {
        round = Integer.parseInt(args[0]);
        phase = Integer.parseInt(args[1]);
        myId = Integer.parseInt(args[2]);

        int thisTownId = Integer.parseInt(args[3]);

        boolean hasAlreadyCommanded = false;
        for (int i = 4; i < args.length; i++){
            Town town = new Town(args[i], hasAlreadyCommanded);
            if (town.isMine()){
                myTowns.add(town);
                if (town.id == thisTownId){
                    thisTown = town;
                    hasAlreadyCommanded = true;
                }
            } else {
                enemyTowns.add(town);
                if (town.isOutlawTown()) {
                    outlawTowns.add(town);
                } else {
                    enemyPlayerTowns.add(town);
                }
            }
        }
    }

    private void command() {
        switch (phase) {
            case 2: steal(); break;
            case 3: recruit(); break;
            case 6: convert(); break;
            case 7: attack(); break;
            case 8: resurrect(); break;
            case 9: move(); break;
            case 11: build(); break;
        }
    }

    private void steal() {
        int goldToSteal = thisTown.corsairs * 10;

        // a player who can't steal it back => very weak next turns
        for (Town town : enemyPlayerTowns) {
            if (town.gold >= goldToSteal && town.hasAlreadyCommanded) {
                printCommand("S " + town.id + " " + thisTown.corsairs);
            }
        }

        // enemy player with most gold, so he won't get too strong
        int mostGold = -1;
        Town richestTown = null;
        for (Town town : enemyPlayerTowns) {
            if (town.gold > mostGold && town.gold >= goldToSteal) {
                richestTown = town;
                mostGold = town.gold;
            }
        }
        if (richestTown != null) {
            printCommand("S " + richestTown.id + " " + thisTown.corsairs);
        }

        // player with most gold (could be a bandit)
        mostGold = -1;
        for (Town town : enemyTowns) {
            if (town.gold > mostGold) {
                richestTown = town;
                mostGold = town.gold;
            }
        }
        printCommand("S " + richestTown.id + " " + thisTown.corsairs);
    }

    private void recruit() {
        if (round % 5 == 0 && !thisTown.isWeak()) { 
            printCommand("W");
        }

        int freePeons = -(MIN_PEONS - thisTown.peons);
        int freeGold = thisTown.gold - thisTown.getSoldiers() - thisTown.getCitizens() * 2 - 300;

        if (freePeons <= 0 || freeGold < COST_PER_SOLDIER) {
            printCommand("W");          
        }

        int bishops = 0;
        if (thisTown.bishops * 50 < thisTown.peons && freeGold >= COST_PER_BISHOP) {
            bishops = 1;
            freeGold -= COST_PER_BISHOP;
            freePeons--;
        }

        int corsairs = 0;
        if (thisTown.getsRobbed() && freeGold >= COST_PER_CORSAIR) {
            corsairs = 1;
            freeGold -= COST_PER_CORSAIR;
            freePeons--;
        }

        int producableSoldiers = Math.min(freePeons, freeGold / COST_PER_SOLDIER);
        int soldierPerType = producableSoldiers / 3;
        int crusaders = soldierPerType + (producableSoldiers % 3);
        printCommand("R " + soldierPerType + " " + crusaders + " " + soldierPerType + " " + corsairs + " " + bishops + " 0 0");
    }

    private void convert() {
        int freeGold = 0;
        if (thisTown.isWeak() || thisTown.gold > 600) {
            freeGold = thisTown.gold;
        } else if (thisTown.corpses <= 0) {
            freeGold = thisTown.gold - 2*BUILDING_COST;
        }
        if (freeGold < COST_PER_CONVERTION || thisTown.bishops == 0) {
            printCommand("W");
        }
        int soldiersToConvert = freeGold / COST_PER_CONVERTION;
        int soldiersPerType = soldiersToConvert / 3;
        int amazons = soldiersPerType + (soldiersToConvert % 3);

        Town destination = null;
        int mostSoldiers = 0;
        for (Town town : enemyPlayerTowns) {
            if (town.getSoldiers() > mostSoldiers && town.getSoldiers() >= soldiersToConvert) {
                destination = town;
                mostSoldiers = town.getSoldiers();
            }
        }
        if (destination == null) {
            for (Town town : enemyPlayerTowns) {
                if (town.getSoldiers() > mostSoldiers) {
                    destination = town;
                    mostSoldiers = town.getSoldiers();
                }
            }
        }

        if (destination == null) {
            printCommand("W");
        }
        printCommand("C " + destination.id + " " + soldiersPerType + " " + soldiersPerType + " " + amazons);
    }

    private void attack() {
        int leastSoldiers = Integer.MAX_VALUE;
        Town destination = null;
        boolean tooMuchSoldiers = thisTown.getSoldiers() > 1000;
        for (Town town : enemyTowns) {
            if (town.getSoldiers() < leastSoldiers && tooMuchSoldiers 
                    || (town.bishops > 0
                    && (town.peons > 10 || (town.necromancers > 0 && town.gold > 200)))) {
                destination = town;
                leastSoldiers = town.getSoldiers();
            }
        }
        if (destination == null) {
            printCommand("W");
        }

        boolean attackTogether = false;
        for (Town town : myTowns) {
            if (!town.hasAlreadyCommanded && !town.isWeak()) {
                attackTogether = true;
            }
        }

        if (round > 5 && (thisTown.getSoldiers() / 2 >= destination.getSoldiers() || attackTogether)) {
            double div = 2;
            while (!thisTown.willBeWeak(div) && div <= 3) {
                div += 0.1;
            }
            div = 1.5 + (3 - div) / 2; //strange...
            printCommand("A " + destination.id + " " + (int)(thisTown.warlocks / div) + " " + (int)(thisTown.crusaders / div) + " " + (int)(thisTown.amazons / div));
        } else {
            printCommand("W");
        }
    }

    private void resurrect() {
        if (thisTown.gold > BUILDING_COST && thisTown.corpses < 20 && thisTown.peons > 19) {
            int freeGold = thisTown.gold - BUILDING_COST;
            printCommand("R " + (freeGold / 20));
        }
        printCommand("R " + thisTown.corpses); //raise as many as possible      
    }

    private void move() {
        if (thisTown.isWeak() || myTowns.size() == 1) {
            printCommand("W");
        }
        int leastGold = thisTown.gold;
        Town destination = null;
        for (Town town : myTowns) {
            if (town.gold < leastGold) {
                leastGold = town.gold;
                destination = town;
            }
        }
        if (thisTown.hasMostGold() || (thisTown.gold > 300 && destination.gold - destination.getSoldiers() - destination.getCitizens() * 2 < 300)) {
            printCommand("T " + destination.id + " " + thisTown.gold / 4);
        }
        for (Town town : myTowns) {
            if (town.isWeak()) {
                printCommand("M " + town.id + " " + (thisTown.warlocks / 4) + " "  + (thisTown.crusaders / 4) + " "  + (thisTown.amazons / 4) + " 0 0 0 0");
            }
        }
        printCommand("W");
    }

    private void build() {
        printCommand("B B");        
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
        private final boolean hasAlreadyCommanded;

        public Town(String string, boolean hasAlreadyCommanded){
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
            this.hasAlreadyCommanded = hasAlreadyCommanded;
        }

        public int getSoldiers() {
            return warlocks + crusaders + amazons;
        }

        public int getCitizens() {
            return corsairs + bishops + necromancers + architects;
        }

        public boolean isMine(){
            return ownerId == myId;
        }

        public boolean isOutlawTown() {
            return ownerId == -1;
        }

        public boolean isWeak() {
            return willBeWeak(1);
        }

        public boolean willBeWeak(double divisor) {
            if (enemyPlayerTowns.size() == 0) {
                return false;
            }
            int totalSoldiers = 0;
            for (Town town : enemyPlayerTowns) {
                totalSoldiers += town.getSoldiers();
            }
            return getSoldiers() / divisor < totalSoldiers / enemyPlayerTowns.size();
        }

        public boolean getsRobbed() {
            int outlaws = 0;
            for (Town town : outlawTowns) {
                outlaws += town.getSoldiers() + town.getCitizens() + town.peons;
            }
            int notOutlawTowns = enemyTowns.size() + myTowns.size() - outlawTowns.size();
            int outlawsPerTown = outlaws / notOutlawTowns;
            return outlawsPerTown - corsairs * 5 > 0;
        }

        public boolean hasMostGold() {
            for (Town town : enemyTowns) {
                if (gold < town.gold) {
                    return false;
                }
            }
            return true;
        }
    }

    private void printCommand(String command) {
        System.out.println(command);
        System.exit(0);
    }
}