import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * AI bot for ants challenge at aichallenge.org
 * @version December 22, 2011
 * @author Abraham Taylor
 *
 */
public class MyBot extends Bot {
	
	private Map<Tile, Tile> orders = new HashMap<Tile, Tile>(); // orders = (destination, current position)
	private Set<Tile> unseenTiles = new HashSet<Tile>();;
	private Set<Tile> enemyHills = new HashSet<Tile>();
	private Set<AntPath> hillGoals = new HashSet<AntPath>(); //enemy hill a* search attack paths
	private Set<AntPath> foodGoals = new HashSet<AntPath>(); // food a* search paths
	private Set<AntPath> unseenGoals = new HashSet<AntPath>(); // unseen a* search paths
	private int turnNumber = -1;
	private int sumOfTimeRemaining = 0;
	
    /**
     * Main method executed by the game engine for starting the bot.
     * 
     * @param args command line arguments
     * 
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws IOException {
        new MyBot().readSystemInput();
    }
    
    /**
     * For every ant check every direction in fixed order (N, E, S, W) and move it if the tile is
     * passable.
     */
    @Override
    public void doTurn() {
    	
    	
    	logger.debug("START TURN #" + ++turnNumber);
    	
        boolean timeRunningOut = false;
    	Ants ants = getAnts();
        orders.clear();
        TreeSet<Tile> sortedAnts = new TreeSet<Tile>(ants.getMyAnts());
        
        // if an ant moves, it is dead in the tile it moved to.
               
        //BEGIN INITIALIZATION CODE
        
        //add new enemy hills to set
        for (Tile enemyHill : ants.getEnemyHills()) {
        	if (!enemyHills.contains(enemyHill)) {
        		enemyHills.add(enemyHill);
        	}
        }
        
        //add all locations to unseen tiles set, run every turn	
        for (int row = 0; row < ants.getRows(); row++) {
        	for (int col = 0; col < ants.getCols(); col++) {
        		unseenTiles.add(new Tile(row, col));
        	}
        }
        
        //remove any tiles that can be seen, run each turn
        for (Iterator<Tile> locIter = unseenTiles.iterator(); locIter.hasNext();) {
        	Tile next = locIter.next();
        	if (ants.isVisible(next)) {
        		locIter.remove();
        	}
        } 
        //END INITIALIZATION CODE

        Set<AntPath> hillGoals2 = new HashSet<AntPath>();
        
        //move along pre-made path to enemy hill
        for (AntPath antPath : hillGoals) {
        	//to implement : What if hill disappears?
        	int i = antPath.getPosition();
        	List<Tile> directions = antPath.getDirections();
        	
        	if (ants.getIlk(antPath.getAnt()) == Ilk.DEAD) {
        		continue;
        	}
        	
        	if (addMove(directions.get(i), directions.get(++i))) {
        		Tile theAnt = directions.get(i);
        		
        		//if not there yet, add new AntPath with new ant position
        		if (!(i == (directions.size() - 1))) {
        			AntPath newAntPath = new AntPath(theAnt, directions, i);
        			hillGoals2.add(newAntPath);
        		}
        		else
        			enemyHills.remove(theAnt);
        	}
        }
        
        hillGoals = hillGoals2;        
        List<Path> hillPaths = new ArrayList<Path>();
		
		//attack hills
		for (Tile antLoc : sortedAnts) {
        	if (!orders.containsValue(antLoc)) {
		        for (Tile hillLoc : enemyHills) {
		        	//if within view range attempt to attack
		        	if (ants.withinRange2(antLoc, hillLoc, ants.getViewRadius2())) {
		        		List<Tile> directions = aSearchForRoute(antLoc, hillLoc, 500);
		        		if (directions == null) {
		        			continue;
		        		}
		        		
		        		//if aSearch didn't reach destination
		        		if (!(hillLoc.equals(directions.get(directions.size() - 1)))) {
		        			logger.debug("within view distance, failed to find hill | iterations : 500 |  : antLoc - " + antLoc + " hillLoc - " + hillLoc);
		        			continue;
		        		}
		        		int distance = ants.getDistance(antLoc, hillLoc);
		        		Path path = new Path(antLoc, hillLoc, distance, directions);
		        		hillPaths.add(path);
		        	}
		       	}
        	}
        }
        
        Map<Tile, Tile> hillTargets = new HashMap<Tile, Tile>();
        Collections.sort(hillPaths, new PathLengthComparator());
        
      //actually move towards hill
        for (Path path : hillPaths) {
        	if (!hillTargets.containsKey(path.getEnd()) && !hillTargets.containsValue(path.getStart())) {
	        	if (addMove(path.getDirections().get(0), path.getDirections().get(1))) {
	        		hillTargets.put(path.getEnd(), path.getStart());
	        		
	        		if (path.getEnd().equals(path.getDirections().get(1))) {
	        			//logger.debug("ENEMY HILL RAZED : " + path.getEnd());
	        			enemyHills.remove(path.getEnd());
	        		}
	        		else {
	        			AntPath newHillGoal = new AntPath(path.getDirections().get(1), path.getDirections(), 1);
	        			hillGoals.add(newHillGoal);
	        		}
	        		
	        		AntPath ap = new AntPath(path.getStart(), null, 0);
	        			          		
	        		if (foodGoals.contains(ap)) {
	        			foodGoals.remove(ap);
	        		}
	        		if (unseenGoals.contains(ap)) {
	        			unseenGoals.remove(ap);
	        		}
	        	}
        	}
        }
        

        Set<AntPath> foodGoals2 = new HashSet<AntPath>();
        
        //move along pre-made path to food
        for (AntPath antPath : foodGoals) {
        	//to implement : What if food disappears?
        	
        	int i = antPath.getPosition();
        	List<Tile> directions = antPath.getDirections();
        	
        	if (ants.getIlk(antPath.getAnt()) == Ilk.DEAD) {
        		continue;
        	}
        
        	if (addMove(directions.get(i), directions.get(++i))) {
        		Tile theAnt = directions.get(i);
        		
        		//if not there yet, add new AntPath with new ant position
        		if (!(i == (directions.size() - 2))) {
        			AntPath newAntPath = new AntPath(theAnt, directions, i);
        			foodGoals2.add(newAntPath);
        		}
        	}
        }
        
        foodGoals = foodGoals2;
        
        //find close food
        Map<Tile, Tile> foodTargets = new HashMap<Tile, Tile>();
        List<Path> foodPaths = new ArrayList<Path>();
        TreeSet<Tile> sortedFood = new TreeSet<Tile>(ants.getFoodTiles());
    	
        for (Tile antLoc : sortedAnts) {
        	if (!orders.containsValue(antLoc)) {
	        	for (Tile foodLoc : sortedFood) {
	        		//if A* search finds distance is less than view range distance then gather it
	        		if (ants.withinRange2(antLoc, foodLoc, ants.getViewRadius2())) {
	        			int distance = ants.getDistance(antLoc, foodLoc);
		        		List<Tile> directions = aSearchForRoute(antLoc, foodLoc, 50);
		        		if (directions == null) {
		        			continue;
		        		}
		        		if (!(directions.get(directions.size() - 1)).equals(foodLoc)) {
		        			//logger.debug("skipped food for depth 50 - " + foodLoc + " for ant : " + antLoc);
		        			continue;
		        		}
		    			Path path = new Path(antLoc, foodLoc, distance, directions);
		        		foodPaths.add(path);
	        		}
	    		}
        	}
        }
        
        Collections.sort(foodPaths, new PathLengthComparator());
        
        //move ants to food
        for (Path path : foodPaths) {
        	if (!foodTargets.containsKey(path.getEnd()) && !foodTargets.containsValue(path.getStart())) {
        		List<Tile> theDirections = path.getDirections();
        		Tile destination = theDirections.get(1);
        		if (addMove(theDirections.get(0), destination)) {
        			foodTargets.put(path.getEnd(), path.getStart());
        			if (theDirections.size() > 3) {
        				AntPath antPath = new AntPath(destination, path.getDirections(), 1);
        				foodGoals.add(antPath);
        			}
        			AntPath ap = new AntPath(path.getStart(), null, 0);
        			if (unseenGoals.contains(ap)) {
            			unseenGoals.remove(ap);
            		}
        		}
        	}
        }
        
        Set<AntPath> unseenGoals2 = new HashSet<AntPath>();
        
        //move along pre-made path to unseen Goal
        for (AntPath antPath : unseenGoals) {
        	//to implement : What if goal disappears?
   
        	int i = antPath.getPosition();
        	List<Tile> directions = antPath.getDirections();
        	
        	if (ants.getIlk(antPath.getAnt()) == Ilk.DEAD) {
        		continue;
        	}
        
        	if (addMove(directions.get(i), directions.get(++i))) {
        		Tile theAnt = directions.get(i);
        		
        		//if not there yet, add new AntPath with new ant position
        		if (!(i == (directions.size() - 1))) {
        			AntPath newAntPath = new AntPath(theAnt, directions, i);
        			unseenGoals2.add(newAntPath);
        		}
        	}
        }
        // abandon old goals, replace with updated one
        unseenGoals = unseenGoals2;
        
        
        //explore unseen areas
        for (Tile antLoc : sortedAnts) {
        	//DEFAULT explorationDepth = 1000. This limits A* search depth
        	int explorationDepth = 1000;
        	
        	if (ants.getTimeRemaining() <= 45) {
        		timeRunningOut = true;
        		logger.debug("ERROR! ran out of time");
        		break;
        	}
        	if (!orders.containsValue(antLoc)) {
        		List<Route> unseenRoutes = new ArrayList<Route>();
        		for (Tile unseenLoc : unseenTiles) {
        			//logger.debug("unseen Location : " + unseenLoc);
        			int distance = ants.getDistance(antLoc, unseenLoc);
        			Route route = new Route(antLoc, unseenLoc, distance);
        			unseenRoutes.add(route);
        		}
        		Collections.sort(unseenRoutes);
        		for (Route route : unseenRoutes) {
        			//logger.debug("Calling ASearch # : " + ++counter + " time this turn.");
        			
                	List<Tile> directions = aSearchForRoute(route.getStart(), route.getEnd(), explorationDepth);
                	if (directions == null) {
                		continue;
                	}
                	
                	if (addMove(directions.get(0),directions.get(1))) {
        				//logger.debug("Order issued : " + bestRoute.get(0) + " to " + bestRoute.get(1));
                		if (directions.size() > 2) {
                			AntPath unseenAntPath = new AntPath(directions.get(1), directions, 1);
                			unseenGoals.add(unseenAntPath);
                		}
                		
        				break;
                	}
                	else {
        				//logger.debug("Attempt to move from " + bestRoute.get(0) + " to " + bestRoute.get(1) + "  FAILED.");
                			break;
                	}
        		}
        	}
        }
        
        
        if (!timeRunningOut) {
	    //do random movement for remaining ants
        	for (Tile antLoc : sortedAnts) {
	        	if (!orders.containsValue(antLoc)) {
	        		doRandomMovement(antLoc);
	        	}
	        }
        }
        
		//do fighting algorithm if still have time
        if (!timeRunningOut) {// TO ADD : time limit!! ants.getTimeRemaining();
			//go through whole map and decide whether each square is SAFE, KILL (opponent), or DIE
        	Status[][][] statuses = calculateStatuses();
        	Map<Tile, Tile> ordersCopy = new HashMap<Tile, Tile>();
        	ordersCopy.putAll(orders);
			//only move to safe squares
        	for (Tile toMoveLoc : ordersCopy.keySet()) {
        		if (statuses[0][toMoveLoc.getRow()][toMoveLoc.getCol()] == Status.DIE
        				|| statuses[0][toMoveLoc.getRow()][toMoveLoc.getCol()] == Status.KILL) {
            		
        			Tile antLoc = ordersCopy.get(toMoveLoc);
        			orders.remove(toMoveLoc);
        			
        	    	//generate random number between 0-3
        	    	int randomNumber = (int)(Math.random() * 4);
        	    	Aim newDirections[] = new Aim[Aim.values().length];
        	    	newDirections = Aim.values();
        	    	
        	    	for (int i = 0; i < newDirections.length; i++) {
        	    		Tile destLoc = ants.getTile(antLoc, newDirections[(i + randomNumber) % 4]);
        	    		
        	    		if (statuses[0][destLoc.getRow()][destLoc.getCol()] != Status.DIE
                				&& statuses[0][destLoc.getRow()][destLoc.getCol()] != Status.KILL) {
                				    if (addMove(antLoc, destLoc)) {
                						break;
                					}
                				}
        	    	}
        			
        			AntPath oldMoveAntPath = new AntPath(toMoveLoc, null, 0);
            		
            		if (foodGoals.contains(oldMoveAntPath))
            			foodGoals.remove(oldMoveAntPath);
            		if (unseenGoals.contains(oldMoveAntPath))
            			unseenGoals.remove(oldMoveAntPath);
            		if (hillGoals.contains(oldMoveAntPath))
            			hillGoals.remove(oldMoveAntPath);
        		}
        	}
        }
        
        doMoves();
        
        
        if (turnNumber > 0) {
        	sumOfTimeRemaining += ants.getTimeRemaining();
     		logger.debug(" Time remaining : " + ants.getTimeRemaining());
        	logger.debug("average time remaining : " + (sumOfTimeRemaining / turnNumber));
        }

        
    }//end turn
    
