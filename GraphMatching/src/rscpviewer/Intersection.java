package rscpviewer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import edu.byu.ece.rapidSmith.design.subsite.CellDesign;
import edu.byu.ece.rapidSmith.design.subsite.CellNet;
import edu.byu.ece.rapidSmith.interfaces.vivado.VivadoInterface;

public class Intersection {
	public static ArrayList<Collection<CellNet>> getCommonNets (String filename_top, String filename_ooc) {
		
		// CellDesign objects to be used
		CellDesign design_top = null;
		CellDesign design_ooc = null;
		
		// try to open the designs and place them into a CellDesign object
		try {
			design_top = VivadoInterface.loadTCP(filename_top).getDesign();
			design_ooc = VivadoInterface.loadTCP(filename_ooc).getDesign();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Collections of CellNets to be later turned into lists
		Collection<CellNet> nets_top = design_top.getNets();
		Collection<CellNet> nets_ooc = design_ooc.getNets();
		
		// A list of CellNets found in design_top
		ArrayList<CellNet> netlist_top = new ArrayList<CellNet>(nets_top);
		// A list of CellNets found in design_ooc
		ArrayList<CellNet> netlist_ooc = new ArrayList<CellNet>(nets_ooc);
		
		// The names of the CellNets in design_top
		ArrayList<String> netnames_top = new ArrayList<String>();
		// The names of the CellNets in design_ooc
		ArrayList<String> netnames_ooc = new ArrayList<String>();
		
		// The name of the OOC module (This would best be passed in as a parameter)
		String moduleName = "aes_128_0" + "/";
		
		// Fill netnames_top with the names of the CellNets in netlist_top
		for (CellNet n : netlist_ooc) {
			netnames_ooc.add(n.getName());
		}

		// Fill netnames_ooc with the names of the CellNets in netlist_ooc
		for (CellNet n : netlist_top) {
			if (n.getName().startsWith(moduleName)) {
				netnames_top.add(n.getName().replaceFirst("^" + moduleName, ""));
			} else if (n.getName().equals("<const0>") || n.getName().equals("<const1>")) {
				System.out.println("adding const " + n.getName());
				netnames_top.add(n.getName());
			}
		}

		// A list to store the intersection of the two netnames lists
		// Initialize it with netnames_top and remove non-intersecting items later
		ArrayList<String> intersection = new ArrayList<String>(netnames_top);
		
		// Make a copy so that we don't modify a list while iterating over it
		ArrayList<String> intersection_copy = new ArrayList<String>(netnames_top);
		
		// Remove non-intersecting items
		System.out.println("before: " + intersection.size());
		for (String s : intersection_copy) {
			if (!netnames_ooc.contains(s)) {
				intersection.remove(s);
				System.out.println("removing " + s);
			}
		}	

		// A collection of CellNets whose names are found in the intersection
		Collection<CellNet> commonElements_top = new ArrayList<CellNet>();
		Collection<CellNet> commonElements_ooc = new ArrayList<CellNet>();
		
		
		// Add full CellNet objects to the collections based solely on name
		for (String s : intersection) {		
			commonElements_top.add(design_top.getNet(moduleName + s));
			commonElements_ooc.add(design_ooc.getNet(s));
		}
		
		// Add collections to an ArrayList and return the ArrayList
		ArrayList<Collection<CellNet>> array = new ArrayList<Collection<CellNet>>();
		array.add(commonElements_top);
		array.add(commonElements_ooc);
		
		return array;
	}
}
