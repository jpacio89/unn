package com.unn.engine;

public class Main {
	// private static Logger logger = Logger.getLogger(Shell.class.toString());
	
	public static void main(String[] args) {
		startServer(args);
	}
	
	private static void startServer(String[] args) {
		int port = args.length > 0 ? Integer.parseInt(args[0]) : 7000;
		Server api = new Server(port);
		api.start();
	}
}