	//this commented code is a not-finished attempt at retreating from the enemy. Never implemented
    /*
    private void retreat(Fight fight) {
    	Ants ants = getAnts();
    	
    	Set<Tile> buddyAnts = fight.getBuddyAnts();
    	Set<Tile> enemyAnts = fight.getEnemyAnts();
    	
    	
    	//Map<Tile, Integer> buddyAntsDistances = new HashMap<Tile, Integer>();
    	
    	for (Tile buddyAnt : buddyAnts) {
    		int distanceToEnemies = 0;
    		if (orders.get(buddyAnt) == null) {
    			logger.debug("buddyAnt : " + buddyAnt + " not in orders");
    		}
    		Tile currentLoc = new Tile(orders.get(buddyAnt).getRow(), orders.get(buddyAnt).getCol());
    		
    		for (Tile enemyAnt : enemyAnts) {
    			distanceToEnemies += ants.getDistance(buddyAnt,enemyAnt);
    		}
    		//buddyAntsDistances.put(buddyAnt, distanceToEnemies);
    		
    		List<AntDistance> newLocations = new ArrayList<AntDistance>();
    
        	for (Aim direction : Aim.values()) {
        		Tile newLoc = ants.getTile(currentLoc, direction);
        		int newDistanceToEnemies = 0;
        		for (Tile enemyAnt : enemyAnts) {
        			newDistanceToEnemies += ants.getDistance(newLoc, enemyAnt);
        		}
        		int distanceDifference = newDistanceToEnemies - distanceToEnemies;
        		newLocations.add(new AntDistance(newLoc, distanceDifference));
        	}
        	
        	Collections.sort(newLocations);
        	
        	for (AntDistance aD : newLocations) {
        		Tile destLoc = aD.getTile();
        		orders.remove(buddyAnt);
        		if (addMove(currentLoc, destLoc)) {
        			logger.debug("Retreat : from " + currentLoc + " to " + destLoc);
        			break;
        		}
        	}
    	}    	
    }*/
   
