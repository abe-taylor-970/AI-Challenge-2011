
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds all game data and current game state.
 */
public class Ants {
    /** Maximum map size. */
    public static final int MAX_MAP_SIZE = 256 * 2;

    private final int loadTime;

    private final int turnTime;

    private final int rows;

    private final int cols;

    private final int turns;

    private final int viewRadius2;

    private final int attackRadius2;

    private final int spawnRadius2;

    private final boolean visible[][];

    private final Set<Tile> visionOffsets;

    private long turnStartTime;
    
    private int players = 1;

    private final Ilk map[][];

    private final Set<Tile> myAnts = new HashSet<Tile>();

    private final Set<Ant> enemyAnts = new HashSet<Ant>();
    
    private final Set<Ant> allAnts = new HashSet<Ant>();

    private final Set<Tile> myHills = new HashSet<Tile>();

    private final Set<Tile> enemyHills = new HashSet<Tile>();

    private final Set<Tile> foodTiles = new HashSet<Tile>();

    private final Set<Order> orders = new HashSet<Order>();

    /**
     * Creates new {@link Ants} object.
     * 
     * @param loadTime timeout for initializing and setting up the bot on turn 0
     * @param turnTime timeout for a single game turn, starting with turn 1
     * @param rows game map height
     * @param cols game map width
     * @param turns maximum number of turns the game will be played
     * @param viewRadius2 squared view radius of each ant
     * @param attackRadius2 squared attack radius of each ant
     * @param spawnRadius2 squared spawn radius of each ant
     */
    public Ants(int loadTime, int turnTime, int rows, int cols, int turns, int viewRadius2,
            int attackRadius2, int spawnRadius2) {
        this.loadTime = loadTime;
        this.turnTime = turnTime;
        this.rows = rows;
        this.cols = cols;
        this.turns = turns;
        this.viewRadius2 = viewRadius2;
        this.attackRadius2 = attackRadius2;
        this.spawnRadius2 = spawnRadius2;
        map = new Ilk[rows][cols];
        for (Ilk[] row : map) {
            Arrays.fill(row, Ilk.LAND);
        }
        visible = new boolean[rows][cols];
        for (boolean[] row : visible) {
            Arrays.fill(row, false);
        }
        // calc vision offsets
        visionOffsets = new HashSet<Tile>();
        int mx = (int)Math.sqrt(viewRadius2);
        for (int row = -mx; row <= mx; ++row) {
            for (int col = -mx; col <= mx; ++col) {
                int d = row * row + col * col;
                if (d <= viewRadius2) {
                    visionOffsets.add(new Tile(row, col));
                }
            }
        }
    }
    
    public Set<Tile> tilesWithinRange2(Tile ant, int range2) {
    	Set<Tile> theTilesInRange = new HashSet<Tile>();
		double range = Math.sqrt(range2);
    	for (int i = 0; i <= range; i++) {
    		for (int j = 0; j <= range; j++) {
    			Tile tile1 = getTile(ant, new Tile(i, j));
    			if (withinRange2(ant, tile1, range2)) {
    				theTilesInRange.add(tile1);
    			}
    			Tile tile2 = getTile(ant, new Tile(i, -j));
    			if (withinRange2(ant, tile2, range2)) {
    				theTilesInRange.add(tile2);
    			}
    			Tile tile3 = getTile(ant, new Tile(-i, j));
    			if (withinRange2(ant, tile3, range2)) {
    				theTilesInRange.add(tile3);
    			}
    			Tile tile4 = getTile(ant, new Tile(-i, -j));
    			if (withinRange2(ant, tile4, range2)) {
    				theTilesInRange.add(tile4);
    			}
    		}
    	}
    	return theTilesInRange;
    }
    
 
    /**
     * returns whether object is within ant's range2
     * @param ant
     * @param object
     * @return
     */
    
    public boolean withinRange2 (Tile ant, Tile object, int range2) {
    	int rowDelta = Math.abs(ant.getRow() - object.getRow());
    	int colDelta = Math.abs(ant.getCol() - object.getCol());
    	rowDelta = Math.min(rowDelta, rows - rowDelta);
        colDelta = Math.min(colDelta, cols - colDelta);
        int distance2 = rowDelta * rowDelta + colDelta * colDelta;
    	
    	if (distance2 <= range2)
    		return true;
    	else
    		return false;
    }

    public double getDistanceAtViewRange2 () {
    	return 2 * Math.sqrt(viewRadius2 / 2);
    }
    
    public double getDistanceAtAttackRange2 () {
    	return 2 * Math.sqrt(attackRadius2 / 2);
    }
    
    /**
     * Returns timeout for initializing and setting up the bot on turn 0.
     * 
     * @return timeout for initializing and setting up the bot on turn 0
     */
    public int getLoadTime() {
        return loadTime;
    }

    /**
     * Returns timeout for a single game turn, starting with turn 1.
     * 
     * @return timeout for a single game turn, starting with turn 1
     */
    public int getTurnTime() {
        return turnTime;
    }

