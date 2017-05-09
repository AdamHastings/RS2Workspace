package rscpviewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import edu.byu.ece.rapidSmith.design.subsite.Cell;
import edu.byu.ece.rapidSmith.design.subsite.CellDesign;
import edu.byu.ece.rapidSmith.design.subsite.CellNet;
import edu.byu.ece.rapidSmith.interfaces.vivado.TincrCheckpoint;
import edu.byu.ece.rapidSmith.interfaces.vivado.VivadoInterface;

public class RSCPViewer 
{
	static final String pathToGraphGrep = System.getProperty("user.home") + "/applications/GraphGrepSX_v3.3_lE/";
	static final String pathToVivadoWorkspace = System.getProperty("user.home") + "/vivado_workspace/";
	private static String oocGraphName;
	private static String implGraphName;
	
	@SuppressWarnings("unused")
	private static void printCellVerbose(Cell c)
	{
		System.out.println("getName() "+c.getName());
		System.out.println("getPseudoPinCount() "+c.getPseudoPinCount());
		System.out.println("getType() "+c.getType());
		System.out.println("isGndSource() "+c.isGndSource());
		System.out.println("isInDesign() "+c.isInDesign());
		System.out.println("isInternal() "+c.isInternal());
		System.out.println("isLut() "+c.isLut());
		System.out.println("isMacro() "+c.isMacro());
		System.out.println("isPlaced() "+c.isPlaced());
		System.out.println("isPort() "+c.isPort());
		System.out.println("isVccSource() "+c.isVccSource());
		System.out.println("getBel() "+c.getBel());
		System.out.println("getBonded() "+c.getBonded());
		System.out.println("getClass() "+c.getClass());
		System.out.println("getDesign() "+c.getDesign());
		System.out.println("getInputPins() "+c.getInputPins());
		System.out.println("getInternalCells() "+c.getInternalCells());
		System.out.println("getInternalNets() "+c.getInternalNets());
		System.out.println("getLibCell() "+c.getLibCell());
		System.out.println("getNetList() "+c.getNetList());
		System.out.println("getOutputPins() "+c.getOutputPins());
		System.out.println("getParent() "+c.getParent());
		System.out.println("getPins() "+c.getPins());
		System.out.println("getPossibleAnchors() "+c.getPossibleAnchors());
		System.out.println("getProperties() "+c.getProperties());
		System.out.println("getPseudoPins() "+c.getPseudoPins());
		System.out.println("getSite() "+c.getSite());
		System.out.println("getSubcells() "+c.getSubcells());
	}
	
	/**
	 * taken from https://www.mkyong.com/java/how-to-execute-shell-command-from-java/
	 * @param command
	 * @return
	 */
	public static String executeCommand(String command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader =
                            new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();
	}

	public static void main(String[] args) 
	{
		TincrCheckpoint goldStandard;
		TincrCheckpoint tcp;

		///////////////////////////////////////////////////////
		//Uncomment out the pair of tcp's you want to work with
		///////////////////////////////////////////////////////
		
		//Counter
		oocGraphName = "ooc_counter";
		implGraphName = "counter_top";
		
		//AES
//		oocGraphName = "ooc_aes128";
//		implGraphName = "aes128_top";
		
		try 
		{
			goldStandard = VivadoInterface.loadTCP(pathToVivadoWorkspace + oocGraphName + '/' + oocGraphName + ".tcp");
			tcp = VivadoInterface.loadTCP(pathToVivadoWorkspace + implGraphName + '/' + implGraphName + ".tcp");
		} catch (IOException e) {
			System.out.println("It didn't work");
			e.printStackTrace();
			return;
		}
		
		//The sets that we typically deal with are cells_gs_placed and nets_gs_placed,
		//  because those are located in physically verified locations in the fabric.
		CellDesign goldDesign = goldStandard.getDesign();
		Set<Cell> cells_gs = new HashSet<Cell>(goldDesign.getCells());
		Set<CellNet> nets_gs = new HashSet<CellNet>(goldDesign.getNets());
		Set<Cell> cells_gs_placed = Sanitizer.filterCells("placed", cells_gs);
		Set<Cell> cells_gs_notplaced = new HashSet<Cell>(cells_gs);
		cells_gs_notplaced.removeAll(cells_gs_placed);
		Set<CellNet> nets_gs_placed = Sanitizer.getConnectingNets(cells_gs_placed);
		Set<CellNet> nets_gs_notplaced = new HashSet<CellNet>(nets_gs);
		nets_gs_notplaced.removeAll(nets_gs_placed);
		System.out.println("Design " + oocGraphName + " has " + nets_gs.size() + " (" +
				nets_gs_placed.size() + ") nets and " + cells_gs.size() + " (" + 
				cells_gs_placed.size() + ") placed cells.");
		assert oocGraphName != null;
		//Create the graph file or the expanded graph file
		GraphGrepSX.createExpandedGraphFile(oocGraphName, cells_gs_placed, nets_gs_placed);
		//Run graph grep to generate the database
		System.out.println(executeCommand(pathToGraphGrep + "ggsxe -b -ged IO/" + oocGraphName + "_ggsx"));
		

		//The sets that we typically deal with are cells_placed and nets_placed,
		//  because those are located in physically verified locations in the fabric.
		CellDesign cd = tcp.getDesign();
		///////////////////////////////////////////////////////////////////////
		//This is how this needs to happen in RapidSmith2 as of pull request #230
		Stream<Cell> macroCellStream = cd.getMacros();
		ArrayList<Cell> macroCells = new ArrayList<Cell>();
		macroCellStream.forEach(element -> macroCells.add(element));
		assert macroCells.size() == 1;
		Cell mc = macroCells.get(0);
		///////////////////////////////////////////////////////////////////////
		Set<Cell> cells = new HashSet<Cell>(mc.getInternalCells());
		Set<CellNet> nets = new HashSet<CellNet>(mc.getInternalNets());
		Set<Cell> cells_placed = Sanitizer.filterCells("placed", cells);
		Set<Cell> cells_notplaced = new HashSet<Cell>(cells);
		cells_notplaced.removeAll(cells_placed);
		Set<CellNet> nets_placed = Sanitizer.getConnectingNets(cells_placed);
		//We do this because when output pins are connected to input pins the algorithm gives wrong ouptut
		nets_placed.removeAll(mc.getNetList());
		Set<CellNet> nets_notplaced = new HashSet<CellNet>(nets);
		nets_notplaced.removeAll(nets_placed);
		System.out.println("Design " + implGraphName + " has " + nets.size() + " (" +
				nets_placed.size() + ") nets and " + cells.size() + " (" + 
				cells_placed.size() + ") placed cells.");
		assert implGraphName != null;
		GraphGrepSX.createExpandedGraphFile(implGraphName, cells_placed, nets_placed);
		System.out.println(executeCommand(pathToGraphGrep + "ggsxe -f -ged IO/" + oocGraphName + "_ggsx IO/" + implGraphName + "_ggsx"));
	}
}