package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import players.*;


public class Game {
	private final Player[] players = {
			new Aegis(),
			new Butter(),
			new Commander(),
			new Lannister(),
			new Machiaveli(),
			new Monarch(),
			new Opportunist(),
			new Politician(),
			new Sehtimianer(),
			new Serenity(),
			new Sleeper(),
			new YoungEarl(),
			new Zealots()
		};
	
	// Game Parameters
	public static final int POPULATION_TYPES = 8;
	private static final int MAX_POPULATION = 100;
	private static final int START_GOLD = 500;
	private static final int START_CORPSES = 5;
	private static final int GOLD_MAX_DEBT = 200;
	private static final float FIGHTING_BONUS = 1.5f;
	private static final int ROUNDS = 100;
	private static final int BIRTH_ROUND = 5;
	private static final int ZOMBIE_WAKING_CHANCE = 10;
	private static final int NECROMANCER_RAISE_CAPACITY = 5;
	private static final int DEMON_SUMMON_CHANCE = 10;
	private static final int BISHOP_PRAYER_CAPACITY = 50;
	private static final int CORSAIR_SURVEILLANCE_RATIO = 5;
	private static final int CORSAIR_TRANSPORT_RATIO = 200;
	private static final int OUTLAWS_PLAYER_RATIO = 5;
	private static final int OUTLAWS_PLAYER_MIN = 1;
	
	// Console
	private static final boolean DEBUG = false;
	private static final boolean GAME_MESSAGES = true;
	
	// Income per Unit
	public static final int GOLD_PER_WARLOCK = -1;
	public static final int GOLD_PER_CRUSADER = -1;
	public static final int GOLD_PER_AMAZON = -1;
	public static final int GOLD_PER_CORSAIR = -2;
	public static final int GOLD_PER_BISHOP = -2;
	public static final int GOLD_PER_NECROMANCER = -2;
	public static final int GOLD_PER_ARCHITECT = -2;
	public static final int GOLD_PER_PEON = 5;
	
	// Income per Building
	public static final int GOLD_PER_TEMPLE = 2;
	public static final int GOLD_PER_BARRACKS = 2;
	public static final int GOLD_PER_ESTATE = 2;
	public static final int GOLD_PER_PALACE = 10;
	
	// Cost of Actions
	public static final int GOLD_PER_RESURRECTION = 20;
	public static final int GOLD_PER_CONVERSION = 50;
	public static final int GOLD_PER_STEAL = 10;
	
	// Cost of Units
	public static final int GOLD_RECRUIT_WARLOCK = 10;
	public static final int GOLD_RECRUIT_CRUSADER = 10;
	public static final int GOLD_RECRUIT_AMAZON = 10;
	public static final int GOLD_RECRUIT_CORSAIR = 12;
	public static final int GOLD_RECRUIT_BISHOP = 20;
	public static final int GOLD_RECRUIT_NECROMANCER = 20;
	public static final int GOLD_RECRUIT_ARCHITECT = 15;
	public static final int GOLD_RECRUIT_DEFAULT = 20;
	
	// Cost of Buildings
	public static final int GOLD_COST_TEMPLE = 200;
	public static final int GOLD_COST_BARRACKS = 200;
	public static final int GOLD_COST_ESTATE = 200;
	public static final int GOLD_COST_PALACE = 500;
	
	// Completion of Buildings
	public static final int COMPLETION_PER_ARCHITECT = 8;
	public static final int COMPLETION_NEEDED = 100;
	
	private final List<Town> towns = new ArrayList<Town>();
	private final Player outlawPlayer = new Outlaw();
	private int round = 0;
	private int phase = 1;
	
	public Game() {
		for (int i = 0; i < players.length; i++) {
			players[i].setId(i);
		}
	}
	
	public static void main(String... args) {
		new Game().run();
	}
	
	public void run() {
			
		if (GAME_MESSAGES) 
			System.out.println("Starting a new game...");
		
		this.initialize();
		
		if (GAME_MESSAGES) 
			System.out.println("Game begins.");

		for (round = 1; round <= ROUNDS; round++) {
			if (GAME_MESSAGES) {
				System.out.println("****** ROUND " + round + " ******");
			}
			if (!makeTurns()) break; //break if only one player left
		}
		printResults();
	}
	
	private void initialize() {		
		
		for (int i = 0; i < players.length; i++) {
			try {
				int startingPopulation[] = players[i].getStartingPopulation();

				int warlocksCount = (startingPopulation[0] >= 0 ? startingPopulation[0] : 0);
				int crusadersCount = (startingPopulation[1] >= 0 ? startingPopulation[1] : 0);
				int amazonsCount = (startingPopulation[2] >= 0 ? startingPopulation[2] : 0);
				int corsairsCount = (startingPopulation[3] >= 0 ? startingPopulation[3] : 0);
				int bishopsCount = (startingPopulation[4] >= 0 ? startingPopulation[4] : 0);
				int necromancersCount =(startingPopulation[5] >= 0 ? startingPopulation[5] : 0);
				int architectsCount = (startingPopulation[6] >= 0 ? startingPopulation[6] : 0);
				int peonsCount = (startingPopulation[7] >= 0 ? startingPopulation[7] : 0);
					
				
				if (GAME_MESSAGES) System.out.println(warlocksCount+"/"+crusadersCount+"/"+amazonsCount+"/"+corsairsCount+"/"+bishopsCount+"/"+necromancersCount+"/"+architectsCount+"/"+peonsCount + " by " + players[i].getDisplayName());

				int total = (warlocksCount + crusadersCount + amazonsCount + bishopsCount + corsairsCount + necromancersCount + architectsCount + peonsCount);
				
				if (total >= 0 && total <= MAX_POPULATION) {
					towns.add(new Town(i, players[i], START_GOLD, START_CORPSES, warlocksCount, crusadersCount, amazonsCount, corsairsCount, bishopsCount, necromancersCount, architectsCount, peonsCount + (MAX_POPULATION - total)));
				} else {
					// Invalid input
					throw new Exception("Invalid input");
				}
			} catch (Exception e) {
				if (DEBUG) {
					System.out.println("Exception in initialize() by " + players[i].getDisplayName());
					e.printStackTrace();
				}
			}
			
		}
		for (int j = 0; j < Math.max(OUTLAWS_PLAYER_MIN, Math.floorDiv(towns.size(), OUTLAWS_PLAYER_RATIO)); j++) {
			towns.add(new Town(towns.size() + j, outlawPlayer, START_GOLD, START_CORPSES, 10, 10, 10, 10, 10, 10, 10, 30));
		}
		
		Collections.shuffle(towns);
	}	
	
