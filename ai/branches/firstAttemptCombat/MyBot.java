/*
    		for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
    			logger.debug(e.toString());
    		}

*/



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
 * AI bot for ants challenge
 * @author Euphony
 *
 */
public class MyBot extends Bot {
	
	
	//TO WORK ON CHANGE UNOCCUPIED IN addMove() ??? not sure how
	
	private Map<Tile, Tile> orders = new HashMap<Tile, Tile>(); // orders = (destination, current position)
	private Set<Tile> unseenTiles = new HashSet<Tile>();;
	private Set<Tile> enemyHills = new HashSet<Tile>();
	private Set<AntPath> hillGoals = new HashSet<AntPath>(); //enemy hill a* search attack paths
	private Set<AntPath> foodGoals = new HashSet<AntPath>(); // food a* search paths
	private Set<AntPath> unseenGoals = new HashSet<AntPath>(); // unseen a* search paths
	private int turnNumber = 0;
	private int sumOfTimeRemaining = 0;
	//Map<Tile, Map<Tile,Integer>> fightAnts = new HashMap<Tile, Map<Tile,Integer>>();
	//see populateFightAnts() for what fightAnts is
	
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
        /*
        //prevent stepping on own hill
        for (Tile myHill : ants.getMyHills()) {
        	orders.put(myHill, null);
        }*/

        Set<AntPath> hillGoals2 = new HashSet<AntPath>();
        
