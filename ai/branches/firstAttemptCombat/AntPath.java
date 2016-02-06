import java.util.List;


public class AntPath {
	private Tile theAnt;
	private int position;
	private List<Tile> directions;
	
	public AntPath (Tile theAnt, List<Tile> directions, int position) {
		this.theAnt = theAnt;
		this.directions = directions;
		this.position = position;
	}
	
	public Tile getAnt() {
		return theAnt;
	}
	
	public void setAnt(Tile theAnt) {
		this.theAnt = theAnt;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
	
	public int getPosition() {
		return position;
	}
	
	public void setDirections(List<Tile> directions) {
		this.directions = directions;
	}
	
	public List<Tile> getDirections() {
		return directions;
	}
	
	@Override
	public boolean equals(Object o) {
		boolean result = false;
		if (o instanceof AntPath) {
			AntPath antPath = (AntPath)o;
			result = theAnt.equals(antPath.theAnt);
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		return theAnt.hashCode();
	}
	
	@Override
	public String toString() {
		return "Ant : " + theAnt.toString() + " destination : " + directions.get(directions.size() - 1);
	}
}
