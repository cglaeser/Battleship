package player;

import java.util.HashMap;
import java.util.Map;

import de.uniba.wiai.lspi.chord.data.ID;

public class Player implements Comparable<Player>{
	
	private ID id;
	private ID startField = null;
	private int nrFields;
	private Map<ID, Boolean> hits = new HashMap<ID, Boolean>();
	
	public Player(ID id, int nrFields){
		this.id = id;
		this.nrFields = nrFields;
	}
	//Achtung: Wir sind in einem Ring!!!!!
	public int getNrHits(){
		//Achtung -> Wenn start == Null -> 0
		return 0;
	}
	
	public int getNrMisses(){
		//Achtung -> Wenn start == Null -> 0
		return 0;
	}
	
	public void shot(ID Field, boolean hit){
		hits.put(Field, hit);
	}
	
	public ID getNonShootField(){
		//Random etwas nicht getroffenes
		return null;
	}
	
	public void setStartField(ID startField){
		this.startField = startField;
	}

	@Override
	public int compareTo(Player arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
}