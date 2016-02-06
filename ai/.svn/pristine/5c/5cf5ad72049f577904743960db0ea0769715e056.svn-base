/**
 * a route between a start and end
 * @author xZ
 *
 */

public class Route implements Comparable<Route>{
	private final Tile start;
	private final Tile end;
	private final int distance;

	
	/**
	 * creates a Route
	 * @param start
	 * @param end
	 * @param distance from start to end
	 */
	public Route(Tile start, Tile end, int distance) {
		this.start = start;
		this.end = end;
		this.distance = distance;
	}
	
	/**
	 * getter for start
	 * @return start Tile
	 */
	public Tile getStart() {
		return start;
	}
	
	/**
	 * getter for end
	 * @return end Tile
	 */
	public Tile getEnd() {
		return end;
	}
	
	/**
	 * getter for distance from start to end
	 * @return int distance
	 */
	public int getDistance() {
		return distance;
	}
	
	@Override
	public int compareTo(Route route) {
		return distance - route.distance;
	}
	
	@Override
	public int hashCode() {
		return start.hashCode() * Ants.MAX_MAP_SIZE * Ants.MAX_MAP_SIZE * end.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		boolean result = false;
		if (o instanceof Route) {
			Route route = (Route)o;
			result = start.equals(route.start) && end.equals(route.end);
		}
		return result;
	}
}
