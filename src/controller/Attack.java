package controller;

public class Attack {
	private final int destinationId;
	private final Player attacker;
	
	public Attack(int destinationId, Player attacker) {
		this.destinationId = destinationId;
		this.attacker = attacker;
	}
	
	public int getDestinationId() {
		return destinationId;
	}

	public Player getAttacker() {
		return attacker;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attacker == null) ? 0 : attacker.hashCode());
		result = prime * result + destinationId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Attack other = (Attack) obj;
		if (attacker == null) {
			if (other.attacker != null)
				return false;
		} else if (!attacker.equals(other.attacker))
			return false;
		if (destinationId != other.destinationId)
			return false;
		return true;
	}
	
	
}