    private void attack(Fight fight) {
    	
    }
    // this is a not-finished attempt at doing something similar to an alpha-beta max-min tree search. never implemented.
	/*
    private void populateBattles() {
    	//TO DO: find out who will be in this fight with numbers. just need buddies number and friends number.
    	
    	Ants ants = getAnts();
    	//Map<Tile, Map<Tile,Integer>> fightAnts = new HashMap<Tile, Map<Tile,Integer>>();
    	//fightAnts = Map<MyAnt, enemyAntsInRangeOfIt>
    	//enemyAntsInRange = Map<enemyAnt, MyAntsInRangeOfIt>
    	//Map<Tile,Integer> enemyAnts = new HashMap<Tile,Integer>();
    	//enemyAnts.put(enemyAnt, buddyAnts);
    	/*if (!enemyAnts.isEmpty()) {
    			fightAnts.put(myAntNewLoc, enemyAnts);
    		}
    	
    	
    	
    	for (Tile myAntNewLoc : orders.keySet()) {
    		Set<Tile> buddyAnts = new HashSet<Tile>();
        	Set<Tile> enemyAnts = new HashSet<Tile>();
        	
    		for (Tile enemyAnt : ants.getEnemyAnts()) {
    			if (ants.withinRange2(myAntNewLoc, enemyAnt, ants.getAttackRadius2())) {
    				enemyAnts.add(enemyAnt);
    				for (Tile myAntNewLoc2 : orders.keySet()) {
    					if (ants.withinRange2(enemyAnt, myAntNewLoc2, ants.getAttackRadius2())) {
    						buddyAnts.add(myAntNewLoc2);
    					}
    				}
    			}
    		}
    		
    		if (enemyAnts.size() > 0) {
    			Fight fight = new Fight(buddyAnts, enemyAnts);
    			boolean added = Battles.add(new Fight(buddyAnts, enemyAnts));
    			logger.debug("Fight list : " + fight);
    			logger.debug("Is added? " + added);
    		}
    	}
    }*/
    
