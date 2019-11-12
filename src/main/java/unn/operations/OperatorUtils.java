package unn.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import unn.interfaces.IOperator;

public class OperatorUtils 
{
	public static ArrayList<IOperator> getParameters (IOperator root)
	{
		ArrayList<IOperator> leaves = new ArrayList<IOperator> ();
		LinkedList<IOperator> queue = new LinkedList<IOperator> ();
		HashMap<IOperator, IOperator> cache = new HashMap<IOperator, IOperator> ();
		
		queue.add (root);
		
		while (queue.size () > 0)
		{
			IOperator node = queue.poll ();
			
			if (node.children () == null)
			{
				if (!cache.containsKey (node))
				{
					cache.put (node, node);
					
					if (node.isParameter()) {
						leaves.add (node);
					}	
				}
			}
			else
			{
				IOperator[] children = node.children();
				
				queue.add (children[0]);
				queue.add (children[1]);
			}
		}
		
		return leaves;
	}
	
}
