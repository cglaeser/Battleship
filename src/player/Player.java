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
	
	private final ID id;
	private ID startField = null;
	private final int nrFields;
	private final int nrShips;
	private final Map<ID, Boolean> hits = new HashMap<ID, Boolean>();
	
	//Cache fields
	private Integer nrHitsCache = null;
	private Integer nrMissesCache = null;
	private Integer nrContradictionsCache = null;
	private Integer nrVirginsCache = null;
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
		return nrShips - getNrHits();
	}

	public int getNrVirgins(){
		if(startField == null){
			return 0;
		}else if(nrVirginsCache == null){
			nrVirginsCache = getNrFieldsWithState(FieldState.VIRGIN);
			return nrVirginsCache;
		}else{
			return nrVirginsCache;
		}
	}
	
	private BigInteger calcMiddle(BigInteger from, BigInteger to){
		if(from.compareTo(to) < 0){
			return from.add(to).divide(BigInteger.valueOf(2)).mod(Main.MAX_ID);
		}else if(from.compareTo(to) > 0){
			return from.add(to.add(Main.MAX_ID)).divide(BigInteger.valueOf(2)).mod(Main.MAX_ID);
		}else{
			return from;
		}
	}
	
	private int getNrFieldsWithState(FieldState state){
		return filterFields(field -> field.state.equals(state)).size();
	}
	
	private List<Field> filterFields(Predicate<Field> condition){
		
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
		if(this.fieldsCache == null && this.startField != null){
			BigInteger start = startField.toBigInteger();
			BigInteger end = id.toBigInteger();
			if(start.compareTo(end) > 0){
				start = start.subtract(Main.MAX_ID);
			}
			BigInteger length = end.subtract(start).add(BigInteger.ONE);
			BigInteger rangeSize = length.divide(BigInteger.valueOf(nrFields));
			
			List<Field> newFieldsCache = new ArrayList<>();
			List<ID> hittedFieldsSorted = new ArrayList<>(hits.keySet());
			ringSort(hittedFieldsSorted);
			int sortedI = 0;
			for(int i=0; i<this.nrFields; i++){
				BigInteger startField = this.startField.toBigInteger().add(rangeSize.multiply(BigInteger.valueOf(i))).mod(Main.MAX_ID);
				BigInteger endField = startField.add(rangeSize).subtract(BigInteger.ONE).mod(Main.MAX_ID);
				Field field = new Field(i, FieldState.VIRGIN, startField, endField);
				for(;sortedI<hittedFieldsSorted.size();sortedI++){
					ID hittedField = hittedFieldsSorted.get(sortedI);
					BigInteger hittedFieldBI = hittedField.toBigInteger();
					if(field.isInside(hittedFieldBI)){
						if(field.state.equals(FieldState.VIRGIN)){
							field.state = getFieldState(hits.get(hittedField));
						}else if(!field.state.equals(FieldState.CONTRADICTORY)){
							FieldState newState = getFieldState(hits.get(hittedField));
							if(!newState.equals(field.state)){
								field.state = FieldState.CONTRADICTORY;
							}
						}
					}else{
						break;
					}
				}
				newFieldsCache.add(field);
			}
			fieldsCache = newFieldsCache;
			return fieldsCache;
		}else{
			return fieldsCache;
		}
	}
	

	
	/**
	 * Sorts he fields in order as that the elements start with the start field and end with the id
	 * @param fields
	 */
	private void ringSort(List<ID> fields){
		Collections.sort(fields);
		if(this.startField != null){
			List<ID> toAppend = new ArrayList<ID>();
			Iterator<ID> it = fields.iterator();
			while(it.hasNext()){
				ID id = it.next();
				if(id.compareTo(this.startField) < 0){
					toAppend.add(id);
					it.remove();
				}
			}
			fields.addAll(toAppend);
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
		nrVirginsCache = null;
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
		
		private int fieldNr;
		private BigInteger chordFrom = null;
		private BigInteger chordTo = null;
		private FieldState state;
		
		public Field(int fieldNr, FieldState state, BigInteger chordFrom, BigInteger chordTo){
			this.fieldNr = fieldNr;
			this.state = state;
			this.chordFrom = chordFrom;
			this.chordTo = chordTo;
		}
		
		public int getFieldNr() {
			return fieldNr;
		}

		public BigInteger getChordFrom() {
			return chordFrom;
		}

		public BigInteger getChordTo() {
			return chordTo;
		}

		public FieldState getState() {
			return state;
		}
		
		public boolean isInside(BigInteger field){
			if(chordFrom.compareTo(chordTo) < 0){
				return field.compareTo(chordFrom) > -1 && field.compareTo(chordTo) < 1;
			}else{
				return field.compareTo(chordFrom) > -1 || field.compareTo(chordTo) < 1;
			}
		}
		
		@Override
		public String toString() {
			return "Field [fieldNr=" + fieldNr + ", chordFrom=" + chordFrom + ", chordTo=" + chordTo + ", state="
					+ state + "]";
		}
	}
}
