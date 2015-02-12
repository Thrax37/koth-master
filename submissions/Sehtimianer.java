import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class Sehtimianer {
private static final int GOLD_MAX_DEBT = 200;
private static final int BIRTH_ROUND = 5;
private static final int ZOMBIE_WAKING_CHANCE = 10;
private static final int NECRO_RAISE_CAPACITY = 5;
private static final int DEMON_SUMMON_CHANCE = 10;
private static final int BISHOP_PRAYER_CAPACITY = 50;
private static final int CORSAIR_SURVEILLANCE_RATIO = 5;

public static final int GOLD_PER_WARLOCK = -1;
public static final int GOLD_PER_CRUSADER = -1;
public static final int GOLD_PER_AMAZON = -1;
public static final int GOLD_PER_CORSAIR = -2;
public static final int GOLD_PER_BISHOP = -2;
public static final int GOLD_PER_NECRO = -2;
public static final int GOLD_PER_ARCHITECT = -2;
public static final int GOLD_PER_PEON = 5;

public static final int GOLD_PER_RESURRECTION = 20;
public static final int GOLD_PER_CONVERSION = 50;
public static final int GOLD_PER_STEAL = 10;

public static final int GOLD_RECRUIT_WARLOCK = 10;
public static final int GOLD_RECRUIT_CRUSADER = 10;
public static final int GOLD_RECRUIT_AMAZON = 10;
public static final int GOLD_RECRUIT_CORSAIR = 12;
public static final int GOLD_RECRUIT_BISHOP = 20;
public static final int GOLD_RECRUIT_NECRO = 20;
public static final int GOLD_RECRUIT_ARCHITECT = 15;
public static final int GOLD_RECRUIT_DEFAULT = 20;

public static final int GOLD_PER_TEMPLE = 2;
public static final int GOLD_PER_BARRACKS = 2;
public static final int GOLD_PER_ESTATE = 2;
public static final int GOLD_PER_PALACE = 10;

public static final int GOLD_COST_BUILDING = 200;

public static final int COMPLETION_PER_ARCHITECT = 8;
public static final int COMPLETION_NEEDED = 100;
public static final int ARCHITECTS = (int) Math.ceil(1.0 * COMPLETION_NEEDED / COMPLETION_PER_ARCHITECT);

int round;
int phase;
int playerID;
int thisTownID;

List<Town> towns;
List<Town> myTowns;
List<Town> playerTowns;
List<Town> outlawTowns;

Town thisTown;

public static void main(String[] args) {
    if (args.length == 0) {
        System.out.println("9 9 9 24 5 2 13 29");
    } else {
        new Sehtimianer().actions(args[0].split(";"));
    }
}

private void actions(String[] args) {

    round = Integer.parseInt(args[0]);
    phase = Integer.parseInt(args[1]);
    playerID = Integer.parseInt(args[2]);
    thisTownID = Integer.parseInt(args[3]);

    towns = new ArrayList<Town>();
    myTowns = new ArrayList<Town>();
    playerTowns = new ArrayList<Town>();
    outlawTowns = new ArrayList<Town>();

    for (int i = 4; i < args.length; i++) {
        towns.add(new Town(args[i]));
    }

    for (Town town : towns) {
        if (town.isMine()) {
            myTowns.add(town);
            if (town.isThisTown()) {
                thisTown = town;
            }
        } else {
            if (town.getOwnerId() == -1) {
                outlawTowns.add(town);
            } else {
                playerTowns.add(town);
            }
        }
    }
    if (outlawTowns.size() == 0 && playerTowns.size() == 0) {
        System.out.print("WIN : D");
        return;
    }

    if (phase == 2) {
        steal();
    } else if (phase == 3) {
        recruit();
    } else if (phase == 6) {
        convert();
    } else if (phase == 7) {
        attack();
    } else if (phase == 8) {
        resurrect();
    } else if (phase == 9) {
        move();
    } else if (phase == 11) {
        build();
    }
}

private List<Town> calcStrongestPlayers() {
    if (playerTowns.size() == 0) {
        return outlawTowns;
    }

    Map<Integer, Integer> playerTownCount = new HashMap<Integer, Integer>();
    Integer count;
    int maxCount = 0;
    for (Town town : playerTowns) {
        count = playerTownCount.get(town.getOwnerId());
        if (count == null) {
            count = Integer.valueOf(1);
        } else {
            count = Integer.valueOf(count + 1);
        }
        playerTownCount.put(town.getOwnerId(), count);

        if (count > maxCount) {
            maxCount = count;
        }
    }
    Set<Integer> strongestPlayers = new HashSet<Integer>();
    for (Entry<Integer, Integer> entry : playerTownCount.entrySet()) {
        if (entry.getValue() == maxCount) {
            strongestPlayers.add(entry.getKey());
        }
    }

    return playerTowns.stream().filter(a -> strongestPlayers.contains(a.getOwnerId())).collect(Collectors.toList());
}

private void steal() {
    // S destinationId corsairs
    List<Town> afterFilter = calcStrongestPlayers().stream().filter(a -> a.getGold() > 0).collect(Collectors.toList());
    if (afterFilter.size() == 0) {
        afterFilter = calcStrongestPlayers();
    }
    Town poorestTown = afterFilter.stream().min((a, b) -> a.gold - b.gold).get();
    System.out.println("S " + poorestTown.getId() + " " + thisTown.getCorsairs());
}

private boolean willAttackSoon() {
    Town strongestTown;
    if (playerTowns.size() > 0) {
        strongestTown = playerTowns.stream().max((a, b) -> a.getSoldiers() - b.getSoldiers()).get();
        if ((thisTown.getSoldiers() + thisTown.getBishops()) / 3 <= strongestTown.getSoldiers()) {
            return false;
        }
    }

    strongestTown = calcStrongestPlayers().stream().max((a, b) -> a.getSoldiers() - b.getSoldiers()).get();

    int deltaWarlocks = thisTown.getWarlocks() - (strongestTown.getWarlocks() + strongestTown.getAmazons());
    int deltaCrusaders = thisTown.getCrusaders() - (strongestTown.getCrusaders() + strongestTown.getWarlocks());
    int deltaAmazons = thisTown.getAmazons() - (strongestTown.getAmazons() + strongestTown.getCrusaders());

    return (deltaWarlocks + deltaCrusaders + deltaAmazons) > 0;
}

private void recruit() {
    // R warlocks crusaders amazons corsairs bishops necros architects
    int peonsAvailable = Math.max(0, thisTown.getPeons() - round);

    int corsairNeed = calcCorsairsNeeded(thisTown);
    int goldForNecros = Math.min(thisTown.getCorpses(), thisTown.getNecros() * NECRO_RAISE_CAPACITY) * GOLD_PER_RESURRECTION;
    int goldAvailable = thisTown.getGold() - thisTown.getBishops() * GOLD_PER_CONVERSION - goldForNecros - GOLD_COST_BUILDING + calcWages(thisTown)
            - (calcCashflow(thisTown) < GOLD_MAX_DEBT ? (corsairNeed * CORSAIR_SURVEILLANCE_RATIO * GOLD_PER_STEAL) : 0);
    int corsairGoldAvailable = thisTown.getGold() + calcWages(thisTown) - goldForNecros;
    boolean onlyCorsair = false;
    if (goldAvailable < 0) {
        if (corsairGoldAvailable > 0) {
            onlyCorsair = true;
            goldAvailable = corsairGoldAvailable;
        } else {
            System.out.println("W");
            return;
        }
    }

    int necroNeed = Math.max(0, (int) Math.ceil(1.0 * thisTown.getCorpses() / NECRO_RAISE_CAPACITY) - thisTown.getNecros());
    necroNeed = Math.max(necroNeed, Math.min(peonsAvailable, calcOtherNecrosNeeded() / 3));
    int architectNeed = Math.max(0, ARCHITECTS - thisTown.getArchitects());

    int necros = 0;
    if (!onlyCorsair) {
        int necroCost = GOLD_RECRUIT_NECRO + GOLD_PER_RESURRECTION * NECRO_RAISE_CAPACITY - GOLD_PER_NECRO;
        necros = Math.min(necroNeed, goldAvailable / necroCost);
        goldAvailable -= necros * necroCost;
        peonsAvailable -= necros;
    }

    if (willAttackSoon()) {
        corsairNeed = calcCorsairSpread(thisTown) * 2 - thisTown.getCorsairs();
    }
    corsairNeed = (int) Math.max(corsairNeed, calcOtherCorsairsNeeded());

    int corsairCost = GOLD_RECRUIT_CORSAIR - GOLD_PER_CORSAIR;
    int corsairs = Math.min(corsairNeed, peonsAvailable);
    corsairs = Math.min(corsairs, goldAvailable / corsairCost);
    goldAvailable -= corsairs * corsairCost;
    peonsAvailable -= corsairs;

    int architects = 0;
    if (!onlyCorsair) {
        int architectCost = GOLD_RECRUIT_ARCHITECT - GOLD_PER_ARCHITECT;
        architects = Math.min(architectNeed, peonsAvailable);
        architects = Math.min(architects, goldAvailable / architectCost);
        goldAvailable -= architects * architectCost;
        peonsAvailable -= architects;
    }

    int bishops = 0;
    if (!onlyCorsair) {
        int peonParts;
        if (round <= 50) {
            peonParts = 20;
        } else if (round <= 70) {
            peonParts = 15;
        } else if (round <= 90) {
            peonParts = 10;
        } else if (round <= 95) {
            peonParts = 5;
        } else {
            peonParts = 3;
        }
        int peonsLeft = Math.max(0, thisTown.getPeons() - (necros + corsairs + architects));
        bishops = (int) Math.min(Math.min(peonsAvailable, peonsLeft / peonParts), goldAvailable / (GOLD_RECRUIT_BISHOP + GOLD_PER_CONVERSION - GOLD_PER_BISHOP));
    }

    if (corsairs > 0 || bishops > 0 || necros > 0 || architects > 0) {
        System.out.println("R 0 0 0 " + corsairs + " " + bishops + " " + necros + " " + architects);
    } else {
        System.out.println("W");
    }
}

private int calcCashflow(Town town) {
    int taxes = (town.getSurvivingPeons() * GOLD_PER_PEON);
    taxes += ((town.getWarlocks() + town.getBishops() + town.getNecros()) * (town.getTemple() * GOLD_PER_TEMPLE));
    taxes += ((town.getCrusaders() + town.getAmazons()) * (town.getBarracks() * GOLD_PER_BARRACKS));
    taxes += ((town.getSurvivingPeons() + town.getCorsairs()) * (town.getEstate() * GOLD_PER_ESTATE));
    taxes += (town.getPalace() * GOLD_PER_PALACE);

    int wages = calcWages(town);

    return taxes + wages;
}

private int calcWages(Town town) {
    int wages = (town.getWarlocks() * GOLD_PER_WARLOCK);
    wages += (town.getCrusaders() * GOLD_PER_CRUSADER);
    wages += (town.getAmazons() * GOLD_PER_AMAZON);
    wages += (town.getCorsairs() * GOLD_PER_CORSAIR);
    wages += (town.getBishops() * GOLD_PER_BISHOP);
    wages += (town.getNecros() * GOLD_PER_NECRO);
    wages += (town.getArchitects() * GOLD_PER_ARCHITECT);
    return wages;
}

private int calcCorsairSpread(Town calcTown) {
    int outlaws = 0;
    for (Town town : outlawTowns) {
        outlaws += town.getPopulation();
    }

    return (int) Math.ceil(1.0 * Math.floorDiv(outlaws, (playerTowns.size() + myTowns.size())) / CORSAIR_SURVEILLANCE_RATIO);
}

private int calcCorsairsNeeded(Town town) {
    return Math.max(0, calcCorsairSpread(town) - town.getCorsairs());
}

private int calcFreeCorsairs(Town town) {
    return Math.max(0, town.getCorsairs() - calcCorsairSpread(town));
}

private int calcOtherNecrosNeeded() {
    int necrosNeed = 0;
    for (Town town : myTowns) {
        if (town == thisTown) {
            continue;
        }
        necrosNeed += Math.max(0, (int) Math.ceil(1.0 * town.getCorpses() / NECRO_RAISE_CAPACITY) - town.getNecros());
    }
    int necrosAvailable = Math.max(0, thisTown.getNecros() - (int) Math.ceil(1.0 * thisTown.getCorpses() / NECRO_RAISE_CAPACITY));
    return Math.max(0, necrosNeed - necrosAvailable);
}

private int calcOtherCorsairsNeeded() {
    int corsairsNeed = 0;
    for (Town town : myTowns) {
        if (town == thisTown) {
            continue;
        }
        corsairsNeed += calcCorsairsNeeded(town);
    }
    return Math.max(0, corsairsNeed - calcFreeCorsairs(thisTown));
}

private void convert() {
    // C destinationId warlocks crusaders amazons
    final int MIN_CONVERT_PERCENTAGE = 10;

    int goldAvailable = thisTown.getGold() - thisTown.getCorpses() * GOLD_PER_RESURRECTION - GOLD_COST_BUILDING
            - (calcCashflow(thisTown) < GOLD_MAX_DEBT ? (calcCorsairsNeeded(thisTown) * CORSAIR_SURVEILLANCE_RATIO * GOLD_PER_STEAL) : 0);
    if (goldAvailable < 0) {
        System.out.println("W");
        return;
    }

    final int canConvert = Math.min(thisTown.getBishops(), goldAvailable / GOLD_PER_CONVERSION);
    if (canConvert == 0) {
        System.out.println("W");
        return;
    }

    List<Town> useTowns = calcStrongestPlayers();

    List<Town> afterFilter = useTowns.stream().filter(a -> a.getSoldiers() > canConvert / MIN_CONVERT_PERCENTAGE).collect(Collectors.toList());
    if (afterFilter.size() == 0) {
        if (playerTowns.size() > 0) {
            useTowns = playerTowns;
        } else {
            useTowns = outlawTowns;
        }
    }
    afterFilter = useTowns.stream().filter(a -> a.getSoldiers() > canConvert).collect(Collectors.toList());
    float getNear = 1.0f;
    while (afterFilter.size() == 0) {
        getNear -= 0.1f;
        final float toLower = getNear;
        afterFilter = useTowns.stream().filter(a -> a.getSoldiers() > canConvert * toLower).collect(Collectors.toList());
    }
    Town smallestTown = afterFilter.stream().min((a, b) -> a.getSoldiers() - b.getSoldiers()).get();

    Town convertTown;
    if (smallestTown.getSoldiers() < canConvert / MIN_CONVERT_PERCENTAGE) {
        convertTown = useTowns.stream().max((a, b) -> a.getSoldiers() - b.getSoldiers()).get();
    } else {
        convertTown = smallestTown;
    }

    int leftToConvert = canConvert;
    int warlocks = Math.min(leftToConvert, convertTown.getWarlocks());
    leftToConvert -= warlocks;
    int crusaders = Math.min(leftToConvert, convertTown.getCrusaders());
    leftToConvert -= crusaders;
    int amazons = Math.min(leftToConvert, convertTown.getAmazons());
    leftToConvert -= amazons;

    System.out.println("C " + convertTown.getId() + " " + warlocks + " " + crusaders + " " + amazons);
}

private void attack() {
    // A destinationId warlocks crusaders amazons

    Town strongestTown;
    if (playerTowns.size() > 0) {
        strongestTown = playerTowns.stream().max((a, b) -> a.getSoldiers() - b.getSoldiers()).get();
        if (thisTown.getSoldiers() / 3 <= strongestTown.getSoldiers()) {
            System.out.println("W");
            return;
        }
    }

    strongestTown = calcStrongestPlayers().stream().max((a, b) -> a.getSoldiers() - b.getSoldiers()).get();

    int warlockNeed = strongestTown.getWarlocks() + strongestTown.getAmazons();
    int crusaderNeed = strongestTown.getCrusaders() + strongestTown.getWarlocks();
    int amazonNeed = strongestTown.getAmazons() + strongestTown.getCrusaders();
    int deltaWarlocks = thisTown.getWarlocks() - warlockNeed;
    int deltaCrusaders = thisTown.getCrusaders() - crusaderNeed;
    int deltaAmazons = thisTown.getAmazons() - amazonNeed;

    if ((deltaWarlocks + deltaCrusaders + deltaAmazons) > 0) {
        // calc Needed Troops
        int warlocks = Math.min(thisTown.getWarlocks(), warlockNeed);
        int crusaders = Math.min(thisTown.getCrusaders(), crusaderNeed);
        int amazons = Math.min(thisTown.getAmazons(), amazonNeed);
        if ((warlocks + crusaders + amazons) == 0) {
            warlocks = 1;
            crusaders = 1;
            amazons = 1;
        } else {
            while (deltaWarlocks < 0 || deltaCrusaders < 0 || deltaAmazons < 0) {
                if (deltaWarlocks < 0) {
                    deltaCrusaders += deltaWarlocks;
                    crusaders -= deltaWarlocks;
                    deltaWarlocks = 0;
                }
                if (deltaCrusaders < 0) {
                    deltaAmazons += deltaCrusaders;
                    amazons -= deltaCrusaders;
                    deltaCrusaders = 0;
                }
                if (deltaAmazons < 0) {
                    deltaWarlocks += deltaAmazons;
                    warlocks -= deltaAmazons;
                    deltaAmazons = 0;
                }
            }
        }
        System.out.println("A " + strongestTown.getId() + " " + warlocks + " " + crusaders + " " + amazons);
    } else {
        System.out.println("W");
    }
}

private void resurrect() {
    // R corpses
    int goldAvailable = thisTown.getGold();
    if (goldAvailable < 0) {
        System.out.println("W");
        return;
    }

    int raise = Math.min(thisTown.getCorpses(), goldAvailable / GOLD_PER_RESURRECTION);
    if (raise > 0) {
        System.out.println("R " + raise);
    } else {
        System.out.println("W");
    }
}

private void move() {
    if (myTowns.size() == 1) {
        System.out.println("W");
        return;
    }

    // M destinationId warlocks crusaders amazons corsairs bishops necros architects
    // T DestinationId Gold

    int thisStolenGold = calcCorsairsNeeded(thisTown) * CORSAIR_SURVEILLANCE_RATIO * GOLD_PER_STEAL;
    int thisGoldAvailable = Math.max(-GOLD_MAX_DEBT, thisTown.getGold() - thisStolenGold);
    int thisCashFlow = calcCashflow(thisTown);

    if (thisGoldAvailable + thisCashFlow <= 0) {
        // Give up the town
        Town sendToTown = myTowns.stream().filter(a -> a.getId() != thisTown.getId()).max((a, b) -> a.getSoldiers() - b.getSoldiers()).get();
        System.out.print("M " + sendToTown.getId() + " " + thisTown.getWarlocks() + " " + thisTown.getCrusaders() + " " + thisTown.getAmazons() + " " + thisTown.getCorsairs() + " "
                + thisTown.getBishops() + " " + thisTown.getNecros() + " " + thisTown.getArchitects());
        return;
    }
    thisGoldAvailable += thisCashFlow;

    int thisCostOfBishop = thisTown.getTemple() * GOLD_PER_TEMPLE + GOLD_PER_BISHOP;
    int thisCostOfNecro = thisTown.getTemple() * GOLD_PER_TEMPLE + GOLD_PER_NECRO;
    int thisCostOfWarlock = thisTown.getTemple() * GOLD_PER_TEMPLE + GOLD_PER_WARLOCK;
    int thisCostOfCrusader = thisTown.getBarracks() * GOLD_PER_BARRACKS + GOLD_PER_CRUSADER;
    int thisCostOfAmazon = thisTown.getBarracks() * GOLD_PER_BARRACKS + GOLD_PER_AMAZON;
    int thisCostOfCorsair = thisTown.getEstate() * GOLD_PER_ESTATE + GOLD_PER_CORSAIR;

    int thisCorsairsAvailable = calcFreeCorsairs(thisTown);
    int thisNecrosAvailable = Math.max(0, thisTown.getNecros() - (int) Math.ceil(1.0 * thisTown.getCorpses() / NECRO_RAISE_CAPACITY));
    int thisBishopsAvailable = Math.max(0, thisTown.getBishops() - (int) Math.ceil(1.0 * thisTown.getPeons() / BISHOP_PRAYER_CAPACITY));
    int thisArchitectsAvailable = Math.max(0, thisTown.getArchitects() - ARCHITECTS);

    int strongestTownSoldiers = 0;
    if (playerTowns.size() > 0) {
        Town strongestTown = playerTowns.stream().max((a, b) -> a.getSoldiers() - b.getSoldiers()).get();
        strongestTownSoldiers = strongestTown.getSoldiers();
    }
    int thisSoldiersAvailable = Math.max(0, thisTown.getSoldiers() - strongestTownSoldiers);
    int thisWarlocksAvailable = thisSoldiersAvailable / 3;
    thisSoldiersAvailable -= thisWarlocksAvailable;
    int thisCrusadersAvailable = thisSoldiersAvailable / 2;
    thisSoldiersAvailable -= thisCrusadersAvailable;
    int thisAmazonsAvailable = thisSoldiersAvailable;
    int warlockDelta = thisWarlocksAvailable - thisTown.getWarlocks();
    int crusaderDelta = thisCrusadersAvailable - thisTown.getCrusaders();
    int amazonDelta = thisAmazonsAvailable - thisTown.getAmazons();
    while (warlockDelta > 0 || crusaderDelta > 0 || amazonDelta > 0) {
        if (warlockDelta > 0) {
            thisWarlocksAvailable -= warlockDelta;
            thisCrusadersAvailable += warlockDelta;
            crusaderDelta += warlockDelta;
            warlockDelta = 0;
        }
        if (crusaderDelta > 0) {
            thisCrusadersAvailable -= crusaderDelta;
            thisAmazonsAvailable += crusaderDelta;
            amazonDelta += crusaderDelta;
            crusaderDelta = 0;
        }
        if (amazonDelta > 0) {
            thisAmazonsAvailable -= amazonDelta;
            thisWarlocksAvailable += amazonDelta;
            warlockDelta += amazonDelta;
            amazonDelta = 0;
        }
    }

    // Calc loosing income for each x the town sends
    int tempThisGoldAvailable = thisGoldAvailable;
    if (thisCostOfCorsair > 0) {
        thisCorsairsAvailable = Math.min(thisCorsairsAvailable, tempThisGoldAvailable / thisCostOfCorsair);
        tempThisGoldAvailable -= thisCorsairsAvailable * thisCostOfCorsair;
    } // else dont plus the gold - would get too complicated
    if (thisCostOfWarlock > 0) {
        thisWarlocksAvailable = Math.min(thisWarlocksAvailable, tempThisGoldAvailable / thisCostOfWarlock);
        tempThisGoldAvailable -= thisWarlocksAvailable * thisCostOfWarlock;
    }
    if (thisCostOfCrusader > 0) {
        thisCrusadersAvailable = Math.min(thisCrusadersAvailable, tempThisGoldAvailable / thisCostOfCrusader);
        tempThisGoldAvailable -= thisCrusadersAvailable * thisCostOfCrusader;
    }
    if (thisCostOfAmazon > 0) {
        thisAmazonsAvailable = Math.min(thisAmazonsAvailable, tempThisGoldAvailable / thisCostOfAmazon);
        tempThisGoldAvailable -= thisAmazonsAvailable * thisCostOfAmazon;
    }
    if (thisCostOfNecro > 0) {
        thisNecrosAvailable = Math.min(thisNecrosAvailable, tempThisGoldAvailable / thisCostOfNecro);
        tempThisGoldAvailable -= thisNecrosAvailable * thisCostOfNecro;
    }
    if (thisCostOfBishop > 0) {
        thisBishopsAvailable = Math.min(thisBishopsAvailable, tempThisGoldAvailable / thisCostOfBishop);
        tempThisGoldAvailable -= thisBishopsAvailable * thisCostOfBishop;
    }

    boolean checkGoldNeed;
    boolean needsGold;
    for (int i = 0; i < 2; i++) {
        checkGoldNeed = i == 0;
        for (Town town : myTowns) {
            if (town == thisTown) {
                continue;
            }
            needsGold = false;

            int costOfBishop = town.getTemple() * GOLD_PER_TEMPLE + GOLD_PER_BISHOP;
            int costOfNecro = town.getTemple() * GOLD_PER_TEMPLE + GOLD_PER_NECRO;
            int costOfWarlock = town.getTemple() * GOLD_PER_TEMPLE + GOLD_PER_WARLOCK;
            int costOfCrusader = town.getBarracks() * GOLD_PER_BARRACKS + GOLD_PER_CRUSADER;
            int costOfAmazon = town.getBarracks() * GOLD_PER_BARRACKS + GOLD_PER_AMAZON;
            int costOfCorsair = town.getEstate() * GOLD_PER_ESTATE + GOLD_PER_CORSAIR;

            int corsairsNeed = calcCorsairsNeeded(town);
            int sendCorsairs = Math.min(corsairsNeed, thisCorsairsAvailable);
            int goldStolen = corsairsNeed * CORSAIR_SURVEILLANCE_RATIO * GOLD_PER_STEAL;
            int goldAvailable = town.getGold() - goldStolen;
            int goldAvailableWithCorsairs = Math.max(-GOLD_MAX_DEBT, goldAvailable + sendCorsairs * CORSAIR_SURVEILLANCE_RATIO * GOLD_PER_STEAL);
            goldAvailable = Math.max(-GOLD_MAX_DEBT, goldAvailable);
            int cashflow = calcCashflow(town);
            if (goldAvailable + cashflow < 0) {
                needsGold = true;
            }
            tryToSwim: if (goldAvailableWithCorsairs < 0) {
                int potentialCashflow = cashflow + sendCorsairs * costOfCorsair;
                if (goldAvailableWithCorsairs + potentialCashflow >= 0) {
                    break tryToSwim;
                } else {
                    int potentialGoldAvailable = goldAvailableWithCorsairs + potentialCashflow;
                    if (costOfWarlock > 0) {
                        potentialGoldAvailable += thisWarlocksAvailable * costOfWarlock;
                    }
                    if (costOfCrusader > 0) {
                        potentialGoldAvailable += thisCrusadersAvailable * costOfCrusader;
                    }
                    if (costOfAmazon > 0) {
                        potentialGoldAvailable += thisAmazonsAvailable * costOfAmazon;
                    }
                    if (costOfNecro > 0) {
                        potentialGoldAvailable += thisNecrosAvailable * costOfNecro;
                    }
                    if (costOfBishop > 0) {
                        potentialGoldAvailable += thisBishopsAvailable * costOfBishop;
                    }
                    if (potentialGoldAvailable >= 0) {
                        System.out.print("M " + town.getId() + " " + thisWarlocksAvailable + " " + thisCrusadersAvailable + " " + thisAmazonsAvailable + " " + sendCorsairs + " "
                                + thisBishopsAvailable + " " + thisNecrosAvailable + " 0");
                        return;
                    }
                }

                // Last hope, check if enough gold can rescue the town from revolt
                int goldNeeded = -(town.getGold() - goldStolen + cashflow);
                if (thisGoldAvailable >= goldNeeded) {
                    System.out.println("T " + town.getId() + " " + goldNeeded);
                    return;
                }
                System.out.println("W");
                continue;
            }

            if (checkGoldNeed && !needsGold) {
                continue;
            }

            goldAvailable = goldAvailableWithCorsairs;
            goldAvailable += cashflow + sendCorsairs * costOfCorsair;
            int sendNecros = 0;
            int sendBishops = 0;
            int sendWarlocks = 0;
            int sendCrusaders = 0;
            int sendAmazons = 0;
            int sendArchitects = 0;

            int soldiersNeed = Math.max(0, strongestTownSoldiers - town.getSoldiers());
            int lastNeededSoldiers = 0;
            while (soldiersNeed > 0 && (thisWarlocksAvailable > 0 || thisCrusadersAvailable > 0 || thisAmazonsAvailable > 0) && soldiersNeed != lastNeededSoldiers) {
                lastNeededSoldiers = soldiersNeed;
                if (thisWarlocksAvailable > 0) {
                    goldAvailable += costOfWarlock;
                    if (goldAvailable < 0) {
                        goldAvailable -= costOfWarlock;
                    } else {
                        soldiersNeed--;
                        thisWarlocksAvailable--;
                        sendWarlocks++;
                    }
                }
                if (thisCrusadersAvailable > 0) {
                    goldAvailable += costOfCrusader;
                    if (goldAvailable < 0) {
                        goldAvailable -= costOfCrusader;
                    } else {
                        soldiersNeed--;
                        thisCrusadersAvailable--;
                        sendCrusaders++;
                    }
                }
                if (thisAmazonsAvailable > 0) {
                    goldAvailable += costOfAmazon;
                    if (goldAvailable < 0) {
                        goldAvailable -= costOfAmazon;
                    } else {
                        soldiersNeed--;
                        thisAmazonsAvailable--;
                        sendAmazons++;
                    }
                }
            }

            int necrosNeed = Math.max(0, (int) Math.ceil(1.0 * town.getCorpses() / NECRO_RAISE_CAPACITY) - town.getNecros());
            int necrosCanAfford = costOfNecro < 0 ? Math.min(necrosNeed, goldAvailable / -costOfNecro) : necrosNeed;
            if (necrosCanAfford > 0) {
                sendNecros = Math.min(necrosCanAfford, thisNecrosAvailable);
                goldAvailable += sendNecros * costOfNecro;
            }

            int bishopsNeed = Math.max(0, (int) Math.ceil(1.0 * town.getPeons() / BISHOP_PRAYER_CAPACITY) - town.getBishops());
            int bishopsCanAfford = costOfBishop < 0 ? Math.min(bishopsNeed, goldAvailable / -costOfBishop) : bishopsNeed;
            if (bishopsCanAfford > 0) {
                sendBishops = Math.min(bishopsCanAfford, thisBishopsAvailable);
                goldAvailable += sendBishops * costOfBishop;
            }

            int architectsNeed = Math.max(0, (int) Math.ceil(1.0 * COMPLETION_NEEDED / COMPLETION_PER_ARCHITECT) - thisTown.getArchitects());
            int architectsCanAfford = Math.min(architectsNeed, goldAvailable / Math.abs(GOLD_PER_ARCHITECT));
            sendArchitects = Math.min(architectsCanAfford, thisArchitectsAvailable);

            if (sendWarlocks > 0 || sendCrusaders > 0 || sendAmazons > 0 || sendCorsairs > 0 || sendBishops > 0 || sendNecros > 0 || sendArchitects > 0) {
                System.out.print("M " + town.getId() + " " + sendWarlocks + " " + sendCrusaders + " " + sendAmazons + " " + sendCorsairs + " " + sendBishops + " " + sendNecros + " "
                        + sendArchitects);
                return;
            }
        }
    }

    System.out.println("W");
}

private void build() {
    // B building building building...
    // (T: Temple, B: Barracks, E: Estate, P: Palace)

    int goldAvailable = thisTown.getGold() - (calcCashflow(thisTown) < GOLD_MAX_DEBT ? (calcCorsairsNeeded(thisTown) * CORSAIR_SURVEILLANCE_RATIO * GOLD_PER_STEAL) : 0);
    if (goldAvailable >= GOLD_COST_BUILDING) {
        char building = 'E';
        int templeUnits = thisTown.getWarlocks() + thisTown.getBishops() + thisTown.getNecros();
        int barracksUnits = thisTown.getCrusaders() + thisTown.getAmazons();
        int estateUnits = thisTown.getCorsairs() + thisTown.getPeons();
        if (templeUnits > barracksUnits) {
            if (templeUnits > estateUnits) {
                building = 'T';
            }
        } else {
            if (barracksUnits > estateUnits) {
                building = 'B';
            }
        }
        System.out.println("B " + building);
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
    private final int necros;
    private final int architects;
    private final int peons;
    private final int temple;
    private final int barracks;
    private final int estate;
    private final int palace;

    public Town(String string) {
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
        necros = Integer.parseInt(args[9]);
        architects = Integer.parseInt(args[10]);
        peons = Integer.parseInt(args[11]);
        temple = Integer.parseInt(args[12]);
        barracks = Integer.parseInt(args[13]);
        estate = Integer.parseInt(args[14]);
        palace = Integer.parseInt(args[15]);
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

    public int getNecros() {
        return necros;
    }

    public int getArchitects() {
        return architects;
    }

    public int getPeons() {
        return peons;
    }

    public int getSurvivingPeons() {
        int peons = this.peons;
        int zombies = Math.floorDiv(corpses, ZOMBIE_WAKING_CHANCE);
        peons = Math.max(0, peons - zombies);
        int demons = Math.floorDiv(peons - (bishops * BISHOP_PRAYER_CAPACITY), DEMON_SUMMON_CHANCE);
        peons = Math.max(0, peons - demons);
        if (round % BIRTH_ROUND == 0) {
            peons += Math.floorDiv(peons, 2);
        }
        return peons;
    }

    public int getTemple() {
        return temple;
    }

    public int getBarracks() {
        return barracks;
    }

    public int getEstate() {
        return estate;
    }

    public int getPalace() {
        return palace;
    }

    public int getSoldiers() {
        return getWarlocks() + getCrusaders() + getAmazons();
    }

    public int getUnits() {
        return getSoldiers() + getCorsairs() + getBishops() + getNecros() + getArchitects();
    }

    public int getPopulation() {
        return getUnits() + getPeons();
    }

    public boolean isMine() {
        return getOwnerId() == playerID;
    }

    public boolean isThisTown() {
        return id == thisTownID;
    }
}
}