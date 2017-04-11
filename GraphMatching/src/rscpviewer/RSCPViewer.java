package rscpviewer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.byu.ece.rapidSmith.design.subsite.Cell;
import edu.byu.ece.rapidSmith.design.subsite.CellDesign;
import edu.byu.ece.rapidSmith.design.subsite.CellNet;
import edu.byu.ece.rapidSmith.design.subsite.Property;
import edu.byu.ece.rapidSmith.design.subsite.PropertyList;
import edu.byu.ece.rapidSmith.device.Bel;
import edu.byu.ece.rapidSmith.interfaces.vivado.TincrCheckpoint;
import edu.byu.ece.rapidSmith.interfaces.vivado.VivadoInterface;

public class RSCPViewer {

	private static void printCellVerbose(Cell c) {
		System.out.println("getName() " + c.getName());
		System.out.println("getPseudoPinCount() " + c.getPseudoPinCount());
		System.out.println("getType() " + c.getType());
		System.out.println("isGndSource() " + c.isGndSource());
		System.out.println("isInDesign() " + c.isInDesign());
		System.out.println("isInternal() " + c.isInternal());
		System.out.println("isLut() " + c.isLut());
		System.out.println("isMacro() " + c.isMacro());
		System.out.println("isPlaced() " + c.isPlaced());
		System.out.println("isPort() " + c.isPort());
		System.out.println("isVccSource() " + c.isVccSource());
		System.out.println("getBel() " + c.getBel());
		System.out.println("getBonded() " + c.getBonded());
		System.out.println("getClass() " + c.getClass());
		System.out.println("getDesign() " + c.getDesign());
		System.out.println("getInputPins() " + c.getInputPins());
		System.out.println("getInternalCells() " + c.getInternalCells());
		System.out.println("getInternalNets() " + c.getInternalNets());
		System.out.println("getLibCell() " + c.getLibCell());
		System.out.println("getNetList() " + c.getNetList());
		System.out.println("getOutputPins() " + c.getOutputPins());
		System.out.println("getParent() " + c.getParent());
		System.out.println("getPins() " + c.getPins());
		System.out.println("getPossibleAnchors() " + c.getPossibleAnchors());
		System.out.println("getProperties() " + c.getProperties());
		System.out.println("getPseudoPins() " + c.getPseudoPins());
		System.out.println("getSite() " + c.getSite());
		System.out.println("getSubcells() " + c.getSubcells());
	}

