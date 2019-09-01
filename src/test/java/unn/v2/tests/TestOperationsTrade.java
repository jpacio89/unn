package unn.v2.tests;


import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;

import plugins.crypto.trade.bot.PostgresManager;
import unn.*;

public class TestOperationsTrade 
{
	final int TEST_SAMPLE_COUNT = 1000;
	final String DATASET_FILE_PATH = "/Users/joaocoelho/Documents/Work/UNN/exchange-crawler/dataset/dataset-5p.csv";
	
	private Dataset loadDataset() {
		return DatasetParser.parseFromFile(DATASET_FILE_PATH);
	}
	
	@Test
	public void testTraderNew() throws Exception {
		// PrintStream fileOut = new PrintStream("./out.log");
		// System.setOut(fileOut);
		
		Config.STIMULI_MIN_VALUE = -10;
		Config.STIMULI_MAX_VALUE = 10;
		
		Dataset dbDataset = PostgresManager.select("ETHBTC");
		
		Dataset dataset = loadDataset();
		Model model = null;
		
		Miner miner = new Miner(dbDataset);
		miner.init();
		miner.mine();
	}
	

	public void testTrader () throws Exception
	{
		
	}
}