	private boolean makeTurns() {
		
		// Phase 1
		if (DEBUG) System.out.println("Phase 1 : Taxes");
		if (onePlayerLeft()) return false;
		raiseTaxes();
		phase++;
		
		// Phase 2
		if (DEBUG) System.out.println("Phase 2 : Thefts");
		if (onePlayerLeft()) return false;
		stealMoney();
		phase++;
		
		// Phase 3
		if (DEBUG) System.out.println("Phase 3 : Recruitments");
		if (onePlayerLeft()) return false;
		recruitTroops();
		phase++;
		
		// Phase 4
		if (DEBUG) System.out.println("Phase 4 : Wages");
		if (onePlayerLeft()) return false;
		payWages();
		phase++;
		
		// Phase 5
		if (DEBUG) System.out.println("Phase 5 : Revolts");
		if (onePlayerLeft()) return false;
		triggerRevolt();
		phase++;
		
		// Phase 6
		if (DEBUG) System.out.println("Phase 6 : Conversions");
		if (onePlayerLeft()) return false;
		convertEnemies();
		phase++;
		
		// Phase 7
		if (DEBUG) System.out.println("Phase 7 : Attacks");
		if (onePlayerLeft()) return false;
		attackFoes();
		phase++;
		
		// Phase 8
		if (DEBUG) System.out.println("Phase 8 : Resurrections");
		if (onePlayerLeft()) return false;
		raiseCorpses();
		phase++;
		
		// Phase 9
		if (DEBUG) System.out.println("Phase 9 : Movements");
		if (onePlayerLeft()) return false;
		moveArmyOrTribute();
		phase++;
		
		// Phase 10
		if (DEBUG) System.out.println("Phase 10 : Defenses");
		if (onePlayerLeft()) return false;
		awakeZombies();
		summonDemons();
		sackCities();
		phase++;
		
		// Phase 11
		if (DEBUG) System.out.println("Phase 11 : Constructions");
		if (onePlayerLeft()) return false;
		workPlants();
		constructBuildings();
		phase++;
		
		// Phase 12
		if (DEBUG) System.out.println("Phase 12 : Children");
		if (onePlayerLeft()) return false;
		makeChildren();
		phase = 1;
		
		
		
		return true;
		
	}
	
	private boolean onePlayerLeft() {
		Player player = null;
		for (Town town : towns) {
			if (player == null) {
				player = town.getOwner();
			} else if (!player.equals(town.getOwner())) {
				return false;
			}
		}
		return true;
	}
	
	private void printResults() {
		
		int townCount = 0;
		int populationCount = 0;
		int goldCount = 0;
		
		List<Score> scores = new ArrayList<Score>();
		
		System.out.println("********** FINISH **********");
		
		for (Player player : players) {
			townCount = 0;
			populationCount = 0;
			goldCount = 0;
			for (Town town : towns) {
				if (player.equals(town.getOwner())) {
					townCount++;
					populationCount += town.getPopulation();
					goldCount += town.getGold();
				}
			}
			scores.add(new Score(player, townCount, populationCount, goldCount));
		}
		
		//neutral player
		townCount = 0;
		populationCount = 0;
		goldCount = 0;
		for (Town town : towns) {
			if (outlawPlayer.equals(town.getOwner())) {
				townCount++;
				populationCount += town.getPopulation();
				goldCount += town.getGold();
			}
		}
		scores.add(new Score(outlawPlayer, townCount, populationCount, goldCount));
	
		//sort descending
		Collections.sort(scores, Collections.reverseOrder());
		
		for (int i = 0; i < scores.size(); i++) {
			Score score = scores.get(i);
			System.out.println(i+1 + ". " + score.print());
		}
	}
	
	private void raiseTaxes() {
		for (Town town : towns) {
			int taxes = 0;
			taxes += (town.getPeons() * Game.GOLD_PER_PEON);
			taxes += ((town.getWarlocks() + town.getBishops() + town.getNecromancers()) * (town.getTemples() * Game.GOLD_PER_TEMPLE));
			taxes += ((town.getCrusaders() + town.getAmazons()) * (town.getBarracks() * Game.GOLD_PER_BARRACKS));
			taxes += ((town.getPeons() + town.getCorsairs()) * (town.getEstates() * Game.GOLD_PER_ESTATE));
			taxes += (town.getPalaces() * Game.GOLD_PER_PALACE);
			town.setGold(town.getGold() + taxes);
			
			if (GAME_MESSAGES && (taxes > 0) && !town.getOwner().equals(outlawPlayer)) System.out.println(town.getOwner().getDisplayName() + " received " + taxes + " gold in taxes");
		}
	}
	
	private void payWages() {
		for (Town town : towns) {
			int wages = 0;
			wages += (town.getWarlocks() * Game.GOLD_PER_WARLOCK);
			wages += (town.getCrusaders() * Game.GOLD_PER_CRUSADER);
			wages += (town.getAmazons() * Game.GOLD_PER_AMAZON);
			wages += (town.getCorsairs() * Game.GOLD_PER_CORSAIR);
			wages += (town.getBishops() * Game.GOLD_PER_BISHOP);
			wages += (town.getNecromancers() * Game.GOLD_PER_NECROMANCER);
			wages += (town.getArchitects() * Game.GOLD_PER_ARCHITECT);
			town.setGold(town.getGold() + wages);
			
			if (GAME_MESSAGES && (wages < 0) && !town.getOwner().equals(outlawPlayer)) System.out.println(town.getOwner().getDisplayName() + " paid " + -wages + " gold in wages");
		}
	}
	
