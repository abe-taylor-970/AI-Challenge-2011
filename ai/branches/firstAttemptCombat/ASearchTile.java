import java.util.List;

public class ASearchTile {
	
	private final Tile tile;
	private int g_score;
	private double h_score;
	private double f_score;
	public List<Tile> previousTiles;
	
	public ASearchTile (Tile tile, int g_score, double h_score, List<Tile> previousTiles) {
		this.tile = tile;
		this.g_score = g_score;
		this.h_score = h_score;
		f_score = g_score + h_score;
		
		this.previousTiles = previousTiles;
	}
		
	public Tile getTile () {
		return tile;
	}
	
	public double getF_score() {
		return f_score;
	}
	
	public int getG_score() {
		return g_score;
	}
	
	public double getH_score() {
		return h_score;
	}
	
	public void setG_score(int g_score) {
		this.g_score = g_score;
	}
	
	public void setH_score(double h_score) {
		this.h_score = h_score;
	}

	@Override
	public int hashCode() {
		return tile.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof ASearchTile) {
            ASearchTile aTile = (ASearchTile)o;
            result = tile.getRow() == aTile.getTile().getRow() && tile.getCol() == aTile.getTile().getCol();
        }
        return result;
	}
	
	@Override
	public String toString() {
		return tile.toString() + " f-score " + f_score;
	}
	
}
