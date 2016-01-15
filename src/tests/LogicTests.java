package tests;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import logic.Main;

public class LogicTests {

	@Test
	public void initializeTest() throws IOException{
		Main main = new Main();
		System.out.println(main.getProperty("localURL"));
	}

}
