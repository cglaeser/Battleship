package player;

import java.net.MalformedURLException;
import java.net.URL;

import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerException;


public enum SoundEffect {
	  
	   EXPLODE("resources/haha.mp3"),   // explosion
	   HIT("resources/dooh.mp3"),         // gong
	   WON("resources/won.mp3");       // bullet
	   
	   String soundFileName;

	   // Constructor to construct each element of the enum with its own sound file.
	   SoundEffect(String soundFileName) {
	      this.soundFileName = soundFileName;
	   }
	   
	   public void play() {
		   String songName = soundFileName;
		   String pathToMp3 = System.getProperty("user.dir") +"/"+ songName;
		   BasicPlayer player = new BasicPlayer();
		   try {
		       player.open(new URL("file:///" + pathToMp3));
		       player.play();
		   } catch (BasicPlayerException | MalformedURLException e) {
		       e.printStackTrace();
		   }
	   }
	   
	   // Optional static method to pre-load all the sound files.
	   static void init() {
	      values(); // calls the constructor for all the elements
	   }
	}