    /**
     * Returns game map height.
     * 
     * @return game map height
     */
    public int getRows() {
        return rows;
    }

    /**
     * Returns game map width.
     * 
     * @return game map width
     */
    public int getCols() {
        return cols;
    }

    /**
     * Returns maximum number of turns the game will be played.
     * 
     * @return maximum number of turns the game will be played
     */
    public int getTurns() {
        return turns;
    }

    /**
     * Returns squared view radius of each ant.
     * 
     * @return squared view radius of each ant
     */
    public int getViewRadius2() {
        return viewRadius2;
    }

    /**
     * Returns squared attack radius of each ant.
     * 
     * @return squared attack radius of each ant
     */
    public int getAttackRadius2() {
        return attackRadius2;
    }

    /**
     * Returns squared spawn radius of each ant.
     * 
     * @return squared spawn radius of each ant
     */
    public int getSpawnRadius2() {
        return spawnRadius2;
    }
    
    public int getPlayers() {
    	return players;
    }

    /**
     * Sets turn start time.
     * 
     * @param turnStartTime turn start time
     */
    public void setTurnStartTime(long turnStartTime) {
        this.turnStartTime = turnStartTime;
    }

    /**
     * Returns how much time the bot has still has to take its turn before timing out.
     * 
     * @return how much time the bot has still has to take its turn before timing out
     */
    public int getTimeRemaining() {
        return turnTime - (int)(System.currentTimeMillis() - turnStartTime);
    }

    /**
     * Returns ilk at the specified location.
     * 
     * @param tile location on the game map
     * 
     * @return ilk at the <cod>tile</code>
     */
    public Ilk getIlk(Tile tile) {
        return map[tile.getRow()][tile.getCol()];
    }

    /**
     * Sets ilk at the specified location.
     * 
     * @param tile location on the game map
     * @param ilk ilk to be set at <code>tile</code>
     */
    public void setIlk(Tile tile, Ilk ilk) {
        map[tile.getRow()][tile.getCol()] = ilk;
    }

    /**
     * Returns ilk at the location in the specified direction from the specified location.
     * 
     * @param tile location on the game map
     * @param direction direction to look up
     * 
     * @return ilk at the location in <code>direction</code> from <cod>tile</code>
     */
    public Ilk getIlk(Tile tile, Aim direction) {
        Tile newTile = getTile(tile, direction);
        return map[newTile.getRow()][newTile.getCol()];
    }

    /**
     * Returns location in the specified direction from the specified location.
     * 
     * @param tile location on the game map
     * @param direction direction to look up
     * 
     * @return location in <code>direction</code> from <cod>tile</code>
     */
    public Tile getTile(Tile tile, Aim direction) {
        int row = (tile.getRow() + direction.getRowDelta()) % rows;
        if (row < 0) {
            row += rows;
        }
        int col = (tile.getCol() + direction.getColDelta()) % cols;
        if (col < 0) {
            col += cols;
        }
        return new Tile(row, col);
    }

    /**
     * Returns location with the specified offset from the specified location.
     * 
     * @param tile location on the game map
     * @param offset offset to look up
     * 
     * @return location with <code>offset</code> from <cod>tile</code>
     */
    public Tile getTile(Tile tile, Tile offset) {
        int row = (tile.getRow() + offset.getRow()) % rows;
        if (row < 0) {
            row += rows;
        }
        int col = (tile.getCol() + offset.getCol()) % cols;
        if (col < 0) {
            col += cols;
        }
        return new Tile(row, col);
    }

    /**
     * Returns a set containing all my ants locations.
     * 
     * @return a set containing all my ants locations
     */
    public Set<Tile> getMyAnts() {
        return myAnts;
    }

    /**
     * Returns a set containing all enemy ants locations.
     * 
     * @return a set containing all enemy ants locations
     */
    public Set<Ant> getEnemyAnts() {
        return enemyAnts;
    }

    public Set<Ant> getAllAnts() {
    	return allAnts;
    }
    /**
     * Returns a set containing all my hills locations.
     * 
     * @return a set containing all my hills locations
     */
    public Set<Tile> getMyHills() {
        return myHills;
    }

    /**
     * Returns a set containing all enemy hills locations.
     * 
     * @return a set containing all enemy hills locations
     */
    public Set<Tile> getEnemyHills() {
        return enemyHills;
    }

    /**
     * Returns a set containing all food locations.
     * 
     * @return a set containing all food locations
     */
    public Set<Tile> getFoodTiles() {
        return foodTiles;
    }

    /**
     * Returns all orders sent so far.
     * 
     * @return all orders sent so far
     */
    public Set<Order> getOrders() {
        return orders;
    }

    /**
     * Returns true if a location is visible this turn
     *
     * @param tile location on the game map
     *
     * @return true if the location is visible
     */
    public boolean isVisible(Tile tile) {
        return visible[tile.getRow()][tile.getCol()];
    }