	private void triggerRevolt() {
		for (Town town : towns) {
			if (outlawPlayer.equals(town.getOwner())) {
				continue;
			}
			if (town.getGold() < 0) {
				if (GAME_MESSAGES) {
					System.out.println(town.getOwner().getDisplayName() + " lost a town due to revolt");
				}
				town.setGold(START_GOLD);
				town.setOwner(outlawPlayer);
			}
		}
	}
	
	private void makeChildren() {
		if (round % BIRTH_ROUND == 0) {
			for (Town town : towns) {
				int birthes = 0;
				birthes += Math.floorDiv(town.getPeons(), 2);
				town.setPeons(town.getPeons() + birthes);
				
				if (GAME_MESSAGES && (birthes > 0) && !town.getOwner().equals(outlawPlayer)) System.out.println(town.getOwner().getDisplayName() + " gave birth to " + birthes + " children");
			}
		}
	}
	
	private void awakeZombies() {
		for (Town town : towns) {
			
			int zombies = Math.floorDiv(town.getCorpses(), ZOMBIE_WAKING_CHANCE);
			int killedCitizens = Math.min(town.getPeons(), zombies);
			
			town.setPeons(town.getPeons() - killedCitizens);
			town.setCorpses(town.getCorpses() - zombies);
			
			if (GAME_MESSAGES && (killedCitizens > 0) && !town.getOwner().equals(outlawPlayer)) System.out.println(town.getOwner().getDisplayName() + " lost " + killedCitizens + " peons from an attack of " + zombies + " zombies");
		}	
	}

	private void summonDemons() {
		for (Town town : towns) {
			
			int demons = Math.floorDiv(town.getPeons() - (town.getBishops() * BISHOP_PRAYER_CAPACITY), DEMON_SUMMON_CHANCE);
			
			if (demons > 0) {
				int killedCitizens = Math.min(town.getPeons(), demons);
				town.setPeons(town.getPeons() - killedCitizens);
				town.setCorpses(town.getCorpses() + killedCitizens);
				
				if (GAME_MESSAGES && (demons > 0) && !town.getOwner().equals(outlawPlayer)) System.out.println(town.getOwner().getDisplayName() + " lost " + killedCitizens + " peons from an attack of " + demons + " demons");
			}
		}	
	}
	
	private void sackCities() {
		int outlaws = 0;
		int otherTowns = 0;
		for (Town town : towns) {
			if (town.getOwner().equals(outlawPlayer)) {
				outlaws += town.getPopulation();			
			} else {
				otherTowns++;
			}
		}
		int averageOutlaws = Math.floorDiv(outlaws, otherTowns);
		
		for (Town town : towns) {
			if (!town.getOwner().equals(outlawPlayer)) {
				int overflowOutlaws = Math.max(0, averageOutlaws - (town.getCorsairs() * CORSAIR_SURVEILLANCE_RATIO));
				int goldReserve = town.getGold() >= GOLD_MAX_DEBT ? town.getGold() : town.getGold() + GOLD_MAX_DEBT;
				int goldToSteal = overflowOutlaws * GOLD_PER_STEAL;
				int goldStolen = Math.min(goldReserve, goldToSteal);
				
				town.setGold(town.getGold() - goldStolen);
				
				if (GAME_MESSAGES && (goldStolen > 0)) System.out.println("Stray bandits stole " + goldStolen + " gold from " + town.getOwner().getDisplayName());
			}
		}	
	}
	
	private String generateArgs() {
		
		StringBuilder builder = new StringBuilder();
		//PlayerId TownId Gold Corpses warlocks crusaders amazons corsairs bishops necromances architects peons
		for (Town town : towns) {
			builder.append(';');
			builder.append(town.getOwner().getId()).append('_');
			builder.append(town.getId()).append('_');
			builder.append(town.getGold()).append('_');
			builder.append(town.getCorpses()).append('_');
			builder.append(town.getWarlocks()).append('_');
			builder.append(town.getCrusaders()).append('_');
			builder.append(town.getAmazons()).append('_');
			builder.append(town.getCorsairs()).append('_');
			builder.append(town.getBishops()).append('_');
			builder.append(town.getNecromancers()).append('_');
			builder.append(town.getArchitects()).append('_');
			//builder.append(town.getPeons());
			builder.append(town.getPeons()).append('_');
			builder.append(this.generateBuildingsList(town));
		}
		return builder.toString();
	}
	
	private String generateBuildingsList(Town town) {
		
		StringBuilder builder = new StringBuilder();
		int temples = 0;
		int barracks = 0;
		int estates = 0;
		int palaces = 0;
		for (Building building : town.getBuildings()) {
			if (building.getCompletion() == COMPLETION_NEEDED) {
				switch (building.getType()) {
					case TEMPLE: temples++; break;
					case BARRACKS: barracks++; break;
					case ESTATE: estates++; break;
					case PALACE: palaces++; break;
					default: break;
				}
			}
		}
		builder.append(temples).append("_");
		builder.append(barracks).append("_");
		builder.append(estates).append("_");
		builder.append(palaces);
		return builder.toString();
	}
		
	private void raiseCorpses() {
		
		for (Town town : towns) {
		
			Player owner = town.getOwner();
			
			try {
				String command = round + ";" + phase + ";" + owner.getId() + ";" + town.getId() + generateArgs();
				String response = town.getCommand(command);
				
				if (DEBUG) {
					System.out.println("Command : " + command); 
					System.out.println("Response : " + response);
				}
				
				switch (response.charAt(0)) {
					case 'R': executeResurrection(new Command(response, town)); break;
					case 'W': break;
					default : break;
							
				}

			} catch (Exception e) {
				if (DEBUG) {
					System.out.println("Exception in makeTurns() by " + owner.getDisplayName());
					e.printStackTrace();
				}
			}
				
		}
	}

