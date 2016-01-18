package tests;

import static org.junit.Assert.*;

import org.junit.Test;

import de.uniba.wiai.lspi.chord.service.ServiceException;
import logic.Main;
import logic.StatisticsManager;

public class FillList {

	@Test
	public void checkListSizeTest() throws ServiceException{
		StatisticsManager sm = new StatisticsManager();
		System.out.println("FieldWithShips: " + sm.getFieldsWithShips());
		assertTrue(sm.getFieldsWithShips().size()==10);	
	}
	
	@Test
	public void checkWrongIndexDeltionTest() throws ServiceException{;
		StatisticsManager sm = new StatisticsManager(Main.getChordInstance(), 25, 25);
		System.out.println("FieldWithShips: " + sm.getFieldsWithShips());
		assertTrue(sm.getFieldsWithShips().size()==25);		
	}
	//Sind Spieler Felder zugeordnet?
	
	public void checkSelfTest() throws ServiceException{
		StatisticsManager sm = new StatisticsManager();
		assertTrue(sm.self().getRemainingShips() == 10);
		assertTrue(sm.self().getNrHits()== 0);
		assertTrue(sm.self().getNrMisses() == 0);
	}

}
