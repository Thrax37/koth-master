import java.util.ArrayList;
import java.util.List;

public class Politician {

    private static final int SOLDIER_COST = 11;
    private static final int AMBASSADOR_COST = 22;
    private static final int SCIENTIST_COST = 22;
    private static final int TRAIN_COST = 14;
    private static final int ARCHITECT_COST = 17;
    private static final int CONVERSION_COST = 50;
    private static final double MIN_SOLDIER_RATIO = 1.0;
    private static final int MIN_PEOPLE = 30;
    private static final int MIN_ARCHITECTS = 13;
    private static final int REINFORCEMENT_ROUND = 5;
    private static final int MAX_PEOPLE = 100;
    int round;
    int phase;
    int playerID;
    int thisTownID;

    List<State> states;
    List<State> myStates;
    List<State> otherStates;

    State thisState;

    public Politician(String... args) {
        args = args[0].split(";");

        round = Integer.parseInt(args[0]);
        phase = Integer.parseInt(args[1]);
        playerID = Integer.parseInt(args[2]);
        thisTownID = Integer.parseInt(args[3]);

        states = new ArrayList<>();
        myStates = new ArrayList<>();
        otherStates = new ArrayList<>();

        for (int i = 4; i < args.length; i++){
            states.add(new State(args[i]));
        }


        for (State state : states){
            if (state.isMine()){
                myStates.add(state);
                if (state.isThisTown()){
                    thisState = state;
                }
            } else {
                otherStates.add(state);
            }
        }
    }

    public static void main(String[] args){
        if (args.length == 0) {
            System.out.println("12 12 12 10 4 1 13 36");
        } else {
            try {
                System.out.println(new Politician(args).conquer());
            } catch (Exception e){
                System.out.println("We, the enslaved people, declare Politician the winner.");
            }
        }
    }

    private String conquer() {

        switch (phase){
            case 2:
                return steal();
            case 3:
                return conscript();
            case 6:
                return bribe();
            case 7:
                return orderAttack();
            case 8:
                return topSecret();
            case 9:
                return handouts();
            case 11:
                return roadConstruction();
            default:
                throw new IllegalStateException();//Civil war!!!
        }
    }

    private String steal() {
        State bestState = otherStates.stream().max((a, b) -> a.profit() - b.profit()).get();
        return "S " + bestState.getId() + " " + thisState.getTrains();
    }

    private String conscript() {
        int gold = thisState.getFreeGold();

        int neededSoldiers = soldiersNeeded();
        neededSoldiers = Math.min(gold / SOLDIER_COST, neededSoldiers);
        int neededAmbassadors = Math.min(neededSoldiers, (gold - 200 - 10 * thisState.getCorpses()) / (CONVERSION_COST + AMBASSADOR_COST));
        neededAmbassadors = Math.min(maxSoldiers(), neededAmbassadors);
        neededSoldiers -= neededAmbassadors;
        thisState.regularPeople -= neededSoldiers;
        gold -= neededSoldiers * SOLDIER_COST;

        neededAmbassadors += (int) Math.max(Math.round(Math.ceil(thisState.getRegularPeople() / 50.0) -
                thisState.getAmbassadors() - neededAmbassadors), 0);
        neededAmbassadors = Math.min(gold / AMBASSADOR_COST, neededAmbassadors);
        thisState.regularPeople -= neededAmbassadors;
        gold -= neededAmbassadors * AMBASSADOR_COST;

        int neededScientists = (int) Math.max(Math.round(Math.ceil(thisState.getCorpses() / 10.0) -
                thisState.getScientists()), 0);
        neededScientists = Math.min(gold / SCIENTIST_COST, neededScientists);
        thisState.regularPeople -= neededScientists;
        gold -= neededScientists * SCIENTIST_COST;

        int neededTrains = 0;
        int neededArchitects = 0;

        if (thisState.getRegularPeople() > MIN_PEOPLE){
            int freePeople = thisState.getRegularPeople() - MIN_PEOPLE;

            neededTrains = Math.max(neededTrains() - thisState.getTrains(), 0);
            neededTrains = Math.min(neededTrains, freePeople);
            neededTrains = Math.min(neededTrains, gold / TRAIN_COST);
            freePeople -= neededTrains;
            gold -= neededTrains * TRAIN_COST;

            neededArchitects = Math.max(MIN_ARCHITECTS - thisState.getArchitects(), 0);
            neededArchitects = Math.min(neededArchitects, freePeople);
            neededArchitects = Math.min(neededArchitects, gold / ARCHITECT_COST);
            freePeople -= neededArchitects;
            gold -= neededArchitects * ARCHITECT_COST;

            if (freePeople + MIN_PEOPLE > MAX_PEOPLE){

                if (freePeople + MIN_PEOPLE > MAX_PEOPLE){
                    freePeople = freePeople + MIN_PEOPLE - MAX_PEOPLE;
                }

                neededAmbassadors += Math.min(gold / AMBASSADOR_COST, freePeople);
                neededAmbassadors = Math.min(neededAmbassadors, maxSoldiers());
                gold -= neededAmbassadors * AMBASSADOR_COST;
                freePeople -= neededAmbassadors;

                neededSoldiers += Math.min(gold / SOLDIER_COST, freePeople);
            }

        }

        int neededAirmen = neededSoldiers / 3;
        int neededSailors = neededSoldiers / 3;
        int neededPrivates = neededSoldiers / 3;


        return "R " + neededAirmen + " " + neededSailors + " " + neededPrivates + " " + neededTrains + " " +
                neededAmbassadors + " " + neededScientists + " " + neededArchitects;
    }