	private void stealMoney() {
		
		for (Town town : towns) {
		
			Player owner = town.getOwner();
			
			try {
				String command = round + ";" + phase + ";" + owner.getId() + ";" + town.getId() + generateArgs();
				String response = town.getCommand(command);
				
				if (DEBUG) {
					System.out.println("Command : " + command); 
					System.out.println("Response : " + response);
				}
				
				switch (response.charAt(0)) {
					case 'S': executeTheft(new Command(response, town)); break;
					case 'W': break;
					default : break;
							
				}

			} catch (Exception e) {
				if (DEBUG) {
					System.out.println("Exception in makeTurns() by " + owner.getDisplayName());
					e.printStackTrace();
				}
			}
				
		}
	}

	private void recruitTroops() {
		
		for (Town town : towns) {
		
			Player owner = town.getOwner();
			
			try {
				String command = round + ";" + phase + ";" + owner.getId() + ";" + town.getId() + generateArgs();
				String response = town.getCommand(command);
				
				if (DEBUG) {
					System.out.println("Command : " + command); 
					System.out.println("Response : " + response);
				}
				
				switch (response.charAt(0)) {
					case 'R': executeRecruitment(new Command(response, town)); break;
					case 'W': break;
					default : break;
							
				}

			} catch (Exception e) {
				if (DEBUG) {
					System.out.println("Exception in makeTurns() by " + owner.getDisplayName());
					e.printStackTrace();
				}
			}
				
		}
	}
	
	private void workPlants() {
		for (Town town : towns) {
			
			int architectsAvailable = town.getArchitects();
			for (Building building : town.getBuildings()) {
				
				if (building.getCompletion() < COMPLETION_NEEDED && architectsAvailable > 0) {
					
					int completion = building.getCompletion();
					int missingCompletion = COMPLETION_NEEDED - completion;
					int architectsRequired = -Math.floorDiv(-missingCompletion, COMPLETION_PER_ARCHITECT);
					
					if (architectsAvailable >= architectsRequired) {
						completion = 100;
						architectsAvailable -= architectsRequired;
						
					} else { 
						completion += (architectsAvailable * COMPLETION_PER_ARCHITECT);
						architectsAvailable = 0;
					}
					
					building.setCompletion(completion);
					
					if (GAME_MESSAGES && (completion == COMPLETION_NEEDED))  System.out.println(town.getOwner().getDisplayName() + " finished the construction of : " + building.getType());
					
					if (DEBUG) System.out.println(town.getOwner().getDisplayName() + ", construction status of " + building.getType() + " : " + completion + "%");
				}
			}
		}
	}
	
	private void constructBuildings() {
		
		
		for (Town town : towns) {
		
			Player owner = town.getOwner();
			
			try {
				String command = round + ";" + phase + ";" + owner.getId() + ";" + town.getId() + generateArgs();
				String response = town.getCommand(command);
				
				if (DEBUG) {
					System.out.println("Command : " + command); 
					System.out.println("Response : " + response);
				}
				
				switch (response.charAt(0)) {
					case 'B': executeConstruction(new Command(response, town)); break;
					case 'W': break;
					default : break;
							
				}

			} catch (Exception e) {
				if (DEBUG) {
					System.out.println("Exception in makeTurns() by " + owner.getDisplayName());
					e.printStackTrace();
				}
			}
				
		}
		
	}

	private void convertEnemies() {
		
		for (Town town : towns) {
		
			Player owner = town.getOwner();
			
			try {
				String command = round + ";" + phase + ";" + owner.getId() + ";" + town.getId() + generateArgs();
				String response = town.getCommand(command);
				
				if (DEBUG) {
					System.out.println("Command : " + command); 
					System.out.println("Response : " + response);
				}
				
				switch (response.charAt(0)) {
					case 'C': executeConversion(new Command(response, town)); break;
					case 'W': break;
					default : break;
							
				}

			} catch (Exception e) {
				if (DEBUG) {
					System.out.println("Exception in makeTurns() by " + owner.getDisplayName());
					e.printStackTrace();
				}
			}
		}
	}

	private void attackFoes() {
		
		for (Town town : towns) {
		
			Player owner = town.getOwner();
			
			try {
				String command = round + ";" + phase + ";" + owner.getId() + ";" + town.getId() + generateArgs();
				String response = town.getCommand(command);
				
				if (DEBUG) {
					System.out.println("Command : " + command); 
					System.out.println("Response : " + response);
				}
				
				switch (response.charAt(0)) {
					case 'A': executeAttack(new Command(response, town)); break;
					case 'W': break;
					default : break;
							
				}

			} catch (Exception e) {
				if (DEBUG) {
					System.out.println("Exception in makeTurns() by " + owner.getDisplayName());
					e.printStackTrace();
				}
			}
		}
	}

	private void moveArmyOrTribute() {
		
		for (Town town : towns) {
		
			Player owner = town.getOwner();
			
			try {
				String command = round + ";" + phase + ";" + owner.getId() + ";" + town.getId() + generateArgs();
				String response = town.getCommand(command);
				
				if (DEBUG) {
					System.out.println("Command : " + command); 
					System.out.println("Response : " + response);
				}
				
				switch (response.charAt(0)) {
					case 'M': executeMovement(new Command(response, town)); break;
					case 'T': executeTribute(new Command(response, town)); break;
					case 'W': break;
					default : break;
							
				}

			} catch (Exception e) {
				if (DEBUG) {
					System.out.println("Exception in makeTurns() by " + owner.getDisplayName());
					e.printStackTrace();
				}
			}
		}
	}
	
