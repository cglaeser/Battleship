package logic;

import java.util.ArrayList;
import java.util.Collections;
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
		this.shipsPerPlayer = 10;
		this.fieldsPerPlayer = 100;
		this.chord = Main.getChordInstance();
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
				hitPlayer = new Player(source, fieldsPerPlayer);
				//neuen spieler zur spielermap hinzufügen
				idToPlayer.put(source, hitPlayer);
			}
			hitPlayer.shot(target, hit);
			preparePlayer();//Sets startfield of player
			if(ourShotsFired.contains(target)){
				ourShotsFired.remove(target);
				if(hitPlayer.getNrHits() == shipsPerPlayer){
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
		idToPlayer.put(ownId, new Player(ownId, fieldsPerPlayer));
		idToPlayer.put(predId, new Player(predId, fieldsPerPlayer));		
	}
	
	private void fillWithFingertable(){
		for(Node node:chord.getFingerTable()){
			if(!idToPlayer.containsKey(node.getNodeID())){
				ID nodeId = node.getNodeID();
				idToPlayer.put(nodeId, new Player(nodeId, fieldsPerPlayer));
			}
		}
	}
	
	private List<Player> preparePlayer(){
		fillWithFingertable();
		List<Player> player = new ArrayList<Player>(idToPlayer.values());
		Collections.sort(player);
		ID lastId = player.get(player.size() - 1).getId();
		for(Player p:player){
			p.setStartField(lastId);
			lastId = p.getId();
		}
		return player;
	}
	
	/**
	 * @param player
	 */
	private void shoot(List<Player> player){
		//TODO add to shotsfired
		//select player != us
	}
	
	private boolean isHit(ID target){
		return false;
	}
	
	private Set<Integer> fillFields(int ships, int fields){
		Set<Integer> fieldsWithShips = new HashSet<Integer>();
		Random r = new Random();
		while(fieldsWithShips.size()< ships){
			fieldsWithShips.add(r.nextInt(fields));
		}
		return fieldsWithShips;
	}
	
	public Set<Integer> getFieldsWithShips(){
		return fieldsWithShips;
	}
	
	public Player self(){
		return idToPlayer.get(chord.getID());
	}

}
