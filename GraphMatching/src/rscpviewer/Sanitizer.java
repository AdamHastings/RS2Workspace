package rscpviewer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.byu.ece.rapidSmith.design.subsite.Cell;
import edu.byu.ece.rapidSmith.design.subsite.CellNet;
import edu.byu.ece.rapidSmith.design.subsite.CellPin;
import edu.byu.ece.rapidSmith.design.subsite.RouteStatus;

/**
 * We want this class to be able to construct an adjacency list out of cells
 *  remove cells that are unplaced or RapidSmith creations
 * @author sean
 */
public class Sanitizer 
{
	/**
	 * This function takes in a set of placed cells and returns a list of CellNets
	 * that connect them. The nets must be fully routed and mapped source and sink pins 
	 * @param cells - A set of placed cells
	 * @return a set of fully routed nets that have sink and sources placed as well
	 */
	public static Set<CellNet> getConnectingNets(Set<Cell> cells)
	{
		Set<CellNet> ret = new HashSet<CellNet>();
		
		//Each net should only be added once
		Set<CellNet> nets = new HashSet<CellNet>();
		for(Cell cell : cells)
			nets.addAll(cell.getNetList());
		
		//Grab only the nets that go between placed cells
		for(CellNet net : nets)
		{
			boolean skip = true;
			//Nets should be fully routed
			if(net.getRouteStatus() != RouteStatus.FULLY_ROUTED)
				continue;
			
			//check source pins
			for(CellPin sourcePin : net.getAllSourcePins())
			{
				//At least one source cell must be placed
				if(cells.contains(sourcePin.getCell()))
					skip = false;
			}
			if(skip)
				continue;
			skip = true;			//Reset the condition
			
			//check sink pins
			for(CellPin sinkPin : net.getSinkPins())
			{
				//At least one sink cell must be placed
				if(cells.contains(sinkPin.getCell()))
					skip = false;
			}
			if(skip)
				continue;
			
			ret.add(net);
		}
		
		return ret;
	}
	
	/**
	 * The resulting adjacency list should only be composed of cells that are placed and 
	 * are connected to at least one other cell in the list by a distance of one net
	 * @param cells
	 * @return
	 */
	public static Map<Cell, HashSet<Cell>> createAdjacencyList(Set<Cell> cells)
	{
		Map<Cell, HashSet<Cell>> ret = new HashMap<Cell, HashSet<Cell>>();
		
		//Each net should only be added once
		Set<CellNet> nets = new HashSet<CellNet>();
		for(Cell cell : cells)
			nets.addAll(cell.getNetList());
			
		for(CellNet net : nets)
		{
			for(CellPin sourcePin : net.getAllSourcePins())
			{
				Cell source = sourcePin.getCell();
				assert source.isPlaced();
				if(!cells.contains(source))
					continue;
				
				for(CellPin sinkPin : net.getSinkPins())
				{
					Cell sink = sinkPin.getCell();
					assert sink.isPlaced();
					if(!cells.contains(sink))
						continue;
					
					//From source to sink
					if(ret.get(source) == null)
					{
						HashSet<Cell> adjacencies = new HashSet<Cell>();
						adjacencies.add(sink);
						ret.put(source, adjacencies);
					}
					else
					{
						ret.get(source).add(sink);
					}
					//From sink to source
					if(ret.get(sink) == null)
					{
						HashSet<Cell> adjacencies = new HashSet<Cell>();
						adjacencies.add(source);
						ret.put(sink, adjacencies);
					}
					else
					{
						ret.get(sink).add(source);
					}
				}
			}
		}
		
		return ret;
	}
	
	/**
	 * Used to filter cells based on certain properties. Needs work.
	 * @param filter
	 * @param cells
	 * @return
	 */
	public static Set<Cell> filterCells(String filter, Set<Cell> cells)
	{
		//TODO implement this with enums if it proves to be helpful
		Set<Cell> ret = new HashSet<Cell>();
		
		switch(filter)
		{
			case "placed":
				for(Cell cell : cells)
					if(cell.isPlaced())
						ret.add(cell);
				break;
			default:
				
				break;
		}
		
		return ret;
	}
}