	public static void runTest(String[] args) {
		TincrCheckpoint goldStandard;
		TincrCheckpoint tcp;
		try {
			goldStandard = VivadoInterface.loadTCP("/home/adam/vivado_workspace/ooc_counter/counter0_gs.tcp");
			tcp = VivadoInterface.loadTCP("/home/adam/vivado_workspace/counter_top/counter_top.tcp");
		} catch (IOException e) {
			System.out.println("It didn't work");
			e.printStackTrace();
			return;
		}

		CellDesign goldDesign = goldStandard.getDesign();
		Collection<Cell> cells_c_gs = goldDesign.getCells();
		ArrayList<Cell> goldCells = new ArrayList<Cell>();
		ArrayList<Bel> goldBels = new ArrayList<Bel>();
		ArrayList<String> goldCellNames = new ArrayList<String>();

		HashSet<CellNet> goldNets = new HashSet<CellNet>(goldDesign.getNets());

		
		for (Cell c : cells_c_gs) {
			if (c == null) {
				System.out.println("We got a null cell.");
				return;
			}
			Bel b = c.getBel();
			if (b == null) { // Cells that end up here are ports for OOC modules
								// and VCC and Ground because they're not placed
								// //cellsNotIncluded.add(c);
				System.out.println("We got a null bel for cell " + c.toString() + "."); // printCellVerbose(c);
			} else {
				String type = b.getType();
				if (type.matches("(.*)LUT(.*)")) {
					PropertyList properties = c.getProperties();
					Property equation = properties.get("INIT");
					System.out.println(c.toString() + ", " + b.toString() + ", " + equation.toString());
				} else if (type.matches("(.*)RAM(.*)")) {
					System.out.println(c.toString() + ", " + b.toString());

					PropertyList properties = c.getProperties();
					for (Property p : properties) {
						if (p.getKey().matches(".*INIT.*")) {
							if (p.getValue().toString().matches("\\d*\\'[h|H]\\p{XDigit}*")) {
								System.out.println(p.toString());
							}
						}
					}
				} else {
					System.out.println(c.toString() + ", " + b.toString());
				}

				goldCells.add(c);
				goldBels.add(b);
				goldCellNames.add(c.getName());
			}
		}
		System.out.println("Number of cells: " + goldCells.size());
		System.out.println("Number of bels: " + goldBels.size());
		

		CellDesign cd = tcp.getDesign();
		Collection<Cell> cells_c = cd.getCells();
		ArrayList<Cell> cells = new ArrayList<Cell>();
		ArrayList<Bel> bels = new ArrayList<Bel>();
		ArrayList<String> cellNames = new ArrayList<String>();

		HashSet<CellNet> nets = new HashSet<CellNet>(cd.getNets());
		
		for (Cell c : cells_c) {
			if (c == null) {
				System.out.println("We got a null cell.");
				return;
			}
			Bel b = c.getBel();
			if (b == null) { // Cells that end up here are ports for OOC modules
								// and VCC and Ground because they're not placed
				// cellsNotIncluded.add(c);
				System.out.println("We got a null bel for cell " + c.toString() + "."); // printCellVerbose(c);
			} else {
				String type = b.getType();
				if (type.matches("(.*)LUT(.*)")) {
					PropertyList properties = c.getProperties();
					Property equation = properties.get("INIT");
					System.out.println(c.toString() + ", " + b.toString() + ", " + equation.toString());
				} else if (type.matches("(.*)RAM(.*)")) {
					System.out.println(c.toString() + ", " + b.toString());
					PropertyList properties = c.getProperties();
					for (Property p : properties) {
						if (p.getKey().matches(".*INIT.*")) {
							if (p.getValue().toString().matches("\\d*\\'[h|H]\\p{XDigit}*")) {
								System.out.println(p.toString());
							}
						}
					}
				} else {
					System.out.println(c.toString() + ", " + b.toString());

				}
				cells.add(c);
				bels.add(b);
				cellNames.add(c.getName());

			}
		}
		System.out.println("Number of cells: " + cells.size());
		System.out.println("Number of bels: " + bels.size());
		System.out.println("Cells Not Included: ");
		

		List<String> goldNetsNames = goldNets.stream().map(CellNet::getName).collect(Collectors.toList());
		List<String> netsNames = new ArrayList<String>(); // =
															// nets.stream().map(CellNet::getName).collect(Collectors.toList());
		System.out.println("==========================================");
		// System.out.println(goldNets.toString());
		// Collections.sort((List<>) goldNets);
		// System.out.println(goldNetsNames.toString());

		// System.out.println(nets.toString());
		for (CellNet n : nets) {
			String netName = n.getName();
			String[] hierarchicalCells = netName.split("/");
			// System.out.println(hierarchicalCells.length);
			// System.out.println(Arrays.toString(hierarchicalCells));
			netsNames.add(hierarchicalCells[hierarchicalCells.length - 1]);
		}
		// System.out.println(netsNames.toString());

		// List<String> sortedGoldNetNames = new
		// ArrayList<String>(goldNetsNames);
		// List<String> sortedNetNames = new ArrayList<String>(netsNames);
		Collections.sort(goldNetsNames);
		Collections.sort(netsNames);
		System.out.println(goldNetsNames.toString());
		System.out.println(netsNames.toString());
		System.out.println("==========================================");

		HashSet<CellNet> nets_intersection = new HashSet<CellNet>(goldNets);
		nets_intersection.retainAll(nets);
		// System.out.println(nets_intersection.toString());
		HashSet<String> net_nameIntersection = new HashSet<String>(goldNetsNames);
		net_nameIntersection.retainAll(netsNames);
		List<String> namesIntersection = new ArrayList<String>(net_nameIntersection);
		Collections.sort(namesIntersection);
		System.out.println(namesIntersection);

		System.out.println("\n=========================\nFound in goldNetsNames, but not intersection");
		goldNetsNames.removeAll(namesIntersection);
		System.out.println(goldNetsNames.toString());

		// Collections.sort(goldCellNames);
		// Collections.sort(cellNames);
		
		 Set<String> goldSet = new HashSet<String>(goldCellNames); 
		 Set<String> cellSet = new HashSet<String>(cellNames); 
		 Set<String> intersection = new HashSet<String>(goldSet); 
		 intersection.retainAll(cellNames);
		 System.out.println(goldSet.toString());
		 System.out.println(cellSet.toString()); List<String>
		 intersection_list = new ArrayList<String>(intersection);
		 Collections.sort(intersection_list);
		 System.out.println(intersection_list.toString());
		 
		 // Note to self: This problem may be and induced subgraph isomorphism problem, not just a subgraph isomorphism problem.
		 // Know the difference!!

		// AdjacencyList.makeAdjacencyList(cells);
		// AdjacencyList.printAdjacencyList();
	}
	

}