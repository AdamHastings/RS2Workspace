package rscpviewer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import edu.byu.ece.rapidSmith.design.subsite.Cell;
import edu.byu.ece.rapidSmith.design.subsite.CellDesign;
import edu.byu.ece.rapidSmith.design.subsite.CellNet;
import edu.byu.ece.rapidSmith.design.subsite.CellPin;
import edu.byu.ece.rapidSmith.design.subsite.Property;
import edu.byu.ece.rapidSmith.design.subsite.PropertyList;
import edu.byu.ece.rapidSmith.interfaces.vivado.VivadoInterface;

public class exploreLUTPermutations {

	public static void explore() {
		String filename1 = "/home/adam/vivado_workspace/aes128_top/aes128_top.tcp";
		String filename2 = "/home/adam/vivado_workspace/ooc_aes128/ooc_aes128.tcp";
		
		
		// CellDesign objects to be used
		CellDesign design1 = null;
		CellDesign design2 = null;
		
		// try to open the designs and place them into a CellDesign object
		try {
			design1 = VivadoInterface.loadTCP(filename1).getDesign();
			design2 = VivadoInterface.loadTCP(filename2).getDesign();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ArrayList<Collection<CellNet>> intersection_list = Intersection.getCommonNets(filename1, filename2);

		// A set to contain all pins connected to the nets
		HashSet<String> CellPins_set = new HashSet<String>();

		// Get all pins connected to all nets
		for (CellNet n : intersection_list.get(0)) {
			// Get all pins connected to this net
			Collection<CellPin> cellpins = n.getPins();
			
			// Add each of the pins to the goldcellPins_set
			for (CellPin pin : cellpins) {		
				CellPins_set.add(pin.getFullName());
			}
		}

		String moduleName = "aes_128_0" + "/";

		// Place the goldCellPins_set into a list
		ArrayList<String> CellPins_list = new ArrayList<String>(CellPins_set);
		// Sort the list in lexicographical order
		Collections.sort(CellPins_list, new Comparator<String>() {
			public int compare(String a, String b) {
				return a.compareTo(b);
			}
		});

		Collection<Cell> cells1 = design1.getCells();
		Collection<Cell> cells2 = design2.getCells();
//		
//		for (Cell c : cells1) {
//			if (c.getName().equals(moduleName + "state_out[9]_i_1")) {
//				System.out.println("found in cells1");
//			}
//		}
//		int i = 0;
//		for (Cell c : cells2) {
//			if (c.getName().equals("state_out[9]_i_1")) {
//				System.out.println("found in cells2");
//			}
//			System.out.println(c.getType().toString() + " --" + c.getName());
//			i++;
//			if (i == 200) {
//				break;
//			}
//		}
		
		Cell c1 = design1.getCell(moduleName + "state_out[89]_i_1__7");
		Cell c2 = design2.getCell("state_out[89]_i_1__7");
		
		if (c1 == null) {
			System.out.println("NULL cell c1");
		} else if (c2 == null) {
			System.out.println("NULL cell c2");
		} else {
			Collection<CellPin> cp1 = c1.getPins();
			Collection<CellPin> cp2 = c2.getPins();
			for (CellPin p : cp1) {
				System.out.println("Pin " + p.getFullName() + ", connected to net " + p.getNet() + ", at bel " + p.getMappedBelPin());
			}
			PropertyList properties1 = c1.getProperties();
			Property equation1 = properties1.get("INIT");
			System.out.println(equation1.toString());
			
			System.out.println("-------");
			for (CellPin p : cp2) {
				System.out.println("Pin " + p.getFullName() + ", connected to net " + p.getNet() + ", at bel " + p.getMappedBelPin());
			}
			PropertyList properties2 = c2.getProperties();
			Property equation2 = properties2.get("INIT");
			System.out.println(equation2.toString());
		}

//		for (Cell c : cells) {
//			// System.out.println(c.getType() + " -- " + c.getName());
//			if (c.getName().equals("state_out[89]_i_1__8")) {
//				System.out.println("found 8");
//			}
//		}
		
		// Using this LUT6 as an example: state_out[89]_i_1__7
	}
}