	private void executeTheft(Command support) {
		
		Town source = support.getSource();
		try {
			String[] args = support.getArgs();
			
			if (support.getCommand().equals("S") && args.length == 2) {
			
				int destinationId = Integer.parseInt(args[0]);
				Town destination = towns.get(towns.indexOf(new Town(destinationId)));
			
				if (!source.getOwner().equals(destination.getOwner())) {
					
					int corsairsCount = Math.max(0, Integer.parseInt(args[1]));
					int availableCorsairs = source.getCorsairs();
					int useableCorsaires = Math.min(corsairsCount, availableCorsairs);
					int goldReserve = destination.getGold() + GOLD_MAX_DEBT > 0 ? destination.getGold() + GOLD_MAX_DEBT : GOLD_MAX_DEBT - Math.abs(destination.getGold());
					int goldToSteal = useableCorsaires * GOLD_PER_STEAL;
					int goldStolen = Math.min(goldReserve, goldToSteal);
						
					if (goldStolen > 0) {
						source.setGold(source.getGold() + goldStolen);
						destination.setGold(destination.getGold() - goldStolen);
						
						if (GAME_MESSAGES) System.out.println(source.getOwner().getDisplayName() + " stole " + goldStolen + " gold from " + destination.getOwner().getDisplayName());
					}
				}
			} else if (support.getCommand().equals("W")) {
				// Do nothing
			} else {
				if (DEBUG) System.out.println("Phase " + phase + " (Theft) : Invalid command by " + source.getOwner().getDisplayName() + "{" + source.getId() + "}");
			}
		} catch (Exception e) {
			if (DEBUG) {
				System.out.println("Exception in executeTheft() by " + source.getOwner().getDisplayName());
				e.printStackTrace();
			}
		}
	}
	
	private void executeResurrection(Command support) {
		
		Town source = support.getSource();
		try {
			String[] args = support.getArgs();
			
			if (support.getCommand().equals("R") && args.length == 1) {
			
				int corpsesToResurrect = Math.max(0, Integer.parseInt(args[0]));
				
				if (corpsesToResurrect > 0) {
					
					int raiseCapacity = source.getNecromancers() * NECROMANCER_RAISE_CAPACITY;
					int corpsesToRaise = Math.min(source.getCorpses(), corpsesToResurrect);
					int corpsesRaisable = Math.min(corpsesToRaise, raiseCapacity);
					int goldAvailable = source.getGold();
					int corpsesAffordable = Math.min(Math.floorDiv(goldAvailable, GOLD_PER_RESURRECTION), corpsesRaisable); 
					int cost = corpsesAffordable * GOLD_PER_RESURRECTION;
					
					if (corpsesAffordable > 0) {
						source.setPeons(source.getPeons() + corpsesAffordable);
						source.setCorpses(source.getCorpses() - corpsesAffordable);
						source.setGold(source.getGold() - cost);
						
						if (GAME_MESSAGES && (corpsesAffordable > 0)) System.out.println(source.getOwner().getDisplayName() + " resurrected " + corpsesAffordable + " corpses");
					}
				}
				
			} else if (support.getCommand().equals("W")) {
				// Do nothing
			} else {
				if (DEBUG) System.out.println("Phase " + phase + " (Resurrection) : Invalid command by " + source.getOwner().getDisplayName() + "{" + source.getId() + "}");
			}
		} catch (Exception e) {
			if (DEBUG) {
				System.out.println("Exception in executeResurrection() by " + source.getOwner().getDisplayName());
				e.printStackTrace();
			}
		}
	}

