package player;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.mp3transform.Decoder;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
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

