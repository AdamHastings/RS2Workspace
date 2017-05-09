package rscpviewer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import edu.byu.ece.rapidSmith.design.subsite.Cell;
import edu.byu.ece.rapidSmith.design.subsite.CellDesign;
import edu.byu.ece.rapidSmith.design.subsite.CellNet;
import edu.byu.ece.rapidSmith.design.subsite.CellPin;
import edu.byu.ece.rapidSmith.interfaces.vivado.TincrCheckpoint;
import edu.byu.ece.rapidSmith.interfaces.vivado.VivadoInterface;

/**
 * The purpose of this class is to facilitate checking graphs with GraphGrep by backtracking
 * @author sean
 *
 */
public class Backtracking 
{
	private static String tcpToLoad;
	private static String findMatches;
	private static String graphName;
	private static String database;
	private static Map<Cell, HashSet<Cell>> adjacencyList;
	private static Map<HashSet<Cell>, Boolean> setOutcomes;
	
	private static void createAdjacencyList(Set<CellNet> nets)
	{
		adjacencyList = new HashMap<Cell, HashSet<Cell>>();
		for(CellNet net : nets)
		{
			for(CellPin sourcePin : net.getAllSourcePins())
			{
				Cell source = sourcePin.getCell();
				assert source.getBel() != null;
				
				for(CellPin sinkPin : net.getSinkPins())
				{
					Cell sink = sinkPin.getCell();
					assert sink.getBel() != null;
					
					//From source to sink
					if(adjacencyList.get(source) == null)
					{
						HashSet<Cell> adjacencies = new HashSet<Cell>();
						adjacencies.add(sink);
						adjacencyList.put(source, adjacencies);
					}
					else
					{
						adjacencyList.get(source).add(sink);
					}
					//From sink to source
					if(adjacencyList.get(sink) == null)
					{
						HashSet<Cell> adjacencies = new HashSet<Cell>();
						adjacencies.add(source);
						adjacencyList.put(sink, adjacencies);
					}
					else
					{
						adjacencyList.get(sink).add(source);
					}
				}
			}
		}
	}
	
	/**
	 * Test validity of a solution
	 * @param moves
	 * @return
	 */
	private static boolean isNotASolution(List<Cell> moves)
	{
		if(moves.size() == 0)
			return false;
		//This should check the output of the graph grep tool
		long size = 1;
		size = new File("matches").length();
		if(size < 1)
		{
//			System.out.println("No Matches");
			setOutcomes.put(new HashSet<Cell>(moves), false);
			return true;
		}
		else
		{
//			System.out.println("Matches Found");
			setOutcomes.put(new HashSet<Cell>(moves), true);
			return false;
		}
	}
	
	/**
	 * Called each time a new solution is found
	 * @param moves
	 */
	private static void processSolution(List<Cell> moves)
	{
		//This will store the moves along with some score or something
//		System.out.println("This best is " + thisBest.size());
	}
	
	/**
	 * Find valid next moves, ordered by best or most constrained. Specifically
	 * we want any candidate to be connected to the most recent move.
	 * @param moves
	 * @param candidates
	 */
	private static void constructCandidates(List<Cell> moves, Set<Cell> candidates)
	{
		//We want to start a graph at all of the nodes
		if(moves.size() == 0)
		{
			candidates.addAll(adjacencyList.keySet());
			return;
		}
		
		for(Cell move : moves)
		{
			for(Cell candidate : adjacencyList.get(move))
			{
				if(moves.contains(candidate))
					continue;
				
				candidates.add(candidate);
			}
		}
	}
	
	/**
	 * Recursive backtrack function
	 * @param move
	 */
	private static void backTrack(List<Cell> moves)
	{
		//Local variables on the stack
		Set<Cell> candidates = new HashSet<Cell>();
		
		if(isNotASolution(moves))
			processSolution(moves);
		else
		{
			constructCandidates(moves, candidates);
			for(Cell cell : candidates)
			{
				Set<HashSet<Cell>> keyset = setOutcomes.keySet();
				
				Set<Cell> movesSet = new HashSet<Cell>(moves);
				movesSet.add(cell);
				if(keyset.contains(movesSet))
					continue;
				
				moves.add(cell);
				System.out.println(moves.size() + " nodes");
				Set<CellNet> nets = Sanitizer.getConnectingNets(new HashSet<Cell>(moves));
				
				//create the graph for the next call to backtrack
				GraphGrepSX.createExpandedGraphFile(graphName, new HashSet<Cell>(moves), nets);
				
//				System.out.println(RSCPViewer.executeCommand(findMatches));
				RSCPViewer.executeCommand(findMatches);
				
				backTrack(moves);
				
				moves.remove(cell);
			}
		}
	}
	
	public static void main(String[] args) 
	{	
		TincrCheckpoint tcp;
		setOutcomes = new HashMap<HashSet<Cell>, Boolean>();
		try 
		{
			//Counter
			tcpToLoad = RSCPViewer.pathToVivadoWorkspace + "counter_top/counter_top.tcp";
			tcp = VivadoInterface.loadTCP(tcpToLoad);
			graphName = "counter0";
			
			//AES
//			tcp = VivadoInterface.loadTCP("/home/sean/vivado_workspace/aes128_top/aes128_top.tcp");
//			graphName = "aes_128_0";
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		database = "IO/" + graphName + "_gs_ggsx";
		findMatches = RSCPViewer.pathToGraphGrep + "ggsxe -f -ged " + database + " IO/" + graphName + "_ggsx --one-match";

		CellDesign cd = tcp.getDesign();
		Stream<Cell> macroCellStream = cd.getMacros();
		ArrayList<Cell> macroCells = new ArrayList<Cell>();
		macroCellStream.forEach(element -> macroCells.add(element));
		assert macroCells.size() == 1;
		Cell mc = macroCells.get(0);
		Set<Cell> cells = new HashSet<Cell>(mc.getInternalCells());
		Set<CellNet> nets = Sanitizer.getConnectingNets(cells);
		
		//Create adjacency list
		createAdjacencyList(nets);
		
		//Call backtrack
		backTrack(new ArrayList<Cell>());
		
//		int bestSize = 0;
//		ArrayList<Cell> bestBest = null;
//		for(ArrayList<Cell> thisBest : bestList)
//			if(thisBest.size() > bestSize)
//			{
//				bestSize = thisBest.size();
//				bestBest = thisBest;
//			}
//		System.out.println("With a size of " + bestSize + " the following cells were found in the golden standard:");
//		System.out.println(bestBest);
//		
//		Set<CellNet> bestNets = RSCPViewer.sanitizeCellsAndNets(new HashSet<Cell>(bestBest));
//		
//		//create the graph for the next call to backtrack
//		GraphGrepSX.createFile(graphName, new HashSet<Cell>(bestBest), bestNets);
//		
//		RSCPViewer.executeCommand(findMatches);
	}
}