	private void executeRecruitment(Command support) {

	    Town source = support.getSource();
	    try {
	        String[] args = support.getArgs();

	        if (support.getCommand().equals("R") && args.length == 7) {
	            int goldAvailable = source.getGold();
	            if (goldAvailable <= 0) {
	                return;
	            }

	            int warlocksCount = Math.max(0, Integer.parseInt(args[0]));
	            int crusadersCount = Math.max(0, Integer.parseInt(args[1]));
	            int amazonsCount = Math.max(0, Integer.parseInt(args[2]));
	            int corsairsCount = Math.max(0, Integer.parseInt(args[3]));
	            int bishopsCount = Math.max(0, Integer.parseInt(args[4]));
	            int necromancersCount = Math.max(0, Integer.parseInt(args[5]));
	            int architectsCount = Math.max(0, Integer.parseInt(args[6]));

	            int originalWarlocksCount = warlocksCount;
	            int originalCrusadersCount = crusadersCount;
	            int originalAmazonsCount = amazonsCount;
	            int originalCorsairsCount = corsairsCount;
	            int originalBishopsCount = bishopsCount;
	            int originalNecromancersCount = necromancersCount;
	            int originalArchitectsCount = architectsCount;

	            int unitsToRecruits = warlocksCount + crusadersCount + amazonsCount + corsairsCount + bishopsCount + necromancersCount + architectsCount;
	            int peonsAvailable = source.getPeons();
	            int recruitableUnits = Math.min(unitsToRecruits, peonsAvailable);
	            if (recruitableUnits != unitsToRecruits) {
	                RandomNumberGenerator random = new RandomNumberGenerator();
	                int[] recruits = random.genNumberWithLimits(recruitableUnits, new int[] { warlocksCount, crusadersCount, amazonsCount, corsairsCount, bishopsCount, necromancersCount,
	                        architectsCount });
	                warlocksCount = recruits[0];
	                crusadersCount = recruits[1];
	                amazonsCount = recruits[2];
	                corsairsCount = recruits[3];
	                bishopsCount = recruits[4];
	                necromancersCount = recruits[5];
	                architectsCount = recruits[6];
	            }

	            int wouldCost;
	            int index = 1;
	            boolean tooExpensive = true;
	            do {
	                wouldCost = warlocksCount * GOLD_RECRUIT_WARLOCK + crusadersCount * GOLD_RECRUIT_CRUSADER + amazonsCount * GOLD_RECRUIT_AMAZON + corsairsCount * GOLD_RECRUIT_CORSAIR
	                        + bishopsCount * GOLD_PER_BISHOP + necromancersCount * GOLD_RECRUIT_NECROMANCER + architectsCount * GOLD_RECRUIT_ARCHITECT;
	                if (goldAvailable < wouldCost) {
	                    RandomNumberGenerator random = new RandomNumberGenerator();
	                    int[] recruits = random.genNumberWithLimits(recruitableUnits - index, new int[] { originalWarlocksCount, originalCrusadersCount, originalAmazonsCount, originalCorsairsCount,
	                            originalBishopsCount, originalNecromancersCount, originalArchitectsCount });
	                    warlocksCount = recruits[0];
	                    crusadersCount = recruits[1];
	                    amazonsCount = recruits[2];
	                    corsairsCount = recruits[3];
	                    bishopsCount = recruits[4];
	                    necromancersCount = recruits[5];
	                    architectsCount = recruits[6];
	                } else {
	                    tooExpensive = false;
	                }
	                index++;
	            } while (tooExpensive);

	            int recruted = warlocksCount + crusadersCount + amazonsCount + corsairsCount + bishopsCount + necromancersCount + architectsCount;
	            if (recruted > 0) {
	                source.setWarlocks(source.getWarlocks() + warlocksCount);
	                source.setCrusaders(source.getCrusaders() + crusadersCount);
	                source.setAmazons(source.getAmazons() + amazonsCount);
	                source.setCorsairs(source.getCorsairs() + corsairsCount);
	                source.setBishops(source.getBishops() + bishopsCount);
	                source.setNecromancers(source.getNecromancers() + necromancersCount);
	                source.setArchitects(source.getArchitects() + architectsCount);
	                source.setPeons(source.getPeons() - recruted);
	                source.setGold(source.getGold() - wouldCost);

	                if (GAME_MESSAGES)
	                    System.out.println(source.getOwner().getDisplayName() + " recruted " + recruted + " units (" + warlocksCount + " Wa / " + crusadersCount + " Cr / " + amazonsCount + " Am / "
	                            + corsairsCount + " Co / " + bishopsCount + " Bi / " + necromancersCount + " Ne / " + architectsCount + " Ar)");
	            }
	        } else if (support.getCommand().equals("W")) {
	            // Do nothing
	        } else {
	            if (DEBUG)
	                System.out.println("Phase " + phase + " (Recruitment) : Invalid command by " + source.getOwner().getDisplayName() + "{" + source.getId() + "}");
	        }
	    } catch (Exception e) {
	        if (DEBUG) {
	            System.out.println("Exception in executeRecruitment() by " + source.getOwner().getDisplayName());
	            e.printStackTrace();
	        }
	    }
	}

	private void executeConstruction(Command support) {
		
		Town source = support.getSource();
		try {
			String[] args = support.getArgs();
			
			if (support.getCommand().equals("B")) {
			
				for (int i = 0; i < args.length; i++) {
					
					int cost = 0;
					BuildingType type;
					switch (args[i]) {
						case "T": cost = GOLD_COST_TEMPLE; type = BuildingType.TEMPLE; break;
						case "B": cost = GOLD_COST_BARRACKS; type = BuildingType.BARRACKS; break;
						case "E": cost = GOLD_COST_ESTATE; type = BuildingType.ESTATE; break;
						case "P": cost = GOLD_COST_PALACE; type = BuildingType.PALACE; break;
						default: throw new Exception("Incorrect building : " + args[i]);		
					}
					
					if (source.getGold() >= cost) {
						source.getBuildings().add(new Building(type));
						source.setGold(source.getGold() - cost);
						if (GAME_MESSAGES) System.out.println(source.getOwner().getDisplayName() + " started the construction of : " + type); 
					}
				}
			} else if (support.getCommand().equals("W")) {
				// Do nothing
			} else {
				if (DEBUG) System.out.println("Phase " + phase + " (Construction) : Invalid command by " + source.getOwner().getDisplayName() + "{" + source.getId() + "}");
			}
		} catch (Exception e) {
			if (DEBUG) {
				System.out.println("Exception in executeConstruction() by " + source.getOwner().getDisplayName());
				e.printStackTrace();
			}
		}
	}

	private void executeConversion(Command support) {
		
		Town source = support.getSource();
		try {
			String[] args = support.getArgs();
			
			if (support.getCommand().equals("C") && args.length == 4) {
			
				int destinationId = Integer.parseInt(args[0]);
				Town destination = towns.get(towns.indexOf(new Town(destinationId)));
				
				if (!source.getOwner().equals(destination.getOwner())) {
					int warlocksCount = Math.max(0, Integer.parseInt(args[1]));
					int crusadersCount = Math.max(0, Integer.parseInt(args[2]));
					int amazonsCount = Math.max(0, Integer.parseInt(args[3]));
					
					int warlocksConvertible = Math.min(destination.getWarlocks(), warlocksCount);
					int crusadersConvertible = Math.min(destination.getCrusaders(), crusadersCount);
					int amazonsConvertible = Math.min(destination.getAmazons(), amazonsCount);
					
					int totalConvertible = (warlocksConvertible + crusadersConvertible + amazonsConvertible);
					int conversionsPossible = Math.min(totalConvertible, source.getBishops());
					int cost = conversionsPossible * GOLD_PER_CONVERSION;
					
					if (source.getGold() >= cost && conversionsPossible > 0) {
						
						RandomNumberGenerator random = new RandomNumberGenerator();
						int[] conversions = random.genNumberWithLimits(conversionsPossible, new int[]{warlocksConvertible, crusadersConvertible, amazonsConvertible});
						int warlocksConverted = conversions[0];
						int crusadersConverted = conversions[1];
						int amazonsConverted = conversions[2];
						int converted = warlocksConverted + crusadersConverted + amazonsConverted;
						
						if (converted > 0) {
							destination.setWarlocks(destination.getWarlocks() - warlocksConverted);
							destination.setCrusaders(destination.getCrusaders() - crusadersConverted);
							destination.setAmazons(destination.getAmazons() - amazonsConverted);
							source.setWarlocks(source.getWarlocks() + warlocksConverted);
							source.setCrusaders(source.getCrusaders() + crusadersConverted);
							source.setAmazons(source.getAmazons() + amazonsConverted);
							source.setGold(source.getGold() - cost);
						
							if (GAME_MESSAGES) System.out.println(source.getOwner().getDisplayName() + " converted " + converted + " units ("+ warlocksConverted + " Wa / " + crusadersConverted + " Cr / " + amazonsConverted + " Am) from " + destination.getOwner().getDisplayName());
						}
					}
				}			
			} else if (support.getCommand().equals("W")) {
				// Do nothing
			} else {
				if (DEBUG) System.out.println("Phase " + phase + " (Conversion) : Invalid command by " + source.getOwner().getDisplayName() + "{" + source.getId() + "}");
			}
		} catch (Exception e) {
			if (DEBUG) {
				System.out.println("Exception in executeConversion() by " + source.getOwner().getDisplayName());
				e.printStackTrace();
			}
		}
	}
	