    private int neededTrains() {
        int totalOutlaws = 0;
        int totalStates = myStates.size();
        for (State state : otherStates){
            if (state.isOutlaw()){
                totalOutlaws += state.getTotalUnits();
            } else {
                totalStates++;
            }
        }
        return (int) Math.round(Math.ceil(.2 * totalOutlaws / totalStates));
    }

    private int soldiersNeeded(){
        int soldiers = (int) ((MIN_SOLDIER_RATIO * thisState.getRegularPeople() - thisState.getTotalSoldiers()) /
                        (MIN_SOLDIER_RATIO + 1));
        soldiers = Math.max(soldiers, minSoldiers() + 5 - thisState.getTotalSoldiers());
        soldiers = Math.max(soldiers, maxSoldiers() / 2 - thisState.getTotalSoldiers());
        soldiers = Math.max(soldiers, 0);
        return soldiers;
    }

    private String bribe() {
        int soldiersToConvert = Math.min(thisState.ambassadors, (thisState.getGold() - 200 - 10 *
                thisState.getCorpses()) / CONVERSION_COST);
        soldiersToConvert = Math.max(soldiersToConvert, 0);
        State toughest = otherStates.stream().max((a,b) -> a.getTotalSoldiers() - b.getTotalSoldiers()).get();
        soldiersToConvert = Math.min(soldiersToConvert, toughest.getTotalSoldiers());
        int airmen = (int) ((1.0 * toughest.getAirmen() / toughest.getTotalSoldiers()) * soldiersToConvert);
        soldiersToConvert -= airmen;
        int sailors = (int) ((1.0 * toughest.getSailors() / toughest.getTotalSoldiers()) * soldiersToConvert) * 3 / 2;
        soldiersToConvert -= sailors;
        int privates = soldiersToConvert;
        return "C " + toughest.getId() + " " + airmen + " " + sailors + " " + privates;
    }

    private String orderAttack() {
        int soldiers = thisState.getFreeSoldiers();
        otherStates.sort((a,b) -> b.getTotalSoldiers() - a.getTotalSoldiers());
        if (otherStates.get(0).getTotalSoldiers() < soldiers){//Attack!!!!
            int airmen = (int) (((thisState.getAirmen()*1.0) / thisState.getTotalSoldiers()) * soldiers);
            int sailors = (int) (((thisState.getSailors()*1.0) / thisState.getTotalSoldiers()) * soldiers);
            int privates = (int) (((thisState.getPrivates()*1.0) / thisState.getTotalSoldiers()) * soldiers);
            return "A " + otherStates.get(0).getId() + " " + airmen + " " + sailors + " " + privates;
        }
        return getWaitString();
    }

    private String handouts() {

        State poorestState = null;
        int goldOfPoorestTown = Integer.MAX_VALUE;

        for (State state : myStates){
            if (state.getGold() < goldOfPoorestTown){
                poorestState = state;
                goldOfPoorestTown = state.getGold();
            }
        }
        if (thisState.getGold() <= goldOfPoorestTown){
            return getWaitString();
        }
        return "T " + poorestState.getId() + " " + (thisState.getGold() - poorestState.getGold())  / 2;
    }

    private String topSecret() {
        return "R " + thisState.getCorpses();
    }

    private String roadConstruction() {
        int airportPriority = thisState.getAirmen() + thisState.getAmbassadors() + thisState.getScientists();
        int highwayPriority = thisState.getSailors() + thisState.getPrivates();
        int railroadPriority = thisState.getTrains() + thisState.getRegularPeople();
        int palacePriority = 5;//Why would we build a palace???
        if (airportPriority > highwayPriority && airportPriority > railroadPriority && airportPriority >
                palacePriority && thisState.getGold() > 200){
            return "B T";
        } else if (highwayPriority > railroadPriority && highwayPriority > palacePriority && thisState.getGold() > 200){
            return "B B";
        } else if (railroadPriority > palacePriority && thisState.getGold() > 200){
            return "B E";
        } else if (thisState.getGold() > 500){
            return "B P";
        } else {
            return getWaitString();
        }
    }

