import java.util.List;


public class Path extends Route {
	private List<Tile> directions;
	
	Path (Tile start, Tile end, int distance, List<Tile> directions) {
		super (start, end, distance);
		this.directions = directions;
	}
	
	public void setDirections(List<Tile> directions) {
		this.directions = directions;
	}
	
	public List<Tile> getDirections() {
		return directions;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		boolean result = false;
		if (o instanceof Path) {
			Path path = (Path)o;
			result = getStart().equals(path.getStart()) && getEnd().equals(path.getEnd());
		}
		return result;
	}
}
