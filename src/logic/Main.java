package logic;

import java.math.BigInteger;
import java.net.MalformedURLException;

import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class Main {
	
	private static ChordImpl chord = null;
	public static BigInteger MAX_ID = BigInteger.valueOf(2).pow(160).subtract(BigInteger.ONE);
	
	public static ChordImpl getChordInstance() throws ServiceException{
		if (chord == null){
			PropertiesLoader.loadPropertyFile();
			chord = new ChordImpl();
		}
		return chord;
	}

	public static void main(String[] args) throws MalformedURLException, ServiceException {
		ChordImpl chord = getChordInstance();
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
			System.out.println("Press any key to start");
			System.console().readLine();
			sm.firstShoot();
		}
	}
}