    private int maxSoldiers(){
        int max = 0;
        for (State state : otherStates){
            if (state.getTotalSoldiers() > max){
                max = state.getTotalSoldiers();
            }
        }
        return max;
    }

    private int minSoldiers(){
        int min = Integer.MAX_VALUE;
        for (State state : otherStates){
            if (state.getTotalSoldiers() < min){
                min = state.getTotalSoldiers();
            }
        }
        return min;
    }

    private class State {

        private final int ownerId;
        private final int id;
        private final int gold;
        private final int corpses;
        private final int airmen;
        private final int sailors;
        private final int privates;
        private final int trains;
        private final int ambassadors;
        private final int scientists;
        private final int architects;
        private int regularPeople;
        private final int airports;
        private final int highways;
        private final int railroads;
        private final int palaces;

        public State(String string){
            String[] args = string.split("_");
            ownerId = Integer.parseInt(args[0]);
            id = Integer.parseInt(args[1]);
            gold = Integer.parseInt(args[2]);
            corpses = Integer.parseInt(args[3]);
            airmen = Integer.parseInt(args[4]);
            sailors = Integer.parseInt(args[5]);
            privates = Integer.parseInt(args[6]);
            trains = Integer.parseInt(args[7]);
            ambassadors = Integer.parseInt(args[8]);
            scientists = Integer.parseInt(args[9]);
            architects = Integer.parseInt(args[10]);
            regularPeople = Integer.parseInt(args[11]);
            airports = Integer.parseInt(args[12]);
            highways = Integer.parseInt(args[13]);
            railroads = Integer.parseInt(args[14]);
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
        public int getAirmen() {
            return airmen;
        }
        public int getSailors() {
            return sailors;
        }
        public int getPrivates() {
            return privates;
        }
        public int getTrains() {
            return trains;
        }
        public int getAmbassadors() {
            return ambassadors;
        }
        public int getScientists() {
            return scientists;
        }
        public int getArchitects() {
            return architects;
        }
        public int getRegularPeople() {
            return regularPeople;
        }
        public int getAirports() {
            return airports;
        }
        public int getHighways() {
            return highways;
        }
        public int getRailroads() {
            return railroads;
        }
        public int getPalaces() {
            return palaces;
        }
        public int getTotalBuildings() {
            return getAirports() + getHighways() + getRailroads() + getPalaces();
        }
        public int getTotalSoldiers() {
            return getAirmen() + getSailors() + getPrivates();
        }
        public int getTotalUnits() {
            return getTotalSoldiers() + getTrains() + getAmbassadors() + getScientists() + getArchitects();
        }
        public int getTotalCitizens() {
            return getTotalUnits() + getRegularPeople();
        }
        public boolean isMine(){
            return getOwnerId() == playerID;
        }
        public boolean isThisTown(){
            return id == thisTownID;
        }
        public int neededGold(){
            return 2 * getTotalUnits() - getTotalSoldiers();
        }
        public int getFreeGold(){
            return gold - neededGold();
        }

        public int getFreeSoldiers() {
            int soldiers = Math.max((int) (getTotalSoldiers() - regularPeople * MIN_SOLDIER_RATIO), 0);
            soldiers = Math.min(soldiers, getTotalSoldiers() - minSoldiers() - 5);
            soldiers = Math.min(soldiers, getTotalSoldiers() - maxSoldiers() / 2);
            soldiers = Math.max(soldiers, 0);
            return soldiers;
        }

        public boolean isOutlaw() {
            return ownerId == -1;
        }

        public int income() {
            return 5 * regularPeople + 10 * trains + 2 * (airmen + ambassadors + scientists) * airports + 2 * (sailors +
                    privates) * highways + 2 * (trains + regularPeople) * railroads + 10 * palaces;
        }

        public int wages() {
            return airmen + sailors + privates + 2 * (trains + ambassadors + scientists + architects);
        }

        public int profit() {
            return income() - wages();
        }
    }

    private static String getWaitString(){
        switch ((int) (Math.random() * 7)){
            case 0:
                return "We will win!";
            case 1:
                return "Winny the Pooh, Winny the Pooh";
            case 2:
                return "Who washed Washington's white woolen underwear when Washington's laundry woman, went west?";
            case 3:
                return "What's up doc?";
            case 4:
                return "Why is this taking so long?";
            case 5:
                return "Wool is better than cotton.";
            case 6:
                return "Will I win?";
            case 7:
                return "What's your favorite color?";
            default:
                return "What! This message should not be being printed.";
        }
    }

}