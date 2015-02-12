import java.util.ArrayList;
import java.util.List;

public class TheLannisters {
    private static final int COST_PER_SOLDIER = 11;
    private static final int COST_PER_CORSAIR = 14;
    private static final int COST_PER_BISHOP = 22;
    private static final int COST_PER_CONVERTION = 50;
    private final int MIN_PEONS;
    private final int phase;
    private final int myId;
    private final int round;
    private final List<Town> myTowns = new ArrayList<>();
    private final List<Town> enemyTowns = new ArrayList<>();
    private final List<Town> enemyPlayerTowns = new ArrayList<>();
    private final List<Town> outlawTowns = new ArrayList<>();
    private Town thisTown = null;

    public static void main(String[] args){
        if (args.length == 0) {
            System.out.println("12 14 10 11 3 5 5 40");
        } else {
            new TheLannisters(args[0].split(";")).command();
        }
    }

    public TheLannisters(String[] args) {
        round = Integer.parseInt(args[0]);
        phase = Integer.parseInt(args[1]);
        myId = Integer.parseInt(args[2]);
        MIN_PEONS = 40 + round/2;
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

        int mostSoldiers = -1;
        Town richestTown = null;
        for (Town town : enemyPlayerTowns) {
            if (town.getSoldiers() > mostSoldiers && town.gold >= goldToSteal) {
                richestTown = town;
                mostSoldiers = town.getSoldiers();
            }
        }
        if (richestTown != null) {
            printCommand("S " + richestTown.id + " " + thisTown.corsairs);
        }

        // player with most gold (could be an outlaw)
        int mostGold = -1;
        for (Town town : enemyTowns) {
            if (town.gold > mostGold) {
                richestTown = town;
                mostGold = town.gold;
            }
        }
        printCommand("S " + richestTown.id + " " + thisTown.corsairs);
    }

    private void recruit() {
        int freePeons = (thisTown.peons - MIN_PEONS);
        if (!thisTown.isWeak()) {
            freePeons /= 2;
        }
        int freeGold = thisTown.gold - thisTown.getSoldiers() - thisTown.getCitizens() * 2;

        if (freePeons <= 0 || freeGold < COST_PER_SOLDIER) {
            printCommand("W");
        }

        int bishops = 0;
        if ((thisTown.bishops * 50 < thisTown.peons || thisTown.gold > 500) && freeGold >= COST_PER_BISHOP) {
            bishops = 1;
            freeGold -= COST_PER_BISHOP;
            freePeons--;
        }

        int necromancers = 0;
        if (thisTown.necromancers == 0 && freeGold >= COST_PER_BISHOP) {
            necromancers = 1;
            freeGold -= COST_PER_BISHOP;
            freePeons--;
        }

        int corsairs = 0;
        if (thisTown.getsRobbed() && freeGold >= COST_PER_CORSAIR) {
            corsairs = 1;
            freeGold -= COST_PER_CORSAIR;
            freePeons--;
        }

        int architects = 0;
        if (thisTown.architects == 0 && freeGold > 500) {
            architects = 1;
            freeGold -= COST_PER_BISHOP;
            freePeons--;
        }

        int producableSoldiers = Math.min(freePeons, freeGold / COST_PER_SOLDIER);
        int soldierPerType = producableSoldiers / 3;
        int crusaders = soldierPerType + (producableSoldiers % 3);
        printCommand("R " + soldierPerType + " " + crusaders + " " + soldierPerType + " " + corsairs + " " + bishops + " " + necromancers + " " + architects);
    }

    private void convert() {
        int freeGold = 0;
        if (thisTown.isWeak() || thisTown.corpses <= 0) {
            freeGold = thisTown.gold - 300;
        }
        if (freeGold < COST_PER_CONVERTION || thisTown.bishops == 0 || (round < 2 && thisTown.getSoldiers() > 35)) {
            printCommand("W");
        }
        int soldiersToConvert = freeGold / COST_PER_CONVERTION;
        int soldiersPerType = soldiersToConvert / 3;
        int amazons = soldiersPerType + (soldiersToConvert % 3);

        Town destination = null;        
        int mostSoldiers = -1;
        if (destination == null) {
            for (Town town : enemyPlayerTowns) {
                if (town.getSoldiers() > mostSoldiers) {
                    destination = town;
                    mostSoldiers = town.getSoldiers();
                }
            }
        }
        if (destination == null) {
            destination = outlawTowns.get(0);
        }
        printCommand("C " + destination.id + " " + soldiersPerType + " " + soldiersPerType + " " + amazons);
    }

    private void attack() {
        int leastSoldiers = Integer.MAX_VALUE;
        Town destination = null;

        if (thisTown.isWeak() || round < 21) {
            printCommand("W");
        }

        for (Town town : enemyTowns) {
            if (town.getSoldiers() < leastSoldiers) {
                destination = town;
                leastSoldiers = town.getSoldiers();
            }
        }

        boolean attackTogether = false;
        for (Town town : myTowns) {
            if (!town.hasAlreadyCommanded && !town.isWeak() && !town.equals(thisTown)) {
                attackTogether = true;
            }
        }

        if (thisTown.getSoldiers() / 3.5 > destination.getSoldiers() || (attackTogether && thisTown.getSoldiers() / 2 >= destination.getSoldiers())) {
            int warlocks = thisTown.warlocks/2;
            int crusaders = thisTown.crusaders/2;
            int amazons = thisTown.amazons/2;
            while (warlocks + crusaders + amazons > (destination.getSoldiers() + 3) * 3) {
                warlocks = Math.max(0, --warlocks);
                crusaders = Math.max(0, --crusaders);
                amazons = Math.max(0, --amazons);
            }
            if (enemyPlayerTowns.size() == 0) {
                // dont send too many soldiers => otherwise they revolt
                printCommand("A " + destination.id + " " + (destination.warlocks + 2) + " " + (destination.crusaders + 2) + " " + (destination.amazons + 2));
            }
            printCommand("A " + destination.id + " " + warlocks + " " + crusaders + " " + amazons);
        } else {
            printCommand("W");
        }
    }

    private void resurrect() {
        printCommand("R 999");      
    }

    private void move() {
        if (myTowns.size() == 1 || thisTown.corpses > 10) {
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
        if (destination != null && (thisTown.hasMostGold() || (thisTown.gold > 100 && destination.gold - destination.getSoldiers() - destination.getCitizens() * 2 < 300) || (thisTown.gold > 300 && destination.getsRobbed()))) {
            printCommand("T " + destination.id + " " + Math.max(thisTown.gold / 10, 30));
        }
        for (Town town : myTowns) {
            if (town.isWeak()) {
                printCommand("M " + town.id + " " + (thisTown.warlocks / 4) + " "  + (thisTown.crusaders / 4) + " "  + (thisTown.amazons / 4) + " 0 0 0 0");
            }
        }
        printCommand("W");
    }

    private void build() {
        if (thisTown.gold >= 500) {
            printCommand("B P");
        } else if (thisTown.gold > 300) {
            if (round % 2 == 0) {
                printCommand("B E");
            }
            printCommand("B B");    
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
            int mySoldiers = (int) (getSoldiers() * divisor);
            int weakerTowns = 0;
            for (Town town : enemyTowns) {
                if (town.getSoldiers() < mySoldiers) {
                    weakerTowns++;
                }
            }
            for (Town town : myTowns) {
                if (town.getSoldiers() < mySoldiers) {
                    weakerTowns++;
                }
            }
            return weakerTowns <= 2;
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