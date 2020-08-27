// import java.util.logging.Logger;
import server.RESTApi;

public class Shell {
	// private static Logger logger = Logger.getLogger(Shell.class.toString());
	
	public static void main(String[] args) throws Exception {		
		startServer(args);
		
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
	
	private static void startServer(String[] args) {
		int port = args.length > 0 ? Integer.parseInt(args[0]) : 7000;
		RESTApi api = new RESTApi(port);
		api.start();
	}
}
