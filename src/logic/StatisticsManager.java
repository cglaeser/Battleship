package logic;

import java.util.HashMap;
import java.util.HashSet;
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
	private Map<ID, Player> player = new HashMap<ID, Player>();
	private Chord chord;
	private Set<Integer> fieldsWithShips = new HashSet<Integer>();
	
	public StatisticsManager() throws ServiceException{
		this.shipsPerPlayer = 10;
		this.fieldsPerPlayer = 100;
		this.chord = Main.getChordInstance();
	}
	
	public StatisticsManager(Chord chord, int shipsPerPlayer, int fieldsPerPlayer){
		this.shipsPerPlayer = shipsPerPlayer;
		this.fieldsPerPlayer = fieldsPerPlayer;
		this.chord = chord;
		//TODO Spielerliste erstellen
	}

	@Override
	public void retrieved(ID target) {
		// TODO Auto-generated method stub
		// TODO hier Broadcast auslösen ob getroffen oder nicht
		
	}

	@Override
	public void broadcast(ID source, ID target, Boolean hit) {
		synchronized (this) {
			
		}
		//sourceID: angeschossener Knoten (Spieler Id -> Ist auch ChordId -> das Ende seines Bereichs!!!)
		//targetID: ChordId in dem Feld
		// TODO Auto-generated method stub
		
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
