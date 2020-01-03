package plugins.crypto.trade.bot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import unn.dataset.InnerDataset;
import unn.interfaces.IOperator;
import unn.mining.StatsWalker;
import unn.operations.OperatorDescriptor;
import unn.operations.RAW;
import unn.structures.Config;
import unn.structures.VTR;

public class PostgresManager // implements IDatabaseManager
{
	static String[] inputs = {
		"m1h",
		"m4h",
		"m8h",
		"m12h",
		"m16h",
		"m20h",
		"m24h",
		"m2d",
		"m3d",
		"m4d",
		"m5d",
		"m6d",
		"m7d",
		"m10d",
		"m13d",
		"m16d",
		"m19d",
		"m22d",
		"m25d",
		"m28d",
		"m31d",
	};

	static String output = "action";

	static Connection c = null;

	static ArrayList<IOperator> operators;
	static ArrayList<IOperator> operatorsWithReward;

	public static void init () {
		if (c != null) {
			return;
		}
		
		try {
    		Class.forName ("org.postgresql.Driver");
    		String connection_string = "jdbc:postgresql://rabbit.pt:5432/" + Config.DATABASE_NAME;
    		String username = Config.DATABASE_USERNAME;
    		String password = Config.DATABASE_PASSWORD;
    		c = DriverManager.getConnection (connection_string, username, password);
	    }
	    catch (Exception e) {
	       e.printStackTrace ();
	       System.err.println(e.getClass ().getName () + ": " + e.getMessage ());
	       System.exit (0);
	    }
	    
	    System.out.println ("Opened database successfully");
	}
	