	//go through whole map, and for each tile on the map, determine whether it is safe, I will kill enemy
	//ants, or I will die. Do this based on the toughest enemy in each area, based on the total number of ants
	//the best ant in each area is fighting.
    private Status[][][] calculateStatuses() {
    	Ants ants = getAnts();
    	int[][][] influence = new int[ants.getPlayers()][ants.getRows()][ants.getCols()]; // represents what each ant could attack in one move : 12 = max # of players
    	int[][] totalInfluence = new int[ants.getRows()][ants.getCols()];
    	int[][][] fighting = new int[ants.getPlayers()][ants.getRows()][ants.getCols()]; // the total number of ants the best ant is fighting
    	Status[][][] statuses = new Status[ants.getPlayers()][ants.getRows()][ants.getCols()];
    	//initialize fighting to impossibly high number of ants to be fighting
    	for (int i = 0; i < ants.getPlayers() ; i++) {
    		for (int j = 0; j < ants.getRows() ; j++) {
    			for (int k = 0; k < ants.getCols(); k++) {
    				fighting[i][j][k] = 1000;
    			}
    		}
    	}
    	
    	Set<Ant> allAnts = ants.getAllAnts();
    	
    	//for each possible move for an ant, find all Tiles within attack radius and add to influence
    	for (Ant theAnt : allAnts) {
    		Set<Tile> possibleInfluence = new HashSet<Tile>();
    		for (Aim direction : Aim.values()) {
    			Tile possibleMove = ants.getTile(theAnt, direction);	
    			Set<Tile> influenceOneDirection = ants.tilesWithinRange2(possibleMove, ants.getAttackRadius2());
    			for (Tile t : influenceOneDirection) {
    				possibleInfluence.add(t);
    			}
    		}
    		for (Tile influenceTile : possibleInfluence) {
    			influence[theAnt.getOwner()][influenceTile.getRow()][influenceTile.getCol()]++;
    			totalInfluence[influenceTile.getRow()][influenceTile.getCol()]++;
    		}
    	}
    	
    	for (Ant theAnt : allAnts) {
    		for (Aim direction : Aim.values()) {
    			Tile possibleMove = ants.getTile(theAnt, direction);
	    		Set<Tile> attackTiles = ants.tilesWithinRange2(possibleMove, ants.getAttackRadius2());
	    		int owner = theAnt.getOwner();
	    		for (Tile attackTile : attackTiles) {
	    			int row = attackTile.getRow();
	    			int col = attackTile.getCol();
	    			int enemies = totalInfluence[row][col] - influence[owner][row][col];
	    			if (fighting[owner][row][col] > enemies) {
	    				fighting[owner][row][col] = enemies;
	    			}
	    		}
    		}
    	}
    	
    	for (Ant theAnt : allAnts) {
			int owner = theAnt.getOwner();
    		
    		//calculate status fighting against best enemy for all directions
    		for (Aim direction : Aim.values()) {
    			Tile possibleMove = ants.getTile(theAnt, direction);
    			int row = possibleMove.getRow();
    			int col = possibleMove.getCol();
    			
    			int bestEnemy = 1000;
    			
    			for (int i = 0; i < ants.getPlayers(); i++) {
    				if (owner == i) {
    					continue;
    				}
    				if (fighting[i][row][col] < bestEnemy) {
    					bestEnemy = fighting[i][row][col];
    				}
    			}
    			if (bestEnemy < fighting[owner][row][col]) {
    				statuses[owner][row][col] = Status.DIE;
    			}
    			else if (bestEnemy == fighting[owner][row][col]) {
    				statuses[owner][row][col] = Status.KILL;
    			}
    			else {
    				statuses[owner][row][col] = Status.SAFE;
    			}
    		}
 
    		//calculate status fighting against best enemy staying in current position
    		
    		int row = theAnt.getRow();
			int col = theAnt.getCol();
			int bestEnemy = 1000;
			
			for (int i = 0; i < ants.getPlayers(); i++) {
				if (owner == i) {
					continue;
				}
				if (fighting[i][row][col] < bestEnemy) {
					bestEnemy = fighting[i][row][col];
				}
			}
			if (bestEnemy < fighting[owner][row][col]) {
				statuses[owner][row][col] = Status.DIE;
			}
			else if (bestEnemy == fighting[owner][row][col]) {
				statuses[owner][row][col] = Status.KILL;
			}
			else {
				statuses[owner][row][col] = Status.SAFE;
			}
    	}
    	
    	return statuses;
    	
    }

    
    private void doMoves() {
    	Ants ants = getAnts();

    	for (Map.Entry<Tile, Tile> entry : orders.entrySet()) {
    	    Tile destLoc = entry.getKey();
    	    Tile antLoc = entry.getValue();
    	    Aim direction = ants.getDirection(antLoc, destLoc);
    	    /*if (direction == null) {
    	    	logger.debug("attempt to move : " + antLoc + " - " + destLoc);
    	    }*/
    	    ants.issueOrder(antLoc, direction);
    	}
    }

    
    private boolean addMove(Tile antLoc, Tile destLoc) {
    	Ants ants = getAnts();
    	
    	if (ants.getIlk(destLoc).isUnoccupied() && !orders.containsKey(destLoc)) {
    		orders.put(destLoc, antLoc);
    		/*logger.debug("Add to orders : antLoc - " + antLoc + " destLoc - " + destLoc);
    		for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
    			logger.debug(e.toString());
    		}*/
    		return true;
    	}
    	else
    		return false;
    }
    
