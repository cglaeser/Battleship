package logic;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class Main {
	

	private static Map<String,String> propertyMap = null;
	private static Map<String,Logger> loggerMap = null;
	private static String logFile = null;
	private static ChordImpl chord = null;
	private static String propertyFile = "battleship.properties";
	private static Logger logger = null;
	public static int NR_BITS_ID = 160;
	public static BigInteger MAX_ID = BigInteger.valueOf(2).pow(NR_BITS_ID).subtract(BigInteger.ONE);
	
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
        properties.load(loader.getResourceAsStream(propertyFile));
		for (String key : properties.stringPropertyNames()) {
		    String value = properties.getProperty(key);
		    propertyMap.put(key, value);
		}
		}
	}
	
	public static Logger getLogger(String name){
		Logger logger = loggerMap.get(name);
		if(logger == null){
			logger = Logger.getLogger(name);
			if(logFile != null && logFile.length() > 0){
				try {
					logger.addHandler(new FileHandler(logFile));
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			loggerMap.put(name, logger);
		}
		return logger;
	}
	
	public static String getProperty(String propertyName) throws IOException{
		loadPropertiesInstance();
		return propertyMap.get(propertyName);	
	}

	public static void main(String[] args) throws IOException, MalformedURLException, ServiceException {
		try{
			if(args.length > 0){
				propertyFile = args[0];
			}
			logFile = getProperty("logFile");
			logger = getLogger(ChordImpl.class.getName());
			logger.info("Battleship is starting");
			int shipsPerPlayer = Integer.parseInt(getProperty("shipsPerPlayer"));
			logger.info("Ships per player: "+shipsPerPlayer);
			int fieldsPerPlayer = Integer.parseInt(getProperty("fieldsPerPlayer"));
			logger.info("Fields per player: "+fieldsPerPlayer);
			String localUrlStr = getProperty("localURL");
			URL localURL = new URL("ocsocket://"+localUrlStr+"/");
			logger.info("Local URL: "+localUrlStr);

			ChordImpl chord = getChordInstance();
			StatisticsManager sm = new StatisticsManager(chord, shipsPerPlayer, fieldsPerPlayer);
			chord.setCallback(sm);
			String bootstrapUrlStr = getProperty("bootstrapURL");
			if(bootstrapUrlStr != null && bootstrapUrlStr.length() > 0){
				URL bootstrapUrl = new URL("ocsocket://"+bootstrapUrlStr);
				logger.info("Connect to bootstrap node "+bootstrapUrl);
				chord.join(localURL, bootstrapUrl);
			}else{
				logger.info("Create bootstrap node on "+localURL);
				chord.create(localURL, ID.valueOf(MAX_ID));
			}
			logger.info("Battleship started: Your ID is "+chord.getID());
			if(chord.getID().toBigInteger().equals(MAX_ID)){
				System.out.println("Press any key to start");
				System.console().readLine();
				sm.firstShoot();
			}
		} catch(Exception e){
			logger.log(Level.SEVERE, "An exception occured. The apllication shuts down.", e);
			throw(e);
		}		
	}
}