import server.RestServer;

public class Shell {
	// private static Logger logger = Logger.getLogger(Shell.class.toString());
	
	public static void main(String[] args) throws Exception {		
		startServer(args);
	}
	
	private static void startServer(String[] args) {
		int port = args.length > 0 ? Integer.parseInt(args[0]) : 7000;
		RestServer api = new RestServer(port);
		api.start();
	}
}
