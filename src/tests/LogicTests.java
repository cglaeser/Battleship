package tests;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import de.uniba.wiai.lspi.chord.service.ServiceException;
import logic.Main;

public class LogicTests {

	@Test
	public void initializeTest() throws IOException, ServiceException{
		Main main = new Main();
		System.out.println(main.getProperty("localURL"));
		//main.main(new String[]{});
	}

}
