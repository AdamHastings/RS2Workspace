package rscpviewer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import edu.byu.ece.rapidSmith.design.subsite.Cell;
import edu.byu.ece.rapidSmith.design.subsite.CellDesign;
import edu.byu.ece.rapidSmith.design.subsite.CellNet;
import edu.byu.ece.rapidSmith.interfaces.vivado.VivadoInterface;

public class Main {
	public static void main(String[] args) {
		
		// Filenames of designs you want to compare
//		String filename1 = "/home/adam/vivado_workspace/aes128_top/aes128_top.tcp";
//		String filename2 = "/home/adam/vivado_workspace/ooc_aes128/aes_128_0_gs.tcp";
//		
//		// Find the CellNets that are found in both designs 
//		ArrayList<Collection<CellNet>> intersection_list = Intersection.getCommonNets(filename1, filename2);
//		
//		// Create files to be used by GraphGrepSX
//		GraphGrepSX.createFile(intersection_list.get(0), filename1);
//		GraphGrepSX.createFile(intersection_list.get(1), filename2);
		
		exploreLUTPermutations.explore();
	}
}
