package logic;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class Main {
	
	private static Chord chord = null;
	private static Map<String,String> propertyMap = null;
	
	public static Chord getChordInstance() throws ServiceException{
		if (chord == null){
			PropertiesLoader.loadPropertyFile();
			chord = new ChordImpl();
		}
		return chord;
	}
	
	private static void loadPropertiesInstance() throws IOException{
		if(propertyMap == null){
		Properties properties = new Properties();
		properties.load(Main.class.getResourceAsStream("battleship.properties"));
		for (String key : properties.stringPropertyNames()) {
		    String value = properties.getProperty(key);
		    propertyMap.put(key, value);
		}
		}
	}
	
	public static String getProperty(String propertyName){
		return propertyMap.get(propertyName);	
	}

	public static void main(String[] args) throws MalformedURLException, ServiceException {
		
		Chord chord = getChordInstance();
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
