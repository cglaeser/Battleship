package logic;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import player.Player;
import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

/**
 * To retrieve Informations
 * @author Sven
 *
 */
public class StatisticsManager implements NotifyCallback{
	
	private int shipsPerPlayer;
	private int fieldsPerPlayer;
	private Map<ID, Player> idToPlayer = new HashMap<ID, Player>();
	private ChordImpl chord;
	private Set<Integer> fieldsWithShips;
	//Our shots
	private Set<ID> ourShotsFired = new HashSet<ID>();
	
	public StatisticsManager() throws ServiceException{
		this(Main.getChordInstance(),10,100);
	}
	
	public StatisticsManager(ChordImpl chord, int shipsPerPlayer, int fieldsPerPlayer){
		this.shipsPerPlayer = shipsPerPlayer;
		this.fieldsPerPlayer = fieldsPerPlayer;
		this.chord = chord;
		fieldsWithShips = fillFields(shipsPerPlayer, fieldsPerPlayer);
	}

	@Override
	public void retrieved(ID target) {
		synchronized (this) {
			if(idToPlayer.isEmpty()){//First time action
				initPlayerMap();
			}
			fillWithFingertable();
		}
		boolean hit = isHit(target);
		chord.broadcast(target, hit);
		shoot(preparePlayer());		
	}

	/**
	 * Nachricht verarbeiten
	 * @see de.uniba.wiai.lspi.chord.service.NotifyCallback#broadcast(de.uniba.wiai.lspi.chord.data.ID, de.uniba.wiai.lspi.chord.data.ID, java.lang.Boolean)
	 */
	@Override
	public void broadcast(ID source, ID target, Boolean hit) {
		synchronized (this) {
			//füge uns + succ + pred zur PlayerMap hinzu
			if(idToPlayer.isEmpty()){//First time action
				initPlayerMap();
			}
			Player hitPlayer = idToPlayer.get(source);
			if(hitPlayer == null){
				hitPlayer = new Player(source, shipsPerPlayer, fieldsPerPlayer);
				//neuen spieler zur spielermap hinzufügen
				idToPlayer.put(source, hitPlayer);
			}
			hitPlayer.shot(target, hit);
			preparePlayer();//Sets startfield of player
			if(ourShotsFired.contains(target)){
				ourShotsFired.remove(target);
				if(hitPlayer.getRemainingShips() == 0){
					//TODO I won -> Logger Win!!!
				}
			}
		}		
	}
	
	public void firstShoot(){
		if(idToPlayer.isEmpty()){//First time action
			initPlayerMap();
		}
		shoot(preparePlayer());		
	}

	private void initPlayerMap() {
		ID ownId = chord.getID();
		ID predId = chord.getPredecessorID();
		idToPlayer.put(ownId, new Player(ownId, shipsPerPlayer, fieldsPerPlayer));
		idToPlayer.put(predId, new Player(predId, shipsPerPlayer, fieldsPerPlayer));		
	}
	
	private void fillWithFingertable(){
		for(Node node:chord.getFingerTable()){
			if(!idToPlayer.containsKey(node.getNodeID())){
				ID nodeId = node.getNodeID();
				idToPlayer.put(nodeId, new Player(nodeId, shipsPerPlayer, fieldsPerPlayer));
			}
		}
	}
	
	private List<Player> preparePlayer(){
		fillWithFingertable();
		List<Player> player = new ArrayList<Player>(idToPlayer.values());
		Collections.sort(player);
		ID lastId = player.get(player.size() - 1).getId();
		for(Player p:player){
			BigInteger newId = lastId.toBigInteger().add(BigInteger.ONE);
			if(newId.compareTo(Main.MAX_ID) > 0){
				newId = newId.subtract(Main.MAX_ID);
			}
			p.setStartField(ID.valueOf(newId));
			lastId = p.getId();
		}
		return player;
	}
	
	/**
	 * @param player
	 */
	private void shoot(List<Player> player){
		Collections.sort(player, new KillSelector());
		int playerIndex = 0;
		Player playerToShootAt = player.get(playerIndex);
		if(playerToShootAt.equals(self())){
			playerIndex++;
			playerToShootAt = player.get(playerIndex);
		}
		ID fieldToShootAt;
		do{
			fieldToShootAt = playerToShootAt.getRandomNonShootField();
			if(fieldToShootAt == null){
				playerIndex++;
				if(playerIndex < player.size()){
					playerToShootAt = player.get(playerIndex);
				}else{//No field found -> shouldn't happen actually
					Random r = new Random();
					Player self = self();
					BigInteger selfStart = self.getStartField().toBigInteger();
					BigInteger selfEnd = self.getId().toBigInteger();
					BigInteger fieldNrToShootAt;
					do{
						fieldNrToShootAt = new BigInteger(Main.NR_BITS_ID, r);
					}while(fieldNrToShootAt.compareTo(selfStart) < 0 && fieldNrToShootAt.compareTo(selfEnd) > 0);
				}
			}			
		}while(fieldToShootAt == null);
		chord.retrieve(fieldToShootAt);
	}
	
	private boolean isHit(ID target){
		return false;
	}
	
	private Set<Integer> fillFields(int nrShips, int nrFields){
		Set<Integer> fieldsWithShips = new HashSet<Integer>();
		List<Integer> fields = new ArrayList<Integer>();
		for(int i = 0; i < nrFields; i++){
			fields.add(i);
		}
		Random r = new Random();
		for(int i = 0; i < nrShips; i++){
			int fIndex = r.nextInt(fields.size());
			fieldsWithShips.add(fields.get(fIndex));
			fields.remove(fIndex);
		}
		return fieldsWithShips;
	}
	
	public Set<Integer> getFieldsWithShips(){
		return fieldsWithShips;
	}
	
	public Player self(){
		return idToPlayer.get(chord.getID());
	}
	
	private class KillSelector implements Comparator<Player>{

		@Override
		public int compare(Player o1, Player o2) {
			if(o1.getNrHits() > o2.getNrHits()){
				return -1;
			}else if(o2.getNrHits() > o1.getNrHits()){
				return 1;
			}else{
				if(o1.getNrMisses() > o2.getNrMisses()){
					return -1;
				}else if(o2.getNrMisses() > o1.getNrMisses()){
					return 1;
				}else{
					return 0;
				}
			}
		}
	}

}