    private boolean doRandomMovement (Tile antLoc) {

    	Ants ants = getAnts();
    	//generate random number between 0-3
    	int randomNumber = (int)(Math.random() * 4);
    	
    	//convert random number to enum
    	Aim newDirections[] = new Aim[Aim.values().length];
    	newDirections = Aim.values();
    	
    	for (int i = 0; i < newDirections.length; i++) {
    		Tile destLoc = ants.getTile(antLoc, newDirections[(i + randomNumber) % 4]);
    		if (addMove(antLoc, destLoc)) {
    			return true;
    		}
    	}
    		
    	// stuck, all directions blocked
    	return false;
    }
    
	// A* search, iterations determines depth
    private List<Tile> aSearchForRoute (Tile startTile, Tile endTile, int iterations) {
    	
    	Ants ants = getAnts();
   
    	Route route = new Route(startTile, endTile, ants.getDistance(startTile, endTile));
    	
    	Set<Tile> closedSet = new HashSet<Tile>(); //already evaluated
    	Set<ASearchTile> openSet = new HashSet<ASearchTile>();  // tentative nodes to evaluate
    	
    	//tie-breaking code for h_score
    	int dx2 = Math.abs(startTile.getRow() - endTile.getRow());
        dx2 = Math.min(dx2, ants.getRows() - dx2);        
        int dy2 = Math.abs(startTile.getCol() - endTile.getCol());
        dy2 = Math.min(dy2, ants.getCols() - dy2);
    	
    	int g_score = 0; //cost from start along best known path
    	double h_score = findH_score(startTile, route, dx2, dy2); //guess of distance to end
    	
    	List<Tile> previousTiles = new ArrayList<Tile>();
    	
    	openSet.add(new ASearchTile(startTile, g_score, h_score, previousTiles));
    	
    	for (int i = 0; !openSet.isEmpty(); i++) {
    		ArrayList<ASearchTile> openSetSorted = new ArrayList<ASearchTile>(openSet);
    		Collections.sort(openSetSorted, new F_scoreComparator());
    		
    		ASearchTile currentATile = openSetSorted.get(0);
    		Tile currentTile = currentATile.getTile();
    		
    		//logger.debug("currently looking at : " + currentATile);
    		
    		//if taking too long or at destination
    		if (i > iterations || currentTile.equals(endTile)) {
    			//logger.debug("F-score for this A-search : " + currentATile.getF_score());    			
    			currentATile.previousTiles.add(currentTile);
    			return currentATile.previousTiles;
    		}
    		
    		else {
    			//logger.debug("add currentTile to closed Set : " + currentTile);
    			openSet.remove(currentATile);
    			closedSet.add(currentATile.getTile());
    		}
    		//add currentTile to nextTilePreviousTiles for insertion
    		List<Tile> nextTilePreviousTiles = new ArrayList<Tile>();
    		for (Tile t : currentATile.previousTiles) {
    			nextTilePreviousTiles.add(t);
    		}
    		
    		nextTilePreviousTiles.add(currentTile);
    		
    		//look at nearby tiles
    		for (Aim direction : Aim.values()) {
    			if (ants.getIlk(currentTile, direction).isPassable()) {
    				Tile nextTile = ants.getTile(currentTile, direction);
    				Route nextTileRoute = new Route (nextTile, endTile, ants.getDistance(nextTile, endTile));
    				g_score = currentATile.getG_score() + 1;
    				h_score = findH_score(startTile, nextTileRoute, dx2, dy2);
    				ASearchTile nextATile = new ASearchTile(nextTile, g_score, h_score, nextTilePreviousTiles);
				
    				if (closedSet.contains(nextATile.getTile())) {
    					continue;
    				}
					
    				//if not in open set, add to open set
	    			else if (!openSet.contains(nextATile)) {
	    				openSet.add(nextATile);
	    			}
	    			
	    			//in open set
	    			else {
	    				ASearchTile existingATile = findASearchTile(nextATile.getTile(), openSet);
	    				
	    				//replace new route of open tile if this path is faster
	    				if (nextATile.getG_score() < existingATile.getG_score()) {
	    					/*if (flag) {
	    						logger.debug("G_SCORE TEST - nextATile : " + nextATile + "   existingATile : " + existingATile);
	    						logger.debug("existingATile.previousTiles = nextATile.previousTiles");
	    					}*/
	    					existingATile.setG_score(nextATile.getG_score());
	    					existingATile.previousTiles = nextATile.previousTiles;
	    				}
	    			}
    			}
    		}//end for different surrounding directions
    	}//end while emptysetisEmpty
    	
    	logger.debug("A* search failed for : startTile - " + startTile + " endTile - " + endTile);
    	//failed Search, return null
    	return null;
    	
    }//endaSearchForRoute()
    	
	private ASearchTile findASearchTile (Tile theTile, Set<ASearchTile> theSet) {
		for (ASearchTile Atile : theSet) {
			if (theTile.equals(Atile.getTile())) {
				return Atile;
			}
		}
		return null;
	}
	
    private double findH_score(Tile startTile, Route route, int dx2, int dy2){
    	//tie-breaking code, prefers straight lines
    	Ants ants = getAnts();
    	Tile currentTile = route.getStart();
    	Tile endTile = route.getEnd();
    	
        int dx1 = Math.abs(currentTile.getRow() - endTile.getRow());
        dx1 = Math.min(dx1, ants.getRows() - dx1);
        int dy1 = Math.abs(currentTile.getCol() - endTile.getCol());
        dy1 = Math.min(dy1, ants.getCols() - dy1);
        
        double cross_product = Math.abs(dx1*dy2 - dx2*dy1);
    	
        //return getDistance (manhattan distance) * tie-breaker
    	return route.getDistance() + cross_product * 0.01;
    }
}
