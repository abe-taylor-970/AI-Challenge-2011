
public class Ant extends Tile {
	private int owner;
	
	public Ant(int row, int col, int owner) {
		super(row, col);
		this.owner = owner;
	}
	
	public int getOwner() {
		return owner;
	}
	
	@Override
	public String toString() {
		String result = super.toString();
		result += " owner = " + owner;
		return result;
	}
}