        //move along pre-made path to enemy hill
        for (AntPath antPath : hillGoals) {
        	//to implement : What if food disappears?
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
        
        //attack hills
        List<Path> hillPaths = new ArrayList<Path>();
        
        for (Tile antLoc : sortedAnts) {
        	if (!orders.containsValue(antLoc)) {
		        for (Tile hillLoc : enemyHills) {
		        	//if within view range attempt to attack
		        	if (ants.withinRange(antLoc, hillLoc, ants.getViewRadius2())) {
		        		List<Tile> directions = aSearchForRoute(antLoc, hillLoc, 500);
		        		if (directions == null) {
		        			continue;
		        		}
		        		
		        		//if aSearch didn't reach destination
		        		if (!(hillLoc.equals(directions.get(directions.size() - 1)))) {
		        			logger.debug("within view distance, failed to find hill | iterations : 500 |  : antLoc - " + antLoc + " hillLoc - " + hillLoc);
		        			continue;
		        		}
		        		/*
		        		if (antLoc.equals(new Tile(8, 58))) {
		        			logger.debug("Route = START : " + directions.get(0) + "  END : " + (directions.get(directions.size() - 1)));
		                	for (int i = 0; i < directions.size(); i++) {
		                		logger.debug("A* Route Tile : " + i + " =  : " + directions.get(i));
		                	}
		        		}*/
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
	        		if (ants.withinRange(antLoc, foodLoc, ants.getViewRadius2())) {
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
        		//logger.debug("ATTEMPTING TO MOVE TO FOOD from  " + path.getStart() + " TO - " + path.getEnd());
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
        		//else
        			//logger.debug("FAILED TO MOVE TO FOOD - from " + path.getStart() + " TO - " + path.getEnd());
        	}
        }
        

        
        Set<AntPath> unseenGoals2 = new HashSet<AntPath>();
        
        //move along pre-made path to unseen Goal
        for (AntPath antPath : unseenGoals) {
        	//to implement : What if food disappears?
        	
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
        	//DEFAULT explorationDepth = 1000
        	//int explorationDepth = 500;
        	int explorationDepth = 1000;
        	/*
        	if ((sortedAnts.size()) > 100) {
        		//DEFAULT = 300 for > 100
        		//explorationDepth = 100;
        		explorationDepth = 500;
        	}
        	
        	if ((sortedAnts.size() > 150)) {
        		explorationDepth = 200;
        	}*/
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
                	/*
                	logger.debug("Route = START : " + route.getStart() + "  END : " + route.getEnd());
                	for (int i = 0; i < directions.size(); i++) {
                		logger.debug("A* Route Tile : " + i + " =  : " + directions.get(i));
                	}*/
                	/*
                	if (f-score is too high?) {
                		logger.debug("SKIPPED ROUTE - start : " + route.getStart() + " end : " + route.getEnd() );
                		continue;
                	}*/
                	//else
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
        /*
        if (!timeRunningOut) {
        	populateBattles();
        	
        	for (Fight fight : Battles) {
        		Set<Tile> enemyAnts = fight.getEnemyAnts();
        		Set<Tile> buddyAnts = fight.getBuddyAnts();
        		if (enemyAnts.size() > buddyAnts.size()) {
        			retreat(fight);
        		}
        		else if (enemyAnts.size() == buddyAnts.size()) {
        			retreat(fight);
        		}
        		else {// buddies > enemies
        			attack(fight);
        		}
        	}
        }*/
        
        if (!timeRunningOut) {
        	Set<Fight> Battles = populateBattles();
        	
        	for (Fight f : Battles) {
        		retreat(f);
        		Tile oldMoveLoc = f.getTheAnt();
        		AntPath oldMoveAntPath = new AntPath(oldMoveLoc, null, 0);
        		
        		if (foodGoals.contains(oldMoveAntPath))
        			foodGoals.remove(oldMoveAntPath);
        		if (unseenGoals.contains(oldMoveAntPath))
        			unseenGoals.remove(oldMoveAntPath);
        		if (hillGoals.contains(oldMoveAntPath))
        			hillGoals.remove(oldMoveAntPath);
       		}
        	
        }
        
        doMoves();
        
        if (turnNumber != 1) {
        	sumOfTimeRemaining += ants.getTimeRemaining();
        }
 		logger.debug(" Time remaining : " + ants.getTimeRemaining());
    	logger.debug("average time remaining : " + (sumOfTimeRemaining / turnNumber));
        
    }//end turn
    
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
    
    private void retreat(Fight f) {
    	
    	Tile toMoveLoc = f.getTheAnt();
    	Set<Tile> enemyAnts = f.getEnemyAnts();
    	
    	Ants ants = getAnts();
    	int distanceToEnemies = 0;
		
		Tile currentLoc = orders.get(toMoveLoc);
		orders.remove(toMoveLoc);
		//logger.debug("in retreat : currentLoc - " + currentLoc + " toMoveLoc : " + toMoveLoc);
		
		for (Tile enemyAnt : enemyAnts) {
			distanceToEnemies += ants.getDistance(toMoveLoc,enemyAnt);
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
    		if (addMove(currentLoc, destLoc)) {
    			logger.debug("Retreat : from " + currentLoc + " to " + destLoc);
    			break;
    		}
    		//else ant should be stuck
    	}
    }
    
    private void attack(Fight fight) {
    	
    }
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
    			if (ants.withinRange(myAntNewLoc, enemyAnt, ants.getAttackRadius2())) {
    				enemyAnts.add(enemyAnt);
    				for (Tile myAntNewLoc2 : orders.keySet()) {
    					if (ants.withinRange(enemyAnt, myAntNewLoc2, ants.getAttackRadius2())) {
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
    
    private Set<Fight> populateBattles() {	
    	Ants ants = getAnts();
    	
    	Set<Fight> Battles = new HashSet<Fight>();
    	
    	for (Tile myAntNewLoc : orders.keySet()) {
        	int numberOfEnemyAnts = 0;
        	int numberOfBuddyAnts = 0;
        	Set<Tile> enemyAnts = new HashSet<Tile>();
        	
    		for (Tile enemyAnt : ants.getEnemyAnts()) {
    			if (ants.withinRange(myAntNewLoc, enemyAnt, ants.getViewRadius2())) {
    				numberOfEnemyAnts++;
    				enemyAnts.add(enemyAnt);
    			}
    		}
    		
    		for (Tile buddyAnt : orders.keySet()) {
    			if (ants.withinRange(myAntNewLoc, buddyAnt, ants.getViewRadius2())) {
    				numberOfBuddyAnts++;
    			}
    		}
    		
    		if (numberOfEnemyAnts > numberOfBuddyAnts + 1) {
    			Battles.add(new Fight(myAntNewLoc, enemyAnts));
    		}
    	}
    	
    	return Battles;
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

    	/*if (direction == null) {
    		logger.debug(" ERRROR !!! attempt to move failed. FROM : " + antLoc + "  TO : " + destLoc);
    	} else */
    	
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
    		
    	/*	if (antLoc.equals(new Tile(28, 19))) {
        		logger.debug("doMoveDirection : antLoc : " + antLoc + " newLoc " + ants.getTile(antLoc, direction));
        		for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
        			logger.debug(e.toString());
        		}
        	//}
    		//logger.debug("DoMoveDirection success : antLoc : " + antLoc + " newLoc " + ants.getTile(antLoc, direction));
    		 */
    }
    
    private boolean doRandomMovement (Tile antLoc) {

    	Ants ants = getAnts();
    	//generate random number between 0-3
    	int randomNumber = (int)(Math.random() * 4);
    	
    	//convert random number to enum
    	Aim newDirections[] = new Aim[Aim.values().length];
    	newDirections = Aim.values();
    	
    	for (int i = 0; i < newDirections.length; i++) {
    		
    		//logger.debug("RANDOM MOVE FROM : " + antLoc + "   TO : " + ants.getTile(antLoc, newDirections[(i + randomNumber) % 4]));
    		Tile destLoc = ants.getTile(antLoc, newDirections[(i + randomNumber) % 4]);
    		if (addMove(antLoc, destLoc)) {
    			return true;
    		}
    	}
    		
    	// stuck, all directions blocked
    //	logger.debug("STUCK! addMove failed - ant Loc : " + antLoc + " destLoc (Ilk) " + ants.getIlk(destLoc));
    	return false;
    }

    
    private List<Tile> aSearchForRoute (Tile startTile, Tile endTile, int iterations) {
    	//boolean flag = false;
    	
    	//if (startTile.equals(new Tile(13,55))) {
    		//flag = true;
    	//}
    	//logger.debug("new A* Search! startTile = " + startTile + "  endTile = " + endTile);
    	
    	Ants ants = getAnts();
		//int startTime = ants.getTimeRemaining();
    	
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
    		//logger.debug("inside main loop for A*Search - iteration : " + i + " - Time remaining : " + ants.getTimeRemaining());
    		/*
    		if ((startTime - ants.getTimeRemaining()) > 15) {
    			logger.debug(" WTF??? SPENT OVER 15 ms on one iteration!");
    		}
    		startTime = ants.getTimeRemaining();*/
    		
    		ArrayList<ASearchTile> openSetSorted = new ArrayList<ASearchTile>(openSet);
    		Collections.sort(openSetSorted, new F_scoreComparator());
    		
    		ASearchTile currentATile = openSetSorted.get(0);
    		Tile currentTile = currentATile.getTile();
    		
    		//logger.debug("currently looking at : " + currentATile);
    		
    		//if taking too long or at destination
    		if (i > iterations || currentTile.equals(endTile)) {
    			//logger.debug("F-score for this A-search : " + currentATile.getF_score());
    			//logger.debug("Time used for this A-search : " + (startTime - ants.getTimeRemaining()) + "  iterations = " + iterations);
    			
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
    		/*
    		if (flag){
    		
    		int k = 0;
    		for (ASearchTile a : openSet) {
    			logger.debug("open Set " + k++ + " : " + a);
        		for (Tile b : a.previousTiles) {
        			logger.debug("open Set Tile previous Tiles = " + b);
        		}
    		}
    		}*/
    		
    		//look at nearby tiles
    		for (Aim direction : Aim.values()) {
    			if (ants.getIlk(currentTile, direction).isPassable()) {
    				Tile nextTile = ants.getTile(currentTile, direction);
    				Route nextTileRoute = new Route (nextTile, endTile, ants.getDistance(nextTile, endTile));
    				g_score = currentATile.getG_score() + 1;
    				h_score = findH_score(startTile, nextTileRoute, dx2, dy2);
    				ASearchTile nextATile = new ASearchTile(nextTile, g_score, h_score, nextTilePreviousTiles);
    				/*
    				if (flag) {
    					logger.debug("nextATile to add to openSet = " + nextATile);
    				
    					int j = 0;
    					for (Tile t : closedSet) {
    						logger.debug("closedSet : " + j + " = " + t);
    						j++;
    					}
    				}*/
				
    				if (closedSet.contains(nextATile.getTile())) {
    					//logger.debug("closedSet.contains(nextATile) == true, nextAtile = " + nextATile);
    					continue;
    				}
    				//if not in open set, add to open set
    			
	    			else if (!openSet.contains(nextATile)) {
	    				//logger.debug("nextAtile being added to open Set : " + nextATile);

	    				openSet.add(nextATile);
	    			}
	    			
	    			//in open set
	    			else {
	    				/*logger.debug("in open set? : " + openSet.contains(nextATile));
	    				int l = 0;
	    				for (ASearchTile tile : openSet) {
	    					logger.debug("open set " + l++ + " : " + tile);
	    				}*/
	    				
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
    	
    	
    	/*List<Tile> failedSearch = new ArrayList<Tile>();
    	failedSearch.add(startTile);
    	failedSearch.add(endTile);
    	return failedSearch;*/
    	
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
	
	/*
	private boolean containsTile (ASearchTile theATile, Set<ASearchTile> theSet) {
		if (findASearchTile(theATile.getTile(), theSet) == null)
			return false;
		else
			return true;
	}*/
	/*
	private List<ASearchTile> returnPreviousTiles (List<ASearchTile> thePreviousTiles) {
	
		ASearchTile theATile = thePreviousTiles.get(0);
		
		if (theATile.getPrevious() == null) {
			thePreviousTiles.add(theATile);
			return thePreviousTiles;
		}
		else {
			List<ASearchTile> thePreviousTiles2 = new ArrayList<ASearchTile>();
			thePreviousTiles2.add(theATile.getPrevious());
			thePreviousTiles.add(returnPreviousTiles(thePreviousTiles2));
			return thePreviousTiles;
		}
	}
*/
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
