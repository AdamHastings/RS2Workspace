package rscpviewer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import edu.byu.ece.rapidSmith.design.subsite.Cell;
import edu.byu.ece.rapidSmith.design.subsite.CellDesign;
import edu.byu.ece.rapidSmith.design.subsite.CellNet;
import edu.byu.ece.rapidSmith.design.subsite.CellPin;
import edu.byu.ece.rapidSmith.design.subsite.Property;
import edu.byu.ece.rapidSmith.design.subsite.PropertyList;
import edu.byu.ece.rapidSmith.device.BelPin;
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

		
//		for (Cell c : cells1) {
//			if (c.getName().equals(moduleName + "r5/t2/t2/s0/out_reg")) {
//				System.out.println("found in cells1");
//			}
//		}
//
//		for (Cell c : cells2) {
//			if (c.getName().equals("r5/t2/t2/s0/out_reg")) {
//				System.out.println("found in cells2");
//			}
////			System.out.println(c.getType().toString() + " --" + c.getName());
////			i++;
////			if (i == 200) {
////				break;
////			}
//		}
		
		Cell c1 = design1.getCell(moduleName + "r5/t2/t2/s0/out_reg");
		Cell c2 = design2.getCell("r5/t2/t2/s0/out_reg");
		
		if (c1 == null) {
			System.out.println("NULL cell c1");
		} else if (c2 == null) {
			System.out.println("NULL cell c2");
		} else {
			Collection<CellPin> cp1 = c1.getPins();
			Collection<CellPin> cp2 = c2.getPins();
//			for (CellPin p : cp1) {
//				System.out.println("Pin " + p.getFullName() + ", connected to net " + p.getNet() + ", at bel " + p.getMappedBelPin());
//			}
//			PropertyList properties1 = c1.getProperties();
//			//Property equation1 = properties1.get("INIT");
//			//System.out.println(equation1.toString());
//			
//			System.out.println("-------");
//			for (CellPin p : cp2) {
//				System.out.println("Pin " + p.getFullName() + ", connected to net " + p.getNet() + ", at bel " + p.getMappedBelPin());
//			}
//			PropertyList properties2 = c2.getProperties();
			
			
			/*
			 * Code used to verify that inputs of RAMBs are not permuted
			 * 
			 */
			
			
			ArrayList<CellPin> cp1l = new ArrayList<CellPin>(cp1);
			ArrayList<CellPin> cp2l = new ArrayList<CellPin>(cp2);
			
			int s = cp1.size();
			if (s != cp2.size()) {
				System.out.println("Cell Pin sizes do not match");
			} else {
				for (int i=0; i < s; i++) {
					String cellpinA = cp1l.get(i).getFullName();
					String cellpinB = cp2l.get(i).getFullName();
					if ((moduleName + cellpinB).equals(cellpinA)) {
						// CellPins match up. Let's check connecting nets next
						CellNet cellnetA = cp1l.get(i).getNet();
						CellNet cellnetB = cp2l.get(i).getNet();
						if (cellnetA == null || cellnetB == null) {
							// I assume this means there's nothing connected to these pins, which is probably fine. (?) verify this with Dr. Hutchings
							// System.out.println("NULL cellnets " + cp1l.get(i).getFullName() + ", " + cp2l.get(i).getFullName());
						} else {
							String cnAname = cellnetA.getName();
							String cnBname = cellnetB.getName();
							
							if (cnAname.startsWith(moduleName)) {
								// System.out.println(cnAname);
								cnAname = cnAname.replaceFirst("^" + moduleName, "");
								// System.out.println(cnAname);
							} else if (cnAname.startsWith("clk")) {
								cnAname = "clk";
							}
							if (cnAname.equals(cnBname)) {
								// input nets match. Now let's check if the BelPins match
								
								BelPin bA = cp1l.get(i).getMappedBelPin();
								BelPin bB = cp2l.get(i).getMappedBelPin();
								if (bA == null || bB == null) {
									System.out.println("one of these is null: " + bA + ", " + bB);
									System.out.println("meaning the following CellPins do not map to a BelPin");
									System.out.println(cp1l.get(i).toString());
									System.out.println(cp2l.get(i).toString());
								} else {
									String bpA = bA.getName();
									String bpB = bB.getName();
									bpA = bpA.split("/")[bpA.split("/").length - 1];
									bpB = bpB.split("/")[bpB.split("/").length - 1];
									if (bpA.equals(bpB)) {
										int j = 0;
									} else {
										System.out.println("BelPins do not match: " + bpA + ", " + bpB);
									}
								}
							} else {
								System.out.println("mismatched nets: " + cnAname + ", " + cnBname);
							}
						}
						
					} else {
						System.out.println(cellpinA + " and " + cellpinB + " do not match");
					}
					
				}
			}
			
			//Property equation2 = properties2.get("INIT");
			//System.out.println(equation2.toString());
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