	private void executeAttack(Command support) {
		
		Town source = support.getSource();
		try {
			String[] args = support.getArgs();
			
			if (support.getCommand().equals("A") && args.length == 4) {
				
				int destinationId = Integer.parseInt(args[0]);
				Town destination = towns.get(towns.indexOf(new Town(destinationId)));
				
				if (!source.getOwner().equals(destination.getOwner())) {
					
					int sourceWarlocks = Math.min(source.getWarlocks(), Math.max(0, Integer.parseInt(args[1])));
					int sourceCrusaders = Math.min(source.getCrusaders(), Math.max(0, Integer.parseInt(args[2])));
					int sourceAmazons = Math.min(source.getAmazons(), Math.max(0, Integer.parseInt(args[3])));
					
					int destinationWarlocks = destination.getWarlocks();
					int destinationCrusaders = destination.getCrusaders();
					int destinationAmazons = destination.getAmazons();
					
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
					
					if (GAME_MESSAGES) System.out.println(source.getOwner().getDisplayName() + " attacked " + destination.getOwner().getDisplayName() + " : " + sourceTotalStrength + " ( " + sourceWarlocks + " Wa / " + sourceCrusaders + " Cr / " + sourceAmazons + " Am) vs " + destinationTotalStrength + " ( " + destinationWarlocks + " Wa / " + destinationCrusaders + " Cr / " + destinationAmazons + " Am)");
					
					if (sourceTotalStrength > destinationTotalStrength) {
						
						RandomNumberGenerator rand = new RandomNumberGenerator();
						int[] limits = new int[3];
						limits[0] = sourceWarlocks;
						limits[1] = sourceCrusaders;
						limits[2] = sourceAmazons;
						int[] losses = rand.genNumberWithLimits((int) Math.ceil(destinationTotalStrength), limits);
							
						source.setWarlocks(source.getWarlocks() - sourceWarlocks);
						source.setCrusaders(source.getCrusaders() - sourceCrusaders);
						source.setAmazons(source.getAmazons() - sourceAmazons);
						
						destination.setWarlocks(sourceWarlocks - losses[0]);
						destination.setCrusaders(sourceCrusaders - losses[1]);
						destination.setAmazons(sourceAmazons - losses[2]);
						destination.setCorsairs(0);
						destination.setBishops(0);
						destination.setNecromancers(0);
						destination.setArchitects(0);
						
						destination.setCorpses(destination.getCorpses() + destinationWarlocks + destinationCrusaders + destinationAmazons + losses[0] + losses[1] + losses[2]);
						
						if (GAME_MESSAGES) System.out.println(source.getOwner().getDisplayName() + " captured a town from " + destination.getOwner().getDisplayName() + " (" + source.getOwner().getDisplayName() + " losses : " + losses[0] + " Wa / " + losses[1] + " Cr / " + losses[2] + " Am)"); 
						
						destination.setOwner(source.getOwner());
								
					} else if (sourceTotalStrength <= destinationTotalStrength) {
						
						RandomNumberGenerator rand = new RandomNumberGenerator();
						int[] limits = new int[3];
						limits[0] = destinationWarlocks;
						limits[1] = destinationCrusaders;
						limits[2] = destinationAmazons;
						int[] losses = rand.genNumberWithLimits((int) Math.ceil(sourceTotalStrength), limits);
						
						source.setWarlocks(source.getWarlocks() - sourceWarlocks);
						source.setCrusaders(source.getCrusaders() - sourceCrusaders);
						source.setAmazons(source.getAmazons() - sourceAmazons);
						
						destination.setWarlocks(destinationWarlocks - losses[0]);
						destination.setCrusaders(destinationCrusaders - losses[1]);
						destination.setAmazons(destinationAmazons - losses[2]);
						
						destination.setCorpses(destination.getCorpses() + sourceWarlocks + sourceCrusaders + sourceAmazons + losses[0] + losses[1] + losses[2]);
						
						
						if (GAME_MESSAGES) System.out.println(source.getOwner().getDisplayName() + " failed to capture a town from " + destination.getOwner().getDisplayName() + " (" + destination.getOwner().getDisplayName() + " losses : " + losses[0] + " Wa / " + losses[1] + " Cr / " + losses[2] + " Am)"); 
						
					}
				}
			} else if (support.getCommand().equals("W")) {
				// Do nothing
			} else {
				if (DEBUG) System.out.println("Phase " + phase + " (Attack) : Invalid command by " + source.getOwner().getDisplayName() + "{" + source.getId() + "}");
			}
		} catch (Exception e) {
			if (DEBUG) {
				System.out.println("Exception in executeAttack() by " + source.getOwner().getDisplayName());
				e.printStackTrace();
			}
		}
	}


