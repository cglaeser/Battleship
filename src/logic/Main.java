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

/**
 * @author Allers / Glaeser
 * Game instantiation class
 */
public class Main {
	private static Map<String,String> propertyMap = null;
	private static Map<String,Logger> loggerMap = new HashMap<String, Logger>();
	private static String logFile = null;
	private static ChordImpl chord = null;
	private static FileHandler fHandler = null;
	private static String propertyFile = "battleship.properties";
	private static Logger logger = null;
	public static int NR_BITS_ID = 160;
	public static BigInteger MAX_ID = BigInteger.valueOf(2).pow(NR_BITS_ID).subtract(BigInteger.ONE);
	
	/**
	 * Gets the current chord instance in use. Creates a new one if none is
	 * in use currently
	 * @return
	 * @throws ServiceException
	 */
	public static ChordImpl getChordInstance() throws ServiceException{
		if (chord == null){
			PropertiesLoader.loadPropertyFile();
			chord = new ChordImpl();
		}
		return chord;
	}
	
	/**
	 * Loads the property file and puts its values into the property map
	 * @throws IOException
	 */
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
	
	/**
	 * Returns the logger with the given name and adds a file handler if
	 * an output file is set at the properties
	 * @param name
	 * @return
	 */
	public static Logger getLogger(String name){
		Logger logger = loggerMap.get(name);
		if(logger == null){
			logger = Logger.getLogger(name);
			if(logFile != null && logFile.length() > 0){
				try {//Add FileHandler if outputfile is set
					if(fHandler == null){
						fHandler = new FileHandler(logFile);
					}
					logger.addHandler(fHandler);
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
	
	/**
	 * Retrieves the property with the given name
	 * @param propertyName
	 * @return
	 * @throws IOException
	 */
	public static String getProperty(String propertyName) throws IOException{
		loadPropertiesInstance();
		return propertyMap.get(propertyName);	
	}

	/**
	 * @param args
	 * @throws IOException If file can't be opened
	 * @throws MalformedURLException If URL in Property File is in an incorrect format 
	 * @throws ServiceException If chord throws an exception
	 * Start the game
	 */
	public static void main(String[] args) throws IOException, MalformedURLException, ServiceException {
		try{
			Logger.getGlobal().setLevel(Level.INFO);
			if(args.length > 0){//Overwrite default property file if another one is given by the player
				propertyFile = args[0];
			}
			logFile = getProperty("logFile");
			logger = getLogger(Main.class.getName());
			logger.info("Battleship is starting");
			int shipsPerPlayer = Integer.parseInt(getProperty("shipsPerPlayer"));
			logger.info("Ships per player: "+shipsPerPlayer);
			int fieldsPerPlayer = Integer.parseInt(getProperty("fieldsPerPlayer"));
			logger.info("Fields per player: "+fieldsPerPlayer);
			String localUrlStr = getProperty("localURL");
			String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
			URL localURL = new URL(protocol+"://"+localUrlStr+"/");
			logger.info("Local URL: "+localUrlStr);

			ChordImpl chord = getChordInstance();
			StatisticsManager sm = new StatisticsManager(chord, shipsPerPlayer, fieldsPerPlayer);
			chord.setCallback(sm);
			String bootstrapUrlStr = getProperty("bootstrapURL");
			if(bootstrapUrlStr != null && bootstrapUrlStr.length() > 0){
				URL bootstrapUrl = new URL(protocol+"://"+bootstrapUrlStr+"/");
				logger.info("Connect to bootstrap node "+bootstrapUrl);
				chord.join(localURL, bootstrapUrl);
			}else{
				logger.info("Create bootstrap node on "+localURL);
				chord.create(localURL);
			}
			logger.info("Battleship started: Your ID is "+chord.getID());
			System.out.println("Press any key to start");
			System.in.read();//Wait for input
			//Check if we have to start
			if(ID.valueOf(MAX_ID).isInInterval(chord.getPredecessorID(), chord.getID()) || MAX_ID.equals(chord.getID().toBigInteger())){
				sm.firstShoot();
			}
		} catch(Exception e){
			logger.log(Level.SEVERE, "An exception occured. The apllication shuts down.", e);
			throw(e);
		}		
	}
}