package logic;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class Main {
	

	private static Map<String,String> propertyMap = null;
	private static ChordImpl chord = null;
	public static BigInteger MAX_ID = BigInteger.valueOf(2).pow(160).subtract(BigInteger.ONE);
	
	public static ChordImpl getChordInstance() throws ServiceException{
		if (chord == null){
			PropertiesLoader.loadPropertyFile();
			chord = new ChordImpl();
		}
		return chord;
	}
	
	private static void loadPropertiesInstance() throws IOException{
		if(propertyMap == null){
			propertyMap = new HashMap<String,String>();
		Properties properties = new Properties();
		ClassLoader loader = Main.class.getClassLoader();
        properties.load(loader.getResourceAsStream("battleship.properties"));
		for (String key : properties.stringPropertyNames()) {
		    String value = properties.getProperty(key);
		    propertyMap.put(key, value);
		}
		}
	}
	
	public static String getProperty(String propertyName) throws IOException{
		loadPropertiesInstance();
		return propertyMap.get(propertyName);	
	}

	public static void main(String[] args) throws IOException, MalformedURLException, ServiceException {
		ChordImpl chord = getChordInstance();
		int shipsPerPlayer = Integer.parseInt(getProperty("shipsPerPlayer"));
		int fieldsPerPlayer = Integer.parseInt(getProperty("fieldsPerPlayer"));
		String localUrlStr = getProperty("localURL");
		StatisticsManager sm = new StatisticsManager(chord, shipsPerPlayer, fieldsPerPlayer);
		chord.setCallback(sm);
		URL localURL = new URL("ocsocket://"+localUrlStr+"/");
		if(args.length > 3){
			String bootstrapUrlStr = getProperty("bootstrapURL");
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