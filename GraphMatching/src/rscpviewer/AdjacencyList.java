package rscpviewer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.byu.ece.rapidSmith.design.subsite.Cell;
import edu.byu.ece.rapidSmith.design.subsite.CellNet;
import edu.byu.ece.rapidSmith.design.subsite.CellPin;
import edu.byu.ece.rapidSmith.device.BelPin;

public class AdjacencyList {
	
	// Data structure to store an adjacency list of a graph
	static Map<BelPin, ArrayList<BelPin>> adjacencyList = new HashMap<BelPin, ArrayList<BelPin>>();
	
	
	/**
	 * Add a node to the adjacencyList
	 * @param node
	 * @param edges
	 */
	public static void addNode(BelPin node, ArrayList<BelPin> edges)
	{
		adjacencyList.put(node, edges);
	}
	
	/**
	 * Create an adjacency list from a collection of cells
	 * @param cells_c
	 */
	public static void makeAdjacencyList(Collection<Cell> cells_c)
	{
		for (Cell c : cells_c)
		{
			Collection<CellPin> pins = c.getPins();
			for (CellPin p : pins)
			{
				ArrayList<BelPin> edges = new ArrayList<BelPin>();
				CellNet n = p.getNet();
				if (n != null) 
				{
					Set<BelPin> b = n.getBelPins();
					edges.addAll(b);

				}
				BelPin node = p.getMappedBelPin();
				AdjacencyList.addNode(node, edges);
			}
		}
	}
	
	/**
	 * Print out the adjacencyList
	 * TODO: Writing to a file seems to be broken. I can't get the files to open up 
	 * after they've been written.
	 */
	public static void printAdjacencyList() {
		System.out.println("BelPin Count: " + adjacencyList.size());
		try {
			FileWriter fw = new FileWriter("test.txt");
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("BelPin Count: " + adjacencyList.size() + "\n");		
			for (Map.Entry<BelPin, ArrayList<BelPin>> entry : adjacencyList.entrySet()) {
				BelPin key = entry.getKey();
				ArrayList<BelPin> value = entry.getValue();
				if (key != null) 
				{
					bw.write(key.toString() + ": " + value.toString() + "\n");
					System.out.print(key.toString() + ": " + value.toString() + "\n");
				}
			}
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