	private void executeMovement(Command support) {
		
		Town source = support.getSource();
		try {
			String[] args = support.getArgs();
			
			if (support.getCommand().equals("M") && args.length == 8) {
				
				int destinationId = Integer.parseInt(args[0]);
				Town destination = towns.get(towns.indexOf(new Town(destinationId)));
			
				if (destinationId != source.getId()) {
				
					int warlocksCount = Math.max(0, Integer.parseInt(args[1]));
					int crusadersCount = Math.max(0, Integer.parseInt(args[2]));
					int amazonsCount = Math.max(0, Integer.parseInt(args[3]));
					int corsairsCount = Math.max(0, Integer.parseInt(args[4]));
					int bishopsCount = Math.max(0, Integer.parseInt(args[5]));
					int necromancersCount = Math.max(0, Integer.parseInt(args[6]));
					int architectsCount = Math.max(0, Integer.parseInt(args[7]));
					
					int movedUnits = 0;
					int movedWarlocks = 0;
					int movedCrusaders = 0;
					int movedAmazons = 0;
					int movedCorsairs = 0;
					int movedBishops = 0;
					int movedNecromancers = 0;
					int movedArchitects = 0;
					
					if (source.getWarlocks() >= warlocksCount) {
						source.setWarlocks(source.getWarlocks() - warlocksCount);
						destination.setWarlocks(destination.getWarlocks() + warlocksCount);
						movedUnits += warlocksCount;
						movedWarlocks += warlocksCount;
						
					}
					if (source.getCrusaders() >= crusadersCount) {
						source.setCrusaders(source.getCrusaders() - crusadersCount);
						destination.setCrusaders(destination.getCrusaders() + crusadersCount);
						movedUnits += crusadersCount;
						movedCrusaders += crusadersCount;
						
					}
					if (source.getAmazons() >= amazonsCount) {
						source.setAmazons(source.getAmazons() - amazonsCount);
						destination.setAmazons(destination.getAmazons() + amazonsCount);
						movedUnits += amazonsCount;
						movedAmazons += amazonsCount;
						
					}
					if (source.getCorsairs() >= corsairsCount) {
						source.setCorsairs(source.getCorsairs() - corsairsCount);
						destination.setCorsairs(destination.getCorsairs() + corsairsCount);
						movedUnits += corsairsCount;
						movedCorsairs += corsairsCount;
						
					}
					if (source.getBishops() >= bishopsCount) {
						source.setBishops(source.getBishops() - bishopsCount);
						destination.setBishops(destination.getBishops() + bishopsCount);
						movedUnits += bishopsCount;
						movedBishops += bishopsCount;
						
					}
					if (source.getNecromancers() >= necromancersCount) {
						source.setNecromancers(source.getNecromancers() - necromancersCount);
						destination.setNecromancers(destination.getNecromancers() + necromancersCount);
						movedUnits += necromancersCount;
						movedNecromancers += necromancersCount;
						
					}
					if (source.getArchitects() >= architectsCount) {
						source.setArchitects(source.getArchitects() - architectsCount);
						destination.setArchitects(destination.getArchitects() + architectsCount);
						movedUnits += architectsCount;
						movedArchitects += architectsCount;
						
					}
					
					if (GAME_MESSAGES && (movedUnits > 0)) System.out.println(source.getOwner().getDisplayName() + " moved " + movedUnits + " units (" + movedWarlocks + " Wa / " + movedCrusaders + " Cr / " + movedAmazons + " Am / " + movedCorsairs + " Co / " + movedBishops + " Bi / " + movedNecromancers + " Ne / " + movedArchitects + " Ar)");				
				}
			} else if (support.getCommand().equals("W")) {
				// Do nothing
			} else {
				if (DEBUG) System.out.println("Phase " + phase + " (Movement) : Invalid command by " + source.getOwner().getDisplayName() + "{" + source.getId() + "}");
			}
		} catch (Exception e) {
			if (DEBUG) {
				System.out.println("Exception in executeMovement() by " + source.getOwner().getDisplayName());
				e.printStackTrace();
			}
		}
	}

	private void executeTribute(Command support) {
		
		Town source = support.getSource();
		try {
			String[] args = support.getArgs();
			
			if (support.getCommand().equals("T") && args.length == 2) {
				
				int destinationId = Integer.parseInt(args[0]);
				Town destination = towns.get(towns.indexOf(new Town(destinationId)));
			
				if (destinationId != source.getId()) {
				
					int goldToMove = Math.max(0, Integer.parseInt(args[1]));
					int goldTransportCapacity = source.getCorsairs() * CORSAIR_TRANSPORT_RATIO;
					int goldAvailable = source.getGold();
					
					int goldToTransport = Math.min(goldAvailable, goldToMove);
					int goldTransported = Math.min(goldToTransport, goldTransportCapacity);
					
					if (goldTransported > 0) {
						
						source.setGold(source.getGold() - goldTransported);
						destination.setGold(destination.getGold() + goldTransported);
					
						if (GAME_MESSAGES && (!source.getOwner().equals(destination.getOwner()))) System.out.println(source.getOwner().getDisplayName() + " offered " + goldTransported + " gold in tribute to " + destination.getOwner().getDisplayName());				
						if (GAME_MESSAGES && (source.getOwner().equals(destination.getOwner()))) System.out.println(source.getOwner().getDisplayName() + " moved " + goldTransported + " gold between cities");
					}
				}
			} else if (support.getCommand().equals("W")) {
				// Do nothing
			} else {
				if (DEBUG) System.out.println("Phase " + phase + " (Movement) : Invalid command by " + source.getOwner().getDisplayName() + "{" + source.getId() + "}");
			}
		} catch (Exception e) {
			if (DEBUG) {
				System.out.println("Exception in executeMovement() by " + source.getOwner().getDisplayName());
				e.printStackTrace();
			}
		}
	}
}
