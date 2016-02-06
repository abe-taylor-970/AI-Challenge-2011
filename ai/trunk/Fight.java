import java.util.Set;


public class Fight {
	private Set<Tile> buddyAnts;
	private Set<Tile> enemyAnts;
	
	public Fight (Set<Tile> buddyAnts, Set<Tile> enemyAnts) {
		this.buddyAnts = buddyAnts;
		this.enemyAnts = enemyAnts;
	}
	
	
	public Set<Tile> getBuddyAnts() {
		return buddyAnts;
	}
	public Set<Tile> getEnemyAnts() {
		return enemyAnts;
	}
	
	public void setBuddyAnts(Set<Tile> buddyAnts) {
		this.buddyAnts = buddyAnts;
	}
	
	public void setEnemyAnts(Set<Tile> enemyAnts) {
		this.enemyAnts = enemyAnts;
	}
	
	@Override 
	public boolean equals(Object o) {
		boolean result = false;
		if (o instanceof Fight) {
			Fight f = (Fight)o;
			if (f.buddyAnts.equals(f.buddyAnts) && f.enemyAnts.equals(f.enemyAnts))
				result = true;
		}
		return result;
		
	}
	
	@Override
	public int hashCode() {
		int result = 0;
		for (Tile buddyAnt : buddyAnts) {
			result += buddyAnt.hashCode();
		}
		
		for (Tile enemyAnt : enemyAnts) {
			result -= enemyAnt.hashCode();
		}
		return result;
	}
	
	@Override
	public String toString() {
		String result = "buddyAnts : ";
		
		for (Tile buddy : buddyAnts) {
			result += buddy.toString() + ", ";
		}
		
		result += "enemyAnts : ";
		for (Tile enemy : enemyAnts) {
			result += enemy.toString() + ", ";
		}
		
		return result;
	}
}

/*
public class Fight {
	private Tile theAnt;
	private Set<Tile> enemyAnts;
	
	public Fight (Tile theAnt, Set<Tile> enemyAnts) {
		this.theAnt = theAnt;
		this.enemyAnts = enemyAnts;
	}
	
	
	public Tile getTheAnt() {
		return theAnt;
	}
	public Set<Tile> getEnemyAnts() {
		return enemyAnts;
	}
	
	public void setEnemyAnts(Set<Tile> enemyAnts) {
		this.enemyAnts = enemyAnts;
	}
	
	@Override 
	public boolean equals(Object o) {
		boolean result = false;
		if (o instanceof Fight) {
			Fight f = (Fight)o;
			if (theAnt.equals(f.theAnt))
				result = true;
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		return theAnt.hashCode();
	}
	
	@Override
	public String toString() {
		String result = "Ant : ";
		
		
		//for (Tile buddy : buddyAnts) {
		//	result += buddy.toString() + ", ";
		//}
		//result += theAnt.toString();
		
		result += "  enemyAnts : ";
		for (Tile enemy : enemyAnts) {
			result += enemy.toString() + ", ";
		}
		
		return result;
	}
}*/