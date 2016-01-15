package tests;

import static org.junit.Assert.*;

import org.junit.Test;

import de.uniba.wiai.lspi.chord.service.ServiceException;
import logic.Main;
import logic.StatisticsManager;

public class FillList {

	@Test
	public void checkListSize() throws ServiceException{
		StatisticsManager sm = new StatisticsManager(Main.getChordInstance(),10,100);
		assertTrue(sm.getFieldsWithShips().size()==10);
	}
	
	//Sind Spieler Felder zugeordnet?
	

}
