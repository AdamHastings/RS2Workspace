package rscpviewer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import edu.byu.ece.rapidSmith.design.subsite.Cell;
import edu.byu.ece.rapidSmith.design.subsite.CellDesign;
import edu.byu.ece.rapidSmith.design.subsite.CellNet;
import edu.byu.ece.rapidSmith.design.subsite.CellPin;
import edu.byu.ece.rapidSmith.interfaces.vivado.TincrCheckpoint;
import edu.byu.ece.rapidSmith.interfaces.vivado.VivadoInterface;

/**
 * The purpose of this class is to construct a graph cell-by-cell, starting first
 * with a single cell in each graph that the user says is the same.
 * @author sean
 *
 */
public class CellByCell 
{
	static final String pathToGraphGrep = "/home/sean/applications/GraphGrepSX_v3.3_lE/";
	static final String pathToVivadoWorkspace = "/home/sean/vivado_workspace/";

	public static void main(String[] args) 
	{
		TincrCheckpoint goldStandard;
		TincrCheckpoint tcp;
		try 
		{
			//Counter
			goldStandard = VivadoInterface.loadTCP(pathToVivadoWorkspace + "ooc_counter/counter0_gs.tcp");
			tcp = VivadoInterface.loadTCP(pathToVivadoWorkspace + "counter_top/counter_top.tcp");
		} catch (IOException e) {
			System.out.println("It didn't work");
			e.printStackTrace();
			return;
		}
		
		//Get both anchors
		CellDesign goldDesign = goldStandard.getDesign();
		Set<Cell> cells_gs = new HashSet<Cell>(goldDesign.getCells());
		Cell anchor_gs = goldDesign.getCell("blink_o_reg");
		System.out.println("Gold anchor: " + anchor_gs);
		
		CellDesign cd = tcp.getDesign();
		Stream<Cell> macroCellStream = cd.getMacros();
		ArrayList<Cell> macroCells = new ArrayList<Cell>();
		macroCellStream.forEach(element -> macroCells.add(element));
		assert macroCells.size() == 1;
		Cell mc = macroCells.get(0);
		Set<Cell> cells = new HashSet<Cell>(mc.getInternalCells());
		Cell anchor = cd.getCell("counter0/blink_o_reg");
		System.out.println("Anchor: " + anchor);
		
		for(CellNet net : anchor.getNetList())
		{
			System.out.println(net);
			System.out.println("\tIs static net: " + net.isStaticNet());
			for(CellPin cellPin : net.getPins())
			{
				System.out.println("\t\t" + cellPin.getDirection() + " " + cellPin.getCell());
			}
		}
		
	}
}