package logic;

import java.net.MalformedURLException;

import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class Main {

	public static void main(String[] args) throws MalformedURLException, ServiceException {
		Chord chord = new ChordImpl();
		chord.create();
		int shipsPerPlayer = Integer.parseInt(args[0]);
		int fieldsPerPlayer = Integer.parseInt(args[1]);
		String localUrlStr = args[2];
		StatisticsManager sm = new StatisticsManager(chord, shipsPerPlayer, fieldsPerPlayer);
		chord.setCallback(sm);
		URL localURL = new URL("ocsocket://"+localUrlStr);
		if(args.length > 3){
			String bootstrapUrlStr = args[3];
			URL bootstrapUrl = new URL("ocsocket://"+bootstrapUrlStr);
			chord.join(localURL, bootstrapUrl);
		}else{
			chord.create(localURL);
			//TODO Wait for input and shoot on input
		}
	}
}
