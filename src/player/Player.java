package player;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import logic.Main;
import de.uniba.wiai.lspi.chord.data.ID;

public class Player implements Comparable<Player>{
	
	private ID id;
	private ID startField = null;
	private int nrFields;
	private int nrShips;
	private Map<ID, Boolean> hits = new HashMap<ID, Boolean>();
	
	//Cache fields
	private Integer nrHitsCache = null;
	private Integer nrMissesCache = null;
	private List<Integer> unusedFieldsCache = null;
	
	public Player(ID id, int nrShips, int nrFields){
		this.id = id;
		this.nrShips = nrShips;
		this.nrFields = nrFields;
	}
	//Achtung: Wir sind in einem Ring!!!!!
	public int getNrHits(){
		if(startField == null){
			return 0;
		}else if(nrHitsCache == null){
			nrHitsCache = getNrFieldsWithState(true);
			return nrHitsCache;
		}else{
			return nrHitsCache;
		}
	}
	
	public int getNrMisses(){
		if(startField == null){
			return 0;
		}else if(nrMissesCache == null){
			nrMissesCache = getNrFieldsWithState(false);
			return nrMissesCache;
		}else{
			return nrMissesCache;
		}
	}
	
	public void shot(ID Field, boolean hit){
		if(!hits.containsKey(Field)){
			resetCache(hit, !hit);
			hits.put(Field, hit);
		}
	}
	
	public ID getRandomNonShootField(){
		List<Integer> unusedFields = getUnusedFields();
		if(unusedFields.size() == 0){
			return null;
		}
		Random r = new Random();
		int fieldNr = unusedFields.get(r.nextInt(unusedFields.size()));
		
		//Auch in Zeile 52-59 fixen wenn Bug vorhanden!!!
		BigInteger length;
		BigInteger start = startField.toBigInteger();
		BigInteger end = id.toBigInteger();
		if(start.compareTo(end) > 0){
			start = start.subtract(Main.MAX_ID);
		}
		length = end.subtract(start);
		BigInteger rangeSize = length.divide(BigInteger.valueOf(nrFields));
		
		BigInteger distToStart = rangeSize.multiply(BigInteger.valueOf(fieldNr)).add(BigInteger.valueOf((long)(0.5 * rangeSize.doubleValue())));
		BigInteger idNr = startField.toBigInteger().add(distToStart);
		if(idNr.compareTo(Main.MAX_ID) > 0){
			idNr = idNr.subtract(Main.MAX_ID);
		}

		return ID.valueOf(idNr);
	}
	
	public ID getId() {
		return id;
	}
	public ID getStartField() {
		return startField;
	}
	public int getNrFields() {
		return nrFields;
	}
	public Map<ID, Boolean> getHits() {
		return hits;
	}
	public void setStartField(ID startField){
		if(!Objects.equals(startField, this.startField)){
			this.startField = startField;
			resetCache();
		}		
	}
	
	public int getRemainingShips(){
		return nrShips-getNrHits();
	}
	
	private int getNrFieldsWithState(boolean hit){
		return filterFields(id -> Objects.equals(hits.get(id), hit)).size();
	}
	
	private Set<Integer> filterFields(Predicate<ID> condition){
		//Auch in Zeile 92-99 fixen wenn Bug vorhanden!!!
		BigInteger length;
		BigInteger start = startField.toBigInteger();
		BigInteger end = id.toBigInteger();
		if(start.compareTo(end) > 0){
			start = start.subtract(Main.MAX_ID);
		}
		length = end.subtract(start);
		BigInteger rangeSize = length.divide(BigInteger.valueOf(nrFields));
		
		Set<Integer> logicalFieldNrsOfState = new HashSet<Integer>();
		for(ID fieldId:hits.keySet()){
			if(condition.test(fieldId)){
				BigInteger fieldNr = fieldId.toBigInteger();
				if(fieldNr.compareTo(start) > 0){
					fieldNr = fieldNr.subtract(Main.MAX_ID);
				}
				BigInteger dist = fieldNr.subtract(start);
				Integer logicalFieldNr = dist.divide(rangeSize).intValue();
				logicalFieldNrsOfState.add(logicalFieldNr);
			}
		}
		return logicalFieldNrsOfState;
	}
	
	private void resetCache(){
		resetCache(true, true);
	}
	
	private void resetCache(boolean hitCache, boolean missCache){
		if(hitCache){
			nrHitsCache = null;			
		}
		if(missCache){
			nrMissesCache = null;			
		}
		unusedFieldsCache = null;
	}
	
	private List<Integer> getUnusedFields(){
		if(unusedFieldsCache == null){
			Set<Integer> usedFields = filterFields(id -> true);
			List<Integer> unusedFields = new ArrayList<Integer>();
			for(int i = 0; i < nrFields; i++){
				if(!usedFields.contains(i)){
					unusedFields.add(i);
				}
			}
			unusedFieldsCache = unusedFields;
			return unusedFieldsCache;
		}else{
			return unusedFieldsCache;
		}
	}

	@Override
	public int compareTo(Player otherPlayer) {
		return id.compareTo(otherPlayer.getId());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Player other = (Player) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