	public static ArrayList<String> getMarkets() {
		init ();
		
		ArrayList<String> markets = new ArrayList<String>();
		
		try {
			c.setAutoCommit (false);
			
			PreparedStatement pst = c.prepareStatement("SELECT market FROM important_markets WHERE market LIKE '%BTC' LIMIT 1 OFFSET 0");
			ResultSet rs = pst.executeQuery();
			
			while (rs.next()) {
	        	String market = rs.getString("market");
	        	markets.add(market);
			}
			
			pst.close();
			c.commit();
			
			return markets;
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		return markets;
	}
	
	public static void saveModelStats(String market, StatsWalker statsWalker) {
		init ();
		
		Statement stmt = null;
		
		try {
			c.setAutoCommit (false);

			stmt = c.createStatement();
			
			int[][] hitMatrix = statsWalker.getHitMatrix();
			String[] colsUpper = new String[hitMatrix.length];
			String[] valsUpper = new String[hitMatrix.length];
			
			for (int i = 0; i < hitMatrix.length; ++i) {
				String[] cols = new String[hitMatrix[i].length];
				String[] vals = new String[hitMatrix[i].length];
				
				for (int j = 0; j < hitMatrix[i].length; ++j) {
					cols[j] = "confusion_" + i + j;
					vals[j] = Double.toString(hitMatrix[i][j]);
				}
				
				colsUpper[i] = String.join(", ", cols);
				valsUpper[i] = String.join(", ", vals);
			}
			
			String q = String.format("INSERT INTO models (market, %s) VALUES ('%s', %s)", String.join(", ", colsUpper), market, String.join(", ", valsUpper));
			PreparedStatement pst = c.prepareStatement(q);
	        pst.execute();
				
			stmt.close();
			c.commit();
		}
		catch (Exception e)
		{
			e.printStackTrace ();
			System.err.println(e.getClass ().getName () + ": " + e.getMessage ());
		}
	}
	
	public static InnerDataset select(String market) {
		init ();
		
		operators = getOperators();
		operatorsWithReward = getAllOperators();
		
		InnerDataset dataset = new InnerDataset();
		dataset.setTrainingLeaves(operators);
		dataset.setAllLeaves(operatorsWithReward);
		
		Statement stmt = null;
		
		try {
			c.setAutoCommit (false);

			stmt = c.createStatement();
			
			PreparedStatement pst = c.prepareStatement(String.format("SELECT utime, m1h, m4h, m8h, m12h, m16h, m20h, m24h, m2d, m3d, m4d, m5d, m6d, m7d, m10d, m13d, m16d, m19d, m22d, m25d, m28d, m31d, action FROM dataset_072019 WHERE market = '%s' ORDER BY rowid ASC", market));
	        ResultSet rs = pst.executeQuery();
	        	
	        while (rs.next()) {
	        	int time = (int) (rs.getLong("utime") / 1000);
	        	String action = rs.getString("action");
	        	int n = -1;
	        	
	        	if (/*"NEUTRAL".equals(action) || */"?".equals(action) || action == null) {
	        		continue;
	        	}
	        	
	        	int reward = Config.STIMULI_NULL_VALUE;
	        	
	        	if ("BUY".equals(action)) {
	        		reward = Config.STIMULI_MAX_VALUE;
	        	} else if ("SELL".equals(action)) {
	        		reward = Config.STIMULI_MIN_VALUE; 
		        } else if ("NEUTRAL".equals(action)) {
	        		reward = Config.STIMULI_NULL_VALUE;
	        	} else {
	        		assert false;
	        	}
	        	
	        	ArrayList<VTR> vtrs = new ArrayList<VTR>();
	        	
	        	for (String input : inputs) {
	        		n++;
	        		Object v = rs.getObject(input);
	        		
	        		if (v == null) {
	        			break;
	        		}
	        		
	        		VTR vtr = new VTR(operators.get(n), mapPrice((double) v), time, reward);
	        		vtrs.add(vtr);
	        	}
	        	
	        	if (vtrs.size() == inputs.length) {
	        		for (VTR vtr : vtrs) {
	    				dataset.add(vtr);
	    			}
	        		VTR vtr = new VTR(operatorsWithReward.get(operatorsWithReward.size() - 1), reward, time, reward);
		        	dataset.add(vtr);
	        	}
	        }
				
			stmt.close();
			c.commit();
		}
		catch (Exception e)
		{
			e.printStackTrace ();
			System.err.println(e.getClass ().getName () + ": " + e.getMessage ());
			System.exit (0);
		}
		
		return dataset;
	}
	
	public static ArrayList<IOperator> getOperators() {
		ArrayList<IOperator> operators = new ArrayList<IOperator>();
		int n = 0;
		for (String input : inputs) {
    		RAW bop = new RAW ();
    		bop.setDescriptor(new OperatorDescriptor (".", input.replace("m", "-"), n));
    		operators.add(bop);
    		n++;
    	}
		return operators;
	}
	
	public static ArrayList<IOperator> getAllOperators() {
		ArrayList<IOperator> operators = new ArrayList<IOperator>();
		int n = 0;
		for (String input : inputs) {
    		RAW bop = new RAW ();
    		bop.setDescriptor(new OperatorDescriptor (".", input.replace("m", "-"), n));
    		operators.add(bop);
    		n++;
    	}
		
		RAW bop = new RAW ();
		bop.setDescriptor(new OperatorDescriptor (".", "action", n));
		
		operators.add(bop);
		return operators;
	}
	
	private static Integer mapPrice(double price) {
		double absPrice = Math.abs(price);
		int absInput = 0;
		
		if (absPrice < 0.5) {
			absInput = 0;
		} else if (absPrice < 1.5) {
			absInput = 1;
		} else if (absPrice < 3.0) {
			absInput = 2;
		} else if (absPrice < 5.0) {
			absInput = 3;
		} else if (absPrice < 8.0) {
			absInput = 4;
		} else if (absPrice < 13.0) {
			absInput = 5;
		} else if (absPrice < 18.0) {
			absInput = 6;
		} else if (absPrice < 25.0) {
			absInput = 7;
		} else if (absPrice < 40.0) {
			absInput = 8;
		} else {
			absInput = 9;
		}
		
		if (price < 0) {
			absInput = -absInput;
		}
		
		return absInput;
	}
}
