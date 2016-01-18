package player;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
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
	private Integer nrContradictionsCache = null;
	private Integer remainingCache = null;
	private List<Field> fieldsCache = null;
	
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
			nrHitsCache = getNrFieldsWithState(FieldState.HIT);
			return nrHitsCache;
		}else{
			return nrHitsCache;
		}
	}
	
	public int getNrMisses(){
		if(startField == null){
			return 0;
		}else if(nrMissesCache == null){
			nrMissesCache = getNrFieldsWithState(FieldState.MISSED);
			return nrMissesCache;
		}else{
			return nrMissesCache;
		}
	}
	
	public int getNrContradictions(){
		if(startField == null){
			return 0;
		}else if(nrContradictionsCache == null){
			nrContradictionsCache = getNrFieldsWithState(FieldState.CONTRADICTORY);
			return nrContradictionsCache;
		}else{
			return nrContradictionsCache;
		}
	}
	
	public void shot(ID Field, boolean hit){
		if(!hits.containsKey(Field)){
			resetCache();
			hits.put(Field, hit);
		}
	}
	
	public ID getRandomNonShootField(){
		Random r = new Random();
		List<Field> virgins = filterFields(field -> FieldState.VIRGIN.equals(field.state));
		if(virgins.size() > 0){
			Field maria = virgins.get(r.nextInt(virgins.size()));
			return ID.valueOf(calcMiddle(maria.chordFrom, maria.chordTo));
		}else{
			return null;
		}
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
			resetCache();
			this.startField = startField;
		}		
	}
	
	public int getRemainingShips(){
		if(startField == null){
			return 0;
		}else if(remainingCache == null){
			remainingCache = getNrFieldsWithState(FieldState.VIRGIN);
			return remainingCache;
		}else{
			return remainingCache;
		}
	}
	
	private BigInteger calcMiddle(BigInteger from, BigInteger to){
		BigInteger half;
		if(from.compareTo(to) < 0){
			half = from.add(to).divide(BigInteger.valueOf(2));
		}else if(from.compareTo(to) > 0){
			half = from.add(to.add(Main.MAX_ID)).divide(BigInteger.valueOf(2)).mod(Main.MAX_ID);
		}else{
			half = BigInteger.ZERO;
		}
		return from.add(half).mod(Main.MAX_ID);
	}
	
	private int getNrFieldsWithState(FieldState state){
		return filterFields(field -> field.state.equals(state)).size();
	}
	
	private List<Field> filterFields(Predicate<Field> condition){
		//Auch in Zeile 92-99 fixen wenn Bug vorhanden!!!
		
		List<Field> filteredFields = new ArrayList<>(getFields());
		Iterator<Field> it = filteredFields.iterator();
		
		while(it.hasNext()){
			Field f = it.next();
			if(!condition.test(f)){
				it.remove();
			}
		}
		return filteredFields;
	}
	
	public List<Field> getFields(){
		if(this.fieldsCache == null){
			BigInteger start = startField.toBigInteger();
			BigInteger end = id.toBigInteger();
			if(start.compareTo(end) > 0){
				start = start.subtract(Main.MAX_ID);
			}
			BigInteger length = end.subtract(start).add(BigInteger.ONE);
			BigInteger rangeSize = length.divide(BigInteger.valueOf(nrFields));
			
			List<Field> newFieldsCache = new ArrayList<>();
			List<ID> hittedFieldsSorted = new ArrayList<>(hits.keySet());
			Collections.sort(hittedFieldsSorted);
			int sortedI = 0;
			for(int i=0; i<this.nrFields; i++){
				FieldState state = FieldState.VIRGIN;
				BigInteger startField = this.id.toBigInteger().add(rangeSize.multiply(BigInteger.valueOf(i))).mod(Main.MAX_ID);
				BigInteger endField = startField.add(rangeSize).subtract(BigInteger.ONE).mod(Main.MAX_ID);
				for(;sortedI<hittedFieldsSorted.size();sortedI++){
					ID hittedField = hittedFieldsSorted.get(sortedI);
					BigInteger hittedFieldBI = hittedField.toBigInteger();
					if(hittedFieldBI.compareTo(endField) < 1){
						if(state.equals(FieldState.VIRGIN)){
							state = getFieldState(hits.get(hittedField));
						}else if(!state.equals(FieldState.CONTRADICTORY)){
							FieldState newState = getFieldState(hits.get(hittedField));
							if(!newState.equals(state)){
								state = FieldState.CONTRADICTORY;
							}
						}
					}else{
						break;
					}
				}
				newFieldsCache.add(new Field(i, state, startField, endField));
			}
			fieldsCache = newFieldsCache;
			return fieldsCache;
		}else{
			return fieldsCache;
		}
	}
	
	private FieldState getFieldState(boolean hit){
		if(hit){
			return FieldState.HIT;
		}else{
			return FieldState.MISSED;
		}
	}
	
	private void resetCache(){
		resetCache(true, true);
	}
	
	private void resetCache(boolean hitCache, boolean missCache){
		nrHitsCache = null;
		nrMissesCache = null;
		nrContradictionsCache = null;
		remainingCache = null;
		fieldsCache = null;
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

	private static enum FieldState{
		HIT, MISSED, VIRGIN, CONTRADICTORY
	}
	
	public class Field{
		
		int fieldNr;
		BigInteger chordFrom = null;
		BigInteger chordTo = null;
		FieldState state;
		
		public Field(int fieldNr, FieldState state, BigInteger chordFrom, BigInteger chordTo){
			this.fieldNr = fieldNr;
			this.state = state;
			this.chordFrom = chordFrom;
			this.chordTo = chordTo;
		}
		
		@Override
		public String toString() {
			return "Field [fieldNr=" + fieldNr + ", chordFrom=" + chordFrom + ", chordTo=" + chordTo + ", state="
					+ state + "]";
		}
	}
}
