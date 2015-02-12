
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Opportunist  {

    private static final float FIGHTING_BONUS = 1.5f;
    public static final int GOLD_PER_PEON = 5;
    public static final int GOLD_PER_TEMPLE = 2;
    public static final int GOLD_PER_BARRACKS = 2;
    public static final int GOLD_PER_ESTATE = 2;
    public static final int GOLD_PER_PALACE = 10;
    private static final int CONVERSION_COST = 50;
    private static final int GOLD_PER_STEAL = 10;
    private static final int GOLD_MAX_DEBT = 200;
    private static final int CORSAIR_COST = 12;
    private static final int BISHOP_COST = 20;
    private static final int ARCHITECT_COST = 15;
    private static final int BARRACKS_COST = 200;
    private static final int MILITARY_COST = 10;

    int round;
    int phase;
    int playerID;
    int thisTownID;


    List<Town> towns;
    List<Town> myTowns;
    List<Town> otherTowns;
    List<Town> otherNonOutlawTowns;
    List<Town> otherOutlawTowns;

    Town thisTown;

    public static void main(String[] args){
        if (args.length == 0) {
            System.out.println("8 8 9 20 10 0 13 30");
        } else {
            new Opportunist().respond(args[0].split(";"));
        }
    }

    private void respond(String[] args) {

        round = Integer.parseInt(args[0]);
        phase = Integer.parseInt(args[1]);
        playerID = Integer.parseInt(args[2]);
        thisTownID = Integer.parseInt(args[3]);

        towns = new ArrayList<>();
        myTowns = new ArrayList<>();
        otherTowns = new ArrayList<>();
        otherNonOutlawTowns= new ArrayList<>();
        otherOutlawTowns= new ArrayList<>();

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
                if (town.getOwnerId()!=-1)
                {
                    otherNonOutlawTowns.add(town);
                }
                else
                {
                    otherOutlawTowns.add(town);
                }
            }
        }


        switch (phase)
        {
            case 2: steal(); break;
            case 3: recruit(); break;
            case 6: convert(); break;
            case 7: attack(); break;
            case 8: resurrect(); break;
            case 9: move(); break;
            case 11: build(); break;
            default: System.out.println("W"); break; 
        }
    }

    private void steal() {
        ArrayList<Town> architectSortedNonOutlawTowns = new ArrayList<>(otherNonOutlawTowns);
        architectSortedNonOutlawTowns.sort((a,b)->b.getArchitects()-a.getArchitects());
        Town targetTown =null;
        int targetTownStolenGold = 0;

        // Try to steal from the towns that have the most architects as they are the long term threat.
        for (Town town : architectSortedNonOutlawTowns)
        {
            if (estimateProceedsOfTheft(town,thisTown)>thisTown.calculateTaxes())
            {
                  targetTown=town;
                  break;
            }
        }

        // see if we can cause revolution in any non outlaw towns or if the target proceeds of theft is not sufficent to support this town... 
        if (targetTown==null || estimateProceedsOfTheft(targetTown,thisTown)+thisTown.calculateTaxes()<thisTown.calculateSalary()*2)
        {
            for (Town town : otherNonOutlawTowns)
            {

                if (town.getGold()-town.calculateSalary()-thisTown.getCorsairs()*GOLD_PER_STEAL<0)
                {
                   int stolenGold = estimateProceedsOfTheft(town,thisTown);
                   if (stolenGold>targetTownStolenGold)
                   {
                       targetTownStolenGold=stolenGold;
                       targetTown=town;
                   }
                }

            }
        }

        // just pick the strongest opponent
        if (targetTown==null && round<5)
        {
            targetTown = otherNonOutlawTowns.stream().max((a,b) -> a.getSoldiers() - b.getSoldiers()).orElse(null);
        }

        // or if the target proceeds of theft is not sufficent to support this town... select the non outlaw town with the most gold!
        if (targetTown==null || estimateProceedsOfTheft(targetTown,thisTown)+thisTown.calculateTaxes()<thisTown.calculateSalary()*2)
        {
            targetTown = otherNonOutlawTowns.stream().max((a,b) -> a.gold - b.gold).orElse(null);
        }

        // otherwise just pick the outlaw town with the most gold.
        if (targetTown==null)
        {
            targetTown = otherTowns.stream().max((a,b) -> a.gold - b.gold).orElse(null);
        }

        System.out.println("S " + targetTown.getId() + " " + thisTown.getCorsairs());

    }

    private void recruit() {

        // try to keep enough peons to breed from... unless last round
        int originalNoOfPeonsAvailableToConvert = round==100?thisTown.getPeons():(round>5?thisTown.getPeons()/10:0);

        int amazonRecruit = 0;
        int crusaderRecruit = 0;
        int warlockRecruit = 0;
        int bishopRecruit = 0;
        int corsairRecruit = 0;
        int architectRecruit = 0;

        while (originalNoOfPeonsAvailableToConvert>0)
        {
            int noOfPeonsAvailableToConvert=originalNoOfPeonsAvailableToConvert;
            int recruitsLeft;
            do
            {
                recruitsLeft=noOfPeonsAvailableToConvert;

                // last round... lets just build soldiers!
                if (round==100)
                {
                    if (noOfPeonsAvailableToConvert-->0) amazonRecruit++;
                    if (noOfPeonsAvailableToConvert-->0) crusaderRecruit++;
                    if (noOfPeonsAvailableToConvert-->0) warlockRecruit++;
                }
                else
                {

                    // see if we want to recruit more bishops...
                    if (thisTown.getBishops()<(round<30?Math.pow(2,round/4):thisTown.getPeons()-50))
                    {
                        if (noOfPeonsAvailableToConvert-->0) bishopRecruit++;
                    }
                }
            } while (noOfPeonsAvailableToConvert>0 && noOfPeonsAvailableToConvert!=recruitsLeft);

            noOfPeonsAvailableToConvert = noOfPeonsAvailableToConvert<0?0:noOfPeonsAvailableToConvert;
            Town simulatedTown = new Town(thisTown);
            simulatedTown.setPeons(thisTown.getPeons()-(originalNoOfPeonsAvailableToConvert-noOfPeonsAvailableToConvert));
            simulatedTown.setAmazons(simulatedTown.getAmazons()+amazonRecruit);
            simulatedTown.setCrusaders(simulatedTown.getCrusaders()+crusaderRecruit);
            simulatedTown.setWarlocks(simulatedTown.getWarlocks()+warlockRecruit);
            simulatedTown.setBishops(simulatedTown.getWarlocks()+bishopRecruit);
            simulatedTown.setCorsairs(simulatedTown.getWarlocks()+corsairRecruit);
            simulatedTown.setArchitects(simulatedTown.getWarlocks()+architectRecruit);
            simulatedTown.setGold(simulatedTown.getGold()-amazonRecruit*MILITARY_COST
                                                         -crusaderRecruit*MILITARY_COST
                                                         -warlockRecruit*MILITARY_COST
                                                         -bishopRecruit*BISHOP_COST
                                                         -corsairRecruit*CORSAIR_COST
                                                         -architectRecruit*ARCHITECT_COST
                                                         -BARRACKS_COST // aways have enough to build a building!
                                                         );

            // ensure that we can afford (both now and in the future) to recruit this number of bishops...
            if (estimateProceedsOfTheft(thisTown) + simulatedTown.calculateTaxes()-simulatedTown.calculateSalary()>0 && simulatedTown.getGold()>simulatedTown.calculateSalary()) 
                break;
            originalNoOfPeonsAvailableToConvert--;
        }

        System.out.println("R " + warlockRecruit + " " + crusaderRecruit + " " + amazonRecruit + " " + corsairRecruit + " " + bishopRecruit + " 0 " + architectRecruit);
    }

    private void convert() {
        int currentGold = thisTown.getGold();
        int futureTaxGeneration = thisTown.calculateTaxes();
        int futureSalaryCost = thisTown.calculateSalary();
        int futureProceedsOfCrime = estimateProceedsOfTheft(thisTown);
        int futureCashFlow = futureTaxGeneration+futureProceedsOfCrime-futureSalaryCost;
        int goldAvailableToSpend = currentGold-=BARRACKS_COST;

        if (goldAvailableToSpend>CONVERSION_COST && futureCashFlow>0)
        {

            Town strongestTown = findStrongestTownThatCanDefeatGivenTown(thisTown);

            if (strongestTown == null)
            {
                // this town is already surpreme! lets see if we can be fivolous and attempt to convert anyway...
                if (thisTown.getGold()>thisTown.calculateSalary()*2)
                {
                    strongestTown=otherNonOutlawTowns.stream().max((a,b)->a.getSoldiers()-b.getSoldiers()).orElse(null);
                }
            }

            // no town targeted... select the town with the most soldiers then.
            if (strongestTown == null)
            {
                strongestTown=otherTowns.stream().max((a,b)->a.getSoldiers()-b.getSoldiers()).orElse(null);
            }

            // we have selected a town... try to convert from it...
            if (strongestTown != null)
            {
                Town simulatedThisTown = new Town(thisTown);
                int amazonConversionCount=0;
                int warlockConversionCount=0;
                int crusaderConversionCount=0;

                // iterate until we are unable to pay for conversion or unable to support converted forces
                while(true)
                {
                    futureTaxGeneration = simulatedThisTown.calculateTaxes();
                    futureSalaryCost = simulatedThisTown.calculateSalary();
                    futureCashFlow = futureTaxGeneration+futureProceedsOfCrime-futureSalaryCost;

                    goldAvailableToSpend-=CONVERSION_COST;

                    // see if we can afford to convert another military unit or have run out of bishops to use...
                    if (amazonConversionCount+warlockConversionCount+crusaderConversionCount==thisTown.getBishops() || goldAvailableToSpend < 0 || futureCashFlow<0) break;

                    // convert a amazon... if any...
                    if (strongestTown.getAmazons()>0)
                    {
                        amazonConversionCount++;
                        simulatedThisTown.setAmazons(simulatedThisTown.getAmazons()+1);
                        strongestTown.setAmazons(strongestTown.getAmazons()-1);
                    }
                    // convert a crusader... if any...
                    else if (strongestTown.getCrusaders()>0)
                    {
                        crusaderConversionCount++;
                        simulatedThisTown.setCrusaders(simulatedThisTown.getCrusaders()+1);
                        strongestTown.setCrusaders(strongestTown.getCrusaders()-1);
                    }
                    // convert a warlock... if any...
                    else if (strongestTown.getWarlocks()>0)
                    {
                        warlockConversionCount++;
                        simulatedThisTown.setWarlocks(simulatedThisTown.getWarlocks()+1);
                        strongestTown.setWarlocks(strongestTown.getWarlocks()-1);
                    }
                    // no more units to convert from the targeted town...
                    else
                    {
                        break;
                    }

                }

                System.out.println("C " + strongestTown.getId() + " " + warlockConversionCount + " " + crusaderConversionCount + " " + amazonConversionCount);
                return;
            }
        }
        System.out.println("W"); 
    }

    private void attack() {

        // nearing end game.. lets just attack every thing blindly :P
        if (round>=99)
        {
            for (Town town : towns)
            {
                if (!town.isMine())
                {
                    Town simulatedThisTown = new Town(thisTown);
                    Town simulatedOtherTown = new Town(town);

                    // attempt to attack the opponent with all our soldiers.
                    if (battle(simulatedThisTown,simulatedOtherTown,thisTown.getWarlocks(),thisTown.getCrusaders(),thisTown.getAmazons()))
                    {
                        System.out.println("A "+ town.getId()+ " " + thisTown.getWarlocks()+ " " + thisTown.getCrusaders()+ " " + thisTown.getAmazons() );
                        return;
                    }
                }
            }
        }

        // we should be in a good position... lets try to take over strongest opponent...
        if (round>32)
        {
            Town strongestTown = otherTowns.stream().max((a,b)->a.getSoldiers()-b.getSoldiers()).orElse(null);
            Town simulatedThisTown = new Town(thisTown);
            Town simulatedStrongestTown = new Town(strongestTown);

            int warlockRegiment = thisTown.getWarlocks();
            int crusaderRegiment = thisTown.getCrusaders();
            int amazonRegiment = thisTown.getAmazons();

            List<Town> remainderNonOutLawTowns = new ArrayList<Town>(otherNonOutlawTowns);
            remainderNonOutLawTowns.remove(strongestTown);

            Town nextStrongestTown = remainderNonOutLawTowns.stream().max((a,b)->a.getSoldiers()-b.getSoldiers()).orElse(null);
            boolean firstLoop=true;

            // attempt to attack the strongest opponent with the least number of soldiers possible and still be in a position to likely not succumb to the next strongest opponent.
            while (nextStrongestTown!=null && warlockRegiment+crusaderRegiment+amazonRegiment>0 && battle(simulatedThisTown,simulatedStrongestTown,warlockRegiment,crusaderRegiment,amazonRegiment))
            {

                Town simulatedThisTownAfterWinning = new Town(simulatedThisTown);
                Town simulatedNextStrongestTown = new Town(nextStrongestTown);

                if (nextStrongestTown==null || 
                    battle(simulatedNextStrongestTown,simulatedThisTownAfterWinning,
                           simulatedNextStrongestTown.getWarlocks()*2/3,
                           simulatedNextStrongestTown.getCrusaders()*2/3,
                           simulatedNextStrongestTown.getAmazons()*2/3))
                {
                    if (firstLoop) break;
                    System.out.println("A "+ strongestTown.getId()+ " " + warlockRegiment+ " " + crusaderRegiment+ " " + amazonRegiment );
                    return;
                }
                firstLoop=false;
                warlockRegiment-=warlockRegiment>0?1:0;
                crusaderRegiment-=crusaderRegiment>0?1:0;
                amazonRegiment-=amazonRegiment>0?1:0;

                simulatedThisTown = new Town(thisTown);
                simulatedStrongestTown = new Town(strongestTown);
            }

            // it looks like we are in a power deadlock with one other town... lets see if going all out will make us the victor....
            if (otherNonOutlawTowns.size()==1)
            {
                simulatedThisTown = new Town(thisTown);
                Town simulatedRemainingTown = new Town(otherNonOutlawTowns.get(0));

                if (battle(simulatedThisTown,simulatedRemainingTown,thisTown.getWarlocks(),thisTown.getCrusaders(),thisTown.getAmazons()))
                {
                    System.out.println("A "+ simulatedRemainingTown.getId()+ " " + thisTown.getWarlocks()+ " " + thisTown.getCrusaders()+ " " + thisTown.getAmazons() );
                    return;
                }
            }
        }

        System.out.println("W");
    }

    private void move() {

        // give half our funds to the most needy town...
        List<Town> poorMyTowns = myTowns.stream().filter(a->a.calculateTaxes()-a.calculateSalary()<0).collect(Collectors.toList());
        if (poorMyTowns.size()>0)
        {
            Town poorTown = poorMyTowns.get(new Random().nextInt(poorMyTowns.size()));

            if (poorTown.getId() != thisTownID)
            {
                System.out.println("T "+poorTown.getId()+ " "+ thisTown.getGold()/2);
                return;
            }
        }

        System.out.println("W");

    }

    private void resurrect() {
        // zombie shmozies!
        System.out.println("W");
    }

    private void build() {
        // endevour to always build a barracks or estate (which ever is more lucrative)
        int currentGold = thisTown.getGold();
        int futureTaxGeneration = thisTown.calculateTaxes();
        int futureSalaryCost = thisTown.calculateSalary();
        int futureProceedsOfCrime = estimateProceedsOfTheft(thisTown);
        int futureCashFlow = futureTaxGeneration+futureProceedsOfCrime-futureSalaryCost;
        int goldAvailableToSpend = currentGold;

        if (goldAvailableToSpend>BARRACKS_COST && futureCashFlow>0)
        {
            if (thisTown.getAmazons()+thisTown.getCrusaders()>thisTown.getCorsairs()+thisTown.getPeons())
            {
                System.out.println("B B");
                return;
            }
            else
            {
                System.out.println("B E");
                return;
            }
        }
        System.out.println("W");
    }

    private class Town  {

        private int ownerId =-1;
        private int id = -1;
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
        private int temples;
        private int barracks;
        private int estates;
        private int palaces;

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

        //Copy constructor
        public Town(Town source)
        {
            this.ownerId=source.ownerId;
            this.id=source.id;
            this.gold=source.gold;
            this.corpses=source.corpses;
            this.warlocks=source.warlocks;
            this.crusaders=source.crusaders;
            this.amazons=source.amazons;
            this.corsairs=source.corsairs;
            this.bishops=source.bishops;
            this.necromancers=source.necromancers;
            this.architects=source.architects;
            this.peons=source.peons;
            this.temples = source.temples;
            this.barracks = source.barracks;
            this.estates = source.estates;
            this.palaces = source.palaces;

        }

        public void setOwnerId(int ownerId)
        {
            this.ownerId = ownerId;
        }

        public void setGold(int gold)
        {
            this.gold = gold;
        }

        public void setCorpses(int corpses)
        {
            this.corpses = corpses;
        }

        public void setWarlocks(int warlocks)
        {
            this.warlocks = warlocks;
        }

        public void setCrusaders(int crusaders)
        {
            this.crusaders = crusaders;
        }

        public void setAmazons(int amazons)
        {
            this.amazons = amazons;
        }

        public void setCorsairs(int corsairs)
        {
            this.corsairs = corsairs;
        }

        public void setBishops(int bishops)
        {
            this.bishops = bishops;
        }

        public void setNecromancers(int necromancers)
        {
            this.necromancers = necromancers;
        }

        public void setArchitects(int architects)
        {
            this.architects = architects;
        }

        public void setPeons(int peons)
        {
            this.peons = peons;
        }

        public int getTemples()
        {
            return temples;
        }

        public int getBarracks()
        {
            return barracks;
        }

        public int getEstates()
        {
            return estates;
        }

        public int getPalaces()
        {
            return palaces;
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

        public int getSoldiers() {
            return getWarlocks() + getCrusaders() + getAmazons();
        }

        public boolean isMine(){
            return getOwnerId() == playerID;
        }

        public boolean isThisTown(){
            return id == thisTownID;
        }

        public int calculateSalary()
        {
            return (getWarlocks() + getCrusaders() + getAmazons()) + 2*(getCorsairs()+getBishops()+ getArchitects()+ getNecromancers());
        }

     // algorithm taken from Game.java 
        public int calculateTaxes()
        {
            int taxes = 0;
            taxes += (getPeons() * GOLD_PER_PEON);
            taxes += ((getWarlocks() + getBishops() + getNecromancers()) * (getTemples() * GOLD_PER_TEMPLE));
            taxes += ((getCrusaders() + getAmazons()) * (getBarracks() * GOLD_PER_BARRACKS));
            taxes += ((getPeons() + getCorsairs()) * (getEstates() * GOLD_PER_ESTATE));
            taxes += (getPalaces() * GOLD_PER_PALACE);
            return  taxes;  
        }

    }

    // algorithm taken from Game.java 
    private boolean battle(Town attackingTown, Town defendingTown, int attackingWarlocks, int attackingCrusaders, int attackingAmazons)
    {
        int sourceWarlocks = Math.min(attackingTown.getWarlocks(), Math.max(0, attackingWarlocks));
        int sourceCrusaders = Math.min(attackingTown.getCrusaders(), Math.max(0, attackingCrusaders));
        int sourceAmazons = Math.min(attackingTown.getAmazons(), Math.max(0, attackingAmazons));

        int destinationWarlocks = defendingTown.getWarlocks();
        int destinationCrusaders = defendingTown.getCrusaders();
        int destinationAmazons = defendingTown.getAmazons();

        int sourceWarlocksBalance = Math.max(0, sourceWarlocks - destinationWarlocks);
        int sourceWarlocksBonus = Math.min(sourceWarlocksBalance, destinationAmazons);
        float sourceWarlocksStrength = (sourceWarlocks - sourceWarlocksBonus + (sourceWarlocksBonus * FIGHTING_BONUS)); 
        int sourceCrusadersBalance = Math.max(0, sourceCrusaders - destinationCrusaders);
        int sourceCrusadersBonus = Math.min(sourceCrusadersBalance, destinationWarlocks);
        float sourceCrusadersStrength = (sourceCrusaders - sourceCrusadersBonus + (sourceCrusadersBonus * FIGHTING_BONUS)); 
        int sourceAmazonsBalance = Math.max(0, sourceAmazons - destinationAmazons);
        int sourceAmazonsBonus = Math.min(sourceAmazonsBalance, destinationCrusaders);
        float sourceAmazonsStrength = (sourceAmazons - sourceAmazonsBonus + (sourceAmazonsBonus * FIGHTING_BONUS)); 
        float sourceTotalStrength = sourceWarlocksStrength + sourceCrusadersStrength + sourceAmazonsStrength;

        int destinationWarlocksBalance = Math.max(0, destinationWarlocks - sourceWarlocks);
        int destinationWarlocksBonus = Math.min(destinationWarlocksBalance, sourceAmazons);
        float destinationWarlocksStrength = (destinationWarlocks - destinationWarlocksBonus + (destinationWarlocksBonus * FIGHTING_BONUS)); 
        int destinationCrusadersBalance = Math.max(0, destinationCrusaders - sourceCrusaders);
        int destinationCrusadersBonus = Math.min(destinationCrusadersBalance, sourceWarlocks);
        float destinationCrusadersStrength = (destinationCrusaders - destinationCrusadersBonus + (destinationCrusadersBonus * FIGHTING_BONUS)); 
        int destinationAmazonsBalance = Math.max(0, destinationAmazons - sourceAmazons);
        int destinationAmazonsBonus = Math.min(destinationAmazonsBalance, sourceCrusaders);
        float destinationAmazonsStrength = (destinationAmazons - destinationAmazonsBonus + (destinationAmazonsBonus * FIGHTING_BONUS)); 
        float destinationTotalStrength = destinationWarlocksStrength + destinationCrusadersStrength + destinationAmazonsStrength;


        if (sourceTotalStrength > destinationTotalStrength) {

            RandomNumberGenerator rand = new RandomNumberGenerator();
            int[] limits = new int[3];
            limits[0] = sourceWarlocks;
            limits[1] = sourceCrusaders;
            limits[2] = sourceAmazons;
            int[] losses = rand.genNumberWithLimits((int) Math.ceil(destinationTotalStrength), limits);

            attackingTown.setWarlocks(attackingTown.getWarlocks() - sourceWarlocks);
            attackingTown.setCrusaders(attackingTown.getCrusaders() - sourceCrusaders);
            attackingTown.setAmazons(attackingTown.getAmazons() - sourceAmazons);

            defendingTown.setWarlocks(sourceWarlocks - losses[0]);
            defendingTown.setCrusaders(sourceCrusaders - losses[1]);
            defendingTown.setAmazons(sourceAmazons - losses[2]);
            defendingTown.setCorsairs(0);
            defendingTown.setBishops(0);
            defendingTown.setNecromancers(0);
            defendingTown.setArchitects(0);

            defendingTown.setCorpses(defendingTown.getCorpses() + destinationWarlocks + destinationCrusaders + destinationAmazons + losses[0] + losses[1] + losses[2]);
            defendingTown.setOwnerId(attackingTown.getOwnerId());
            return true;
        } else if (sourceTotalStrength <= destinationTotalStrength) {

            RandomNumberGenerator rand = new RandomNumberGenerator();
            int[] limits = new int[3];
            limits[0] = destinationWarlocks;
            limits[1] = destinationCrusaders;
            limits[2] = destinationAmazons;
            int[] losses = rand.genNumberWithLimits((int) Math.ceil(sourceTotalStrength), limits);

            attackingTown.setWarlocks(attackingTown.getWarlocks() - sourceWarlocks);
            attackingTown.setCrusaders(attackingTown.getCrusaders() - sourceCrusaders);
            attackingTown.setAmazons(attackingTown.getAmazons() - sourceAmazons);

            defendingTown.setWarlocks(destinationWarlocks - losses[0]);
            defendingTown.setCrusaders(destinationCrusaders - losses[1]);
            defendingTown.setAmazons(destinationAmazons - losses[2]);

            defendingTown.setCorpses(defendingTown.getCorpses() + sourceWarlocks + sourceCrusaders + sourceAmazons + losses[0] + losses[1] + losses[2]);
        }
        return false;

    }

    /**
     * Taken from Game.java
     * 
     * Generate N random numbers when their SUM is known
     * 
     * @author Deepak Azad
     */

    public class RandomNumberGenerator  {

        public int[] genNumbers(int n, int sum){
            int[] nums = new int[n];
            int upperbound = Long.valueOf(Math.round(sum*1.0/n)).intValue();
            int offset = Long.valueOf(Math.round(0.5*upperbound)).intValue();

            int cursum = 0;
            Random random = new Random(new Random().nextInt());
            for(int i=0 ; i < n ; i++){
                int rand = random.nextInt(upperbound) + offset;
                if( cursum + rand > sum || i == n - 1) {
                    rand = sum - cursum;
                }
                cursum += rand;
                nums[i]=rand;
                if(cursum == sum){
                    break;
                }
            }
            return nums;
        }

        public int[] genNumberWithLimits(int sum, int[] limits) {

            int n = limits.length;
            int[] nums = new int[n];
            int total = 0;

            for (int l : limits) {
                total += l;
            }

            if (total <= sum)
                return limits;

            Random random = new Random(new Random().nextInt());
            while (sum > 0) {  
                int x = random.nextInt(n);
                if (nums[x] < limits[x]) {
                    nums[x] += 1;
                    sum--;
                }
            }
            return nums;
        }
    }

    // algorithm taken from Game.java
    private int estimateProceedsOfTheft(Town theivingTown)
    {
        int bestTownToTheiveProceedsAmount = -1;
        for (Town town : towns)
        {
            int goldStolen = estimateProceedsOfTheft(town,theivingTown);

            if (goldStolen > bestTownToTheiveProceedsAmount)
            {
                bestTownToTheiveProceedsAmount=goldStolen;
            }               
        }
        return bestTownToTheiveProceedsAmount;
    }

    // algorithm taken from Game.java
    private int estimateProceedsOfTheft(Town victimTown,Town theivingTown)
    {
        int goldStolen = 0;

        if (victimTown.getOwnerId()!= theivingTown.getOwnerId())
        {
            int goldReserve = victimTown.getGold() + GOLD_MAX_DEBT > 0 ? victimTown.getGold() + GOLD_MAX_DEBT : GOLD_MAX_DEBT - Math.abs(victimTown.getGold());
            int goldToSteal = theivingTown.getCorsairs() * GOLD_PER_STEAL;
            goldStolen = Math.min(goldReserve, goldToSteal);
        }

        return goldStolen;
    }

    // exactly as the method name states :)
    private Town findStrongestTownThatCanDefeatGivenTown(Town defendingTown)
    {
        int strongestSurvivingForce=-1;
        Town strongestTown=null;
        for (Town town : towns)
        {
            if (town.getOwnerId()!=defendingTown.getOwnerId() && town.getOwnerId()!=-1)
            {
                Town simulatedThisTown = new Town(defendingTown);
                Town simulatedOtherTown = new Town(town);

                // check to see if the other town could defeat this town
                if (battle(simulatedOtherTown,simulatedThisTown,simulatedOtherTown.getWarlocks(), simulatedOtherTown.getCrusaders(), simulatedOtherTown.getAmazons()))
                {
                    //and if so, if it is the most overwhelming win, then that town is the target of conversion.
                    int survivingForce=simulatedOtherTown.getAmazons()+simulatedOtherTown.getCrusaders()+simulatedOtherTown.getWarlocks()+
                                    simulatedThisTown.getAmazons()+simulatedThisTown.getCrusaders()+simulatedThisTown.getWarlocks();
                    if (survivingForce>strongestSurvivingForce)
                    {
                        strongestSurvivingForce=survivingForce;
                        strongestTown = town;
                    }
                }
            }
        }
        return strongestTown;
    }
}