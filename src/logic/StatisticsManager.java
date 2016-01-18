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
import java.util.logging.Level;
import java.util.logging.Logger;

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
	private Logger logger = Main.getLogger(StatisticsManager.class.getName());
	
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
			logger.info("Retrieved shoot at "+target);
			if(idToPlayer.isEmpty()){//First time action
				initPlayerMap();
			}
			fillWithFingertable();
			boolean hit = isHit(target);
			logger.info("Shot at: "+target+"; Was hit?: "+hit);
			chord.broadcast(target, hit);
			List<Player> preparedPlayer = preparePlayer();
			logPlayerState(preparedPlayer);
			self().shot(target, hit);
			shoot(preparedPlayer);		
		}
	}

	/**
	 * Nachricht verarbeiten
	 * @see de.uniba.wiai.lspi.chord.service.NotifyCallback#broadcast(de.uniba.wiai.lspi.chord.data.ID, de.uniba.wiai.lspi.chord.data.ID, java.lang.Boolean)
	 */
	@Override
	public void broadcast(ID source, ID target, Boolean hit) {
		synchronized (this) {
			if(idToPlayer.isEmpty()){//First time action
				initPlayerMap();
			}
			Player hitPlayer = idToPlayer.get(source);
			logger.info("Retrieved broadcast: Player: "+source+"; Field: "+target+"; Hit: "+ hit);
			if(hitPlayer == null){
				logger.info("New player found: "+source);
				hitPlayer = new Player(source, shipsPerPlayer, fieldsPerPlayer);
				//neuen spieler zur spielermap hinzufügen
				idToPlayer.put(source, hitPlayer);
			}
			hitPlayer.shot(target, hit);
			List<Player> preparedPlayer = preparePlayer();//Sets startfield of player
			logPlayerState(preparedPlayer);
			if(ourShotsFired.contains(target)){
				logger.info("Shot at Player: "+source+"; Field: "+target+"; Hit: "+hit+" was from you.");
				ourShotsFired.remove(target);
				if(hitPlayer.getRemainingShips() == 0){
					logger.log(Level.SEVERE, "You won!!!");
				}
			}
		}		
	}
	
	public void firstShoot(){
		logger.info("First shot is from you");
		if(idToPlayer.isEmpty()){//First time action
			initPlayerMap();
		}
		shoot(preparePlayer());		
	}

	private void initPlayerMap() {
		ID ownId = chord.getID();
		ID predId = chord.getPredecessorID();
		logger.info("Initialisation of player map: Your Range: From "+predId.toBigInteger().add(BigInteger.ONE)+" to "+ownId);
		idToPlayer.put(ownId, new Player(ownId, shipsPerPlayer, fieldsPerPlayer));
		idToPlayer.put(predId, new Player(predId, shipsPerPlayer, fieldsPerPlayer));		
	}
	
	private void fillWithFingertable(){
		logger.info("Check fingertable");
		for(Node node:chord.getFingerTable()){
			if(!idToPlayer.containsKey(node.getNodeID())){
				ID nodeId = node.getNodeID();
				logger.info("New player found: "+nodeId);
				idToPlayer.put(nodeId, new Player(nodeId, shipsPerPlayer, fieldsPerPlayer));
			}
		}
	}
	
	private List<Player> preparePlayer(){
		logger.info("Prepare player");
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
			logger.info("New range of player "+p.getId()+": From "+newId+" to "+lastId);
		}
		return player;
	}
	
	/**
	 * @param player
	 */
	private void shoot(List<Player> player){
		logger.info("Shoot player");
		Collections.sort(player, new KillSelector());
		int playerIndex = 0;
		Player playerToShootAt = player.get(playerIndex);
		if(playerToShootAt.equals(self())){
			playerIndex++;
			playerToShootAt = player.get(playerIndex);
		}
		logger.info("Player to shoot at "+playerToShootAt.getId());
		ID fieldToShootAt;
		do{
			fieldToShootAt = playerToShootAt.getRandomNonShootField();
			logger.info("Field to shoot at "+fieldToShootAt);
			if(fieldToShootAt == null){
				logger.info("Field was null");
				playerIndex++;
				if(playerIndex < player.size()){
					playerToShootAt = player.get(playerIndex);
					logger.info("Player to shoot at "+playerToShootAt.getId());
				}else{//No field found -> choose a random field -> shouldn't happen actually
					logger.info("No field found: Shoot randomly");
					Random r = new Random();
					Player self = self();
					BigInteger selfStart = self.getStartField().toBigInteger();
					BigInteger selfEnd = self.getId().toBigInteger();
					BigInteger fieldNrToShootAt;
					do{
						fieldNrToShootAt = new BigInteger(Main.NR_BITS_ID, r);
					}while(fieldNrToShootAt.compareTo(selfStart) < 0 && fieldNrToShootAt.compareTo(selfEnd) > 0);
					fieldToShootAt = ID.valueOf(fieldNrToShootAt);
					logger.info("Random field: "+fieldNrToShootAt);
				}
			}			
		}while(fieldToShootAt == null);
		chord.retrieveAsync(fieldToShootAt);
	}
	
	private boolean isHit(ID target){
		return false;
	}
	
	private Set<Integer> fillFields(int nrShips, int nrFields){
		logger.info("Fill fields with ships");
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
		logger.info("Fields with ships: "+fieldsWithShips);
		return fieldsWithShips;
	}
	
	public Set<Integer> getFieldsWithShips(){
		return fieldsWithShips;
	}
	
	public Player self(){
		return idToPlayer.get(chord.getID());
	}
	
	private void logPlayerState(List<Player> player){
		logger.info("Current player states:");
		for(Player p:player){
			logger.info("Player: "+p.getId()+"; Hits: "+p.getNrHits()+"; Misses: "+p.getNrMisses()+"; Remaining: "+p.getRemainingShips());
		}
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
					BigInteger distO1 = BigInteger.ZERO;
					BigInteger distO2 = BigInteger.ZERO;
					BigInteger selfId = self().getId().toBigInteger();
					BigInteger o1Id = o1.getId().toBigInteger();
					BigInteger o2Id = o2.getId().toBigInteger();
					if(o1Id.compareTo(selfId) > 0){
						distO1 = o1Id.subtract(selfId);
					}else if(o1Id.compareTo(selfId) < 0){
						distO1 = o1Id.add(selfId);
					}
					if(o2Id.compareTo(selfId) > 0){
						distO2 = o2Id.subtract(selfId);
					}else if(o2Id.compareTo(selfId) < 0){
						distO2 = o2Id.add(selfId);
					}
					return distO1.compareTo(distO2);
				}
			}
		}
	}

}
