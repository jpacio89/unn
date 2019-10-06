import java.util.Scanner;
import java.util.logging.Logger;

import plugins.crypto.trade.bot.TradeEnvironment;
import plugins.openml.OpenML;
import server.RESTApi;
import unn.Config;
import unn.IEnvironment;

public class Shell {
	private static Logger logger = Logger.getLogger(Shell.class.toString());
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		OpenML ml = new OpenML();
		ml.init();
		ml.getDataset();
		
		//IEnvironment env = new TradeEnvironment();
		//env.init();
		
		//startServer(env);
		
		/*Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.print("unn > ");
			String cmd = scanner.nextLine();			
			switch(cmd) {
			case "/print/stats":
				//tester.printStats();
				break;
			case "/print/report":
				//tester.printReport();
				break;
			}
		}*/
	}
	
	private static void startServer(IEnvironment env) {
		RESTApi api = new RESTApi(env);
		api.start();
	}
}
