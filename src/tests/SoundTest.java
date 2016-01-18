package tests;

import org.junit.Test;

import player.SoundEffect;

public class SoundTest {

	@Test
	public void targetAchievedSoundTest() throws InterruptedException{
		SoundEffect.EXPLODE.play();
		   Thread.sleep(10000);
	}
	
	@Test
	public void wonSoundTest() throws InterruptedException{
		SoundEffect.WON.play();
		   Thread.sleep(10000);
	}
	
	@Test
	public void hitSoundTest() throws InterruptedException{
		SoundEffect.HIT.play();
		   Thread.sleep(10000);
	}
}
