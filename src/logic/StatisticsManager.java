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
import player.Player.Field;
import player.SoundEffect;
import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

/**
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
	private boolean alreadyWon = false;
	
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
			if(idToPlayer.isEmpty()){//If the map is emty, then this is the first received message. So we have to initialize our players map.
				initPlayerMap();
			}
			fillWithFingertable();//Check for new player in finger table
			boolean hit = isHit(target);
			self().shot(target, hit);
			logger.info("Shot at: "+target+"; Was hit?: "+hit);
			chord.broadcastAsync(target, hit);
			List<Player> preparedPlayer = preparePlayer();//Set the estimated start fields
			logPlayerState(preparedPlayer);
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
			if(idToPlayer.isEmpty()){//If the map is emty, then this is the first received message. So we have to initialize our players map.
				initPlayerMap();
			}
			Player hitPlayer = idToPlayer.get(source);
			logger.info("Retrieved broadcast: Player: "+source+"; Field: "+target+"; Hit: "+ hit);
			if(hitPlayer == null){//Player was unknown before. So add him.
				logger.info("New player found: "+source);
				hitPlayer = new Player(source, shipsPerPlayer, fieldsPerPlayer);
				idToPlayer.put(source, hitPlayer);
			}
			hitPlayer.shot(target, hit);
			List<Player> preparedPlayer = preparePlayer();//Sets estimated start fields of player
			logPlayerState(preparedPlayer);
			if(ourShotsFired.contains(target)){//Check if it was our shot
				logger.info("Shot at Player: "+source+"; Field: "+target+"; Hit: "+hit+" was from you.");
				ourShotsFired.remove(target);
				//If it was our shot and there are no ships left for the player, then we have won
				if(hitPlayer.getRemainingShips() == 0 && !alreadyWon){
					SoundEffect.WON.play();
					logger.log(Level.SEVERE, "You won!!!");
					alreadyWon = true;
				}
			}
		}		
	}
	
	public void firstShoot(){
		logger.info("First shot is from you");
		if(idToPlayer.isEmpty()){//Initialize player map
			initPlayerMap();
		}
		shoot(preparePlayer());		
	}

	private void initPlayerMap() {
		ID ownId = chord.getID();
		ID predId = chord.getPredecessorID();
		logger.info("Initialisation of player map: Your Range: From "+predId.toBigInteger().add(BigInteger.ONE)+" to "+ownId.toBigInteger());
		Player self = new Player(ownId, shipsPerPlayer, fieldsPerPlayer);
		self.setStartField(ID.valueOf(predId.toBigInteger().add(BigInteger.ONE)));//Start field of ourself is one higher then the ID of our predecessor
		idToPlayer.put(ownId, self);
		idToPlayer.put(predId, new Player(predId, shipsPerPlayer, fieldsPerPlayer));		
	}
	
	/**
	 * Checks the finger table if it contains unknown players
	 */
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
	
	/**
	 * Sets the estimated start fields of the player
	 * @return
	 */
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
			logger.info("New range of player "+p.getId()+": From "+newId+" to "+lastId.toBigInteger());
		}
		return player;
	}
	
	/**
	 * @param player
	 */
	private void shoot(List<Player> player){
		logger.info("Shoot player");
		Collections.sort(player, new KillSelector());//Sort by our criterion
		int playerIndex = 0;
		ID fieldToShootAt = null;
		do{
			if(playerIndex < player.size()){
				Player playerToShootAt = player.get(playerIndex);
				logger.info("Player to shoot at "+playerToShootAt.getId());
				playerIndex++;
				if(!playerToShootAt.equals(self())){//Choose player if its not our player
					fieldToShootAt = playerToShootAt.getRandomNonShootField();
					logger.info("Field to shoot at "+fieldToShootAt);
				}
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
		}while(fieldToShootAt == null);
		ourShotsFired.add(fieldToShootAt);//Add field to our shots
		chord.retrieveAsync(fieldToShootAt);
	}
	
	private boolean isHit(ID target){
		for(Field f:self().getFields()){
			if(f.isInside(target.toBigInteger())){
				return fieldsWithShips.contains(f.getFieldNr());
			}
		};
		return false;
	}
	
	/**
	 * Fills fields randomly with ships
	 */
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
			String logStr = "Player: "+p.getId()+"; Hits: "+p.getNrHits()+"; Misses: "+p.getNrMisses()+"; Remaining: "+p.getRemainingShips()+"; Virgins: "+p.getNrVirgins()+"; Contradictions: "+p.getNrContradictions()+"\n";
			for(Field f:p.getFields()){
				logStr += f+"\n";
			}
			logger.info(logStr);			
		}
	}
	
	/**
	 * Sorts the player by our criterion
	 * @author Sven
	 *
	 */
	private class KillSelector implements Comparator<Player>{

		@Override
		public int compare(Player o1, Player o2) {
			if(o1.getNrHits() > o2.getNrHits()){//1.Nr of hits
				return -1;
			}else if(o2.getNrHits() > o1.getNrHits()){
				return 1;
			}else{
				if(o1.getNrMisses() > o2.getNrMisses()){//2. Nr of known fields
					return -1;
				}else if(o2.getNrMisses() > o1.getNrMisses()){
					return 1;
				}else{// 3. Who is the clockwise closest
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
