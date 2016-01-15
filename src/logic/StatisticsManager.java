package logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import player.Player;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import de.uniba.wiai.lspi.chord.service.ServiceException;

/**
 * To retrieve Informations
 * @author Sven
 *
 */
public class StatisticsManager implements NotifyCallback{
	
	private int shipsPerPlayer;
	private int fieldsPerPlayer;
	private Map<ID, Player> idToPlayer = new HashMap<ID, Player>();
	private Chord chord;
	private Set<Integer> fieldsWithShips;
	//Our shots
	private Set<ID> ourShotsFired = new HashSet<ID>();
	
	public StatisticsManager() throws ServiceException{
		this(Main.getChordInstance(),10,100);
	}
	
	public StatisticsManager(Chord chord, int shipsPerPlayer, int fieldsPerPlayer){
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
		// TODO Auto-generated method stub
		
	}
	
	private void fillWithFingertable(){
		
	}
	
	private List<Player> preparePlayer(){
		fillWithFingertable();
		//TODO sort Players and set startField
		return null;
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