    /**
     * Calculates distance between two locations on the game map. (manhattan distance)
     * 
     * @param t1 one location on the game map
     * @param t2 another location on the game map
     * 
     * @return distance between <code>t1</code> and <code>t2</code>
     */
    public int getDistance(Tile t1, Tile t2) {
        int rowDelta = Math.abs(t1.getRow() - t2.getRow());
        int colDelta = Math.abs(t1.getCol() - t2.getCol());
        rowDelta = Math.min(rowDelta, rows - rowDelta);
        colDelta = Math.min(colDelta, cols - colDelta);
        return rowDelta + colDelta;
    }

    /**
     * Returns orthogonal direction from one location to the another.
     * 
     * @param t1 one location on the game map (Start location)
     * @param t2 another location on the game map (End location)
     * 
     * @return orthogonal directions from <code>t1</code> to <code>t2</code>
     */
    public Aim getDirection(Tile t1, Tile t2) {
        if (t1.getRow() < t2.getRow()) {
            if (t2.getRow() - t1.getRow() >= rows / 2) {
                return Aim.NORTH;
            } else {
                return Aim.SOUTH;
            }
        } else if (t1.getRow() > t2.getRow()) {
            if (t1.getRow() - t2.getRow() >= rows / 2) {
                return Aim.SOUTH;
            } else {
                return Aim.NORTH;
            }
        }
        if (t1.getCol() < t2.getCol()) {
            if (t2.getCol() - t1.getCol() >= cols / 2) {
                return Aim.WEST;
            } else {
                return Aim.EAST;
            }
        } else if (t1.getCol() > t2.getCol()) {
            if (t1.getCol() - t2.getCol() >= cols / 2) {
                return Aim.EAST;
            } else {
                return Aim.WEST;
            }
        }
        return null;
    }

    /**
     * Clears game state information about my ants locations.
     */
    public void clearMyAnts() {
        for (Tile myAnt : myAnts) {
            map[myAnt.getRow()][myAnt.getCol()] = Ilk.LAND;
        }
        myAnts.clear();
    }

    /**
     * Clears game state information about enemy ants locations.
     */
    public void clearEnemyAnts() {
        for (Tile enemyAnt : enemyAnts) {
            map[enemyAnt.getRow()][enemyAnt.getCol()] = Ilk.LAND;
        }
        enemyAnts.clear();
    }
    
    public void clearAllAnts() {
    	allAnts.clear();
    }
    
    /**
     * Clears game state information about food locations.
     */
    public void clearFood() {
        for (Tile food : foodTiles) {
            map[food.getRow()][food.getCol()] = Ilk.LAND;
        }
        foodTiles.clear();
    }

    /**
     * Clears game state information about my hills locations.
     */
    public void clearMyHills() {
        myHills.clear();
    }

    /**
     * Clears game state information about enemy hills locations.
     */
    public void clearEnemyHills() {
        enemyHills.clear();
    }

    /**
     * Clears game state information about dead ants locations.
     */
    public void clearDeadAnts() {
        //currently we do not have list of dead ants, so iterate over all map
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (map[row][col] == Ilk.DEAD) {
                    map[row][col] = Ilk.LAND;
                }
            }
        }
    }

    /**
     * Clears visible information
     */
    public void clearVision() {
        for (int row = 0; row < rows; ++row) {
            for (int col = 0; col < cols; ++col) {
                visible[row][col] = false;
            }
        }
    }

    /**
     * Calculates visible information
     */
    public void setVision() {
        for (Tile antLoc : myAnts) {
            for (Tile locOffset : visionOffsets) {
                Tile newLoc = getTile(antLoc, locOffset);
                visible[newLoc.getRow()][newLoc.getCol()] = true;
            }
        }
    }

    /**
     * Updates game state information about new ants and food locations.
     * 
     * @param ilk ilk to be updated
     * @param tile location on the game map to be updated
     */
    public void update(Ilk ilk, Tile tile) {
        map[tile.getRow()][tile.getCol()] = ilk;
        if (ilk == Ilk.FOOD) {
        	foodTiles.add(tile);
        }
    }
    
    public void updateAnts(int row, int col, int owner) {
    	allAnts.add(new Ant(row, col, owner));
    	
    	if (owner == 0) {
    		myAnts.add(new Tile(row, col));
    		map[row][col] = Ilk.MY_ANT;
    	}
    	else {
    		if (owner > players - 1) {
    			players = owner + 1;
    		}
    		enemyAnts.add(new Ant(row, col, owner));
    		map[row][col] = Ilk.ENEMY_ANT;
    	}
    }

    /**
     * Updates game state information about hills locations.
     *
     * @param owner owner of hill
     * @param tile location on the game map to be updated
     */
    public void updateHills(int owner, Tile tile) {
        if (owner > 0)
            enemyHills.add(tile);
        else
            myHills.add(tile);
    }

    /**
     * Issues an order by sending it to the system output.
     * 
     * @param myAnt map tile with my ant
     * @param direction direction in which to move my ant
     */
    public void issueOrder(Tile myAnt, Aim direction) {
        Order order = new Order(myAnt, direction);
        orders.add(order);
        System.out.println(order);
    }
}
