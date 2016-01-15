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
	private Set<ID> shotsFired = new HashSet<ID>();
	
	public StatisticsManager() throws ServiceException{
		this.shipsPerPlayer = 10;
		this.fieldsPerPlayer = 100;
		this.chord = Main.getChordInstance();
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
		// TODO Broadcast auslösen ob getroffen oder nicht
		// TODO Spieler beschießen
		
	}

	@Override
	public void broadcast(ID source, ID target, Boolean hit) {
		synchronized (this) {
			if(idToPlayer.isEmpty()){//First time action
				initPlayerMap();
			}
			Player hitPlayer = idToPlayer.get(source);
			if(hitPlayer == null){
				hitPlayer = new Player(source, fieldsPerPlayer);
				idToPlayer.put(source, hitPlayer);
			}
			List<Player> playerList = preparePlayer();
			if(shotsFired.contains(target)){
				shotsFired.remove(target);
				if(hitPlayer.getNrHits() == shipsPerPlayer){
					//TODO I won
				}
			}
		}
		//sourceID: angeschossener Knoten (Spieler Id -> Ist auch ChordId -> das Ende seines Bereichs!!!)
		//targetID: ChordId in dem Feld
		// TODO Auto-generated method stub
		
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
	
	private void shoot(List<Player> player){
		//TODO add to shotsfired
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

}
