
public class AntDistance implements Comparable<AntDistance>{
	private Tile tile;
	private int distance;
	
	public AntDistance(Tile tile, int distance) {
		this.tile = tile;
		this.distance = distance;
	}
	
	public Tile getTile() {
		return tile;
	}
	
	public int getDistance() {
		return distance;
	}
	
	@Override
	public int compareTo(AntDistance Ad) {
		return Ad.distance - this.distance;
	}
}
